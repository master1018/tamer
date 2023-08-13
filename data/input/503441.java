package proguard.classfile.constant.visitor;
import proguard.classfile.Clazz;
import proguard.classfile.constant.*;
public interface ConstantVisitor
{
    public void visitIntegerConstant(           Clazz clazz, IntegerConstant            integerConstant);
    public void visitLongConstant(              Clazz clazz, LongConstant               longConstant);
    public void visitFloatConstant(             Clazz clazz, FloatConstant              floatConstant);
    public void visitDoubleConstant(            Clazz clazz, DoubleConstant             doubleConstant);
    public void visitStringConstant(            Clazz clazz, StringConstant             stringConstant);
    public void visitUtf8Constant(              Clazz clazz, Utf8Constant               utf8Constant);
    public void visitFieldrefConstant(          Clazz clazz, FieldrefConstant           fieldrefConstant);
    public void visitInterfaceMethodrefConstant(Clazz clazz, InterfaceMethodrefConstant interfaceMethodrefConstant);
    public void visitMethodrefConstant(         Clazz clazz, MethodrefConstant          methodrefConstant);
    public void visitClassConstant(             Clazz clazz, ClassConstant              classConstant);
    public void visitNameAndTypeConstant(       Clazz clazz, NameAndTypeConstant        nameAndTypeConstant);
}
