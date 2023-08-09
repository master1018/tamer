public class BasicAddressFieldWrapper extends BasicFieldWrapper implements AddressField {
  public BasicAddressFieldWrapper(Field field) {
    super(field);
  }
  public Address getValue(Address addr)
    throws UnmappedAddressException, UnalignedAddressException, WrongTypeException {
    return field.getAddress(addr);
  }
  public Address getValue()
    throws UnmappedAddressException, UnalignedAddressException, WrongTypeException {
    return field.getAddress();
  }
}
