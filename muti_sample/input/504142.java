public abstract class SmsAddress {
    public static final int TON_UNKNOWN = 0;
    public static final int TON_INTERNATIONAL = 1;
    public static final int TON_NATIONAL = 2;
    public static final int TON_NETWORK = 3;
    public static final int TON_SUBSCRIBER = 4;
    public static final int TON_ALPHANUMERIC = 5;
    public static final int TON_ABBREVIATED = 6;
    public int ton;
    public String address;
    public byte[] origBytes;
    public String getAddressString() {
        return address;
    }
    public boolean isAlphanumeric() {
        return ton == TON_ALPHANUMERIC;
    }
    public boolean isNetworkSpecific() {
        return ton == TON_NETWORK;
    }
    public boolean couldBeEmailGateway() {
        return address.length() <= 4;
    }
}
