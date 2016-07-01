// NIST-developed software is provided by NIST as a public service. You may use, copy and distribute copies of the software in any medium, provided that you keep intact this entire notice. You may improve, modify and create derivative works of the software or any portion of the software, and you may copy and distribute such modifications or works. Modified works should carry a notice stating that you changed the software and should note the date and nature of any such change. Please explicitly acknowledge the National Institute of Standards and Technology as the source of the software.

// NIST-developed software is expressly provided "AS IS." NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED, IN FACT OR ARISING BY OPERATION OF LAW, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT AND DATA ACCURACY. NIST NEITHER REPRESENTS NOR WARRANTS THAT THE OPERATION OF THE SOFTWARE WILL BE UNINTERRUPTED OR ERROR-FREE, OR THAT ANY DEFECTS WILL BE CORRECTED. NIST DOES NOT WARRANT OR MAKE ANY REPRESENTATIONS REGARDING THE USE OF THE SOFTWARE OR THE RESULTS THEREOF, INCLUDING BUT NOT LIMITED TO THE CORRECTNESS, ACCURACY, RELIABILITY, OR USEFULNESS OF THE SOFTWARE.

// You are solely responsible for determining the appropriateness of using and distributing the software and you assume all risks associated with its use, including but not limited to the risks and costs of program errors, compliance with applicable laws, damage to or loss of data, programs or equipment, and the unavailability or interruption of operation. This software is not intended to be used in any situation where a failure could cause risk of injury or damage to property. The software developed by NIST employees is not subject to copyright protection within the United States.

package gov.nist.isg.lineage.mapper.lib;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Collection of connected components labeling methods.
 */
public class ConnectedComponents {

  /**
   * computes the perimeter of the labeled objects within the pixeldata.
   *
   * This algorithm matches the MATLAB regionprops.computePerimeterFromBoundaryOld (aka the
   * option PerimeterOld)
   * @param pixeldata array of pixels
   * @param width the width of the image
   * @param maxval the highest pixel value found within pixeldata
   * @return array of object perimeter lengths
   */
  public static double[] getPerimeter(short[] pixeldata, int width, int maxval) {

    Map<Integer, int[][]> B = getBoundaryPixelList(pixeldata, width, maxval);
    double[] perimeter = new double[maxval];
    for (int label = 1; label <= maxval; label++) {

      int[][] boundaryList = B.get(label);
      // if this object label does not exist in the pixel data
      if(boundaryList == null)
        continue;

      perimeter[label-1] = 0;
      for(int k = 1; k < boundaryList.length; k++) {
        double dx =  boundaryList[k][0] - boundaryList[k-1][0];
        double dy =  boundaryList[k][1] - boundaryList[k-1][1];
        double delta = Math.sqrt(dx*dx + dy*dy);
        perimeter[label-1] += delta;
      }
    }

    return perimeter;
  }



  public static Map<Integer,int[][]> getBoundaryPixelList(short[] pixeldata, int width, int maxval) {
    Map<Integer,int[][]> B = new HashMap<Integer,int[][]>(maxval+1);

    int[][] startCoords = new int[maxval][2];
    for (int i = 0; i < maxval; i++) {
      startCoords[i][0] = -1;
      startCoords[i][1] = -1;
    }

    // find the first pixel row-wise
    for (int i = 0; i < pixeldata.length; i++) {
      if (pixeldata[i] > 0) {
        // if this is the first time finding a pixel
        if (startCoords[pixeldata[i] - 1][0] < 0) {
          startCoords[pixeldata[i] - 1][0] = i % width;
          startCoords[pixeldata[i] - 1][1] = i / width;
        }
      }
    }


    int xP, yP, xT, yT;
    Point pt;
    int dir;
    boolean done;

    // Loop over the labeled objects in the image
    for (int label = 1; label <= maxval; label++) {

      int xS = startCoords[label - 1][0];
      int yS = startCoords[label - 1][1];

      // this label does not exit
      if (xS < 0)
        continue;

      // allocate two lists to store the bound pixel locations
      List<Integer> xLocs = new ArrayList<Integer>();
      List<Integer> yLocs = new ArrayList<Integer>();

      pt = new Point(xS, yS);
      // add the starting point
      xLocs.add(pt.x);
      yLocs.add(pt.y);

      dir = findNextPoint(pt, 0, pixeldata, width, label);
      xT = pt.x;
      yT = pt.y;

      // true if isolated pixel
      done = (xS == pt.x && yS == pt.y);
      while (!done) {
        // add the point to the perimeter list
        xLocs.add(pt.x);
        yLocs.add(pt.y);

        // record the previous point
        xP = pt.x;
        yP = pt.y;
        // find the next point
        dir = (dir + 5) % 8;
        dir = findNextPoint(pt, dir, pixeldata, width, label);

        // back at the starting point, entering from the same direction
        done = (xP == xS && yP == yS) && (pt.x == xT && pt.y == yT);
      }

      // copy the boundary points into a 2D array
      int[][] boundaryList = new int[xLocs.size()][2];
      for(int i = 0; i < xLocs.size(); i++) {
        boundaryList[i][0] = xLocs.get(i);
        boundaryList[i][1] = yLocs.get(i);
      }
      // add this labeled objects boundary to the output list
      B.put(label, boundaryList);
    }

    return B;
  }

  /**
   * worker method to find the next point along the perimeter within a 3x3 neighborhood.
   * The Point passed in is updated with the new pixel location.
   * @param pt the current point (pixel location) within the image
   * @param dir the direction to start searching in
   * @param px the array of pixels
   * @param width the width of the image
   * @param label the object label to find the next point of. Only pixel values matching this are
   *              considered for the next pixel.
   * @return the direction of the next pixel along the perimeter.
   */
  private static int findNextPoint(Point pt, int dir, short[] px, int width, int label) {
    final int[][] delta = {{1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}, {0, -1}, {1, -1}};

    for (int i = 0; i < 7; i++) {
      int x = pt.x + delta[dir][0];
      int y = pt.y + delta[dir][1];
      if (px[y * width + x] == label) {
        pt.x = x;
        pt.y = y;
        break;
      } else {
        // not the right label
        dir = (dir + 1) % 8;
      }
    }

    return dir;
  }


  /**
   * Method to label the connected components of a binary image according to 8 connectedness.
   * This method modifies the pixeldata array passed to it.
   *
   * @param pixeldata the array of pizel data
   * @param n the width of the image
   * @param m the height of the image
   * @return the number of connected components found.
   */
  public static int labelConnectedComponents(short[] pixeldata, int n, int m) {

    short[] nbs = {0, 0, 0, 0};
    short label = 1;
    UnionFind uf = new UnionFind(m);

    short minval;
    int k;
    int endindx;

    // loop over row 0
    k = 0;
    if (pixeldata[k] > 0) {
      pixeldata[k] = label++;
    }
    for (k = 1; k < n; k++) {
      if (pixeldata[k] > 0) {
        if (pixeldata[k - 1] == 0) {
          pixeldata[k] = label++;
        } else {
          pixeldata[k] = pixeldata[k - 1];
        }
      }
    }

    // loop over rows 1 -> m-1
    for (int i = 1; i < m; i++) {
      // check the first element in the row
      k = i * n;
      if (pixeldata[k] > 0) {
        nbs[0] = pixeldata[k - n];
        nbs[1] = pixeldata[k - n + 1];
        if (nbs[0] == 0 && nbs[1] == 0) {
          pixeldata[k] = label++;
        } else {
          minval = Short.MAX_VALUE;
          if (nbs[0] > 0) {
            minval = (minval <= nbs[0]) ? minval : nbs[0];
          }
          if (nbs[1] > 0) {
            minval = (minval <= nbs[1]) ? minval : nbs[1];
          }
          pixeldata[k] = minval;
          if (nbs[0] > 0 && nbs[1] > 0) {
            uf.union(nbs[0], nbs[1]);
          }
        }
      }

      // loop over the current row except the last element
      endindx = k + n - 1;
      k++;
      for (; k < endindx; k++) {
        if (pixeldata[k] > 0) {
          nbs[0] = pixeldata[k - n - 1];
          nbs[1] = pixeldata[k - n];
          nbs[2] = pixeldata[k - n + 1];
          nbs[3] = pixeldata[k - 1];
          if (nbs[0] == 0 && nbs[1] == 0 && nbs[2] == 0 && nbs[3] == 0) {
            pixeldata[k] = label++;
          } else {
            minval = Short.MAX_VALUE;
            if (nbs[0] > 0) {
              minval = (minval <= nbs[0]) ? minval : nbs[0];
            }
            if (nbs[1] > 0) {
              minval = (minval <= nbs[1]) ? minval : nbs[1];
            }
            if (nbs[2] > 0) {
              minval = (minval <= nbs[2]) ? minval : nbs[2];
            }
            if (nbs[3] > 0) {
              minval = (minval <= nbs[3]) ? minval : nbs[3];
            }
            pixeldata[k] = minval;
            if (nbs[0] > 0) {
              if (nbs[1] > 0) {
                uf.union(nbs[0], nbs[1]);
              }
              if (nbs[2] > 0) {
                uf.union(nbs[0], nbs[2]);
              }
              if (nbs[3] > 0) {
                uf.union(nbs[0], nbs[3]);
              }
            }
            if (nbs[1] > 0) {
              if (nbs[2] > 0) {
                uf.union(nbs[1], nbs[2]);
              }
              if (nbs[3] > 0) {
                uf.union(nbs[1], nbs[3]);
              }
            }
            if (nbs[2] > 0) {
              if (nbs[3] > 0) {
                uf.union(nbs[2], nbs[3]);
              }
            }
          }
        }
      }

      // handle last element in the current row
      if (pixeldata[k] > 0) {
        nbs[0] = pixeldata[k - n - 1];
        nbs[1] = pixeldata[k - n];
        nbs[2] = pixeldata[k - 1];
        if (nbs[0] == 0 && nbs[1] == 0 && nbs[2] == 0) {
          pixeldata[k] = label++;
        } else {
          minval = Short.MAX_VALUE;
          if (nbs[0] > 0) {
            minval = (minval <= nbs[0]) ? minval : nbs[0];
          }
          if (nbs[1] > 0) {
            minval = (minval <= nbs[1]) ? minval : nbs[1];
          }
          if (nbs[2] > 0) {
            minval = (minval <= nbs[2]) ? minval : nbs[2];
          }
          pixeldata[k] = minval;
          if (nbs[0] > 0) {
            if (nbs[1] > 0) {
              uf.union(nbs[0], nbs[1]);
            }
            if (nbs[2] > 0) {
              uf.union(nbs[0], nbs[2]);
            }
          }
          if (nbs[1] > 0) {
            if (nbs[2] > 0) {
              uf.union(nbs[1], nbs[2]);
            }
          }
        }
      }
    }

    // merge the label sets according to the equivalences in the union find data structure.
    short[] labels = new short[label];
    for (int i = 0; i < label; i++) {
      labels[uf.root(i)] = 1;
    }
    int newNb;
    if (labels[0] == 1) {
      newNb = 0;
    } else {
      newNb = 1;
    }
    for (int i = 0; i < label; i++) {
      if (labels[i] > 0) {
        labels[i] = (short) newNb++;
      }
    }

    // second pass to relabel the image
    for (int i = 0; i < pixeldata.length; i++) {
      if (pixeldata[i] > 0) {
        pixeldata[i] = labels[uf.root(pixeldata[i])];
      }
    }

    return (newNb - 1);
  }
}
