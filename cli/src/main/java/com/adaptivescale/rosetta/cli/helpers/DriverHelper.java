package com.adaptivescale.rosetta.cli.helpers;

import com.adaptivescale.rosetta.common.models.DriverInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.IntStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class DriverHelper {
    /**
     * Prints drivers that are downloadable
     *
     * @param path where to search
     */
    public static void printDrivers(Path path) {
        List<DriverInfo> drivers = getDrivers(path);

        if (drivers.isEmpty()) {
            System.out.println("No drivers found.");
            return;
        }

        System.out.println("Downloadable Drivers:");
        System.out.println("=====================");

        IntStream.range(0, drivers.size()).forEach(index -> {
            DriverInfo driverInfo = drivers.get(index);
            System.out.printf("%2d: %s%n", index + 1, driverInfo.getName());
        });

        System.out.println("=====================");
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
     * @throws RuntimeException if a download error occurs
     */
    private static void downloadDriver(DriverInfo driverInfo) {
        try {
            // Attempt to get the ROSETTA_DRIVERS environment variable
            String rosettaDriversPath = System.getenv("ROSETTA_DRIVERS");
            if (rosettaDriversPath == null) {
                // Fall back to 'drivers' folder one level up if ROSETTA_DRIVERS is not set
                rosettaDriversPath = Paths.get("..", "drivers").toString();
            }

            // Construct the destination directory path
            Path rosettaPath = Paths.get(rosettaDriversPath);

            // Ensure the directory exists and is writable, or fall back
            if (!Files.exists(rosettaPath) || !Files.isWritable(rosettaPath)) {
                throw new IllegalArgumentException("No writable directory available for drivers");
            }

            // Open a connection to the URL of the driver
            URL url = new URL(driverInfo.getLink());
            // Get the file name from the URL
            String fileName = driverInfo.getLink().substring(driverInfo.getLink().lastIndexOf('/') + 1);
            // Construct the destination path
            Path destination = rosettaPath.resolve(fileName);

            // Check if the file already exists
            if (Files.exists(destination)) {
                System.out.println("Driver already exists: " + destination.toAbsolutePath());
                return;
            }

            // Check if we have write permission to the directory
            if (!Files.isWritable(rosettaPath)) {
                throw new IOException("No write permission to the directory: " + rosettaPath);
            }

            // Download the driver file and save it to the specified directory
            Files.copy(url.openStream(), destination);
            System.out.println("Driver downloaded successfully: " + destination.toAbsolutePath());

            // If the downloaded file is a zip file, unzip it
            if (fileName.endsWith(".zip")) {
                unzipFile(destination, rosettaPath);
                // Delete the zip file after extraction
                Files.delete(destination);
                System.out.println("Zip file extracted and deleted: " + destination.toAbsolutePath());
            }

        } catch (Exception e) {
            System.out.println("Error downloading the driver: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Unzips a zip file to the specified directory
     *
     * @param zipFilePath the path to the zip file
     * @param destDir     the destination directory
     * @throws IOException if an I/O error occurs
     */
    private static void unzipFile(Path zipFilePath, Path destDir) throws IOException {
        try (ZipFile zipFile = new ZipFile(zipFilePath.toFile())) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                Path entryDestination = destDir.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(entryDestination);
                } else {
                    Files.createDirectories(entryDestination.getParent());
                    Files.copy(zipFile.getInputStream(entry), entryDestination, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }
}
