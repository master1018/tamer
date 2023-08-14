package proguard.classfile.attribute;
import proguard.classfile.*;
import proguard.classfile.attribute.visitor.*;
public class LocalVariableTypeTableAttribute extends Attribute
{
    public int                     u2localVariableTypeTableLength;
    public LocalVariableTypeInfo[] localVariableTypeTable;
    public LocalVariableTypeTableAttribute()
    {
    }
    public LocalVariableTypeTableAttribute(int                     u2attributeNameIndex,
                                           int                     u2localVariableTypeTableLength,
                                           LocalVariableTypeInfo[] localVariableTypeTable)
    {
        super(u2attributeNameIndex);
        this.u2localVariableTypeTableLength = u2localVariableTypeTableLength;
        this.localVariableTypeTable         = localVariableTypeTable;
    }
    public void accept(Clazz clazz, Method method, CodeAttribute codeAttribute, AttributeVisitor attributeVisitor)
    {
        attributeVisitor.visitLocalVariableTypeTableAttribute(clazz, method, codeAttribute, this);
    }
    public void localVariablesAccept(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableTypeInfoVisitor localVariableTypeInfoVisitor)
    {
        for (int index = 0; index < u2localVariableTypeTableLength; index++)
        {
            localVariableTypeInfoVisitor.visitLocalVariableTypeInfo(clazz, method, codeAttribute, localVariableTypeTable[index]);
        }
    }
}
