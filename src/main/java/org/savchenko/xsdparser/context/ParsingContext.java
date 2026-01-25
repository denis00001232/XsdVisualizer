package org.savchenko.xsdparser.context;

import org.eclipse.xsd.XSDComplexTypeDefinition;
import org.savchenko.node.XsdNode;
import org.savchenko.xsdparser.utils.QualifiedName;


import java.util.*;

public class ParsingContext {
    private final ParsingMode mode;
    private final List<XsdNode> allNodes;
    private final Set<QualifiedName> processedTypes;
    private final Queue<XSDComplexTypeDefinition> pendingTypes;
    
    private ParsingContext(
        ParsingMode mode,
        List<XsdNode> allNodes,
        Set<QualifiedName> processedTypes,
        Queue<XSDComplexTypeDefinition> pendingTypes
    ) {
        this.mode = mode;
        this.allNodes = allNodes;
        this.processedTypes = processedTypes;
        this.pendingTypes = pendingTypes;
    }
    
    public static ParsingContext forTreeMode() {
        return new ParsingContext(ParsingMode.TREE, null, null, null);
    }
    
    public static ParsingContext forNavigationMode() {
        return new ParsingContext(
            ParsingMode.NAVIGATION,
            new ArrayList<>(),
            new HashSet<>(),
            new LinkedList<>()
        );
    }
    
    public boolean isTreeMode() {
        return mode == ParsingMode.TREE;
    }
    
    public boolean isNavigationMode() {
        return mode == ParsingMode.NAVIGATION;
    }
    
    public void addNode(XsdNode node) {
        if (allNodes != null) {
            allNodes.add(node);
        }
    }
    
    public List<XsdNode> getAllNodes() {
        return allNodes != null ? allNodes : Collections.emptyList();
    }
    
    public boolean markAsProcessed(QualifiedName qname) {
        return processedTypes != null && processedTypes.add(qname);
    }
    
    public void addPendingType(XSDComplexTypeDefinition type) {
        if (pendingTypes != null) {
            pendingTypes.add(type);
        }
    }
    
    public XSDComplexTypeDefinition pollPendingType() {
        return pendingTypes != null ? pendingTypes.poll() : null;
    }
    
    public boolean hasPendingTypes() {
        return pendingTypes != null && !pendingTypes.isEmpty();
    }
    
    public enum ParsingMode {
        TREE,
        NAVIGATION
    }
}