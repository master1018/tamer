package proguard.classfile.attribute.preverification.visitor;
import proguard.classfile.*;
import proguard.classfile.attribute.CodeAttribute;
import proguard.classfile.attribute.preverification.*;
public interface StackMapFrameVisitor
{
    public void visitSameZeroFrame(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, SameZeroFrame sameZeroFrame);
    public void visitSameOneFrame( Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, SameOneFrame  sameOneFrame);
    public void visitLessZeroFrame(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, LessZeroFrame lessZeroFrame);
    public void visitMoreZeroFrame(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, MoreZeroFrame moreZeroFrame);
    public void visitFullFrame(    Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, FullFrame     fullFrame);
}
