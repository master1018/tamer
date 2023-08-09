package proguard.classfile.attribute.visitor;
import proguard.classfile.*;
import proguard.classfile.attribute.*;
public interface LineNumberInfoVisitor
{
    public void visitLineNumberInfo(Clazz clazz, Method method, CodeAttribute codeAttribute, LineNumberInfo lineNumberInfo);
}
