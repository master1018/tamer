public class MachineDescriptionSPARC64Bit extends MachineDescriptionTwosComplement implements MachineDescription {
  public long getAddressSize() {
    return 8;
  }
  public boolean isBigEndian() {
    return true;
  }
  public boolean isLP64() {
    return true;
  }
}
