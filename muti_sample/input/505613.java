package proguard.classfile.attribute.visitor;
import proguard.classfile.Clazz;
import proguard.classfile.attribute.InnerClassesInfo;
public interface InnerClassesInfoVisitor
{
    public void visitInnerClassesInfo(Clazz clazz, InnerClassesInfo innerClassesInfo);
}
