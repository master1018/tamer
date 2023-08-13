package proguard.classfile.editor;
import proguard.classfile.attribute.*;
public class LocalVariableTableAttributeEditor
{
    private LocalVariableTableAttribute targetLocalVariableTableAttribute;
    public LocalVariableTableAttributeEditor(LocalVariableTableAttribute targetLocalVariableTableAttribute)
    {
        this.targetLocalVariableTableAttribute = targetLocalVariableTableAttribute;
    }
    public void addLocalVariableInfo(LocalVariableInfo localVariableInfo)
    {
        int                 localVariableTableLength = targetLocalVariableTableAttribute.u2localVariableTableLength;
        LocalVariableInfo[] localVariableTable       = targetLocalVariableTableAttribute.localVariableTable;
        if (localVariableTable.length <= localVariableTableLength)
        {
            targetLocalVariableTableAttribute.localVariableTable = new LocalVariableInfo[localVariableTableLength+1];
            System.arraycopy(localVariableTable, 0,
                             targetLocalVariableTableAttribute.localVariableTable, 0,
                             localVariableTableLength);
            localVariableTable = targetLocalVariableTableAttribute.localVariableTable;
        }
        localVariableTable[targetLocalVariableTableAttribute.u2localVariableTableLength++] = localVariableInfo;
    }
}