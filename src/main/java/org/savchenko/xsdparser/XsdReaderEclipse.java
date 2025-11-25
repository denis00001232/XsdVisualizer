package org.savchenko.xsdparser;

import org.apache.xerces.xs.XSTypeDefinition;
import org.savchenko.dto.CellDto;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.xsd.*;

import java.util.*;

public class XsdReaderEclipse {

    private XSDSchema xsdSchema;
    private CellDto rootCellDto;
    private List<CellDto> cellDtoList = new ArrayList<>();
    private Set<String> alreadyProcessedComplexTypes = new HashSet<>();
    private Queue<XSDComplexTypeDefinition> otherComplexTypes = new LinkedList<>();
    private boolean isNav = false;

    public CellDto readSchemaElement(String xsdFile) {
        loadSchema(xsdFile);

        List<XSDElementDeclaration> rootElements = xsdSchema.getElementDeclarations();
        if (rootElements.isEmpty()) {
            throw new RuntimeException("No root elements found in schema");
        }

        XSDElementDeclaration rootElement = rootElements.get(0);

        rootCellDto = new CellDto();
        rootCellDto.setType("element");
        rootCellDto.setName("root");
        rootCellDto.setFileName(rootElement.getName());

        parseXSDElementDeclaration(rootElement, rootCellDto);

        return rootCellDto;
    }
    /*
    public List<CellDto> readSchemaElementAsNav(String xsdFile) throws Exception {
        isNav = true;
        loadSchema(xsdFile);

        List<XSDElementDeclaration> rootElements = xsdSchema.getElementDeclarations();
        if (rootElements.isEmpty()) {
            throw new RuntimeException("No root elements found in schema");
        }

        XSDElementDeclaration rootElement = rootElements.get(0);

        rootCellDto = new CellDto();
        rootCellDto.setType("element");
        rootCellDto.setName("root");

        cellDtoList.add(rootCellDto);

        XSDTypeDefinition rootType = rootElement.getTypeDefinition();
        if (rootType instanceof XSDComplexTypeDefinition ctd) {
            parseComplexTypeRoot(ctd, rootCellDto);
        } else {
            rootCellDto.setName(rootElement.getName());
        }

        // Обработка отложенных complexType, если в режиме навигации
        while (!otherComplexTypes.isEmpty()) {
            XSDComplexTypeDefinition ctd = otherComplexTypes.poll();
            createNewCellDtoTree(ctd);
        }

        return cellDtoList;
    }
    */
    private void loadSchema(String xsdFile) {
        ResourceSet resourceSet = new ResourceSetImpl();
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xsd", new org.eclipse.xsd.util.XSDResourceFactoryImpl());
        Resource resource = resourceSet.getResource(URI.createFileURI(xsdFile), true);
        xsdSchema = (XSDSchema) resource.getContents().get(0);
    }

    private void parseXSDElementDeclaration(XSDElementDeclaration element, CellDto parent) {
        CellDto cellDto = new CellDto();
        parent.getChildren().add(cellDto);
        cellDto.setName(element.getName());
        cellDto.setType("element");

        // minOccurs и maxOccurs берутся из объявления части схемы, для элемента можно получить XSDParticle
        int minOccurs = 1;
        int maxOccurs = 1;
        if (element.eContainer() instanceof XSDParticle particle) {
            minOccurs = particle.getMinOccurs();
            maxOccurs = particle.getMaxOccurs();
        }
        cellDto.setMinOccurs(String.valueOf(minOccurs));
        cellDto.setMaxOccurs(maxOccurs == -1 ? "unbounded" : String.valueOf(maxOccurs));

        XSDAnnotation xsdAnnotation = element.getAnnotation();
        if (xsdAnnotation != null) {
            cellDto.setDocumentation(xsdAnnotation.getUserInformation().get(0).getTextContent());
        }

        if (element.getAnnotation() != null && !element.getAnnotation().getUserInformation().get(0).getTextContent().isEmpty()) {
            cellDto.setDocumentation(element.getAnnotation().getUserInformation().get(0).getTextContent());
        }

        XSDTypeDefinition xsdTypeDefinition = element.getTypeDefinition();
        if (xsdTypeDefinition instanceof XSDComplexTypeDefinition xsdComplexTypeDefinition) {
            parseXSDComplexTypeDefinition(xsdComplexTypeDefinition, cellDto);
        } else if (xsdTypeDefinition instanceof XSDSimpleTypeDefinition xsdSimpleTypeDefinition) {
            parseSimpleType(xsdSimpleTypeDefinition, cellDto);
        } else {
            // Примитивный тип, может быть имя типа
            cellDto.setName(cellDto.getName() + " : " + xsdTypeDefinition.getName());
        }
    }

    /*
    private void createNewCellDtoTree(XSDComplexTypeDefinition complexType) {
        CellDto cellRoot = new CellDto();
        cellRoot.setType("schema");
        cellRoot.setName("root");
        cellDtoList.add(cellRoot);
        parseComplexTypeRoot(complexType, cellRoot);
    }

     */

    private void parseSimpleType(XSDSimpleTypeDefinition xsdSimpleTypeDefinition, CellDto cellDtoPrev) {

    }

    private void parseXSDComplexTypeDefinition(XSDComplexTypeDefinition xsdComplexTypeDefinition, CellDto parent) {
        CellDto cellDto = new CellDto();
        parent.getChildren().add(cellDto);
        cellDto.setType("ct");
        cellDto.setName(xsdComplexTypeDefinition.getName());
        cellDto.setDocumentation(getDocumentation(xsdComplexTypeDefinition.getAnnotation()));

        XSDTypeDefinition xsdTypeDefinition = xsdComplexTypeDefinition.getBaseTypeDefinition();
        if (!xsdTypeDefinition.getName().equals("anyType")) {
            parseBase(xsdTypeDefinition, cellDto);
        }
         xsdComplexTypeDefinition.getContent();
        if (complexContent instanceof XSDComplexContent complexCon) {
            parseComplexContent(complexCon, cellDto);
        } else {
            XSDParticle particle = complexType.getContent() instanceof XSDParticle p ? p : null;
            if (particle != null) {
                parseParticle(particle, cellDto);
            }
        }
    }

    private void parseBase(XSDTypeDefinition xsdTypeDefinition, CellDto cellDtoPrev) {
        CellDto cellDto = new CellDto();
        cellDtoPrev.getChildren().add(cellDto);
        cellDto.setType("base");
        parseXSDComplexTypeDefinition((XSDComplexTypeDefinition) xsdTypeDefinition, cellDto);
    }

    private String getDocumentation(XSDAnnotation xsdAnnotation) {
        if (xsdAnnotation == null) return null;
        return xsdAnnotation.getUserInformation().get(0).getTextContent();
    }

    /*
    private void parseComplexContent(XSDComplexContent complexContent, CellDto parent) {
        CellDto cell = new CellDto();
        parent.getChildren().add(cell);
        cell.setType("complexContent");
        cell.setDocumentation(getFirstDocumentation(complexContent));

        XSDExtension extension = complexContent.getExtension();
        if (extension != null) {
            XSDTypeDefinition base = extension.getBaseTypeDefinition();
            CellDto baseCell = new CellDto();
            baseCell.setType("base");
            baseCell.setName(base.getName());
            cell.getChildren().add(baseCell);

            // Разбор расширения — элементы, атрибуты, группы
            XSDParticle particle = extension.getContent() instanceof XSDParticle p ? p : null;
            if (particle != null) {
                parseParticle(particle, cell);
            }

            for (XSDAttributeUse attrUse : extension.getAttributeUses()) {
                parseAttribute(attrUse.getAttributeDeclaration(), cell);
            }
        } else {
            // Возможен restriction или другое содержимое, по аналогии можно добавить
        }
    }
    private void parseComplexTypeRoot(XSDComplexTypeDefinition complexType, CellDto parent) {
        CellDto cell = new CellDto();
        parent.getChildren().add(cell);
        cell.setType("ct");
        cell.setName(complexType.getName());
        cell.setTargetNameSpace(complexType.getSchema().getTargetNamespace());
        cell.setDocumentation(getFirstDocumentation(complexType));

        if (isNav && complexType.getName() != null) {
            String fileName = createFileName(complexType);
            cell.setLinkToChild(fileName);
            if (alreadyProcessedComplexTypes.add(fileName)) {
                otherComplexTypes.add(complexType);
            }
            parent.setFileName(fileName);
            return;
        }

        // Атрибуты
        for (XSDAttributeUse attrUse : complexType.getAttributeUses()) {
            parseAttribute(attrUse.getAttributeDeclaration(), cell);
        }
        // Атрибутные группы
        // В Eclipse XSD атрибутные группы могут быть получены через getAttributeContents() с фильтрацией

        XSDComplexTypeContent complexContent = complexType.getContent();
        if (complexContent instanceof XSDComplexContent complexCon) {
            parseComplexContent(complexCon, cell);
        } else {
            // Основное содержимое: последовательность, выбор, группа или элемент
            XSDElementDeclaration childElement = complexType.getElementDeclaration();
            if (childElement != null) {
                parseElement(childElement, cell);
            } else {
                // В иных случаях можно получить части content модели из particle
                XSDParticle particle = complexType.getContent() instanceof XSDParticle p ? p : null;
                if (particle != null) {
                    parseParticle(particle, cell);
                }
            }
        }
    }
    private void parseParticle(XSDParticle particle, CellDto parent) {
        XSDTerm term = particle.getTerm();
        if (term instanceof XSDElementDeclaration element) {
            parseElement(element, parent);
        } else if (term instanceof XSDModelGroup group) {
            parseModelGroup(group, parent, particle.getMinOccurs(), particle.getMaxOccurs());
        } else if (term instanceof XSDWildcard) {
            CellDto anyCell = new CellDto();
            parent.getChildren().add(anyCell);
            anyCell.setType("any");
            anyCell.setMinOccurs(String.valueOf(particle.getMinOccurs()));
            anyCell.setMaxOccurs(particle.getMaxOccurs() == -1 ? "unbounded" : String.valueOf(particle.getMaxOccurs()));
            anyCell.setName("any");
        }
    }

    private void parseModelGroup(XSDModelGroup group, CellDto parent, int minOccurs, int maxOccurs) {
        CellDto groupCell = new CellDto();
        parent.getChildren().add(groupCell);
        groupCell.setMinOccurs(String.valueOf(minOccurs));
        groupCell.setMaxOccurs(maxOccurs == -1 ? "unbounded" : String.valueOf(maxOccurs));

        switch (group.getCompositor()) {
            case SEQUENCE -> groupCell.setType("sequence");
            case CHOICE -> groupCell.setType("choice");
            case ALL -> groupCell.setType("all");
            default -> groupCell.setType("group");
        }
        groupCell.setName("modelGroup");

        if (group.getAnnotation() != null && !group.getAnnotation().getDocumentations().isEmpty()) {
            groupCell.setDocumentation(group.getAnnotation().getDocumentations().get(0).getMixedText());
        }

        for (XSDParticle childParticle : group.getParticles()) {
            parseParticle(childParticle, groupCell);
        }
    }

    private void parseAttribute(XSDAttributeDeclaration attribute, CellDto parent) {
        CellDto cell = new CellDto();
        parent.getChildren().add(cell);
        cell.setType("attribute");
        cell.setName(attribute.getName());
        if (attribute.getUseLiteral() != null && "optional".equals(attribute.getUseLiteral().toString())) {
            cell.setMinOccurs("0");
        }

        XSDTypeDefinition typeDef = attribute.getType();
        if (typeDef instanceof XSDSimpleTypeDefinition simpleType) {
            parseSimpleType(simpleType, cell);
        } else {
            cell.setName(cell.getName() + " : " + typeDef.getName());
        }
    }

    private void parseSimpleType(XSDSimpleTypeDefinition simpleType, CellDto parent) {
        CellDto cell = new CellDto();
        parent.getChildren().add(cell);
        cell.setType("simpleType");
        cell.setName(simpleType.getName());
        if (simpleType.getSchema() != null) {
            cell.setTargetNameSpace(simpleType.getSchema().getTargetNamespace());
        }
    }


    private String createFileName(XSDComplexTypeDefinition complexType) {
        try {
            String ns = complexType.getSchema().getTargetNamespace();
            ns = ns.replace("http://", "").replace("/", "_").replace(".xsd", "") + "_";
            return ns + complexType.getName().replace("#", "sharp") + ".html";
        } catch (Exception e) {
            return complexType.getName() + UUID.randomUUID() + ".html";
        }
    }

     */
}