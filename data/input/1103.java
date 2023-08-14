public class NotInHeapException extends AddressException {
  public NotInHeapException(long addr) {
    super(addr);
  }
  public NotInHeapException(String detail, long addr) {
    super(detail, addr);
  }
}
