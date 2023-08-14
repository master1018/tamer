public final class ObjectIdentifier {
    private final int[] oid;
    private int hash = -1;
    private String soid;
    private String sOID;
    private String name;
    private Object group;
    public ObjectIdentifier(int[] oid) {
        validateOid(oid);
        this.oid = oid;
    }
    public ObjectIdentifier(int[] oid, String name, Object oidGroup) {
        this(oid);
        if (oidGroup == null) {
            throw new NullPointerException(Messages.getString("security.172")); 
        }
        this.group = oidGroup;
        this.name = name;
        toOIDString(); 
    }
    public int[] getOid() {
        return oid;
    }
    public String getName() {
        return name;
    }
    public Object getGroup() {
        return group;
    }
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        return Arrays.equals(oid, ((ObjectIdentifier) o).oid);
    }
    public String toOIDString() {
        if (sOID == null) {
            sOID = "OID." + toString(); 
        }
        return sOID;
    }
    public String toString() {
        if (soid == null) {
            StringBuilder sb = new StringBuilder(4 * oid.length);
            for (int i = 0; i < oid.length - 1; ++i) {
                sb.append(oid[i]);
                sb.append('.');
            }
            sb.append(oid[oid.length - 1]);
            soid = sb.toString();
        }
        return soid;
    }
    public int hashCode() {
        if (hash == -1) {
            hash = hashIntArray(oid);
        }
        return hash;
    }
    public static void validateOid(int[] oid) {
        if (oid == null) {
            throw new NullPointerException(Messages.getString("security.98")); 
        }
        if (oid.length < 2) {
            throw new IllegalArgumentException(
                    Messages.getString("security.99")); 
        }
        if (oid[0] > 2) {
            throw new IllegalArgumentException(
                    Messages.getString("security.9A")); 
        } else if (oid[0] != 2 && oid[1] > 39) {
            throw new IllegalArgumentException(
                    Messages.getString("security.9B")); 
        }
    }
    public static int hashIntArray(int[] array) {
        int intHash = 0;
        for (int i = 0; i < array.length && i < 4; i++) {
            intHash += array[i] << (8 * i); 
        }
        return intHash & 0x7FFFFFFF; 
    }
}
