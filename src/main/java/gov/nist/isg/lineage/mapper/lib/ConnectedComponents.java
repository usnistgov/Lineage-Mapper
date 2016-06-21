// NIST-developed software is provided by NIST as a public service. You may use, copy and distribute copies of the software in any medium, provided that you keep intact this entire notice. You may improve, modify and create derivative works of the software or any portion of the software, and you may copy and distribute such modifications or works. Modified works should carry a notice stating that you changed the software and should note the date and nature of any such change. Please explicitly acknowledge the National Institute of Standards and Technology as the source of the software.

// NIST-developed software is expressly provided "AS IS." NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED, IN FACT OR ARISING BY OPERATION OF LAW, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT AND DATA ACCURACY. NIST NEITHER REPRESENTS NOR WARRANTS THAT THE OPERATION OF THE SOFTWARE WILL BE UNINTERRUPTED OR ERROR-FREE, OR THAT ANY DEFECTS WILL BE CORRECTED. NIST DOES NOT WARRANT OR MAKE ANY REPRESENTATIONS REGARDING THE USE OF THE SOFTWARE OR THE RESULTS THEREOF, INCLUDING BUT NOT LIMITED TO THE CORRECTNESS, ACCURACY, RELIABILITY, OR USEFULNESS OF THE SOFTWARE.

// You are solely responsible for determining the appropriateness of using and distributing the software and you assume all risks associated with its use, including but not limited to the risks and costs of program errors, compliance with applicable laws, damage to or loss of data, programs or equipment, and the unavailability or interruption of operation. This software is not intended to be used in any situation where a failure could cause risk of injury or damage to property. The software developed by NIST employees is not subject to copyright protection within the United States.

package main.java.gov.nist.isg.lineage.mapper.lib;

import java.awt.Point;


public class ConnectedComponents {

  public static double[] getPerimeter(short[] pixeldata, int width, int maxval) {

    int[][] startCoords = new int[maxval][2];
    double[] perimeter = new double[maxval];
    for (int i = 0; i < perimeter.length; i++) {
      perimeter[i] = 0;
      startCoords[i][0] = -1;
      startCoords[i][1] = -1;
    }

    // loop over the image backwards looking for the first pixel rowwise
    for (int i = 0; i < pixeldata.length; i++) {
      if (pixeldata[i] > 0) {
        // if this is the first time finding a pixel
        if (startCoords[pixeldata[i] - 1][0] < 0) {
          startCoords[pixeldata[i] - 1][0] = i % width;
          startCoords[pixeldata[i] - 1][1] = i / width;
        }
      }
    }

    int xP, yP, xC, yC, xT, yT;
    Point pt;
    int dir;
    boolean done;

    for (int label = 1; label <= maxval; label++) {

      int xS = startCoords[label - 1][0];
      int yS = startCoords[label - 1][1];

      // this label does not exit
      if (xS < 0) {
        continue;
      }

      perimeter[label - 1] = 0;
      pt = new Point(xS, yS);
      dir = findNextPoint(pt, 0, pixeldata, width, label);
      xP = xS;
      yP = yS;
      xT = pt.x;
      yT = pt.y;
      xC = pt.x;
      yC = pt.y;

      // true if isolated pixel
      done = (xS == xT && yS == yT);
      while (!done) {

        pt.x = xC;
        pt.y = yC;
        dir = (dir + 5) % 8;
        dir = findNextPoint(pt, dir, pixeldata, width, label);

        xP = xC;
        yP = yC;
        xC = pt.x;
        yC = pt.y;

        int deltaX = xP - xC;
        int deltaY = yP - yC;
        perimeter[label - 1] += Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        // back at the starting point
        done = (xP == xS && yP == yS) && (xC == xT && yC == yT);
      }
    }

    return perimeter;
  }


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
