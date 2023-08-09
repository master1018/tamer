public class TypeCodeInputStream extends EncapsInputStream implements TypeCodeReader
{
    private Map typeMap = null;
    private InputStream enclosure = null;
    private boolean isEncapsulation = false;
    public TypeCodeInputStream(org.omg.CORBA.ORB orb, byte[] data, int size) {
        super(orb, data, size);
    }
    public TypeCodeInputStream(org.omg.CORBA.ORB orb,
                               byte[] data,
                               int size,
                               boolean littleEndian,
                               GIOPVersion version) {
        super(orb, data, size, littleEndian, version);
    }
    public TypeCodeInputStream(org.omg.CORBA.ORB orb,
                               ByteBuffer byteBuffer,
                               int size,
                               boolean littleEndian,
                               GIOPVersion version) {
        super(orb, byteBuffer, size, littleEndian, version);
    }
    public void addTypeCodeAtPosition(TypeCodeImpl tc, int position) {
        if (typeMap == null) {
            typeMap = new HashMap(16);
        }
        typeMap.put(new Integer(position), tc);
    }
    public TypeCodeImpl getTypeCodeAtPosition(int position) {
        if (typeMap == null)
            return null;
        return (TypeCodeImpl)typeMap.get(new Integer(position));
    }
    public void setEnclosingInputStream(InputStream enclosure) {
        this.enclosure = enclosure;
    }
    public TypeCodeReader getTopLevelStream() {
        if (enclosure == null)
            return this;
        if (enclosure instanceof TypeCodeReader)
            return ((TypeCodeReader)enclosure).getTopLevelStream();
        return this;
    }
    public int getTopLevelPosition() {
        if (enclosure != null && enclosure instanceof TypeCodeReader) {
            int topPos = ((TypeCodeReader)enclosure).getTopLevelPosition();
            int pos = topPos - getBufferLength() + getPosition();
            return pos;
        }
        return getPosition();
    }
    public static TypeCodeInputStream readEncapsulation(InputStream is, org.omg.CORBA.ORB _orb) {
        TypeCodeInputStream encap;
        int encapLength = is.read_long();
        byte[] encapBuffer = new byte[encapLength];
        is.read_octet_array(encapBuffer, 0, encapBuffer.length);
        if (is instanceof CDRInputStream) {
            encap = new TypeCodeInputStream((ORB)_orb, encapBuffer, encapBuffer.length,
                                            ((CDRInputStream)is).isLittleEndian(),
                                            ((CDRInputStream)is).getGIOPVersion());
        } else {
            encap = new TypeCodeInputStream((ORB)_orb, encapBuffer, encapBuffer.length);
        }
        encap.setEnclosingInputStream(is);
        encap.makeEncapsulation();
        return encap;
    }
    protected void makeEncapsulation() {
        consumeEndian();
        isEncapsulation = true;
    }
    public void printTypeMap() {
        System.out.println("typeMap = {");
        Iterator i = typeMap.keySet().iterator();
        while (i.hasNext()) {
            Integer pos = (Integer)i.next();
            TypeCodeImpl tci = (TypeCodeImpl)typeMap.get(pos);
            System.out.println("  key = " + pos.intValue() + ", value = " + tci.description());
        }
        System.out.println("}");
    }
}
