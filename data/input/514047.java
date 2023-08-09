package proguard.classfile.attribute.preverification;
import proguard.classfile.*;
import proguard.classfile.attribute.CodeAttribute;
import proguard.classfile.attribute.preverification.visitor.VerificationTypeVisitor;
public class UninitializedType extends VerificationType
{
    public int u2newInstructionOffset;
    public UninitializedType()
    {
    }
    public UninitializedType(int u2newInstructionOffset)
    {
        this.u2newInstructionOffset = u2newInstructionOffset;
    }
    public int getTag()
    {
        return UNINITIALIZED_TYPE;
    }
    public void accept(Clazz clazz, Method method, CodeAttribute codeAttribute, int instructionOffset, VerificationTypeVisitor verificationTypeVisitor)
    {
        verificationTypeVisitor.visitUninitializedType(clazz, method, codeAttribute, instructionOffset, this);
    }
    public void stackAccept(Clazz clazz, Method method, CodeAttribute codeAttribute, int instructionOffset, int stackIndex, VerificationTypeVisitor verificationTypeVisitor)
    {
        verificationTypeVisitor.visitStackUninitializedType(clazz, method, codeAttribute, instructionOffset, stackIndex, this);
    }
    public void variablesAccept(Clazz clazz, Method method, CodeAttribute codeAttribute, int instructionOffset, int variableIndex, VerificationTypeVisitor verificationTypeVisitor)
    {
        verificationTypeVisitor.visitVariablesUninitializedType(clazz, method, codeAttribute, instructionOffset, variableIndex, this);
    }
    public boolean equals(Object object)
    {
        if (!super.equals(object))
        {
            return false;
        }
        UninitializedType other = (UninitializedType)object;
        return this.u2newInstructionOffset == other.u2newInstructionOffset;
    }
    public int hashCode()
    {
        return super.hashCode() ^
               u2newInstructionOffset;
    }
    public String toString()
    {
        return "u:" + u2newInstructionOffset;
    }
}
