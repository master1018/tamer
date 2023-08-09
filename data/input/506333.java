package proguard.classfile.editor;
import proguard.classfile.*;
import proguard.classfile.attribute.annotation.*;
public class ElementValuesEditor
{
    private final ProgramClass      targetClass;
    private final Annotation        targetAnnotation;
    private final ArrayElementValue targetArrayElementValue;
    private final boolean           replaceElementValues;
    public ElementValuesEditor(ProgramClass targetClass,
                               Annotation   targetAnnotation,
                               boolean      replaceElementValues)
    {
        this.targetClass             = targetClass;
        this.targetAnnotation        = targetAnnotation;
        this.targetArrayElementValue = null;
        this.replaceElementValues    = replaceElementValues;
    }
    public ElementValuesEditor(ProgramClass      targetClass,
                               ArrayElementValue targetArrayElementValue,
                               boolean           replaceElementValues)
    {
        this.targetClass             = targetClass;
        this.targetAnnotation        = null;
        this.targetArrayElementValue = targetArrayElementValue;
        this.replaceElementValues    = replaceElementValues;
    }
    public void addElementValue(ElementValue elementValue)
    {
        if (targetAnnotation != null)
        {
            if (!replaceElementValues ||
                !replaceElementValue(targetAnnotation.u2elementValuesCount,
                                     targetAnnotation.elementValues,
                                     elementValue))
            {
                targetAnnotation.elementValues =
                    addElementValue(targetAnnotation.u2elementValuesCount,
                                    targetAnnotation.elementValues,
                                    elementValue);
                targetAnnotation.u2elementValuesCount++;
            }
        }
        else
        {
            if (!replaceElementValues ||
                !replaceElementValue(targetArrayElementValue.u2elementValuesCount,
                                     targetArrayElementValue.elementValues,
                                     elementValue))
            {
                targetArrayElementValue.elementValues =
                    addElementValue(targetArrayElementValue.u2elementValuesCount,
                                    targetArrayElementValue.elementValues,
                                    elementValue);
                targetArrayElementValue.u2elementValuesCount++;
            }
        }
    }
    public void deleteElementValue(String elementValueMethodName)
    {
        if (targetAnnotation != null)
        {
            targetAnnotation.u2elementValuesCount =
                deleteElementValue(targetAnnotation.u2elementValuesCount,
                                   targetAnnotation.elementValues,
                                   elementValueMethodName);
        }
        else
        {
            targetArrayElementValue.u2elementValuesCount =
                deleteElementValue(targetArrayElementValue.u2elementValuesCount,
                                   targetArrayElementValue.elementValues,
                                   elementValueMethodName);
        }
    }
    private boolean replaceElementValue(int            elementValuesCount,
                                        ElementValue[] elementValues,
                                        ElementValue   elementValue)
    {
        int index = findElementValue(elementValuesCount,
                                     elementValues,
                                     elementValue.getMethodName(targetClass));
        if (index < 0)
        {
            return false;
        }
        elementValues[index] = elementValue;
        return true;
    }
    private ElementValue[] addElementValue(int            elementValuesCount,
                                           ElementValue[] elementValues,
                                           ElementValue   elementValue)
    {
        if (elementValues.length <= elementValuesCount)
        {
            ElementValue[] newElementValues = new ElementValue[elementValuesCount + 1];
            System.arraycopy(elementValues, 0,
                             newElementValues, 0,
                             elementValuesCount);
            elementValues = newElementValues;
        }
        elementValues[elementValuesCount] = elementValue;
        return elementValues;
    }
    private int deleteElementValue(int            elementValuesCount,
                                   ElementValue[] elementValues,
                                   String         elementValueMethodName)
    {
        int index = findElementValue(elementValuesCount,
                                     elementValues,
                                     elementValueMethodName);
        if (index < 0)
        {
            return elementValuesCount;
        }
        System.arraycopy(elementValues, index + 1,
                         elementValues, index,
                         elementValuesCount - index - 1);
        elementValues[--elementValuesCount] = null;
        return elementValuesCount;
    }
    private int findElementValue(int            elementValuesCount,
                                 ElementValue[] elementValues,
                                 String         elementValueName)
    {
        for (int index = 0; index < elementValuesCount; index++)
        {
            if (elementValues[index].getMethodName(targetClass).equals(elementValueName))
            {
                return index;
            }
        }
        return -1;
    }
}