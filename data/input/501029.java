package proguard.classfile.attribute.annotation;
import proguard.classfile.*;
import proguard.classfile.attribute.annotation.visitor.ElementValueVisitor;
import proguard.classfile.visitor.ClassVisitor;
public class EnumConstantElementValue extends ElementValue
{
    public int u2typeNameIndex;
    public int u2constantNameIndex;
    public Clazz[] referencedClasses;
    public EnumConstantElementValue()
    {
    }
    public EnumConstantElementValue(int u2elementNameIndex,
                                    int u2typeNameIndex,
                                    int u2constantNameIndex)
    {
        super(u2elementNameIndex);
        this.u2typeNameIndex     = u2typeNameIndex;
        this.u2constantNameIndex = u2constantNameIndex;
    }
    public void referencedClassesAccept(ClassVisitor classVisitor)
    {
        if (referencedClasses != null)
        {
            for (int index = 0; index < referencedClasses.length; index++)
            {
                Clazz referencedClass = referencedClasses[index];
                if (referencedClass != null)
                {
                    referencedClass.accept(classVisitor);
                }
            }
        }
    }
    public int getTag()
    {
        return ClassConstants.ELEMENT_VALUE_ENUM_CONSTANT;
    }
    public void accept(Clazz clazz, Annotation annotation, ElementValueVisitor elementValueVisitor)
    {
        elementValueVisitor.visitEnumConstantElementValue(clazz, annotation, this);
    }
}
