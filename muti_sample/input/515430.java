package proguard.classfile.attribute.preverification;
import proguard.classfile.*;
import proguard.classfile.attribute.CodeAttribute;
import proguard.classfile.attribute.preverification.visitor.VerificationTypeVisitor;
public abstract class VerificationType implements VisitorAccepter
{
    public static final int TOP_TYPE                = 0;
    public static final int INTEGER_TYPE            = 1;
    public static final int FLOAT_TYPE              = 2;
    public static final int DOUBLE_TYPE             = 3;
    public static final int LONG_TYPE               = 4;
    public static final int NULL_TYPE               = 5;
    public static final int UNINITIALIZED_THIS_TYPE = 6;
    public static final int OBJECT_TYPE             = 7;
    public static final int UNINITIALIZED_TYPE      = 8;
    public Object visitorInfo;
    public abstract int getTag();
    public abstract void accept(Clazz clazz, Method method, CodeAttribute codeAttribute, int instructionOffset, VerificationTypeVisitor verificationTypeVisitor);
    public abstract void stackAccept(Clazz clazz, Method method, CodeAttribute codeAttribute, int instructionOffset, int stackIndex, VerificationTypeVisitor verificationTypeVisitor);
    public abstract void variablesAccept(Clazz clazz, Method method, CodeAttribute codeAttribute, int instructionOffset, int variableIndex, VerificationTypeVisitor verificationTypeVisitor);
    public Object getVisitorInfo()
    {
        return visitorInfo;
    }
    public void setVisitorInfo(Object visitorInfo)
    {
        this.visitorInfo = visitorInfo;
    }
    public boolean equals(Object object)
    {
        return object != null &&
               this.getClass() == object.getClass();
    }
    public int hashCode()
    {
        return this.getClass().hashCode();
    }
}
