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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Matrix2D {

  int m;
  int n;
  double[][] data;

  public Matrix2D(int newM, int newN) {
    if (newM < 1 || newN < 1) {
      m = 0;
      n = 0;
      data = null;
      return;
    }
    m = newM;
    n = newN;
    data = new double[m][n];
  }

  /**
   * Find the unique elements in the matrix
   *
   * @param mat matrix to find the unique elements of
   * @return List of Doubles that contain the unique values
   */
  public static List<Double> unique(Matrix2D mat) {
    Set<Double> myset = new HashSet<Double>();
    for (int i = 0; i < mat.m; i++) {
      for (int j = 0; j < mat.n; j++) {
        myset.add(mat.data[i][j]);
      }
    }
    List<Double> u = new ArrayList<Double>(myset);
    Collections.sort(u);
    return u;
  }

  public void set(int i, int j, double val) {
    data[i - 1][j - 1] = val;
  }

  public void set(double i, double j, double val) {
    this.set((int) i, (int) j, val);
  }

  public double get(int i, int j) {
    return this.data[i - 1][j - 1];
  }

  public double get(double i, double j) {
    return this.get((int) i, (int) j);
  }

  public double[][] getData() {
    return data;
  }

  public void initTo(double init) {
    for (int i = 0; i < m; i++) {
      for (int j = 0; j < n; j++) {
        data[i][j] = init;
      }
    }
  }

  public int getM() {
    return this.m;
  }

  public int getN() {
    return this.n;
  }

  public int numel() {
    return this.m * this.n;
  }

  public int length() {
    return (m >= n) ? m : n;
  }

  public void increment(int i, int j) {
    data[i - 1][j - 1]++;
  }

  public void increment(double i, double j) {
    this.increment((int) i, (int) j);
  }

  public boolean isNaN(int i, int j) {
    return Double.isNaN(data[i - 1][j - 1]);
  }

  public boolean isNaN(double i, double j) {
    return this.isNaN((int) i, (int) j);
  }

  public void add(int i, int j, double val) {
    data[i - 1][j - 1] += val;
  }

  public void add(double i, double j, double val) {
    this.add((int) i, (int) j, val);
  }

  public Matrix2D sumVertical() {
    Matrix2D ret = new Matrix2D(1, n);
    for (int i = 0; i < m; i++) {
      for (int j = 0; j < n; j++) {
        ret.data[0][j] += data[i][j];
      }
    }
    return ret;
  }

  public Matrix2D sumHorizontal() {
    Matrix2D ret = new Matrix2D(m, 1);
    for (int i = 0; i < m; i++) {
      for (int j = 0; j < n; j++) {
        ret.data[i][0] += data[i][j];
      }
    }
    return ret;
  }

  public double sum() {
    double ret = 0;
    for (int i = 0; i < m; i++) {
      for (int j = 0; j < n; j++) {
        ret += data[i][j];
      }
    }
    return ret;
  }

  public double max() {
    double ret = Double.NaN;
    for (int i = 0; i < m; i++) {
      for (int j = 0; j < n; j++) {
        if (Double.isNaN(ret)) {
          ret = data[i][j];
        } else {
          ret = Math.max(ret, data[i][j]);
        }
      }
    }
    return ret;
  }

  public double min() {
    double ret = Double.NaN;
    for (int i = 0; i < m; i++) {
      for (int j = 0; j < n; j++) {
        if (Double.isNaN(ret)) {
          ret = data[i][j];
        } else {
          ret = Math.min(ret, data[i][j]);
        }
      }
    }
    return ret;
  }

  public void rowWiseMin(Matrix2D minValues, Matrix2D minIndicies) {
    if (minValues.getM() != m || minValues.getN() != 1) {
      throw new IllegalArgumentException("minValues input Matrix2D wrong size");
    }
    if (minIndicies.getM() != m || minIndicies.getN() != 1) {
      throw new IllegalArgumentException("minIndicies input Matrix2D wrong size");
    }
    // loop over the rows
    for (int i = 0; i < m; i++) {
      // find the min value  in the row
      double mv = Double.NaN;
      int indx = 0;
      for (int j = 0; j < n; j++) {
        // if the value is non nan
        if (!Double.isNaN(data[i][j])) {
          if (Double.isNaN(mv)) {
            mv = data[i][j];
            indx = j;
          } else {
            if (data[i][j] < mv) {
              mv = data[i][j];
              indx = j;
            }
          }
        }
      }
      if (Double.isNaN(mv)) {
        // +1 is to account for zero based indexing used above
        minValues.set(i + 1, 1, Double.NaN);
        minIndicies.set(i + 1, 1, Double.NaN);
      } else {
        minValues.set(i + 1, 1, mv);
        minIndicies.set(i + 1, 1, indx + 1);
      }
    }
  }

  public void colWiseMin(Matrix2D minValues, Matrix2D minIndicies) {
    if (minValues.getN() != n || minValues.getM() != 1) {
      throw new IllegalArgumentException("minValues input Matrix2D wrong size");
    }
    if (minIndicies.getN() != n || minIndicies.getM() != 1) {
      throw new IllegalArgumentException("minIndicies input Matrix2D wrong size");
    }
    // loop over the cols
    for (int j = 0; j < n; j++) {
      // find the min value in the column
      double mv = Double.NaN;
      int indx = 0;
      for (int i = 0; i < m; i++) {
        // if the value is non nan
        if (!Double.isNaN(data[i][j])) {
          if (Double.isNaN(mv)) {
            mv = data[i][j];
            indx = i;
          } else {
            if (data[i][j] < mv) {
              mv = data[i][j];
              indx = i;
            }
          }
        }
      }
      if (Double.isNaN(mv)) {
        minValues.set(1, j + 1, Double.NaN);
        minIndicies.set(1, j + 1, Double.NaN);
      } else {
        minValues.set(1, j + 1, mv);
        minIndicies.set(1, j + 1, indx + 1);
      }
    }
  }

  public void rowWiseMax(Matrix2D maxValues, Matrix2D maxIndicies) {
    if (maxValues.getM() != m || maxValues.getN() != 1) {
      throw new IllegalArgumentException("maxValues input Matrix2D wrong size");
    }
    if (maxIndicies.getM() != m || maxIndicies.getN() != 1) {
      throw new IllegalArgumentException("maxIndicies input Matrix2D wrong size");
    }
    // loop over the rows
    for (int i = 0; i < m; i++) {
      // find the min value  in the row
      double mv = Double.NaN;
      int indx = 0;
      for (int j = 0; j < n; j++) {
        // if the value is non nan
        if (!Double.isNaN(data[i][j])) {
          if (Double.isNaN(mv)) {
            mv = data[i][j];
            indx = j;
          } else {
            if (data[i][j] > mv) {
              mv = data[i][j];
              indx = j;
            }
          }
        }
      }
      if (Double.isNaN(mv)) {
        maxValues.set(i + 1, 1, Double.NaN);
        maxIndicies.set(i + 1, 1, Double.NaN);
      } else {
        maxValues.set(i + 1, 1, mv);
        maxIndicies.set(i + 1, 1, indx + 1);
      }
    }
  }

  public void colWiseMax(Matrix2D maxValues, Matrix2D maxIndicies) {
    if (maxValues.getN() != n || maxValues.getM() != 1) {
      throw new IllegalArgumentException("maxValues input Matrix2D wrong size");
    }
    if (maxIndicies.getN() != n || maxIndicies.getM() != 1) {
      throw new IllegalArgumentException("maxIndicies input Matrix2D wrong size");
    }
    // loop over the columns
    for (int j = 0; j < n; j++) {
      // find the max value  in the row
      double mv = Double.NaN;
      int indx = 0;
      for (int i = 0; i < m; i++) {
        // if the value is non nan
        if (!Double.isNaN(data[i][j])) {
          if (Double.isNaN(mv)) {
            mv = data[i][j];
            indx = i;
          } else {
            if (data[i][j] > mv) {
              mv = data[i][j];
              indx = i;
            }
          }
        }
      }
      if (Double.isNaN(mv)) {
        maxValues.set(1, j + 1, Double.NaN);
        maxIndicies.set(1, j + 1, Double.NaN);
      } else {
        maxValues.set(1, j + 1, mv);
        maxIndicies.set(1, j + 1, indx + 1);
      }
    }
  }

  /**
   * @return the number of nonzero elements in the matrix
   */
  public int nnz() {
    int ret = 0;
    for (int i = 0; i < m; i++) {
      for (int j = 0; j < n; j++) {
        if (Double.compare(data[i][j], 0) != 0) {
          ret++;
        }
      }
    }
    return ret;
  }

  /**
   * Display the matrix to the console
   */
  public void disp() {
    System.out.println("->");
    for (int i = 0; i < m; i++) {
      for (int j = 0; j < n - 1; j++) {
        System.out.print(String.format("%04.2f, ", data[i][j]));
      }
      System.out.print(String.format("%04.2f\n", data[i][n - 1]));
    }
  }

  /**
   * @return clone of the Matrix2D object
   */
  @Override
  public Matrix2D clone() {
    Matrix2D ret = new Matrix2D(m, n);
    for (int i = 0; i < m; i++) {
      for (int j = 0; j < n; j++) {
        ret.data[i][j] = data[i][j];
      }
    }
    return ret;
  }

  public void dispSparse() {
    System.out.println("->");
    for (int j = 0; j < n; j++) {
      for (int i = 0; i < m; i++) {
        if(data[i][j] != 0 && !Double.isNaN(data[i][j]) && !Double.isInfinite(data[i][j])) {
          System.out.println("(" + (i+1) + "," + (j+1) + ")  " + String.format("%04.4f", data[i][j]));
        }
      }
    }
  }

  public void writeToCSV(File f) {

    try {
      FileWriter fw = new FileWriter(f);
      PrintWriter pw = new PrintWriter(fw);

      for (int i = 0; i < m; i++) {
        for (int j = 0; j < n-1; j++) {
          pw.print(data[i][j] + ",");
        }
        pw.println(data[i][n-1]);
      }

      //Flush the output to the file
      pw.flush();
      pw.close();
      fw.close();
    } catch (IOException e) {
      Log.error(e);
    }
  }

  /**
   * Converts image tile into human readable string.
   */
  @Override
  public String toString() {
    return "Matrix2D: m=" + m + ", n=" + n;
  }

}
