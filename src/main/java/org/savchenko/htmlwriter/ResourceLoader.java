package org.savchenko.htmlwriter;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ResourceLoader {
    
    public String loadResource(String resourceName) {
        try {
            Path path = Paths.get(getClass().getClassLoader()
                .getResource(resourceName).toURI());
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load resource: " + resourceName, e);
        }
    }
    
    public String loadCss(String fileName) {
        return loadResource(fileName);
    }
    
    public String loadScript(String fileName) {
        return loadResource(fileName);
    }
}