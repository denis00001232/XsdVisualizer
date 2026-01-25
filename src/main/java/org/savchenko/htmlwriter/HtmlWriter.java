package org.savchenko.htmlwriter;

import org.jsoup.nodes.Document;
import org.savchenko.htmlwriter.config.HtmlWriterConfig;
import org.savchenko.htmlwriter.config.ResourceLoader;
import org.savchenko.htmlwriter.filewriter.HtmlFileWriter;
import org.savchenko.htmlwriter.generator.HtmlDocumentGenerator;
import org.savchenko.htmlwriter.generator.NavigationTableGenerator;
import org.savchenko.node.XsdNode;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

//api to simple use
public class HtmlWriter {
    private final HtmlWriterConfig config;
    private final HtmlDocumentGenerator documentGenerator;
    private final NavigationTableGenerator tableGenerator;
    private final HtmlFileWriter fileWriter;
    private final BrowserLauncher browserLauncher;
    
    public HtmlWriter() {
        ResourceLoader resourceLoader = new ResourceLoader();
        this.config = HtmlWriterConfig.createDefault(resourceLoader);
        HtmlElementWriter elementWriter = new HtmlElementWriter();
        this.documentGenerator = new HtmlDocumentGenerator(config, elementWriter);
        this.tableGenerator = new NavigationTableGenerator(config);
        this.fileWriter = new HtmlFileWriter();
        this.browserLauncher = new BrowserLauncher();
    }
    
    public HtmlWriter(HtmlWriterConfig config) {
        this.config = config;
        HtmlElementWriter elementWriter = new HtmlElementWriter();
        this.documentGenerator = new HtmlDocumentGenerator(config, elementWriter);
        this.tableGenerator = new NavigationTableGenerator(config);
        this.fileWriter = new HtmlFileWriter();
        this.browserLauncher = new BrowserLauncher();
    }
    
    public File writeSchema(XsdNode rootNode) {
        Document doc = documentGenerator.generateSchemaDocument(rootNode);
        String filePath = config.getSchemasPath() + "/" + rootNode.getFileName();
        return fileWriter.writeToFile(doc, filePath);
    }

    public void writeSchemaAndOpen(XsdNode rootNode) {
        browserLauncher.openInBrowser(writeSchema(rootNode));
    }
    
    public void writeNavigationTable(List<XsdNode> nodes) {
        Document doc = tableGenerator.generateNavigationTable(nodes);
        String filePath = config.getNavigationPath();
        fileWriter.writeToFile(doc, filePath);
        for (XsdNode node : nodes) {
            writeSchema(node);
        }
    }
    
    public void writeNavigationTableAndOpen(List<XsdNode> nodes) {
        writeNavigationTable(nodes);
        browserLauncher.openInBrowser(config.getNavigationPath());
    }
}