public class ClosestSymbol {
  private String name;
  private long   offset;
  public ClosestSymbol(String name, long offset) {
    this.name = name;
    this.offset = offset;
  }
  public String getName() {
    return name;
  }
  public long getOffset() {
    return offset;
  }
  public String offsetAsHex() {
    return "0x" + Long.toHexString(offset);
  }
}
