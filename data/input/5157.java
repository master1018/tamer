public class MachineDescriptionSPARC32Bit extends MachineDescriptionTwosComplement implements MachineDescription {
  public long getAddressSize() {
    return 4;
  }
  public boolean isBigEndian() {
    return true;
  }
}
