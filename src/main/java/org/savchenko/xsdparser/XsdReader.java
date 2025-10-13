package org.savchenko.xsdparser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.savchenko.dto.CellDto;
import org.xmlet.xsdparser.core.XsdParser;
import org.xmlet.xsdparser.xsdelements.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class XsdReader {
    private XsdElement xsdElementRoot;
    private CellDto cellDtoRoot;
    private int idItr = 1;

    public XsdReader(String xsdFile) {
        XsdParser xsdParser = new XsdParser(xsdFile);
        Optional<XsdElement> o = xsdParser.getResultXsdElements().findFirst();
        if (o.isEmpty()) {
            throw new RuntimeException();
        }
        xsdElementRoot = o.get();
        cellDtoRoot = new CellDto();
        cellDtoRoot.setType("element");
        cellDtoRoot.setName("root");
        parseXsdElement(xsdElementRoot, cellDtoRoot);
    }

    public CellDto getReadResult() {
        return cellDtoRoot;
    }


    private void parseXsdElement(XsdElement xsdElement, CellDto cellDtoPrev) {
        CellDto cellDto = new CellDto();
        cellDtoPrev.getChildren().add(cellDto);
        cellDto.setName(xsdElement.getName());
        cellDto.setType("element");
        cellDto.setMinOccurs(String.valueOf(xsdElement.getMinOccurs()));
        cellDto.setMaxOccurs(xsdElement.getMaxOccurs());

        if (xsdElement.getAnnotation() != null) {
            XsdDocumentation xsdDocumentation = xsdElement.getAnnotation().getDocumentations().get(0);
            if (xsdDocumentation != null) {
                cellDto.setDocumentation(xsdDocumentation.getContent());
            }
        }

        XsdComplexType xsdComplexType = xsdElement.getXsdComplexType();
        XsdSimpleType xsdSimpleType = xsdElement.getXsdSimpleType();
        if (xsdComplexType != null) {
            parseXsdComplexType(xsdComplexType, cellDto);
        } else if (xsdSimpleType != null){
            parseSimpleType(xsdSimpleType, cellDto);
        };

    }

    private void parseSimpleType(XsdSimpleType xsdSimpleType, CellDto cellDtoPrev) {
        CellDto cellDto = new CellDto();
        cellDtoPrev.getChildren().add(cellDto);
        cellDto.setType("simpleType");
        cellDto.setName(xsdSimpleType.getName());
    }

    private void parseXsdComplexType(XsdComplexType xsdComplexType, CellDto cellDtoPrev) {
        CellDto cellDto = new CellDto();
        cellDtoPrev.getChildren().add(cellDto);
        cellDto.setType("complexType");
        cellDto.setName(xsdComplexType.getName());
        if (xsdComplexType.getAnnotation() != null) {
            XsdDocumentation xsdDocumentation = xsdComplexType.getAnnotation().getDocumentations().get(0);
            if (xsdDocumentation != null) {
                cellDto.setDocumentation(xsdDocumentation.getContent());
            }
        }
        xsdComplexType.getXsdAttributes().forEach(xsdAttribute -> {
            parseXsdAttribute(xsdAttribute, cellDto);
        });
        xsdComplexType.getXsdAttributeGroup().forEach(xsdAttributeGroup -> {
            parseXsdAttributeGroup(xsdAttributeGroup, cellDto);
        });

        XsdComplexContent xsdComplexContent = xsdComplexType.getComplexContent();
        if (xsdComplexContent != null) {
            parseXsdComplexContent(xsdComplexContent, cellDto);
        } else {
            XsdAbstractElement xsdAbstractElement = xsdComplexType.getXsdChildElement();
            determinateAbstractElement(xsdAbstractElement, cellDto);
        }
    }

    private void parseXsdComplexContent(XsdComplexContent xsdComplexContent, CellDto cellDtoPrev) {
        CellDto cellDto = new CellDto();
        cellDtoPrev.getChildren().add(cellDto);
        cellDto.setType("complexContent");

        if (xsdComplexContent.getAnnotation() != null) {
            XsdDocumentation xsdDocumentation = xsdComplexContent.getAnnotation().getDocumentations().get(0);
            if (xsdDocumentation != null) {
                cellDto.setDocumentation(xsdDocumentation.getContent());
            }
        }

        XsdExtension xsdExtension = xsdComplexContent.getXsdExtension();
        XsdAnnotatedElements xsdBase = xsdExtension.getBase();
        parseXsdBase(xsdBase, cellDto);
        XsdAbstractElement xsdAbstractElement = xsdExtension.getXsdChildElement();
        determinateAbstractElement(xsdAbstractElement, cellDto);
        xsdExtension.getXsdAttributes().forEach(xsdAttribute -> {
            parseXsdAttribute(xsdAttribute, cellDto);
        });
        xsdExtension.getXsdAttributeGroup().forEach(xsdAttributeGroup -> {
            parseXsdAttributeGroup(xsdAttributeGroup, cellDto);
        });


    }

    private void parseXsdAttribute(XsdAttribute xsdAttribute, CellDto cellDtoPrev) {
        CellDto cellDto = new CellDto();
        cellDtoPrev.getChildren().add(cellDto);
        cellDto.setType("attribute");
        cellDto.setName(xsdAttribute.getName());
        String use = xsdAttribute.getUse();
        if (Objects.equals(use, "optional")) {
            cellDto.setMinOccurs("0");
        }
        XsdSimpleType xsdSimpleType = xsdAttribute.getXsdSimpleType();
        if (xsdSimpleType != null) {
            parseSimpleType(xsdSimpleType, cellDto);
        }
    }

    private void parseXsdAttributeGroup(XsdAttributeGroup xsdAttributeGroup, CellDto cellDtoPrev) {
        CellDto cellDto = new CellDto();
        cellDtoPrev.getChildren().add(cellDto);
        cellDto.setType("attributeGroup");
        cellDto.setName(xsdAttributeGroup.getName());
        xsdAttributeGroup.getXsdAttributes().forEach(xsdAttribute -> {
            parseXsdAttribute(xsdAttribute, cellDto);
        });
        xsdAttributeGroup.getAllXsdAttributeGroups().forEach(xsdAttributeGroupItr -> {
            parseXsdAttributeGroup(xsdAttributeGroupItr, cellDto);
        });
    }

    private void parseXsdBase(XsdAnnotatedElements xsdBase, CellDto cellDtoPrev) {
        CellDto cellDto = new CellDto();
        cellDtoPrev.getChildren().add(cellDto);
        cellDto.setType("base");
        if (xsdBase instanceof XsdComplexType xsdComplexType) {
            parseXsdComplexType(xsdComplexType, cellDto);
        }
    }

    private void determinateAbstractElement(XsdAbstractElement xsdAbstractElement, CellDto cellDto) {
        if (xsdAbstractElement instanceof XsdSequence xsdSequence) {
            parseXsdSequence(xsdSequence, cellDto);
        } else if (xsdAbstractElement instanceof XsdGroup xsdGroup) {
            parseXsdGroup(xsdGroup, cellDto);
        } else if (xsdAbstractElement instanceof XsdChoice xsdChoice) {
            parseXsdChoice(xsdChoice, cellDto);
        } else if (xsdAbstractElement instanceof XsdAll xsdAll) {
            parseXsdAll(xsdAll, cellDto);
        } else if (xsdAbstractElement instanceof XsdElement xsdElement) {
            parseXsdElement(xsdElement, cellDto);
        } else if (xsdAbstractElement instanceof XsdAny xsdAny) {
            parseXsdAny(xsdAny, cellDto);
        }
    }

    private void parseXsdSequence(XsdSequence xsdSequence, CellDto cellDtoPrev) {
        CellDto cellDto = new CellDto();
        cellDtoPrev.getChildren().add(cellDto);
        cellDto.setType("sequence");

        if (xsdSequence.getAnnotation() != null) {
            XsdDocumentation xsdDocumentation = xsdSequence.getAnnotation().getDocumentations().get(0);
            if (xsdDocumentation != null) {
                cellDto.setDocumentation(xsdDocumentation.getContent());
            }
        }

        xsdSequence.getXsdElements().forEach(xsdAbstractElement -> {
            determinateAbstractElement(xsdAbstractElement, cellDto);
        });
    }

    private void parseXsdGroup(XsdGroup xsdGroup, CellDto cellDtoPrev) {
        CellDto cellDto = new CellDto();
        cellDtoPrev.getChildren().add(cellDto);
        cellDto.setType("group");
        cellDto.setMinOccurs(String.valueOf(xsdGroup.getMinOccurs()));
        cellDto.setMaxOccurs(xsdGroup.getMaxOccurs());

        if (xsdGroup.getAnnotation() != null) {
            XsdDocumentation xsdDocumentation = xsdGroup.getAnnotation().getDocumentations().get(0);
            if (xsdDocumentation != null) {
                cellDto.setDocumentation(xsdDocumentation.getContent());
            }
        }

        xsdGroup.getXsdElements().forEach(xsdAbstractElement -> {
            determinateAbstractElement(xsdAbstractElement, cellDto);
        });
    }

    private void parseXsdChoice(XsdChoice xsdChoice, CellDto cellDtoPrev) {
        CellDto cellDto = new CellDto();
        cellDtoPrev.getChildren().add(cellDto);
        cellDto.setType("choice");
        cellDto.setMinOccurs(String.valueOf(xsdChoice.getMinOccurs()));
        cellDto.setMaxOccurs(xsdChoice.getMaxOccurs());

        if (xsdChoice.getAnnotation() != null) {
            XsdDocumentation xsdDocumentation = xsdChoice.getAnnotation().getDocumentations().get(0);
            if (xsdDocumentation != null) {
                cellDto.setDocumentation(xsdDocumentation.getContent());
            }
        }

        xsdChoice.getXsdElements().forEach(xsdAbstractElement -> {
            determinateAbstractElement(xsdAbstractElement, cellDto);
        });
    }

    private void parseXsdAll(XsdAll xsdAll, CellDto cellDtoPrev) {
        CellDto cellDto = new CellDto();
        cellDtoPrev.getChildren().add(cellDto);
        cellDto.setType("all");
        cellDto.setMinOccurs(String.valueOf(xsdAll.getMinOccurs()));
        cellDto.setMaxOccurs(String.valueOf(xsdAll.getMaxOccurs()));

        if (xsdAll.getAnnotation() != null) {
            XsdDocumentation xsdDocumentation = xsdAll.getAnnotation().getDocumentations().get(0);
            if (xsdDocumentation != null) {
                cellDto.setDocumentation(xsdDocumentation.getContent());
            }
        }

        xsdAll.getXsdElements().forEach(xsdAbstractElement -> {
            determinateAbstractElement(xsdAbstractElement, cellDto);
        });
    }

    private void parseXsdAny(XsdAny xsdAny, CellDto cellDtoPrev) {
        CellDto cellDto = new CellDto();
        cellDtoPrev.getChildren().add(cellDto);
        cellDto.setType("any");
        cellDto.setMinOccurs(String.valueOf(xsdAny.getMinOccurs()));
        cellDto.setMaxOccurs(xsdAny.getMaxOccurs());

        if (xsdAny.getAnnotation() != null) {
            XsdDocumentation xsdDocumentation = xsdAny.getAnnotation().getDocumentations().get(0);
            if (xsdDocumentation != null) {
                cellDto.setDocumentation(xsdDocumentation.getContent());
            }
        }

        xsdAny.getXsdElements().forEach(xsdAbstractElement -> {
            determinateAbstractElement(xsdAbstractElement, cellDto);
        });
    }

}
