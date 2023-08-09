public class IPAddressName implements GeneralNameInterface {
    private byte[] address;
    private boolean isIPv4;
    private String name;
    public IPAddressName(DerValue derValue) throws IOException {
        this(derValue.getOctetString());
    }
    public IPAddressName(byte[] address) throws IOException {
        if (address.length == 4 || address.length == 8) {
            isIPv4 = true;
        } else if (address.length == 16 || address.length == 32) {
            isIPv4 = false;
        } else {
            throw new IOException("Invalid IPAddressName");
        }
        this.address = address;
    }
    public IPAddressName(String name) throws IOException {
        if (name == null || name.length() == 0) {
            throw new IOException("IPAddress cannot be null or empty");
        }
        if (name.charAt(name.length() - 1) == '/') {
            throw new IOException("Invalid IPAddress: " + name);
        }
        if (name.indexOf(':') >= 0) {
            parseIPv6(name);
            isIPv4 = false;
        } else if (name.indexOf('.') >= 0) {
            parseIPv4(name);
            isIPv4 = true;
        } else {
            throw new IOException("Invalid IPAddress: " + name);
        }
    }
    private void parseIPv4(String name) throws IOException {
        int slashNdx = name.indexOf('/');
        if (slashNdx == -1) {
            address = InetAddress.getByName(name).getAddress();
        } else {
            address = new byte[8];
            byte[] mask = InetAddress.getByName
                (name.substring(slashNdx+1)).getAddress();
            byte[] host = InetAddress.getByName
                (name.substring(0, slashNdx)).getAddress();
            System.arraycopy(host, 0, address, 0, 4);
            System.arraycopy(mask, 0, address, 4, 4);
        }
    }
    private final static int MASKSIZE = 16;
    private void parseIPv6(String name) throws IOException {
        int slashNdx = name.indexOf('/');
        if (slashNdx == -1) {
            address = InetAddress.getByName(name).getAddress();
        } else {
            address = new byte[32];
            byte[] base = InetAddress.getByName
                (name.substring(0, slashNdx)).getAddress();
            System.arraycopy(base, 0, address, 0, 16);
            int prefixLen = Integer.parseInt(name.substring(slashNdx+1));
            if (prefixLen > 128)
                throw new IOException("IPv6Address prefix is longer than 128");
            BitArray bitArray = new BitArray(MASKSIZE * 8);
            for (int i = 0; i < prefixLen; i++)
                bitArray.set(i, true);
            byte[] maskArray = bitArray.toByteArray();
            for (int i = 0; i < MASKSIZE; i++)
                address[MASKSIZE+i] = maskArray[i];
        }
    }
    public int getType() {
        return NAME_IP;
    }
    public void encode(DerOutputStream out) throws IOException {
        out.putOctetString(address);
    }
    public String toString() {
        try {
            return "IPAddress: " + getName();
        } catch (IOException ioe) {
            HexDumpEncoder enc = new HexDumpEncoder();
            return "IPAddress: " + enc.encodeBuffer(address);
        }
    }
    public String getName() throws IOException {
        if (name != null)
            return name;
        if (isIPv4) {
            byte[] host = new byte[4];
            System.arraycopy(address, 0, host, 0, 4);
            name = InetAddress.getByAddress(host).getHostAddress();
            if (address.length == 8) {
                byte[] mask = new byte[4];
                System.arraycopy(address, 4, mask, 0, 4);
                name = name + "/" +
                       InetAddress.getByAddress(mask).getHostAddress();
            }
        } else {
            byte[] host = new byte[16];
            System.arraycopy(address, 0, host, 0, 16);
            name = InetAddress.getByAddress(host).getHostAddress();
            if (address.length == 32) {
                byte[] maskBytes = new byte[16];
                for (int i=16; i < 32; i++)
                    maskBytes[i-16] = address[i];
                BitArray ba = new BitArray(16*8, maskBytes);
                int i=0;
                for (; i < 16*8; i++) {
                    if (!ba.get(i))
                        break;
                }
                name = name + "/" + i;
                for (; i < 16*8; i++) {
                    if (ba.get(i)) {
                        throw new IOException("Invalid IPv6 subdomain - set " +
                            "bit " + i + " not contiguous");
                    }
                }
            }
        }
        return name;
    }
    public byte[] getBytes() {
        return address.clone();
    }
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof IPAddressName))
            return false;
        byte[] other = ((IPAddressName)obj).getBytes();
        if (other.length != address.length)
            return false;
        if (address.length == 8 || address.length == 32) {
            int maskLen = address.length/2;
            byte[] maskedThis = new byte[maskLen];
            byte[] maskedOther = new byte[maskLen];
            for (int i=0; i < maskLen; i++) {
                maskedThis[i] = (byte)(address[i] & address[i+maskLen]);
                maskedOther[i] = (byte)(other[i] & other[i+maskLen]);
                if (maskedThis[i] != maskedOther[i]) {
                    return false;
                }
            }
            for (int i=maskLen; i < address.length; i++)
                if (address[i] != other[i])
                    return false;
            return true;
        } else {
            return Arrays.equals(other, address);
        }
    }
    public int hashCode() {
        int retval = 0;
        for (int i=0; i<address.length; i++)
            retval += address[i] * i;
        return retval;
    }
    public int constrains(GeneralNameInterface inputName)
    throws UnsupportedOperationException {
        int constraintType;
        if (inputName == null)
            constraintType = NAME_DIFF_TYPE;
        else if (inputName.getType() != NAME_IP)
            constraintType = NAME_DIFF_TYPE;
        else if (((IPAddressName)inputName).equals(this))
            constraintType = NAME_MATCH;
        else {
            byte[] otherAddress = ((IPAddressName)inputName).getBytes();
            if (otherAddress.length == 4 && address.length == 4)
                constraintType = NAME_SAME_TYPE;
            else if ((otherAddress.length == 8 && address.length == 8) ||
                     (otherAddress.length == 32 && address.length == 32)) {
                boolean otherSubsetOfThis = true;
                boolean thisSubsetOfOther = true;
                boolean thisEmpty = false;
                boolean otherEmpty = false;
                int maskOffset = address.length/2;
                for (int i=0; i < maskOffset; i++) {
                    if ((byte)(address[i] & address[i+maskOffset]) != address[i])
                        thisEmpty=true;
                    if ((byte)(otherAddress[i] & otherAddress[i+maskOffset]) != otherAddress[i])
                        otherEmpty=true;
                    if (!(((byte)(address[i+maskOffset] & otherAddress[i+maskOffset]) == address[i+maskOffset]) &&
                          ((byte)(address[i]   & address[i+maskOffset])      == (byte)(otherAddress[i] & address[i+maskOffset])))) {
                        otherSubsetOfThis = false;
                    }
                    if (!(((byte)(otherAddress[i+maskOffset] & address[i+maskOffset])      == otherAddress[i+maskOffset]) &&
                          ((byte)(otherAddress[i]   & otherAddress[i+maskOffset]) == (byte)(address[i] & otherAddress[i+maskOffset])))) {
                        thisSubsetOfOther = false;
                    }
                }
                if (thisEmpty || otherEmpty) {
                    if (thisEmpty && otherEmpty)
                        constraintType = NAME_MATCH;
                    else if (thisEmpty)
                        constraintType = NAME_WIDENS;
                    else
                        constraintType = NAME_NARROWS;
                } else if (otherSubsetOfThis)
                    constraintType = NAME_NARROWS;
                else if (thisSubsetOfOther)
                    constraintType = NAME_WIDENS;
                else
                    constraintType = NAME_SAME_TYPE;
            } else if (otherAddress.length == 8 || otherAddress.length == 32) {
                int i = 0;
                int maskOffset = otherAddress.length/2;
                for (; i < maskOffset; i++) {
                    if ((address[i] & otherAddress[i+maskOffset]) != otherAddress[i])
                        break;
                }
                if (i == maskOffset)
                    constraintType = NAME_WIDENS;
                else
                    constraintType = NAME_SAME_TYPE;
            } else if (address.length == 8 || address.length == 32) {
                int i = 0;
                int maskOffset = address.length/2;
                for (; i < maskOffset; i++) {
                    if ((otherAddress[i] & address[i+maskOffset]) != address[i])
                        break;
                }
                if (i == maskOffset)
                    constraintType = NAME_NARROWS;
                else
                    constraintType = NAME_SAME_TYPE;
            } else {
                constraintType = NAME_SAME_TYPE;
            }
        }
        return constraintType;
    }
    public int subtreeDepth() throws UnsupportedOperationException {
        throw new UnsupportedOperationException
            ("subtreeDepth() not defined for IPAddressName");
    }
}
