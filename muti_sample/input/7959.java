public final class TypeCodeOutputStream extends EncapsOutputStream
{
    private OutputStream enclosure = null;
    private Map typeMap = null;
    private boolean isEncapsulation = false;
    public TypeCodeOutputStream(ORB orb) {
        super(orb, false);
    }
    public TypeCodeOutputStream(ORB orb, boolean littleEndian) {
        super(orb, littleEndian);
    }
    public org.omg.CORBA.portable.InputStream create_input_stream()
    {
        TypeCodeInputStream tcis
            = new TypeCodeInputStream((ORB)orb(), getByteBuffer(), getIndex(), isLittleEndian(), getGIOPVersion());
        return tcis;
    }
    public void setEnclosingOutputStream(OutputStream enclosure) {
        this.enclosure = enclosure;
    }
    public TypeCodeOutputStream getTopLevelStream() {
        if (enclosure == null)
            return this;
        if (enclosure instanceof TypeCodeOutputStream)
            return ((TypeCodeOutputStream)enclosure).getTopLevelStream();
        return this;
    }
    public int getTopLevelPosition() {
        if (enclosure != null && enclosure instanceof TypeCodeOutputStream) {
            int pos = ((TypeCodeOutputStream)enclosure).getTopLevelPosition() + getPosition();
            if (isEncapsulation) pos += 4;
            return pos;
        }
        return getPosition();
    }
    public void addIDAtPosition(String id, int position) {
        if (typeMap == null)
            typeMap = new HashMap(16);
        typeMap.put(id, new Integer(position));
    }
    public int getPositionForID(String id) {
        if (typeMap == null)
            throw wrapper.refTypeIndirType( CompletionStatus.COMPLETED_NO ) ;
        return ((Integer)typeMap.get(id)).intValue();
    }
    public void writeRawBuffer(org.omg.CORBA.portable.OutputStream s, int firstLong) {
        s.write_long(firstLong);
        ByteBuffer byteBuffer = getByteBuffer();
        if (byteBuffer.hasArray())
        {
             s.write_octet_array(byteBuffer.array(), 4, getIndex() - 4);
        }
        else
        {
             byte[] buf = new byte[byteBuffer.limit()];
             for (int i = 0; i < buf.length; i++)
                  buf[i] = byteBuffer.get(i);
             s.write_octet_array(buf, 4, getIndex() - 4);
        }
    }
    public TypeCodeOutputStream createEncapsulation(org.omg.CORBA.ORB _orb) {
        TypeCodeOutputStream encap = new TypeCodeOutputStream((ORB)_orb, isLittleEndian());
        encap.setEnclosingOutputStream(this);
        encap.makeEncapsulation();
        return encap;
    }
    protected void makeEncapsulation() {
        putEndian();
        isEncapsulation = true;
    }
    public static TypeCodeOutputStream wrapOutputStream(OutputStream os) {
        boolean littleEndian = ((os instanceof CDROutputStream) ? ((CDROutputStream)os).isLittleEndian() : false);
        TypeCodeOutputStream tos = new TypeCodeOutputStream((ORB)os.orb(), littleEndian);
        tos.setEnclosingOutputStream(os);
        return tos;
    }
    public int getPosition() {
        return getIndex();
    }
    public int getRealIndex(int index) {
        int topPos = getTopLevelPosition();
        return topPos;
    }
    public byte[] getTypeCodeBuffer() {
        ByteBuffer theBuffer = getByteBuffer();
        byte[] tcBuffer = new byte[getIndex() - 4];
        for (int i = 0; i < tcBuffer.length; i++)
            tcBuffer[i] = theBuffer.get(i+4);
        return tcBuffer;
    }
    public void printTypeMap() {
        System.out.println("typeMap = {");
        Iterator i = typeMap.keySet().iterator();
        while (i.hasNext()) {
            String id = (String)i.next();
            Integer pos = (Integer)typeMap.get(id);
            System.out.println("  key = " + id + ", value = " + pos);
        }
        System.out.println("}");
    }
}
