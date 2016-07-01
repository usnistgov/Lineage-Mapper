// NIST-developed software is provided by NIST as a public service. You may use, copy and distribute copies of the software in any medium, provided that you keep intact this entire notice. You may improve, modify and create derivative works of the software or any portion of the software, and you may copy and distribute such modifications or works. Modified works should carry a notice stating that you changed the software and should note the date and nature of any such change. Please explicitly acknowledge the National Institute of Standards and Technology as the source of the software.

// NIST-developed software is expressly provided "AS IS." NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED, IN FACT OR ARISING BY OPERATION OF LAW, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT AND DATA ACCURACY. NIST NEITHER REPRESENTS NOR WARRANTS THAT THE OPERATION OF THE SOFTWARE WILL BE UNINTERRUPTED OR ERROR-FREE, OR THAT ANY DEFECTS WILL BE CORRECTED. NIST DOES NOT WARRANT OR MAKE ANY REPRESENTATIONS REGARDING THE USE OF THE SOFTWARE OR THE RESULTS THEREOF, INCLUDING BUT NOT LIMITED TO THE CORRECTNESS, ACCURACY, RELIABILITY, OR USEFULNESS OF THE SOFTWARE.

// You are solely responsible for determining the appropriateness of using and distributing the software and you assume all risks associated with its use, including but not limited to the risks and costs of program errors, compliance with applicable laws, damage to or loss of data, programs or equipment, and the unavailability or interruption of operation. This software is not intended to be used in any situation where a failure could cause risk of injury or damage to property. The software developed by NIST employees is not subject to copyright protection within the United States.

package gov.nist.isg.lineage.mapper.lib;

import java.util.ArrayList;
import java.util.List;

/**
 * Object to hold the metadata about an individual cell being tracked
 */
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
  private List<Integer> touchingCells = null;
  // holds the images labels of the cells this cell touches

  /**
   * Object to hold the metadata about an individual cell being tracked
   */
  public Cell() {}

  /**
   * Object to hold the metadata about an individual cell being tracked
   * @param label the label of this cell in the image
   */
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
   *
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
