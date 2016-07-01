// NIST-developed software is provided by NIST as a public service. You may use, copy and distribute copies of the software in any medium, provided that you keep intact this entire notice. You may improve, modify and create derivative works of the software or any portion of the software, and you may copy and distribute such modifications or works. Modified works should carry a notice stating that you changed the software and should note the date and nature of any such change. Please explicitly acknowledge the National Institute of Standards and Technology as the source of the software.

// NIST-developed software is expressly provided "AS IS." NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED, IN FACT OR ARISING BY OPERATION OF LAW, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT AND DATA ACCURACY. NIST NEITHER REPRESENTS NOR WARRANTS THAT THE OPERATION OF THE SOFTWARE WILL BE UNINTERRUPTED OR ERROR-FREE, OR THAT ANY DEFECTS WILL BE CORRECTED. NIST DOES NOT WARRANT OR MAKE ANY REPRESENTATIONS REGARDING THE USE OF THE SOFTWARE OR THE RESULTS THEREOF, INCLUDING BUT NOT LIMITED TO THE CORRECTNESS, ACCURACY, RELIABILITY, OR USEFULNESS OF THE SOFTWARE.

// You are solely responsible for determining the appropriateness of using and distributing the software and you assume all risks associated with its use, including but not limited to the risks and costs of program errors, compliance with applicable laws, damage to or loss of data, programs or equipment, and the unavailability or interruption of operation. This software is not intended to be used in any situation where a failure could cause risk of injury or damage to property. The software developed by NIST employees is not subject to copyright protection within the United States.

package gov.nist.isg.lineage.mapper.lib;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class to hold a 2D matrix of double values, including simple processing operations that can be
 * performed on the data.
 */
public class Matrix2D {

  int m;
  int n;
  double[][] data;

  /**
   * Create a new double precision matrix.
   * @param newM the height
   * @param newN the width
   */
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

  /**
   * Initialize all elements of the matrix to the specified value.
   * @param init the value to set all matrix elements to.
   */
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

  /**
   * return a 1D matrix containing the sum of this matrix performed column-wise.
   * @return the vertical sum of this matrix (column-wise).
   */
  public Matrix2D sumVertical() {
    Matrix2D ret = new Matrix2D(1, n);
    for (int i = 0; i < m; i++) {
      for (int j = 0; j < n; j++) {
        ret.data[0][j] += data[i][j];
      }
    }
    return ret;
  }

  /**
   * return a 1D matrix containing the sum of this matrix performed row-wise.
   * @return the horizontal sum of this matrix (row-wise).
   */
  public Matrix2D sumHorizontal() {
    Matrix2D ret = new Matrix2D(m, 1);
    for (int i = 0; i < m; i++) {
      for (int j = 0; j < n; j++) {
        ret.data[i][0] += data[i][j];
      }
    }
    return ret;
  }

  /**
   * Compute the sum of all elements in this matrix
   * @return the sum of all elements in this matrix.
   */
  public double sum() {
    double ret = 0;
    for (int i = 0; i < m; i++) {
      for (int j = 0; j < n; j++) {
        ret += data[i][j];
      }
    }
    return ret;
  }

  /**
   * Compute the maximum value in this matrix.
   * @return the maximum value in the matrix.
   */
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

  /**
   * Compute the minimum value in this matrix.
   * @return the minimum value in the matrix.
   */
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

  /**
   * Find the minimum value per row (value and index).
   * @param minValues matrix to hold the row-size minimum values
   * @param minIndicies matrix to hold the index of the row-wise minimum values.
   */
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

  /**
   * Find the minimum value per column (value and index).
   * @param minValues matrix to hold the column-size minimum values
   * @param minIndicies matrix to hold the index of the column-wise minimum values.
   */
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

  /**
   * Find the maximum value per row (value and index).
   * @param maxValues matrix to hold the row-size maximum values
   * @param maxIndicies matrix to hold the index of the row-wise maximum values.
   */
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

  /**
   * Find the maximum value per column (value and index).
   * @param maxValues matrix to hold the column-size maximum values
   * @param maxIndicies matrix to hold the index of the column-wise maximum values.
   */
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
   * Display the matrix on the console
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
   * @return clone of the Matrix2D object (deep copy)
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

  /**
   * Display the matrix on the console in sparse form
   */
  public void dispSparse() {
    System.out.println("->");
    for (int j = 0; j < n; j++) {
      for (int i = 0; i < m; i++) {
        if (data[i][j] != 0 && !Double.isNaN(data[i][j]) && !Double.isInfinite(data[i][j])) {
          System.out.println("(" + (i + 1) + "," + (j + 1) + ")  " + String.format("%04.4f", data[i][j]));
        }
      }
    }
  }

  /**
   * write the matrix to a csv file
   * @param f file to write to matrix data to
   */
  public void writeToCSV(File f) {

    try {
      FileWriter fw = new FileWriter(f);
      PrintWriter pw = new PrintWriter(fw);

      for (int i = 0; i < m; i++) {
        for (int j = 0; j < n - 1; j++) {
          if(data[i][j]%1 ==0) {
            pw.print((int)data[i][j] + ",");
          }else{
            pw.print(data[i][j] + ",");
          }
        }
        if(data[i][n-1]%1 ==0) {
          pw.println((int)data[i][n - 1]);
        }else{
          pw.println(data[i][n - 1]);
        }
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
