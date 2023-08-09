public class UniqueIdentity {
    private BitArray    id;
    public UniqueIdentity(BitArray id) {
        this.id = id;
    }
    public UniqueIdentity(byte[] id) {
        this.id = new BitArray(id.length*8, id);
    }
    public UniqueIdentity(DerInputStream in) throws IOException {
        DerValue derVal = in.getDerValue();
        id = derVal.getUnalignedBitString(true);
    }
    public UniqueIdentity(DerValue derVal) throws IOException {
        id = derVal.getUnalignedBitString(true);
    }
    public String toString() {
        return ("UniqueIdentity:" + id.toString() + "\n");
    }
    public void encode(DerOutputStream out, byte tag) throws IOException {
        byte[] bytes = id.toByteArray();
        int excessBits = bytes.length*8 - id.length();
        out.write(tag);
        out.putLength(bytes.length + 1);
        out.write(excessBits);
        out.write(bytes);
    }
    public boolean[] getId() {
        if (id == null) return null;
        return id.toBooleanArray();
    }
}
