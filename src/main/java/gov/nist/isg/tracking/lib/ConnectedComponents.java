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

import java.awt.*;


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
