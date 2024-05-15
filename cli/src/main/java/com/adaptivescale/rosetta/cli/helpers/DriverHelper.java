package com.adaptivescale.rosetta.cli.helpers;

import com.adaptivescale.rosetta.common.models.DriverInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.IntStream;

public class DriverHelper {
    /**
     * Prints drivers that are downloadable
     *
     * @param path where to search
     */
    public static void printDrivers(Path path) {
        List<DriverInfo> drivers = getDrivers(path);
        IntStream.range(0, drivers.size())
          .forEach(index -> {
              DriverInfo driverInfo = drivers.get(index);
              System.out.println(String.format("%d - Driver name: %s", index + 1, driverInfo.getName()));
          });
    }

    /**
     * Finds drivers that are downloadable
     *
     * @param path where to search
     * @return List<DriverInfo>
     */
    public static List<DriverInfo> getDrivers(Path path) {
        try {
            List<DriverInfo> drivers = new ObjectMapper(new YAMLFactory()).readValue(path.toFile(), new TypeReference<List<DriverInfo>>() {
            });
            return drivers;
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Downloads driver from the selected path by driver index
     *
     * @param path     where to search
     * @param driverId which driver to download
     * @return DriverInfo
     */
    public static DriverInfo getDriver(Path path, Integer driverId) {
        try {
            List<DriverInfo> drivers = DriverHelper.getDrivers(path);
            DriverInfo driverInfo = drivers.get(driverId - 1);
            downloadDriver(driverInfo);
            return driverInfo;
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Downloads driver to the specified directory
     *
     * @param driverInfo which driver to download
     */
    private static void downloadDriver(DriverInfo driverInfo) {
        try {
            String rosettaDriversPath = System.getenv("ROSETTA_DRIVERS");
            if (rosettaDriversPath == null) {
                throw new IllegalArgumentException("ROSETTA_DRIVERS environment variable not set");
            }

            // Open a connection to the URL of the driver
            URL url = new URL(driverInfo.getLink());
            // Get the file name from the URL
            String fileName = driverInfo.getLink().substring(driverInfo.getLink().lastIndexOf('/') + 1);
            // Construct the destination path
            Path destination = Paths.get(rosettaDriversPath, fileName);
            // Download the driver file and save it to the specified directory
            Files.copy(url.openStream(), destination);
            System.out.println("Driver downloaded successfully: " + destination.toAbsolutePath());
        } catch (Exception e) {
            System.out.println("Error downloading the driver: " + e.getMessage());
        }
    }
}
