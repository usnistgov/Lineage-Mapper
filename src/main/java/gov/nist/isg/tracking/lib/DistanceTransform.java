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


public class DistanceTransform {

  public static void assignNearestConnectedLabel(short[] marker, final short[] mask, int width,
                                                 int height) {

    int k;
    myStack pixStack = new myStack(height);
    myStack edgeStack = new myStack(height);
    myStack nameSwap;

    // loop over the marker image looking for edge pixels
    // start at index 1,1 and end and (m-1,n-1)
    int start = width + 1;
    int end = marker.length - width - 1;
    for (int i = start; i < end; i++) {
      // if not background
      if (marker[i] > 0) {
        if (marker[i - width - 1] > 0 || marker[i - width] > 0 || marker[i - width + 1] > 0 ||
            // top left, top, top right
            marker[i - 1] > 0 || marker[i + 1] > 0 || // left , right
            marker[i + width - 1] > 0 || marker[i + width] > 0
            || marker[i + width + 1] > 0) { // bottom left, bottom, bottom right
          edgeStack.push(i);
        }
      }
    }

    int label;
    while (!edgeStack.isEmpty()) {

      // loop over the 4 connected edge pixels
      for (int i = 0; i < edgeStack.nbElements; i++) {
        k = edgeStack.data[i];

        label = marker[k];
        // check left pixel
        k = k - 1;
        if (marker[k] == 0 && mask[k] > 0) {
          marker[k] = (short) label;
          pixStack.push(k);
          edgeStack.data[i] = -1;
        }
        k = k - width + 1; // top
        if (marker[k] == 0 && mask[k] > 0) {
          marker[k] = (short) label;
          pixStack.push(k);
          edgeStack.data[i] = -1;
        }
        k = k + width + width; // bottom
        if (marker[k] == 0 && mask[k] > 0) {
          marker[k] = (short) label;
          pixStack.push(k);
          edgeStack.data[i] = -1;
        }
        k = k - width + 1; // right
        if (marker[k] == 0 && mask[k] > 0) {
          marker[k] = (short) label;
          pixStack.push(k);
          edgeStack.data[i] = -1;
        }
      }

      // loop over uncovered 8 connected
      for (int i = 0; i < edgeStack.nbElements; i++) {
        k = edgeStack.data[i];

        if (k >= 0) {
          label = marker[k];
          k = k - width - 1; // top left
          if (marker[k] == 0 && mask[k] > 0) {
            marker[k] = (short) label;
            pixStack.push(k);
          }
          k = k + width + width; // bottom left
          if (marker[k] == 0 && mask[k] > 0) {
            marker[k] = (short) label;
            pixStack.push(k);
          }
          k = k - width - width + 2;  // top right
          if (marker[k] == 0 && mask[k] > 0) {
            marker[k] = (short) label;
            pixStack.push(k);
          }
          k = k + width + width; // bottom right
          if (marker[k] == 0 && mask[k] > 0) {
            marker[k] = (short) label;
            pixStack.push(k);
          }
        }
      }

      // swap the stacks
      // pointer name change so that edgeStack always has the current list of edge pixels
      nameSwap = edgeStack;
      edgeStack = pixStack;
      pixStack = nameSwap;
      pixStack.clear();

      // sort the pixel locations so that any pixels that are equal distance from 2 objects will be handled in pixel order
      quickSort(pixStack.data, 0, pixStack.nbElements - 1);
    }
  }


  public static double[] geodesicDistance(short[] marker, final short[] mask, int width,
                                          int height) {

    int k;
    myStack pixStack = new myStack(height);
    myStack edgeStack = new myStack(height);
    myStack nameSwap;

    if (mask.length != marker.length) {
      throw new IllegalArgumentException("mask and marker must be the same size");
    }
    if (mask.length != width * height) {
      throw new IllegalArgumentException(
          "width times height must match the number of elements in mask");
    }

    double[] dist = new double[mask.length];
    // initialize dist to infinity
    for (int i = 0; i < dist.length; i++) {
      dist[i] = Double.POSITIVE_INFINITY;
    }
    // any dist found under a marker pixel has a distance of zero
    for (int i = 0; i < dist.length; i++) {
      if (marker[i] > 0) {
        dist[i] = 0;
      }
    }
    // any dist under a pixel not valid in the mask is NaN
    for (int i = 0; i < dist.length; i++) {
      if (mask[i] == 0) {
        dist[i] = Double.NaN;
      }
    }

    // loop over the marker image looking for edge pixels
    // start at index 1,1 and end and (m-1,n-1)
    int start = width + 1;
    int end = marker.length - width - 1;
    for (int i = start; i < end; i++) {
      // if not background
      if (marker[i] > 0) {
        if (marker[i - width - 1] > 0 || marker[i - width] > 0 || marker[i - width + 1] > 0 ||
            // top left, top, top right
            marker[i - 1] > 0 || marker[i + 1] > 0 || // left , right
            marker[i + width - 1] > 0 || marker[i + width] > 0
            || marker[i + width + 1] > 0) { // bottom left, bottom, bottom right
          edgeStack.push(i);
        }
      }
    }

    int iterationCount = 0;
    int label;
    while (!edgeStack.isEmpty()) {
      iterationCount++;

      // loop over the 4 connected edge pixels
      for (int i = 0; i < edgeStack.nbElements; i++) {
        k = edgeStack.data[i];

        label = marker[k];
        // check left pixel
        k = k - 1;
        if (marker[k] == 0 && mask[k] > 0) {
          marker[k] = (short) label;
          pixStack.push(k);
          edgeStack.data[i] = -1;
        }
        k = k - width + 1; // top
        if (marker[k] == 0 && mask[k] > 0) {
          marker[k] = (short) label;
          pixStack.push(k);
          edgeStack.data[i] = -1;
        }
        k = k + width + width; // bottom
        if (marker[k] == 0 && mask[k] > 0) {
          marker[k] = (short) label;
          pixStack.push(k);
          edgeStack.data[i] = -1;
        }
        k = k - width + 1; // right
        if (marker[k] == 0 && mask[k] > 0) {
          marker[k] = (short) label;
          pixStack.push(k);
          edgeStack.data[i] = -1;
        }
      }

      // loop over uncovered 8 connected
      for (int i = 0; i < edgeStack.nbElements; i++) {
        k = edgeStack.data[i];

        if (k >= 0) {
          label = marker[k];
          k = k - width - 1; // top left
          if (marker[k] == 0 && mask[k] > 0) {
            marker[k] = (short) label;
            pixStack.push(k);
          }
          k = k + width + width; // bottom left
          if (marker[k] == 0 && mask[k] > 0) {
            marker[k] = (short) label;
            pixStack.push(k);
          }
          k = k - width - width + 2;  // top right
          if (marker[k] == 0 && mask[k] > 0) {
            marker[k] = (short) label;
            pixStack.push(k);
          }
          k = k + width + width; // bottom right
          if (marker[k] == 0 && mask[k] > 0) {
            marker[k] = (short) label;
            pixStack.push(k);
          }
        }
      }

      // update the distance values found
      for (int i = 0; i < pixStack.data.length; i++) {
        dist[pixStack.data[i]] = (double) iterationCount;
      }

      // swap the stacks
      // pointer name change so that edgeStack always has the current list of edge pixels
      nameSwap = edgeStack;
      edgeStack = pixStack;
      pixStack = nameSwap;
      pixStack.clear();

      // sort the pixel locations so that any pixels that are equal distance from 2 objects will be handled in pixel order
      quickSort(pixStack.data, 0, pixStack.nbElements - 1);
    }
    return dist;
  }

  private static int partition(int arr[], int left, int right) {
    int i = left, j = right;
    int tmp;
    int pivot = arr[(left + right) / 2];

    while (i <= j) {
      while (arr[i] < pivot) {
        i++;
      }
      while (arr[j] > pivot) {
        j--;
      }
      if (i <= j) {
        tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
        i++;
        j--;
      }
    }
    ;

    return i;
  }

  private static void quickSort(int arr[], int left, int right) {
    int index = partition(arr, left, right);
    if (left < index - 1) {
      quickSort(arr, left, index - 1);
    }
    if (index < right) {
      quickSort(arr, index, right);
    }
  }

  private static class myStack {

    protected int[] data;
    protected int nbElements;
    private int arraySize;

    myStack(int newsize) {
      data = new int[newsize];
      nbElements = 0;
      arraySize = newsize;
    }

    public void push(int val) {
      if (nbElements >= arraySize) {
        int[] temp = data;
        arraySize = arraySize * 2;
        data = new int[arraySize];
        for (int i = 0; i < nbElements; i++) {
          data[i] = temp[i];
        }
      }
      data[nbElements++] = val;
    }

    public boolean isEmpty() {
      return nbElements == 0;
    }

    public void clear() {
      nbElements = 0;
    }

  }


}


