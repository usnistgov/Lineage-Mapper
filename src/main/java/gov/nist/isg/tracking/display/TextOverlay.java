package main.java.gov.nist.isg.tracking.display;

import java.awt.*;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Overlay;
import ij.gui.TextRoi;
import ij.process.ImageProcessor;

/**
 * Created by mmajursk on 6/2/2014.
 */
public class TextOverlay {

  public static ImagePlus overlayLabels(ImagePlus imp) {

    Overlay o = new Overlay();
    ImageStack is = imp.getStack();

    for(int k = 0; k < is.getSize(); k++ ) {

      ImageProcessor ip = is.getProcessor(k+1);
      // compute the label centroids
      int maxVal = 0;
      int nb = ip.getWidth() * ip.getHeight();
      for (int i = 0; i < nb; i++) {
        maxVal = Math.max(maxVal, ip.get(i));
      }

      double[] xCent = new double[maxVal + 1];
      double[] yCent = new double[maxVal + 1];
      double[] size = new double[maxVal + 1];

      for (int y = 0; y < ip.getHeight(); y++) {
        for (int x = 0; x < ip.getWidth(); x++) {
          int val = ip.get(x, y);
          if (val > 0) {
            xCent[val] += x;
            yCent[val] += y;
            size[val]++;
          }
        }
      }

      for (int i = 1; i < xCent.length; i++) {
        xCent[i] /= size[i];
        yCent[i] /= size[i];
      }

      double avgSize = 0;
      nb = 0;
      for (int i = 1; i < xCent.length; i++) {
        if(size[i] > 0) {
          avgSize += size[i];
          nb++;
        }
      }
      avgSize /= nb;
      int fontSize = (int) (1.5*Math.sqrt(avgSize/Math.PI));
      fontSize = Math.max(fontSize, 6);
      fontSize = Math.min(fontSize, 18);


      for (int i = 1; i < xCent.length; i++) {
        if(size[i] > 0 ) {
          TextRoi
              roi =
              new TextRoi(xCent[i], yCent[i], (new Integer(i)).toString(),
                          (new Font(Font.MONOSPACED, Font.PLAIN, fontSize)));
          roi.setJustification(TextRoi.CENTER);
          Rectangle r = roi.getBounds();
          roi.setLocation(xCent[i] - (r.getWidth() / 2) + 1, yCent[i] - (r.getHeight() / 2) + 1);

          roi.setPosition(k + 1);
          o.addElement(roi);
        }
      }

    }
    imp.setOverlay(o);

    return imp;
  }




}

