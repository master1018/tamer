package proguard.classfile.instruction.visitor;
import proguard.classfile.*;
import proguard.classfile.attribute.CodeAttribute;
import proguard.classfile.instruction.*;
public class MultiInstructionVisitor implements InstructionVisitor
{
    private static final int ARRAY_SIZE_INCREMENT = 5;
    private InstructionVisitor[] instructionVisitors;
    private int                  instructionVisitorCount;
    public MultiInstructionVisitor()
    {
    }
    public MultiInstructionVisitor(InstructionVisitor[] instructionVisitors)
    {
        this.instructionVisitors     = instructionVisitors;
        this.instructionVisitorCount = instructionVisitors.length;
    }
    public void addInstructionVisitor(InstructionVisitor instructionVisitor)
    {
        ensureArraySize();
        instructionVisitors[instructionVisitorCount++] = instructionVisitor;
    }
    private void ensureArraySize()
    {
        if (instructionVisitors == null)
        {
            instructionVisitors = new InstructionVisitor[ARRAY_SIZE_INCREMENT];
        }
        else if (instructionVisitors.length == instructionVisitorCount)
        {
            InstructionVisitor[] newInstructionVisitors =
                new InstructionVisitor[instructionVisitorCount +
                                     ARRAY_SIZE_INCREMENT];
            System.arraycopy(instructionVisitors, 0,
                             newInstructionVisitors, 0,
                             instructionVisitorCount);
            instructionVisitors = newInstructionVisitors;
        }
    }
    public void visitSimpleInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, SimpleInstruction simpleInstruction)
    {
        for (int index = 0; index < instructionVisitorCount; index++)
        {
            instructionVisitors[index].visitSimpleInstruction(clazz, method, codeAttribute, offset, simpleInstruction);
        }
    }
    public void visitVariableInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, VariableInstruction variableInstruction)
    {
        for (int index = 0; index < instructionVisitorCount; index++)
        {
            instructionVisitors[index].visitVariableInstruction(clazz, method, codeAttribute, offset, variableInstruction);
        }
    }
    public void visitConstantInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, ConstantInstruction constantInstruction)
    {
        for (int index = 0; index < instructionVisitorCount; index++)
        {
            instructionVisitors[index].visitConstantInstruction(clazz, method, codeAttribute, offset, constantInstruction);
        }
    }
    public void visitBranchInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, BranchInstruction branchInstruction)
    {
        for (int index = 0; index < instructionVisitorCount; index++)
        {
            instructionVisitors[index].visitBranchInstruction(clazz, method, codeAttribute, offset, branchInstruction);
        }
    }
    public void visitTableSwitchInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, TableSwitchInstruction tableSwitchInstruction)
    {
        for (int index = 0; index < instructionVisitorCount; index++)
        {
            instructionVisitors[index].visitTableSwitchInstruction(clazz, method, codeAttribute, offset, tableSwitchInstruction);
        }
    }
    public void visitLookUpSwitchInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, LookUpSwitchInstruction lookUpSwitchInstruction)
    {
        for (int index = 0; index < instructionVisitorCount; index++)
        {
            instructionVisitors[index].visitLookUpSwitchInstruction(clazz, method, codeAttribute, offset, lookUpSwitchInstruction);
        }
    }
}
