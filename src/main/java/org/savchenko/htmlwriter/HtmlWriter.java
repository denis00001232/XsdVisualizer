package org.savchenko.htmlwriter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.savchenko.ExcelReader.ExcelReader;
import org.savchenko.dto.CellDto;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

public class HtmlWriter {
    private String style = readFile("style.css");
    private String styleTable = readFile("style_table.css");
    private String script = readFile("script.js");

    private String readFile(String name) {
        try {
            Path path = Paths.get(getClass().getClassLoader().getResource(name).toURI());
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Создает html представление от одного рута
     * @param cellDtoRoot
     */
    public void writeHtml(CellDto cellDtoRoot) {
        Document doc = Jsoup.parse("<html></html>");
        doc.outputSettings().charset("UTF-8"); // Важно!
        doc.head().appendElement("meta").attr("charset", "UTF-8");
        doc.title(cellDtoRoot.getFileName());
        doc.head().appendElement("style").text(style);
        doc.body().appendElement("svg")
                .attr("class", "connectors-container")
                .attr("id", "svgLayer");
        Element containerNavigation = doc.body().appendElement("div");
        writeNavigationPanel(containerNavigation);
        Element containerBlockRoot = doc.body().appendElement("div")
                .attr("class", "container-block-root");
        writeContainerRoot(containerBlockRoot, cellDtoRoot);
        writeContainerSequence(containerBlockRoot, cellDtoRoot);
        doc.body().appendElement("script").text(script);

        try {
            File file = new File("schema_doc/schemas/" + cellDtoRoot.getFileName());
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(doc.html().getBytes());
            fileOutputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeSingleHtml(CellDto cellDtoRoot) {
        Document doc = Jsoup.parse("<html></html>");
        doc.outputSettings().charset("UTF-8"); // Важно!
        doc.head().appendElement("meta").attr("charset", "UTF-8");
        doc.title(cellDtoRoot.getFileName());
        doc.head().appendElement("style").text(style);
        doc.body().appendElement("svg")
                .attr("class", "connectors-container")
                .attr("id", "svgLayer");
        Element containerNavigation = doc.body().appendElement("div");
        writeNavigationPanel(containerNavigation);
        Element containerBlockRoot = doc.body().appendElement("div")
                .attr("class", "container-block-root");
        writeContainerRoot(containerBlockRoot, cellDtoRoot);
        writeContainerSequence(containerBlockRoot, cellDtoRoot);
        doc.body().appendElement("script").text(script);

        try {
            File file = new File("schema_doc/schemas/" + cellDtoRoot.getFileName() + ".html");
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(doc.html().getBytes());
            fileOutputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeNavTable(List<CellDto> cellDtoList) {
        Document doc = Jsoup.parse("<html></html>");
        doc.outputSettings().charset("UTF-8"); // Важно!
        doc.head().appendElement("meta").attr("charset", "UTF-8");
        doc.title("Navigation page");
        doc.head().appendElement("style").text(styleTable);
        Element table = doc.body().appendElement("table").attr("class", "styled-table");
        Element row = table.appendElement("tr");
        row.appendElement("th")
                .appendText("Structure name");
        row.appendElement("th")
                .appendText("Description");
        for (CellDto cellDto : cellDtoList) {
            try {
                row = table.appendElement("tr");
                row.appendElement("td").appendElement("a")
                        .attr("href", "schemas/" + cellDto.getFileName())
                        .attr("target", "_blank")
                        .appendText(cellDto.getChildren().get(0).getName());
                row.appendElement("td")
                        .appendText(String.valueOf(cellDto.getChildren().get(0).getDocumentation()));
            } catch (Exception e) {

            }
        }


        try {
            File file = new File("schema_doc/NavigationPanel.html");
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            navigateToFile(file);
            fileOutputStream.write(doc.html().getBytes());
            fileOutputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void navigateToFile(File file) {
        try {
            URI uri = file.toURI(); // корректно сформирует file:// URI
            Desktop.getDesktop().browse(uri);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private void writeNavigationPanel(Element element) {
        element.attr("class", "navigation-panel");
        element.appendElement("div")
                .attr("class", "button-panel")
                .attr("onclick", "showAll()")
                .appendText("Show all element");
        element.appendElement("div")
                .attr("class", "button-panel")
                .attr("onclick", "hideAll()")
                .appendText("Hide all elements");
    }

    private void writeContainerRoot(Element rootElement, CellDto cellDto) {
        Element container = rootElement.appendElement("div")
                .attr("class", "container");
        Element containerInfo = container.appendElement("div")
                .attr("class", "container-info");

        if (cellDto.getName() != null) {
            containerInfo.appendElement("div")
                    .attr("class", "name")
                    .appendText(cellDto.getName());
        }

        if (cellDto.getTargetNameSpace() != null) {
            containerInfo.appendElement("div")
                    .attr("class", "namespace")
                    .appendText(cellDto.getTargetNameSpace());
        }

        if (cellDto.getDocumentation() != null) {
            containerInfo.appendElement("div")
                    .attr("class", "description")
                    .appendText(cellDto.getDocumentation());
        }
        if (cellDto.getLinkToChild() != null) {
            containerInfo.appendElement("div")
                    .attr("class", "name")
                    .appendText(cellDto.getLinkToChild());
        }
    }

    private void writeContainer(Element rootElement, CellDto cellDto) {
        Element container = rootElement.appendElement("div")
                .attr("class", "container");
        Element containerInfo = container.appendElement("div")
                .attr("class", "container-info");
        if (!cellDto.getChildren().isEmpty()) {
            Element containerButton = container.appendElement("div")
                    .attr("class", "container-button");
            writeButton(containerButton);
        }
        if (cellDto.getName() != null) {
            if (cellDto.getLinkToChild() != null) {
                containerInfo.appendElement("a")
                        .attr("class", "name-link")
                        .attr("href", cellDto.getLinkToChild())
                        .appendText(cellDto.getName());
            } else {
                containerInfo.appendElement("div")
                        .attr("class", "name")
                        .appendText(cellDto.getName());
            }

        }

        if (cellDto.getTargetNameSpace() != null) {
            containerInfo.appendElement("div")
                    .attr("class", "namespace")
                    .appendText(cellDto.getTargetNameSpace());
        }

        if (cellDto.getType() != null) {
            containerInfo.appendElement("div")
                    .attr("class", "element")
                    .appendText(cellDto.getType());
        }

        Element occurs = containerInfo.appendElement("div")
                .attr("class", "occurs");

        if (cellDto.getMinOccurs() != null) {
            occurs.appendElement("div")
                    .attr("class", "occurs")
                    .appendText("min: " + cellDto.getMinOccurs());
        }
        if (Objects.equals(cellDto.getMaxOccurs(), "unbounded")) {
            occurs.appendElement("div")
                    .attr("class", "occurs")
                    .appendText("max: " + "∞");
            container.addClass("unbounded-max-occurs");
        }
        else if (cellDto.getMaxOccurs() != null) {
            occurs.appendElement("div")
                    .attr("class", "occurs")
                    .appendText("max: " + cellDto.getMaxOccurs());
        }
        if (Objects.equals(cellDto.getMinOccurs(), "0")) {
            container.addClass("zero-min-occurs");
        }

        if (Objects.equals(cellDto.getMinOccurs(), "1") && Objects.equals(cellDto.getMaxOccurs(), "1")) {
            occurs.remove();
        }

        if (cellDto.getDocumentation() != null) {
            containerInfo.appendElement("div")
                    .attr("class", "description")
                    .text(cellDto.getDocumentation());
        }

        if (Objects.equals(cellDto.getType(), "st")) {
            container.addClass("simple-type");
        }

        if (Objects.equals(cellDto.getType(), "seq") ||
                Objects.equals(cellDto.getType(), "choice") ||
                Objects.equals(cellDto.getType(), "complexCon") ||
                Objects.equals(cellDto.getType(), "base") ||
                Objects.equals(cellDto.getType(), "ct") ||
                Objects.equals(cellDto.getType(), "any") ||
                Objects.equals(cellDto.getType(), "all")) {
            container.addClass("inter-type");
        }

        if (cellDto.getLinkToChild() != null) {
            container.addClass("ct-with-import");
        }

        if (Objects.equals(cellDto.getType(), "attribute") ||
                Objects.equals(cellDto.getType(), "attributeGroup")) {
            container.addClass("attribute");
        }
    }

    private void writeButton(Element container) {
        Element button = container.appendElement("div")
                .attr("class", "button")
                .attr("onclick", "changeVisibility(this)");
        button.appendElement("div").attr("class", "button-text").appendText("-");
    }

    private void writeContainerBlock(Element rootElement, CellDto cellDto) {
        Element containerBlock = rootElement.appendElement("div")
                .attr("class", "container-block");
        writeContainer(containerBlock, cellDto);
        writeContainerSequence(containerBlock, cellDto);
    }

    private void writeContainerSequence(Element rootElement, CellDto cellDto) {
        Element containerSequence = rootElement.appendElement("div")
                .attr("class", "container-sequence");
        for (CellDto cellDtoChild: cellDto.getChildren()) {
            writeContainerBlock(containerSequence, cellDtoChild);
        }
    }


}
