package proguard.classfile.editor;
import proguard.classfile.attribute.ExceptionsAttribute;
public class ExceptionsAttributeEditor
{
    private ExceptionsAttribute targetExceptionsAttribute;
    public ExceptionsAttributeEditor(ExceptionsAttribute targetExceptionsAttribute)
    {
        this.targetExceptionsAttribute = targetExceptionsAttribute;
    }
    public void addException(int exceptionIndex)
    {
        int   exceptionIndexTableLength = targetExceptionsAttribute.u2exceptionIndexTableLength;
        int[] exceptionIndexTable       = targetExceptionsAttribute.u2exceptionIndexTable;
        if (exceptionIndexTable.length <= exceptionIndexTableLength)
        {
            targetExceptionsAttribute.u2exceptionIndexTable = new int[exceptionIndexTableLength+1];
            System.arraycopy(exceptionIndexTable, 0,
                             targetExceptionsAttribute.u2exceptionIndexTable, 0,
                             exceptionIndexTableLength);
            exceptionIndexTable = targetExceptionsAttribute.u2exceptionIndexTable;
        }
        exceptionIndexTable[targetExceptionsAttribute.u2exceptionIndexTableLength++] = exceptionIndex;
    }
}
