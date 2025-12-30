package org.savchenko.xsdparser;

import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.xsd.*;
import org.savchenko.dto.CellDto;

import java.util.*;

public class XsdReaderEclipse {

    private XSDSchema xsdSchema;
    private CellDto rootCellDto;
    private List<CellDto> cellDtoList = new ArrayList<>();
    private Set<String> alreadyReadenNames = new HashSet<>();
    private Queue<XSDComplexTypeDefinition> otherComplexTypes = new LinkedList<>();
    private boolean isNav = false;

    public CellDto readSchemaElement(String xsdFile) {
        loadSchema(xsdFile);

        List<XSDSchemaContent> rootElements = xsdSchema.getContents().stream().filter(el -> (el instanceof XSDElementDeclaration)).toList();
        if (rootElements.isEmpty()) {
            throw new RuntimeException("No root elements found in schema");
        }

        XSDElementDeclaration rootElement = (XSDElementDeclaration) rootElements.get(0);

        rootCellDto = new CellDto();
        rootCellDto.setType("element");
        rootCellDto.setName("root");
        rootCellDto.setFileName(rootElement.getName());

        parseXSDElementDeclarationRoot(rootElement, rootCellDto);

        return rootCellDto;
    }


    public List<CellDto> readSchemaElementAsNav(String xsdFile) throws Exception {
        isNav = true;
        loadSchema(xsdFile);

        List<XSDSchemaContent> rootElements = xsdSchema.getContents().stream().filter(el -> (el instanceof XSDElementDeclaration)).toList();
        if (rootElements.isEmpty()) {
            throw new RuntimeException("No root elements found in schema");
        }

        XSDElementDeclaration rootElement = (XSDElementDeclaration) rootElements.get(0);

        XSDTypeDefinition rootType = rootElement.getTypeDefinition();
        if (rootType instanceof XSDComplexTypeDefinition xsdComplexTypeDefinition) {
            otherComplexTypes.add(xsdComplexTypeDefinition);
        } else {
            rootCellDto.setName(rootElement.getName());
        }

        while (!otherComplexTypes.isEmpty()) {
            XSDComplexTypeDefinition xsdComplexTypeDefinition = otherComplexTypes.poll();
            createNewCellDtoTree(xsdComplexTypeDefinition);
        }

        return cellDtoList;
    }

    private void loadSchema(String xsdFile) {
        ResourceSet resourceSet = new ResourceSetImpl();
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xsd", new org.eclipse.xsd.util.XSDResourceFactoryImpl());
        Resource resource = resourceSet.getResource(URI.createFileURI(xsdFile), true);
        xsdSchema = (XSDSchema) resource.getContents().get(0);
    }

    private void parseXSDElementDeclarationRoot(XSDElementDeclaration element, CellDto cellDtoPrev) {
        CellDto cellDto = new CellDto();
        cellDtoPrev.getChildren().add(cellDto);
        cellDto.setType("element");
        cellDto.setMinOccurs("1");
        cellDto.setMaxOccurs("1");
        parseXSDElementDeclaration(element, cellDto);
    }

    private void parseXSDElementDeclaration(XSDElementDeclaration element, CellDto cellDto) {
        cellDto.setName(element.getName());
        cellDto.setType("element");
        cellDto.setPathFromRoot(cellDto.getPathFromRoot() + element.getName() + "/");
        XSDAnnotation xsdAnnotation = element.getAnnotation();
        if (xsdAnnotation != null) {
            cellDto.setDocumentation(getDocumentation(element.getAnnotation()));
        }
        if (element.getAnnotation() != null && !element.getAnnotation().getUserInformation().get(0).getTextContent().isEmpty()) {
            cellDto.setDocumentation(getDocumentation(element.getAnnotation()));
        }
        XSDTypeDefinition xsdTypeDefinition = element.getTypeDefinition();
        if (xsdTypeDefinition instanceof XSDComplexTypeDefinition xsdComplexTypeDefinition) {
            parseXSDComplexTypeDefinition(xsdComplexTypeDefinition, cellDto);
        } else if (xsdTypeDefinition instanceof XSDSimpleTypeDefinition xsdSimpleTypeDefinition) {
            parseXSDSimpleTypeDefinition(xsdSimpleTypeDefinition, cellDto);
        }
    }


    private void createNewCellDtoTree(XSDComplexTypeDefinition xsdComplexTypeDefinition) {
        CellDto cellRoot = new CellDto();
        cellRoot.setType("schema");
        cellRoot.setName("root");
        cellRoot.setPathFromRoot(xsdComplexTypeDefinition.getName() + "/");
        cellDtoList.add(cellRoot);
        parseXSDComplexTypeDefinition(xsdComplexTypeDefinition, cellRoot);
    }


    private void parseXSDSimpleTypeDefinition(XSDSimpleTypeDefinition xsdSimpleTypeDefinition, CellDto cellDtoPrev) {
        CellDto cellDto = new CellDto();
        cellDtoPrev.getChildren().add(cellDto);
        cellDto.setType("st");
        cellDto.setDocumentation(getDocumentation(xsdSimpleTypeDefinition.getAnnotation()));
        cellDto.setName(xsdSimpleTypeDefinition.getName());
        cellDto.setTargetNameSpace(xsdSimpleTypeDefinition.getTargetNamespace());
    }

    private String createFileName(XSDComplexTypeDefinition xsdComplexTypeDefinition) {
        String name = fixNamespace(xsdComplexTypeDefinition.getTargetNamespace()) + xsdComplexTypeDefinition.getName() + ".html";
        return name.replaceAll("#", "sharp");
    }

    private String fixNamespace(String nameSpace) {
        nameSpace = nameSpace.replace("http://", "");
        nameSpace = nameSpace.replace("/", "_");
        nameSpace = nameSpace.replace(".xsd", "");
        nameSpace += "_";
        return nameSpace;
    }

    private void parseXSDComplexTypeDefinition(XSDComplexTypeDefinition xsdComplexTypeDefinition, CellDto cellDtoPrev) {
        CellDto cellDto = new CellDto();
        cellDtoPrev.getChildren().add(cellDto);
        cellDto.setType("ct");
        cellDto.setPathFromRoot(cellDtoPrev.getPathFromRoot());
        cellDto.setName(xsdComplexTypeDefinition.getName());
        cellDto.setDocumentation(getDocumentation(xsdComplexTypeDefinition.getAnnotation()));

        if (xsdComplexTypeDefinition.getName() != null) {
            cellDto.setName(xsdComplexTypeDefinition.getName());
            cellDto.setTargetNameSpace(xsdComplexTypeDefinition.getTargetNamespace());
            if (isNav) {
                if (!Objects.equals(cellDtoPrev.getType(), "schema")) {
                    cellDto.setLinkToChild(createFileName(xsdComplexTypeDefinition));
                    if (alreadyReadenNames.add(createFileName(xsdComplexTypeDefinition))) {
                        otherComplexTypes.add(xsdComplexTypeDefinition);
                    }
                    return;
                } else {
                    cellDtoPrev.setFileName(createFileName(xsdComplexTypeDefinition));
                }

            }
        }

        List<XSDAttributeGroupContent> xsdAttributeGroupContents = xsdComplexTypeDefinition.getAttributeContents();
        for (XSDAttributeGroupContent xsdAttributeGroupContent : xsdAttributeGroupContents) {
            if (xsdAttributeGroupContent instanceof XSDAttributeUse xsdAttributeUse) {
                parseXSDAttributeUse(xsdAttributeUse, cellDto);
            } else if (xsdAttributeGroupContent instanceof XSDAttributeGroupDefinition xsdAttributeGroupDefinition) {
                parseXSDAttributeGroupDefinition(xsdAttributeGroupDefinition, cellDto);
            }
        }
        XSDTypeDefinition xsdTypeDefinition = xsdComplexTypeDefinition.getBaseTypeDefinition();
        if (!xsdTypeDefinition.getName().equals("anyType")) {
            parseBase(xsdTypeDefinition, cellDto);
        }
        XSDComplexTypeContent xsdComplexTypeContent = xsdComplexTypeDefinition.getContent();
        if (xsdComplexTypeContent instanceof XSDParticle xsdParticle) {
            parseParticle(xsdParticle, cellDto);
        }
    }

    private void parseXSDAttributeUse(XSDAttributeUse xsdAttributeUse, CellDto cellDtoPrev) {
        CellDto cellDto = new CellDto();

        cellDtoPrev.getChildren().add(cellDto);
        cellDto.setType("attribute");
        cellDto.setName(xsdAttributeUse.getAttributeDeclaration().getName());
        cellDto.setPathFromRoot(cellDtoPrev.getPathFromRoot() + xsdAttributeUse.getAttributeDeclaration().getName() + "/");
        cellDto.setDocumentation(getDocumentation(xsdAttributeUse.getAttributeDeclaration().getAnnotation()));
        cellDto.setMaxOccurs("1");
        cellDto.setMaxOccurs("0");
        if (xsdAttributeUse.getUse().getName().equals("required")) {
            cellDto.setMinOccurs("1");
        }
        parseXSDSimpleTypeDefinition(xsdAttributeUse.getAttributeDeclaration().getTypeDefinition(), cellDto);
    }

    private void parseXSDAttributeGroupDefinition(XSDAttributeGroupDefinition xsdAttributeGroupDefinition, CellDto cellDtoPrev) {
        XSDAttributeGroupDefinition xsdAttributeGroupDefinitionResolved = xsdAttributeGroupDefinition.getResolvedAttributeGroupDefinition();
        CellDto cellDto = new CellDto();
        cellDtoPrev.getChildren().add(cellDto);
        cellDto.setType("attributeGroup");
        cellDto.setName(xsdAttributeGroupDefinitionResolved.getName());
        cellDto.setDocumentation(getDocumentation(xsdAttributeGroupDefinitionResolved.getAnnotation()));
        cellDto.setMaxOccurs("1");
        cellDto.setMaxOccurs("1");
        xsdAttributeGroupDefinitionResolved.getAttributeUses().forEach(a -> parseXSDAttributeUse(a, cellDto));

    }

    private void parseParticle(XSDParticle xsdParticle, CellDto cellDtoPrev) {
        CellDto cellDto = new CellDto();
        cellDtoPrev.getChildren().add(cellDto);
        cellDto.setPathFromRoot(cellDtoPrev.getPathFromRoot());
        cellDto.setMinOccurs(String.valueOf(xsdParticle.getMinOccurs()));
        cellDto.setMaxOccurs(xsdParticle.getMaxOccurs() == -1 ? "unbounded" : String.valueOf(xsdParticle.getMaxOccurs()));

        XSDTerm xsdTerm = xsdParticle.getTerm();

        if (xsdTerm instanceof XSDElementDeclaration xsdElementDeclaration) {
            parseXSDElementDeclaration(xsdElementDeclaration, cellDto);
        }
        else if (xsdTerm instanceof XSDModelGroup xsdModelGroup) {
            // Проверяем, является ли это ссылкой на группу
            if (xsdModelGroup.eContainer() instanceof XSDModelGroupDefinition groupDef) {
                parseXSDModelGroupDefinition(groupDef, cellDto);
            } else {
                // Обычная xs:sequence, xs:choice, xs:all
                parseXSDModelGroup(xsdModelGroup, cellDto);
            }
        }
        else if (xsdTerm instanceof XSDWildcard xsdWildcard) {
            parseXSDWildcard(xsdWildcard, cellDto);
        }
    }

    private void parseXSDWildcard(XSDWildcard xsdWildcard, CellDto cellDtoPrev) {
        CellDto cellDto = new CellDto();
        cellDtoPrev.getChildren().add(cellDto);
        cellDto.setType("any");
    }

    private void parseXSDModelGroupDefinition(XSDModelGroupDefinition xsdModelGroupDefinition, CellDto cellDto) {
        cellDto.setType("group");
        cellDto.setName(xsdModelGroupDefinition.getName());
        cellDto.setTargetNameSpace(xsdModelGroupDefinition.getTargetNamespace());
        CellDto cellDtoChild = new CellDto();
        cellDto.getChildren().add(cellDtoChild);
        cellDtoChild.setMinOccurs("1");
        cellDtoChild.setMaxOccurs("1");
        cellDtoChild.setPathFromRoot(cellDto.getPathFromRoot());
        XSDModelGroupDefinition xsdModelGroupDefinitionResolved = xsdModelGroupDefinition.getResolvedModelGroupDefinition();
        cellDto.setDocumentation(getDocumentation(xsdModelGroupDefinitionResolved.getAnnotation()));
        parseXSDModelGroup(xsdModelGroupDefinitionResolved.getModelGroup(), cellDtoChild);
    }

    private void parseXSDModelGroup(XSDModelGroup xsdModelGroup, CellDto cellDto) {
        switch (xsdModelGroup.getCompositor().getValue()) {
            case XSDCompositor.SEQUENCE -> cellDto.setType("seq");
            case XSDCompositor.CHOICE -> cellDto.setType("choice");
            case XSDCompositor.ALL -> cellDto.setType("all");
        }
        xsdModelGroup.getParticles().forEach(p -> parseParticle(p, cellDto));
    }

    private void parseBase(XSDTypeDefinition xsdTypeDefinition, CellDto cellDtoPrev) {
        CellDto cellDto = new CellDto();
        cellDtoPrev.getChildren().add(cellDto);
        cellDto.setType("base");
        if (xsdTypeDefinition instanceof XSDSimpleTypeDefinition xsdSimpleTypeDefinition) {
            parseXSDSimpleTypeDefinition(xsdSimpleTypeDefinition, cellDto);
        } else if (xsdTypeDefinition instanceof XSDComplexTypeDefinition xsdComplexTypeDefinition) {
            parseXSDComplexTypeDefinition(xsdComplexTypeDefinition, cellDto);
        } else {
            throw new RuntimeException("I don't fucking know how to parse this bullshit");
        }
    }

    private String getDocumentation(XSDAnnotation xsdAnnotation) {
        if (xsdAnnotation == null) return null;

        // Проверка на пустой список
        if (xsdAnnotation.getUserInformation().isEmpty()) return null;

        String text = xsdAnnotation.getUserInformation().get(0).getTextContent();
        if (text == null) return null;

        // Удаляем ВСЕ переносы в начале и конце
        text = text.trim();
        return text.isEmpty() ? null : text;
    }
}