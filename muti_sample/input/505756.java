public abstract class ASN1Collection
    extends DERObject
{
    private int size;
    private DEREncodable obj0;
    private DEREncodable obj1;
    private DEREncodable obj2;
    private DEREncodable obj3;
    private DEREncodable[] rest;
    public final DEREncodable getObjectAt(int index) {
        if ((index < 0) || (index >= size)) {
            throw new IndexOutOfBoundsException(Integer.toString(index));
        }
        switch (index) {
            case 0: return obj0;
            case 1: return obj1;
            case 2: return obj2;
            case 3: return obj3;
            default: return rest[index - 4];
        }
    }
    public final int size() {
        return size;
    }
    public final int hashCode() {
        Enumeration e = this.getObjects();
        int hashCode = 0;
        while (e.hasMoreElements()) {
            Object o = e.nextElement();
            if (o != null) {
                hashCode ^= o.hashCode();
            }
        }
        return hashCode;
    }
    protected void addObject(DEREncodable obj) {
        if (obj == null) {
            throw new NullPointerException("obj == null");
        }
        int sz = size;
        switch (sz) {
            case 0: obj0 = obj; break;
            case 1: obj1 = obj; break;
            case 2: obj2 = obj; break;
            case 3: obj3 = obj; break;
            case 4: {
                rest = new DEREncodable[5];
                rest[0] = obj;
                break;
            }
            default: {
                int index = sz - 4;
                if (index >= rest.length) {
                    DEREncodable[] newRest = new DEREncodable[index * 2 + 10];
                    System.arraycopy(rest, 0, newRest, 0, rest.length);
                    rest = newRest;
                }
                rest[index] = obj;
                break;
            }
        }
        size++;
    }
    private void setObjectAt(DEREncodable obj, int index) {
        switch (index) {
            case 0: obj0 = obj; break;
            case 1: obj1 = obj; break;
            case 2: obj2 = obj; break;
            case 3: obj3 = obj; break;
            default: {
                rest[index - 4] = obj;
                break;
            }
        }
    }
     abstract void encode(DEROutputStream out) throws IOException;
    public final Enumeration getObjects() {
        return new ASN1CollectionEnumeration();
    }
    private class ASN1CollectionEnumeration implements Enumeration {
        private final int origSize = size;
        private int at = 0;
        public boolean hasMoreElements() {
            if (size != origSize) {
                throw new ConcurrentModificationException();
            }
            return at < origSize;
        }
        public Object nextElement() {
            if (size != origSize) {
                throw new ConcurrentModificationException();
            }
            switch (at++) {
                case 0: return obj0;
                case 1: return obj1;
                case 2: return obj2;
                case 3: return obj3;
                default: return rest[at - 5];
            }
        }
    }
    protected void sort() {
        if (size <= 1) {
            return;
        }
        boolean swapped = true;
        while (swapped) {
            int index = 0;
            byte[] a = getEncoded(getObjectAt(0));
            swapped = false;
            while (index != size - 1) {
                int nextIndex = index + 1;
                byte[] b = getEncoded(getObjectAt(nextIndex));
                if (lessThanOrEqual(a, b)) {
                    a = b;
                } else {
                    DEREncodable o = getObjectAt(index);
                    setObjectAt(getObjectAt(nextIndex), index);
                    setObjectAt(o, nextIndex);
                    swapped = true;
                }
                index++;
            }
        }
    }
    private static boolean lessThanOrEqual(byte[] a, byte[] b) {
        if (a.length <= b.length) {
            for (int i = 0; i != a.length; i++) {
                int l = a[i] & 0xff;
                int r = b[i] & 0xff;
                if (r > l) {
                    return true;
                } else if (l > r) {
                    return false;
                }
            }
            return true;
        } else {
            for (int i = 0; i != b.length; i++) {
                 int l = a[i] & 0xff;
                 int r = b[i] & 0xff;
                 if (r > l) {
                     return true;
                 } else if (l > r) {
                     return false;
                 }
             }
             return false;
         }
    }
    private static byte[] getEncoded(DEREncodable obj) {
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        ASN1OutputStream aOut = new ASN1OutputStream(bOut);
        try {
            aOut.writeObject(obj);
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "cannot encode object added to collection");
        }
        return bOut.toByteArray();
    }
    public final String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < size; i++) {
            if (i != 0) sb.append(", ");
            sb.append(getObjectAt(i));
        }
        sb.append(']');
        return sb.toString();
    }
}
