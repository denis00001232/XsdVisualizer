package org.savchenko.htmlwriter;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;

public class BrowserLauncher {
    
    public void openInBrowser(File file) {
        if (!Desktop.isDesktopSupported()) {
            throw new UnsupportedOperationException("Desktop is not supported");
        }
        
        try {
            URI uri = file.toURI();
            Desktop.getDesktop().browse(uri);
        } catch (Exception e) {
            throw new RuntimeException("Failed to open file in browser: " + file, e);
        }
    }
    
    public void openInBrowser(String filePath) {
        openInBrowser(new File(filePath));
    }
}