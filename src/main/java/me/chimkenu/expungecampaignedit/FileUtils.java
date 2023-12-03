package me.chimkenu.expungecampaignedit;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.stream.Stream;

public class FileUtils {
    public static void copyDirectory(String sourceDirectory, String destinationDirectory) throws IOException {
        try (Stream<Path> walk = Files.walk(Paths.get(sourceDirectory))) {
            walk.forEach(source -> {
                Path destination = Paths.get(destinationDirectory, source.toString()
                        .substring(sourceDirectory.length()));

                if (destination.toString().contains("session.lock")) {
                    return;
                }

                try {
                    Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
                } catch (DirectoryNotEmptyException ignored) {
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public static void deleteDirectory(Path root) throws IOException {
        try (Stream<Path> walk = Files.walk(root)) {
            walk.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }
}
