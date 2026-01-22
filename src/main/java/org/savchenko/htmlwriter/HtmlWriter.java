package org.savchenko.htmlwriter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.savchenko.dto.XsdNode;

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
    private final String style = readFile("style.css");
    private final String styleTable = readFile("style_table.css");
    private final String script = readFile("script.js");

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
     * @param xsdNodeRoot
     */
    public void writeHtml(XsdNode xsdNodeRoot) {
        Document doc = Jsoup.parse("<html></html>");
        doc.outputSettings().prettyPrint(false);
        doc.outputSettings().charset("UTF-8"); // Важно!
        doc.head().appendElement("meta").attr("charset", "UTF-8");
        doc.title(xsdNodeRoot.getFileName());
        doc.head().appendElement("style").text(style);
        doc.body().appendElement("svg")
                .attr("class", "connectors-container")
                .attr("id", "svgLayer");
        Element containerNavigation = doc.body().appendElement("div");
        writeNavigationPanel(containerNavigation);
        Element containerBlockRoot = doc.body().appendElement("div")
                .attr("class", "container-block-root");
        writeContainerRoot(containerBlockRoot, xsdNodeRoot);
        writeContainerSequence(containerBlockRoot, xsdNodeRoot);
        doc.body().appendElement("script").text(script);

        try {
            File file = new File("schema_doc/schemas/" + xsdNodeRoot.getFileName());
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

    public void writeSingleHtml(XsdNode xsdNodeRoot) {
        Document doc = Jsoup.parse("<html></html>");
        doc.outputSettings().prettyPrint(false);
        doc.outputSettings().charset("UTF-8"); // Важно!
        doc.head().appendElement("meta").attr("charset", "UTF-8");
        doc.title(xsdNodeRoot.getFileName());
        doc.head().appendElement("style").text(style);
        doc.body().appendElement("svg")
                .attr("class", "connectors-container")
                .attr("id", "svgLayer");
        Element containerNavigation = doc.body().appendElement("div");
        writeNavigationPanel(containerNavigation);
        Element containerBlockRoot = doc.body().appendElement("div")
                .attr("class", "container-block-root");
        writeContainerRoot(containerBlockRoot, xsdNodeRoot);
        writeContainerSequence(containerBlockRoot, xsdNodeRoot);
        doc.body().appendElement("script").text(script);

        try {
            File file = new File("schema_doc/schemas/" + xsdNodeRoot.getFileName() + ".html");
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

    public void writeNavTable(List<XsdNode> xsdNodeList) {
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
        for (XsdNode xsdNode : xsdNodeList) {
            try {
                row = table.appendElement("tr");
                row.appendElement("td").appendElement("a")
                        .attr("href", "schemas/" + xsdNode.getFileName())
                        .attr("target", "_blank")
                        .appendText(xsdNode.getChildren().get(0).getName());
                row.appendElement("td")
                        .appendText(String.valueOf(xsdNode.getChildren().get(0).getDocumentation()));
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

    private void writeContainerRoot(Element rootElement, XsdNode xsdNode) {
        Element container = rootElement.appendElement("div")
                .attr("class", "container");
        Element containerInfo = container.appendElement("div")
                .attr("class", "container-info");

        if (xsdNode.getName() != null) {
            containerInfo.appendElement("div")
                    .attr("class", "name")
                    .appendText(xsdNode.getName());
        }

        if (xsdNode.getTargetNameSpace() != null) {
            containerInfo.appendElement("div")
                    .attr("class", "namespace")
                    .appendText(xsdNode.getTargetNameSpace());
        }

        if (xsdNode.getDocumentation() != null) {
            containerInfo.appendElement("div")
                    .attr("class", "description")
                    .html(xsdNode.getDocumentation().replace("\n", "&#10;"));
        }
    }

    private void writeContainer(Element rootElement, XsdNode xsdNode) {
        Element container = rootElement.appendElement("div")
                .attr("class", "container");
        container.appendElement("input")
                .attr("type", "checkbox").attr("class", "cube-checkbox");
        Element containerInfo = container.appendElement("div")
                .attr("class", "container-info");
        if (!xsdNode.getChildren().isEmpty()) {
            Element containerButton = container.appendElement("div")
                    .attr("class", "container-button");
            writeButton(containerButton);
        }

        //------------------


        if (xsdNode.getTargetNameSpace() != null) {
            containerInfo.appendElement("div")
                    .attr("class", "namespace")
                    .appendText(xsdNode.getTargetNameSpace());
        }

        if (xsdNode.getType() != null) {
            containerInfo.appendElement("div")
                    .attr("class", "element");
                    //.appendText(cellDto.getType());
        }

        Element occurs = containerInfo.appendElement("div")
                .attr("class", "occurs");

        if (xsdNode.getMinOccurs() != null) {
            occurs.appendElement("div")
                    .attr("class", "occurs")
                    .appendText("min: " + xsdNode.getMinOccurs());
        }
        if (Objects.equals(xsdNode.getMaxOccurs(), "unbounded")) {
            occurs.appendElement("div")
                    .attr("class", "occurs")
                    .appendText("max: " + "∞");
            container.addClass("unbounded-max-occurs");
        }
        else if (xsdNode.getMaxOccurs() != null) {
            occurs.appendElement("div")
                    .attr("class", "occurs")
                    .appendText("max: " + xsdNode.getMaxOccurs());
        }
        if (Objects.equals(xsdNode.getMinOccurs(), "0")) {
            container.addClass("zero-min-occurs");
        }

        if (Objects.equals(xsdNode.getMinOccurs(), "1") && Objects.equals(xsdNode.getMaxOccurs(), "1")) {
            occurs.remove();
        }

        if (xsdNode.getDocumentation() != null) {
            containerInfo.appendElement("div")
                    .attr("class", "description")
                    .attr("style", "min-width: " + countLength(xsdNode.getDocumentation()) + "px;")
                    .appendChild(new TextNode(xsdNode.getDocumentation()));
        }

        if (Objects.equals(xsdNode.getType(), "st")) {
            container.addClass("simple-type");
        }

        if (Objects.equals(xsdNode.getType(), "seq") ||
                Objects.equals(xsdNode.getType(), "choice") ||
                Objects.equals(xsdNode.getType(), "complexCon") ||
                Objects.equals(xsdNode.getType(), "base") ||
                Objects.equals(xsdNode.getType(), "ct") ||
                Objects.equals(xsdNode.getType(), "any") ||
                Objects.equals(xsdNode.getType(), "all")) {
            container.addClass("inter-type");
        }

        if (xsdNode.getLinkToChild() != null) {
            container.addClass("ct-with-import");
        }

        if (Objects.equals(xsdNode.getType(), "attribute") ||
                Objects.equals(xsdNode.getType(), "attributeGroup")) {
            container.addClass("attribute");
            if (Objects.equals(xsdNode.getMinOccurs(), "0")) {
                occurs.text("optional");
            } else {
                occurs.text("required");
            }
        }


        //------------------
    }

    private void writeComplexType(XsdNode xsdNode, Element containerInfo, Element container) {
        if (xsdNode.getName() != null) {
            if (xsdNode.getLinkToChild() != null) {
                containerInfo.appendElement("a")
                        .attr("class", "name-link")
                        .attr("href", xsdNode.getLinkToChild())
                        .appendText(xsdNode.getName());
            } else {
                containerInfo.appendElement("div")
                        .attr("class", "name")
                        .appendText(xsdNode.getName());
            }
        }
        if (xsdNode.getTargetNameSpace() != null) {
            containerInfo.appendElement("div")
                    .attr("class", "namespace")
                    .appendText(xsdNode.getTargetNameSpace());
        }
        containerInfo.appendElement("div")
                .attr("class", "element")
                .appendText("ct");


    }

    private void writeButton(Element container) {
        Element button = container.appendElement("div")
                .attr("class", "button")
                .attr("onclick", "changeVisibility(this)");
        button.appendElement("div").attr("class", "button-text").appendText("-");
    }

    private void writeContainerBlock(Element rootElement, XsdNode xsdNode) {
        Element containerBlock = rootElement.appendElement("div")
                .attr("class", "container-block");
        writeContainer(containerBlock, xsdNode);
        writeContainerSequence(containerBlock, xsdNode);
    }

    private void writeContainerSequence(Element rootElement, XsdNode xsdNode) {
        Element containerSequence = rootElement.appendElement("div")
                .attr("class", "container-sequence");
        for (XsdNode xsdNodeChild : xsdNode.getChildren()) {
            writeContainerBlock(containerSequence, xsdNodeChild);
        }
    }

    private static final double CHAR_WIDTH = 6.0;  // пикселей на символ
    private static final double MAX_WIDTH = 300.0;

    private double countLength(String description) {
        if (description == null || description.isEmpty()) {
            return 0;
        }

        String[] lines = description.split("\n");
        int maxLength = 0;

        for (String line : lines) {
            String trimmed = line.trim();  // ✅ Вызываем один раз
            if (trimmed.length() > maxLength) {
                maxLength = trimmed.length();
            }
        }

        double width = maxLength * CHAR_WIDTH;
        return Math.min(width, MAX_WIDTH);  // ✅ Более читаемо
    }




}
