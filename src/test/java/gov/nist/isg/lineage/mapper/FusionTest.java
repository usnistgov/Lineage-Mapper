// NIST-developed software is provided by NIST as a public service. You may use, copy and distribute copies of the software in any medium, provided that you keep intact this entire notice. You may improve, modify and create derivative works of the software or any portion of the software, and you may copy and distribute such modifications or works. Modified works should carry a notice stating that you changed the software and should note the date and nature of any such change. Please explicitly acknowledge the National Institute of Standards and Technology as the source of the software.

// NIST-developed software is expressly provided "AS IS." NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED, IN FACT OR ARISING BY OPERATION OF LAW, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT AND DATA ACCURACY. NIST NEITHER REPRESENTS NOR WARRANTS THAT THE OPERATION OF THE SOFTWARE WILL BE UNINTERRUPTED OR ERROR-FREE, OR THAT ANY DEFECTS WILL BE CORRECTED. NIST DOES NOT WARRANT OR MAKE ANY REPRESENTATIONS REGARDING THE USE OF THE SOFTWARE OR THE RESULTS THEREOF, INCLUDING BUT NOT LIMITED TO THE CORRECTNESS, ACCURACY, RELIABILITY, OR USEFULNESS OF THE SOFTWARE.

// You are solely responsible for determining the appropriateness of using and distributing the software and you assume all risks associated with its use, including but not limited to the risks and costs of program errors, compliance with applicable laws, damage to or loss of data, programs or equipment, and the unavailability or interruption of operation. This software is not intended to be used in any situation where a failure could cause risk of injury or damage to property. The software developed by NIST employees is not subject to copyright protection within the United States.

package gov.nist.isg.lineage.mapper;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;

import gov.nist.isg.lineage.mapper.app.TrackingAppParams;
import gov.nist.isg.lineage.mapper.lib.Log;
import ij.ImagePlus;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class FusionTest {

  private static String testReferenceFolder = "fusion";

  @BeforeClass
  public static void runTracking() {

    String testDataDirectory = null;
    try {
      testDataDirectory = new File(".").getCanonicalPath() + File.separator + "test" + File
          .separator;
    } catch (IOException e) {}
    // check that the tracking test data directory exists
    assertNotNull("tracking test data directory must not be null", testDataDirectory);

    // run basic (default parameter) tracking
    TrackingAppParams params = new TrackingAppParams();
    params.setInputDirectory(testDataDirectory);
    params.setFilenamePattern("seg_{iii}.tif");
    params.setOutputDirectory(testDataDirectory + File.separator + "junit_results" + File.separator);
    params.setOutputPrefix("trk-");

    // set the default parameters
    params.setWeightCellOverlap(1);
    params.setWeightCentroids(0.5);
    params.setWeightCellSize(0.2);

    params.setMaxCentroidsDist(50);
    params.setDivisionOverlapThreshold(0.2);
    params.setMinCellLife(32);
    params.setCellDeathDeltaTreshold(10);
    params.setDaughterSizeSimilarity(0.5);
    params.setDaughterAspectRatioSimilarity(0.7);
    params.setMotherCircularityThreshold(0.3);
    params.setNumFramesToCheckCircularity(5);
    params.setEnableCellDivision(false);
    params.setEnableCellFusion(true);
    params.setFusionOverlapThreshold(0.2);
    params.setCellSizeThreshold(100);

    params.setCellDensityAffectsCI(true);
    params.setBorderCellAffectsCI(true);

    params.setIsMacro(true); // disables auto-open of lineage viewer webpage

    LineageMapper lm = new LineageMapper(params);
    lm.run();
  }

  @Test
  public void checkBirthDeathMetadata() {
    Log.mandatory("Checking the birth and death metadata");
    String testDataDirectory = null;
    try {
      testDataDirectory = new File(".").getCanonicalPath() + File.separator + "test" + File
          .separator;
    } catch (IOException e) {}
    // check that the tracking test data directory exists
    assertNotNull("tracking test data directory must not be null", testDataDirectory);

    // Check that the output metadata is the same as the reference tracking result
    // Check the birth-death csv file
    File a = new File(testDataDirectory + "junit_results" + File.separator +
        "trk-birth-death.csv");
    File b = new File(testDataDirectory + "ref_track_results" + File.separator +
        testReferenceFolder + File.separator + "trk-birth-death.csv");

    // check that the birth-death csv file matches the reference version
    assertTrue("birth-death csv file must match reference", Utils.isEqualContents(a, b));
  }

  @Test
  public void checkCIMetadata() {
    Log.mandatory("Checking the confidence index metadata");
    String testDataDirectory = null;
    try {
      testDataDirectory = new File(".").getCanonicalPath() + File.separator + "test" + File
          .separator;
    } catch (IOException e) {}
    // check that the tracking test data directory exists
    assertNotNull("tracking test data directory must not be null", testDataDirectory);

    // Check the confidence-index file
    File a = new File(testDataDirectory + "junit_results" + File.separator +
        "trk-confidence-index.csv");
    File b = new File(testDataDirectory + "ref_track_results" + File.separator +
        testReferenceFolder + File.separator + "trk-confidence-index.csv");

    // check that the confidence-index csv file matches the reference version
    assertTrue("confidence-index csv file must match reference", Utils.isEqualContents(a, b));
  }

  @Test
  public void checkFusionMetadata() {
    Log.mandatory("Checking the fusion metadata");
    String testDataDirectory = null;
    try {
      testDataDirectory = new File(".").getCanonicalPath() + File.separator + "test" + File
          .separator;
    } catch (IOException e) {}
    // check that the tracking test data directory exists
    assertNotNull("tracking test data directory must not be null", testDataDirectory);

    // Check the division file
    File a = new File(testDataDirectory + "junit_results" + File.separator +
        "trk-fusion.csv");
    File b = new File(testDataDirectory + "ref_track_results" + File.separator +
        testReferenceFolder + File.separator + "trk-fusion.csv");

    // check that the division csv file matches the reference version
    assertTrue("fusion csv file must match reference", Utils.isEqualContents(a, b));
  }

  @Test
  public void checkTrackedImages() {
    Log.mandatory("Checking the tracked images");
    String testDataDirectory = null;
    try {
      testDataDirectory = new File(".").getCanonicalPath() + File.separator + "test" + File
          .separator;
    } catch (IOException e) {}
    // check that the tracking test data directory exists
    assertNotNull("tracking test data directory must not be null", testDataDirectory);

    // Check the output tracked images
    DecimalFormat df = new DecimalFormat("000");
    for(int i = 1; i <= 10; i++) {
      String filename = "trk-seg_" + df.format(i) + ".tif";
      ImagePlus imgA = new ImagePlus(testDataDirectory + "junit_results" + File.separator +
          filename);
      ImagePlus imgB = new ImagePlus(testDataDirectory + "ref_track_results" + File.separator +
          testReferenceFolder + File.separator + filename);

      short[] pixelsA = (short[]) imgA.getProcessor().convertToShort(false).getPixels();
      short[] pixelsB = (short[]) imgB.getProcessor().convertToShort(false).getPixels();

      // check that both tracked images are equal
      assertTrue("tracked image " + filename + " must match reference", Arrays.equals(pixelsA, pixelsB));
    }
  }
  
}
