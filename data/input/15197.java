public abstract class OutputStreamHook extends ObjectOutputStream
{
    private HookPutFields putFields = null;
    private class HookPutFields extends ObjectOutputStream.PutField
    {
        private Hashtable fields = new Hashtable();
        public void put(String name, boolean value){
            fields.put(name, new Boolean(value));
        }
        public void put(String name, char value){
            fields.put(name, new Character(value));
        }
        public void put(String name, byte value){
            fields.put(name, new Byte(value));
        }
        public void put(String name, short value){
            fields.put(name, new Short(value));
        }
        public void put(String name, int value){
            fields.put(name, new Integer(value));
        }
        public void put(String name, long value){
            fields.put(name, new Long(value));
        }
        public void put(String name, float value){
            fields.put(name, new Float(value));
        }
        public void put(String name, double value){
            fields.put(name, new Double(value));
        }
        public void put(String name, Object value){
            fields.put(name, value);
        }
        public void write(ObjectOutput out) throws IOException {
            OutputStreamHook hook = (OutputStreamHook)out;
            ObjectStreamField[] osfields = hook.getFieldsNoCopy();
            for (int i = 0; i < osfields.length; i++) {
                Object value = fields.get(osfields[i].getName());
                hook.writeField(osfields[i], value);
            }
        }
    }
    abstract void writeField(ObjectStreamField field, Object value) throws IOException;
    public OutputStreamHook()
        throws java.io.IOException {
        super();
    }
    public void defaultWriteObject() throws IOException {
        writeObjectState.defaultWriteObject(this);
        defaultWriteObjectDelegate();
    }
    public abstract void defaultWriteObjectDelegate();
    public ObjectOutputStream.PutField putFields()
        throws IOException {
        putFields = new HookPutFields();
        return putFields;
    }
    protected byte streamFormatVersion = 1;
    public byte getStreamFormatVersion() {
        return streamFormatVersion;
    }
    abstract ObjectStreamField[] getFieldsNoCopy();
    public void writeFields()
        throws IOException {
        writeObjectState.defaultWriteObject(this);
        putFields.write(this);
    }
    public abstract org.omg.CORBA_2_3.portable.OutputStream getOrbStream();
    protected abstract void beginOptionalCustomData();
    protected WriteObjectState writeObjectState = NOT_IN_WRITE_OBJECT;
    protected void setState(WriteObjectState newState) {
        writeObjectState = newState;
    }
    protected static class WriteObjectState {
        public void enterWriteObject(OutputStreamHook stream) throws IOException {}
        public void exitWriteObject(OutputStreamHook stream) throws IOException {}
        public void defaultWriteObject(OutputStreamHook stream) throws IOException {}
        public void writeData(OutputStreamHook stream) throws IOException {}
    }
    protected static class DefaultState extends WriteObjectState {
        public void enterWriteObject(OutputStreamHook stream) throws IOException {
            stream.setState(IN_WRITE_OBJECT);
        }
    }
    protected static final WriteObjectState NOT_IN_WRITE_OBJECT = new DefaultState();
    protected static final WriteObjectState IN_WRITE_OBJECT = new InWriteObjectState();
    protected static final WriteObjectState WROTE_DEFAULT_DATA = new WroteDefaultDataState();
    protected static final WriteObjectState WROTE_CUSTOM_DATA = new WroteCustomDataState();
    protected static class InWriteObjectState extends WriteObjectState {
        public void enterWriteObject(OutputStreamHook stream) throws IOException {
            throw new IOException("Internal state failure: Entered writeObject twice");
        }
        public void exitWriteObject(OutputStreamHook stream) throws IOException {
            stream.getOrbStream().write_boolean(false);
            if (stream.getStreamFormatVersion() == 2)
                stream.getOrbStream().write_long(0);
            stream.setState(NOT_IN_WRITE_OBJECT);
        }
        public void defaultWriteObject(OutputStreamHook stream) throws IOException {
            stream.getOrbStream().write_boolean(true);
            stream.setState(WROTE_DEFAULT_DATA);
        }
        public void writeData(OutputStreamHook stream) throws IOException {
            stream.getOrbStream().write_boolean(false);
            stream.beginOptionalCustomData();
            stream.setState(WROTE_CUSTOM_DATA);
        }
    }
    protected static class WroteDefaultDataState extends InWriteObjectState {
        public void exitWriteObject(OutputStreamHook stream) throws IOException {
            if (stream.getStreamFormatVersion() == 2)
                stream.getOrbStream().write_long(0);
            stream.setState(NOT_IN_WRITE_OBJECT);
        }
        public void defaultWriteObject(OutputStreamHook stream) throws IOException {
            throw new IOException("Called defaultWriteObject/writeFields twice");
        }
        public void writeData(OutputStreamHook stream) throws IOException {
            stream.beginOptionalCustomData();
            stream.setState(WROTE_CUSTOM_DATA);
        }
    }
    protected static class WroteCustomDataState extends InWriteObjectState {
        public void exitWriteObject(OutputStreamHook stream) throws IOException {
            if (stream.getStreamFormatVersion() == 2)
                ((org.omg.CORBA.portable.ValueOutputStream)stream.getOrbStream()).end_value();
            stream.setState(NOT_IN_WRITE_OBJECT);
        }
        public void defaultWriteObject(OutputStreamHook stream) throws IOException {
            throw new IOException("Cannot call defaultWriteObject/writeFields after writing custom data in RMI-IIOP");
        }
        public void writeData(OutputStreamHook stream) throws IOException {}
    }
}
