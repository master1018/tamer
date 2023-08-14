package proguard.classfile.editor;
import proguard.classfile.attribute.visitor.LineNumberInfoVisitor;
import proguard.classfile.attribute.*;
import proguard.classfile.*;
public class LineNumberInfoAdder
implements   LineNumberInfoVisitor
{
    private final LineNumberTableAttributeEditor lineNumberTableAttributeEditor;
    public LineNumberInfoAdder(LineNumberTableAttribute targetLineNumberTableAttribute)
    {
        this.lineNumberTableAttributeEditor = new LineNumberTableAttributeEditor(targetLineNumberTableAttribute);
    }
    public void visitLineNumberInfo(Clazz clazz, Method method, CodeAttribute codeAttribute, LineNumberInfo lineNumberInfo)
    {
        LineNumberInfo newLineNumberInfo =
            new LineNumberInfo(lineNumberInfo.u2startPC,
                               lineNumberInfo.u2lineNumber);
        lineNumberTableAttributeEditor.addLineNumberInfo(newLineNumberInfo);
    }
}
