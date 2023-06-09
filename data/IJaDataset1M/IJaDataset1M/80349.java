package org.collada.colladaschema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * <p>Java class for gles_texcombiner_argumentAlpha_type complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="gles_texcombiner_argumentAlpha_type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="operand" type="{http://www.collada.org/2005/11/COLLADASchema}gles_texcombiner_operandAlpha_enums" default="SRC_ALPHA" />
 *       &lt;attribute name="source" type="{http://www.collada.org/2005/11/COLLADASchema}gles_texcombiner_source_enums" />
 *       &lt;attribute name="unit" type="{http://www.w3.org/2001/XMLSchema}NCName" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "gles_texcombiner_argumentAlpha_type")
public class GlesTexcombinerArgumentAlphaType {

    @XmlAttribute
    protected GlesTexcombinerOperandAlphaEnums operand;

    @XmlAttribute
    protected GlesTexcombinerSourceEnums source;

    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String unit;

    /**
     * Gets the value of the operand property.
     * 
     * @return
     *     possible object is
     *     {@link GlesTexcombinerOperandAlphaEnums }
     *     
     */
    public GlesTexcombinerOperandAlphaEnums getOperand() {
        if (operand == null) {
            return GlesTexcombinerOperandAlphaEnums.SRC_ALPHA;
        } else {
            return operand;
        }
    }

    /**
     * Sets the value of the operand property.
     * 
     * @param value
     *     allowed object is
     *     {@link GlesTexcombinerOperandAlphaEnums }
     *     
     */
    public void setOperand(GlesTexcombinerOperandAlphaEnums value) {
        this.operand = value;
    }

    /**
     * Gets the value of the source property.
     * 
     * @return
     *     possible object is
     *     {@link GlesTexcombinerSourceEnums }
     *     
     */
    public GlesTexcombinerSourceEnums getSource() {
        return source;
    }

    /**
     * Sets the value of the source property.
     * 
     * @param value
     *     allowed object is
     *     {@link GlesTexcombinerSourceEnums }
     *     
     */
    public void setSource(GlesTexcombinerSourceEnums value) {
        this.source = value;
    }

    /**
     * Gets the value of the unit property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Sets the value of the unit property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUnit(String value) {
        this.unit = value;
    }
}
