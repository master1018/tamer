public class Signature implements Parcelable {
    private final byte[] mSignature;
    private int mHashCode;
    private boolean mHaveHashCode;
    private String mString;
    public Signature(byte[] signature) {
        mSignature = signature.clone();
    }
    public Signature(String text) {
        final int N = text.length()/2;
        byte[] sig = new byte[N];
        for (int i=0; i<N; i++) {
            char c = text.charAt(i*2);
            byte b = (byte)(
                    (c >= 'a' ? (c - 'a' + 10) : (c - '0'))<<4);
            c = text.charAt(i*2 + 1);
            b |= (byte)(c >= 'a' ? (c - 'a' + 10) : (c - '0'));
            sig[i] = b;
        }
        mSignature = sig;
    }
    public char[] toChars() {
        return toChars(null, null);
    }
    public char[] toChars(char[] existingArray, int[] outLen) {
        byte[] sig = mSignature;
        final int N = sig.length;
        final int N2 = N*2;
        char[] text = existingArray == null || N2 > existingArray.length
                ? new char[N2] : existingArray;
        for (int j=0; j<N; j++) {
            byte v = sig[j];
            int d = (v>>4)&0xf;
            text[j*2] = (char)(d >= 10 ? ('a' + d - 10) : ('0' + d));
            d = v&0xf;
            text[j*2+1] = (char)(d >= 10 ? ('a' + d - 10) : ('0' + d));
        }
        if (outLen != null) outLen[0] = N;
        return text;
    }
    public String toCharsString() {
        if (mString != null) return mString;
        String str = new String(toChars());
        mString = str;
        return mString;
    }
    public byte[] toByteArray() {
        byte[] bytes = new byte[mSignature.length];
        System.arraycopy(mSignature, 0, bytes, 0, mSignature.length);
        return bytes;
    }
    @Override
    public boolean equals(Object obj) {
        try {
            if (obj != null) {
                Signature other = (Signature)obj;
                return Arrays.equals(mSignature, other.mSignature);
            }
        } catch (ClassCastException e) {
        }
        return false;
    }
    @Override
    public int hashCode() {
        if (mHaveHashCode) {
            return mHashCode;
        }
        mHashCode = Arrays.hashCode(mSignature);
        mHaveHashCode = true;
        return mHashCode;
    }
    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel dest, int parcelableFlags) {
        dest.writeByteArray(mSignature);
    }
    public static final Parcelable.Creator<Signature> CREATOR
            = new Parcelable.Creator<Signature>() {
        public Signature createFromParcel(Parcel source) {
            return new Signature(source);
        }
        public Signature[] newArray(int size) {
            return new Signature[size];
        }
    };
    private Signature(Parcel source) {
        mSignature = source.createByteArray();
    }
}
