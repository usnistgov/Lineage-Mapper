/**
 * LineageVisualization.js
 * 
 * Contains Javascript code to allow colony lineage visualization with D3 library
 * 
 * @author Mylene Simon <mylene.simon@nist.gov>
 */

var Lineage = Lineage || {};

(function(lineage){
	
/* Data file names */
var lineageDataFileName = "lineage.csv";
var birthDataFileName = "birth.csv";
var deathDataFileName = "death.csv";

/* Data folders */
var replica1DataFolder = "data/R1/";
var replica2DataFolder = "data/R2/";
var replica3DataFolder = "data/R3/";

/* Set visualization area size and margins */
var margin = {top: 40, right: 40, bottom: 40, left: 40},
	width = 500 - margin.left - margin.right,
	height = 500 - margin.top - margin.bottom;

/* Select Cluster Dendrogram Layout from D3 and add custom separation function to avoid nodes overlapping */
var cluster = d3.layout.cluster()
				.separation(function(a, b) { return a.parent === b.parent ? 1 : .7; });

/* Select div and create the SVG in it */
var svg = d3.select("div.right-panel").append("svg");

var graph = svg.append("g")
 	.attr("id", "lineageGraph");
	//.attr("transform", "translate(" + margin.left + "," + margin.top + ")");

var zoom = d3.behavior.zoom();
svg.call(zoom//.scaleExtent([0.1, 100])
		  .on("zoom", zooming));

/* Colony which the user wants to see the lineage */ 
var colonyOfInterest = 1;

/* Time frames range */
var startTimeFrame = 0;
var endTimeFrame = 180;

/* Maximum depth between the colony of interest and the other colonies in the lineage tree */
var depth = 1;

/* Table of the computed lineage trees */
var trees = [];

/* Table of the colonies information */
var colonies = {};

/* Timer for ajaxRequest function */
var ajaxtimer = 0;	


/* 
 * FUNCTION getColoniesInformationAndComputeLineageTrees : 
 * 			Call the parse CSV file functions to get the colonies information 
 * 			and compute the lineage trees 
 */
lineage.getColoniesInformationAndComputeLineageTrees = function(replica) {
	
	var chosenReplicaFolder = "";
	switch(replica) {
		case 1 : chosenReplicaFolder = replica1DataFolder; break;
		case 2 : chosenReplicaFolder = replica2DataFolder; break;
		case 3 : chosenReplicaFolder = replica3DataFolder; break;
		default : console.log("Replica not found.");
	}
	
	queue(1)
	.defer(lineage.parseCSVBirthFile, chosenReplicaFolder + birthDataFileName)
	.defer(lineage.parseCSVDeathFile, chosenReplicaFolder + deathDataFileName)
	.defer(lineage.parseCSVLineageFile, chosenReplicaFolder + lineageDataFileName)
	.awaitAll(function(error, results) {
		  console.log("Initialisation done");
	});

};


/* 
 * FUNCTION parseCSVBirthFile : 
 * 			Parse CSV birth file and store colonies birth information in colonies table
 */
lineage.parseCSVBirthFile = function(birthFileName, callback) {
	
	console.log("Parsing " + birthFileName);
	
	/* Clear colonies table */
	for(var colonyId in colonies) {
		delete colonies[colonyId];
	}
	colonies = {};
	
	/* Parse CSV file */
	d3.text(birthFileName, function(datasetText) {
		
		// Get file rows
		var rows = d3.csv.parseRows(datasetText);
		
		// Delete CSV header row from data table
		rows.shift();
		
		// Store colony birth time frame in the colonies table
		rows.forEach(function(row) {
			colonies[row[0]] = { id : row[0], birth : +row[1] };
		});
		
		console.log("Parsing of " + birthFileName + " completed");
		return callback(null, 0);
	});
	
};


/* 
 * FUNCTION parseCSVDeathFile : 
 * 			Parse CSV death file and store colonies death information in colonies table 
 */
lineage.parseCSVDeathFile = function(deathFileName, callback) {
	
	console.log("Parsing " + deathFileName);
	
	/* Parse CSV file */
	d3.text(deathFileName, function(datasetText) {
		
		// Get file rows
		var rows = d3.csv.parseRows(datasetText);
		
		// Delete CSV header row from data table
		rows.shift();
		
		// Store colony death time frame in the colonies table
		rows.forEach(function(row) {
			colonies[row[0]].death = +row[1];
		});
		
		console.log("Parsing of " + deathFileName + " completed");
		return callback(null, 0);
	});
	
};


/* 
 * FUNCTION parseCSVLineageFile : 
 * 			Parse CSV file and compute the lineage trees 
 */
lineage.parseCSVLineageFile = function(lineageFileName, callback) {
	
	console.log("Parsing " + lineageFileName);
	
	/* Empty graph and reset original zoom level and position */
	emptyGraphArea();
	
	/* Set style class for selected tab (replica 1, 2 or 3) */
	if(lineageFileName == "data/R1/lineage.csv") {
		$("li#replica1tab").addClass("active");
		$("li#replica2tab").removeClass("active");
		$("li#replica3tab").removeClass("active");
	}
	else if(lineageFileName == "data/R2/lineage.csv") {
		$("li#replica1tab").removeClass("active");
		$("li#replica2tab").addClass("active");
		$("li#replica3tab").removeClass("active");
	}
	else {
		$("li#replica1tab").removeClass("active");
		$("li#replica2tab").removeClass("active");
		$("li#replica3tab").addClass("active");
	}
	
	/* Clear trees table */
	while(trees.length > 0) trees.pop();
	trees = [];
	
	/* Parse CSV file */
	d3.text(lineageFileName, function(datasetText) {
		
		// Get file rows
		var rows = d3.csv.parseRows(datasetText);

		// Delete CSV header row from data table
		rows.shift();
		
		// Create nodes for each unique source and target.
		rows.forEach(function(row) {
			// Parse the the line to get 
			// result colony and merged colonies nodes information
			// and create the nodes with their relations
			// /!\ D3 convention for nodes (one parent allowed, multiple children allowed):
			//     Parent of the node = result colony, 
			//     Children of the node = merged colonies
			var parent = colonies[row[1]];
			for(var i=2; i<row.length; ++i) {
				if(row[i] != "") {
					var child = colonies[row[i]];
					if (parent.children) parent.children.push(child);
					else parent.children = [child];
					// store 'children' (ie colonies before the fusion) in another var for backup and information display
					parent._children = parent.children;
					child.parent = parent;
				}
			}
		});
		
		// Extract all the root nodes and compute the layout to get the trees
		for(var id in colonies) {
			if(! colonies[id].parent) {
				trees.push(cluster.nodes(colonies[id]));
			}
		}
		
		console.log("Parsing of " + lineageFileName + " completed");
		
		lineage.ajaxRequest();
		
		return callback(null, 0);
		
	});

};


/* 
 * FUNCTION drawLineageTree : 
 * 			Draw the lineage for the colony of interest 
 */
function drawLineageTree() {
	
	/* Empty graph and reset original zoom level and position */
	emptyGraphArea();
	
	/* Find the colony of interest in the trees to know the tree to display */
	var selectedTree = -1;
	for(var i=0; i<trees.length && selectedTree == -1; ++i) {
		for(var j=0; j<trees[i].length && selectedTree == -1; ++j) {
			if(trees[i][j].id == colonyOfInterest) {
				selectedTree = i;
			}
		}
	}
	
	/* Tables to store the nodes and links of the lineage tree */
	var nodes = [];
	var links = [];
	
	/* If the colony is not found in the trees, no lineage information is available */
	if(selectedTree == -1) {
		console.log("Colony doesn't exist.");
	}
	/* Else draw the lineage tree */
	else {
	
		/* Extract the root node and compute the layout to get the lineage nodes */
		nodes = trees[selectedTree]; 
		// Restore the children table for each node with the _children backup table
		nodes.forEach(function(d) { if(d._children) d.children = d._children; });
		
		/* Store max depth for the tree, used if depth is not '*' */
		var maxDepth = 0;
		
		/* Get only the nodes that fit the depth defined by the user 
		 * (depth in the tree between the colony of interest and the other nodes of the tree) */
		if(depth != "*"){
			if(isNaN(depth)) {
				alert("The depth must be a number, or * if you want to see the whole tree, otherwise your chosen depth may be not taken into account.");
				console.log("User used incorrect format to enter the depth");
			}
			else {
				// Select the colony of interest in the tree
				var rightDepth = 0;
				var finaleColonyForThisDepth = colonies[colonyOfInterest];
				
				for(var i=0; i<depth && finaleColonyForThisDepth.parent; ++i) {
					finaleColonyForThisDepth = finaleColonyForThisDepth.parent;
					++rightDepth;
				}
				maxDepth = +depth + rightDepth; 
				nodes = cluster.nodes(finaleColonyForThisDepth);
				removeNodesAboveMaxDepth(nodes, maxDepth);
			}
		}
		
		/* Count initial colonies to adapt graph size */
		computeWidthAndHeight(nodes, width, height);
		
		svg.attr("width", width + margin.left + margin.right)
		.attr("height", height + margin.top + margin.bottom);
		
		/* Compute the cluster dendrogram layout to get the y positions of the nodes and links to display */
		cluster = cluster.size([height, width]);
		nodes = cluster.nodes(nodes[0]); 
		if(depth != "*" && !isNaN(depth)) removeNodesAboveMaxDepth(nodes, maxDepth);
		links = cluster.links(nodes);
		
		/* If the dendrogram is too big, adapt zoom level to fit it better in the screen */
		var divWidth = parseInt(d3.select("div.right-panel").style("width"));
		divWidth *= 0.9; // Use 90% of the div width for the scaling (because of the scrolling bars)
		var svgWidth = parseInt(svg.style("width"));
		var divHeight = parseInt(d3.select("div.right-panel").style("height"));
		divHeight *= 0.9; // Use 90% of the div height for the scaling (because of the scrolling bars)
		var svgHeight = parseInt(svg.style("height"));
		if(svgWidth > divWidth || svgHeight > divHeight) {
			var scale = Math.min(divWidth / svgWidth, divHeight / svgHeight);
			graph.attr("transform", "translate(" + margin.left + "," + 0 + ")" +
		    " scale(" + scale + ")");
			zoom.scale(scale);
			zoom.translate([margin.left, 0]);
		}
		
		/* Change computed x positions to the time positions 
		 * ! Here, the x position (regarding the time axis) of the node is d.y,
		 *   because x and y positions are inverted to have the root node on the right and not 
		 *   on the top (dendrogram default layout is with root node on the top, and child nodes below)
		 */
		var timeScaleCoefficient = Math.floor(width / (endTimeFrame - startTimeFrame));
		nodes.forEach(function(d) { 
			d.y = +(d.birth) * timeScaleCoefficient; 
			d.radius = 18; 
		});
	
		/* Define a quadtree and call the collide function to manage collisions between nodes */ 
		var q = d3.geom.quadtree(nodes),
	    i = 0,
	    n = nodes.length;
		
		while (++i < n) {
		    q.visit(collide(nodes[i]));
		  }
		
		/* Create the link lines between the nodes */
		graph.selectAll(".link")
		  .data(links)
		.enter().append("path")
		  .attr("class", "link")
		  .attr("d", elbow);
		
		/* Add extra links for the colonies that do not merge between their birth date (the node) and their death date */
		nodes.forEach(function(n) {
			if(! n.parent) {
				graph.append("path")
				  .attr("class", "link")
				  .attr("d", "M" + n.y + "," + n.x
					      + "V" + n.x + "H" + n.death * timeScaleCoefficient);
			}
		});
		
		/* Create the nodes and add mouse events on them */
		var node = graph.selectAll(".node")
			.data(nodes)
		  .enter().append("g")
		  .attr("class", "node")
		  .attr('id', function(d) { return 'node' + d.id; })
		  .on("mouseover", onMouseOver)
		  .on("mouseout", onMouseOut)
		  .on("click", click);
		
		/* Create the node circles */
		node.append("circle")
		  .attr("r", 18)
		  .attr("cx", function(d) { return d.y; })
		  .attr("cy", function(d) { return d.x; });
		
		/* Create the node texts */
		node.append("text")
			.attr("transform", function(d) { return "translate(" + d.y + "," + d.x + ")"; })
			.attr("y", ".3em")
	      .text(function(d) { return d.id; });
		
		/* Highlight colony of interest node */
		d3.select("#node" + colonyOfInterest).attr("class", "nodeOfInterest");
		
		/* Set X axis (time axis) */
		var axisScale = d3.scale.linear()
		.domain([startTimeFrame, endTimeFrame])
		.range([0, (endTimeFrame - startTimeFrame) * timeScaleCoefficient]);
	
		var xAxis = d3.svg.axis()
		.scale(axisScale)
		.orient("bottom");
		
		/* Create the X axis (time axis) */
		graph.append("g")
		.attr("class", "x axis")
		.attr("transform", "translate(0," + (height+ margin.bottom/2) + ")")
		.call(xAxis);
		
		graph.append("text")
	    .attr("class", "x axis")
	    .attr("text-anchor", "end")
	    .attr("x", width)
	    .attr("y", height + margin.bottom + margin.bottom/2)
	    .text("Time frames");
	
	}
	
	/* Function elbow :
	 * 			Compute the links lines
	 */
	function elbow(d, i) {
		  return "M" + d.source.y + "," + d.source.x
		      + "V" + d.target.x + "H" + d.target.y;
	}
	
	/* Function removeNodesAboveMaxDepth :
	 * 			Remove nodes that are not is the depth range chosen by the user
	 */
	function removeNodesAboveMaxDepth(nodesTable, maximumDepth) {
		nodesTable.sort(function(a, b) { return a.depth - b.depth; });
		while(nodesTable[nodesTable.length - 1].depth > maximumDepth) {
			nodesTable.pop();
		}
		for(var i=nodesTable.length - 1; i>=0 && nodesTable[i].depth == maximumDepth; --i) {
			if(nodesTable[i].children){
				nodesTable[i]._children = nodesTable[i].children;
				nodesTable[i].children = null;
			}
		}
	}
	
	/* Function computeWidthAndHeight :
	 * 			Compute the height and height of the tree regarding the number of nodes
	 */
	function computeWidthAndHeight(nodesTable, treeWidth, treeHeight) {
		var nodeBirthClasses = {};
		nodesTable.forEach(function(d) {
			nodeBirthClasses[Math.floor(d.birth / 20)] ? nodeBirthClasses[Math.floor(d.birth / 20)] ++ : nodeBirthClasses[Math.floor(d.birth / 20)] = 1; 
		});
		var max = 1;
		for(var key in nodeBirthClasses) {
			if(nodeBirthClasses[key] > max) max=nodeBirthClasses[key];
		}
		height = max * 60;
		if(height < 500) {
			height = 500;
		}
		width = height * 1.5;
	}
}


/* 
 * FUNCTION emptyGraphArea : 
 * 			Empty graph and reset original zoom level and position 
 */
function emptyGraphArea() {
	
	$("#lineageGraph").empty();
	graph.attr("transform", "translate(" + margin.left + "," + 0 + ")" +
    " scale(1)");
	zoom.scale(1);
	zoom.translate([margin.left, 0]);
	window.scrollTo(0,0);
	$("div.right-panel").scrollTop(0);
}


/* 
 * FUNCTION onMouseOver : 
 * 			Enlarge on which is the mouse 
 */
function onMouseOver(d)
{
	if(d.id == colonyOfInterest) {
		d3.select(this).attr("class", "nodeOfInterestSelected");
	}
	else {
		d3.select(this).attr("class", "nodeSelected");
	}
    d3.select(this).select("circle").attr("r", 30);
    d3.select(this).select("text").attr("y", ".3em");
}


/* 
 * FUNCTION onMouseOut : 
 * 			Reset the node in its original state 
 */
function onMouseOut(d)
{
	if(d.id == colonyOfInterest) {
		d3.select(this).attr("class", "nodeOfInterest");
	}
	else {
		d3.select(this).attr("class", "node");
	}
	d3.select(this).select("circle").attr("r", 18);
    d3.select(this).select("text").attr("y", ".3em");
}


/* 
 * FUNCTION click  : 
 * 			Display information about the clicked node 
 */
function click(d,e) {
	$('#infobox')
    .dialog({
        width:200,
        height:330,
        modal: true,
        open: function() {
        	var colonyInfo = "";
        	var birthTime = "<p>Birth time: " + d.birth +"</p>";
        	var deathTime = "<p>Death time: " + d.death +"</p>";
        	var parentsTable = 
        	"<table width=150><tbody>" +
        	"	<tr>" +
        	"		<th>Parent colonies</td>" +
        	"	</tr>";
        	var childTable = 
        		"<table width=150><tbody>" +
            	"	<tr>" +
            	"		<th>Child colony</td>" +
            	"	</tr>";
        	if(d._children) {
            	for(var i=0; i<d._children.length; ++i) {
                	parentsTable = parentsTable + 
                	"	<tr>" +
                	"		<td>" +
                	"			<a href='javascript:void(0)' onclick='Lineage.colonyClickInsideInfobox(" + d._children[i].id + ");'>" + 
                					d._children[i].id + 
                	"			</a>" +
                	"		</td>" +
                	"	</tr>";
                }
            }
            else {
            	parentsTable = parentsTable +
            		"	<tr>" +
                	"		<td>" + "No parents" + "</td>" +
                	"	</tr>";
            }
        	parentsTable = parentsTable +"</tbody></table><br/>";
        	if(d.parent) {
        		childTable = childTable +
                	"	<tr>" +
                	"		<td>" + 
			        "			<a href='javascript:void(0)' onclick='Lineage.colonyClickInsideInfobox(" + d.parent.id + ");'>" + 
									d.parent.id + 
					"			</a>" +
                	"		</td>" +
                	"	</tr>";
        	}
        	else {
        		childTable = childTable +
            	"	<tr>" +
            	"		<td>" + "No child" + "</td>" +
            	"	</tr>";
            }
        	childTable = childTable + "</tbody></table><br/>";
        	
            colonyInfo = birthTime + deathTime + parentsTable + childTable;
            $(this).html((colonyInfo)
            		);
          },
        title: "Colony " + d.id
    }); 
}


/* 
 * FUNCTION zooming :
 *  		Allow zoom on the graph 
 */
function zooming() {
    var scale = d3.event.scale,
        translation = d3.event.translate,
        tbound = -height * scale,
        bbound = height * scale,
        lbound = (-width + margin.left) * scale,
        rbound = (width - margin.right) * scale;
    // limit translation to thresholds
    translation = [
        Math.max(Math.min(translation[0], rbound), lbound),
        Math.max(Math.min(translation[1], bbound), tbound)
    ];
    
    d3.select("g")
        .attr("transform", "translate(" + translation + ")" +
              " scale(" + scale + ")");
}


/* 
 * FUNCTION ajaxRequest : 
 * 			Get colony number entered by the user and call updateColonyOfInterestAndLineageTree function 
 */
lineage.ajaxRequest = function() {

	console.log("clear timeout");
	ajaxtimer && clearTimeout(ajaxtimer);

	ajaxtimer = setTimeout(
			function() {
				colonyOfInterest = $("#lineageColony").val();
				depth = $("#lineageDepth").val();
				lineage.updateLineageTree();
			},
			1000);
};


/* 
 * FUNCTION updateLineageTree :
 * 			Call drawLineageTree function to update the tree
 */
lineage.updateLineageTree = function() {
	console.log("Update lineage tree : colony " + colonyOfInterest + ", depth : " + depth);
	drawLineageTree();
}; 


/* 
 * FUNCTION colonyClickInsideInfobox:
 * 			Close the colony infobox and reload the lineage tree with the selected colony
 */
lineage.colonyClickInsideInfobox = function(selectedColony) {
	$('#infobox').dialog('close');
	colonyOfInterest = selectedColony;
	$("#lineageColony").val(selectedColony);
	lineage.updateLineageTree();
};


/* 
 * FUNCTION collide :
 * 			Detect collisions between nodes and reorganize the colliding nodes in the aim to see them entirely 
 */
function collide(node) {
	var r = node.radius + 18,
		nx1 = node.x - r,
		nx2 = node.x + r,
		ny1 = node.y - r,
		ny2 = node.y + r;
	
	return function(quad, x1, y1, x2, y2) {
    if (quad.point && (quad.point !== node)) {
    	var x = node.x - quad.point.x,
    		y = node.y - quad.point.y,
    		l = Math.sqrt(x * x + y * y),
    		r = node.radius + quad.point.radius + 1;
    	
    	if (l < r) {
    		l = (l - r) / l * .5;
    		node.x -= x *= l;
    		node.y -= y *= l;
    		quad.point.x += x;
    		quad.point.y += y;
    	}
    }
	return x1 > nx2
	    || x2 < nx1
	    || y1 > ny2
	    || y2 < ny1;
	};
}

}(Lineage));
