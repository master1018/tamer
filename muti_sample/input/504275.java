package proguard.classfile.attribute;
public class LineNumberInfo
{
    public int u2startPC;
    public int u2lineNumber;
    public LineNumberInfo()
    {
    }
    public LineNumberInfo(int u2startPC, int u2lineNumber)
    {
        this.u2startPC    = u2startPC;
        this.u2lineNumber = u2lineNumber;
    }
}
