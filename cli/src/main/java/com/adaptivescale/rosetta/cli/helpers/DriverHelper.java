package com.adaptivescale.rosetta.cli.helpers;

import com.adaptivescale.rosetta.common.models.DriverInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.DirectoryStream;
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
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(new YAMLFactory());
    private static final TypeReference<List<DriverInfo>> DRIVER_INFO_TYPE_REF = new TypeReference<List<DriverInfo>>() {};

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

        System.out.println("Downloadable drivers:");
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
            if (!Files.exists(path)) {
                URL resource = DriverHelper.class.getClassLoader().getResource(path.toString());
                if (resource == null) {
                    throw new RuntimeException("Drivers resource not found: " + path);
                }
                return OBJECT_MAPPER.readValue(resource, DRIVER_INFO_TYPE_REF);
            } else {
                return OBJECT_MAPPER.readValue(path.toFile(), DRIVER_INFO_TYPE_REF);
            }
        } catch (Exception exception) {
            throw new RuntimeException("Failed to read drivers from path: " + path, exception);
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
            Path rosettaPath = resolveDriverDirectory();

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

    /**
     * Prints all the downloaded .jar drivers in the specified directory, including those in subdirectories.
     */
    public static void printDownloadedDrivers() {
        try {
            Path rosettaPath = resolveDriverDirectory();

            // Print the list of downloaded .jar drivers
            System.out.printf("Downloaded .jar drivers (%s):%n", rosettaPath.toRealPath());
            System.out.println("=====================");
            boolean hasDrivers = listJarFiles(rosettaPath);

            if (!hasDrivers) {
                System.out.println("No downloaded .jar drivers found.");
            }
            System.out.println("=====================");

        } catch (IOException e) {
            System.out.println("Error reading the drivers directory: " + e.getMessage());
        }
    }

    /**
     * Recursively lists all .jar files in the specified directory and its subdirectories.
     *
     * @param directory The directory to search for .jar files.
     * @return True if any .jar files are found, otherwise false.
     * @throws IOException If an I/O error occurs.
     */
    private static boolean listJarFiles(Path directory) throws IOException {
        boolean hasDrivers = false;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    // Recursively search in subdirectories
                    hasDrivers |= listJarFiles(entry);
                } else if (entry.getFileName().toString().endsWith(".jar")) {
                    // Print the full path of the .jar file
                    System.out.println("File: " + entry.toRealPath());
                    hasDrivers = true;
                }
            }
        }

        return hasDrivers;
    }

    /**
     * Resolves the directory where drivers are stored.
     *
     * @return Path to the drivers directory
     * @throws RuntimeException if no valid directory is found
     */
    private static Path resolveDriverDirectory() {
        // Attempt to get the ROSETTA_DRIVERS environment variable
        String rosettaDriversPath = System.getenv("ROSETTA_DRIVERS");
        Path rosettaPath = null;

        if (rosettaDriversPath != null) {
            // Remove any trailing '/*' or '/' from the path
            rosettaDriversPath = rosettaDriversPath.replaceAll("/\\*$", "").replaceAll("/$", "");
            // If ROSETTA_DRIVERS is set, use that path
            rosettaPath = Paths.get(rosettaDriversPath);
        } else {
            try {
                // Get the path to the executing JAR file
                Path jarPath = Paths.get(DriverHelper.class.getProtectionDomain().getCodeSource().getLocation().toURI());
                Path jarDirectory = jarPath.getParent();  // Directory where the JAR file is located

                // First check the directory where the JAR file is located
                rosettaPath = jarDirectory.getParent().resolve("drivers");
                if (!Files.exists(rosettaPath)) {
                    // Fail if neither path exists
                    throw new RuntimeException("No drivers directory found in any expected location, please set ROSETTA_DRIVERS to a directory.");
                }
            } catch (URISyntaxException e) {
                throw new RuntimeException("Failed to locate the directory of the executing JAR file.", e);
            }
        }

        // Check if the final resolved path exists and is writable
        if (!Files.exists(rosettaPath) || !Files.isWritable(rosettaPath)) {
            throw new RuntimeException(String.format("ROSETTA_DRIVERS (%s) directory path not found, re-check your configuration!", rosettaPath.toAbsolutePath()));
        }

        return rosettaPath;
    }
}
