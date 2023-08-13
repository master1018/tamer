package proguard.classfile.editor;
import proguard.classfile.attribute.visitor.LocalVariableTypeInfoVisitor;
import proguard.classfile.attribute.*;
import proguard.classfile.*;
public class LocalVariableTypeInfoAdder
implements   LocalVariableTypeInfoVisitor
{
    private final ConstantAdder                     constantAdder;
    private final LocalVariableTypeTableAttributeEditor localVariableTypeTableAttributeEditor;
    public LocalVariableTypeInfoAdder(ProgramClass                    targetClass,
                                      LocalVariableTypeTableAttribute targetLocalVariableTypeTableAttribute)
    {
        this.constantAdder                         = new ConstantAdder(targetClass);
        this.localVariableTypeTableAttributeEditor = new LocalVariableTypeTableAttributeEditor(targetLocalVariableTypeTableAttribute);
    }
    public void visitLocalVariableTypeInfo(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableTypeInfo localVariableTypeInfo)
    {
        LocalVariableTypeInfo newLocalVariableTypeInfo =
            new LocalVariableTypeInfo(localVariableTypeInfo.u2startPC,
                                      localVariableTypeInfo.u2length,
                                      constantAdder.addConstant(clazz, localVariableTypeInfo.u2nameIndex),
                                      constantAdder.addConstant(clazz, localVariableTypeInfo.u2signatureIndex),
                                      localVariableTypeInfo.u2index);
        newLocalVariableTypeInfo.referencedClasses = localVariableTypeInfo.referencedClasses;
        localVariableTypeTableAttributeEditor.addLocalVariableTypeInfo(newLocalVariableTypeInfo);
    }
}