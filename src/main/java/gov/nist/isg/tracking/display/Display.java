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


package main.java.gov.nist.isg.tracking.display;


import java.awt.image.ColorModel;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.VirtualStack;
import ij.process.ImageProcessor;

public class Display {


  private static void validateImages(ImagePlus labeled, ImagePlus grayscale) {

    // get the width and height of the labeled image
    int height = labeled.getHeight();
    int width = labeled.getWidth();

    // check that the two images are the same size (width and height)
    if (height != grayscale.getHeight() || width != grayscale.getWidth()) {
      // the two images were not the same size
      throw new IllegalArgumentException("Input images were different sizes.");
    }

//        // check that the two image stacks are the same size
//        if(labeled.getImageStack().getSize() != grayscale.getImageStack().getSize()) {
//            throw new IllegalArgumentException("Input Labeled image stack must have the same number of slices as the grayscale image stack");
//        }

    // get ImageStacks that are behind the ImagePlus objects
    ImageStack labelStack = labeled.getImageStack();

    // get the image processor for the labeled image
    ImageProcessor ipL = labelStack.getProcessor(1); // slices are 1 based
    if (ipL.getBitDepth() != 8 && ipL.getBitDepth() != 16) {
      // the edges in the labeled image cannot be used to generate a superimposed image
      throw new IllegalArgumentException(
          "Input Labeled image was not 8 or 16 bit. It must be a labeled mask, not RGB or 32bit floating point.");
    }
  }

  private static int convertColorString(String maskColor) {
    // create the color to use for the labeled image
    String[] validColors = {"red", "green", "blue", "black", "white"};
    int[] color = {0, 255, 0}; // green default
    for (int i = 0; i < validColors.length; i++) {
      if (maskColor.equalsIgnoreCase(validColors[i])) {
        switch (i) {
          case 0:
            color[0] = 255;
            color[1] = 0;
            color[2] = 0;
            break;
          case 1:
            color[0] = 0;
            color[1] = 255;
            color[2] = 0;
            break;
          case 2:
            color[0] = 0;
            color[1] = 0;
            color[2] = 255;
            break;
          case 3:
            color[0] = 0;
            color[1] = 0;
            color[2] = 0;
            break;
          case 4:
            color[0] = 255;
            color[1] = 255;
            color[2] = 255;
            break;
        }
        break;
      }
    }
    int colorVal = ((color[0] << 16) & 0xff0000)
                   | ((color[1] << 8) & 0xff00)
                   | (color[2] & 0xff);

    return colorVal;
  }


  // Superimposes the Images applying the color map from the grayscale image
  public static ImagePlus superimposeMask(ImagePlus labeled, ImagePlus grayscale, boolean contour,
                                          String maskColor, String virtualStackDirectory)
      throws InterruptedException {

    validateImages(labeled, grayscale); // this will throw exception if there is a problem

    // get the width and height of the labeled image
    int height = labeled.getHeight();
    int width = labeled.getWidth();

    // get ImageStacks that are behind the ImagePlus objects
    ImageStack labelStack = labeled.getImageStack();
    ImageStack grayStack = grayscale.getImageStack();

    ImageStack outputStack;
    if (virtualStackDirectory == null) {
      // create a memory based stack
      outputStack = new ImageStack(width, height);
    } else {
      // create a virtual disk based stack
      outputStack = new VirtualStack(width, height, null, virtualStackDirectory);
    }

    int colorVal = convertColorString(maskColor);
    int endIndex = Math.max(labelStack.getSize(), grayStack.getSize());
    for (int i = 0; i < endIndex; i++) {

      if (Thread.interrupted()) {
        throw new InterruptedException("Thread Interrupted");
      }

//      IJ.showProgress(i, endIndex);

      int sliceNb = i + 1; // slices are 1 based
      if (labelStack.getSize() < sliceNb) {
        sliceNb = labelStack.getSize();
      }
      IJ.showStatus(
          "Superimposing(" + i + "/" + endIndex + ") " + labelStack.getSliceLabel(sliceNb));
      ImageProcessor ipL = labelStack.getProcessor(sliceNb);

      sliceNb = i + 1; // slices are 1 based
      if (grayStack.getSize() < sliceNb) {
        sliceNb = grayStack.getSize();
      }
      ImageProcessor ipG = grayStack.getProcessor(sliceNb);

      // get the Image Processor for the current output slice
      ImageProcessor ipO = ipG.convertToRGB();

      if (contour) {
        // color just the edges of teh objects in labeled image
        for (int y = 0; y < height; y++) {
          for (int x = 0; x < width; x++) {
            // if this is foreground pixel
            if (ipL.get(x, y) > 0) {
              int pix = ipL.get(x, y);
              if (x == 0 || y == 0 || x == (width - 1) || y == (height - 1)) {
                // edge pixel
                ipO.set(x, y, colorVal);
              } else {
                // if touching a different label
                if (ipL.get(x - 1, y) != pix || ipL.get(x + 1, y) != pix || ipL.get(x, y - 1) != pix
                    || ipL.get(x, y + 1) != pix ||
                    ipL.get(x - 1, y - 1) != pix || ipL.get(x - 1, y + 1) != pix
                    || ipL.get(x + 1, y - 1) != pix || ipL.get(x + 1, y + 1) != pix) {
                  // border pixel
                  ipO.set(x, y, colorVal);
                }
              }
            }
          }
        }
      } else {
        // Color foreground pixels without regard to edges of objects

        for (int y = 0; y < height; y++) {
          for (int x = 0; x < width; x++) {
            // if this is foreground pixel
            if (ipL.get(x, y) > 0) {
              ipO.set(x, y, colorVal);
            }
          }
        }
      }

      String imgName = String.format("superimpose_%08d.tif", i);

      // add the current slice to the output stack
      if (virtualStackDirectory == null) {
        outputStack.addSlice(imgName, ipO);
      } else {
        IJ.saveAsTiff(new ImagePlus(imgName, ipO),
                      ((VirtualStack) outputStack).getDirectory() + imgName);
        ((VirtualStack) outputStack).addSlice(imgName);
      }

    }
//    IJ.showProgress(endIndex, endIndex);

    return new ImagePlus("Superimposed", outputStack);
  }


  // Superimposes the Images applying the color map from the labeled image
  public static ImagePlus superimposeLabeled(ImagePlus labeled, ImagePlus grayscale,
                                             boolean contour, String virtualStackDirectory)
      throws InterruptedException {

    validateImages(labeled, grayscale); // this will throw exception if there is a problem

    // get the width and height of the labeled image
    int height = labeled.getHeight();
    int width = labeled.getWidth();

    // get ImageStacks that are behind the ImagePlus objects
    ImageStack labelStack = labeled.getImageStack();
    ImageStack grayStack = grayscale.getImageStack();

    ImageStack outputStack;
    if (virtualStackDirectory == null) {
      // create a memory based stack
      outputStack = new ImageStack(width, height);
    } else {
      // create a virtual disk based stack
      outputStack = new VirtualStack(width, height, null, virtualStackDirectory);
    }

    ColorModel
        colorModel =
        labelStack.getProcessor(1)
            .getColorModel(); // get the color model to apply to the grayscale image
    int endIndex = Math.max(labelStack.getSize(), grayStack.getSize());
    for (int i = 0; i < endIndex; i++) {

      if (Thread.interrupted()) {
        throw new InterruptedException("Thread Interrupted");
      }
//      IJ.showProgress(i, endIndex);

      int sliceNb = i + 1; // slices are 1 based
      if (labelStack.getSize() < sliceNb) {
        sliceNb = labelStack.getSize();
      }
      IJ.showStatus(
          "Superimposing(" + i + "/" + endIndex + ") " + labelStack.getSliceLabel(sliceNb));
      ImageProcessor ipL = labelStack.getProcessor(sliceNb);

      sliceNb = i + 1;
      if (grayStack.getSize() < sliceNb) {
        sliceNb = grayStack.getSize();
      }
      ImageProcessor ipG = grayStack.getProcessor(sliceNb);

      ImageProcessor ipO = ipG.convertToRGB();

      if (contour) {
        // color just the edges of teh objects in labeled image
        for (int y = 0; y < height; y++) {
          for (int x = 0; x < width; x++) {
            // if this is foreground pixel
            if (ipL.get(x, y) > 0) {
              int pix = ipL.get(x, y);
              if (x == 0 || y == 0 || x == (width - 1) || y == (height - 1)) {
                // edge pixel
                ipO.set(x, y, colorModel.getRGB(pix));
              } else {
                // if touching a different label
                if (ipL.get(x - 1, y) != pix || ipL.get(x + 1, y) != pix || ipL.get(x, y - 1) != pix
                    || ipL.get(x, y + 1) != pix ||
                    ipL.get(x - 1, y - 1) != pix || ipL.get(x - 1, y + 1) != pix
                    || ipL.get(x + 1, y - 1) != pix || ipL.get(x + 1, y + 1) != pix) {
                  // border pixel
                  ipO.set(x, y, colorModel.getRGB(pix));
                }
              }
            }
          }
        }
      } else {
        // Color foreground pixels without regard to edges of objects

        for (int y = 0; y < height; y++) {
          for (int x = 0; x < width; x++) {
            // if this is foreground pixel
            if (ipL.get(x, y) > 0) {
              ipO.set(x, y, colorModel.getRGB(ipL.get(x, y)));
            }
          }
        }
      }

      String imgName = String.format("superimpose_%08d.tif", i);

      // add the current slice to the output stack
      if (virtualStackDirectory == null) {
        outputStack.addSlice(imgName, ipO);
      } else {
        IJ.saveAsTiff(new ImagePlus(imgName, ipO),
                      ((VirtualStack) outputStack).getDirectory() + imgName);
        ((VirtualStack) outputStack).addSlice(imgName);
      }
    }
//    IJ.showProgress(endIndex, endIndex);

    return new ImagePlus(grayscale.getTitle(), outputStack);
  }


  public static ImagePlus contour(ImagePlus labeled) {

    ImageStack labelStack = labeled.getImageStack();
    // get the image processor for the labeled image
    ImageProcessor ipL = labelStack.getProcessor(1); // slices are 1 based
    if (ipL.getBitDepth() != 8 && ipL.getBitDepth() != 16) {
      // the edges in the labeled image cannot be used to generate a superimposed image
      throw new IllegalArgumentException(
          "Input Labeled image was not 8 or 16 bit. It must be a labeled mask, not RGB or 32bit floating point.");
    }

    int width = labeled.getWidth();
    int height = labeled.getHeight();
    ImageStack outputStack = new ImageStack(width, height);
    for (int i = 0; i < labelStack.getSize(); i++) {
      ipL = labelStack.getProcessor(i + 1); // slices are 1 based

      // add the slice to the output image as an RGB image
      outputStack.addSlice(labelStack.getSliceLabel(i + 1), ipL.createProcessor(width, height));

      // get the Image Processor for the current output slice
      ImageProcessor ipO = outputStack.getProcessor(i + 1);

      // color just the edges of teh objects in labeled image
      for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
          // if this is foreground pixel
          if (ipL.get(x, y) > 0) {
            int pix = ipL.get(x, y);
            if (x == 0 || y == 0 || x == (width - 1) || y == (height - 1)) {
              // edge pixel
              ipO.set(x, y, pix);
            } else {
              // if touching a different label
              if (ipL.get(x - 1, y) != pix || ipL.get(x + 1, y) != pix || ipL.get(x, y - 1) != pix
                  || ipL.get(x, y + 1) != pix ||
                  ipL.get(x - 1, y - 1) != pix || ipL.get(x - 1, y + 1) != pix
                  || ipL.get(x + 1, y - 1) != pix || ipL.get(x + 1, y + 1) != pix) {
                // border pixel
                ipO.set(x, y, pix);
              }
            }
          }
        }
      }
    }

    return new ImagePlus(labeled.getTitle(), outputStack);
  }


}
