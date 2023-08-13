package proguard.classfile.attribute.preverification;
import proguard.classfile.*;
import proguard.classfile.attribute.CodeAttribute;
import proguard.classfile.attribute.preverification.visitor.VerificationTypeVisitor;
public class ObjectType extends VerificationType
{
    public int u2classIndex;
    public ObjectType()
    {
    }
    public ObjectType(int u2classIndex)
    {
        this.u2classIndex = u2classIndex;
    }
    public int getTag()
    {
        return OBJECT_TYPE;
    }
    public void accept(Clazz clazz, Method method, CodeAttribute codeAttribute, int instructionOffset, VerificationTypeVisitor verificationTypeVisitor)
    {
        verificationTypeVisitor.visitObjectType(clazz, method, codeAttribute, instructionOffset, this);
    }
    public void stackAccept(Clazz clazz, Method method, CodeAttribute codeAttribute, int instructionOffset, int stackIndex, VerificationTypeVisitor verificationTypeVisitor)
    {
        verificationTypeVisitor.visitStackObjectType(clazz, method, codeAttribute, instructionOffset, stackIndex, this);
    }
    public void variablesAccept(Clazz clazz, Method method, CodeAttribute codeAttribute, int instructionOffset, int variableIndex, VerificationTypeVisitor verificationTypeVisitor)
    {
        verificationTypeVisitor.visitVariablesObjectType(clazz, method, codeAttribute, instructionOffset, variableIndex, this);
    }
    public boolean equals(Object object)
    {
        if (!super.equals(object))
        {
            return false;
        }
        ObjectType other = (ObjectType)object;
        return this.u2classIndex == other.u2classIndex;
    }
    public int hashCode()
    {
        return super.hashCode() ^
               u2classIndex;
    }
    public String toString()
    {
        return "a:" + u2classIndex;
    }
}
