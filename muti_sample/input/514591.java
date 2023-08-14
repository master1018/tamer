package proguard.classfile.attribute.preverification;
import proguard.classfile.*;
import proguard.classfile.attribute.CodeAttribute;
import proguard.classfile.attribute.preverification.visitor.StackMapFrameVisitor;
public class LessZeroFrame extends StackMapFrame
{
    public int choppedVariablesCount;
    public LessZeroFrame()
    {
    }
    public LessZeroFrame(int tag)
    {
        choppedVariablesCount = LESS_ZERO_FRAME + 3 - tag;
    }
    public LessZeroFrame(byte choppedVariablesCount)
    {
        this.choppedVariablesCount = (int)choppedVariablesCount;
    }
    public int getTag()
    {
        return LESS_ZERO_FRAME + 3 - choppedVariablesCount;
    }
    public void accept(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, StackMapFrameVisitor stackMapFrameVisitor)
    {
        stackMapFrameVisitor.visitLessZeroFrame(clazz, method, codeAttribute, offset, this);
    }
    public boolean equals(Object object)
    {
        if (!super.equals(object))
        {
            return false;
        }
        LessZeroFrame other = (LessZeroFrame)object;
        return this.u2offsetDelta == other.u2offsetDelta &&
               this.choppedVariablesCount != other.choppedVariablesCount;
    }
    public int hashCode()
    {
        return super.hashCode() ^ choppedVariablesCount;
    }
    public String toString()
    {
        return super.toString()+"Var: (chopped "+choppedVariablesCount+"), Stack: (empty)";
    }
}
