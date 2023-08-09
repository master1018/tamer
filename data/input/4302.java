public class ValueHandlerImpl implements javax.rmi.CORBA.ValueHandlerMultiFormat {
    public static final String FORMAT_VERSION_PROPERTY
        = "com.sun.CORBA.MaxStreamFormatVersion";
    private static final byte MAX_SUPPORTED_FORMAT_VERSION = (byte)2;
    private static final byte STREAM_FORMAT_VERSION_1 = (byte)1;
    private static final byte MAX_STREAM_FORMAT_VERSION;
    static {
        MAX_STREAM_FORMAT_VERSION = getMaxStreamFormatVersion();
    }
    private static byte getMaxStreamFormatVersion() {
        try {
            String propValue = (String) AccessController.doPrivileged(
                                        new PrivilegedAction() {
                public java.lang.Object run() {
                    return System.getProperty(ValueHandlerImpl.FORMAT_VERSION_PROPERTY);
                }
            });
            if (propValue == null)
                return MAX_SUPPORTED_FORMAT_VERSION;
            byte result = Byte.parseByte(propValue);
            if (result < 1 || result > MAX_SUPPORTED_FORMAT_VERSION)
                throw new ExceptionInInitializerError("Invalid stream format version: "
                                                      + result
                                                      + ".  Valid range is 1 through "
                                                      + MAX_SUPPORTED_FORMAT_VERSION);
            return result;
        } catch (Exception ex) {
            Error err = new ExceptionInInitializerError(ex);
            err.initCause( ex ) ;
            throw err ;
        }
    }
    public static final short kRemoteType = 0;
    public static final short kAbstractType = 1;
    public static final short kValueType = 2;
    private Hashtable inputStreamPairs = null;
    private Hashtable outputStreamPairs = null;
    private CodeBase codeBase = null;
    private boolean useHashtables = true;
    private boolean isInputStream = true;
    private IIOPOutputStream outputStreamBridge = null;
    private IIOPInputStream inputStreamBridge = null;
    private OMGSystemException omgWrapper = OMGSystemException.get(
        CORBALogDomains.RPC_ENCODING ) ;
    private UtilSystemException utilWrapper = UtilSystemException.get(
        CORBALogDomains.RPC_ENCODING ) ;
    public byte getMaximumStreamFormatVersion() {
        return MAX_STREAM_FORMAT_VERSION;
    }
    public void writeValue(org.omg.CORBA.portable.OutputStream out,
                           java.io.Serializable value,
                           byte streamFormatVersion) {
        if (streamFormatVersion == 2) {
            if (!(out instanceof org.omg.CORBA.portable.ValueOutputStream)) {
                throw omgWrapper.notAValueoutputstream() ;
            }
        } else if (streamFormatVersion != 1) {
            throw omgWrapper.invalidStreamFormatVersion(
                new Integer(streamFormatVersion) ) ;
        }
        writeValueWithVersion(out, value, streamFormatVersion);
    }
    public ValueHandlerImpl(){}
    public ValueHandlerImpl(boolean isInputStream) {
        this();
        useHashtables = false;
        this.isInputStream = isInputStream;
    }
    public void writeValue(org.omg.CORBA.portable.OutputStream _out,
                           java.io.Serializable value) {
        writeValueWithVersion(_out, value, STREAM_FORMAT_VERSION_1);
    }
    private void writeValueWithVersion(org.omg.CORBA.portable.OutputStream _out,
                                       java.io.Serializable value,
                                       byte streamFormatVersion) {
        org.omg.CORBA_2_3.portable.OutputStream out =
            (org.omg.CORBA_2_3.portable.OutputStream) _out;
        if (!useHashtables) {
            if (outputStreamBridge == null) {
                outputStreamBridge = createOutputStream();
                outputStreamBridge.setOrbStream(out);
            }
            try {
                outputStreamBridge.increaseRecursionDepth();
                writeValueInternal(outputStreamBridge, out, value, streamFormatVersion);
            } finally {
                outputStreamBridge.decreaseRecursionDepth();
            }
            return;
        }
        IIOPOutputStream jdkToOrbOutputStreamBridge = null;
        if (outputStreamPairs == null)
            outputStreamPairs = new Hashtable();
        jdkToOrbOutputStreamBridge = (IIOPOutputStream)outputStreamPairs.get(_out);
        if (jdkToOrbOutputStreamBridge == null) {
            jdkToOrbOutputStreamBridge = createOutputStream();
            jdkToOrbOutputStreamBridge.setOrbStream(out);
            outputStreamPairs.put(_out, jdkToOrbOutputStreamBridge);
        }
        try {
            jdkToOrbOutputStreamBridge.increaseRecursionDepth();
            writeValueInternal(jdkToOrbOutputStreamBridge, out, value, streamFormatVersion);
        } finally {
            if (jdkToOrbOutputStreamBridge.decreaseRecursionDepth() == 0) {
                outputStreamPairs.remove(_out);
            }
        }
    }
    private void writeValueInternal(IIOPOutputStream bridge,
                                    org.omg.CORBA_2_3.portable.OutputStream out,
                                    java.io.Serializable value,
                                    byte streamFormatVersion)
    {
        Class clazz = value.getClass();
        if (clazz.isArray())
            write_Array(out, value, clazz.getComponentType());
        else
            bridge.simpleWriteObject(value, streamFormatVersion);
    }
    public java.io.Serializable readValue(org.omg.CORBA.portable.InputStream _in,
                                          int offset,
                                          java.lang.Class clazz,
                                          String repositoryID,
                                          org.omg.SendingContext.RunTime _sender)
    {
        CodeBase sender = CodeBaseHelper.narrow(_sender);
        org.omg.CORBA_2_3.portable.InputStream in =
            (org.omg.CORBA_2_3.portable.InputStream) _in;
        if (!useHashtables) {
            if (inputStreamBridge == null) {
                inputStreamBridge = createInputStream();
                inputStreamBridge.setOrbStream(in);
                inputStreamBridge.setSender(sender); 
                inputStreamBridge.setValueHandler(this);
            }
            java.io.Serializable result = null;
            try {
                inputStreamBridge.increaseRecursionDepth();
                result = (java.io.Serializable) readValueInternal(inputStreamBridge, in, offset, clazz, repositoryID, sender);
            } finally {
                if (inputStreamBridge.decreaseRecursionDepth() == 0) {
                }
            }
            return result;
        }
        IIOPInputStream jdkToOrbInputStreamBridge = null;
        if (inputStreamPairs == null)
            inputStreamPairs = new Hashtable();
        jdkToOrbInputStreamBridge = (IIOPInputStream)inputStreamPairs.get(_in);
        if (jdkToOrbInputStreamBridge == null) {
            jdkToOrbInputStreamBridge = createInputStream();
            jdkToOrbInputStreamBridge.setOrbStream(in);
            jdkToOrbInputStreamBridge.setSender(sender); 
            jdkToOrbInputStreamBridge.setValueHandler(this);
            inputStreamPairs.put(_in, jdkToOrbInputStreamBridge);
        }
        java.io.Serializable result = null;
        try {
            jdkToOrbInputStreamBridge.increaseRecursionDepth();
            result = (java.io.Serializable) readValueInternal(jdkToOrbInputStreamBridge, in, offset, clazz, repositoryID, sender);
        } finally {
            if (jdkToOrbInputStreamBridge.decreaseRecursionDepth() == 0) {
                inputStreamPairs.remove(_in);
            }
        }
        return result;
    }
    private java.io.Serializable readValueInternal(IIOPInputStream bridge,
                                                  org.omg.CORBA_2_3.portable.InputStream in,
                                                  int offset,
                                                  java.lang.Class clazz,
                                                  String repositoryID,
                                                  com.sun.org.omg.SendingContext.CodeBase sender)
    {
        java.io.Serializable result = null;
        if (clazz == null) {
            if (isArray(repositoryID)){
                read_Array(bridge, in, null, sender, offset);
            } else {
                bridge.simpleSkipObject(repositoryID, sender);
            }
            return result;
        }
        if (clazz.isArray()) {
            result = (java.io.Serializable)read_Array(bridge, in, clazz, sender, offset);
        } else {
            result = (java.io.Serializable)bridge.simpleReadObject(clazz, repositoryID, sender, offset);
        }
        return result;
    }
    public java.lang.String getRMIRepositoryID(java.lang.Class clz) {
        return RepositoryId.createForJavaType(clz);
    }
    public boolean isCustomMarshaled(java.lang.Class clz) {
        return ObjectStreamClass.lookup(clz).isCustomMarshaled();
    }
    public org.omg.SendingContext.RunTime getRunTimeCodeBase() {
        if (codeBase != null)
            return codeBase;
        else {
            codeBase = new FVDCodeBaseImpl();
            FVDCodeBaseImpl fvdImpl = (FVDCodeBaseImpl) codeBase;
            fvdImpl.setValueHandler(this);
            return codeBase;
        }
    }
     public boolean useFullValueDescription(Class clazz, String repositoryID)
        throws IOException
     {
        return RepositoryId.useFullValueDescription(clazz, repositoryID);
     }
     public String getClassName(String id)
     {
        RepositoryId repID = RepositoryId.cache.getId(id);
        return repID.getClassName();
     }
     public Class getClassFromType(String id)
        throws ClassNotFoundException
     {
        RepositoryId repId = RepositoryId.cache.getId(id);
        return repId.getClassFromType();
     }
     public Class getAnyClassFromType(String id)
        throws ClassNotFoundException
     {
        RepositoryId repId = RepositoryId.cache.getId(id);
        return repId.getAnyClassFromType();
     }
     public String createForAnyType(Class cl)
     {
        return RepositoryId.createForAnyType(cl);
     }
     public String getDefinedInId(String id)
     {
        RepositoryId repId = RepositoryId.cache.getId(id);
        return repId.getDefinedInId();
     }
     public String getUnqualifiedName(String id)
     {
        RepositoryId repId = RepositoryId.cache.getId(id);
        return repId.getUnqualifiedName();
     }
     public String getSerialVersionUID(String id)
     {
        RepositoryId repId = RepositoryId.cache.getId(id);
        return repId.getSerialVersionUID();
     }
     public boolean isAbstractBase(Class clazz)
     {
        return RepositoryId.isAbstractBase(clazz);
     }
     public boolean isSequence(String id)
     {
        RepositoryId repId = RepositoryId.cache.getId(id);
        return repId.isSequence();
     }
    public java.io.Serializable writeReplace(java.io.Serializable value) {
        return ObjectStreamClass.lookup(value.getClass()).writeReplace(value);
    }
    protected void writeCharArray(org.omg.CORBA_2_3.portable.OutputStream out,
                                char[] array,
                                int offset,
                                int length)
    {
        out.write_wchar_array(array, offset, length);
    }
    private void write_Array(org.omg.CORBA_2_3.portable.OutputStream out, java.io.Serializable obj, Class type) {
        int i, length;
        if (type.isPrimitive()) {
            if (type == Integer.TYPE) {
                int[] array = (int[])((Object)obj);
                length = array.length;
                out.write_ulong(length);
                out.write_long_array(array, 0, length);
            } else if (type == Byte.TYPE) {
                byte[] array = (byte[])((Object)obj);
                length = array.length;
                out.write_ulong(length);
                out.write_octet_array(array, 0, length);
            } else if (type == Long.TYPE) {
                long[] array = (long[])((Object)obj);
                length = array.length;
                out.write_ulong(length);
                out.write_longlong_array(array, 0, length);
            } else if (type == Float.TYPE) {
                float[] array = (float[])((Object)obj);
                length = array.length;
                out.write_ulong(length);
                out.write_float_array(array, 0, length);
            } else if (type == Double.TYPE) {
                double[] array = (double[])((Object)obj);
                length = array.length;
                out.write_ulong(length);
                out.write_double_array(array, 0, length);
            } else if (type == Short.TYPE) {
                short[] array = (short[])((Object)obj);
                length = array.length;
                out.write_ulong(length);
                out.write_short_array(array, 0, length);
            } else if (type == Character.TYPE) {
                char[] array = (char[])((Object)obj);
                length = array.length;
                out.write_ulong(length);
                writeCharArray(out, array, 0, length);
            } else if (type == Boolean.TYPE) {
                boolean[] array = (boolean[])((Object)obj);
                length = array.length;
                out.write_ulong(length);
                out.write_boolean_array(array, 0, length);
            } else {
                throw new Error("Invalid primitive type : " +
                    obj.getClass().getName());
            }
        } else if (type == java.lang.Object.class) {
            Object[] array = (Object[])((Object)obj);
            length = array.length;
            out.write_ulong(length);
            for (i = 0; i < length; i++) {
                Util.writeAny(out, array[i]);
            }
        } else {
            Object[] array = (Object[])((Object)obj);
            length = array.length;
            out.write_ulong(length);
            int callType = kValueType;
            if (type.isInterface()) {
                String className = type.getName();
                if (java.rmi.Remote.class.isAssignableFrom(type)) {
                    callType = kRemoteType;
                } else if (org.omg.CORBA.Object.class.isAssignableFrom(type)){
                    callType = kRemoteType;
                } else if (RepositoryId.isAbstractBase(type)) {
                    callType = kAbstractType;
                } else if (ObjectStreamClassCorbaExt.isAbstractInterface(type)) {
                    callType = kAbstractType;
                }
            }
            for (i = 0; i < length; i++) {
                switch (callType) {
                case kRemoteType:
                    Util.writeRemoteObject(out, array[i]);
                    break;
                case kAbstractType:
                    Util.writeAbstractObject(out,array[i]);
                    break;
                case kValueType:
                    try{
                        out.write_value((java.io.Serializable)array[i]);
                    } catch(ClassCastException cce){
                        if (array[i] instanceof java.io.Serializable)
                            throw cce;
                        else {
                            Utility.throwNotSerializableForCorba(
                                array[i].getClass().getName());
                        }
                    }
                    break;
                }
            }
        }
    }
    protected void readCharArray(org.omg.CORBA_2_3.portable.InputStream in,
                                 char[] array,
                                 int offset,
                                 int length)
    {
        in.read_wchar_array(array, offset, length);
    }
    private java.lang.Object read_Array(IIOPInputStream bridge,
                                        org.omg.CORBA_2_3.portable.InputStream in,
                                        Class sequence,
                                        com.sun.org.omg.SendingContext.CodeBase sender,
                                        int offset)
    {
        try {
            int length = in.read_ulong();
            int i;
            if (sequence == null) {
                for (i = 0; i < length; i++)
                    in.read_value();
                return null;
            }
            Class componentType = sequence.getComponentType();
            Class actualType = componentType;
            if (componentType.isPrimitive()) {
                if (componentType == Integer.TYPE) {
                    int[] array = new int[length];
                    in.read_long_array(array, 0, length);
                    return ((java.io.Serializable)((Object)array));
                } else if (componentType == Byte.TYPE) {
                    byte[] array = new byte[length];
                    in.read_octet_array(array, 0, length);
                    return ((java.io.Serializable)((Object)array));
                } else if (componentType == Long.TYPE) {
                    long[] array = new long[length];
                    in.read_longlong_array(array, 0, length);
                    return ((java.io.Serializable)((Object)array));
                } else if (componentType == Float.TYPE) {
                    float[] array = new float[length];
                    in.read_float_array(array, 0, length);
                    return ((java.io.Serializable)((Object)array));
                } else if (componentType == Double.TYPE) {
                    double[] array = new double[length];
                    in.read_double_array(array, 0, length);
                    return ((java.io.Serializable)((Object)array));
                } else if (componentType == Short.TYPE) {
                    short[] array = new short[length];
                    in.read_short_array(array, 0, length);
                    return ((java.io.Serializable)((Object)array));
                } else if (componentType == Character.TYPE) {
                    char[] array = new char[length];
                    readCharArray(in, array, 0, length);
                    return ((java.io.Serializable)((Object)array));
                } else if (componentType == Boolean.TYPE) {
                    boolean[] array = new boolean[length];
                    in.read_boolean_array(array, 0, length);
                    return ((java.io.Serializable)((Object)array));
                } else {
                    throw new Error("Invalid primitive componentType : " + sequence.getName());
                }
            } else if (componentType == java.lang.Object.class) {
                Object[] array = (Object[])java.lang.reflect.Array.newInstance(
                    componentType, length);
                bridge.activeRecursionMgr.addObject(offset, array);
                for (i = 0; i < length; i++) {
                    Object objectValue = null;
                    try {
                        objectValue = Util.readAny(in);
                    } catch(IndirectionException cdrie) {
                        try {
                            objectValue = bridge.activeRecursionMgr.getObject(
                                cdrie.offset);
                        } catch (IOException ie) {
                            throw utilWrapper.invalidIndirection( ie,
                                new Integer( cdrie.offset ) ) ;
                        }
                    }
                    array[i] = objectValue;
                }
                return ((java.io.Serializable)((Object)array));
            } else {
                Object[] array = (Object[])java.lang.reflect.Array.newInstance(
                    componentType, length);
                bridge.activeRecursionMgr.addObject(offset, array);
                int callType = kValueType;
                boolean narrow = false;
                if (componentType.isInterface()) {
                    boolean loadStubClass = false;
                    if (java.rmi.Remote.class.isAssignableFrom(componentType)) {
                        callType = kRemoteType;
                        loadStubClass = true;
                    } else if (org.omg.CORBA.Object.class.isAssignableFrom(componentType)){
                        callType = kRemoteType;
                        loadStubClass = true;
                    } else if (RepositoryId.isAbstractBase(componentType)) {
                        callType = kAbstractType;
                        loadStubClass = true;
                    } else if (ObjectStreamClassCorbaExt.isAbstractInterface(componentType)) {
                        callType = kAbstractType;
                    }
                    if (loadStubClass) {
                        try {
                            String codebase = Util.getCodebase(componentType);
                            String repID = RepositoryId.createForAnyType(componentType);
                            Class stubType =
                                Utility.loadStubClass(repID, codebase, componentType);
                            actualType = stubType;
                        } catch (ClassNotFoundException e) {
                            narrow = true;
                        }
                    } else {
                        narrow = true;
                    }
                }
                for (i = 0; i < length; i++) {
                    try {
                        switch (callType) {
                        case kRemoteType:
                            if (!narrow)
                                array[i] = (Object)in.read_Object(actualType);
                            else {
                                array[i] = Utility.readObjectAndNarrow(in, actualType);
                            }
                            break;
                        case kAbstractType:
                            if (!narrow)
                                array[i] = (Object)in.read_abstract_interface(actualType);
                            else {
                                array[i] = Utility.readAbstractAndNarrow(in, actualType);
                            }
                            break;
                        case kValueType:
                            array[i] = (Object)in.read_value(actualType);
                            break;
                        }
                    } catch(IndirectionException cdrie) {
                        try {
                            array[i] = bridge.activeRecursionMgr.getObject(
                                cdrie.offset);
                        } catch (IOException ioe) {
                            throw utilWrapper.invalidIndirection( ioe,
                                new Integer( cdrie.offset ) ) ;
                        }
                    }
                }
                return ((java.io.Serializable)((Object)array));
            }
        } finally {
            bridge.activeRecursionMgr.removeObject(offset);
        }
    }
    private boolean isArray(String repId){
        return RepositoryId.cache.getId(repId).isSequence();
    }
    protected String getOutputStreamClassName() {
        return "com.sun.corba.se.impl.io.IIOPOutputStream";
    }
   private IIOPOutputStream createOutputStream() {
        final String name = getOutputStreamClassName();
        try {
             IIOPOutputStream stream = createOutputStreamBuiltIn(name);
             if (stream != null) {
                 return stream;
             }
             return createCustom(IIOPOutputStream.class, name);
        } catch (Throwable t) {
            InternalError ie = new InternalError(
                "Error loading " + name
            );
                ie.initCause(t);
                throw ie;
        }
    }
    private IIOPOutputStream createOutputStreamBuiltIn(
        final String name
    ) throws Throwable {
        try {
            return AccessController.doPrivileged(
                new PrivilegedExceptionAction<IIOPOutputStream>() {
                    public IIOPOutputStream run() throws IOException {
                        return createOutputStreamBuiltInNoPriv(name);
                    }
                }
            );
        } catch (java.security.PrivilegedActionException exc) {
            throw exc.getCause();
        }
    }
    private IIOPOutputStream createOutputStreamBuiltInNoPriv(
        final String name
    ) throws IOException {
        return
            name.equals(
                IIOPOutputStream
                    .class.getName()
            ) ?
            new IIOPOutputStream() :
            name.equals(
                com.sun.corba.se.impl.orbutil.IIOPOutputStream_1_3
                    .class.getName()
            ) ?
            new com.sun.corba.se.impl.orbutil.IIOPOutputStream_1_3() :
            name.equals(
                com.sun.corba.se.impl.orbutil.IIOPOutputStream_1_3_1
                    .class.getName()
            ) ?
            new com.sun.corba.se.impl.orbutil.IIOPOutputStream_1_3_1() :
            null;
    }
    protected String getInputStreamClassName() {
        return "com.sun.corba.se.impl.io.IIOPInputStream";
    }
    private IIOPInputStream createInputStream() {
        final String name = getInputStreamClassName();
        try {
             IIOPInputStream stream = createInputStreamBuiltIn(name);
             if (stream != null) {
                 return stream;
             }
             return createCustom(IIOPInputStream.class, name);
        } catch (Throwable t) {
            InternalError ie = new InternalError(
                "Error loading " + name
            );
                ie.initCause(t);
                throw ie;
        }
    }
     private IIOPInputStream createInputStreamBuiltIn(
         final String name
     ) throws Throwable {
         try {
             return AccessController.doPrivileged(
                 new PrivilegedExceptionAction<IIOPInputStream>() {
                     public IIOPInputStream run() throws IOException {
                         return createInputStreamBuiltInNoPriv(name);
                     }
                 }
             );
         } catch (java.security.PrivilegedActionException exc) {
             throw exc.getCause();
         }
     }
     private IIOPInputStream createInputStreamBuiltInNoPriv(
         final String name
     ) throws IOException {
         return
             name.equals(
                 IIOPInputStream
                     .class.getName()
             ) ?
             new IIOPInputStream() :
             name.equals(
                 com.sun.corba.se.impl.orbutil.IIOPInputStream_1_3
                     .class.getName()
             ) ?
             new com.sun.corba.se.impl.orbutil.IIOPInputStream_1_3() :
             name.equals(
                 com.sun.corba.se.impl.orbutil.IIOPInputStream_1_3_1
                     .class.getName()
             ) ?
             new com.sun.corba.se.impl.orbutil.IIOPInputStream_1_3_1() :
             null;
     }
     private <T> T createCustom(
         final Class<T> type, final String className
     ) throws Throwable {
                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                if (cl == null)
                    cl = ClassLoader.getSystemClassLoader();
                Class<?> clazz = cl.loadClass(className);
                Class<? extends T> streamClass = clazz.asSubclass(type);
                return streamClass.newInstance();
    }
    protected TCKind getJavaCharTCKind() {
        return TCKind.tk_wchar;
    }
}
