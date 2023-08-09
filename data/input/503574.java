package proguard.classfile.attribute;
import proguard.classfile.*;
import proguard.classfile.attribute.visitor.AttributeVisitor;
import proguard.classfile.visitor.ClassVisitor;
public class SignatureAttribute extends Attribute
{
    public int u2signatureIndex;
    public Clazz[] referencedClasses;
    public SignatureAttribute()
    {
    }
    public SignatureAttribute(int u2attributeNameIndex,
                              int u2signatureIndex)
    {
        super(u2attributeNameIndex);
        this.u2signatureIndex = u2signatureIndex;
    }
    public void referencedClassesAccept(ClassVisitor classVisitor)
    {
        if (referencedClasses != null)
        {
            for (int index = 0; index < referencedClasses.length; index++)
            {
                if (referencedClasses[index] != null)
                {
                    referencedClasses[index].accept(classVisitor);
                }
            }
        }
    }
    public void accept(Clazz clazz, AttributeVisitor attributeVisitor)
    {
        attributeVisitor.visitSignatureAttribute(clazz, this);
    }
    public void accept(Clazz clazz, Field field, AttributeVisitor attributeVisitor)
    {
        attributeVisitor.visitSignatureAttribute(clazz, field, this);
    }
    public void accept(Clazz clazz, Method method, AttributeVisitor attributeVisitor)
    {
        attributeVisitor.visitSignatureAttribute(clazz, method, this);
    }
 }
