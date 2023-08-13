package proguard.classfile.attribute.visitor;
import proguard.classfile.*;
import proguard.classfile.attribute.*;
public interface LocalVariableTypeInfoVisitor
{
    public void visitLocalVariableTypeInfo(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableTypeInfo localVariableTypeInfo);
}
