public class ObjectStreamClass_1_3_1 implements java.io.Serializable {
    public static final long kDefaultUID = -1;
    private static Object noArgsList[] = {};
    private static Class noTypesList[] = {};
    private static Hashtable translatedFields;
    static final ObjectStreamClass_1_3_1 lookup(Class cl)
    {
        ObjectStreamClass_1_3_1 desc = lookupInternal(cl);
        if (desc.isSerializable() || desc.isExternalizable())
            return desc;
        return null;
    }
    static ObjectStreamClass_1_3_1 lookupInternal(Class cl)
    {
        ObjectStreamClass_1_3_1 desc = null;
        synchronized (descriptorFor) {
            desc = findDescriptorFor(cl);
            if (desc != null) {
                return desc;
            }
                boolean serializable = classSerializable.isAssignableFrom(cl);
                ObjectStreamClass_1_3_1 superdesc = null;
                if (serializable) {
                    Class superclass = cl.getSuperclass();
                    if (superclass != null)
                        superdesc = lookup(superclass);
                }
                boolean externalizable = false;
                if (serializable) {
                    externalizable =
                        ((superdesc != null) && superdesc.isExternalizable()) ||
                        classExternalizable.isAssignableFrom(cl);
                    if (externalizable) {
                        serializable = false;
                    }
                }
            desc = new ObjectStreamClass_1_3_1(cl, superdesc,
                                         serializable, externalizable);
        }
        desc.init();
        return desc;
    }
    public final String getName() {
        return name;
    }
    public static final long getSerialVersionUID( java.lang.Class clazz) {
        ObjectStreamClass_1_3_1 theosc = ObjectStreamClass_1_3_1.lookup( clazz );
        if( theosc != null )
        {
                return theosc.getSerialVersionUID( );
        }
        return 0;
    }
    public final long getSerialVersionUID() {
        return suid;
    }
    public final String getSerialVersionUIDStr() {
        if (suidStr == null)
            suidStr = Long.toHexString(suid).toUpperCase();
        return suidStr;
    }
    public static final long getActualSerialVersionUID( java.lang.Class clazz )
    {
        ObjectStreamClass_1_3_1 theosc = ObjectStreamClass_1_3_1.lookup( clazz );
        if( theosc != null )
        {
                return theosc.getActualSerialVersionUID( );
        }
        return 0;
    }
    public final long getActualSerialVersionUID() {
        return actualSuid;
    }
    public final String getActualSerialVersionUIDStr() {
        if (actualSuidStr == null)
            actualSuidStr = Long.toHexString(actualSuid).toUpperCase();
        return actualSuidStr;
    }
    public final Class forClass() {
        return ofClass;
    }
    public ObjectStreamField[] getFields() {
        if (fields.length > 0) {
            ObjectStreamField[] dup = new ObjectStreamField[fields.length];
            System.arraycopy(fields, 0, dup, 0, fields.length);
            return dup;
        } else {
            return fields;
        }
    }
    public boolean hasField(ValueMember field){
        for (int i = 0; i < fields.length; i++){
            try{
                if (fields[i].getName().equals(field.name)) {
                    if (fields[i].getSignature().equals(ValueUtility.getSignature(field)))
                        return true;
                }
            }
            catch(Throwable t){}
        }
        return false;
    }
    final ObjectStreamField[] getFieldsNoCopy() {
        return fields;
    }
    public final ObjectStreamField getField(String name) {
        for (int i = fields.length-1; i >= 0; i--) {
            if (name.equals(fields[i].getName())) {
                return fields[i];
            }
        }
        return null;
    }
    public Serializable writeReplace(Serializable value) {
        if (writeReplaceObjectMethod != null) {
            try {
                return (Serializable) writeReplaceObjectMethod.invoke(value,noArgsList);
            }
            catch(Throwable t) {
                throw new RuntimeException(t.getMessage());
            }
        }
        else return value;
    }
    public Object readResolve(Object value) {
        if (readResolveObjectMethod != null) {
            try {
                return readResolveObjectMethod.invoke(value,noArgsList);
            }
            catch(Throwable t) {
                throw new RuntimeException(t.getMessage());
            }
        }
        else return value;
    }
    public final String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(name);
        sb.append(": static final long serialVersionUID = ");
        sb.append(Long.toString(suid));
        sb.append("L;");
        return sb.toString();
    }
    private ObjectStreamClass_1_3_1(java.lang.Class cl, ObjectStreamClass_1_3_1 superdesc,
                              boolean serial, boolean extern)
    {
        ofClass = cl;           
        if (Proxy.isProxyClass(cl)) {
            forProxyClass = true;
        }
        name = cl.getName();
        superclass = superdesc;
        serializable = serial;
        if (!forProxyClass) {
            externalizable = extern;
        }
        insertDescriptorFor(this);
    }
    private void init() {
      synchronized (lock) {
        final Class cl = ofClass;
        if (fields != null) 
                return;
        if (!serializable ||
            externalizable ||
            forProxyClass ||
            name.equals("java.lang.String")) {
            fields = NO_FIELDS;
        } else if (serializable) {
            AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                try {
                    Field pf = cl.getDeclaredField("serialPersistentFields");
                    pf.setAccessible(true);
                    java.io.ObjectStreamField[] f =
                           (java.io.ObjectStreamField[])pf.get(cl);
                    int mods = pf.getModifiers();
                    if ((Modifier.isPrivate(mods)) &&
                        (Modifier.isStatic(mods)) &&
                        (Modifier.isFinal(mods)))
                    {
                        fields = (ObjectStreamField[])translateFields((Object[])pf.get(cl));
                    }
                } catch (NoSuchFieldException e) {
                    fields = null;
                } catch (IllegalAccessException e) {
                    fields = null;
                } catch (IllegalArgumentException e) {
                    fields = null;
                } catch (ClassCastException e) {
                    fields = null;
                }
                if (fields == null) {
                    Field[] actualfields = cl.getDeclaredFields();
                    int numFields = 0;
                    ObjectStreamField[] tempFields =
                        new ObjectStreamField[actualfields.length];
                    for (int i = 0; i < actualfields.length; i++) {
                        int modifiers = actualfields[i].getModifiers();
                        if (!Modifier.isStatic(modifiers) &&
                            !Modifier.isTransient(modifiers)) {
                            tempFields[numFields++] =
                                new ObjectStreamField(actualfields[i]);
                        }
                    }
                    fields = new ObjectStreamField[numFields];
                    System.arraycopy(tempFields, 0, fields, 0, numFields);
                } else {
                    for (int j = fields.length-1; j >= 0; j--) {
                        try {
                            Field reflField = cl.getDeclaredField(fields[j].getName());
                            if (fields[j].getType() == reflField.getType()) {
                                fields[j].setField(reflField);
                            }
                        } catch (NoSuchFieldException e) {
                        }
                    }
                }
                return null;
            }
            });
            if (fields.length > 1)
                Arrays.sort(fields);
            computeFieldInfo();
        }
         if (isNonSerializable()) {
             suid = 0L;
         } else {
             AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                if (forProxyClass) {
                    suid = 0L;
                } else {
                    try {
                        final Field f = cl.getDeclaredField("serialVersionUID");
                        int mods = f.getModifiers();
                        if (Modifier.isStatic(mods) &&
                            Modifier.isFinal(mods) ) {
                            f.setAccessible(true);
                            suid = f.getLong(cl);
                        } else {
                            suid = ObjectStreamClass.getSerialVersionUID(cl);
                        }
                    } catch (NoSuchFieldException ex) {
                        suid = ObjectStreamClass.getSerialVersionUID(cl);
                    } catch (IllegalAccessException ex) {
                        suid = ObjectStreamClass.getSerialVersionUID(cl);
                    }
                }
                try {
                    writeReplaceObjectMethod = cl.getDeclaredMethod("writeReplace", noTypesList);
                    if (Modifier.isStatic(writeReplaceObjectMethod.getModifiers())) {
                        writeReplaceObjectMethod = null;
                    } else {
                        writeReplaceObjectMethod.setAccessible(true);
                    }
                } catch (NoSuchMethodException e2) {
                }
                try {
                    readResolveObjectMethod = cl.getDeclaredMethod("readResolve", noTypesList);
                    if (Modifier.isStatic(readResolveObjectMethod.getModifiers())) {
                       readResolveObjectMethod = null;
                    } else {
                       readResolveObjectMethod.setAccessible(true);
                    }
                } catch (NoSuchMethodException e2) {
                }
                if (serializable && !forProxyClass) {
                    try {
                      Class[] args = {java.io.ObjectOutputStream.class};
                      writeObjectMethod = cl.getDeclaredMethod("writeObject", args);
                      hasWriteObjectMethod = true;
                      int mods = writeObjectMethod.getModifiers();
                      if (!Modifier.isPrivate(mods) ||
                        Modifier.isStatic(mods)) {
                        writeObjectMethod = null;
                        hasWriteObjectMethod = false;
                      }
                    } catch (NoSuchMethodException e) {
                    }
                    try {
                      Class[] args = {java.io.ObjectInputStream.class};
                      readObjectMethod = cl.getDeclaredMethod("readObject", args);
                      int mods = readObjectMethod.getModifiers();
                      if (!Modifier.isPrivate(mods) ||
                        Modifier.isStatic(mods)) {
                        readObjectMethod = null;
                      }
                    } catch (NoSuchMethodException e) {
                    }
                }
                return null;
            }
          });
        }
        actualSuid = computeStructuralUID(this, cl);
      }
    }
    ObjectStreamClass_1_3_1(String n, long s) {
        name = n;
        suid = s;
        superclass = null;
    }
    private static Object[] translateFields(Object objs[])
        throws NoSuchFieldException {
        try{
            java.io.ObjectStreamField fields[] = (java.io.ObjectStreamField[])objs;
            Object translation[] = null;
            if (translatedFields == null)
                translatedFields = new Hashtable();
            translation = (Object[])translatedFields.get(fields);
            if (translation != null)
                return translation;
            else {
                Class osfClass = com.sun.corba.se.impl.orbutil.ObjectStreamField.class;
                translation = (Object[])java.lang.reflect.Array.newInstance(osfClass, objs.length);
                Object arg[] = new Object[2];
                Class types[] = {String.class, Class.class};
                Constructor constructor = osfClass.getDeclaredConstructor(types);
                for (int i = fields.length -1; i >= 0; i--){
                    arg[0] = fields[i].getName();
                    arg[1] = fields[i].getType();
                    translation[i] = constructor.newInstance(arg);
                }
                translatedFields.put(fields, translation);
            }
            return (Object[])translation;
        }
        catch(Throwable t){
            throw new NoSuchFieldException();
        }
    }
    static boolean compareClassNames(String streamName,
                                     String localName,
                                     char pkgSeparator) {
        int streamNameIndex = streamName.lastIndexOf(pkgSeparator);
        if (streamNameIndex < 0)
            streamNameIndex = 0;
        int localNameIndex = localName.lastIndexOf(pkgSeparator);
        if (localNameIndex < 0)
            localNameIndex = 0;
        return streamName.regionMatches(false, streamNameIndex,
                                        localName, localNameIndex,
                                        streamName.length() - streamNameIndex);
    }
    final boolean typeEquals(ObjectStreamClass_1_3_1 other) {
        return (suid == other.suid) &&
            compareClassNames(name, other.name, '.');
    }
    final void setSuperclass(ObjectStreamClass_1_3_1 s) {
        superclass = s;
    }
    final ObjectStreamClass_1_3_1 getSuperclass() {
        return superclass;
    }
    final boolean hasWriteObject() {
        return hasWriteObjectMethod;
    }
    final boolean isCustomMarshaled() {
        return (hasWriteObject() || isExternalizable());
    }
    boolean hasExternalizableBlockDataMode() {
        return hasExternalizableBlockData;
    }
    final ObjectStreamClass_1_3_1 localClassDescriptor() {
        return localClassDesc;
    }
    boolean isSerializable() {
        return serializable;
    }
    boolean isExternalizable() {
        return externalizable;
    }
    boolean isNonSerializable() {
        return ! (externalizable || serializable);
    }
    private void computeFieldInfo() {
        primBytes = 0;
        objFields = 0;
        for (int i = 0; i < fields.length; i++ ) {
            switch (fields[i].getTypeCode()) {
            case 'B':
            case 'Z':
                primBytes += 1;
                break;
            case 'C':
            case 'S':
                primBytes += 2;
                break;
            case 'I':
            case 'F':
                primBytes += 4;
                break;
            case 'J':
            case 'D' :
                primBytes += 8;
                break;
            case 'L':
            case '[':
                objFields += 1;
                break;
            }
        }
    }
    private static long computeStructuralUID(ObjectStreamClass_1_3_1 osc, Class cl) {
        ByteArrayOutputStream devnull = new ByteArrayOutputStream(512);
        long h = 0;
        try {
            if ((!java.io.Serializable.class.isAssignableFrom(cl)) ||
                (cl.isInterface())){
                return 0;
            }
            if (java.io.Externalizable.class.isAssignableFrom(cl)) {
                return 1;
            }
            MessageDigest md = MessageDigest.getInstance("SHA");
            DigestOutputStream mdo = new DigestOutputStream(devnull, md);
            DataOutputStream data = new DataOutputStream(mdo);
            Class parent = cl.getSuperclass();
            if ((parent != null))
            {
                data.writeLong(computeStructuralUID(lookup(parent), parent));
            }
            if (osc.hasWriteObject())
                data.writeInt(2);
            else
                data.writeInt(1);
            ObjectStreamField[] fields = osc.getFields();
            int numNonNullFields = 0;
            for (int i = 0; i < fields.length; i++)
                if (fields[i].getField() != null)
                    numNonNullFields++;
            Field [] field = new java.lang.reflect.Field[numNonNullFields];
            for (int i = 0, fieldNum = 0; i < fields.length; i++) {
                if (fields[i].getField() != null) {
                    field[fieldNum++] = fields[i].getField();
                }
            }
            if (field.length > 1)
                Arrays.sort(field, compareMemberByName);
            for (int i = 0; i < field.length; i++) {
                Field f = field[i];
                int m = f.getModifiers();
                data.writeUTF(f.getName());
                data.writeUTF(getSignature(f.getType()));
            }
            data.flush();
            byte hasharray[] = md.digest();
            for (int i = 0; i < Math.min(8, hasharray.length); i++) {
                h += (long)(hasharray[i] & 255) << (i * 8);
            }
        } catch (IOException ignore) {
            h = -1;
        } catch (NoSuchAlgorithmException complain) {
            throw new SecurityException(complain.getMessage());
        }
        return h;
    }
    static String getSignature(Class clazz) {
        String type = null;
        if (clazz.isArray()) {
            Class cl = clazz;
            int dimensions = 0;
            while (cl.isArray()) {
                dimensions++;
                cl = cl.getComponentType();
            }
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < dimensions; i++) {
                sb.append("[");
            }
            sb.append(getSignature(cl));
            type = sb.toString();
        } else if (clazz.isPrimitive()) {
            if (clazz == Integer.TYPE) {
                type = "I";
            } else if (clazz == Byte.TYPE) {
                type = "B";
            } else if (clazz == Long.TYPE) {
                type = "J";
            } else if (clazz == Float.TYPE) {
                type = "F";
            } else if (clazz == Double.TYPE) {
                type = "D";
            } else if (clazz == Short.TYPE) {
                type = "S";
            } else if (clazz == Character.TYPE) {
                type = "C";
            } else if (clazz == Boolean.TYPE) {
                type = "Z";
            } else if (clazz == Void.TYPE) {
                type = "V";
            }
        } else {
            type = "L" + clazz.getName().replace('.', '/') + ";";
        }
        return type;
    }
    static String getSignature(Method meth) {
        StringBuffer sb = new StringBuffer();
        sb.append("(");
        Class[] params = meth.getParameterTypes(); 
        for (int j = 0; j < params.length; j++) {
            sb.append(getSignature(params[j]));
        }
        sb.append(")");
        sb.append(getSignature(meth.getReturnType()));
        return sb.toString();
    }
    static String getSignature(Constructor cons) {
        StringBuffer sb = new StringBuffer();
        sb.append("(");
        Class[] params = cons.getParameterTypes(); 
        for (int j = 0; j < params.length; j++) {
            sb.append(getSignature(params[j]));
        }
        sb.append(")V");
        return sb.toString();
    }
    static private ObjectStreamClassEntry[] descriptorFor = new ObjectStreamClassEntry[61];
    private static ObjectStreamClass_1_3_1 findDescriptorFor(Class cl) {
        int hash = cl.hashCode();
        int index = (hash & 0x7FFFFFFF) % descriptorFor.length;
        ObjectStreamClassEntry e;
        ObjectStreamClassEntry prev;
        while ((e = descriptorFor[index]) != null && e.get() == null) {
            descriptorFor[index] = e.next;
        }
        prev = e;
        while (e != null ) {
            ObjectStreamClass_1_3_1 desc = (ObjectStreamClass_1_3_1)(e.get());
            if (desc == null) {
                prev.next = e.next;
            } else {
                if (desc.ofClass == cl)
                    return desc;
                prev = e;
            }
            e = e.next;
        }
        return null;
    }
    private static void insertDescriptorFor(ObjectStreamClass_1_3_1 desc) {
        if (findDescriptorFor(desc.ofClass) != null) {
            return;
        }
        int hash = desc.ofClass.hashCode();
        int index = (hash & 0x7FFFFFFF) % descriptorFor.length;
        ObjectStreamClassEntry e = new ObjectStreamClassEntry(desc);
        e.next = descriptorFor[index];
        descriptorFor[index] = e;
    }
    private static Field[] getDeclaredFields(final Class clz) {
        return (Field[]) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return clz.getDeclaredFields();
            }
        });
    }
    private String name;
    private ObjectStreamClass_1_3_1 superclass;
    private boolean serializable;
    private boolean externalizable;
    private ObjectStreamField[] fields;
    private Class ofClass;
    boolean forProxyClass;
    private long suid = kDefaultUID;
    private String suidStr = null;
    private long actualSuid = kDefaultUID;
    private String actualSuidStr = null;
    int primBytes;
    int objFields;
    private Object lock = new Object();
    private boolean hasWriteObjectMethod;
    private boolean hasExternalizableBlockData;
    Method writeObjectMethod;
    Method readObjectMethod;
    private Method writeReplaceObjectMethod;
    private Method readResolveObjectMethod;
    private ObjectStreamClass_1_3_1 localClassDesc;
    private static Class classSerializable = null;
    private static Class classExternalizable = null;
    static {
        try {
            classSerializable = Class.forName("java.io.Serializable");
            classExternalizable = Class.forName("java.io.Externalizable");
        } catch (Throwable e) {
            System.err.println("Could not load java.io.Serializable or java.io.Externalizable.");
        }
    }
    private static final long serialVersionUID = -6120832682080437368L;
    public static final ObjectStreamField[] NO_FIELDS =
        new ObjectStreamField[0];
    private static class ObjectStreamClassEntry 
    {
        ObjectStreamClassEntry(ObjectStreamClass_1_3_1 c) {
            this.c = c;
        }
        ObjectStreamClassEntry next;
        public Object get()
        {
            return c;
        }
        private ObjectStreamClass_1_3_1 c;
    }
    private static Comparator compareClassByName =
        new CompareClassByName();
    private static class CompareClassByName implements Comparator {
        public int compare(Object o1, Object o2) {
            Class c1 = (Class)o1;
            Class c2 = (Class)o2;
            return (c1.getName()).compareTo(c2.getName());
        }
    }
    private static Comparator compareMemberByName =
        new CompareMemberByName();
    private static class CompareMemberByName implements Comparator {
        public int compare(Object o1, Object o2) {
            String s1 = ((Member)o1).getName();
            String s2 = ((Member)o2).getName();
            if (o1 instanceof Method) {
                s1 += getSignature((Method)o1);
                s2 += getSignature((Method)o2);
            } else if (o1 instanceof Constructor) {
                s1 += getSignature((Constructor)o1);
                s2 += getSignature((Constructor)o2);
            }
            return s1.compareTo(s2);
        }
    }
    private static class MethodSignature implements Comparator {
        Member member;
        String signature;      
        static MethodSignature[] removePrivateAndSort(Member[] m) {
            int numNonPrivate = 0;
            for (int i = 0; i < m.length; i++) {
                if (! Modifier.isPrivate(m[i].getModifiers())) {
                    numNonPrivate++;
                }
            }
            MethodSignature[] cm = new MethodSignature[numNonPrivate];
            int cmi = 0;
            for (int i = 0; i < m.length; i++) {
                if (! Modifier.isPrivate(m[i].getModifiers())) {
                    cm[cmi] = new MethodSignature(m[i]);
                    cmi++;
                }
            }
            if (cmi > 0)
                Arrays.sort(cm, cm[0]);
            return cm;
        }
        public int compare(Object o1, Object o2) {
            if (o1 == o2)
                return 0;
            MethodSignature c1 = (MethodSignature)o1;
            MethodSignature c2 = (MethodSignature)o2;
            int result;
            if (isConstructor()) {
                result = c1.signature.compareTo(c2.signature);
            } else { 
                result = c1.member.getName().compareTo(c2.member.getName());
                if (result == 0)
                    result = c1.signature.compareTo(c2.signature);
            }
            return result;
        }
        final private boolean isConstructor() {
            return member instanceof Constructor;
        }
        private MethodSignature(Member m) {
            member = m;
            if (isConstructor()) {
                signature = ObjectStreamClass_1_3_1.getSignature((Constructor)m);
            } else {
                signature = ObjectStreamClass_1_3_1.getSignature((Method)m);
            }
        }
    }
}
