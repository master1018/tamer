public class LineNumberTableElement {
  private int start_bci;
  private int line_number;
  public LineNumberTableElement(int start_bci, int line_number) {
    this.start_bci = start_bci;
    this.line_number = line_number;
  }
  public int getStartBCI() {
    return this.start_bci;
  }
  public int getLineNumber() {
    return this.line_number;
  }
}
