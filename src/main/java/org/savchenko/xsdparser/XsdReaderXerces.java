package org.savchenko.xsdparser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.xerces.xs.*;
import org.savchenko.dto.CellDto;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.xmlet.xsdparser.core.XsdParser;
import org.xmlet.xsdparser.xsdelements.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class XsdReaderXerces {
    private XSModel xsModel;
    private XSElementDeclaration xsdElementRoot;
    private List<CellDto> cellDtoList = new ArrayList<>();
    private Set<String> alreadyReadenNames = new HashSet<>();
    private Queue<XsdComplexType> otherComplexTypes = new LinkedList<>();
    private boolean isNav = false;

    private XSModel loadSchema(String xsdPath) {
        try {
            // Регистрация реализации (иногда нужно вызвать один раз в static блоке)
            System.setProperty(DOMImplementationRegistry.PROPERTY,
                    "org.apache.xerces.dom.DOMXSImplementationSourceImpl");
            DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
            XSImplementation xsImpl = (XSImplementation) registry.getDOMImplementation("XS-Loader");

            XSLoader xsLoader = xsImpl.createXSLoader(null);
            DOMConfiguration config = xsLoader.getConfig();

            XSModel xsModel = xsLoader.loadURI(xsdPath);
            if (xsModel == null) {
                throw new RuntimeException("XSModel is null for schema: " + xsdPath);
            }
            return xsModel;
        } catch (Exception e) {
            throw new RuntimeException("Error loading schema: " + xsdPath, e);
        }
    }

    private XSElementDeclaration findRootElement(XSModel xsModel) {
        StringList namespaces = xsModel.getNamespaces();

        for (int i = 0; i < namespaces.getLength(); i++) {
            String ns = namespaces.item(i);
            XSNamedMap elements = xsModel.getComponentsByNamespace(
                    XSConstants.ELEMENT_DECLARATION, ns
            );
            if (elements != null && elements.getLength() > 0) {
                // Берём первый попавшийся глобальный элемент
                return (XSElementDeclaration) elements.item(0);
            }
        }
        throw new RuntimeException("No global elements found in schema.");
    }


    public CellDto readSchemaElement(String xsdFile) {
        xsModel = loadSchema(xsdFile);
        xsdElementRoot = findRootElement(xsModel);
        CellDto cellDtoRoot = new CellDto();
        cellDtoRoot.setType("element");
        cellDtoRoot.setFileName(xsdElementRoot.getName());
        cellDtoRoot.setName("root");
        parseXSElementDeclaration(xsdElementRoot, cellDtoRoot);
        return cellDtoRoot;
    }

    private void parseXSParticle(XSParticle xsParticle, CellDto cellDtoPrev) {
        if (xsParticle == null) return;
        CellDto cellDto = new CellDto();
        cellDtoPrev.getChildren().add(cellDto);
        cellDto.setMinOccurs(String.valueOf(xsParticle.getMinOccurs())); //Выставляем min и maxOccurs
        cellDto.setMaxOccurs(String.valueOf(xsParticle.getMaxOccurs()));
        if (xsParticle.getMaxOccursUnbounded()) {
            cellDto.setMaxOccurs("unbounded");
        }
        XSTerm xsTerm = xsParticle.getTerm(); //Дальше решаем кто это вообще
        if (xsTerm instanceof XSElementDeclaration xsElementDeclaration) {
            parseXSElementDeclaration(xsElementDeclaration, cellDto);
        } else if (xsTerm instanceof XSModelGroup xsModelGroup) {
            parseXSModelGroup(xsModelGroup, cellDto);
        } else if (xsTerm instanceof XSWildcard xsWildcard) {
            parseXSWildcard(xsWildcard, cellDto);
        }

    }

    private void parseXSModelGroup(XSModelGroup xsModelGroup, CellDto cellDtoPrev) {
        switch (xsModelGroup.getCompositor()) {
            case XSModelGroup.COMPOSITOR_SEQUENCE -> cellDtoPrev.setType("seq");
            case XSModelGroup.COMPOSITOR_CHOICE -> cellDtoPrev.setType("choice");
            case XSModelGroup.COMPOSITOR_ALL -> cellDtoPrev.setType("all");
        }
        XSObjectList xsObjectList = xsModelGroup.getParticles();
        for (int i = 0; i < xsObjectList.getLength(); i++) {
            XSParticle xsParticle = (XSParticle) xsObjectList.get(i);
            parseXSParticle(xsParticle, cellDtoPrev);
        }
    }

    private void parseXSWildcard(XSWildcard xsWildcard, CellDto cellDtoPrev) {
        cellDtoPrev.setType("any");
    }

    private void parseXSElementDeclaration(XSElementDeclaration xsElementDeclaration, CellDto cellDtoPrev) {
        xsElementDeclaration.getName();
        cellDtoPrev.setType("element");
        cellDtoPrev.setName(xsElementDeclaration.getName());
        XSAnnotation xsAnnotation = xsElementDeclaration.getAnnotation();
        cellDtoPrev.setDocumentation(getDocumentationText(xsAnnotation));
        XSTypeDefinition xsTypeDefinition = xsElementDeclaration.getTypeDefinition();
        if (xsTypeDefinition instanceof XSComplexTypeDefinition xsComplexTypeDefinition) {
            parseXSComplexTypeDefinition(xsComplexTypeDefinition, cellDtoPrev);
        } else if (xsTypeDefinition instanceof XSSimpleTypeDefinition xsSimpleTypeDefinition) {
            parseXSSimpleTypeDefinition(xsSimpleTypeDefinition, cellDtoPrev);
        }

    }

    private void parseXSComplexTypeDefinition(XSComplexTypeDefinition xsComplexTypeDefinition, CellDto cellDtoPrev) {
        CellDto cellDto = new CellDto();
        cellDtoPrev.getChildren().add(cellDto);
        cellDto.setType("ct");
        if (xsComplexTypeDefinition.getName() != null) {
            cellDto.setName(xsComplexTypeDefinition.getName());
            cellDto.setTargetNameSpace(xsComplexTypeDefinition.getNamespace());
        }
        if (xsComplexTypeDefinition.getDerivationMethod() == XSConstants.DERIVATION_EXTENSION) {
            XSTypeDefinition xsTypeDefinition = xsComplexTypeDefinition.getBaseType();
            parseBase(xsTypeDefinition, cellDto);
        }
        parseXSParticle(xsComplexTypeDefinition.getParticle(), cellDto);
        XSObjectList xsObjectList = xsComplexTypeDefinition.getAttributeUses();
        for (int i = 0; i < xsObjectList.getLength(); i++) {
            XSAttributeUse xsAttributeUse = (XSAttributeUse) xsObjectList.get(i);
            parseXSAttributeUse(xsAttributeUse, cellDto);
        }
        XSObjectList annotations = xsComplexTypeDefinition.getAnnotations();
        if (annotations.getLength() != 0) {
            cellDto.setDocumentation(getDocumentationText((XSAnnotation) annotations.get(0)));
        }
    }

    private void parseXSAttributeUse(XSAttributeUse xsAttributeUse, CellDto cellDtoPrev) {
        CellDto cellDto = new CellDto();
        cellDtoPrev.getChildren().add(cellDto);
        cellDto.setType("attribute");
        cellDto.setMinOccurs("0");
        cellDto.setMaxOccurs("1");
        if (xsAttributeUse.getRequired()) {
            cellDto.setMinOccurs("1");
        }
        XSAttributeDeclaration xsAttributeDeclaration = xsAttributeUse.getAttrDeclaration();
        cellDto.setName(xsAttributeDeclaration.getName());
    }

    private void parseXSSimpleTypeDefinition(XSSimpleTypeDefinition xsSimpleTypeDefinition, CellDto cellDtoPrev) {
        CellDto cellDto = new CellDto();
        cellDtoPrev.getChildren().add(cellDto);
        cellDto.setType("st");
        if (xsSimpleTypeDefinition.getName() != null) {
            cellDto.setName(xsSimpleTypeDefinition.getName());
            cellDto.setTargetNameSpace(xsSimpleTypeDefinition.getNamespace());
        }
        XSObjectList annotations = xsSimpleTypeDefinition.getAnnotations();
        if (annotations.getLength() != 0) {
            cellDto.setDocumentation(getDocumentationText((XSAnnotation) annotations.get(0)));
        }
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(cellDto));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void parseBase(XSTypeDefinition xsTypeDefinition, CellDto cellDtoPrev) {
        CellDto cellDto = new CellDto();
        cellDtoPrev.getChildren().add(cellDto);
        cellDto.setType("base");
        if (xsTypeDefinition instanceof XSComplexTypeDefinition xsComplexTypeDefinition) {
            parseXSComplexTypeDefinition(xsComplexTypeDefinition, cellDto);
        } else if (xsTypeDefinition instanceof XSSimpleTypeDefinition xsSimpleTypeDefinition) {
            parseXSSimpleTypeDefinition(xsSimpleTypeDefinition, cellDto);
        }
    }

    public static String getDocumentationText(XSAnnotation annotation) {
        if (annotation == null) return null;

        try {
            String xml = annotation.getAnnotationString();
            if (xml == null || xml.isEmpty()) return null;

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document doc = builder.parse(
                    new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))
            );

            NodeList docs = doc.getElementsByTagNameNS(
                    "http://www.w3.org/2001/XMLSchema",
                    "documentation"
            );

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < docs.getLength(); i++) {
                if (i > 0) sb.append("\n");
                sb.append(docs.item(i).getTextContent().trim());
            }
            return sb.isEmpty() ? null : sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error reading xs:documentation", e);
        }
    }
}
