package proguard.classfile.attribute.preverification;
import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.preverification.visitor.StackMapFrameVisitor;
import proguard.classfile.attribute.visitor.AttributeVisitor;
public class StackMapTableAttribute extends Attribute
{
    public int             u2stackMapFramesCount;
    public StackMapFrame[] stackMapFrames;
    public StackMapTableAttribute()
    {
    }
    public StackMapTableAttribute(StackMapFrame[] stackMapFrames)
    {
        this(stackMapFrames.length, stackMapFrames);
    }
    public StackMapTableAttribute(int             stackMapFramesCount,
                                  StackMapFrame[] stackMapFrames)
    {
        this.u2stackMapFramesCount = stackMapFramesCount;
        this.stackMapFrames        = stackMapFrames;
    }
    public void accept(Clazz clazz, Method method, CodeAttribute codeAttribute, AttributeVisitor attributeVisitor)
    {
        attributeVisitor.visitStackMapTableAttribute(clazz, method, codeAttribute, this);
    }
    public void stackMapFramesAccept(Clazz clazz, Method method, CodeAttribute codeAttribute, StackMapFrameVisitor stackMapFrameVisitor)
    {
        int offset = 0;
        for (int index = 0; index < u2stackMapFramesCount; index++)
        {
            StackMapFrame stackMapFrame = stackMapFrames[index];
            offset += stackMapFrame.getOffsetDelta() + (index == 0 ? 0 : 1);
            stackMapFrame.accept(clazz, method, codeAttribute, offset, stackMapFrameVisitor);
        }
    }
}
