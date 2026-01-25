package org.savchenko.htmlwriter;

import lombok.Getter;

@Getter
public class HtmlWriterConfig {
    private final String outputDirectory;
    private final String schemasSubDirectory;
    private final String styleCss;
    private final String styleTableCss;
    private final String scriptJs;
    
    public HtmlWriterConfig(
        String outputDirectory,
        String schemasSubDirectory,
        String styleCss,
        String styleTableCss,
        String scriptJs
    ) {
        this.outputDirectory = outputDirectory;
        this.schemasSubDirectory = schemasSubDirectory;
        this.styleCss = styleCss;
        this.styleTableCss = styleTableCss;
        this.scriptJs = scriptJs;
    }
    
    public static HtmlWriterConfig createDefault(ResourceLoader resourceLoader) {
        return new HtmlWriterConfig(
            "schema_doc",
            "schemas",
            resourceLoader.loadCss("style.css"),
            resourceLoader.loadCss("style_table.css"),
            resourceLoader.loadScript("script.js")
        );
    }

    public String getSchemasPath() {
        return outputDirectory + "/" + schemasSubDirectory;
    }
    
    public String getNavigationPath() {
        return outputDirectory + "/NavigationPanel.html";
    }
}