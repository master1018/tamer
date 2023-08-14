public class SerializableEntity extends AbstractHttpEntity {
    private byte[] objSer;
    private Serializable objRef;
    public SerializableEntity(Serializable ser, boolean bufferize) throws IOException {
        super();
        if (ser == null) {
            throw new IllegalArgumentException("Source object may not be null");
        }
        if (bufferize) {
            createBytes(ser);
        } else {
            this.objRef = ser;
        }
    }
    private void createBytes(Serializable ser) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(baos);
        out.writeObject(ser);
        out.flush();	
        this.objSer = baos.toByteArray();
    }
    public InputStream getContent() throws IOException, IllegalStateException {
        if (this.objSer == null) {
            createBytes(this.objRef);
        }
        return new ByteArrayInputStream(this.objSer);
    }
    public long getContentLength() {
        if (this.objSer ==  null) { 
            return -1;
        } else {
            return this.objSer.length;
        }
    }
    public boolean isRepeatable() {
        return true;
    }
    public boolean isStreaming() {
        return this.objSer == null;
    }
    public void writeTo(OutputStream outstream) throws IOException {
        if (outstream == null) {
            throw new IllegalArgumentException("Output stream may not be null");
        }
        if (this.objSer == null) {
            ObjectOutputStream out = new ObjectOutputStream(outstream);
            out.writeObject(this.objRef);
            out.flush();
        } else {
            outstream.write(this.objSer);
            outstream.flush();
        }
    }
}
