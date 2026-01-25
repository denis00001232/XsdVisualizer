package org.savchenko.htmlwriter;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.savchenko.node.XsdNode;

import java.util.Objects;

public class HtmlElementWriter {
    
    public void writeNavigationPanel(Element parent) {
        parent.attr("class", "navigation-panel");
        parent.appendElement("div")
            .attr("class", "button-panel")
            .attr("onclick", "showAll()")
            .appendText("Show all element");
        parent.appendElement("div")
            .attr("class", "button-panel")
            .attr("onclick", "hideAll()")
            .appendText("Hide all elements");
    }
    
    public void writeRootContainer(Element parent, XsdNode node) {
        Element container = parent.appendElement("div")
            .attr("class", "container");
        Element containerInfo = container.appendElement("div")
            .attr("class", "container-info");

        if (node.getName() != null) {
            containerInfo.appendElement("div")
                .attr("class", "name")
                .appendText(node.getName());
        }

        if (node.getTargetNameSpace() != null) {
            containerInfo.appendElement("div")
                .attr("class", "namespace")
                .appendText(node.getTargetNameSpace());
        }

        if (node.getDocumentation() != null) {
            containerInfo.appendElement("div")
                .attr("class", "description")
                .html(node.getDocumentation().replace("\n", "&#10;"));
        }
    }
    
    public void writeContainer(Element parent, XsdNode node) {
        Element container = parent.appendElement("div")
            .attr("class", "container");
        container.appendElement("input")
            .attr("type", "checkbox")
            .attr("class", "cube-checkbox");
        Element containerInfo = container.appendElement("div")
            .attr("class", "container-info");
            
        if (node.hasChildren()) {
            Element containerButton = container.appendElement("div")
                .attr("class", "container-button");
            writeButton(containerButton);
        }
        
        switch (node.getType()) {
            case SEQUENCE -> writeModelGroup(node, containerInfo, container, "seq");
            case CHOICE -> writeModelGroup(node, containerInfo, container, "choice");
            case ALL -> writeModelGroup(node, containerInfo, container, "all");
            case ANY -> writeModelGroup(node, containerInfo, container, "any");
            case COMPLEX_TYPE -> writeComplexType(node, containerInfo, container);
            case BASE -> writeModelGroup(node, containerInfo, container, "base");
            case ELEMENT -> writeElement(node, containerInfo, container);
            case SIMPLE_TYPE -> writeSimpleType(node, containerInfo, container);
            case GROUP -> writeGroup(node, containerInfo, container);
            case ATTRIBUTE -> writeAttribute(node, containerInfo, container, "attribute");
            case ATTRIBUTE_GROUP -> writeAttribute(node, containerInfo, container, "attr group");
        }
    }
    
    public void writeContainerSequence(Element parent, XsdNode node) {
        Element containerSequence = parent.appendElement("div")
            .attr("class", "container-sequence");
        for (XsdNode child : node.getChildren()) {
            writeContainerBlock(containerSequence, child);
        }
    }
    
    private void writeContainerBlock(Element parent, XsdNode node) {
        Element containerBlock = parent.appendElement("div")
            .attr("class", "container-block");
        writeContainer(containerBlock, node);
        writeContainerSequence(containerBlock, node);
    }
    
    private void writeComplexType(XsdNode node, Element containerInfo, Element container) {
        if (node.getName() != null) {
            if (node.getLinkToChild() != null) {
                containerInfo.appendElement("a")
                    .attr("class", "name-link")
                    .attr("href", node.getLinkToChild())
                    .appendText(node.getName());
                container.addClass("ct-with-import");
            } else {
                writeNodeName(node, containerInfo);
                container.addClass("inter-type");
            }
            writeTargetNamespace(node, containerInfo);
        } else {
            container.addClass("inter-type");
        }
        writeNodeType("ct", containerInfo);
        writeRecursive(node, containerInfo);
        writeDocumentation(node, containerInfo);
    }
    
    private void writeSimpleType(XsdNode node, Element containerInfo, Element container) {
        writeNodeName(node, containerInfo);
        writeTargetNamespace(node, containerInfo);
        writeNodeType("st", containerInfo);
        writeDocumentation(node, containerInfo);
        container.addClass("simple-type");
    }
    
    private void writeElement(XsdNode node, Element containerInfo, Element container) {
        writeNodeName(node, containerInfo);
        writeNodeType("element", containerInfo);
        writeOccurs(node, containerInfo, container);
        writeDocumentation(node, containerInfo);
    }
    
    private void writeGroup(XsdNode node, Element containerInfo, Element container) {
        writeNodeName(node, containerInfo);
        writeTargetNamespace(node, containerInfo);
        writeNodeType("group", containerInfo);
        writeOccurs(node, containerInfo, container);
        writeDocumentation(node, containerInfo);
        container.addClass("group-type");
    }
    
    private void writeModelGroup(XsdNode node, Element containerInfo, Element container, String type) {
        writeNodeType(type, containerInfo);
        writeOccurs(node, containerInfo, container);
        container.addClass("inter-type");
    }
    
    private void writeAttribute(XsdNode node, Element containerInfo, Element container, String type) {
        writeNodeName(node, containerInfo);
        writeNodeType("attribute", containerInfo);
        Element occurs = containerInfo.appendElement("div")
            .attr("class", "occurs");
        if ("0".equals(node.getMinOccurs())) {
            occurs.text("optional");
        } else {
            occurs.text("required");
        }
        container.addClass("attribute");
    }
    
    private void writeOccurs(XsdNode node, Element containerInfo, Element container) {
        if (Objects.equals(node.getMinOccurs(), "1") && 
            Objects.equals(node.getMaxOccurs(), "1")) {
            return;
        }
        
        Element occurs = containerInfo.appendElement("div")
            .attr("class", "occurs");
            
        if (node.getMinOccurs() != null) {
            occurs.appendElement("div")
                .attr("class", "occurs")
                .appendText("min: " + node.getMinOccurs());
        }
        
        if (Objects.equals(node.getMaxOccurs(), "unbounded")) {
            occurs.appendElement("div")
                .attr("class", "occurs")
                .appendText("max: ∞");
            container.addClass("unbounded-max-occurs");
        } else if (node.getMaxOccurs() != null) {
            occurs.appendElement("div")
                .attr("class", "occurs")
                .appendText("max: " + node.getMaxOccurs());
        }
        
        if (Objects.equals(node.getMinOccurs(), "0")) {
            container.addClass("zero-min-occurs");
        }
    }
    
    private void writeNodeName(XsdNode node, Element containerInfo) {
        if (node.getName() != null) {
            containerInfo.appendElement("div")
                .attr("class", "name")
                .appendText(node.getName());
        }
    }
    
    private void writeTargetNamespace(XsdNode node, Element containerInfo) {
        if (node.getTargetNameSpace() != null) {
            containerInfo.appendElement("div")
                .attr("class", "namespace")
                .appendText(node.getTargetNameSpace());
        }
    }
    
    private void writeNodeType(String type, Element containerInfo) {
        containerInfo.appendElement("div")
            .attr("class", "element")
            .appendText(type);
    }
    
    private void writeRecursive(XsdNode node, Element containerInfo) {
        if (node.isRecursive()) {
            containerInfo.appendElement("div")
                .attr("class", "recursive")
                .appendText("recursive");
        }
    }
    
    private void writeDocumentation(XsdNode node, Element containerInfo) {
        if (node.getDocumentation() != null) {
            containerInfo.appendElement("div")
                .attr("class", "description")
                .appendChild(new TextNode(node.getDocumentation()));
        }
    }
    
    private void writeButton(Element container) {
        Element button = container.appendElement("div")
            .attr("class", "button")
            .attr("onclick", "changeVisibility(this)");
        button.appendElement("div")
            .attr("class", "button-text")
            .appendText("-");
    }
}