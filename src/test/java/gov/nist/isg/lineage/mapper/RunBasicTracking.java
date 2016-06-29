package test.java.gov.nist.isg.lineage.mapper;

import java.io.File;

import main.java.gov.nist.isg.lineage.mapper.LineageMapper;
import main.java.gov.nist.isg.lineage.mapper.app.TrackingAppParams;

public class RunBasicTracking {

  public static void runWithDivision(String testDataDirectory) {
    // clear out the old results
    Utils.deleteFolder(new File(testDataDirectory + File.separator + "test_track_results"));

    TrackingAppParams params = new TrackingAppParams();
    params.setInputDirectory(testDataDirectory);
    params.setFilenamePattern("seg_{iii}.tif");
    params.setOutputDirectory(testDataDirectory + File.separator + "test_track_results" + File.separator);
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
    params.setEnableCellDivision(true);
    params.setEnableCellFusion(false);
    params.setFusionOverlapThreshold(0.2);
    params.setCellSizeThreshold(100);

    params.setCellDensityAffectsCI(true);
    params.setBorderCellAffectsCI(true);

    params.setIsMacro(true); // disables auto-open of lineage viewer webpage

    LineageMapper lm = new LineageMapper(params);
    lm.run();
  }

  public static void runWithFusion(String testDataDirectory) {
    // clear out the old results
    Utils.deleteFolder(new File(testDataDirectory + File.separator + "test_track_results"));

    TrackingAppParams params = new TrackingAppParams();
    params.setInputDirectory(testDataDirectory);
    params.setFilenamePattern("seg_{iii}.tif");
    params.setOutputDirectory(testDataDirectory + File.separator + "test_track_results" + File.separator);
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

  public static void runWithDivisionFusion(String testDataDirectory) {
    // clear out the old results
    Utils.deleteFolder(new File(testDataDirectory + File.separator + "test_track_results"));

    TrackingAppParams params = new TrackingAppParams();
    params.setInputDirectory(testDataDirectory);
    params.setFilenamePattern("seg_{iii}.tif");
    params.setOutputDirectory(testDataDirectory + File.separator + "test_track_results" + File.separator);
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
    params.setEnableCellDivision(true);
    params.setEnableCellFusion(true);
    params.setFusionOverlapThreshold(0.2);
    params.setCellSizeThreshold(100);

    params.setCellDensityAffectsCI(true);
    params.setBorderCellAffectsCI(true);

    params.setIsMacro(true); // disables auto-open of lineage viewer webpage

    LineageMapper lm = new LineageMapper(params);
    lm.run();
  }
}
