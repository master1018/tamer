package proguard.classfile.attribute;
import proguard.classfile.*;
import proguard.classfile.attribute.visitor.*;
public class LocalVariableTableAttribute extends Attribute
{
    public int                 u2localVariableTableLength;
    public LocalVariableInfo[] localVariableTable;
    public LocalVariableTableAttribute()
    {
    }
    public LocalVariableTableAttribute(int                 u2attributeNameIndex,
                                       int                 u2localVariableTableLength,
                                       LocalVariableInfo[] localVariableTable)
    {
        super(u2attributeNameIndex);
        this.u2localVariableTableLength = u2localVariableTableLength;
        this.localVariableTable         = localVariableTable;
    }
    public void accept(Clazz clazz, Method method, CodeAttribute codeAttribute, AttributeVisitor attributeVisitor)
    {
        attributeVisitor.visitLocalVariableTableAttribute(clazz, method, codeAttribute, this);
    }
    public void localVariablesAccept(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableInfoVisitor localVariableInfoVisitor)
    {
        for (int index = 0; index < u2localVariableTableLength; index++)
        {
            localVariableInfoVisitor.visitLocalVariableInfo(clazz, method, codeAttribute, localVariableTable[index]);
        }
    }
}
