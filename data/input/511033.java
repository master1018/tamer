import proguard.classfile.*;
import proguard.classfile.attribute.CodeAttribute;
import proguard.classfile.editor.CodeAttributeEditor;
import proguard.classfile.instruction.*;
import proguard.classfile.instruction.visitor.InstructionVisitor;
import proguard.classfile.util.SimplifiedVisitor;
public class GotoGotoReplacer
extends      SimplifiedVisitor
implements   InstructionVisitor
{
    private final CodeAttributeEditor codeAttributeEditor;
    private final InstructionVisitor  extraInstructionVisitor;
    public GotoGotoReplacer(CodeAttributeEditor codeAttributeEditor)
    {
        this(codeAttributeEditor, null);
    }
    public GotoGotoReplacer(CodeAttributeEditor codeAttributeEditor,
                            InstructionVisitor  extraInstructionVisitor)
    {
        this.codeAttributeEditor     = codeAttributeEditor;
        this.extraInstructionVisitor = extraInstructionVisitor;
    }
    public void visitAnyInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, Instruction instruction) {}
    public void visitBranchInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, BranchInstruction branchInstruction)
    {
        byte opcode = branchInstruction.opcode;
        if (opcode == InstructionConstants.OP_GOTO ||
            opcode == InstructionConstants.OP_GOTO_W)
        {
            int branchOffset = branchInstruction.branchOffset;
            int targetOffset = offset + branchOffset;
            if (branchOffset != branchInstruction.length(offset) &&
                !codeAttributeEditor.isModified(offset) &&
                !codeAttributeEditor.isModified(targetOffset))
            {
                Instruction targetInstruction =
                    InstructionFactory.create(codeAttribute.code, targetOffset);
                if (targetInstruction.opcode == InstructionConstants.OP_GOTO)
                {
                    int targetBranchOffset = ((BranchInstruction)targetInstruction).branchOffset;
                    Instruction newBranchInstruction =
                         new BranchInstruction(opcode,
                                               (branchOffset + targetBranchOffset));
                    codeAttributeEditor.replaceInstruction(offset,
                                                           newBranchInstruction);
                    if (extraInstructionVisitor != null)
                    {
                        extraInstructionVisitor.visitBranchInstruction(clazz, method, codeAttribute, offset, branchInstruction);
                    }
                }
            }
        }
    }
}
