// NIST-developed software is provided by NIST as a public service. You may use, copy and distribute copies of the software in any medium, provided that you keep intact this entire notice. You may improve, modify and create derivative works of the software or any portion of the software, and you may copy and distribute such modifications or works. Modified works should carry a notice stating that you changed the software and should note the date and nature of any such change. Please explicitly acknowledge the National Institute of Standards and Technology as the source of the software.

// NIST-developed software is expressly provided "AS IS." NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED, IN FACT OR ARISING BY OPERATION OF LAW, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT AND DATA ACCURACY. NIST NEITHER REPRESENTS NOR WARRANTS THAT THE OPERATION OF THE SOFTWARE WILL BE UNINTERRUPTED OR ERROR-FREE, OR THAT ANY DEFECTS WILL BE CORRECTED. NIST DOES NOT WARRANT OR MAKE ANY REPRESENTATIONS REGARDING THE USE OF THE SOFTWARE OR THE RESULTS THEREOF, INCLUDING BUT NOT LIMITED TO THE CORRECTNESS, ACCURACY, RELIABILITY, OR USEFULNESS OF THE SOFTWARE.

// You are solely responsible for determining the appropriateness of using and distributing the software and you assume all risks associated with its use, including but not limited to the risks and costs of program errors, compliance with applicable laws, damage to or loss of data, programs or equipment, and the unavailability or interruption of operation. This software is not intended to be used in any situation where a failure could cause risk of injury or damage to property. The software developed by NIST employees is not subject to copyright protection within the United States.

/**
 * LineageVisualization.js
 *
 * Contains Javascript code to allow colony lineage visualization with D3 library
 *
 * @author Mylene Simon <mylene.simon@nist.gov>
 */

var Lineage = Lineage || {};

(function (lineage) {

    var treeTypeId;
    /* 1 = division 2 = fusion */

    /* Set visualization area size and margins */
    var margin = {top: 40, right: 40, bottom: 40, left: 40},
        width = 500 - margin.left - margin.right,
        height = 500 - margin.top - margin.bottom;

    /* Select Cluster Dendrogram Layout from D3 and add custom separation function to avoid nodes overlapping */
    var cluster = d3.layout.cluster()
        .separation(function (a, b) {
            return a.parent === b.parent ? 1 : .7;
        });

    /* Select div and create the SVG in it */
    var svg = d3.select("div.right-panel").append("svg");

    var graph = svg.append("g")
        .attr("id", "lineageGraph");
    //.attr("transform", "translate(" + margin.left + "," + margin.top + ")");

    var zoom = d3.behavior.zoom();
    svg.call(zoom//.scaleExtent([0.1, 100])
        .on("zoom", zooming));


    /* Colony which the user wants to see the lineage */
    var cellOfInterest = 1;

    /* Time frames range */
    var startTimeFrame = 0;
    var endTimeFrame = 10;
    var radius = 16;
    var treeRoot = []; // root for the full tree (if the user wants to plot cell=*

    /* Maximum depth between the colony of interest and the other cells in the lineage tree */
    var depth = 5;

    /* Table of the computed lineage trees */
    var trees = [];

    /* Table of the cells information */
    var cells = {};

    /* Timer for ajaxRequest function */
    var ajaxtimer = 0;

    var scrollbarWidth = getScrollbarWidth();

    /*
     * FUNCTION getCellsInformationAndComputeLineageTrees :
     * 			Call the parse CSV file functions to get the cells information
     * 			and compute the lineage trees
     */
    lineage.getCellsInformationAndComputeLineageTrees = function (replica) {

        treeTypeId = replica;

        /* Set style class for selected tab (replica 1, 2 or 3) */
        if (replica == 1) {
            $("li#replica1tab").addClass("active");
            $("li#replica2tab").removeClass("active");
        }
        else if (replica == 2) {
            $("li#replica1tab").removeClass("active");
            $("li#replica2tab").addClass("active");
        }
        else {
            $("li#replica1tab").removeClass("active");
            $("li#replica2tab").removeClass("active");
        }

        if (replica == 1) {
            queue(1)
                .defer(lineage.parseCSVBirthDeathFile)
                .defer(lineage.parseCSVDivisionFile)
                .awaitAll(function (error, results) {
                    console.log("Initialisation done");
                });
        }
        else if (replica == 2) {
            queue(1)
                .defer(lineage.parseCSVBirthDeathFile)
                .defer(lineage.parseCSVFusionFile)
                .awaitAll(function (error, results) {
                    console.log("Initialisation done");
                });
        }
    };


    /*
     * FUNCTION parseCSVBirthDeathFile :
     * 			Parse CSV birth file and store cells birth information in cells table
     */

    lineage.parseCSVBirthDeathFile = function (callback) {

        console.log("Parsing birth death data");

        // Clear cells table
        for (var cellId in cells) {
            delete cells[cellId];
        }
        cells = {};

        // Parse string data
        // Get file rows
        var rows = d3.csv.parseRows(birthDeathText);

        // Delete CSV header row from data table
//        rows.shift();

        // Store colony birth time frame in the cells table
        rows.forEach(function (row) {
            cells[row[0]] = { id: row[0], birth: +row[1], death: +row[2] };

            // dynamically determine end time frame by looking at cell death frames
            var deathFrame = parseInt(row[2]);
            if (deathFrame > endTimeFrame) {
                endTimeFrame = deathFrame;
            }
        });

        console.log("Parsing of birth and death data completed");
        return callback(null, 0);
    };


    /*
     * FUNCTION parseCSVDivisionFile :
     * 			Parse CSV file and compute the lineage trees
     */
    lineage.parseCSVDivisionFile = function (callback) {

        console.log("Parsing division data");

        /* Empty graph and reset original zoom level and position */
        emptyGraphArea();

        /* Clear trees table */
        while (trees.length > 0) trees.pop();
        trees = [];


        // Get file rows
        var rows = d3.csv.parseRows(divisionText);

        // Delete CSV header row from data table
//        rows.shift();

        // Create nodes for each unique source and target.
        rows.forEach(function (row) {
            // Parse the the line to get
            // result colony and merged cells nodes information
            // and create the nodes with their relations
            // /!\ D3 convention for nodes (one parent allowed, multiple children allowed):
            //     Parent of the node = result colony,
            //     Children of the node = merged cells
            var parent = cells[row[1]];
            for (var i = 2; i < row.length; i++) {
                if (row[i] != "") {
                    var child = cells[row[i]];
                    if (parent.children) parent.children.push(child);
                    else parent.children = [child];
                    // store 'children' (ie cells before the fusion) in another var for backup and information display
                    parent._children = parent.children;
                    child.parent = parent;
                }
            }
        });


        // Extract all the root nodes and compute the layout to get the trees
        for (var id in cells) {
            if (!cells[id].parent) {
                trees.push(cluster.nodes(cells[id]));
            }
        }

        treeRoot = {id:"root",birth:0,death:0};
        // add a special ancestor node to be parent to all the cells
        for (var id in cells) {
            if (!cells[id].parent) {
                var child = cells[id];
                if (treeRoot.children) treeRoot.children.push(child);
                else treeRoot.children = [child];
            }
        }
        treeRoot._children = treeRoot.children;
        trees.push(cluster.nodes(treeRoot));


        console.log("Parsing of division data complete");
        lineage.ajaxRequest();
        return callback(null, 0);

    };


    /*
     * FUNCTION parseCSVDivisionFile :
     * 			Parse CSV file and compute the lineage trees
     */
    lineage.parseCSVFusionFile = function (callback) {

        console.log("Parsing fusion data");

        /* Empty graph and reset original zoom level and position */
        emptyGraphArea();

        /* Clear trees table */
        while (trees.length > 0) trees.pop();
        trees = [];


        // Get file rows
        var rows = d3.csv.parseRows(fusionText);

        // Delete CSV header row from data table
//        rows.shift();

        // Create nodes for each unique source and target.
        rows.forEach(function (row) {
            // Parse the the line to get
            // result colony and merged cells nodes information
            // and create the nodes with their relations
            // /!\ D3 convention for nodes (one parent allowed, multiple children allowed):
            //     Parent of the node = result colony,
            //     Children of the node = merged cells
            var parent = cells[row[1]];
            for (var i = 2; i < row.length; i++) {
                if (row[i] != "" && row[i] != "null") {
                    var child = cells[row[i]];
                    if (parent.children) parent.children.push(child);
                    else parent.children = [child];
                    // store 'children' (ie cells before the fusion) in another var for backup and information display
                    parent._children = parent.children;
                    try {
                        child.parent = parent;
                    }catch(err){
                        console.log("err");
                    }
                }
            }
        });

        // Extract all the root nodes and compute the layout to get the trees
        for (var id in cells) {
            if (!cells[id].parent) {
                trees.push(cluster.nodes(cells[id]));
            }
        }

        treeRoot = {id:"root",birth:0,death:0};
        // add a special ancestor node to be parent to all the cells
        for (var id in cells) {
            if (!cells[id].parent) {
                var child = cells[id];
                if (treeRoot.children) treeRoot.children.push(child);
                else treeRoot.children = [child];
            }
        }
        treeRoot._children = treeRoot.children;
        trees.push(cluster.nodes(treeRoot));

        console.log("Parsing of fusion data complete");
        lineage.ajaxRequest();
        return callback(null, 0);

    };


    /*
     * FUNCTION drawLineageTree :
     * 			Draw the lineage for the colony of interest
     */
    function drawLineageTree() {

        /* Empty graph and reset original zoom level and position */
        emptyGraphArea();

        var selectedTree = -1;
        if(cellOfInterest == "*") {
            cellOfInterest = "root";
        }
        /* Find the colony of interest in the trees to know the tree to display */
        for (var i = 0; i < trees.length && selectedTree == -1; ++i) {
            for (var j = 0; j < trees[i].length && selectedTree == -1; ++j) {
                if (trees[i][j].id == cellOfInterest) {
                    selectedTree = i;
                }
            }
        }


        /* Tables to store the nodes and links of the lineage tree */
        var nodes = [];
        var links = [];

        /* If the colony is not found in the trees, no lineage information is available */
        if (selectedTree == -1) {
            console.log("Cell doesn't exist.");
        }else{
            /* Else draw the lineage tree */
            var divWidth = parseInt(d3.select("div.right-panel").style("width"));
            divWidth -= scrollbarWidth + margin.left + margin.right;
            var divHeight = parseInt(d3.select("div.right-panel").style("height"));
            divHeight -= scrollbarWidth + margin.top + margin.bottom;

            /* Extract the root node and compute the layout to get the lineage nodes */
//            if(cellOfInterest == "root") {
//                nodes = trees[trees.length-1];
//            }else {
//                nodes = trees[selectedTree];
//            }
            nodes = trees[selectedTree];
            // Restore the children table for each node with the _children backup table
            nodes.forEach(function (d) {
                if (d._children) d.children = d._children;
            });

            /* Store max depth for the tree, used if depth is not '*' */
            var maxDepth = 0;

            /* Get only the nodes that fit the depth defined by the user
             * (depth in the tree between the colony of interest and the other nodes of the tree) */
            if (depth != "*") {
                if (isNaN(depth)) {
                    alert("The depth must be a number, or * if you want to see the whole tree, otherwise your chosen depth may be not taken into account.");
                    console.log("User used incorrect format to enter the depth");
                }
                else {
                    // Select the colony of interest in the tree
                    if (cellOfInterest == "root") {
                        var finaleColonyForThisDepth = "root";
                        maxDepth = depth;
                    } else {
                        var rightDepth = 0;
                        var finaleColonyForThisDepth = cells[cellOfInterest];
                        for (var i = 0; i < depth && finaleColonyForThisDepth.parent.id != "root"; ++i) {
                            finaleColonyForThisDepth = finaleColonyForThisDepth.parent;
                            ++rightDepth;
                        }
                        maxDepth = +depth + rightDepth;
                        nodes = cluster.nodes(finaleColonyForThisDepth);
                    }

                    removeNodesAboveMaxDepth(nodes, maxDepth);
                }
            }

            /* Count initial cells to adapt graph size */
            computeWidthAndHeight(nodes, width, height);

            svg.attr("width", width + margin.left + margin.right)
                .attr("height", height + margin.top + margin.bottom);

            /* Compute the cluster dendrogram layout to get the y positions of the nodes and links to display */
            cluster = cluster.size([height, width]);
            nodes = cluster.nodes(nodes[0]);
            if (depth != "*" && !isNaN(depth)) removeNodesAboveMaxDepth(nodes, maxDepth);
            links = cluster.links(nodes);

            /* If the dendrogram is too big, adapt zoom level to fit it better in the screen */
            var svgWidth = parseInt(svg.style("width"));
            var svgHeight = parseInt(svg.style("height"));
            if (svgWidth > divWidth || svgHeight > divHeight) {
                var scale = Math.max(divWidth / svgWidth, divHeight / svgHeight);
                graph.attr("transform", "translate(" + margin.left + "," + 0 + ")" +
                    " scale(" + scale + ")");
                zoom.scale(scale);
                zoom.translate([margin.left, 0]);
                svg.attr("width", Math.floor(width * scale) + margin.left + margin.right)
                    .attr("height", Math.floor(height * scale) + margin.top + margin.bottom);
            }

            /* Change computed x positions to the time positions
             * ! Here, the x position (regarding the time axis) of the node is d.y,
             *   because x and y positions are inverted to have the root node on the right and not
             *   on the top (dendrogram default layout is with root node on the top, and child nodes below)
             */
            var timeScaleCoefficient = 4 + Math.floor(width / (endTimeFrame - startTimeFrame));
            nodes.forEach(function (d) {
                d.y = +(d.birth) * timeScaleCoefficient;
                d.radius = radius;
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

            /* Add extra links for the cells that do not merge between their birth date (the node) and their death date */
            nodes.forEach(function (n) {
                if(treeTypeId == 1) {
                    // Division
                    if (!n.children || n.children.id == "root") {
                        graph.append("path")
                            .attr("class", "link")
                            .attr("d", "M" + n.y + "," + n.x
                                + "V" + n.x + "H" + n.death * timeScaleCoefficient);
                    }
                }else{
                    // Fusion
                    if (!n.parent || n.parent.id == "root") {
                        graph.append("path")
                            .attr("class", "link")
                            .attr("d", "M" + n.y + "," + n.x
                                + "V" + n.x + "H" + n.death * timeScaleCoefficient);
                    }
                }
            });

            /* Create the nodes and add mouse events on them */
            var node = graph.selectAll(".node")
                .data(nodes)
                .enter().append("g")
                .attr("class", "node")
                .attr('id', function (d) {
                    return 'node' + d.id;
                })
                .on("mouseover", onMouseOver)
                .on("mouseout", onMouseOut)
                .on("click", click);

            /* Create the node circles */
            node.append("circle")
                .attr("r", radius)
                .attr("cx", function (d) {
                    return d.y;
                })
                .attr("cy", function (d) {
                    return d.x;
                });



            /* Create the node texts */
            node.append("text")
                .attr("transform", function (d) {
                    return "translate(" + d.y + "," + d.x + ")";
                })
                .attr("y", ".3em")
                .text(function (d) {
                    return d.id;
                });

            /* Highlight colony of interest node */
            d3.select("#node" + cellOfInterest).attr("class", "nodeOfInterest");

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
                .attr("transform", "translate(0," + (height + margin.bottom / 2) + ")")
                .call(xAxis);

            graph.append("text")
                .attr("class", "x axis")
                .attr("text-anchor", "end")
                .attr("x", width)
                .attr("y", height + margin.bottom + margin.bottom / 2)
                .text("Time frames");

        }

        /* Function elbow :
         * 			Compute the links lines
         */
        function elbow(d) {

            if(d.source.id =="root" || d.target.id == "root") {
                return;
            }
            if(treeTypeId == 1) { /* division */
                return "M" + d.source.y + "," + d.source.x
                    + "H" + d.target.y + "V" + d.target.x;
            }else{  /* fusion */
                return "M" + d.source.y + "," + d.source.x
                     + "V" + d.target.x + "H" + d.target.y;
            }
        }

        /* Function removeNodesAboveMaxDepth :
         * 			Remove nodes that are not is the depth range chosen by the user
         */
        function removeNodesAboveMaxDepth(nodesTable, maximumDepth) {
            nodesTable.sort(function (a, b) {
                return a.depth - b.depth;
            });
            while (nodesTable[nodesTable.length - 1].depth > maximumDepth) {
                nodesTable.pop();
            }
            for (var i = nodesTable.length - 1; i >= 0 && nodesTable[i].depth == maximumDepth; --i) {
                if (nodesTable[i].children) {
                    nodesTable[i]._children = nodesTable[i].children;
                    nodesTable[i].children = null;
                }
            }
        }

        /* Function computeWidthAndHeight :
         * Compute the width and height of the tree regarding the number of nodes
         */
        function computeWidthAndHeight(nodesTable) {
            var nodeBirthClasses = {};
            nodesTable.forEach(function (d) {
                nodeBirthClasses[Math.floor(d.birth / 20)] ?
                    nodeBirthClasses[Math.floor(d.birth / 20)]++ :
                    nodeBirthClasses[Math.floor(d.birth / 20)] = 1;
            });
            var max = 1;
            for (var key in nodeBirthClasses) {
                if (nodeBirthClasses[key] > max) max = nodeBirthClasses[key];
            }
            height = max * 60;
            if (height < 500) {
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
        window.scrollTo(0, 0);
        $("div.right-panel").scrollTop(0);
    }


    /*
     * FUNCTION onMouseOver :
     * 			Enlarge on which is the mouse
     */
    function onMouseOver(d) {
        if (d.id == cellOfInterest) {
            d3.select(this).attr("class", "nodeOfInterestSelected");
        }
        else {
            d3.select(this).attr("class", "nodeSelected");
        }
        d3.select(this).select("circle").attr("r", 2*radius);
        d3.select(this).select("text").attr("y", ".3em");
    }


    /*
     * FUNCTION onMouseOut :
     * 			Reset the node in its original state
     */
    function onMouseOut(d) {
        if (d.id == cellOfInterest) {
            d3.select(this).attr("class", "nodeOfInterest");
        }
        else {
            d3.select(this).attr("class", "node");
        }
        d3.select(this).select("circle").attr("r", radius);
        d3.select(this).select("text").attr("y", ".3em");
    }


    /*
     * FUNCTION click  :
     * 			Display information about the clicked node
     */
    function click(d, e) {

        $('#infobox')
            .dialog({
                width: 250,
                height: 330,
                modal: true,
                open: function () {
                    var birthTime = "<p>Birth Frame: " + d.birth + "</p>";
                    var deathTime = "<p>Death Frame: " + d.death + "</p>";
                    var parentsTable =
                        "<table width=150><tbody>" +
                        "	<tr>" +
                        "		<th>Parent cells</td>" +
                        "	</tr>";
                    var childTable =
                        "<table width=150><tbody>" +
                        "	<tr>" +
                        "		<th>Child Cells</td>" +
                        "	</tr>";
                    if(treeTypeId == 1) {
                        // Division
                        if (d._children) {
                            for (var i = 0; i < d._children.length; ++i) {
                                childTable = childTable +
                                    "	<tr>" +
                                    "		<td>" +
                                    "			<a href='javascript:void(0)' onclick='Lineage.colonyClickInsideInfobox(" + d._children[i].id + ");'>" +
                                    d._children[i].id +
                                    "			</a>" +
                                    "		</td>" +
                                    "	</tr>";
                            }
                        }else{
                            childTable = childTable +
                                "	<tr>" +
                                "		<td>" + "No children" + "</td>" +
                                "	</tr>";
                        }
                        childTable = childTable + "</tbody></table><br/>";
                        if (d.parent) {
                            parentsTable = parentsTable +
                                "	<tr>" +
                                "		<td>" +
                                "			<a href='javascript:void(0)' onclick='Lineage.colonyClickInsideInfobox(" + d.parent.id + ");'>" +
                                d.parent.id +
                                "			</a>" +
                                "		</td>" +
                                "	</tr>";
                        }
                        else {
                            parentsTable = parentsTable +
                                "	<tr>" +
                                "		<td>" + "No parents" + "</td>" +
                                "	</tr>";
                        }
                        parentsTable = parentsTable + "</tbody></table><br/>";

                    }else{
                        // Fusion
                        if (d._children) {
                            for (var i = 0; i < d._children.length; ++i) {
                                parentsTable = parentsTable +
                                    "	<tr>" +
                                    "		<td>" +
                                    "			<a href='javascript:void(0)' onclick='Lineage.colonyClickInsideInfobox(" + d._children[i].id + ");'>" +
                                    d._children[i].id +
                                    "			</a>" +
                                    "		</td>" +
                                    "	</tr>";
                            }
                        }else{
                            parentsTable = parentsTable +
                                "	<tr>" +
                                "		<td>" + "No parents" + "</td>" +
                                "	</tr>";
                        }
                        parentsTable = parentsTable + "</tbody></table><br/>";
                        if (d.parent) {
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
                                "		<td>" + "No children" + "</td>" +
                                "	</tr>";
                        }
                        childTable = childTable + "</tbody></table><br/>";
                    }

                    colonyInfo = birthTime + deathTime + parentsTable + childTable;
                    $(this).html((colonyInfo)
                    );
                },
                title: "Cell ID: " + d.id
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
    lineage.ajaxRequest = function () {

        console.log("clear timeout");
        ajaxtimer && clearTimeout(ajaxtimer);

        ajaxtimer = setTimeout(
            function () {
                cellOfInterest = $("#lineageCell").val();
                depth = $("#lineageDepth").val();
                lineage.updateLineageTree();
            },
            1000);
    };


    /*
     * FUNCTION updateLineageTree :
     * 			Call drawLineageTree function to update the tree
     */
    lineage.updateLineageTree = function () {
        console.log("Update lineage tree : colony " + cellOfInterest + ", depth : " + depth);
        drawLineageTree();
    };


    /*
     * FUNCTION colonyClickInsideInfobox:
     * 			Close the colony infobox and reload the lineage tree with the selected colony
     */
    lineage.colonyClickInsideInfobox = function (selectedColony) {
        $('#infobox').dialog('close');
        cellOfInterest = selectedColony;
        $("#lineageCell").val(selectedColony);
        lineage.updateLineageTree();
    };


    /*
     * FUNCTION collide :
     * 			Detect collisions between nodes and reorganize the colliding nodes in the aim to see them entirely
     */
    function collide(node) {
        var r = node.radius + 2*radius,
            nx1 = node.x - r,
            nx2 = node.x + r,
            ny1 = node.y - r,
            ny2 = node.y + r;

        return function (quad, x1, y1, x2, y2) {

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

    /**
     * Taken from http://stackoverflow.com/questions/13382516/getting-scroll-bar-width-using-javascript
     * @returns {Number} Width in pixels
     */
    function getScrollbarWidth() {
        var outer = document.createElement("div");
        outer.style.visibility = "hidden";
        outer.style.width = "100px";
        outer.style.msOverflowStyle = "scrollbar"; // needed for WinJS apps

        document.body.appendChild(outer);

        var widthNoScroll = outer.offsetWidth;
        // force scrollbars
        outer.style.overflow = "scroll";

        // add innerdiv
        var inner = document.createElement("div");
        inner.style.width = "100%";
        outer.appendChild(inner);

        var widthWithScroll = inner.offsetWidth;

        // remove divs
        outer.parentNode.removeChild(outer);

        return widthNoScroll - widthWithScroll;
    }

}(Lineage));
