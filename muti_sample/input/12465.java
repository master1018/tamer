public class TenuredGeneration extends OneContigSpaceCardGeneration {
  public TenuredGeneration(Address addr) {
    super(addr);
  }
  public Generation.Name kind() {
    return Generation.Name.MARK_SWEEP_COMPACT;
  }
  public String name() {
    return "tenured generation";
  }
}
