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


package test;


import java.io.File;
import java.util.List;

import ij.ImagePlus;
import ij.ImageStack;
import ij.VirtualStack;
import main.java.gov.nist.isg.tracking.lib.TrackerFileManager;

public class BuildImageStack {


  public static ImageStack builtStack() {
//        File segmentedImagesDirectory = new File("/Users/mmajurski/Workspace/NIST/celltracker/testdata/NIH_BC_segmented_images/");
//    File segmentedImagesDirectory = new File("C:\\majurski\\image-data\\temp\\CT_Test\\");
    File segmentedImagesDirectory = new File("E:\\image-data\\Cell_Tracking_Paper_Datasets\\NIH_3T3\\segmented_images\\");
    String segCommonName = "segmented_";

    // build file list
    List<File> imgs = TrackerFileManager.findFilesInDirectory(segmentedImagesDirectory);
    imgs = TrackerFileManager.filterFilenamesBySuffix(imgs, "tif");
    imgs = TrackerFileManager.findFilesByCommonName(imgs, segCommonName);

    ImagePlus imp = new ImagePlus(imgs.get(0).getAbsolutePath());
    int w = imp.getWidth();
    int h = imp.getHeight();

    ImageStack output = new ImageStack(w, h);

    for (File f : imgs) {
      imp = new ImagePlus(f.getAbsolutePath());
      output.addSlice(imp.getTitle(), imp.getProcessor());
    }

    return output;
  }


  public static ImageStack buildVirtualStack() {

//        File segmentedImagesDirectory = new File("/Users/mmajurski/Workspace/NIST/celltracker/testdata/NIH_BC_segmented_images/");
    File segmentedImagesDirectory = new File("C:\\majurski\\image-data\\temp\\CT_Test\\");
    String segCommonName = "segmented_";

    // build file list
    List<File> imgs = TrackerFileManager.findFilesInDirectory(segmentedImagesDirectory);
    imgs = TrackerFileManager.filterFilenamesBySuffix(imgs, "tif");
    imgs = TrackerFileManager.findFilesByCommonName(imgs, segCommonName);
    imgs = imgs.subList(0,50);

    ImagePlus imp = new ImagePlus(imgs.get(0).getAbsolutePath());
    int w = imp.getWidth();
    int h = imp.getHeight();

    VirtualStack output = new VirtualStack(w, h, null, segmentedImagesDirectory.getAbsolutePath());
    //addSlice(java.lang.String sliceLabel, ImageProcessor ip)

    for (File f : imgs) {
      output.addSlice(f.getName());
    }

    return output;

  }

}
