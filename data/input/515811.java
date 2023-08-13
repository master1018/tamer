package proguard.classfile.editor;
import proguard.classfile.attribute.visitor.LocalVariableInfoVisitor;
import proguard.classfile.attribute.*;
import proguard.classfile.*;
public class LocalVariableInfoAdder
implements   LocalVariableInfoVisitor
{
    private final ConstantAdder                     constantAdder;
    private final LocalVariableTableAttributeEditor localVariableTableAttributeEditor;
    public LocalVariableInfoAdder(ProgramClass                targetClass,
                                  LocalVariableTableAttribute targetLocalVariableTableAttribute)
    {
        this.constantAdder                     = new ConstantAdder(targetClass);
        this.localVariableTableAttributeEditor = new LocalVariableTableAttributeEditor(targetLocalVariableTableAttribute);
    }
    public void visitLocalVariableInfo(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableInfo localVariableInfo)
    {
        LocalVariableInfo newLocalVariableInfo =
            new LocalVariableInfo(localVariableInfo.u2startPC,
                                  localVariableInfo.u2length,
                                  constantAdder.addConstant(clazz, localVariableInfo.u2nameIndex),
                                  constantAdder.addConstant(clazz, localVariableInfo.u2descriptorIndex),
                                  localVariableInfo.u2index);
        newLocalVariableInfo.referencedClass = localVariableInfo.referencedClass;
        localVariableTableAttributeEditor.addLocalVariableInfo(newLocalVariableInfo);
    }
}