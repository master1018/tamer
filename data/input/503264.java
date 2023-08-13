import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.visitor.AttributeVisitor;
import proguard.classfile.editor.CodeAttributeEditor;
import proguard.classfile.instruction.visitor.InstructionVisitor;
import proguard.classfile.util.SimplifiedVisitor;
public class PeepholeOptimizer
extends      SimplifiedVisitor
implements   AttributeVisitor
{
    private final BranchTargetFinder  branchTargetFinder;
    private final CodeAttributeEditor codeAttributeEditor;
    private final InstructionVisitor  instructionVisitor;
    public PeepholeOptimizer(CodeAttributeEditor codeAttributeEditor,
                             InstructionVisitor  instructionVisitor)
    {
        this(null, codeAttributeEditor, instructionVisitor);
    }
    public PeepholeOptimizer(BranchTargetFinder  branchTargetFinder,
                             CodeAttributeEditor codeAttributeEditor,
                             InstructionVisitor  instructionVisitor)
    {
        this.branchTargetFinder  = branchTargetFinder;
        this.codeAttributeEditor = codeAttributeEditor;
        this.instructionVisitor  = instructionVisitor;
    }
    public void visitAnyAttribute(Clazz clazz, Attribute attribute) {}
    public void visitCodeAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute)
    {
        if (branchTargetFinder != null)
        {
            branchTargetFinder.visitCodeAttribute(clazz, method, codeAttribute);
        }
        codeAttributeEditor.reset(codeAttribute.u4codeLength);
        codeAttribute.instructionsAccept(clazz, method, instructionVisitor);
        codeAttributeEditor.visitCodeAttribute(clazz, method, codeAttribute);
    }
}
