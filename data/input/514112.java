import proguard.classfile.*;
import proguard.classfile.attribute.CodeAttribute;
import proguard.classfile.editor.CodeAttributeEditor;
import proguard.classfile.instruction.*;
import proguard.classfile.instruction.visitor.InstructionVisitor;
import proguard.classfile.util.SimplifiedVisitor;
public class NopRemover
extends      SimplifiedVisitor
implements   InstructionVisitor
{
    private final CodeAttributeEditor codeAttributeEditor;
    private final InstructionVisitor  extraInstructionVisitor;
    public NopRemover(CodeAttributeEditor codeAttributeEditor)
    {
        this(codeAttributeEditor, null);
    }
    public NopRemover(CodeAttributeEditor codeAttributeEditor,
                      InstructionVisitor  extraInstructionVisitor)
    {
        this.codeAttributeEditor     = codeAttributeEditor;
        this.extraInstructionVisitor = extraInstructionVisitor;
    }
    public void visitAnyInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, Instruction instruction) {}
    public void visitSimpleInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, SimpleInstruction simpleInstruction)
    {
        if (simpleInstruction.opcode == InstructionConstants.OP_NOP &&
            !codeAttributeEditor.isModified(offset))
        {
            codeAttributeEditor.deleteInstruction(offset);
            if (extraInstructionVisitor != null)
            {
                extraInstructionVisitor.visitSimpleInstruction(clazz, method, codeAttribute, offset, simpleInstruction);
            }
        }
    }
}
