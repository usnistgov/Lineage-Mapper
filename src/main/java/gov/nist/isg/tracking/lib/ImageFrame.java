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


package main.java.gov.nist.isg.tracking.lib;

import java.util.ArrayList;
import java.util.List;

import ij.process.ImageProcessor;


public class ImageFrame implements Comparable<ImageFrame> {

  private static final boolean roundCentroids = false;
  private static final int distToEdgeToConsiderOnBorder = 2;

  private int imageFrameState = State.DEFAULT;
  int frameNumber = 0;
  List<Cell> cells = null;
  Matrix2D trackVector = null;
  Matrix2D fusion = null;
  Matrix2D division = null;
  Matrix2D cost = null;
  Matrix2D overlap = null;
  String title = null;
  private int maxCellLabelNumber = 0;
  private ImageTile image = null;

  /**
   * Create a new ImageFrame.
   * @param ip the ImageProcessor to copy image data from.
   * @param frameNb the number of this frame in the image sequence.
   * @param title the title of the image.
   */
  public ImageFrame(ImageProcessor ip, int frameNb, String title) {
    imageFrameState = State.INIT;
    image = new ImageTile(ip);
    frameNumber = frameNb;
    imageFrameState = State.READ;
    this.title = title;
  }

  /**
   * Create a new ImageFrame.
   * @param frameNb the number of this frame in the image sequence.
   * @param title the title of the image.
   */
  public ImageFrame(int frameNb, String title) {
    imageFrameState = State.INIT;
    image = null;
    frameNumber = frameNb;
    this.title = title;
  }


  /**
   * Get the title of this ImageFrame.
   * @return String containing the title of this ImageFrame.
   */
  public String getTitle() {
    return this.title;
  }

  /**
   * Set the title of this ImageFrame.
   * @param title the new title of this ImageFrame.
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Get a reference to the overlap Matrix2D of this ImageFrame.
   * @return the Matrix2D overlap for this ImageFrame.
   */
  public Matrix2D getOverlap() {
    return this.overlap;
  }

  /**
   * Set the Matrix2D overlap for this ImageFrame.
   * @param overlap the new overlap Matrix2D.
   * @return a reference to this ImageFrame.
   */
  public ImageFrame setOverlap(Matrix2D overlap) {
    this.overlap = overlap;
    return this;
  }

  /**
   * Get a reference to the cost Matrix2D of this ImageFrame.
   * @return the Matrix2D cost for this ImageFrame.
   */
  public Matrix2D getCost() {
    return this.cost;
  }

  /**
   * Set the Matrix2D cost for this ImageFrame.
   * @param cost the new cost Matrix2D
   * @return a reference to this ImageFrame.
   */
  public ImageFrame setCost(Matrix2D cost) {
    this.cost = cost;
    return this;
  }

  /**
   * Get a reference to the fusion Matrix2D of this ImageFrame.
   * @return the Matrix2D fusion for this ImageFrame.
   */
  public Matrix2D getFusion() {
    return this.fusion;
  }

  /**
   * Set the Matrix2D fusion for this ImageFrame.
   * @param fusion the new fusion Matrix2D
   * @return a reference to this ImageFrame.
   */
  public ImageFrame setFusion(Matrix2D fusion) {
    this.fusion = fusion;
    return this;
  }

  /**
   * Get a reference to the division Matrix2D of this ImageFrame.
   * @return the Matrix2D division for this ImageFrame.
   */
  public Matrix2D getDivision() {
    return this.division;
  }

  /**
   * Set the Matrix2D division for this ImageFrame.
   * @param division the new division Matrix2D.
   * @return a reference to this ImageFrame.
   */
  public ImageFrame setDivision(Matrix2D division) {
    this.division = division;
    return this;
  }

  /**
   * Get a reference to the track vector Matrix2D of this ImageFrame.
   * @return the Matrix2D track vector for this ImageFrame.
   */
  public Matrix2D getTrackVector() {
    return this.trackVector;
  }

  /**
   * Set the Matrix2D trackVector of this ImageFrame.
   * @param trackVector the new trackVector.
   * @return a reference to this ImageFrame.
   */
  public ImageFrame setTrackVector(Matrix2D trackVector) {
    this.trackVector = trackVector;
    return this;
  }

  /**
   * Get the frame number for the ImageFrame.
   * @return the frame number for this ImageFrame.
   */
  public int getFrameNb() {
    return this.frameNumber;
  }

  /**
   * Apply the global labels that were generated during tracking to the ImageTile holding the pixel data.
   */
  public void applyGlobalLabels() {

    if (imageFrameState < State.TRACKED) {
      throw new IllegalStateException(
          "Current Frame has not yet been tracked, so it cannot have global labels applied");
    }

    Log.debug(
            "Renumbering " + this.getTitle() + " to output global tracked numbering");
    // create a renumbering vector to relabel pixels from the imgLabel to the cells new label
    short[] renum = new short[this.getMaxCellImgLabelNumber() + 1];
    for (int i = 0; i < renum.length; i++) {
      renum[i] = 0;
    }

    for (Cell c : this.cells) {
      renum[c.getImgLabel()] = (short) c.getGlobalLabel();
    }

    // get a reference to the current image pixeldata
    short[] px = image.getPixelData();

    // loop over the image pixeldata relabeling the pixels to the target labels
    for (int i = 0; i < px.length; i++) {
      if (px[i] > 0) {
        px[i] = renum[px[i]];
      }
    }
  }

  /**
   * Get a reference to the ImageTile for this ImageFrame.
   * @return the ImageTile this ImageFrame holds main.java.gov.nist.isg.tracking.metadata for.
   */
  public ImageTile getImage() {
    return image;
  }

  /**
   * Set the ImageTile this ImageFrame holds main.java.gov.nist.isg.tracking.metadata for.
   * @param ip
   */
  public void setImage(ImageProcessor ip) {
    image = new ImageTile(ip);
    imageFrameState = State.READ;
  }

  /**
   * Release the ImageTile from memory by setting its pixel array to null.
   * This can only be done if the state of this ImageFrame is past written.
   * @return a reference to this ImageFrame.
   */
  public ImageFrame releaseImageTile() {
    if (imageFrameState >= State.WRITTEN) {
      image.releaseMemory();
      imageFrameState = State.RELEASED;
    } else {
      throw new IllegalStateException(
          "ImageFrame has not yet been written to disk, cannot release the ImageTile.");
    }
    return this;
  }

  /**
   * Get a reference to the List of cells within this ImageFrame.
   * @return a reference to the List of Cells within the ImageTile that this ImageFrame holds main.java.gov.nist.isg.tracking.metadata about.
   */
  public List<Cell> getCellsList() {
    return cells;
  }

  /**
   * Process this ImageFrame.
   * If its ImageTile has been read into memory, extract out main.java.gov.nist.isg.tracking.metadata from the ImageTile.
   * @return a reference to this ImageFrame.
   */
  public ImageFrame processImage() {
    if (imageFrameState < State.READ) {
      throw new IllegalStateException(
          "ImageTile is not in READ state, therefore it cannot be processed");
    }

    Log.debug( "Processing Image: " + this.getTitle());

    short[] pixeldata = image.getPixelData();

    // find the max label in image
    maxCellLabelNumber = Utils.getMaxValue(pixeldata);

    double[] area = new double[maxCellLabelNumber];
    double[] perimeter = new double[maxCellLabelNumber];
    double[] centroidx = new double[maxCellLabelNumber];
    double[] centroidy = new double[maxCellLabelNumber];
    boolean[] bordercell = new boolean[maxCellLabelNumber];
    Matrix2D touching = new Matrix2D(maxCellLabelNumber, maxCellLabelNumber);
    // Initialize these variables
    for (int i = 0; i < maxCellLabelNumber; i++) {
      area[i] = 0;
      perimeter[i] = 0;
      centroidx[i] = 0;
      centroidy[i] = 0;
      bordercell[i] = false;
    }

    int pb = distToEdgeToConsiderOnBorder;
    int width = image.getWidth();
    int height = image.getHeight();
    for (int i = 0; i < pixeldata.length; i++) {
      if (pixeldata[i] > 0) {
        int pix = pixeldata[i];
        // update the area for this cell
        int x = i % width;
        int y = i / width;
        area[pix - 1]++;
        centroidx[pix - 1] += x;
        centroidy[pix - 1] += y;

        if (x <= pb || x >= (width - pb - 1) || y <= pb || y >= (height - pb - 1)) {
          // set the border cell to 1 to indicate this object body touches the edge of the image
          bordercell[pix - 1] = true;
        }

        // update the touching matrix
        // check left pixel
        if (pixeldata[i - 1] > 0 && pix != pixeldata[i - 1]) {
          touching.increment(pix, pixeldata[i - 1]);
        }

        // check right pixel
        if (pixeldata[i + 1] > 0 && pix != pixeldata[i + 1]) {
          touching.increment(pix, pixeldata[i + 1]);
        }

        // check top pixel
        if (pixeldata[i - width] > 0 && pix != pixeldata[i - width]) {
          touching.increment(pix, pixeldata[i - width]);
        }

        // check bottom pixel
        if (pixeldata[i + width] > 0 && pix != pixeldata[i + width]) {
          touching.increment(pix, pixeldata[i + width]);
        }
      }
    }

    // do a Moore boundary tracing for each cell to extract the perimeter of the object
    perimeter = ConnectedComponents.getPerimeter(pixeldata, width, maxCellLabelNumber);

    // compute the centroids using the first and zeroth image moment
    cells = new ArrayList<Cell>();
    Cell curCell;
    for (int i = 0; i < maxCellLabelNumber; i++) {
      if (area[i] > 0) { // if this label has pixel in the image
        curCell = new Cell(i + 1);
        curCell.setArea(area[i]);
        curCell.setCentroidX(centroidx[i] / area[i]);
        curCell.setCentroidY(centroidy[i] / area[i]);
        curCell.setPerimeter(perimeter[i]);
        curCell.setCircularity((4 * Math.PI * area[i]) / (perimeter[i] * perimeter[i]));
        curCell.setBorderCell(bordercell[i]);
        // loop over this row of touching adding in the touching matrix data to the cell
        for (int j = 1; j <= maxCellLabelNumber; j++) {
          if (touching.get(i + 1, j) > 0) {
            curCell.addTouchingCell(j);
          }
        }
        // add the current cell to the List
        cells.add(curCell);
      }
    }

    if (roundCentroids) {
      for (Cell c : cells) {
        c.setCentroidX((double) Math.round(c.getCentroidX()));
        c.setCentroidY((double) Math.round(c.getCentroidY()));
      }
    }
    imageFrameState = State.PROCESSED;
    return this;
  }

  /**
   * Compute the aspect ratio of each cell.
   */
  public void computeAspectRatio() {
    // Compute Aspect ratio of cur frame cells this is used to compare potential daughters
    // aspect ratio is based on the second moment of inertia, (major axis length) / (minor axis length)
    // second moments:
    // m11, m20, m02
    // major axis length = sqrt(2*a1/area)
    // a1 = m20 + m02 + sqrt((m20 - m02) + 4*m11*m11)
    // minor axis length = sqrt(2*a2/area)
    // a2 = m20 + m02 - sqrt((m20 - m02) + 4*m11*m11)
    // source: Digital Image Processing: An Algorithmic Introduction Using Java
    // By Wilhelm Burger, Mark J. Burg page 230-232

    // test if the image has been processed
    if (imageFrameState < State.READ) {
      throw new IllegalStateException(
          "ImageTile is not in READ state, therefore it cannot be processed");
    }

    // if the image has not been processed, do so now
    if (imageFrameState < State.PROCESSED) {
      processImage();
    }

    Log.debug( "Computing Aspect Ratios: " + this.getTitle());

    double[] m00 = new double[maxCellLabelNumber + 1];
    double[] m10 = new double[maxCellLabelNumber + 1];
    double[] m01 = new double[maxCellLabelNumber + 1];
    // Initialize to NaN
    for (int i = 0; i <= maxCellLabelNumber; i++) {
      m00[i] = Double.NaN;
      m10[i] = Double.NaN;
      m01[i] = Double.NaN;
    }
    // fill in the zero and first moments
    for (Cell c : cells) {
      int i = c.getImgLabel();
      m00[i] = c.getArea();
      m10[i] = c.getCentroidX();
      m01[i] = c.getCentroidY();
    }

    // variables to hold the second moments
    double[] m11 = new double[maxCellLabelNumber + 1];
    double[] m02 = new double[maxCellLabelNumber + 1];
    double[] m20 = new double[maxCellLabelNumber + 1];
    // Initialize to NaN
    for (int i = 0; i <= maxCellLabelNumber; i++) {
      m11[i] = 0;
      m20[i] = 0;
      m02[i] = 0;
    }

    int width = image.getWidth();
    short[] pixeldata = image.getPixelData();
    for (int i = 0; i < pixeldata.length; i++) {
      if (pixeldata[i] > 0) {
        int pix = pixeldata[i];
        int x = i % width;
        int y = i / width;
        m11[pix] += (x - m10[pix]) * (y - m01[pix]);
        m20[pix] += (x - m10[pix]) * (x - m10[pix]);
        m02[pix] += (y - m01[pix]) * (y - m01[pix]);
      }
    }

    for (int i = 1; i <= maxCellLabelNumber; i++) {
      m11[i] = m11[i] / m00[i];
      m20[i] = m20[i] / m00[i];
      m02[i] = m02[i] / m00[i];
    }

    for (Cell c : cells) {
      int i = c.getImgLabel();
      double temp = Math.sqrt((m20[i] - m02[i]) * (m20[i] - m02[i]) + 4 * m11[i] * m11[i]);
      double a1 = Math.sqrt(2 * (m20[i] + m02[i] + temp) / m00[i]);
      double a2 = Math.sqrt(2 * (m20[i] + m02[i] - temp) / m00[i]);
      c.setAspectRatio(a1 / a2);
    }
  }

  /**
   * Determine if the aspect ratio has been computed for the cells within this ImageFrame.
   * Computing aspect ratios is expensive.
   * @return the state of whether or not aspect ratios have been computed.
   */
  public boolean isAspectRatioGenerated() {
    for (Cell c : cells) {
      if (Double.isNaN(c.getAspectRatio())) {
        return false;
      }
    }
    return true;
  }

  /**
   * Compute the bounding boxes of the cells within the ImageTile that this ImageFrame holds main.java.gov.nist.isg.tracking.metadata for.
   * Output is a 2D int array where the first dimension is number of cells long and the second
   * dimension holds 4 values. [xMin, xMax, yMin, yMax];
   * @return array of bounding box data.
   */
  public int[][] computeBoundingBox() {
    if (imageFrameState < State.READ) {
      throw new IllegalStateException(
          "ImageTile is not in READ state, therefore it cannot be processed");
    }
    Log.debug( "Computing Bounding Boxes: " + this.getTitle());

    short[] pixeldata = image.getPixelData();
    int maxval = Utils.getMaxValue(pixeldata);

    int[][] bb = new int[maxval][4];

    int width = image.getWidth();
    int height = image.getHeight();

    for (int i = 0; i < maxval; i++) {
      bb[i][0] = width; // to hold x min
      bb[i][1] = 0; // to hold x max
      bb[i][2] = height; // to hold y min
      bb[i][3] = 0; // to hold y max
    }

    for (int i = 0; i < pixeldata.length; i++) {
      if (pixeldata[i] > 0) {
        int pix = pixeldata[i];
        int x = i % width;
        int y = i / width;
        bb[pix - 1][0] = (x < bb[pix - 1][0]) ? x : bb[pix - 1][0];
        bb[pix - 1][1] = (x > bb[pix - 1][1]) ? x : bb[pix - 1][1];
        bb[pix - 1][2] = (y < bb[pix - 1][2]) ? y : bb[pix - 1][2];
        bb[pix - 1][3] = (y > bb[pix - 1][3]) ? y : bb[pix - 1][3];
      }
    }
    return bb;
  }

  /**
   * Get the cell with the requested global label.
   * @return the Cell Object at with the label cellNb.
   */
  public Cell getCellByGlobalLabel(int cellNb) {
    for (Cell c : cells) {
      if (c.getGlobalLabel() == cellNb) {
        return c;
      }
    }
    return null;
  }

  /**
   * Get the cell with the requested image pixel label.
   * @return the Cell Object at with the label cellNb.
   */
  public Cell getCellByImgLabel(int cellNb) {
    for (Cell c : cells) {
      if (c.getImgLabel() == cellNb) {
        return c;
      }
    }
    return null;
  }

  /**
   * Get the max global label used in the List of cells.
   * @return the max global label.
   */
  public int getMaxCellGlobalLabelNumber() {
    int maxval = 0;
    for (Cell c : cells) {
      maxval = (c.getGlobalLabel() > maxval) ? c.getGlobalLabel() : maxval;
    }
    return maxval;
  }

  /**
   * Get the max image pixel label used in the List of cells.
   * @return the max image pixel label.
   */
  public int getMaxCellImgLabelNumber() {
    int maxval = 0;
    for (Cell c : cells) {
      maxval = (c.getImgLabel() > maxval) ? c.getImgLabel() : maxval;
    }
    return maxval;
  }

  /**
   * Get the current state of the ImageFrame.
   * @return the current state of the ImageFrame.
   */
  public int getState() {
    return imageFrameState;
  }

  /**
   * Set the state of the ImageFrame.
   * @param newstate the new State of the ImageFrame.
   */
  public void setState(int newstate) {
    imageFrameState = newstate;
  }

  /**
   * Returns a human readable String representation of this object.
   * @return a String representation of this ImageFrame.
   */
  @Override
  public String toString() {
//		return "ImageFrame: " + image.getSourceImageName() + " has " + cells.size() + " cells";
    if (cells == null) {
      return "Image Frame: " + this.getTitle();
    } else {
      return "ImageFrame: " + this.getTitle() + " has " + cells.size() + " cells";
    }
  }

  /**
   * Compares one ImageFrame to another.
   * @param o the ImageFrame to compare this ImageFrame to.
   * @return the result of the comparison.
   */
  @Override
  public int compareTo(ImageFrame o) {
    return frameNumber - o.frameNumber;
  }

  // TODO look into making this an enum
  public static class State {
    public static int DEFAULT = 0;
    public static int INIT = 1;
    public static int READ = 2;
    public static int PROCESSED = 3;
    public static int TRACKED = 4;
    public static int WRITTEN = 5;
    public static int RELEASED = 6;
  }




}
