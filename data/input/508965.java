import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.visitor.AttributeVisitor;
import proguard.classfile.instruction.*;
import proguard.classfile.instruction.visitor.InstructionVisitor;
import proguard.classfile.util.SimplifiedVisitor;
public class VariableUsageMarker
extends      SimplifiedVisitor
implements   AttributeVisitor,
             InstructionVisitor
{
    private boolean[] variableUsed = new boolean[ClassConstants.TYPICAL_VARIABLES_SIZE];
    public boolean isVariableUsed(int variableIndex)
    {
        return variableUsed[variableIndex];
    }
    public void visitAnyAttribute(Clazz clazz, Attribute attribute) {}
    public void visitCodeAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute)
    {
        int maxLocals = codeAttribute.u2maxLocals;
        if (variableUsed.length < maxLocals)
        {
            variableUsed = new boolean[maxLocals];
        }
        else
        {
            for (int index = 0; index < maxLocals; index++)
            {
                variableUsed[index] = false;
            }
        }
        codeAttribute.instructionsAccept(clazz, method, this);
    }
    public void visitAnyInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, Instruction instruction) {}
    public void visitVariableInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, VariableInstruction variableInstruction)
    {
        variableUsed[variableInstruction.variableIndex] = true;
        if (variableInstruction.isCategory2())
        {
            variableUsed[variableInstruction.variableIndex + 1] = true;
        }
    }
}
