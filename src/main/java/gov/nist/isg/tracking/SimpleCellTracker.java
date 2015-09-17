// Disclaimer: IMPORTANT: This software was developed at the National
// Institute of Standards and Technology by employees of the Federal
// Government in the course of their official duties. Pursuant to
// title 17 Section 105 of the United States Code this software is not
// subject to copyright protection and is in the public domain. This
// is an experimental system. NIST assumes no responsibility
// whatsoever for its use by other parties, and makes no guarantees,
// expressed or implied, about its quality, reliability, or any other
// characteristic. We would appreciate acknowledgement if the software
// is used. This software can be redistributed and/or modified freely
// provided that any derivative works bear some notice that they are
// derived from it, and any modified versions bear some notice that
// they have been modified.


package main.java.gov.nist.isg.tracking;


import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.VirtualStack;
import ij.io.FileSaver;
import main.java.gov.nist.isg.tracking.app.TrackingAppParams;
import main.java.gov.nist.isg.tracking.lib.Cell;
import main.java.gov.nist.isg.tracking.lib.ConnectedComponents;
import main.java.gov.nist.isg.tracking.lib.DistanceTransform;
import main.java.gov.nist.isg.tracking.lib.ImageFrame;
import main.java.gov.nist.isg.tracking.lib.ImageTile;
import main.java.gov.nist.isg.tracking.lib.Log;
import main.java.gov.nist.isg.tracking.lib.Matrix2D;


public class SimpleCellTracker implements Runnable {

  private static final boolean limitDivisionToTwoDaughters = true;

  protected ImageStack inputStack = null;
  protected ImageStack outputStack = null;
  private double maxCentroidsDistance = 150;
  private int cellSizeThreshold = 200;
  private double weightSize = 0.2;
  private double weightCentroids = 0.5;
  private double weightOverlap = 1.0;
  private double fusionOverlapThreshold = 0.2;
  private double divisionOverlapThreshold = 0.2;
  private double daughterSizeSimilarity = 0.5;
  private double daughterAspectRatioSimilarity = 0.7;
  private double motherCircularityThreshold = 0.3;
  private boolean enableCellFusion = false;
  private boolean enableCellDivision = true;
  private int numberFramesToCheckCircularity = 5;
  private int globalHighestCellLabel = 0;
  private List<ImageFrame> framesList = null;

  private String outputFilePath = null;

  public SimpleCellTracker(TrackingAppParams params) {
    inputStack = params.getInputStack();
    outputStack = params.getOutputStack();

    String name = params.getOutputDirectory();
    if(!name.endsWith(File.separator))
      name += File.separator;
    outputFilePath = name + params.getPrefix() + ".tif";

    // copy the params form tracking main.java.gov.nist.isg.tracking.app params to local variables
    initParams(params);

    // build the list of image frames to track
    initFramesList();

  }



  private void initParams(TrackingAppParams params) {
    this.maxCentroidsDistance = params.getMaxCentroidsDist();
    this.cellSizeThreshold = params.getCellSizeThreshold();
    this.weightOverlap = params.getWeightCellOverlap();
    this.weightCentroids = params.getWeightCentroids();
    this.weightSize = params.getWeightCellSize();
    this.fusionOverlapThreshold = params.getFusionOverlapThreshold();
    this.divisionOverlapThreshold = params.getDivisionOverlapThreshold();
    this.daughterAspectRatioSimilarity = params.getDaughterAspectRatioSimilarity();
    this.daughterSizeSimilarity = params.getDaughterSizeSimilarity();
    this.motherCircularityThreshold = params.getMotherCircularityThreshold();
    this.enableCellDivision = params.isEnableCellDivision();
    this.enableCellFusion = params.isEnableCellFusion();
    this.numberFramesToCheckCircularity = params.getNumFramesToCheckCircularity();
  }


  private void initFramesList() {
    framesList = new ArrayList<ImageFrame>(inputStack.getSize());

    for (int i = 0; i < inputStack.getSize(); i++) {
      ImageFrame curFrame = new ImageFrame(i, inputStack.getSliceLabel(i + 1));
      framesList.add(i, curFrame);
    }
    TrackingAppParams.getInstance().setFramesList(framesList);
  }

  public void run() {
    try{
      if(outputStack == null) {
        outputStack = new ImageStack(inputStack.getWidth(), inputStack.getHeight());
      }

      Log.setLogLevel(Log.LogType.NONE);

      File file = new File(outputFilePath);
      file = file.getParentFile();
      if(!file.exists()) {
        file.mkdir();
      }

      worker();

      FileSaver fs = new FileSaver(new ImagePlus("track",outputStack));
      fs.saveAsZip(outputFilePath);


    }catch(Exception e) {
      Log.error(e);
    }
  }

  public void worker() throws InterruptedException {


    TrackingAppParams params = TrackingAppParams.getInstance();

    ImageFrame curFrame = null;
    ImageFrame prevFrame = null;

    for (int i = 0; i < inputStack.getSize(); i++) {

      if (Thread.interrupted()) {
        throw new InterruptedException("Cell Tracker Interrupted");
      }

      // update the progress bar
      params.setProgressBar(((float) i) / framesList.size());

      // get the current frame from the frames list
      curFrame = framesList.get(i);
      // add to the current frame the image data
      curFrame.setImage(inputStack.getProcessor(i + 1));

      // track the pair of image
      Log.mandatory("Tracking: " + (prevFrame == null ? "\"\"" : prevFrame.getTitle()) + " -> " + curFrame.getTitle());
      trackImageFramePair(curFrame, prevFrame, i);

      if (prevFrame != null) {
        // if the prevFrame is not null, apply global labels and write the image to outputStack
        prevFrame.applyGlobalLabels();
        appendSlice(prevFrame.getImage().getAsImagePlus(), prevFrame.getFrameNb());
        // release the image tile
        prevFrame.setState(ImageFrame.State.WRITTEN);
        prevFrame.releaseImageTile();
      }

      prevFrame = curFrame;
    }

    if (Thread.interrupted()) {
      throw new InterruptedException("Cell Tracker Interrupted");
    }
    // write the last image to the output image stack
    prevFrame.applyGlobalLabels();
    appendSlice(prevFrame.getImage().getAsImagePlus(), prevFrame.getFrameNb());
    // release the image tile
    prevFrame.setState(ImageFrame.State.WRITTEN);
    prevFrame.releaseImageTile();

    if(params.isSaveOutputsEnabled() && params.isSaveAsStackEnabled() && outputStack != null && !outputStack.isVirtual()) {
      String dir = params.getOutputDirectory();
      if (!dir.endsWith(File.separator))
        dir = dir + File.separator;
      String prefix = params.getPrefix();

      IJ.saveAsTiff(new ImagePlus(prefix, outputStack), dir + prefix);
    }


  }


  private void appendSlice(ImagePlus imp, int nb) {

    TrackingAppParams params = TrackingAppParams.getInstance();

    String name = "";
    if(params.isSaveOutputsEnabled()) {
      if(outputStack.isVirtual() || !params.isSaveAsStackEnabled()) {
        String dir = params.getOutputDirectory();
        if (!dir.endsWith(File.separator))
          dir = dir + File.separator;

        if (dir.length() > 0) {
          String prefix = params.getPrefix();
          int maxNb = params.getFramesList().size();
          String temp = Integer.toString(Math.max(4, Integer.toString(maxNb).length()));

          name = prefix + String.format("%0" + temp + "d", nb) + ".tif";

          // write the image to disk
          if(!IJ.saveAsTiff(imp, (dir + name))) {
            throw new IllegalArgumentException("Unable to save image to disk.");
          }
        }
      }
    }

    if (outputStack != null) {
      if(outputStack.isVirtual()) {
        ((VirtualStack)outputStack).addSlice(name);
      }else{
        outputStack.addSlice(name, imp.getProcessor());
      }

    }
  }


  /**
   * Function to track cells from the ImageFrame at time t-1 to the ImageFrame at time t.
   *
   * @param curFrame  the ImageFrame at the current time (t)
   * @param prevFrame the ImageFrame at the previous time (t-1)
   * @param frameNb   the frame number of the current time slice
   */
  private void trackImageFramePair(ImageFrame curFrame, ImageFrame prevFrame, int frameNb)
      throws InterruptedException {

    // Process the ImageTile to extract out the basic features
    curFrame.processImage();

    // if this is the first ImageFrame in the tracking sequence then generate a numbering for the cells contained within
    if (frameNb == 0) {
      // generating a sequential numbering of the cells within the first ImageFrame
      globalHighestCellLabel = 0;
      // apply those labels to the Cell objects
      for (Cell c : curFrame.getCellsList()) {
        c.setGlobalLabel(++globalHighestCellLabel);
      }
      // set the state to tracked as this is the first frame
      curFrame.setState(ImageFrame.State.TRACKED);
      return;
    }

    // If the previous ImageFrame has not been processed this cannot continue so throw an error
    if (prevFrame.getState() < ImageFrame.State.PROCESSED) {
      throw new IllegalStateException("Previous Frame is in the incorrect state");
    }

    // Generate the overlap matrix for the previous an current frames cost.disp(); overlap.disp(); trackVector.disp(); new ImageJ(); curFrame.getImage().wrapAsImagePlus().show();
    computeOverlap(curFrame, prevFrame);

    // Generate the cost matrix between the cells in the previous and current frames
    computeCost(curFrame, prevFrame);
    // generate the track vector that maps the previous frame cell numbers onto the current frame
    generateTrackVector(curFrame);

    // check for division cases
    if (checkDivision(curFrame, prevFrame, frameNb)) {
      // since the pixeldata has been altered overlap and cost need to be updated
      // Generate the overlap matrix for the previous an current frames
      computeOverlap(curFrame, prevFrame);
      // Generate the cost matrix between the cells in the previous and current frames
      computeCost(curFrame, prevFrame);
    }

    // check for fusion cases
    if (checkFusion(curFrame, prevFrame)) {
      // since the pixeldata has been altered overlap and cost need to be updated
      // Generate the overlap matrix for the previous an current frames
      computeOverlap(curFrame, prevFrame);
      // Generate the cost matrix between the cells in the previous and current frames
      computeCost(curFrame, prevFrame);
    }



    // find the best match for the untracked source and target cells
    hungarianOptimization(curFrame);

    // convert the trackVector into global cell label numbers
    globalHighestCellLabel = renumberTracking(curFrame, prevFrame, globalHighestCellLabel);
  }


  /**
   * Compute the overlap between the previous ImageFrame and the current ImageFrame. Resulting
   * overlap Matrix2D will contain the number of pixels between the previous cell i and the current
   * cell j at the matrix location (i,j). This overlap matrix is stored in the current frame
   *
   * @param curFrame  the ImageFrame of the current time slice in the cell tracking.
   * @param prevFrame the ImageFrame of the previous time slice in the cell tracking.
   */
  private void computeOverlap(final ImageFrame curFrame, final ImageFrame prevFrame) {

    // get a reference to the two ImageTiles
    ImageTile curImage = curFrame.getImage();
    ImageTile prevImage = prevFrame.getImage();

    Log.debug(
            "Computing overlap between " + curFrame.getTitle() + " and " + prevFrame.getTitle());

    // find the max cell number for each ImageTile, this defines the size of the overlap matrix
    int curMaxCellNb = curFrame.getMaxCellImgLabelNumber();
    int prevMaxCellNb = prevFrame.getMaxCellImgLabelNumber();

    // overlap i value is previous frame label, j value is current frame label
    Matrix2D overlap = new Matrix2D(prevMaxCellNb, curMaxCellNb);
    // set the overlap matrix in the current frame to this instance of overlap
    curFrame.setOverlap(overlap);

    // set dimensions to iterate over by looking at the min width and height of both images
    // this ensures that despite potentially different size images, the overlap will only be computed on the shared area between the two images
    int width = Math.min(curImage.getWidth(), prevImage.getWidth());
    int height = Math.min(curImage.getHeight(), prevImage.getHeight());

    // loop over the pixels that are shared between the two images recording the overlap between each labeled body and every other body
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        // if both images have a nonzero pixel values
        if (curImage.get(x, y) > 0 && prevImage.get(x, y) > 0) {
          overlap.increment(prevImage.get(x, y), curImage.get(x, y));
        }
      }
    }
  }


  /**
   * Compute the cost between the previous ImageFrame and the current ImageFrame. cost(i,j) =
   * centroidTerm*centroidWeight + overapTerm*overlapWeight + sizeTerm*sizeWeight;
   *
   * @param prevFrame the ImageFrame holding the main.java.gov.nist.isg.tracking.metadata for the previous Image in the tracking
   *                  sequence.
   * @param curFrame  the ImageFrame holding the main.java.gov.nist.isg.tracking.metadata for the current Image in the tracking
   *                  sequence.
   */
//	private static void computeCost(final ImageFrame curFrame, final ImageFrame prevFrame,
//			final double maxCentroidDistance, final double weightCentroids, final double weightSize, final double weightOverlap) {
  private void computeCost(final ImageFrame curFrame, final ImageFrame prevFrame) {

    Log.debug(
            "Computing cost between " + curFrame.getTitle() + " and " + prevFrame.getTitle());

    // get a reference to the overlap matrix held within the current ImageFrame
    Matrix2D overlap = curFrame.getOverlap();
    // find the max cell number for each ImageTile, this defines the size of the cost matrix
    int curMaxCellNb = overlap.getN();
    int prevMaxCellNb = overlap.getM();

    // create and init the cost matrix
    Matrix2D cost = new Matrix2D(prevMaxCellNb, curMaxCellNb);
    // set the current ImageFrames cost matrix to this instance
    curFrame.setCost(cost);
    cost.initTo(Double.NaN);

    // loop over the cells in the previous frame
    for (Cell prev : prevFrame.getCellsList()) {
      int i = prev.getImgLabel();
      // loop over the cells in the current frame
      for (Cell cur : curFrame.getCellsList()) {
        // get the labels for the previous and current cell
        int j = cur.getImgLabel();

        // delta x between the two centroids
        double deltax = prev.getCentroidX() - cur.getCentroidX();
        // delta y between the two centroids
        double deltay = prev.getCentroidY() - cur.getCentroidY();
        // the euclidean distance between the two centroids
        double deltaCentroid = Math.sqrt(deltax * deltax + deltay * deltay);

        // create the overlap term for the current pair of cells
        double overlapTerm =
            1 - (overlap.get(i, j) / (2 * prev.getArea()) + overlap.get(i, j) / (2 * cur
                .getArea()));
        // if the two cells overlap, or if they are within the max centroid displacement then assign the two cells a cost
        if (overlapTerm != 1 || deltaCentroid <= maxCentroidsDistance) {
          deltaCentroid = Math.min(deltaCentroid, maxCentroidsDistance);
          double centroidTerm = deltaCentroid / maxCentroidsDistance;
          double sizeTerm =
              Math.abs(prev.getArea() - cur.getArea()) / (Math.max(prev.getArea(), cur.getArea()));
          cost.set(i, j, overlapTerm * weightOverlap + centroidTerm * weightCentroids
                         + sizeTerm * weightSize);
        }
      }
    }
  }

  /**
   * Computes the row wise minimum of cost to track cells in prevFrame to cells in curFrame
   *
   * @param curFrame the current ImageFrame
   */
  private void generateTrackVector(ImageFrame curFrame) {
    // Find the minimum of all the rows of the cost matrix and stock the indexes (the number of the corresponding
    // target cells) into the frame_track_vector.
    // The min_cost_vector is a vector that has the minimum cost value between each source cell and its corresponding
    // target cell that might be tracked to.
    Log.debug( "Generating initial track vector");

    // get a reference to the cost matrix
    Matrix2D cost = curFrame.getCost();

    // create the matrices to hold the resulting index and value data after cost has been minimized row wise
    Matrix2D trackVector = new Matrix2D(cost.getM(), 1);
    Matrix2D trackvals = new Matrix2D(cost.getM(), 1);
    // minimize cost row wise
    cost.rowWiseMin(trackvals, trackVector);

    // set the current ImageFrames track vector to this instance
    curFrame.setTrackVector(trackVector);
  }

  /**
   * Checks for division cases between the previous and the current ImageFrame. If division is found
   * it is dealt with specifically If division is disabled, the division cases are relabeled so the
   * both have the same number as the first daughter. This daughter is then tracked to the mother
   * Therefore even though the cells divided in the segmented masks the output tracked mask will
   * have them with the same label. If pixel data is changed both overlap and cost will need to be
   * recomputed to reflect the current state of the ImageTile pixels
   *
   * @param curFrame  the current frame at time (t) in the tracking sequence
   * @param prevFrame the previous frame at time (t-1) in the tracking sequence
   * @param frameNb   the frame number of the current ImageFrame in the tracking sequence
   * @return whether overlap and cost need to be recomputed because changes were made to the pixels
   * of the ImageFrame
   */
  private boolean checkDivision(ImageFrame curFrame, final ImageFrame prevFrame,
                                final int frameNb) {

    Log.debug(
            "Check division between " + curFrame.getTitle() + " and " + prevFrame.getTitle());

    // Initialize the frame_division vector that contains the number of the target cells that result from a
    // division of one source cell. For example if cell 2 divides into cells 4 and 6, then the resulting frame_division holds
    // a value of 2 at the index locations 4 and 6
    Matrix2D division = new Matrix2D(curFrame.getMaxCellImgLabelNumber(), 1);
    // set the current ImageFrames division to this instance
    curFrame.setDivision(division);
    division.initTo(0);

    // get a reference to the overlap, cost, and trackVector from the current ImageFrame
    Matrix2D overlap = curFrame.getOverlap();
    Matrix2D cost = curFrame.getCost();
    Matrix2D trackVector = curFrame.getTrackVector();

    // flag controls whether overlap and cost need to be recomputed because of any alterations of the pixel values of the underlying ImageTile
    boolean recomputeRequired = false;

    // potential mother cells are any cells in the previous frame who have more than one cell in the current frame
    // whos minimum cost column-wise is that cell in the previous frame

    // compute the column-wise minimum of cost to find the cells in the current frame that are tracked to the same parent in the previous frame
    Matrix2D colWiseMinValues = new Matrix2D(1, cost.getN());
    Matrix2D colWiseMinIndx = new Matrix2D(1, cost.getN());
    cost.colWiseMin(colWiseMinValues, colWiseMinIndx);

    // find the number of cells in the current frame that are mapped to each cell in the previous frame
    // if a previous cell has more than one cell mapped to it then that is a division candidate
    Matrix2D nbMapped = new Matrix2D(cost.getM(), 1);
    nbMapped.initTo(0);
    // loop across the colWiseMinIndx vector
    for (int j = 1; j <= colWiseMinIndx.getN(); j++) {
      if (!Double.isNaN(colWiseMinIndx.get(1, j))) {
        nbMapped.increment(colWiseMinIndx.get(1, j), 1);
      }
    }

    // if all potential mother cells only map to one cell in the current frame then there are no division cases so return
    List<Cell> potentialMotherCells = new ArrayList<Cell>();
    for (int i = 1; i <= nbMapped.getM(); i++) {
      if (nbMapped.get(i, 1) > 1) {
        // a previous cell has been found that has more than one current cell mapped to it
        // add it to the potential mother cells list
        potentialMotherCells.add(prevFrame.getCellByImgLabel(i));
      }
    }

    // if no potential mother cells were found, return here. since recompute is initially set to false, overlap and cost are not going to be recomputed.
    if (potentialMotherCells.size() == 0) {
      return recomputeRequired;
    }

    // If execution got to here then there are potential division cases, now we need to determine which (if any) of these are actual division events
    Log.debug( "Division(s) found");
    // determine if the aspect ratio has been computed, if not compute it as it is required for the daughter aspect ratio threshold
    if (!curFrame.isAspectRatioGenerated()) {
      curFrame.computeAspectRatio();
    }

    // List of mother cell numbers
    Set<Integer> foundMotherNumbers = new HashSet<Integer>();

    // loop over the potential mother cells and determine if a division event has occurred
    for (Cell motherCell : potentialMotherCells) {
      // find potential daughter cells
      int motherCellNb = motherCell.getImgLabel();
      // List of potential daughter cells of the current mother cell
      List<Cell> potentialDaughters = new ArrayList<Cell>();
      // loop over all of the cells in the current frame looking for daughters of this mother cell
      for (int daughterNb = 1; daughterNb <= colWiseMinIndx.getN(); daughterNb++) {
        // potential daughters are the cells in the current ImageFrame that map back to the current mother cell number in the column wise minimization of cost
        if (motherCellNb == (int) colWiseMinIndx.get(1, daughterNb)) {
          // get the potential daughter cell
          Cell daughter = curFrame.getCellByImgLabel(daughterNb);
          // only daughters that overlap with the mother cell more than a threshold are considered
          // compute the overlap value for the daughter to the mother
          double val = overlap.get(motherCellNb, daughter.getImgLabel()) / daughter.getArea();
          // if this daughter meets the division overlap threshold, add it to the potential daughters list
          if (val > divisionOverlapThreshold) {
            potentialDaughters.add(daughter);
          }
        }
      }

      // remove potential daughters that are tracked to cells other than the potential mother if they overlap the cell they are tracked to
      List<Cell> toRemove = new ArrayList<Cell>();
      for (Cell daughter : potentialDaughters) {
        // get the daughter number
        int dn = daughter.getImgLabel();
        // loop over the trackVector looking for cells in the previous frame that track to this daughter but are from a different mother
        for (int i = 1; i <= trackVector.getM(); i++) {
          // if the daughter number matches, and the cell it is tracked to in the previous frame is not the current mother cell number
          if (dn == (int) trackVector.get(i, 1) && i != motherCellNb) {
            // if the daughter is being tracked to a cell other than the potential mother
            if (overlap.get(i, dn) > 0) {
              // if the daughter is tracked to and overlaps a cell that is not the potential mother, remove it as a daughter
              toRemove.add(daughter);
              continue;
            }
          }
        }
      }
      // remove the invalid daughters found above
      potentialDaughters.removeAll(toRemove);

      // If there are 0 or 1 potential daughters then there is no division, so skip to the next potential mother
      if (potentialDaughters.size() <= 1) {
        continue;
      }

      // if there are more than 2 potential daughter cells and the user wants to restrict mitosis to 2 daughter cells
      // take the best 2 daughters by cost, aka 2 potential daughters with the lowest cost
      // do not limit the number of daughters if mitosis is disallowed
      if (enableCellDivision && potentialDaughters.size() > 2 && limitDivisionToTwoDaughters) {
        // keep daughters that correspond to the 2 smallest costs
        double minval = Double.MAX_VALUE;
        Cell d1 = null;
        Cell d2 = null;
        for (Cell d : potentialDaughters) {
          double curCost = cost.get(motherCell.getImgLabel(), d.getImgLabel());
          if (curCost < minval) {
            d1 = d;
            minval = curCost;
          }
        }
        minval = Double.MAX_VALUE;
        for (Cell d : potentialDaughters) {
          if (d == d1) // skip the smallest cost
          {
            continue;
          }
          double curCost = cost.get(motherCell.getImgLabel(), d.getImgLabel());
          if (curCost < minval) {
            d2 = d;
            minval = curCost;
          }
        }
        potentialDaughters.clear(); // clear the potential daughters
        potentialDaughters.add(d1); // add both daughters being kept back in
        potentialDaughters.add(d2);
      }

      // filter the daughters to ensure that they overlap the mother cell enough to be considered daughters
      // (overlap(mother, d1) + overlap(mother, d2)) / size(mother) must be above the division overlap threshold
      double overlapSum = 0;
      for (Cell d : potentialDaughters) {
        overlapSum += overlap.get(motherCell.getImgLabel(), d.getImgLabel());
      }
      if (overlapSum / motherCell.getArea() <= divisionOverlapThreshold) {
        // the combined set of daughters did not overlap the mother enough to be considered a mitosis event
        // skip to the next potential mother cell
        continue;
      }

      // to be considered a valid mitosis a pair of daughters must have similar sizes and aspect ratios
      // valid mitosis events into a pair of daughters have size similarities and aspect ratio similarities greater than the threshold
      // if there are more than 2 potential daughter cells, each pair of daughter cells is tested for size and aspect ratio similarity
      // only the pairs that are above the thresholds are considered valid division events
      // The similarity between size and aspect ratio is computed with the following metric: 1-(abs(a1-a2) / (a1+a2) ). This metric is equal to 1 when
      // the two values a1 and a2 are identical and is equal to 0 when there is an extreme case of dissimilarity between a1 and a2.
      // In this case a1 and a2 represent size or aspect ratio

      // Check to see if the mother cell was circular enough to be considered a mitosis within the previous n frames
      // the number of frames to look into the past checking how circular the mother cell was is determined by the user.
      boolean motherCellWasCircularEnough = false;
      // find the lowest numbered frame to check circularity in
      int end = frameNb - numberFramesToCheckCircularity;
      // if the ending frame precedes the first frame in the sequence, then the mother cell is defined as being circular enough
      if (end <= 0) {
        motherCellWasCircularEnough = true;
      } else {
        // find the global label of the mother cell
        int motherCellGlobalLabel = motherCell.getGlobalLabel();
        // for the mother cell loop over the last n frames in time
        for (int i = frameNb - 1; i >= end; i--) {
          // get the cell in the given time frame using the mother cells global label
          Cell c = framesList.get(i).getCellByGlobalLabel(motherCellGlobalLabel);
          // if the cell still exists in this frame
          if (c == null) {
            break; // the cell no longer exists as the iterator walks back through time
          } else {
            // or the mother cell circularity flag with the inequality checking that the mother cell was circular enough to be considered a mitosis
            // the mother cell only has to be below the circularity threshold once within the last n frames in order to be considered valid.
            motherCellWasCircularEnough =
                motherCellWasCircularEnough || (c.getCircularity() > motherCircularityThreshold);
          }
        }
      }

      // if cell mitosis in enabled, and the mother cell was circular enough, test if the daughters meet the criteria
      if (!enableCellDivision || motherCellWasCircularEnough) {
        // loop over all possible combinations of daughters checking the aspect and size ratios
        for (int i = 0; i < potentialDaughters.size(); i++) {
          for (int j = i + 1; j < potentialDaughters.size(); j++) {
            // get the current two daughter cells
            Cell c1 = potentialDaughters.get(i);
            Cell c2 = potentialDaughters.get(j);

            // compute the similarity metrics for the two current daughters
            double
                sizeSimilarity =
                1 - (Math.abs(c1.getArea() - c2.getArea()) / (c1.getArea() + c2.getArea()));
            double
                aspectRatioSimilarity =
                1 - (Math.abs(c1.getAspectRatio() - c2.getAspectRatio()) / (c1.getAspectRatio() + c2
                    .getAspectRatio()));
            // determine if this was a valid division
            boolean
                validDivision =
                (sizeSimilarity > daughterSizeSimilarity) && (aspectRatioSimilarity
                                                              > daughterAspectRatioSimilarity);

            // if mitosis is disabled, or the size and aspect ratios are above the threshold add them to the division matrix
            // they are added to division matrix if mitosis is disabled to allow those cells to be merged into one label later
            if (!enableCellDivision || validDivision) {
              // add the current mother to the list of found mothers.
              foundMotherNumbers.add(motherCellNb);

              // add the mother cell number as the value of the division vector for the current pair of daughters
              division.set(c1.getImgLabel(), 1, motherCellNb);
              division.set(c2.getImgLabel(), 1, motherCellNb);
              // set the trackVector for the current mother cell to 0 to kill that mother cell
              // in a division the mother cell dies and the 2 daughters are born
              trackVector.set(motherCellNb, 1, 0);
              // kill any tracks that track a cell in the previous frame to either one of the current daughters
              for (int k = 1; k <= trackVector.length(); k++) {
                if (c1.getImgLabel() == (int) trackVector.get(k, 1)) {
                  trackVector.set(k, 1, Double.NaN);
                }
                if (c2.getImgLabel() == (int) trackVector.get(k, 1)) {
                  trackVector.set(k, 1, Double.NaN);
                }
              }
            }
          }
        }
      }
    }

    // if cell mitosis is not enabled check to see if there are any nonzero entries in frame_division
    // if there are, those division events need to be merged so they share a common label with all of the daughters
    if (!enableCellDivision && division.nnz() > 0) {
      Log.debug( "Division disallowed, renumbering cells to prevent the division");
      // initialize the renumbering vector which will not only merge the division events into one label
      // but also relabel the cells so that the resulting labels are consecutive starting at 1
      Matrix2D renum = new Matrix2D(division.length(), 1);
      for (int i = 1; i <= division.length(); i++) {
        renum.set(i, 1, i);
      }

      // loop over the mother cells relabeling the daughters
      for (Integer motherCellNb : foundMotherNumbers) {
        // loop over the daughters
        boolean firstDaughter = true;
        int d1 = -1;
        // loop over all cells in the current frame
        for (int daughterNb = 1; daughterNb <= division.length(); daughterNb++) {
          // if the cell tracked to the daughterNb is the mother cell
          if (motherCellNb == (int) division.get(daughterNb, 1)) {
            // this is a daughter of the given mother cell
            // remove any tracks to the daughters so that this overwrites any previously assigned tracks
            for (int k = 1; k <= trackVector.length(); k++) {
              if (daughterNb == (int) trackVector.get(k, 1)) {
                trackVector.set(k, 1, Double.NaN);
              }
            }
            // track the mother cell to the first daughter
            if (firstDaughter) {
              trackVector.set(motherCellNb, 1, daughterNb);
              d1 = daughterNb;
              firstDaughter = false;
            } else {
              // renumber the non first daughter to the first daughters number
              renum.set(daughterNb, 1, d1);
            }
          }
        }
      }

      // adjust the renum_vec so that resulting numbers are sequential
      List<Double> u = Matrix2D.unique(renum);
      Matrix2D renum2 = new Matrix2D(renum.getM(), 1);
      renum2.initTo(0);
      int k = 1; // is for the new cell numbering, so the new numbering starts at 1
      for (int i = 0; i < u.size(); i++) {
        boolean assigned = false;
        for (int j = 1; j <= renum.length(); j++) {
          if (renum.get(j, 1) == u.get(i)) {
            renum2.set(j, 1, k);
            assigned = true;
          }
        }
        if (assigned) {
          k++;
        }
      }
      renum = renum2;

      // relabel the non nan values of track vector with the values in renum, so the trackVector reflects the altered numbering of the ImageTile
      for (int i = 1; i <= trackVector.length(); i++) {
        if (!Double.isNaN(trackVector.get(i, 1)) || trackVector.get(i, 1) > 0) {
          trackVector.set(i, 1, renum.get(trackVector.get(i, 1), 1));
        }
      }

      // create the pixel renumbering vector
      short[] rn = new short[curFrame.getMaxCellImgLabelNumber() + 1];
      for (int i = 0; i < rn.length; i++) {
        rn[i] = 0;
      }
      // use renum to adjust the labels of the cells
      for (Cell c : curFrame.getCellsList()) {
        // update the Cell object labelings
        c.setGlobalLabel((int) renum.get(c.getImgLabel(), 1));
        // update the renumber pixel vector
        rn[c.getImgLabel()] = (short) c.getGlobalLabel();
      }

      // get the pixeldata from the ImageTile
      short[] px = curFrame.getImage().getPixelData();
      // renumber the pixels
      for (int i = 0; i < px.length; i++) {
        if (px[i] > 0) {
          px[i] = rn[px[i]];
        }
      }
      // update the main.java.gov.nist.isg.tracking.metadata about the image now that pixel data has changed
      curFrame.processImage();
      // if the aspect ratio had been generated, recompute it now that pixel data has changed
      if (curFrame.isAspectRatioGenerated()) {
        curFrame.computeAspectRatio();
      }

      // set division to zeros because division is disabled
      division.initTo(0);
      recomputeRequired = true;
    }

    return recomputeRequired;
  }

  /**
   * Checks for fusion cases between the previous and the current ImageFrame. If fusion is found it
   * is dealt with specifically If fusion is disabled, the cell in the current ImageFrame that is a
   * fusion of two or more cells in the previous frame is cut into pieces using the nearest
   * connected neighbor in the previous frame. If fusion is enabled, the cells that fused into the
   * cell in the current ImageFrame are considered dead and the new fused cell is born
   *
   * @param curFrame  the current frame at time (t) in the tracking sequence
   * @param prevFrame the previous frame at time (t-1) in the tracking sequence
   * @return whether overlap and cost need to be recomputed because changes were made to the pixels
   * of the ImageFrame
   */
  private boolean checkFusion(ImageFrame curFrame, final ImageFrame prevFrame) {

    Log.debug(
        "Check fusion between " + curFrame.getTitle() + " and " + prevFrame.getTitle());

    boolean recomputeRequired = false;

    // create the new fusion matrix
    Matrix2D  fusion =
        new Matrix2D(prevFrame.getMaxCellImgLabelNumber(), curFrame.getMaxCellImgLabelNumber());
    // set the current ImageFrame's fusion matrix to this instance
    curFrame.setFusion(fusion);

    // get references to the current frames trackVector
    Matrix2D trackVector = curFrame.getTrackVector();

    // look for fusion cases where multiple cells in the previous frame track to a single cell in the current frame
    boolean fusionFound = populateFrameFusion(curFrame, prevFrame);

    // if no fusion was found return
    if (!fusionFound) {
      return recomputeRequired;
    }

    // fusion matrix is currently binary, 1 if there is a fusion event and between those two cells and a 0 if no fusion
    // renumber frameFusion with the cell numbers instead of 1's and 0's
    relabelFrameFusionWithCellNumbers(fusion, trackVector, curFrame.getMaxCellImgLabelNumber());

    // If fusion is enabled, return at this point
    if (enableCellFusion) return recomputeRequired;

    Log.debug("Fusion(s) found");

    // If this code executes then there is one or more fusion cases
    // Since cell fusion is disallowed we need to cut the fused cells apart
    // give every pixel that overlaps between the current frame and the previous frame to the cell that the pixel belonged to in the previous frame
    // the remaining pixels are labeled according to the closest cell and most dominant neighbor if contested
    int[][] cellBoundingBoxes = curFrame.computeBoundingBox();

    // for every fusion case, loop over the bounding box cutting the cell apart
    for (int j = 1; j <= fusion.getN(); j++) {
      int nbFound = 0;
      for (int i = 1; i <= fusion.getM(); i++) {
        if (fusion.get(i, j) > 0) {
          nbFound++;
        }
      }
      if (nbFound >= 2) {
        int xMin = cellBoundingBoxes[j - 1][0];
        int xMax = cellBoundingBoxes[j - 1][1];
        int yMin = cellBoundingBoxes[j - 1][2];
        int yMax = cellBoundingBoxes[j - 1][3];

        cutFusedCell(curFrame, prevFrame, j, xMin, xMax, yMin, yMax);
      }
    }


    // Eliminate the multiple bodies cells. This loop is made to eliminate, after dividing the fused cell region,
    // cases where a sub-cell k (part of the target cell region named k) might have several unconnected bodies. The
    // algorithm works as follows:
    // - First, in the target fused cell region, referred to as current_fused_cell_box, we set all the other cells
    // numbers to zero except for cell k which will be set to 1. This will give us a binary region.
    // - Second, we label the the bodies in that binary region.
    // - Third, if we find multiple bodies, we check the size of each and we keep the biggest one as cell k and we
    // renumber the rest of the bodies region according to their most dominant neighbor number.

    List<Double> cellsToCheckConnectivity = Matrix2D.unique(fusion);
    if (cellsToCheckConnectivity.get(0) == 0) {
      cellsToCheckConnectivity.remove(0);
    }

    // ensure that each label has one and only one connected body
    checkObjectBodyConnectivity(curFrame, cellsToCheckConnectivity);

    // delete the cells below the size threshold after being cut
    enforceMinObjectSize(curFrame);

    // update the main.java.gov.nist.isg.tracking.metadata about the image now that pixel data has changed
    curFrame.processImage();
    // if the aspect ratio had been generated, recompute it now that pixel data has changed
    if (curFrame.isAspectRatioGenerated()) {
      curFrame.computeAspectRatio();
    }

    recomputeRequired = true;
    return recomputeRequired;
  }

  /**
   * This function populates the fusion matrix. Fusion matrix is a mxn matrix where m is the number
   * of cells in the previous frame and n is the number of cells in the current frame. fusion(i,j)
   * is 1 if those two cells i and j overlap. A fusion event is a column that has more than 1 non
   * zero element
   *
   * @param curFrame  the current frame at time (t) in the tracking sequence
   * @param prevFrame the previous frame at time (t-1) in the tracking sequence
   * @return flag which is true if one or more fusion events were found in the current image frame
   */
  private boolean populateFrameFusion(ImageFrame curFrame, final ImageFrame prevFrame) {

    // get references to the required data from the current frame
    Matrix2D overlap = curFrame.getOverlap();
    Matrix2D trackVector = curFrame.getTrackVector();
    Matrix2D fusion = curFrame.getFusion();
    fusion.initTo(0);

    // check to see if any target has been tracked to more than one source cell, if yes then check to see if this is a fusion case
    for (int i = 1; i <= overlap.getM(); i++) {
      // if the track vector cost is NaN the cell is dead
      if (!Double.isNaN(trackVector.get(i, 1)) && trackVector.get(i, 1) > 0) {
        // change the corresponding element between source cell i and its target cell in fusion matrix
        fusion.set(i, trackVector.get(i, 1), 1);
      }
    }

    // S contains the column wise sum of frame fusion
    Matrix2D S = fusion.sumVertical();

    // look for fusion candidates
    for (int i = 1; i <= overlap.getM(); i++) {
      if (Double.isNaN(trackVector.get(i, 1)) || trackVector.get(i, 1) == 0) {
        continue;  // if the cell is dead
      }

      if (S.get(1, (int) trackVector.get(i, 1)) < 2) {
        // cell i does not belong to any fusion region, reset the element between i and its target cell to 0
        fusion.set(i, trackVector.get(i, 1), 0);
        continue;
      }

      // if cell i belongs to a fused region and does not meet the fusion threshold, delete the track trackVector.disp();
      double val = overlap.get(i, trackVector.get(i, 1)) / prevFrame.getCellByImgLabel(i).getArea();
      if (val < fusionOverlapThreshold) {
        fusion.set(i, trackVector.get(i, 1), 0);
        trackVector.set(i, 1, Double.NaN);
      }
    }

    // look for any fusion cases while cleaning up the fusion matrix
    boolean fusionFound = false;
    // clean frame fusion removing any values that are the only nonzero value in a column
    for (int j = 1; j <= fusion.getN(); j++) {
      int nbFound = 0;
      for (int i = 1; i <= fusion.getM(); i++) {
        if (fusion.get(i, j) > 0) {
          nbFound++;
        }
      }
      if (nbFound == 1) {
        for (int i = 1; i <= fusion.getM(); i++) {
          fusion.set(i, j, 0);
        }
      }
      if (nbFound >= 2) {
        fusionFound = true;
      }
    }
    return fusionFound;
  }

  /**
   * Relabel the fusion matrix to reflect the image pixel labels instead of being binary. This also
   * adjusts the trackVector to reflect the fusion cases. This affects new numbers to use when
   * decomposing the fusion cell regions.
   *
   * @param fusion        the matrix holding the fusion events
   * @param trackVector   the vector tracking cells in the previous frame to the current frame
   * @param highestCellNb the highest cell label number used thus far
   */
  private void relabelFrameFusionWithCellNumbers(Matrix2D fusion, Matrix2D trackVector,
                                                 final int highestCellNb) {
    int newCellNb = highestCellNb;

    Matrix2D S = fusion.sumVertical();
    // Affect new numbers to use when decomposing the fusion target cell regions. This is done by using the
    // frame_fusion matrix as explained in the beginning of the function.
    for (int j = 1; j <= S.getN(); j++) {
      // if this is a fused cell
      if (S.get(1, j) >= 2) {
        // indicator that tells us the first time we encounter a source cell that form the fused target cell j this
        // cell will have the same number as j
        boolean firstOccurrence = true;
        // Scout all the lines searching for the cells that form target cell j and affect them with new numbers
        for (int i = 1; i <= fusion.getM(); i++) {
          // if object i does not belong to object j: continue
          if (fusion.get(i, j) == 0) {
            continue;
          }

          // if fusion is disabled, delete all tracked source cells so they die and the fused cell gets a different number
          if (enableCellFusion) {
            trackVector.set(i, 1, Double.NaN);
          }

          if (firstOccurrence) {
            // if source cell i is the first cell that we encounter that belongs to cell j: affect it with the same number as j
            firstOccurrence = false;
            fusion.set(i, j, j);
          } else {
            // if fusion is disabled all cells in the column get the columns number
            // if we reach this stage of the loop, this means that cell i is not the first cell that we encountered
            // and that it also belongs to the fused cell_region: affect it with the Highest_cell_number
            if (enableCellFusion) {
              fusion.set(i, j, j);
              trackVector.set(i, 1, Double.NaN);
            }else{
              newCellNb++;
              fusion.set(i, j, newCellNb);
              trackVector.set(i, 1, newCellNb);
            }
          }
        }
      }
    }
  }


  /**
   * This function will cut the fusedCellNb apart to prevent fusion.
   *
   * @param curFrame    the current frame at time (t) in the tracking sequence
   * @param prevFrame   the previous frame at time (t-1) in the tracking sequence
   * @param fusedCellNb the cell number that is currently a fusion of two or more cells in the
   *                    previous frame. This cell is going to be cut apart to prevent the fusion
   *                    from happening
   * @param xMinG       the x minimum from the cells bounding box
   * @param xMaxG       the x maximum from the cells bounding box
   * @param yMinG       the y minimum from the cells bounding box
   * @param yMaxG       the y maximum from the cells bounding box
   */
  private void cutFusedCell(ImageFrame curFrame, final ImageFrame prevFrame, final int fusedCellNb,
                            final int xMinG, final int xMaxG, final int yMinG, final int yMaxG) {

    Log.debug("Cutting Cell " + fusedCellNb + " apart due to disallowed fusion");
    // get a reference to the fusion matrix from the current frame
    Matrix2D fusion = curFrame.getFusion();

    // this is a vector number cells in previous frame long
    Matrix2D neighbors = new Matrix2D(fusion.getM(), 1);

    // get references to the two ImageTiles
    ImageTile curImage = curFrame.getImage();
    ImageTile prevImage = prevFrame.getImage();

    // find the minimum width and height to ensure that the iteration stays within bounds for both images, regardless of any size difference
    int width = Math.min(curImage.getWidth(), prevImage.getWidth());
    int height = Math.min(curImage.getHeight(), prevImage.getHeight());

    // loop over the bounding box of the cell to be cut
    for (int x = xMinG; x <= xMaxG; x++) {
      for (int y = yMinG; y <= yMaxG; y++) {
        // get the pixel in the current image
        short pix2 = curImage.get(x, y);
        // if this is not a background pixel and it belongs to the fused cell
        if (pix2 > 0 && pix2 == fusedCellNb) {
          // get the pixel in the same location as pix2 from the previous image
          short pix1 = prevImage.get(x, y);
          // if the previous image pixel is not background and it belongs to a cell that fused into the cell in the current frame
          if (pix1 > 0 && fusion.get(pix1, pix2) > 0) {
            // the pixel in current image overlaps the pixel in the previous image involved in the fusion,
            // so simply assign the current image the pixel value that represents the same cell from the previous image
            curImage.set(x, y, (short) Math.round(fusion.get(pix1, pix2)));
          } else {
            // the pixel did not overlap a cell in the previous image
            // find the closest, dominant neighbor
            neighbors.initTo(0);
            // this controls the search for pixels
            boolean foundNeighbors = false;
            // this is the distance from the current pixel that we are looking for a pixel in the previous frame
            int pixelDist = 1;

            // loop increasing the pixelDist until we have found a non-background pixel
            while (!foundNeighbors) {
              // constrain search to the bounding box
              int xMin = Math.max(0, x - pixelDist);
              int xMax = Math.min(width - 1, x + pixelDist);
              int yMin = Math.max(0, y - pixelDist);
              int yMax = Math.min(height - 1, y + pixelDist);

              // loop over the mini box around the current pixel looking for a label to assign it
              for (int k = yMin; k <= yMax; k++) {
                pix1 = prevImage.get(xMin, k);
                if (pix1 > 0 && fusion.get(pix1, pix2) > 0) {
                  foundNeighbors = true;
                  neighbors.increment(pix1, 1);
                }
                pix1 = prevImage.get(xMax, k);
                if (pix1 > 0 && fusion.get(pix1, pix2) > 0) {
                  foundNeighbors = true;
                  neighbors.increment(pix1, 1);
                }
              }
              for (int k = (xMin + 1); k <= (xMax - 1); k++) {
                pix1 = prevImage.get(k, yMin);
                if (pix1 > 0 && fusion.get(pix1, pix2) > 0) {
                  foundNeighbors = true;
                  neighbors.increment(pix1, 1);
                }
                pix1 = prevImage.get(k, yMax);
                if (pix1 > 0 && fusion.get(pix1, pix2) > 0) {
                  foundNeighbors = true;
                  neighbors.increment(pix1, 1);
                }
              }
              pixelDist++;
            }
            // find the dominant neighbor
            int neighborNb = 1;
            double maxVal = neighbors.get(1, 1);
            for (int k = 2; k <= neighbors.getM(); k++) {
              if (neighbors.get(k, 1) > maxVal) {
                maxVal = neighbors.get(k, 1);
                neighborNb = k;
              }
            }
            // write the dominant neighbor to the image
            curImage.set(x, y, (short) Math.round(fusion.get(neighborNb, pix2)));
          }
        }
      }
    }

  }

  /**
   * This function will modify the curFrame's ImageTile pixeldata so that all of the labels each
   * have one conected blob, aka no label will have two or more non-connected regions within the
   * image
   *
   * @param curFrame                 the ImageFrame to check the connectivity of
   * @param cellsToCheckConnectivity the list of cells that are going to be checked to ensure they
   *                                 have a single connected body
   */
  private void checkObjectBodyConnectivity(ImageFrame curFrame,
                                           List<Double> cellsToCheckConnectivity) {

    // return if there are no cells to check
    if (cellsToCheckConnectivity.size() == 0) {
      return;
    }

    Log.debug( "Checking object body connectivity");

    // find the highest object number
    int highestObjectNb = curFrame.getImage().getMaxValue();

    // short circuit if empty image
    if (highestObjectNb == 0) {
      return;
    }

    // extract the bounding boxes from all of the cells within the image
    int[][] cellBoundingBoxes = curFrame.computeBoundingBox();
    // get a reference to the ImageTile within the curFrame
    ImageTile curImage = curFrame.getImage();

    // for(int cellNb = 1; cellNb <= highestObjectNb; cellNb++) {
    for (Double d : cellsToCheckConnectivity) {
      int cellNb = d.intValue();

      // extract the bounding box with a 1 pixel border (the ImageTile is padded by 1 pixel, so this wont go out of bounds)
      int xMin = cellBoundingBoxes[cellNb - 1][0] - 1;
      int xMax = cellBoundingBoxes[cellNb - 1][1] + 1;
      int yMin = cellBoundingBoxes[cellNb - 1][2] - 1;
      int yMax = cellBoundingBoxes[cellNb - 1][3] + 1;

      // make a sub image of just the current cell
      short[] subPixeldata = curImage.getSubImagePixelData(xMin, xMax, yMin, yMax);
      for (int i = 0; i < subPixeldata.length; i++) {
        // make binary by setting all the wrong cell pixel to zero
        // "true" pixels have cellNb as a value
        if (subPixeldata[i] != cellNb) {
          subPixeldata[i] = 0;
        }
      }

      int subWidth = xMax - xMin + 1;
      int subHeight = yMax - yMin + 1;

      // label the subimage
      int
          nbObjFound =
          ConnectedComponents.labelConnectedComponents(subPixeldata, subWidth, subHeight);

      // if more than one object was found, then the current cell number has more than one non-connected body
      if (nbObjFound > 1) {

        // get the sizes of the objects that were just labeled
        int[] objSizes = ImageTile.getObjectSizes(subPixeldata);
        // the winner body is the largest one, ignoring background 0
        int winnerBody = 0;
        int maxval = 0;
        for (int i = 1; i < objSizes.length; i++) {
          if (objSizes[i] > maxval) {
            winnerBody = i;
            maxval = objSizes[i];
          }
        }
        // Create the matrix body_neighbors that holds all the numbers of the neighbors of each body
        int[][] bodyNeighbors = new int[highestObjectNb][nbObjFound];
        // extract subimage from ImageTile without making it binary
        short[] labelPixeldata = curImage.getSubImagePixelData(xMin, xMax, yMin, yMax);
        for (int y = 1; y < subHeight - 1; y++) {
          int k = y * subWidth;
          int endindx = k + subWidth - 1;
          int pix;
          for (; k < endindx; k++) {
            if (subPixeldata[k] > 0 && subPixeldata[k] != winnerBody) {
              // Check if the left neighbor pixel is not the background and is not object k in image_out
              pix = labelPixeldata[k - 1];
              if (pix > 0 && pix != cellNb) {
                bodyNeighbors[pix - 1][subPixeldata[k] - 1]++;
              }
              // Check if the top neighbor pixel is not the background and is not object k in image_out
              pix = labelPixeldata[k - subWidth];
              if (pix > 0 && pix != cellNb) {
                bodyNeighbors[pix - 1][subPixeldata[k] - 1]++;
              }
              // Check if the right neighbor pixel is not the background and is not object k in image_out
              pix = labelPixeldata[k + 1];
              if (pix > 0 && pix != cellNb) {
                bodyNeighbors[pix - 1][subPixeldata[k] - 1]++;
              }
              // Check if the bottom neighbor pixel is not the background and is not object k in image_out
              pix = labelPixeldata[k + subWidth];
              if (pix > 0 && pix != cellNb) {
                bodyNeighbors[pix - 1][subPixeldata[k] - 1]++;
              }
            }
          }
        }
        // Find the dominant neighbor of each body
        short[] renum = new short[nbObjFound + 1];
        renum[0] = 0;
        for (int objNb = 1; objNb <= nbObjFound; objNb++) {
          int winner = 0;
          maxval = 0;
          for (int neighbor = 1; neighbor <= highestObjectNb; neighbor++) {
            if (bodyNeighbors[neighbor - 1][objNb - 1] > maxval) {
              maxval = bodyNeighbors[neighbor - 1][objNb - 1];
              winner = neighbor;
            }
          }
          renum[objNb] = (short) winner;
        }
        renum[winnerBody] = (short) cellNb;

        // update the pixels in labelPixeldata
        for (int i = 0; i < labelPixeldata.length; i++) {
          if (subPixeldata[i] > 0) {
            labelPixeldata[i] = renum[subPixeldata[i]];
          }
        }

        // copy updated subimage pixeldata back into ImageTile
        curImage.overwriteSubImage(labelPixeldata, xMin, xMax, yMin, yMax);
      }
    }
  }


  /**
   * This will delete any cells that have a size lower than the minimum cell size threshold.
   *
   * @param curFrame the ImageFrame to enforce the minimum cell size on
   * @return boolean flag indicating whether any objects were deleted
   */
  private boolean enforceMinObjectSize(ImageFrame curFrame) {

    Log.debug( "Removing cells below " + cellSizeThreshold + " pixels in area");

    // get references to trackVector, fusion, and the ImageTile within the current ImageFrame
    Matrix2D fusion = curFrame.getFusion();
    Matrix2D trackVector = curFrame.getTrackVector();
    ImageTile curImage = curFrame.getImage();
    // get the sizes of the objects within the ImageTile
    int[] objSizes = curImage.getObjectSizes();
    int maxObjNb = objSizes.length - 1;

    // find the labels of the cells involved in fusion events
    List<Double> cellsInFusion = Matrix2D.unique(fusion);
    if (cellsInFusion.get(0) == 0) {
      cellsInFusion.remove(0);
    }

    // find the objects that are too small
    boolean[] invalidObjects = new boolean[maxObjNb + 1];
    for (int i = 0; i < invalidObjects.length; i++) {
      invalidObjects[i] = false;
    }

    // find the objects involved in fusion that are under the size limit
    for (Double d : cellsInFusion) {
      int val = d.intValue();
      invalidObjects[val] = objSizes[val] <= cellSizeThreshold;
    }

    // if there are no objects below the size threshold, return
    boolean shortCircuit = true;
    for (int i = 1; i < invalidObjects.length; i++) {
      if (invalidObjects[i]) {
        shortCircuit = false;
      }
    }

    // no objects were found to be too small, so return
    boolean modified = false;
    if (shortCircuit) {
      return modified;
    }

    // There are objects in curFrame that are below the minSizeThreshold that need to be deleted and have their pixels reassigned

    // create a renum vector to make the output labels sequential
    short[] renum = new short[maxObjNb + 1];
    renum[0] = 0;
    short newLabel = 1;
    for (int i = 1; i <= maxObjNb; i++) {
      // if this object is invalid, assign it a new label of 0 (background)
      if (invalidObjects[i]) {
        renum[i] = 0;
      } else {
        renum[i] = newLabel++;
      }
    }

    // update the trackVector to reflect the changes about to be made to the pixels,
    // if we are deleting a cell that is invalid, its track vector needs to be deleted as well
    for (int i = 1; i <= trackVector.getM(); i++) {
      // if source cell is dead
      if (Double.isNaN(trackVector.get(i, 1))) {
        continue;
      }

      if (0 == (int) trackVector.get(i, 1)) {
        continue;
      }

      // if source cell is tracked to a target cell below the size threshold, delete the track
      if (invalidObjects[(int) trackVector.get(i, 1)]) {
        trackVector.set(i, 1, Double.NaN);
        // loop over the row in fusion and set all values to 0
        for (int j = 1; j <= fusion.getN(); j++) {
          fusion.set(i, j, 0);
        }
        continue;
      }

      // update the trackVector to show the new numbering
      trackVector.set(i, 1, (int) renum[(int) trackVector.get(i, 1)]);
    }

    // remove any values from fusion that are a single element in the column
    for (int j = 1; j <= fusion.getN(); j++) {
      int nbFound = 0;
      for (int i = 1; i <= fusion.getM(); i++) {
        if (fusion.get(i, j) > 0) {
          nbFound++;
        }
      }
      if (nbFound == 1) {
        for (int i = 1; i <= fusion.getM(); i++) {
          fusion.set(i, j, 0);
        }
      }
    }
    // renumber fusion
    for (int i = 1; i <= fusion.getM(); i++) {
      for (int j = 1; j <= fusion.getN(); j++) {
        if (fusion.get(i, j) > 0) {
          fusion.set(i, j, (int) renum[(int) fusion.get(i, j)]);
        }
      }
    }

    // update division to reflect the numbering changes from deleting cells
    Matrix2D division = curFrame.getDivision();
    Matrix2D temp = new Matrix2D(division.getM(), division.getN());
    for(int i = 1; i <= division.getM(); i++) {
      if(division.get(i,1) > 0 ) {
        int motherCellNb = (int) division.get(i,1);
        temp.set(renum[i], 1, motherCellNb);
      }
    }
    curFrame.setDivision(temp);

    // renumber the pixels in the image
    short[] px = curImage.getPixelData();
    // create the mask of pixel locations that need a label
    short[] mask = new short[px.length];
    System.arraycopy(px, 0, mask, 0, px.length);
    for (int i = 0; i < px.length; i++) {
      if (px[i] > 0) {
        px[i] = renum[px[i]];
      }
    }

    // assign the label of the nearest connected body to all of the deleted pixels
    // this modifies px internally, which is just a reference to the pixeldata within curImage, so it updates the image pixels
    DistanceTransform
        .assignNearestConnectedLabel(px, mask, curImage.getWidth(), curImage.getHeight());
    modified = true;
    return modified;
  }


  /**
   * This will look at the cost matrix within the current ImageFrame and assign any untracked cells
   * to the best possible track This is done by finding the optimization of the cost matrix
   *
   * @param curFrame the ImageFrame to work on
   */
  private void hungarianOptimization(ImageFrame curFrame) {
    // find the untracked target cells

    Log.debug( "Performing hungarian optimization to assign untracked objects");

    // get references to the required data from the current ImageFrame
    Matrix2D trackVector = curFrame.getTrackVector();
    Matrix2D cost = curFrame.getCost();
    Matrix2D fusion = curFrame.getFusion();
    Matrix2D division = curFrame.getDivision();

    Matrix2D untrackedTargetCells = new Matrix2D(curFrame.getMaxCellImgLabelNumber(), 1);
    untrackedTargetCells.initTo(0);

    for (int i = 1; i <= trackVector.getM(); i++) {
      if (trackVector.get(i, 1) > 0) {
        untrackedTargetCells.set(trackVector.get(i, 1), 1, 1);
        // set untracked for this cell index to 1 untrackedTargetCells.disp(); trackVector.disp(); fusion.disp(); division.disp();
      }
    }

    // invert untrackedTargetCells
    for (int i = 1; i <= untrackedTargetCells.getM(); i++) {
      if (untrackedTargetCells.get(i, 1) > 0) {
        untrackedTargetCells.set(i, 1, 0);
      } else {
        untrackedTargetCells.set(i, 1, 1);
      }
    }

    // remove any cells from division if division is non null
    for (int i = 1; i <= division.getM(); i++) {
      if (division.get(i, 1) > 0) {
        untrackedTargetCells.set(i, 1, 0);
      }
    }

    // delete any cells that came from fusion and are dead due to size of fusion being disabled
    for (int i = 1; i <= fusion.getM(); i++) {
      for (int j = 1; j <= fusion.getN(); j++) {
        if (fusion.get(i, j) > 0) {
          // remove any cells from fusion cases (the i,j element of fusion will be the cell number involved)
          untrackedTargetCells.set(fusion.get(i, j), 1, 0);
        }
      }
    }

    boolean untrackedTargetCellsExist = false;
    for (int i = 1; i <= untrackedTargetCells.getM(); i++) {
      if (untrackedTargetCells.get(i, 1) > 0) {
        untrackedTargetCellsExist = true;
      }
    }
    // short circuit if there are no untracked target cells
    if (!untrackedTargetCellsExist) {
      return;
    }

    // find the untracked source cells
    Matrix2D untrackedSourceCells = new Matrix2D(trackVector.getM(), 1);
    untrackedSourceCells.initTo(0);
    for (int i = 1; i <= trackVector.getM(); i++) {
      if (Double.isNaN(trackVector.get(i, 1))) {
        untrackedSourceCells.set(i, 1, 1);
      }
    }

    // if any fusion elements are non zero, set the untrackedsource row to 0
    for (int i = 1; i <= fusion.getM(); i++) {
      for (int j = 1; j <= fusion.getN(); j++) {
        if (fusion.get(i, j) > 0) {
          // remove any cells from fusion cases (the i,j element of fusion will be the cell number involved)
          untrackedSourceCells.set(i, 1, 0);
        }
      }
    }

    boolean untrackedSourceCellsExist = false;
    for (int i = 1; i <= untrackedSourceCells.length(); i++) {
      if (untrackedSourceCells.get(i, 1) > 0) {
        untrackedSourceCellsExist = true;
      }
    }
    // short circuit if no untracked source cells exist
    if (!untrackedSourceCellsExist) {
      return;
    }

    // create a copy of cost that can be modified without destroying the original localCost.disp();
    Matrix2D localCost = cost.clone();

    // remove the costs between the tracked cells
    for (int i = 1; i <= localCost.getM(); i++) {
      for (int j = 1; j <= localCost.getN(); j++) {
        if (untrackedSourceCells.get(i, 1) <= 0) {
          localCost.set(i, j, Double.NaN);
        }
        if (untrackedTargetCells.get(j, 1) <= 0) {
          localCost.set(i, j, Double.NaN);
        }
      }
    }


    // trackVector is reference to the matrix within the curFrame
    assignNewTrack(localCost, trackVector);

  }

  /**
   * This function will recursively search for the minimum cost to track one cell to another, this
   * is the worker function of the hungarian optimization
   *
   * @param cost        the matrix holding the costs between pairs of cells
   * @param trackVector the vector tracking cells in the previous frame ot the current frame
   */
  private void assignNewTrack(Matrix2D cost, Matrix2D trackVector) {

    // if every element in cost is NaN then return, this is the stopping condition
    boolean nonNaNFound = false;
    for (int i = 1; i <= cost.getM(); i++) {
      for (int j = 1; j <= cost.getN(); j++) {
        if (!Double.isNaN(cost.get(i, j))) {
          nonNaNFound = true;
        }
      }
    }
    if (!nonNaNFound) {
      return;
    }

    // find the row wise min of cost
    Matrix2D rowminvals = new Matrix2D(cost.getM(), 1);
    Matrix2D rowminindx = new Matrix2D(cost.getM(), 1);
    cost.rowWiseMin(rowminvals, rowminindx);

    // find the column wise min of cost
    Matrix2D colminvals = new Matrix2D(1, cost.getN());
    Matrix2D colminindx = new Matrix2D(1, cost.getN());
    cost.colWiseMin(colminvals, colminindx);

    for (int i = 1; i <= rowminvals.getM(); i++) {
      if (!Double.isNaN(rowminvals.get(i, 1))) {
        if (colminindx.get(1, rowminindx.get(i, 1)) == i) {
          trackVector.set(i, 1, rowminindx.get(i, 1));
          // set the whole current column at index rowmincost[1] in cost to NaN
          for (int k = 1; k <= cost.getM(); k++) {
            cost.set(k, rowminindx.get(i, 1), Double.NaN);
          }
          // set the whole row at index i in cost to NaN
          for (int k = 1; k <= cost.getN(); k++) {
            cost.set(i, k, Double.NaN);
          }
        }
      }
    }

    assignNewTrack(cost, trackVector);
  }

  /**
   * This generates the global labels for the current ImageFrame
   *
   * @param globalHighestCellLabel the highest label of a cell thus far
   * @return the new global highest cell number used in the tracking
   */
  private int renumberTracking(ImageFrame curFrame, final ImageFrame prevFrame,
                               int globalHighestCellLabel) {

    Log.debug( "Generating " + curFrame.getTitle() + " global tracked labels");

    Matrix2D trackVector = curFrame.getTrackVector();

    for (Cell c : prevFrame.getCellsList()) {
      int prevCellImgLabel = c.getImgLabel();
      if (Double.isNaN(trackVector.get(prevCellImgLabel, 1))
          || trackVector.get(prevCellImgLabel, 1) <= 0) {
        // the cell is dead in the current frame
        // do nothing
      } else {
        // cell in current frame is not dead and has been tracked to cellImgLabel in previous frame
        int curCellImgLabel = (int) trackVector.get(prevCellImgLabel, 1);
        Cell curC = curFrame.getCellByImgLabel(curCellImgLabel);
        curC.setGlobalLabel(c.getGlobalLabel());
      }
    }

    // update the cell labels and assign new labels to hte ones that do not have a renumber value
    for (Cell c : curFrame.getCellsList()) {
      // assign new labels to the new cells
      if (c.getGlobalLabel() == 0) {
        // this is a new cell birth
        c.setGlobalLabel(++globalHighestCellLabel);
      }
    }
    curFrame.setState(ImageFrame.State.TRACKED);

    return globalHighestCellLabel;
  }

}
