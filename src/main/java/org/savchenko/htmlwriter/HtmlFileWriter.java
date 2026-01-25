package org.savchenko.htmlwriter;

import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HtmlFileWriter {
    
    public void writeToFile(Document document, String filePath) {
        File file = new File(filePath);
        ensureDirectoryExists(file.getParentFile());
        
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(document.html().getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to write HTML file: " + filePath, e);
        }
    }
    
    private void ensureDirectoryExists(File directory) {
        if (directory != null && !directory.exists()) {
            if (!directory.mkdirs()) {
                throw new RuntimeException("Failed to create directory: " + directory);
            }
        }
    }
    
    public Path getFilePath(String path) {
        return Paths.get(path);
    }
}