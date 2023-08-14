public final class MarshalledObject<T> implements Serializable {
    private byte[] objBytes = null;
    private byte[] locBytes = null;
    private int hash;
    private static final long serialVersionUID = 8988374069173025854L;
    public MarshalledObject(T obj) throws IOException {
        if (obj == null) {
            hash = 13;
            return;
        }
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ByteArrayOutputStream lout = new ByteArrayOutputStream();
        MarshalledObjectOutputStream out =
            new MarshalledObjectOutputStream(bout, lout);
        out.writeObject(obj);
        out.flush();
        objBytes = bout.toByteArray();
        locBytes = (out.hadAnnotations() ? lout.toByteArray() : null);
        int h = 0;
        for (int i = 0; i < objBytes.length; i++) {
            h = 31 * h + objBytes[i];
        }
        hash = h;
    }
    public T get() throws IOException, ClassNotFoundException {
        if (objBytes == null)   
            return null;
        ByteArrayInputStream bin = new ByteArrayInputStream(objBytes);
        ByteArrayInputStream lin =
            (locBytes == null ? null : new ByteArrayInputStream(locBytes));
        MarshalledObjectInputStream in =
            new MarshalledObjectInputStream(bin, lin);
        T obj = (T) in.readObject();
        in.close();
        return obj;
    }
    public int hashCode() {
        return hash;
    }
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj != null && obj instanceof MarshalledObject) {
            MarshalledObject other = (MarshalledObject) obj;
            if (objBytes == null || other.objBytes == null)
                return objBytes == other.objBytes;
            if (objBytes.length != other.objBytes.length)
                return false;
            for (int i = 0; i < objBytes.length; ++i) {
                if (objBytes[i] != other.objBytes[i])
                    return false;
            }
            return true;
        } else {
            return false;
        }
    }
    private static class MarshalledObjectOutputStream
        extends MarshalOutputStream
    {
        private ObjectOutputStream locOut;
        private boolean hadAnnotations;
        MarshalledObjectOutputStream(OutputStream objOut, OutputStream locOut)
            throws IOException
        {
            super(objOut);
            this.useProtocolVersion(ObjectStreamConstants.PROTOCOL_VERSION_2);
            this.locOut = new ObjectOutputStream(locOut);
            hadAnnotations = false;
        }
        boolean hadAnnotations() {
            return hadAnnotations;
        }
        protected void writeLocation(String loc) throws IOException {
            hadAnnotations |= (loc != null);
            locOut.writeObject(loc);
        }
        public void flush() throws IOException {
            super.flush();
            locOut.flush();
        }
    }
    private static class MarshalledObjectInputStream
        extends MarshalInputStream
    {
        private ObjectInputStream locIn;
        MarshalledObjectInputStream(InputStream objIn, InputStream locIn)
            throws IOException
        {
            super(objIn);
            this.locIn = (locIn == null ? null : new ObjectInputStream(locIn));
        }
        protected Object readLocation()
            throws IOException, ClassNotFoundException
        {
            return (locIn == null ? null : locIn.readObject());
        }
    }
}
