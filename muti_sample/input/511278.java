package proguard.classfile.attribute.annotation;
import proguard.classfile.*;
import proguard.classfile.attribute.annotation.visitor.ElementValueVisitor;
import proguard.classfile.visitor.ClassVisitor;
public class ClassElementValue extends ElementValue
{
    public int u2classInfoIndex;
    public Clazz[] referencedClasses;
    public ClassElementValue()
    {
    }
    public ClassElementValue(int u2elementNameIndex,
                             int u2classInfoIndex)
    {
        super(u2elementNameIndex);
        this.u2classInfoIndex = u2classInfoIndex;
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
        return ClassConstants.ELEMENT_VALUE_CLASS;
    }
    public void accept(Clazz clazz, Annotation annotation, ElementValueVisitor elementValueVisitor)
    {
        elementValueVisitor.visitClassElementValue(clazz, annotation, this);
    }
}
