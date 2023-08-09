package proguard.classfile.editor;
import proguard.classfile.*;
import proguard.classfile.attribute.ExceptionsAttribute;
import proguard.classfile.constant.ClassConstant;
import proguard.classfile.constant.visitor.ConstantVisitor;
import proguard.classfile.util.SimplifiedVisitor;
public class ExceptionAdder
extends      SimplifiedVisitor
implements   ConstantVisitor
{
    private final ConstantAdder             constantAdder;
    private final ExceptionsAttributeEditor exceptionsAttributeEditor;
    public ExceptionAdder(ProgramClass        targetClass,
                          ExceptionsAttribute targetExceptionsAttribute)
    {
        constantAdder             = new ConstantAdder(targetClass);
        exceptionsAttributeEditor = new ExceptionsAttributeEditor(targetExceptionsAttribute);
    }
    public void visitClassConstant(Clazz clazz, ClassConstant classConstant)
    {
        constantAdder.visitClassConstant(clazz, classConstant);
        exceptionsAttributeEditor.addException(constantAdder.getConstantIndex());
    }
}
