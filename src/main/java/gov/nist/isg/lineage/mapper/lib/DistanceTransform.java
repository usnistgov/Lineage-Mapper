// NIST-developed software is provided by NIST as a public service. You may use, copy and distribute copies of the software in any medium, provided that you keep intact this entire notice. You may improve, modify and create derivative works of the software or any portion of the software, and you may copy and distribute such modifications or works. Modified works should carry a notice stating that you changed the software and should note the date and nature of any such change. Please explicitly acknowledge the National Institute of Standards and Technology as the source of the software.

// NIST-developed software is expressly provided "AS IS." NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED, IN FACT OR ARISING BY OPERATION OF LAW, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT AND DATA ACCURACY. NIST NEITHER REPRESENTS NOR WARRANTS THAT THE OPERATION OF THE SOFTWARE WILL BE UNINTERRUPTED OR ERROR-FREE, OR THAT ANY DEFECTS WILL BE CORRECTED. NIST DOES NOT WARRANT OR MAKE ANY REPRESENTATIONS REGARDING THE USE OF THE SOFTWARE OR THE RESULTS THEREOF, INCLUDING BUT NOT LIMITED TO THE CORRECTNESS, ACCURACY, RELIABILITY, OR USEFULNESS OF THE SOFTWARE.

// You are solely responsible for determining the appropriateness of using and distributing the software and you assume all risks associated with its use, including but not limited to the risks and costs of program errors, compliance with applicable laws, damage to or loss of data, programs or equipment, and the unavailability or interruption of operation. This software is not intended to be used in any situation where a failure could cause risk of injury or damage to property. The software developed by NIST employees is not subject to copyright protection within the United States.

package main.java.gov.nist.isg.lineage.mapper.lib;


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


