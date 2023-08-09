package proguard.classfile.attribute;
import proguard.classfile.VisitorAccepter;
public class ExceptionInfo implements VisitorAccepter
{
    public int u2startPC;
    public int u2endPC;
    public int u2handlerPC;
    public int u2catchType;
    public Object visitorInfo;
    public ExceptionInfo()
    {
        this(0, 0, 0, 0);
    }
    public ExceptionInfo(int u2startPC,
                         int u2endPC,
                         int u2handlerPC,
                         int u2catchType)
    {
        this.u2startPC   = u2startPC;
        this.u2endPC     = u2endPC;
        this.u2handlerPC = u2handlerPC;
        this.u2catchType = u2catchType;
    }
    public boolean isApplicable(int instructionOffset)
    {
        return instructionOffset >= u2startPC &&
               instructionOffset <  u2endPC;
    }
    public boolean isApplicable(int startOffset, int endOffset)
    {
        return u2startPC < endOffset &&
               u2endPC   > startOffset;
    }
    public Object getVisitorInfo()
    {
        return visitorInfo;
    }
    public void setVisitorInfo(Object visitorInfo)
    {
        this.visitorInfo = visitorInfo;
    }
}
