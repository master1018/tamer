package proguard.classfile.attribute;
import proguard.classfile.Clazz;
import proguard.classfile.visitor.ClassVisitor;
public class LocalVariableTypeInfo
{
    public int u2startPC;
    public int u2length;
    public int u2nameIndex;
    public int u2signatureIndex;
    public int u2index;
    public Clazz[] referencedClasses;
    public LocalVariableTypeInfo()
    {
    }
    public LocalVariableTypeInfo(int   u2startPC,
                                 int   u2length,
                                 int   u2nameIndex,
                                 int   u2signatureIndex,
                                 int   u2index)
    {
        this.u2startPC        = u2startPC;
        this.u2length         = u2length;
        this.u2nameIndex      = u2nameIndex;
        this.u2signatureIndex = u2signatureIndex;
        this.u2index          = u2index;
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
}
