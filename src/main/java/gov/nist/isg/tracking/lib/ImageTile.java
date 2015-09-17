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

//
// ================================================================

// ================================================================
//
// Author: tjb3
// Date:   May 10, 2013 2:58:58 PM EST
//
// Time-stamp: <May 10, 2013 2:58:58 PM tjb3>
//
// Description of ImageTile.java
//
// ================================================================

package main.java.gov.nist.isg.tracking.lib;

import java.util.Arrays;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;


public class ImageTile {


  private int width = 0;
  private int height = 0;
  private short[] pixeldata = null;


  /**
   * Create an ImageTile from an ImageProcessor. The ImageTile contains exactly the same pixel data
   * as the ImagePlus image with the addition of a one pixel padding on all four sides.
   * The padded pixels have a value of zero.
   * @param ip the ImageProcessor to copy image data from.
   */
  public ImageTile(ImageProcessor ip) {

    this.width = ip.getWidth() + 2;
    this.height = ip.getHeight() + 2;
    int nbpixels = this.width * this.height;
    pixeldata = new short[nbpixels];

    // convert the image to 16 bit short
    if (ip.getBitDepth() != 16) {
      if (ip.getBitDepth() > 16) {
        Log.mandatory("Warning: Down-Converting Image To 16bit");
      } else {
        Log.mandatory("Up-Converting Image To 16bit: ");
      }

      ip = ip.convertToShort(false);
      // the false prevents the image from being rescaled as it is cast into short
    }

    // after this point ip is the image processor holding the short version of the image
    short[] pixels = (short[]) ip.getPixels();

    // copy in the pixel data while padding

    // add a zero first row
    Arrays.fill(pixeldata, 0, this.width, (short) 0);
    int k = this.width;

    // loop over the middle rows
    int subWidth = this.width - 2;

    for (int j = 1; j < this.height - 1; j++) {
      // copy over a row of the image
      pixeldata[k++] = 0; // add the padding zero at the start of the row

      int srcIdx = (j - 1) * subWidth;
      System.arraycopy(pixels, srcIdx, pixeldata, k, subWidth);
      k += subWidth;

      pixeldata[k++] = 0; // add the padding zero at the end of the row
    }
    // add a zero last row
    Arrays.fill(pixeldata, nbpixels - this.width, nbpixels, (short) 0);



    // loop over pixeldata and relabel the pixels to be sequentially numbered
    int maxval = 0;
    for(short s : pixeldata) {
      maxval = Math.max(maxval, s);
    }
    short[] u = new short[maxval+1];
    for(short s : pixeldata) {
      u[s] = 1;
    }
    short[] renum = new short[maxval+1];
    short nv = 1;
    for(int i = 1; i < u.length; i++) {
      if(u[i] > 0) {
        renum[i] = nv++;
      }
    }
    for(int i = 0; i < pixeldata.length; i++) {
      pixeldata[i] = renum[pixeldata[i]];
    }

  }

  /**
   * Create an ImagePlug object from the given pixel vector.
   * @param pixdata the short array of pixel data to use in creating the ImagePlus object.
   * @param w the width of the resulting image.
   * @param h the height of the resulting image.
   * @return an ImagePlus object w pixels wide and h pixels tall containing the pixdata pixels.
   */
  public static ImagePlus wrapAsImagePlus(short[] pixdata, int w, int h) {
    Log.debug( "Wrapping ImageTile into ImagePlus");
    // Create new ImagePlus to hold the output image
    ImagePlus image = IJ.createImage("Image", "16-bit", w, h, 1);
    image.getProcessor().setPixels(pixdata);
    return image;
  }


  /**
   * Get the highest pixel value contained within the pixel array of this ImageTile object.
   * @return a short containing the highest number in the array.
   */
  public short getMaxValue() {
    return Utils.getMaxValue(this.pixeldata);
  }

  /**
   * Find the number of pixels with each discrete short value within the input array.
   * Alternatively, find the size of each object within the labeled mask image.
   * @param pixdata the short array to find the pixel counts from.
   * @return array containing the size of each object, the histogram of the image with
   * a bin size of one.
   */
  public static int[] getObjectSizes(short[] pixdata) {
    short maxval = Utils.getMaxValue(pixdata);

    int[] objSizes = new int[maxval + 1];
    for (int i = 0; i < pixdata.length; i++) {
      objSizes[pixdata[i]]++;
    }
    return objSizes;
  }

  /**
   * Find the number of pixels with each discrete short value within this ImageTile object.
   * Alternatively, find the size of each object within the labeled mask image.
   * @return array containing the size of each object, the histogram of the image with
   * a bin size of one.
   */
  public int[] getObjectSizes() {
    return getObjectSizes(pixeldata);
  }

  /**
   * Get a copy of this ImageTile object as an ImagePlus object.
   * @return an ImagePlus version of this ImageTile without the zero padding.
   */
  public ImagePlus getAsImagePlus() {
    // undo the image padding
    short[] px = getSubImagePixelData(1, this.width - 2, 1, this.height - 2);

    // Create new ImagePlus to hold the output image
    ImagePlus image = IJ.createImage(null, "16-bit", this.width - 2, this.height - 2, 1);
    image.getProcessor().setPixels(px);
    image.setDisplayRange(0, Utils.getMaxValue(px));
    return image;
  }

  /**
   * Get an a copy of this ImageTile object as an ImagePlus object.
   * @return an ImagePlus version of this Image with the zero padding.
   */
  public ImagePlus wrapAsImagePlus() {
    // Create new ImagePlus to hold the output image
    return wrapAsImagePlus(pixeldata, this.width, this.height);
  }

  /**
   * Get a reference to the pixel vector of this ImageTile object.
   * @return a reference to the array holding the pixel data for this ImageTile.
   */
  public short[] getPixelData() {
    return pixeldata;
  }

  /**
   * Extract a sub region of this ImageTile object as a short array.
   * @param xMin the starting x coordinate of the sub region.
   * @param xMax the ending x coordinate of the sub region.
   * @param yMin the starting y coordinate of the sub region.
   * @param yMax the ending y coordinate of the sub region.
   * @return array containing the pixels from the requested sub region.
   */
  public short[] getSubImagePixelData(int xMin, int xMax, int yMin, int yMax) {
    Log.debug(
            "Extracting subimage x: " + xMin + ":" + xMax + " y: " + yMin + ":" + yMax);
    if (xMin < 0 || xMin > xMax) {
      throw new IllegalArgumentException("SubImage xMin out of bounds");
    }
    if (xMax >= this.width) {
      throw new IllegalArgumentException("SubImage xMax out of bounds");
    }
    if (yMin < 0 || yMin > yMax) {
      throw new IllegalArgumentException("SubImage yMin out of bounds");
    }
    if (yMax >= this.height) {
      throw new IllegalArgumentException("SubImage yMax out of bounds");
    }

    int subWidth = xMax - xMin + 1;
    int subHeight = yMax - yMin + 1;
    int nbPixelsInSubImage = subWidth * subHeight;
    short[] subImagePixelData = new short[nbPixelsInSubImage];

    int k = 0;
    for (int y = yMin; y <= yMax; y++) {
      // copy over a row of the image
      System.arraycopy(pixeldata, this.width * y + xMin, subImagePixelData, k, subWidth);
      k += subWidth;
    }
    return subImagePixelData;
  }

  /**
   * Copy a sub region of pixels into this ImageTile object.
   * @param pixdata the pixel data to be copied into this ImageTile object.
   * @param xMin the starting x coordinate of the sub region.
   * @param xMax the ending x coordinate of the sub region.
   * @param yMin the starting y coordinate of the sub region.
   * @param yMax the ending y coordinate of the sub region.
   */
  public void overwriteSubImage(short[] pixdata, int xMin, int xMax, int yMin, int yMax) {
    Log.debug(
            "Overwriting subimage x: " + xMin + ":" + xMax + " y: " + yMin + ":" + yMax);
    if (xMin < 0 || xMin > xMax) {
      throw new IllegalArgumentException("SubImage xMin out of bounds");
    }
    if (xMax >= this.width) {
      throw new IllegalArgumentException("SubImage xMax out of bounds");
    }
    if (yMin < 0 || yMin > yMax) {
      throw new IllegalArgumentException("SubImage yMin out of bounds");
    }
    if (yMax >= this.height) {
      throw new IllegalArgumentException("SubImage yMax out of bounds");
    }

    int subWidth = xMax - xMin + 1;

    if (pixdata.length != (subWidth * (yMax - yMin + 1))) {
      throw new IllegalArgumentException("SubImage bounds do not match the pixel data length");
    }

    int k = 0;
    for (int y = yMin; y <= yMax; y++) {
      // copy over a row of the new pixeldata
      System.arraycopy(pixdata, k, pixeldata, this.width * y + xMin, subWidth);
      k += subWidth;
    }
  }

  /**
   * Get the pixel at index k within this ImageTile pixel array.
   * @param k the index of the pixel to get.
   * @return the pixel value at index k.
   */
  public short get(int k) {
    return pixeldata[k];
  }

  /**
   * Set the pixel at index k within this ImageTile pixel array.
   * @param k the index of the pixel to set.
   * @param val the new value to place at index k.
   */
  public void set(int k, short val) {
    pixeldata[k] = val;
  }

  /**
   * Get the pixel at coordinate (x,y) within this ImageTile pixel array.
   * @param x the x coordinate of the pixel to get.
   * @param y the y coordinate of the pixel to get.
   * @return the pixel value in this ImageTile at coordinate (x,y).
   */
  public short get(int x, int y) {
    return pixeldata[y * this.width + x];
  }

  /**
   * Set the pixel at coordinate (x,y) within this ImageTile pixel array.
   * @param x the x coordinate of the pixel to set.
   * @param y the y coordinate of the pixel to set.
   * @param val the new value to place at coordinate (x,y).
   */
  public void set(int x, int y, short val) {
    pixeldata[y * this.width + x] = val;
  }



  /**
   * Get the height of this ImageTile object.
   * @return the height (in pixels).
   */
  public int getHeight() {
    return height;
  }

  /**
   * Get the width of this ImageTile object.
   * @return the width (in pixels).
   */
  public int getWidth() {
    return width;
  }

  /**
   * Set the pixel array for this ImageTile to null to allow garbage collection.
   */
  public void releaseMemory() {
    pixeldata = null;
  }

  /**
   * Clones this ImageTile object into a new copy of it.
   * @return a new clone of this ImageTile object.
   */
  @Override
  public ImageTile clone() {
    ImageTile ret = new ImageTile(this.getAsImagePlus().getProcessor());
    ret.width = this.width;
    ret.height = this.height;

    if (this.pixeldata != null) {
      ret.pixeldata = new short[this.pixeldata.length];
      System.arraycopy(this.pixeldata, 0, ret.pixeldata, 0, this.pixeldata.length);
    } else {
      ret.pixeldata = null;
    }
    return ret;
  }

  /**
   * Returns a human readable String representation of this object.
   * @return a String representation of this ImageTile.
   */
  @Override
  public String toString() {
    return "Image: " + " width: " + width + " height: " + height;
  }

}
