public final class ObjectStreamClassUtil_1_3 {
    public static long computeSerialVersionUID(final Class cl) {
        long csuid = ObjectStreamClass.getSerialVersionUID(cl);
        if (csuid == 0)
            return csuid; 
        csuid = (ObjectStreamClassUtil_1_3.getSerialVersion(csuid, cl).longValue());
        return csuid;
    }
    private static Long getSerialVersion(final long csuid, final Class cl)
    {
        return (Long) AccessController.doPrivileged(new PrivilegedAction() {
          public Object run() {
            long suid;
            try {
                final Field f = cl.getDeclaredField("serialVersionUID");
                int mods = f.getModifiers();
                if (Modifier.isStatic(mods) &&
                    Modifier.isFinal(mods) && Modifier.isPrivate(mods)) {
                    suid = csuid;
                 } else {
                    suid = _computeSerialVersionUID(cl);
                 }
              } catch (NoSuchFieldException ex) {
                  suid = _computeSerialVersionUID(cl);
              }
              return new Long(suid);
           }
        });
    }
    public static long computeStructuralUID(boolean hasWriteObject, Class cl) {
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
            if ((parent != null) && (parent != java.lang.Object.class)) {
                boolean hasWriteObjectFlag = false;
                Class [] args = {java.io.ObjectOutputStream.class};
                Method hasWriteObjectMethod = ObjectStreamClassUtil_1_3.getDeclaredMethod(parent, "writeObject", args,
                       Modifier.PRIVATE, Modifier.STATIC);
                if (hasWriteObjectMethod != null)
                    hasWriteObjectFlag = true;
                data.writeLong(ObjectStreamClassUtil_1_3.computeStructuralUID(hasWriteObjectFlag, parent));
            }
            if (hasWriteObject)
                data.writeInt(2);
            else
                data.writeInt(1);
            Field[] field = ObjectStreamClassUtil_1_3.getDeclaredFields(cl);
            Arrays.sort(field, compareMemberByName);
            for (int i = 0; i < field.length; i++) {
                Field f = field[i];
                int m = f.getModifiers();
                if (Modifier.isTransient(m) || Modifier.isStatic(m))
                    continue;
                data.writeUTF(f.getName());
                data.writeUTF(getSignature(f.getType()));
            }
            data.flush();
            byte hasharray[] = md.digest();
            int minimum = Math.min(8, hasharray.length);
            for (int i = minimum; i > 0; i--) {
                h += (long)(hasharray[i] & 255) << (i * 8);
            }
        } catch (IOException ignore) {
            h = -1;
        } catch (NoSuchAlgorithmException complain) {
            throw new SecurityException(complain.getMessage());
        }
        return h;
    }
    private static long _computeSerialVersionUID(Class cl) {
        ByteArrayOutputStream devnull = new ByteArrayOutputStream(512);
        long h = 0;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA");
            DigestOutputStream mdo = new DigestOutputStream(devnull, md);
            DataOutputStream data = new DataOutputStream(mdo);
            data.writeUTF(cl.getName());
            int classaccess = cl.getModifiers();
            classaccess &= (Modifier.PUBLIC | Modifier.FINAL |
                            Modifier.INTERFACE | Modifier.ABSTRACT);
            Method[] method = cl.getDeclaredMethods();
            if ((classaccess & Modifier.INTERFACE) != 0) {
                classaccess &= (~Modifier.ABSTRACT);
                if (method.length > 0) {
                    classaccess |= Modifier.ABSTRACT;
                }
            }
            data.writeInt(classaccess);
            if (!cl.isArray()) {
                Class interfaces[] = cl.getInterfaces();
                Arrays.sort(interfaces, compareClassByName);
                for (int i = 0; i < interfaces.length; i++) {
                    data.writeUTF(interfaces[i].getName());
                }
            }
            Field[] field = cl.getDeclaredFields();
            Arrays.sort(field, compareMemberByName);
            for (int i = 0; i < field.length; i++) {
                Field f = field[i];
                int m = f.getModifiers();
                if (Modifier.isPrivate(m) &&
                    (Modifier.isTransient(m) || Modifier.isStatic(m)))
                    continue;
                data.writeUTF(f.getName());
                data.writeInt(m);
                data.writeUTF(getSignature(f.getType()));
            }
            if (hasStaticInitializer(cl)) {
                data.writeUTF("<clinit>");
                data.writeInt(Modifier.STATIC); 
                data.writeUTF("()V");
            }
            MethodSignature[] constructors =
                MethodSignature.removePrivateAndSort(cl.getDeclaredConstructors());
            for (int i = 0; i < constructors.length; i++) {
                MethodSignature c = constructors[i];
                String mname = "<init>";
                String desc = c.signature;
                desc = desc.replace('/', '.');
                data.writeUTF(mname);
                data.writeInt(c.member.getModifiers());
                data.writeUTF(desc);
            }
            MethodSignature[] methods =
                MethodSignature.removePrivateAndSort(method);
            for (int i = 0; i < methods.length; i++ ) {
                MethodSignature m = methods[i];
                String desc = m.signature;
                desc = desc.replace('/', '.');
                data.writeUTF(m.member.getName());
                data.writeInt(m.member.getModifiers());
                data.writeUTF(desc);
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
    private static String getSignature(Class clazz) {
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
    private static String getSignature(Method meth) {
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
    private static String getSignature(Constructor cons) {
        StringBuffer sb = new StringBuffer();
        sb.append("(");
        Class[] params = cons.getParameterTypes(); 
        for (int j = 0; j < params.length; j++) {
            sb.append(getSignature(params[j]));
        }
        sb.append(")V");
        return sb.toString();
    }
    private static Field[] getDeclaredFields(final Class clz) {
        return (Field[]) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return clz.getDeclaredFields();
            }
        });
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
                signature = ObjectStreamClassUtil_1_3.getSignature((Constructor)m);
            } else {
                signature = ObjectStreamClassUtil_1_3.getSignature((Method)m);
            }
        }
    }
    private static Method hasStaticInitializerMethod = null;
    private static boolean hasStaticInitializer(Class cl) {
        if (hasStaticInitializerMethod == null) {
            Class classWithThisMethod = null;
            try {
                try {
                    classWithThisMethod = Class.forName("sun.misc.ClassReflector");
                } catch (ClassNotFoundException cnfe) {
                }
                if (classWithThisMethod == null)
                    classWithThisMethod = java.io.ObjectStreamClass.class;
                hasStaticInitializerMethod =
                    classWithThisMethod.getDeclaredMethod("hasStaticInitializer",
                                                          new Class[] { Class.class });
            } catch (NoSuchMethodException ex) {
            }
            if (hasStaticInitializerMethod == null) {
                throw new InternalError("Can't find hasStaticInitializer method on "
                                        + classWithThisMethod.getName());
            }
            hasStaticInitializerMethod.setAccessible(true);
        }
        try {
            Boolean retval = (Boolean)
                hasStaticInitializerMethod.invoke(null, new Object[] { cl });
            return retval.booleanValue();
        } catch (Exception ex) {
            throw new InternalError("Error invoking hasStaticInitializer: "
                                    + ex);
        }
    }
    private static Method getDeclaredMethod(final Class cl, final String methodName, final Class[] args,
                                     final int requiredModifierMask,
                                     final int disallowedModifierMask) {
        return (Method) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                Method method = null;
                try {
                    method =
                        cl.getDeclaredMethod(methodName, args);
                        int mods = method.getModifiers();
                        if ((mods & disallowedModifierMask) != 0 ||
                            (mods & requiredModifierMask) != requiredModifierMask) {
                            method = null;
                        }
                } catch (NoSuchMethodException e) {
                }
                return method;
            }
        });
    }
}
