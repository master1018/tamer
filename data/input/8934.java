public class XObjectOutputStream extends AbstractObjectOutputStream {
    XObjectOutputStream(OutputStream out) throws IOException {
        super(out);
    }
    protected boolean enableReplaceObject(boolean enable)
    {
        throw new Error("not implemented");
    }
    protected void annotateClass(Class<?> cl) throws IOException {
    }
    public void close() throws IOException{
        out.close();
    }
    protected Object replaceObject(Object obj) throws IOException {
        return obj;
    }
    protected void writeStreamHeader() throws IOException {
        super.writeStreamHeader();
    }
    final protected void writeObjectOverride(Object obj) throws IOException {
        Object prevCurrentObject = currentObject;
        currentObject = obj;
        System.out.println("writeObjectOverride(" + obj.toString() + ")");
        try {
        Method writeObjectMethod = getWriteObjectMethod(obj.getClass());
        if (writeObjectMethod != null) {
            Object[] arglist = {this};
            invokeMethod(obj, writeObjectMethod, arglist);
        } else
            defaultWriteObject();
        } finally {
            currentObject = prevCurrentObject;
        }
    }
    public void defaultWriteObject() throws IOException {
        Object obj = currentObject;
        System.out.println("XObjectOutputStream.defaultWriteObject(" +
                            obj.toString() + ")");
        Field[] fields = obj.getClass().getFields();
        for (int i= 0; i < fields.length; i++) {
            int mods = fields[i].getModifiers();
            if (Modifier.isStatic(mods) || Modifier.isTransient(mods))
                continue;
            Class FieldType = fields[i].getType();
            if (FieldType.isPrimitive()) {
                System.out.println("Field " + fields[i].getName() +
                                    " has primitive type " + FieldType.toString());
            } else {
                System.out.println("**Field " + fields[i].getName() +
                                   " is an Object of type " + FieldType.toString());
                try {
                    writeObject(fields[i].get(obj));
                    if (FieldType.isArray()) {
                        Object[] array = ((Object[]) fields[i].get(obj));
                        Class componentType = FieldType.getComponentType();
                        if (componentType.isPrimitive())
                            System.out.println("Output " + array.length + " primitive elements of" +
                                               componentType.toString());
                        else {
                            System.out.println("Output " + array.length + " of Object elements of" +
                                               componentType.toString());
                            for (int k = 0; k < array.length; k++) {
                                writeObject(array[k]);
                            }
                        }
                    }
                } catch (IllegalAccessException e) {
                    throw new IOException(e.getMessage());
                }
            }
        }
    }
    public PutField putFields() throws IOException {
        currentPutField = new InternalPutField();
        return currentPutField;
    }
    public void writeFields() throws IOException {
        currentPutField.write(this);
    }
    static final class InternalPutField extends ObjectOutputStream.PutField {
        String fieldName[];
        int    intValue[];
        int next;
        InternalPutField() {
            fieldName = new String[10];
            intValue = new int[10];
            next = 0;
        }
        public void put(String name, boolean value) {
        }
        public void put(String name, char value) {
        }
        public void put(String name, byte value) {
        }
        public void put(String name, short value) {
        }
        public void put(String name, int value) {
            if (next < fieldName.length) {
                fieldName[next] = name;
                intValue[next] = value;
                next++;
            }
        }
        public void put(String name, long value) {
        }
        public void put(String name, float value) {
        }
        public void put(String name, double value) {
        }
        public void put(String name, Object value) {
        }
        public void write(ObjectOutput out) throws IOException {
            for (int i = 0; i < next; i++)
                System.out.println(fieldName[i] + "=" + intValue[i]);
        }
    };
    public void write(int data) throws IOException {
    }
    public void write(byte b[]) throws IOException {
    }
    public void write(byte b[], int off, int len) throws IOException {
    }
    public void writeBoolean(boolean data) throws IOException {
    }
    public void writeByte(int data) throws IOException {
    }
    public void writeShort(int data)  throws IOException {
    }
    public void writeChar(int data)  throws IOException {
    }
    public void writeInt(int data)  throws IOException{}
    public void writeLong(long data)  throws IOException{}
    public void writeFloat(float data) throws IOException{}
    public void writeDouble(double data) throws IOException{}
    public void writeBytes(String data) throws IOException{}
    public void writeChars(String data) throws IOException{}
    public void writeUTF(String data) throws IOException{}
    public void reset() throws IOException {}
    public void available() throws IOException {}
    public void drain() throws IOException {}
    private Object currentObject = null;
    private InternalPutField currentPutField;
    static public Method getWriteObjectMethod(final Class cl) {
        Method writeObjectMethod = (Method)
            java.security.AccessController.doPrivileged
            (new java.security.PrivilegedAction() {
                public Object run() {
                    Method m = null;
                    try {
                        Class[] args = {ObjectOutputStream.class};
                        m = cl.getDeclaredMethod("writeObject", args);
                        int mods = m.getModifiers();
                        if (!Modifier.isPrivate(mods) ||
                            Modifier.isStatic(mods)) {
                            m = null;
                        } else {
                            m.setAccessible(true);
                        }
                    } catch (NoSuchMethodException e) {
                        m = null;
                    }
                    return m;
                }
            });
        return writeObjectMethod;
    }
    static private void invokeMethod(final Object obj, final Method m,
                                        final Object[] argList)
        throws IOException
    {
        try {
            java.security.AccessController.doPrivileged
                (new java.security.PrivilegedExceptionAction() {
                    public Object run() throws InvocationTargetException,
                                        java.lang.IllegalAccessException {
                        m.invoke(obj, argList);
                        return null;
                    }
                });
        } catch (java.security.PrivilegedActionException e) {
            Exception ex = e.getException();
            if (ex instanceof InvocationTargetException) {
                Throwable t =
                        ((InvocationTargetException)ex).getTargetException();
                if (t instanceof IOException)
                    throw (IOException)t;
                else if (t instanceof RuntimeException)
                    throw (RuntimeException) t;
                else if (t instanceof Error)
                    throw (Error) t;
                else
                    throw new Error("interal error");
            } else {
            }
        }
    }
};
