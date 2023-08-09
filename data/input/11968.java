public class UnalignedAddressException extends AddressException {
  public UnalignedAddressException(long addr) {
    super(addr);
  }
  public UnalignedAddressException(String detail, long addr) {
    super(detail, addr);
  }
}
