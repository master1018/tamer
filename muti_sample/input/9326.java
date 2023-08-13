class XObjectInputStream extends AbstractObjectInputStream {
    XObjectInputStream(InputStream in)
        throws IOException, StreamCorruptedException
        {
            super(in);
            dis = new DataInputStream(in);
        }
    final public void defaultReadObject()
        throws IOException, ClassNotFoundException, NotActiveException
    {
    }
    final protected Object readObjectOverride()
        throws OptionalDataException, ClassNotFoundException, IOException {
        Object readResult = null;
        Object prevObject = currentObject;
        Class  prevDesc   = currentClassDescriptor;
        boolean NotImplemented = true;
        if (NotImplemented)
            throw new IOException("readObjectOverride not implemented");
        try {
            currentObject = null;
            String className = dis.readUTF();
            currentClassDescriptor = Class.forName(className);
            try {
                currentObject =
                    allocateNewObject(currentClassDescriptor,
                                   currentClassDescriptor);
            } catch (InstantiationException e) {
                throw new InvalidClassException(currentClassDescriptor.getName(),
                                                e.getMessage());
            } catch (IllegalAccessException e) {
                throw new InvalidClassException(currentClassDescriptor.getName(),
                                                e.getMessage());
            }
        } finally {
            readResult = currentObject;
            currentObject = prevObject;
        }
        return readResult;
    }
    public ObjectInputStream.GetField readFields()
        throws IOException, ClassNotFoundException, NotActiveException {
            throw new Error("not implememted");
    }
    public synchronized void registerValidation(ObjectInputValidation obj,
                                                int prio)
        throws NotActiveException, InvalidObjectException {
    }
    public int read() throws IOException {
        return dis.read();
    }
    public int read(byte[] data, int offset, int length) throws IOException {
        return dis.read(data, offset, length);
    }
    public int available() throws IOException {
        return in.available();
    }
    public boolean readBoolean() throws IOException {
        throw new IOException("Not Implemented");
    }
    public byte readByte() throws IOException {
        throw new IOException("Not Implemented");
    }
    public int readUnsignedByte()  throws IOException {
        throw new IOException("Not Implemented");
    }
    public short readShort()  throws IOException {
        throw new IOException("Not Implemented");
    }
    public int readUnsignedShort() throws IOException {
        throw new IOException("Not Implemented");
    }
    public char readChar()  throws IOException {
        throw new IOException("Not Implemented");
    }
    public int readInt()  throws IOException {
        throw new IOException("Not Implemented");
    }
    public long readLong()  throws IOException {
        throw new IOException("Not Implemented");
    }
    public float readFloat() throws IOException {
        throw new IOException("Not Implemented");
    }
    public double readDouble() throws IOException {
        throw new IOException("Not Implemented");
    }
    public void readFully(byte[] data) throws IOException {
        throw new IOException("Not Implemented");
    }
    public void readFully(byte[] data, int offset, int size) throws IOException {
        throw new IOException("Not Implemented");
    }
    public int skipBytes(int len) throws IOException {
        throw new IOException("Not Implemented");
    }
    public String readLine() throws IOException {
        throw new IOException("Not Implemented");
    }
    public String readUTF() throws IOException {
        throw new IOException("Not Implemented");
    }
    public void close() throws IOException {
        in.close();
    }
     public static class InternalGetField extends ObjectInputStream.GetField {
        public ObjectStreamClass getObjectStreamClass() {
            throw new Error("not implemented");
        }
        public boolean defaulted(String name)
            throws IOException, IllegalArgumentException
        {
            throw new Error("not implemented");
        }
         public boolean get(String name, boolean defvalue)
             throws IOException, IllegalArgumentException {
             throw new Error("not implemented");
         }
         public char get(String name, char defvalue)
             throws IOException, IllegalArgumentException {
             throw new Error("not implemented");
         }
         public byte get(String name, byte defvalue)
             throws IOException, IllegalArgumentException {
             throw new Error("not implemented");
         }
         public short get(String name, short defvalue)
             throws IOException, IllegalArgumentException {
             throw new Error("not implemented");
         }
         public int get(String name, int defvalue)
             throws IOException, IllegalArgumentException {
             throw new Error("not implemented");
         }
         public long get(String name, long defvalue)
             throws IOException, IllegalArgumentException {
             throw new Error("not implemented");
         }
         public float get(String name, float defvalue)
             throws IOException, IllegalArgumentException {
             throw new Error("not implemented");
         }
         public double get(String name, double defvalue)
             throws IOException, IllegalArgumentException {
             throw new Error("not implemented");
         }
         public Object get(String name, Object defvalue)
             throws IOException, IllegalArgumentException {
             throw new Error("not implemented");
         }
         public void read(ObjectInputStream in)
             throws IOException, ClassNotFoundException {
         }
     }
    private Object currentObject;
    private Class currentClassDescriptor;
    static public Method getReadObjectMethod(final Class cl) {
        Method readObjectMethod = (Method)
            java.security.AccessController.doPrivileged
            (new java.security.PrivilegedAction() {
                public Object run() {
                    Method m = null;
                    try {
                        Class[] args = {ObjectInputStream.class};
                        m = cl.getDeclaredMethod("readObject", args);
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
        return readObjectMethod;
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
    protected boolean enableResolveObject(boolean enable)
        throws SecurityException
    {
        throw new Error("To be implemented");
    }
    private DataInputStream dis;
};
