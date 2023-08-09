public class IIOPOutputStream
    extends com.sun.corba.se.impl.io.OutputStreamHook
{
    private UtilSystemException wrapper = UtilSystemException.get(
        CORBALogDomains.RPC_ENCODING ) ;
    private static Bridge bridge =
        (Bridge)AccessController.doPrivileged(
            new PrivilegedAction() {
                public Object run() {
                    return Bridge.get() ;
                }
            }
        ) ;
    private org.omg.CORBA_2_3.portable.OutputStream orbStream;
    private Object currentObject = null;
    private ObjectStreamClass currentClassDesc = null;
    private int recursionDepth = 0;
    private int simpleWriteDepth = 0;
    private IOException abortIOException = null;
    private java.util.Stack classDescStack = new java.util.Stack();
    private Object[] writeObjectArgList = {this};
    public IIOPOutputStream()
        throws java.io.IOException
   {
        super();
    }
    protected void beginOptionalCustomData() {
        if (streamFormatVersion == 2) {
            org.omg.CORBA.portable.ValueOutputStream vout
                = (org.omg.CORBA.portable.ValueOutputStream)orbStream;
            vout.start_value(currentClassDesc.getRMIIIOPOptionalDataRepId());
        }
    }
    public final void setOrbStream(org.omg.CORBA_2_3.portable.OutputStream os) {
        orbStream = os;
    }
    public final org.omg.CORBA_2_3.portable.OutputStream getOrbStream() {
        return orbStream;
    }
    public final void increaseRecursionDepth(){
        recursionDepth++;
    }
    public final int decreaseRecursionDepth(){
        return --recursionDepth;
    }
    public final void writeObjectOverride(Object obj)
        throws IOException
    {
        writeObjectState.writeData(this);
        Util.writeAbstractObject((OutputStream)orbStream, obj);
    }
    public final void simpleWriteObject(Object obj, byte formatVersion)
    {
        byte oldStreamFormatVersion = streamFormatVersion;
        streamFormatVersion = formatVersion;
        Object prevObject = currentObject;
        ObjectStreamClass prevClassDesc = currentClassDesc;
        simpleWriteDepth++;
        try {
            outputObject(obj);
        } catch (IOException ee) {
            if (abortIOException == null)
                abortIOException = ee;
        } finally {
            streamFormatVersion = oldStreamFormatVersion;
            simpleWriteDepth--;
            currentObject = prevObject;
            currentClassDesc = prevClassDesc;
        }
        IOException pending = abortIOException;
        if (simpleWriteDepth == 0)
            abortIOException = null;
        if (pending != null) {
            bridge.throwException( pending ) ;
        }
    }
    ObjectStreamField[] getFieldsNoCopy() {
        return currentClassDesc.getFieldsNoCopy();
    }
    public final void defaultWriteObjectDelegate()
    {
        try {
            if (currentObject == null || currentClassDesc == null)
                throw new NotActiveException("defaultWriteObjectDelegate");
            ObjectStreamField[] fields =
                currentClassDesc.getFieldsNoCopy();
            if (fields.length > 0) {
                outputClassFields(currentObject, currentClassDesc.forClass(),
                                  fields);
            }
        } catch(IOException ioe) {
            bridge.throwException(ioe);
        }
    }
    public final boolean enableReplaceObjectDelegate(boolean enable)
    {
        return false;
    }
    protected final void annotateClass(Class<?> cl) throws IOException{
        throw new IOException("Method annotateClass not supported");
    }
    public final void close() throws IOException{
    }
    protected final void drain() throws IOException{
    }
    public final void flush() throws IOException{
        try{
            orbStream.flush();
        } catch(Error e) {
            IOException ioexc = new IOException(e.getMessage());
            ioexc.initCause(e) ;
            throw ioexc ;
        }
    }
    protected final Object replaceObject(Object obj) throws IOException{
        throw new IOException("Method replaceObject not supported");
    }
    public final void reset() throws IOException{
        try{
            if (currentObject != null || currentClassDesc != null)
                throw new IOException("Illegal call to reset");
            abortIOException = null;
            if (classDescStack == null)
                classDescStack = new java.util.Stack();
            else
                classDescStack.setSize(0);
        } catch(Error e) {
            IOException ioexc = new IOException(e.getMessage());
            ioexc.initCause(e) ;
            throw ioexc ;
        }
    }
    public final void write(byte b[]) throws IOException{
        try{
            writeObjectState.writeData(this);
            orbStream.write_octet_array(b, 0, b.length);
        } catch(Error e) {
            IOException ioexc = new IOException(e.getMessage());
            ioexc.initCause(e) ;
            throw ioexc ;
        }
    }
    public final void write(byte b[], int off, int len) throws IOException{
        try{
            writeObjectState.writeData(this);
            orbStream.write_octet_array(b, off, len);
        } catch(Error e) {
            IOException ioexc = new IOException(e.getMessage());
            ioexc.initCause(e) ;
            throw ioexc ;
        }
    }
    public final void write(int data) throws IOException{
        try{
            writeObjectState.writeData(this);
            orbStream.write_octet((byte)(data & 0xFF));
        } catch(Error e) {
            IOException ioexc = new IOException(e.getMessage());
            ioexc.initCause(e) ;
            throw ioexc ;
        }
    }
    public final void writeBoolean(boolean data) throws IOException{
        try{
            writeObjectState.writeData(this);
            orbStream.write_boolean(data);
        } catch(Error e) {
            IOException ioexc = new IOException(e.getMessage());
            ioexc.initCause(e) ;
            throw ioexc ;
        }
    }
    public final void writeByte(int data) throws IOException{
        try{
            writeObjectState.writeData(this);
            orbStream.write_octet((byte)data);
        } catch(Error e) {
            IOException ioexc = new IOException(e.getMessage());
            ioexc.initCause(e) ;
            throw ioexc ;
        }
    }
    public final void writeBytes(String data) throws IOException{
        try{
            writeObjectState.writeData(this);
            byte buf[] = data.getBytes();
            orbStream.write_octet_array(buf, 0, buf.length);
        } catch(Error e) {
            IOException ioexc = new IOException(e.getMessage());
            ioexc.initCause(e) ;
            throw ioexc ;
        }
    }
    public final void writeChar(int data) throws IOException{
        try{
            writeObjectState.writeData(this);
            orbStream.write_wchar((char)data);
        } catch(Error e) {
            IOException ioexc = new IOException(e.getMessage());
            ioexc.initCause(e) ;
            throw ioexc ;
        }
    }
    public final void writeChars(String data) throws IOException{
        try{
            writeObjectState.writeData(this);
            char buf[] = data.toCharArray();
            orbStream.write_wchar_array(buf, 0, buf.length);
        } catch(Error e) {
            IOException ioexc = new IOException(e.getMessage());
            ioexc.initCause(e) ;
            throw ioexc ;
        }
    }
    public final void writeDouble(double data) throws IOException{
        try{
            writeObjectState.writeData(this);
            orbStream.write_double(data);
        } catch(Error e) {
            IOException ioexc = new IOException(e.getMessage());
            ioexc.initCause(e) ;
            throw ioexc ;
        }
    }
    public final void writeFloat(float data) throws IOException{
        try{
            writeObjectState.writeData(this);
            orbStream.write_float(data);
        } catch(Error e) {
            IOException ioexc = new IOException(e.getMessage());
            ioexc.initCause(e) ;
            throw ioexc ;
        }
    }
    public final void writeInt(int data) throws IOException{
        try{
            writeObjectState.writeData(this);
            orbStream.write_long(data);
        } catch(Error e) {
            IOException ioexc = new IOException(e.getMessage());
            ioexc.initCause(e) ;
            throw ioexc ;
        }
    }
    public final void writeLong(long data) throws IOException{
        try{
            writeObjectState.writeData(this);
            orbStream.write_longlong(data);
        } catch(Error e) {
            IOException ioexc = new IOException(e.getMessage());
            ioexc.initCause(e) ;
            throw ioexc ;
        }
    }
    public final void writeShort(int data) throws IOException{
        try{
            writeObjectState.writeData(this);
            orbStream.write_short((short)data);
        } catch(Error e) {
            IOException ioexc = new IOException(e.getMessage());
            ioexc.initCause(e) ;
            throw ioexc ;
        }
    }
    protected final void writeStreamHeader() throws IOException{
    }
    protected void internalWriteUTF(org.omg.CORBA.portable.OutputStream stream,
                                    String data)
    {
        stream.write_wstring(data);
    }
    public final void writeUTF(String data) throws IOException{
        try{
            writeObjectState.writeData(this);
            internalWriteUTF(orbStream, data);
        } catch(Error e) {
            IOException ioexc = new IOException(e.getMessage());
            ioexc.initCause(e) ;
            throw ioexc ;
        }
    }
    private boolean checkSpecialClasses(Object obj) throws IOException {
        if (obj instanceof ObjectStreamClass) {
            throw new IOException("Serialization of ObjectStreamClass not supported");
        }
        return false;
    }
    private boolean checkSubstitutableSpecialClasses(Object obj)
        throws IOException
    {
        if (obj instanceof String) {
            orbStream.write_value((java.io.Serializable)obj);
            return true;
        }
        return false;
    }
    private void outputObject(final Object obj) throws IOException{
        currentObject = obj;
        Class currclass = obj.getClass();
        currentClassDesc = ObjectStreamClass.lookup(currclass);
        if (currentClassDesc == null) {
            throw new NotSerializableException(currclass.getName());
        }
        if (currentClassDesc.isExternalizable()) {
            orbStream.write_octet(streamFormatVersion);
            Externalizable ext = (Externalizable)obj;
            ext.writeExternal(this);
        } else {
            int stackMark = classDescStack.size();
            try {
                ObjectStreamClass next;
                while ((next = currentClassDesc.getSuperclass()) != null) {
                    classDescStack.push(currentClassDesc);
                    currentClassDesc = next;
                }
                do {
                    WriteObjectState oldState = writeObjectState;
                    try {
                        setState(NOT_IN_WRITE_OBJECT);
                        if (currentClassDesc.hasWriteObject()) {
                            invokeObjectWriter(currentClassDesc, obj );
                        } else {
                            defaultWriteObjectDelegate();
                        }
                    } finally {
                        setState(oldState);
                    }
                } while (classDescStack.size() > stackMark &&
                         (currentClassDesc = (ObjectStreamClass)classDescStack.pop()) != null);
            } finally {
                classDescStack.setSize(stackMark);
            }
        }
    }
    private void invokeObjectWriter(ObjectStreamClass osc, Object obj)
        throws IOException
    {
        Class c = osc.forClass() ;
        try {
            orbStream.write_octet(streamFormatVersion);
            writeObjectState.enterWriteObject(this);
            osc.writeObjectMethod.invoke( obj, writeObjectArgList ) ;
            writeObjectState.exitWriteObject(this);
        } catch (InvocationTargetException e) {
            Throwable t = e.getTargetException();
            if (t instanceof IOException)
                throw (IOException)t;
            else if (t instanceof RuntimeException)
                throw (RuntimeException) t;
            else if (t instanceof Error)
                throw (Error) t;
            else
                throw new Error("invokeObjectWriter internal error",e);
        } catch (IllegalAccessException e) {
        }
    }
    void writeField(ObjectStreamField field, Object value) throws IOException {
        switch (field.getTypeCode()) {
            case 'B':
                if (value == null)
                    orbStream.write_octet((byte)0);
                else
                    orbStream.write_octet(((Byte)value).byteValue());
                break;
            case 'C':
                if (value == null)
                    orbStream.write_wchar((char)0);
                else
                    orbStream.write_wchar(((Character)value).charValue());
                break;
            case 'F':
                if (value == null)
                    orbStream.write_float((float)0);
                else
                    orbStream.write_float(((Float)value).floatValue());
                break;
            case 'D':
                if (value == null)
                    orbStream.write_double((double)0);
                else
                    orbStream.write_double(((Double)value).doubleValue());
                break;
            case 'I':
                if (value == null)
                    orbStream.write_long((int)0);
                else
                    orbStream.write_long(((Integer)value).intValue());
                break;
            case 'J':
                if (value == null)
                    orbStream.write_longlong((long)0);
                else
                    orbStream.write_longlong(((Long)value).longValue());
                break;
            case 'S':
                if (value == null)
                    orbStream.write_short((short)0);
                else
                    orbStream.write_short(((Short)value).shortValue());
                break;
            case 'Z':
                if (value == null)
                    orbStream.write_boolean(false);
                else
                    orbStream.write_boolean(((Boolean)value).booleanValue());
                break;
            case '[':
            case 'L':
                writeObjectField(field, value);
                break;
            default:
                throw new InvalidClassException(currentClassDesc.getName());
            }
    }
    private void writeObjectField(ObjectStreamField field,
                                  Object objectValue) throws IOException {
        if (ObjectStreamClassCorbaExt.isAny(field.getTypeString())) {
            javax.rmi.CORBA.Util.writeAny(orbStream, objectValue);
        }
        else {
            Class type = field.getType();
            int callType = ValueHandlerImpl.kValueType;
            if (type.isInterface()) {
                String className = type.getName();
                if (java.rmi.Remote.class.isAssignableFrom(type)) {
                    callType = ValueHandlerImpl.kRemoteType;
                } else if (org.omg.CORBA.Object.class.isAssignableFrom(type)){
                    callType = ValueHandlerImpl.kRemoteType;
                } else if (RepositoryId.isAbstractBase(type)) {
                    callType = ValueHandlerImpl.kAbstractType;
                } else if (ObjectStreamClassCorbaExt.isAbstractInterface(type)) {
                    callType = ValueHandlerImpl.kAbstractType;
                }
            }
            switch (callType) {
            case ValueHandlerImpl.kRemoteType:
                Util.writeRemoteObject(orbStream, objectValue);
                break;
            case ValueHandlerImpl.kAbstractType:
                Util.writeAbstractObject(orbStream, objectValue);
                break;
            case ValueHandlerImpl.kValueType:
                try{
                    orbStream.write_value((java.io.Serializable)objectValue, type);
                }
                catch(ClassCastException cce){
                    if (objectValue instanceof java.io.Serializable)
                        throw cce;
                    else
                        Utility.throwNotSerializableForCorba(objectValue.getClass().getName());
                }
            }
        }
    }
    private void outputClassFields(Object o, Class cl,
                                   ObjectStreamField[] fields)
        throws IOException, InvalidClassException {
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].getField() == null)
                throw new InvalidClassException(cl.getName(),
                                                "Nonexistent field " + fields[i].getName());
            try {
                switch (fields[i].getTypeCode()) {
                    case 'B':
                        byte byteValue = fields[i].getField().getByte( o ) ;
                        orbStream.write_octet(byteValue);
                        break;
                    case 'C':
                        char charValue = fields[i].getField().getChar( o ) ;
                        orbStream.write_wchar(charValue);
                        break;
                    case 'F':
                        float floatValue = fields[i].getField().getFloat( o ) ;
                        orbStream.write_float(floatValue);
                        break;
                    case 'D' :
                        double doubleValue = fields[i].getField().getDouble( o ) ;
                        orbStream.write_double(doubleValue);
                        break;
                    case 'I':
                        int intValue = fields[i].getField().getInt( o ) ;
                        orbStream.write_long(intValue);
                        break;
                    case 'J':
                        long longValue = fields[i].getField().getLong( o ) ;
                        orbStream.write_longlong(longValue);
                        break;
                    case 'S':
                        short shortValue = fields[i].getField().getShort( o ) ;
                        orbStream.write_short(shortValue);
                        break;
                    case 'Z':
                        boolean booleanValue = fields[i].getField().getBoolean( o ) ;
                        orbStream.write_boolean(booleanValue);
                        break;
                    case '[':
                    case 'L':
                        Object objectValue = fields[i].getField().get( o ) ;
                        writeObjectField(fields[i], objectValue);
                        break;
                    default:
                        throw new InvalidClassException(cl.getName());
                }
            } catch (IllegalAccessException exc) {
                throw wrapper.illegalFieldAccess( exc, fields[i].getName() ) ;
            }
        }
    }
}
