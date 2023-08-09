public class UnmappedAddressException extends AddressException {
  public UnmappedAddressException(long addr) {
    super(addr);
  }
  public UnmappedAddressException(String detail, long addr) {
    super(detail, addr);
  }
}
