package proguard.classfile.editor;
import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.annotation.*;
import proguard.classfile.attribute.preverification.*;
import proguard.classfile.constant.ClassConstant;
import proguard.classfile.constant.visitor.ConstantVisitor;
import proguard.classfile.util.SimplifiedVisitor;
public class InterfaceAdder
extends      SimplifiedVisitor
implements   ConstantVisitor
{
    private final ConstantAdder    constantAdder;
    private final InterfacesEditor interfacesEditor;
    public InterfaceAdder(ProgramClass targetClass)
    {
        constantAdder    = new ConstantAdder(targetClass);
        interfacesEditor = new InterfacesEditor(targetClass);
    }
    public void visitClassConstant(Clazz clazz, ClassConstant classConstant)
    {
        interfacesEditor.addInterface(constantAdder.addConstant(clazz, classConstant));
    }
}