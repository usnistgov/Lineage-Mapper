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


public class Cell {

  private int globalLabel = 0;
  private int imgLabel = 0;
  private double area = 0;
  private double centroidX = Double.NaN;
  private double centroidY = Double.NaN;
  private double perimeter = 0;
  private double circularity = Double.NaN;
  private boolean borderCell = false;
  private double aspectRatio = Double.NaN;
  private List<Integer>
      touchingCells =
      null;
      // holds the images labels of the cells this cell touches

  public Cell() { }

  public Cell(int label) {
    this.imgLabel = label;
  }

  /**
   * @return the label of the cell in its source image
   */
  public int getGlobalLabel() {
    return globalLabel;
  }


  /**
   * @param label the label to set
   * @return current Cell object
   */
  public Cell setGlobalLabel(int label) {
    this.globalLabel = label;
    return this;
  }

  /**
   * @return the imgLabel, the label this cell has in the image
   */
  public int getImgLabel() {
    return imgLabel;
  }

  /**
   * @return the area
   */
  public double getArea() {
    return area;
  }

  /**
   * @param area the area to set
   * @return current Cell object
   */
  public Cell setArea(double area) {
    this.area = area;
    return this;
  }

  /**
   * @return the x component of the centroid
   */
  public double getCentroidX() {
    return centroidX;
  }

  /**
   * @param x the x component of the centroid to set
   * @return current Cell object
   */
  public Cell setCentroidX(double x) {
    this.centroidX = x;
    return this;
  }

  /**
   * @return the y component of the centroid
   */
  public double getCentroidY() {
    return centroidY;
  }

  /**
   * @param y the y component of the centroid to set
   * @return current Cell object
   */
  public Cell setCentroidY(double y) {
    this.centroidY = y;
    return this;
  }

  /**
   * @return the 2 element primitive double array holding the x and y centroid coordinates {x,y}
   */
  public double[] getCentroids() {
    double[] ret = new double[2];
    ret[0] = centroidX;
    ret[1] = centroidY;
    return ret;
  }

  /**
   * @param x the x component of the centroid to set
   * @param y the y component of the centroid to set
   * @return current Cell object
   */
  public Cell setCentroids(double x, double y) {
    this.centroidX = x;
    this.centroidY = y;
    return this;
  }

  /**
   * @return the perimeter
   */
  public double getPerimeter() {
    return perimeter;
  }

  /**
   * @param perimeter the perimeter to set
   * @return current Cell object
   */
  public Cell setPerimeter(double perimeter) {
    this.perimeter = perimeter;
    return this;
  }

  /**
   * @return the circularity
   */
  public double getCircularity() {
    return circularity;
  }

  /**
   * @param circularity the circularity to set
   * @return current Cell object
   */
  public Cell setCircularity(double circularity) {
    this.circularity = circularity;
    return this;
  }

  /**
   * @return the borderCell
   */
  public boolean isBorderCell() {
    return borderCell;
  }

  /**
   * @param borderCell the borderCell to set
   * @return current Cell object
   */
  public Cell setBorderCell(boolean borderCell) {
    this.borderCell = borderCell;
    return this;
  }

  /**
   * @return the aspectRatio
   */
  public double getAspectRatio() {
    return aspectRatio;
  }

  /**
   * @param aspectRatio the aspectRatio to set
   */
  public Cell setAspectRatio(double aspectRatio) {
    this.aspectRatio = aspectRatio;
    return this;
  }

  /**
   * @return a list of cells touching this cell
   */
  public List<Integer> getTouchingCells() {
    return touchingCells;
  }

  public Cell setTouchingCells(List<Integer> touchingCells) {
    this.touchingCells = touchingCells;
    return this;
  }

  /**
   * Adds a cell index to the list of cells touching this one
   * @param c the number of the cell to add to the touching list
   */
  public void addTouchingCell(Integer c) {
    if (touchingCells == null) {
      touchingCells = new ArrayList<Integer>();
    }
    touchingCells.add(c);
  }

  /**
   * Converts cell into human readable string.
   */
  @Override
  public String toString() {
    return "Cell: " + imgLabel + " label: " + globalLabel + " area: " + (int) area + " cent: ("
           + (int) centroidX + "," + (int) centroidY + ")";
  }


}
