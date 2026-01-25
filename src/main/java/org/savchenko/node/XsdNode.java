package org.savchenko.node;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class XsdNode {
    private String qName;
    private String minOccurs = "1";
    private String maxOccurs = "1";
    private String fileName; //html file name
    private String linkToChild; //for href
    private String targetNameSpace; //actual namespace
    private String name;
    private XsdNodeType type = XsdNodeType.ANY;
    private String documentation;
    private boolean recursive = false;
    private List<XsdNode> children = new ArrayList<>();
    @JsonIgnore
    private XsdNode parentNode;

    public XsdNode(XsdNode parentNode) {
        this.parentNode = parentNode;
    }

    public boolean isChainAlreadyHasQName(String qName) {
        if (qName.equals(this.qName)) {
            return true;
        }
        if (parentNode == null) {
            return false;
        }
        return parentNode.isChainAlreadyHasQName(qName);
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }
}
