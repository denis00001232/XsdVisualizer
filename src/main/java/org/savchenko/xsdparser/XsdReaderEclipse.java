package org.savchenko.xsdparser;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.xsd.*;
import org.savchenko.dto.XsdNode;
import org.savchenko.dto.XsdNodeType;

import java.util.*;

public class XsdReaderEclipse {
    private XSDSchema xsdSchema;
    private XsdNode rootXsdNode;
    private List<XsdNode> xsdNodeList = new ArrayList<>();
    private Set<String> alreadyReadenNames = new HashSet<>();
    private Queue<XSDComplexTypeDefinition> otherComplexTypes = new LinkedList<>();
    private boolean isNav = false;

    public XsdNode readSchemaElement(String xsdFile) {
        XSDElementDeclaration rootElement = loadSchema(xsdFile);

        rootXsdNode = new XsdNode(null);
        rootXsdNode.setType(XsdNodeType.ELEMENT);
        rootXsdNode.setName("root");
        rootXsdNode.setFileName(rootElement.getName());

        parseXSDElementDeclaration(rootElement, rootXsdNode);

        return rootXsdNode;
    }


    public List<XsdNode> readSchemaElementAsNav(String xsdFile) throws Exception {
        isNav = true;
        XSDElementDeclaration rootElement = loadSchema(xsdFile);

        XSDTypeDefinition rootType = rootElement.getTypeDefinition();
        if (rootType instanceof XSDComplexTypeDefinition xsdComplexTypeDefinition) {
            otherComplexTypes.add(xsdComplexTypeDefinition);
        } else {
            rootXsdNode.setName(rootElement.getName());
        }

        while (!otherComplexTypes.isEmpty()) {
            XSDComplexTypeDefinition xsdComplexTypeDefinition = otherComplexTypes.poll();
            createNewCellDtoTree(xsdComplexTypeDefinition);
        }

        return xsdNodeList;
    }

    private XSDElementDeclaration loadSchema(String xsdFile) {
        ResourceSet resourceSet = new ResourceSetImpl();
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xsd", new org.eclipse.xsd.util.XSDResourceFactoryImpl());
        Resource resource = resourceSet.getResource(URI.createFileURI(xsdFile), true);
        xsdSchema = (XSDSchema) resource.getContents().get(0);
        List<XSDSchemaContent> rootElements = xsdSchema.getContents().stream().filter(el -> (el instanceof XSDElementDeclaration)).toList();
        if (rootElements.isEmpty()) {
            throw new RuntimeException("No root elements found in schema");
        }

        return (XSDElementDeclaration) rootElements.get(0);
    }

    private void parseXSDElementDeclaration(XSDElementDeclaration element, XsdNode xsdNode) {
        xsdNode.setName(element.getName());
        xsdNode.setType(XsdNodeType.ELEMENT);
        XSDAnnotation xsdAnnotation = element.getAnnotation();
        if (xsdAnnotation != null) {
            xsdNode.setDocumentation(getDocumentation(element.getAnnotation()));
        }
        if (element.getAnnotation() != null && !element.getAnnotation().getUserInformation().get(0).getTextContent().isEmpty()) {
            xsdNode.setDocumentation(getDocumentation(element.getAnnotation()));
        }
        XSDTypeDefinition xsdTypeDefinition = element.getTypeDefinition();
        if (xsdTypeDefinition instanceof XSDComplexTypeDefinition xsdComplexTypeDefinition) {
            parseXSDComplexTypeDefinition(xsdComplexTypeDefinition, xsdNode);
        } else if (xsdTypeDefinition instanceof XSDSimpleTypeDefinition xsdSimpleTypeDefinition) {
            parseXSDSimpleTypeDefinition(xsdSimpleTypeDefinition, xsdNode);
        }
    }


    private void createNewCellDtoTree(XSDComplexTypeDefinition xsdComplexTypeDefinition) {
        XsdNode cellRoot = new XsdNode(null);
        cellRoot.setType(XsdNodeType.SCHEMA);
        cellRoot.setName("root");
        xsdNodeList.add(cellRoot);
        parseXSDComplexTypeDefinition(xsdComplexTypeDefinition, cellRoot);
    }


    private void parseXSDSimpleTypeDefinition(XSDSimpleTypeDefinition xsdSimpleTypeDefinition, XsdNode xsdNodePrev) {
        XsdNode xsdNode = new XsdNode(xsdNodePrev);
        xsdNodePrev.getChildren().add(xsdNode);
        xsdNode.setType(XsdNodeType.SIMPLE_TYPE);
        xsdNode.setDocumentation(getDocumentation(xsdSimpleTypeDefinition.getAnnotation()));
        xsdNode.setName(xsdSimpleTypeDefinition.getName());
        xsdNode.setTargetNameSpace(xsdSimpleTypeDefinition.getTargetNamespace());
    }

    private String createSafeQName(XSDComplexTypeDefinition xsdComplexTypeDefinition) {
        String name = removeForbiddenSymbols(xsdComplexTypeDefinition.getTargetNamespace()) +
                xsdComplexTypeDefinition.getName() + ".html";
        return name.replaceAll("#", "sharp");
    }

    private String removeForbiddenSymbols(String nameSpace) {
        if (nameSpace == null || nameSpace.isBlank()) {
            return "";
        }
        String result = nameSpace
                .replaceFirst("^https?://", "")
                .replace(".xsd", "")
                // запрещённые символы Windows
                .replaceAll("[\\\\/:*?\"<>|]", "_")
                // управляющие символы
                .replaceAll("[\\x00-\\x1F]", "_")
                // схлопываем несколько подчёркиваний
                .replaceAll("_+", "_")
                // убираем подчёркивание в конце
                .replaceAll("[._ ]+$", "");
        return result + "_";
    }

    private void parseXSDComplexTypeDefinition(XSDComplexTypeDefinition xsdComplexTypeDefinition, XsdNode xsdNodePrev) {
        XsdNode xsdNode = new XsdNode(xsdNodePrev);
        xsdNodePrev.getChildren().add(xsdNode);
        xsdNode.setType(XsdNodeType.COMPLEX_TYPE);
        xsdNode.setName(xsdComplexTypeDefinition.getName());
        xsdNode.setDocumentation(getDocumentation(xsdComplexTypeDefinition.getAnnotation()));

        if (xsdComplexTypeDefinition.getName() != null) {
            xsdNode.setName(xsdComplexTypeDefinition.getName());
            xsdNode.setTargetNameSpace(xsdComplexTypeDefinition.getTargetNamespace());
            if (isNav) {
                if (xsdNodePrev.getType() != XsdNodeType.SCHEMA) {
                    String qName = createSafeQName(xsdComplexTypeDefinition);
                    xsdNode.setLinkToChild(qName);
                    if (alreadyReadenNames.add(qName)) {
                        otherComplexTypes.add(xsdComplexTypeDefinition);
                    }
                    return;
                } else {
                    xsdNodePrev.setFileName("schema");
                }
            }
        }
        String safeQName = createSafeQName(xsdComplexTypeDefinition);
        xsdNode.setQName(safeQName);

        if (xsdNode.isChainAlreadyHasQName(safeQName)) {
            System.out.println("Stopped recursion");
            xsdNode.setRecursive(true);
            return;
        }


        List<XSDAttributeGroupContent> xsdAttributeGroupContents = xsdComplexTypeDefinition.getAttributeContents();
        for (XSDAttributeGroupContent xsdAttributeGroupContent : xsdAttributeGroupContents) {
            if (xsdAttributeGroupContent instanceof XSDAttributeUse xsdAttributeUse) {
                parseXSDAttributeUse(xsdAttributeUse, xsdNode);
            } else if (xsdAttributeGroupContent instanceof XSDAttributeGroupDefinition xsdAttributeGroupDefinition) {
                parseXSDAttributeGroupDefinition(xsdAttributeGroupDefinition, xsdNode);
            }
        }
        XSDTypeDefinition xsdTypeDefinition = xsdComplexTypeDefinition.getBaseTypeDefinition();
        if (!xsdTypeDefinition.getName().equals("anyType")) {
            parseBase(xsdTypeDefinition, xsdNode);
        }
        XSDComplexTypeContent xsdComplexTypeContent = xsdComplexTypeDefinition.getContent();
        if (xsdComplexTypeContent instanceof XSDParticle xsdParticle) {
            parseParticle(xsdParticle, xsdNode);
        }
    }

    private void parseXSDAttributeUse(XSDAttributeUse xsdAttributeUse, XsdNode xsdNodePrev) {
        XsdNode xsdNode = new XsdNode(xsdNodePrev);

        xsdNodePrev.getChildren().add(xsdNode);
        xsdNode.setType(XsdNodeType.ATTRIBUTE);
        xsdNode.setName(xsdAttributeUse.getAttributeDeclaration().getName());
        xsdNode.setDocumentation(getDocumentation(xsdAttributeUse.getAttributeDeclaration().getAnnotation()));
        xsdNode.setMinOccurs("0");
        if (xsdAttributeUse.getUse().getName().equals("required")) {
            xsdNode.setMinOccurs("1");
        }
        parseXSDSimpleTypeDefinition(xsdAttributeUse.getAttributeDeclaration().getTypeDefinition(), xsdNode);
    }

    private void parseXSDAttributeGroupDefinition(XSDAttributeGroupDefinition xsdAttributeGroupDefinition, XsdNode xsdNodePrev) {
        XSDAttributeGroupDefinition xsdAttributeGroupDefinitionResolved = xsdAttributeGroupDefinition.getResolvedAttributeGroupDefinition();
        XsdNode xsdNode = new XsdNode(xsdNodePrev);
        xsdNodePrev.getChildren().add(xsdNode);
        xsdNode.setType(XsdNodeType.ATTRIBUTE_GROUP);
        xsdNode.setName(xsdAttributeGroupDefinitionResolved.getName());
        xsdNode.setDocumentation(getDocumentation(xsdAttributeGroupDefinitionResolved.getAnnotation()));
        xsdAttributeGroupDefinitionResolved.getAttributeUses().forEach(a -> parseXSDAttributeUse(a, xsdNode));

    }

    private void parseParticle(XSDParticle xsdParticle, XsdNode xsdNodePrev) {
        XsdNode xsdNode = new XsdNode(xsdNodePrev);
        xsdNodePrev.getChildren().add(xsdNode);
        xsdNode.setMinOccurs(String.valueOf(xsdParticle.getMinOccurs()));
        xsdNode.setMaxOccurs(xsdParticle.getMaxOccurs() == -1 ? "unbounded" : String.valueOf(xsdParticle.getMaxOccurs()));

        XSDTerm xsdTerm = xsdParticle.getTerm();

        if (xsdTerm instanceof XSDElementDeclaration xsdElementDeclaration) {
            parseXSDElementDeclaration(xsdElementDeclaration, xsdNode);
        }
        else if (xsdTerm instanceof XSDModelGroup xsdModelGroup) {
            // Проверяем, является ли это ссылкой на группу
            if (xsdModelGroup.eContainer() instanceof XSDModelGroupDefinition groupDef) {
                parseXSDModelGroupDefinition(groupDef, xsdNode);
            } else {
                // Обычная xs:sequence, xs:choice, xs:all
                parseXSDModelGroup(xsdModelGroup, xsdNode);
            }
        }
        else if (xsdTerm instanceof XSDWildcard) {
            parseXSDWildcard(xsdNode);
        }
    }

    private void parseXSDWildcard(XsdNode xsdNodePrev) {
        XsdNode xsdNode = new XsdNode(xsdNodePrev);
        xsdNodePrev.getChildren().add(xsdNode);
        xsdNode.setType(XsdNodeType.ANY);
    }

    private void parseXSDModelGroupDefinition(XSDModelGroupDefinition xsdModelGroupDefinition, XsdNode xsdNode) {
        xsdNode.setType(XsdNodeType.GROUP);
        xsdNode.setName(xsdModelGroupDefinition.getName());
        xsdNode.setTargetNameSpace(xsdModelGroupDefinition.getTargetNamespace());
        XsdNode xsdNodeChild = new XsdNode(xsdNode);
        xsdNode.getChildren().add(xsdNodeChild);
        XSDModelGroupDefinition xsdModelGroupDefinitionResolved = xsdModelGroupDefinition.getResolvedModelGroupDefinition();
        xsdNode.setDocumentation(getDocumentation(xsdModelGroupDefinitionResolved.getAnnotation()));
        parseXSDModelGroup(xsdModelGroupDefinitionResolved.getModelGroup(), xsdNodeChild);
    }

    private void parseXSDModelGroup(XSDModelGroup xsdModelGroup, XsdNode xsdNode) {
        switch (xsdModelGroup.getCompositor().getValue()) {
            case XSDCompositor.SEQUENCE -> xsdNode.setType(XsdNodeType.SEQUENCE);
            case XSDCompositor.CHOICE -> xsdNode.setType(XsdNodeType.CHOICE);
            case XSDCompositor.ALL -> xsdNode.setType(XsdNodeType.ALL);
        }
        xsdModelGroup.getParticles().forEach(p -> parseParticle(p, xsdNode));
    }

    private void parseBase(XSDTypeDefinition xsdTypeDefinition, XsdNode xsdNodePrev) {
        XsdNode xsdNode = new XsdNode(xsdNodePrev);
        xsdNodePrev.getChildren().add(xsdNode);
        xsdNode.setType(XsdNodeType.BASE);
        if (xsdTypeDefinition instanceof XSDSimpleTypeDefinition xsdSimpleTypeDefinition) {
            parseXSDSimpleTypeDefinition(xsdSimpleTypeDefinition, xsdNode);
        } else if (xsdTypeDefinition instanceof XSDComplexTypeDefinition xsdComplexTypeDefinition) {
            parseXSDComplexTypeDefinition(xsdComplexTypeDefinition, xsdNode);
        } else {
            throw new RuntimeException("Parsing error");
        }
    }

    private String getDocumentation(XSDAnnotation xsdAnnotation) {
        if (xsdAnnotation == null) return null;
        if (xsdAnnotation.getUserInformation().isEmpty()) return null;

        String text = xsdAnnotation.getUserInformation().get(0).getTextContent();
        if (text == null) return null;

        // Удаляем ВСЕ переносы в начале и конце
        text = text.trim();
        return text.isEmpty() ? null : text;
    }
}