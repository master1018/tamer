package proguard.classfile.editor;
import proguard.classfile.*;
import proguard.classfile.attribute.*;
public class AttributesEditor
{
    private final ProgramClass  targetClass;
    private final ProgramMember targetMember;
    private final CodeAttribute targetAttribute;
    private final boolean       replaceAttributes;
    public AttributesEditor(ProgramClass targetClass,
                            boolean      replaceAttributes)
    {
        this(targetClass, null, null, replaceAttributes);
    }
    public AttributesEditor(ProgramClass  targetClass,
                            ProgramMember targetMember,
                            boolean       replaceAttributes)
    {
        this(targetClass, targetMember, null, replaceAttributes);
    }
    public AttributesEditor(ProgramClass  targetClass,
                            ProgramMember targetMember,
                            CodeAttribute targetAttribute,
                            boolean       replaceAttributes)
    {
        this.targetClass       = targetClass;
        this.targetMember      = targetMember;
        this.targetAttribute   = targetAttribute;
        this.replaceAttributes = replaceAttributes;
    }
    public void addAttribute(Attribute attribute)
    {
        if (targetAttribute != null)
        {
            if (!replaceAttributes ||
                !replaceAttribute(targetAttribute.u2attributesCount,
                                  targetAttribute.attributes,
                                  attribute))
            {
                targetAttribute.attributes =
                    addAttribute(targetAttribute.u2attributesCount,
                                 targetAttribute.attributes,
                                 attribute);
                targetAttribute.u2attributesCount++;
            }
        }
        else if (targetMember != null)
        {
            if (!replaceAttributes ||
                !replaceAttribute(targetMember.u2attributesCount,
                                  targetMember.attributes,
                                  attribute))
            {
                targetMember.attributes =
                    addAttribute(targetMember.u2attributesCount,
                                 targetMember.attributes,
                                 attribute);
                targetMember.u2attributesCount++;
            }
        }
        else
        {
            if (!replaceAttributes ||
                !replaceAttribute(targetClass.u2attributesCount,
                                  targetClass.attributes,
                                  attribute))
            {
                targetClass.attributes =
                    addAttribute(targetClass.u2attributesCount,
                                 targetClass.attributes,
                                 attribute);
                targetClass.u2attributesCount++;
            }
        }
    }
    public void deleteAttribute(String attributeName)
    {
        if (targetAttribute != null)
        {
            targetAttribute.u2attributesCount =
                deleteAttribute(targetAttribute.u2attributesCount,
                                targetAttribute.attributes,
                                attributeName);
        }
        else if (targetMember != null)
        {
            targetMember.u2attributesCount =
                deleteAttribute(targetMember.u2attributesCount,
                                targetMember.attributes,
                                attributeName);
        }
        else
        {
            targetClass.u2attributesCount =
                deleteAttribute(targetClass.u2attributesCount,
                                targetClass.attributes,
                                attributeName);
        }
    }
    private boolean replaceAttribute(int         attributesCount,
                                     Attribute[] attributes,
                                     Attribute   attribute)
    {
        int index = findAttribute(attributesCount,
                                  attributes,
                                  attribute.getAttributeName(targetClass));
        if (index < 0)
        {
            return false;
        }
        attributes[index] = attribute;
        return true;
    }
    private Attribute[] addAttribute(int         attributesCount,
                                     Attribute[] attributes,
                                     Attribute   attribute)
    {
        if (attributes.length <= attributesCount)
        {
            Attribute[] newAttributes = new Attribute[attributesCount + 1];
            System.arraycopy(attributes, 0,
                             newAttributes, 0,
                             attributesCount);
            attributes = newAttributes;
        }
        attributes[attributesCount] = attribute;
        return attributes;
    }
    private int deleteAttribute(int         attributesCount,
                                Attribute[] attributes,
                                String      attributeName)
    {
        int index = findAttribute(attributesCount,
                                  attributes,
                                  attributeName);
        if (index < 0)
        {
            return attributesCount;
        }
        System.arraycopy(attributes, index + 1,
                         attributes, index,
                         attributesCount - index - 1);
        attributes[--attributesCount] = null;
        return attributesCount;
    }
    private int findAttribute(int         attributesCount,
                              Attribute[] attributes,
                              String      attributeName)
    {
        for (int index = 0; index < attributesCount; index++)
        {
            if (attributes[index].getAttributeName(targetClass).equals(attributeName))
            {
                return index;
            }
        }
        return -1;
    }
}
