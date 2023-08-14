package proguard.classfile.attribute.preverification;
import proguard.classfile.*;
import proguard.classfile.attribute.CodeAttribute;
import proguard.classfile.attribute.preverification.visitor.*;
public class SameOneFrame extends StackMapFrame
{
    public VerificationType stackItem;
    public SameOneFrame()
    {
    }
    public SameOneFrame(int tag)
    {
        u2offsetDelta = tag - SAME_ONE_FRAME;
    }
    public SameOneFrame(VerificationType stackItem)
    {
        this.stackItem = stackItem;
    }
    public void stackItemAccept(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, VerificationTypeVisitor verificationTypeVisitor)
    {
        stackItem.accept(clazz, method, codeAttribute, offset, verificationTypeVisitor);
    }
    public int getTag()
    {
        return u2offsetDelta < 64 ?
            SAME_ONE_FRAME + u2offsetDelta :
            SAME_ONE_FRAME_EXTENDED;
    }
    public void accept(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, StackMapFrameVisitor stackMapFrameVisitor)
    {
        stackMapFrameVisitor.visitSameOneFrame(clazz, method, codeAttribute, offset, this);
    }
    public boolean equals(Object object)
    {
        if (!super.equals(object))
        {
            return false;
        }
        SameOneFrame other = (SameOneFrame)object;
        return this.u2offsetDelta == other.u2offsetDelta &&
               this.stackItem.equals(other.stackItem);
    }
    public int hashCode()
    {
        return super.hashCode() ^ stackItem.hashCode();
    }
    public String toString()
    {
        return super.toString()+"Var: ..., Stack: ["+stackItem.toString()+"]";
    }
}
