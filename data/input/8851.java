class BinaryAttribute implements Constants {
    Identifier name;
    byte data[];
    BinaryAttribute next;
    BinaryAttribute(Identifier name, byte data[], BinaryAttribute next) {
        this.name = name;
        this.data = data;
        this.next = next;
    }
    public static BinaryAttribute load(DataInputStream in, BinaryConstantPool cpool, int mask) throws IOException {
        BinaryAttribute atts = null;
        int natt = in.readUnsignedShort();  
        for (int i = 0 ; i < natt ; i++) {
            Identifier id = cpool.getIdentifier(in.readUnsignedShort());
            int len = in.readInt();
            if (id.equals(idCode) && ((mask & ATT_CODE) == 0)) {
                in.skipBytes(len);
            } else {
                byte data[] = new byte[len];
                in.readFully(data);
                atts = new BinaryAttribute(id, data, atts);
            }
        }
        return atts;
    }
    static void write(BinaryAttribute attributes, DataOutputStream out,
                      BinaryConstantPool cpool, Environment env) throws IOException {
        int attributeCount = 0;
        for (BinaryAttribute att = attributes; att != null; att = att.next)
            attributeCount++;
        out.writeShort(attributeCount);
        for (BinaryAttribute att = attributes; att != null; att = att.next) {
            Identifier name = att.name;
            byte data[] = att.data;
            out.writeShort(cpool.indexString(name.toString(), env));
            out.writeInt(data.length);
            out.write(data, 0, data.length);
        }
    }
    public Identifier getName() { return name; }
    public byte getData()[] { return data; }
    public BinaryAttribute getNextAttribute() { return next; }
}
