package proguard.classfile.instruction.visitor;
import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.visitor.AttributeVisitor;
import proguard.classfile.util.SimplifiedVisitor;
public class AllInstructionVisitor
extends      SimplifiedVisitor
implements   AttributeVisitor
{
    private final InstructionVisitor instructionVisitor;
    public AllInstructionVisitor(InstructionVisitor instructionVisitor)
    {
        this.instructionVisitor = instructionVisitor;
    }
    public void visitAnyAttribute(Clazz clazz, Attribute attribute) {}
    public void visitCodeAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute)
    {
        codeAttribute.instructionsAccept(clazz, method, instructionVisitor);
    }
}
