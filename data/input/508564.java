package proguard.classfile.editor;
import proguard.classfile.*;
import proguard.classfile.constant.*;
import proguard.classfile.constant.visitor.ConstantVisitor;
public class ConstantAdder
implements   ConstantVisitor
{
    private final ConstantPoolEditor constantPoolEditor;
    private int constantIndex;
    public ConstantAdder(ProgramClass targetClass)
    {
        constantPoolEditor = new ConstantPoolEditor(targetClass);
    }
    public int addConstant(Clazz clazz, int constantIndex)
    {
        clazz.constantPoolEntryAccept(constantIndex, this);
        return this.constantIndex;
    }
    public int addConstant(Clazz clazz, Constant constant)
    {
        constant.accept(clazz, this);
        return this.constantIndex;
    }
    public int getConstantIndex()
    {
        return constantIndex;
    }
    public void visitIntegerConstant(Clazz clazz, IntegerConstant integerConstant)
    {
        constantIndex =
            constantPoolEditor.addIntegerConstant(integerConstant.getValue());
    }
    public void visitLongConstant(Clazz clazz, LongConstant longConstant)
    {
        constantIndex =
            constantPoolEditor.addLongConstant(longConstant.getValue());
    }
    public void visitFloatConstant(Clazz clazz, FloatConstant floatConstant)
    {
        constantIndex =
            constantPoolEditor.addFloatConstant(floatConstant.getValue());
    }
    public void visitDoubleConstant(Clazz clazz, DoubleConstant doubleConstant)
    {
        constantIndex =
            constantPoolEditor.addDoubleConstant(doubleConstant.getValue());
    }
    public void visitStringConstant(Clazz clazz, StringConstant stringConstant)
    {
        constantIndex =
            constantPoolEditor.addStringConstant(stringConstant.getString(clazz),
                                                 stringConstant.referencedClass,
                                                 stringConstant.referencedMember);
    }
    public void visitUtf8Constant(Clazz clazz, Utf8Constant utf8Constant)
    {
        constantIndex =
            constantPoolEditor.addUtf8Constant(utf8Constant.getString());
    }
    public void visitFieldrefConstant(Clazz clazz, FieldrefConstant fieldrefConstant)
    {
        clazz.constantPoolEntryAccept(fieldrefConstant.u2classIndex, this);
        constantIndex =
            constantPoolEditor.addFieldrefConstant(constantIndex,
                                                   fieldrefConstant.getName(clazz),
                                                   fieldrefConstant.getType(clazz),
                                                   fieldrefConstant.referencedClass,
                                                   fieldrefConstant.referencedMember);
    }
    public void visitInterfaceMethodrefConstant(Clazz clazz, InterfaceMethodrefConstant interfaceMethodrefConstant)
    {
        clazz.constantPoolEntryAccept(interfaceMethodrefConstant.u2classIndex, this);
        constantIndex =
            constantPoolEditor.addInterfaceMethodrefConstant(constantIndex,
                                                             interfaceMethodrefConstant.getName(clazz),
                                                             interfaceMethodrefConstant.getType(clazz),
                                                             interfaceMethodrefConstant.referencedClass,
                                                             interfaceMethodrefConstant.referencedMember);
    }
    public void visitMethodrefConstant(Clazz clazz, MethodrefConstant methodrefConstant)
    {
        clazz.constantPoolEntryAccept(methodrefConstant.u2classIndex, this);
        constantIndex =
            constantPoolEditor.addMethodrefConstant(constantIndex,
                                                    methodrefConstant.getName(clazz),
                                                    methodrefConstant.getType(clazz),
                                                    methodrefConstant.referencedClass,
                                                    methodrefConstant.referencedMember);
    }
    public void visitClassConstant(Clazz clazz, ClassConstant classConstant)
    {
        constantIndex =
            constantPoolEditor.addClassConstant(classConstant.getName(clazz),
                                                classConstant.referencedClass);
    }
    public void visitNameAndTypeConstant(Clazz clazz, NameAndTypeConstant nameAndTypeConstant)
    {
        constantIndex =
            constantPoolEditor.addNameAndTypeConstant(nameAndTypeConstant.getName(clazz),
                                                      nameAndTypeConstant.getType(clazz));
    }
}
