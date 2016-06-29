package test.java.gov.nist.isg.lineage.mapper;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;

import ij.ImagePlus;
import main.java.gov.nist.isg.lineage.mapper.app.TrackingAppParams;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TrackingTestSuite {

  @Test
  public void divisionTracking() {
    String testDataDirectory = null;
    try {
      testDataDirectory = new File(".").getCanonicalPath() + File.separator + "test" + File
          .separator;
    } catch (IOException e) {}
    // check that the tracking test data directory exists
    assertNotNull("tracking test data directory must not be null", testDataDirectory);

    // run basic (default parameter) tracking
    RunBasicTracking.runWithDivision(testDataDirectory);

    // Check that the output metadata is the same as the reference tracking result
    // Check the birth-death csv file
    File a = new File(testDataDirectory + "test_track_results" + File.separator +
        "trk-birth-death.csv");
    File b = new File(testDataDirectory + "ref_track_results" + File.separator +
        "division" + File.separator + "trk-birth-death.csv");

    // check that the birth-death csv file matches the reference version
    assertTrue("birth-death csv file must match reference", Utils.isEqualContents(a, b));


    // Check the confidence-index file
    a = new File(testDataDirectory + "test_track_results" + File.separator +
        "trk-confidence-index.csv");
    b = new File(testDataDirectory + "ref_track_results" + File.separator +
        "division" + File.separator + "trk-confidence-index.csv");

    // check that the confidence-index csv file matches the reference version
    assertTrue("confidence-index csv file must match reference", Utils.isEqualContents(a, b));


    // Check the division file
    a = new File(testDataDirectory + "test_track_results" + File.separator +
        "trk-division.csv");
    b = new File(testDataDirectory + "ref_track_results" + File.separator +
        "division" + File.separator + "trk-division.csv");

    // check that the division csv file matches the reference version
    assertTrue("division csv file must match reference", Utils.isEqualContents(a, b));


    // Check the output tracked images
    DecimalFormat df = new DecimalFormat("000");
    for(int i = 1; i <= 10; i++) {
      String filename = "trk-seg_" + df.format(i) + ".tif";
      ImagePlus imgA = new ImagePlus(testDataDirectory + "test_track_results" + File.separator +
          filename);
      ImagePlus imgB = new ImagePlus(testDataDirectory + "ref_track_results" + File.separator +
          "division" + File.separator + filename);

      short[] pixelsA = (short[]) imgA.getProcessor().convertToShort(false).getPixels();
      short[] pixelsB = (short[]) imgB.getProcessor().convertToShort(false).getPixels();

      // check that both tracked images are equal
      assertTrue("tracked image " + filename + " must match reference", Arrays.equals(pixelsA, pixelsB));
    }
  }

  @Test
  public void fusionTracking() {
    String testDataDirectory = null;
    try {
      testDataDirectory = new File(".").getCanonicalPath() + File.separator + "test" + File
          .separator;
    } catch (IOException e) {}
    // check that the tracking test data directory exists
    assertNotNull("tracking test data directory must not be null", testDataDirectory);

    // run basic (default parameter) tracking
    RunBasicTracking.runWithFusion(testDataDirectory);

    // Check that the output metadata is the same as the reference tracking result
    // Check the birth-death csv file
    File a = new File(testDataDirectory + "test_track_results" + File.separator +
        "trk-birth-death.csv");
    File b = new File(testDataDirectory + "ref_track_results" + File.separator +
        "fusion" + File.separator + "trk-birth-death.csv");

    // check that the birth-death csv file matches the reference version
    assertTrue("birth-death csv file must match reference", Utils.isEqualContents(a, b));


    // Check the confidence-index file
    a = new File(testDataDirectory + "test_track_results" + File.separator +
        "trk-confidence-index.csv");
    b = new File(testDataDirectory + "ref_track_results" + File.separator +
        "fusion" + File.separator + "trk-confidence-index.csv");

    // check that the confidence-index csv file matches the reference version
    assertTrue("confidence-index csv file must match reference", Utils.isEqualContents(a, b));


    // Check the fusion file
    a = new File(testDataDirectory + "test_track_results" + File.separator +
        "trk-fusion.csv");
    b = new File(testDataDirectory + "ref_track_results" + File.separator +
        "fusion" + File.separator + "trk-fusion.csv");

    // check that the fusion csv file matches the reference version
    assertTrue("fusion csv file must match reference", Utils.isEqualContents(a, b));


    // Check the output tracked images
    DecimalFormat df = new DecimalFormat("000");
    for(int i = 1; i <= 10; i++) {
      String filename = "trk-seg_" + df.format(i) + ".tif";
      ImagePlus imgA = new ImagePlus(testDataDirectory + "test_track_results" + File.separator +
          filename);
      ImagePlus imgB = new ImagePlus(testDataDirectory + "ref_track_results" + File.separator +
          "fusion" + File.separator + filename);

      short[] pixelsA = (short[]) imgA.getProcessor().convertToShort(false).getPixels();
      short[] pixelsB = (short[]) imgB.getProcessor().convertToShort(false).getPixels();

      // check that both tracked images are equal
      assertTrue("tracked image " + filename + " must match reference", Arrays.equals(pixelsA, pixelsB));
    }
  }


  @Test
  public void divisionFusionTracking() {
    String testDataDirectory = null;
    try {
      testDataDirectory = new File(".").getCanonicalPath() + File.separator + "test" + File
          .separator;
    } catch (IOException e) {}
    // check that the tracking test data directory exists
    assertNotNull("tracking test data directory must not be null", testDataDirectory);

    // run basic (default parameter) tracking
    RunBasicTracking.runWithDivisionFusion(testDataDirectory);

    // Check that the output metadata is the same as the reference tracking result
    // Check the birth-death csv file
    File a = new File(testDataDirectory + "test_track_results" + File.separator +
        "trk-birth-death.csv");
    File b = new File(testDataDirectory + "ref_track_results" + File.separator +
        "divisionfusion" + File.separator + "trk-birth-death.csv");

    // check that the birth-death csv file matches the reference version
    assertTrue("birth-death csv file must match reference", Utils.isEqualContents(a, b));


    // Check the confidence-index file
    a = new File(testDataDirectory + "test_track_results" + File.separator +
        "trk-confidence-index.csv");
    b = new File(testDataDirectory + "ref_track_results" + File.separator +
        "divisionfusion" + File.separator + "trk-confidence-index.csv");

    // check that the confidence-index csv file matches the reference version
    assertTrue("confidence-index csv file must match reference", Utils.isEqualContents(a, b));


    // Check the fusion file
    a = new File(testDataDirectory + "test_track_results" + File.separator +
        "trk-fusion.csv");
    b = new File(testDataDirectory + "ref_track_results" + File.separator +
        "divisionfusion" + File.separator + "trk-fusion.csv");

    // check that the fusion csv file matches the reference version
    assertTrue("fusion csv file must match reference", Utils.isEqualContents(a, b));


    // Check the output tracked images
    DecimalFormat df = new DecimalFormat("000");
    for(int i = 1; i <= 10; i++) {
      String filename = "trk-seg_" + df.format(i) + ".tif";
      ImagePlus imgA = new ImagePlus(testDataDirectory + "test_track_results" + File.separator +
          filename);
      ImagePlus imgB = new ImagePlus(testDataDirectory + "ref_track_results" + File.separator +
          "divisionfusion" + File.separator + filename);

      short[] pixelsA = (short[]) imgA.getProcessor().convertToShort(false).getPixels();
      short[] pixelsB = (short[]) imgB.getProcessor().convertToShort(false).getPixels();

      // check that both tracked images are equal
      assertTrue("tracked image " + filename + " must match reference", Arrays.equals(pixelsA, pixelsB));
    }
  }

  @Test
  public void jarHelpPrintTest() {
    TrackingAppParams.printParameterHelp();
  }

}