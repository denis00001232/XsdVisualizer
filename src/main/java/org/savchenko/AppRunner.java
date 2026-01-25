package org.savchenko;

import org.savchenko.htmlwriter.HtmlWriter;
import org.savchenko.xsdparser.XsdParser;

public class AppRunner {
    public void run(String... args) {
        String path = args[0];
        String mode = args[1];
        XsdParser xsdParser = new XsdParser();
        HtmlWriter htmlWriter = new HtmlWriter();

        if (args[0] == null) {
            System.out.println("Please provide the path to the XSD file or directory.");
            return;
        }

        switch (mode) {
            case "-s" -> {
                htmlWriter.writeSchemaAndOpen(xsdParser.parseAsTree(path));
            }
            case "-t" -> {
                htmlWriter.writeNavigationTableAndOpen(xsdParser.parseAsNavigation(path));
            }
            default -> {
                System.out.println("Unknown mode. Use -s for single schema or -t for navigation table.");
            }
        }
    }
}
