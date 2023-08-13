package proguard.classfile.editor;
import proguard.classfile.attribute.*;
public class LocalVariableTypeTableAttributeEditor
{
    private LocalVariableTypeTableAttribute targetLocalVariableTypeTableAttribute;
    public LocalVariableTypeTableAttributeEditor(LocalVariableTypeTableAttribute targetLocalVariableTypeTableAttribute)
    {
        this.targetLocalVariableTypeTableAttribute = targetLocalVariableTypeTableAttribute;
    }
    public void addLocalVariableTypeInfo(LocalVariableTypeInfo localVariableTypeInfo)
    {
        int                     localVariableTypeTableLength = targetLocalVariableTypeTableAttribute.u2localVariableTypeTableLength;
        LocalVariableTypeInfo[] localVariableTypeTable       = targetLocalVariableTypeTableAttribute.localVariableTypeTable;
        if (localVariableTypeTable.length <= localVariableTypeTableLength)
        {
            targetLocalVariableTypeTableAttribute.localVariableTypeTable = new LocalVariableTypeInfo[localVariableTypeTableLength+1];
            System.arraycopy(localVariableTypeTable, 0,
                             targetLocalVariableTypeTableAttribute.localVariableTypeTable, 0,
                             localVariableTypeTableLength);
            localVariableTypeTable = targetLocalVariableTypeTableAttribute.localVariableTypeTable;
        }
        localVariableTypeTable[targetLocalVariableTypeTableAttribute.u2localVariableTypeTableLength++] = localVariableTypeInfo;
    }
}