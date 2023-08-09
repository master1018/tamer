public class Bytes {
  private boolean swap;
  public Bytes(MachineDescription machDesc) {
    swap = !machDesc.isBigEndian();
  }
  public short swapShort(short x) {
    if (!swap)
      return x;
    return (short) (((x >> 8) & 0xFF) | (x << 8));
  }
  public int swapInt(int x) {
    if (!swap)
      return x;
    return (swapShort((short) x) << 16) | (swapShort((short) (x >> 16)) & 0xFFFF);
  }
  public long swapLong(long x) {
    if (!swap)
      return x;
    return (swapInt((int) x) << 32) | (swapInt((int) (x >> 32)) & 0xFFFFFFFF);
  }
}
