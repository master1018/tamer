public class BasicLineNumberInfo implements LineNumberInfo {
  private String sourceFileName;
  private int lineNo;
  private Address startPC;
  private Address endPC;
  public BasicLineNumberInfo(String sourceFileName,
                             int lineNo,
                             Address startPC,
                             Address endPC) {
    this.sourceFileName = sourceFileName;
    this.lineNo = lineNo;
    this.startPC = startPC;
    this.endPC = endPC;
  }
  public String  getSourceFileName() { return sourceFileName; }
  public int     getLineNumber()     { return lineNo; }
  public Address getStartPC()        { return startPC; }
  public Address getEndPC()          { return endPC; }
  public void    setEndPC(Address pc) { endPC = pc; }
}
