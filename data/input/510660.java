package proguard.classfile.attribute.preverification;
import proguard.classfile.*;
import proguard.classfile.attribute.CodeAttribute;
import proguard.classfile.attribute.preverification.visitor.VerificationTypeVisitor;
public class FloatType extends VerificationType
{
    public int getTag()
    {
        return FLOAT_TYPE;
    }
    public void accept(Clazz clazz, Method method, CodeAttribute codeAttribute, int instructionOffset, VerificationTypeVisitor verificationTypeVisitor)
    {
        verificationTypeVisitor.visitFloatType(clazz, method, codeAttribute, instructionOffset, this);
    }
    public void stackAccept(Clazz clazz, Method method, CodeAttribute codeAttribute, int instructionOffset, int stackIndex, VerificationTypeVisitor verificationTypeVisitor)
    {
        verificationTypeVisitor.visitStackFloatType(clazz, method, codeAttribute, instructionOffset, stackIndex, this);
    }
    public void variablesAccept(Clazz clazz, Method method, CodeAttribute codeAttribute, int instructionOffset, int variableIndex, VerificationTypeVisitor verificationTypeVisitor)
    {
        verificationTypeVisitor.visitVariablesFloatType(clazz, method, codeAttribute, instructionOffset, variableIndex, this);
    }
    public String toString()
    {
        return "f";
    }
}
