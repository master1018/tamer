package proguard.classfile.attribute.preverification;
import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.preverification.visitor.StackMapFrameVisitor;
import proguard.classfile.attribute.visitor.AttributeVisitor;
public class StackMapAttribute extends Attribute
{
    public int         u2stackMapFramesCount;
    public FullFrame[] stackMapFrames;
    public StackMapAttribute()
    {
    }
    public StackMapAttribute(FullFrame[] stackMapFrames)
    {
        this(stackMapFrames.length, stackMapFrames);
    }
    public StackMapAttribute(int         stackMapFramesCount,
                             FullFrame[] stackMapFrames)
    {
        this.u2stackMapFramesCount = stackMapFramesCount;
        this.stackMapFrames        = stackMapFrames;
    }
    public void accept(Clazz clazz, Method method, CodeAttribute codeAttribute, AttributeVisitor attributeVisitor)
    {
        attributeVisitor.visitStackMapAttribute(clazz, method, codeAttribute, this);
    }
    public void stackMapFramesAccept(Clazz clazz, Method method, CodeAttribute codeAttribute, StackMapFrameVisitor stackMapFrameVisitor)
    {
        for (int index = 0; index < u2stackMapFramesCount; index++)
        {
            FullFrame stackMapFrame = stackMapFrames[index];
            stackMapFrameVisitor.visitFullFrame(clazz, method, codeAttribute, stackMapFrame.getOffsetDelta(), stackMapFrame);
        }
    }
}
