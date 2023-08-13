package proguard.classfile.attribute;
import proguard.classfile.*;
import proguard.classfile.attribute.visitor.AttributeVisitor;
import proguard.classfile.constant.visitor.ConstantVisitor;
public class ExceptionsAttribute extends Attribute
{
    public int   u2exceptionIndexTableLength;
    public int[] u2exceptionIndexTable;
    public ExceptionsAttribute()
    {
    }
    public ExceptionsAttribute(int   u2attributeNameIndex,
                               int   u2exceptionIndexTableLength,
                               int[] u2exceptionIndexTable)
    {
        super(u2attributeNameIndex);
        this.u2exceptionIndexTableLength = u2exceptionIndexTableLength;
        this.u2exceptionIndexTable       = u2exceptionIndexTable;
    }
    public void accept(Clazz clazz, Method method, AttributeVisitor attributeVisitor)
    {
        attributeVisitor.visitExceptionsAttribute(clazz, method, this);
    }
    public void exceptionEntriesAccept(ProgramClass programClass, ConstantVisitor constantVisitor)
    {
        for (int index = 0; index < u2exceptionIndexTableLength; index++)
        {
            programClass.constantPoolEntryAccept(u2exceptionIndexTable[index],
                                                 constantVisitor);
        }
    }
}
