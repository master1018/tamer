package proguard.classfile.editor;
import proguard.classfile.attribute.*;
public class LineNumberTableAttributeEditor
{
    private LineNumberTableAttribute targetLineNumberTableAttribute;
    public LineNumberTableAttributeEditor(LineNumberTableAttribute targetLineNumberTableAttribute)
    {
        this.targetLineNumberTableAttribute = targetLineNumberTableAttribute;
    }
    public void addLineNumberInfo(LineNumberInfo lineNumberInfo)
    {
        int              lineNumberTableLength = targetLineNumberTableAttribute.u2lineNumberTableLength;
        LineNumberInfo[] lineNumberTable       = targetLineNumberTableAttribute.lineNumberTable;
        if (lineNumberTable.length <= lineNumberTableLength)
        {
            targetLineNumberTableAttribute.lineNumberTable = new LineNumberInfo[lineNumberTableLength+1];
            System.arraycopy(lineNumberTable, 0,
                             targetLineNumberTableAttribute.lineNumberTable, 0,
                             lineNumberTableLength);
            lineNumberTable = targetLineNumberTableAttribute.lineNumberTable;
        }
        lineNumberTable[targetLineNumberTableAttribute.u2lineNumberTableLength++] = lineNumberInfo;
    }
}