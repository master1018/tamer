package proguard.classfile.instruction.visitor;
import proguard.classfile.*;
import proguard.classfile.attribute.CodeAttribute;
import proguard.classfile.instruction.Instruction;
import proguard.classfile.util.SimplifiedVisitor;
public class InstructionCounter
extends      SimplifiedVisitor
implements   InstructionVisitor
{
    private int count;
    public int getCount()
    {
        return count;
    }
    public void visitAnyInstruction(Clazz         clazz,
                                    Method        method,
                                    CodeAttribute codeAttribute,
                                    int           offset,
                                    Instruction   instruction)
    {
        count++;
    }
}
