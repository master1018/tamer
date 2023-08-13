package proguard.classfile.instruction.visitor;
import proguard.classfile.*;
import proguard.classfile.attribute.CodeAttribute;
import proguard.classfile.instruction.*;
public interface InstructionVisitor
{
    public void visitSimpleInstruction(      Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, SimpleInstruction       simpleInstruction);
    public void visitVariableInstruction(    Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, VariableInstruction     variableInstruction);
    public void visitConstantInstruction(    Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, ConstantInstruction     constantInstruction);
    public void visitBranchInstruction(      Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, BranchInstruction       branchInstruction);
    public void visitTableSwitchInstruction( Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, TableSwitchInstruction  tableSwitchInstruction);
    public void visitLookUpSwitchInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, LookUpSwitchInstruction lookUpSwitchInstruction);
}
