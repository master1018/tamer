package proguard.classfile.attribute;
import proguard.classfile.*;
import proguard.classfile.attribute.visitor.*;
public class LineNumberTableAttribute extends Attribute
{
    public int              u2lineNumberTableLength;
    public LineNumberInfo[] lineNumberTable;
    public LineNumberTableAttribute()
    {
    }
    public LineNumberTableAttribute(int              u2attributeNameIndex,
                                    int              u2lineNumberTableLength,
                                    LineNumberInfo[] lineNumberTable)
    {
        super(u2attributeNameIndex);
        this.u2lineNumberTableLength = u2lineNumberTableLength;
        this.lineNumberTable         = lineNumberTable;
    }
    public int getLineNumber(int pc)
    {
        for (int index = u2lineNumberTableLength-1 ; index >= 0 ; index--)
        {
            LineNumberInfo info = lineNumberTable[index];
            if (pc >= info.u2startPC)
            {
                return info.u2lineNumber;
            }
        }
        return u2lineNumberTableLength > 0 ?
            lineNumberTable[0].u2lineNumber :
            0;
    }
    public void accept(Clazz clazz, Method method, CodeAttribute codeAttribute, AttributeVisitor attributeVisitor)
    {
        attributeVisitor.visitLineNumberTableAttribute(clazz, method, codeAttribute, this);
    }
    public void lineNumbersAccept(Clazz clazz, Method method, CodeAttribute codeAttribute, LineNumberInfoVisitor lineNumberInfoVisitor)
    {
        for (int index = 0; index < u2lineNumberTableLength; index++)
        {
            lineNumberInfoVisitor.visitLineNumberInfo(clazz, method, codeAttribute, lineNumberTable[index]);
        }
    }
}
