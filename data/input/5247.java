final class RemoteClass {
    private final BatchEnvironment env;
    private final ClassDoc implClass;
    private ClassDoc[] remoteInterfaces;
    private Method[] remoteMethods;
    private long interfaceHash;
    static RemoteClass forClass(BatchEnvironment env, ClassDoc implClass) {
        RemoteClass remoteClass = new RemoteClass(env, implClass);
        if (remoteClass.init()) {
            return remoteClass;
        } else {
            return null;
        }
    }
    private RemoteClass(BatchEnvironment env, ClassDoc implClass) {
        this.env = env;
        this.implClass = implClass;
    }
    ClassDoc classDoc() {
        return implClass;
    }
    ClassDoc[] remoteInterfaces() {
        return remoteInterfaces.clone();
    }
    Method[] remoteMethods() {
        return remoteMethods.clone();
    }
    long interfaceHash() {
        return interfaceHash;
    }
    private boolean init() {
        if (implClass.isInterface()) {
            env.error("rmic.cant.make.stubs.for.interface",
                      implClass.qualifiedName());
            return false;
        }
        List<ClassDoc> remotesImplemented = new ArrayList<ClassDoc>();
        for (ClassDoc cl = implClass; cl != null; cl = cl.superclass()) {
            for (ClassDoc intf : cl.interfaces()) {
                if (!remotesImplemented.contains(intf) &&
                    intf.subclassOf(env.docRemote()))
                {
                    remotesImplemented.add(intf);
                    if (env.verbose()) {
                        env.output("[found remote interface: " +
                                   intf.qualifiedName() + "]");
                    }
                }
            }
            if (cl == implClass && remotesImplemented.isEmpty()) {
                if (implClass.subclassOf(env.docRemote())) {
                    env.error("rmic.must.implement.remote.directly",
                              implClass.qualifiedName());
                } else {
                    env.error("rmic.must.implement.remote",
                              implClass.qualifiedName());
                }
                return false;
            }
        }
        remoteInterfaces =
            remotesImplemented.toArray(
                new ClassDoc[remotesImplemented.size()]);
        Map<String,Method> methods = new HashMap<String,Method>();
        boolean errors = false;
        for (ClassDoc intf : remotesImplemented) {
            if (!collectRemoteMethods(intf, methods)) {
                errors = true;
            }
        }
        if (errors) {
            return false;
        }
        String[] orderedKeys =
            methods.keySet().toArray(new String[methods.size()]);
        Arrays.sort(orderedKeys);
        remoteMethods = new Method[methods.size()];
        for (int i = 0; i < remoteMethods.length; i++) {
            remoteMethods[i] = methods.get(orderedKeys[i]);
            if (env.verbose()) {
                String msg = "[found remote method <" + i + ">: " +
                    remoteMethods[i].operationString();
                ClassDoc[] exceptions = remoteMethods[i].exceptionTypes();
                if (exceptions.length > 0) {
                    msg += " throws ";
                    for (int j = 0; j < exceptions.length; j++) {
                        if (j > 0) {
                            msg += ", ";
                        }
                        msg +=  exceptions[j].qualifiedName();
                    }
                }
                msg += "\n\tname and descriptor = \"" +
                    remoteMethods[i].nameAndDescriptor();
                msg += "\n\tmethod hash = " +
                    remoteMethods[i].methodHash() + "]";
                env.output(msg);
            }
        }
        interfaceHash = computeInterfaceHash();
        return true;
    }
    private boolean collectRemoteMethods(ClassDoc intf,
                                         Map<String,Method> table)
    {
        if (!intf.isInterface()) {
            throw new AssertionError(
                intf.qualifiedName() + " not an interface");
        }
        boolean errors = false;
    nextMethod:
        for (MethodDoc method : intf.methods()) {
            boolean hasRemoteException = false;
            for (ClassDoc ex : method.thrownExceptions()) {
                if (env.docRemoteException().subclassOf(ex)) {
                    hasRemoteException = true;
                    break;
                }
            }
            if (!hasRemoteException) {
                env.error("rmic.must.throw.remoteexception",
                          intf.qualifiedName(),
                          method.name() + method.signature());
                errors = true;
                continue nextMethod;
            }
            MethodDoc implMethod = findImplMethod(method);
            if (implMethod != null) {           
                for (ClassDoc ex : implMethod.thrownExceptions()) {
                    if (!ex.subclassOf(env.docException())) {
                        env.error("rmic.must.only.throw.exception",
                                  implMethod.name() + implMethod.signature(),
                                  ex.qualifiedName());
                        errors = true;
                        continue nextMethod;
                    }
                }
            }
            Method newMethod = new Method(method);
            String key = newMethod.nameAndDescriptor();
            Method oldMethod = table.get(key);
            if (oldMethod != null) {
                newMethod = newMethod.mergeWith(oldMethod);
            }
            table.put(key, newMethod);
        }
        for (ClassDoc superintf : intf.interfaces()) {
            if (!collectRemoteMethods(superintf, table)) {
                errors = true;
            }
        }
        return !errors;
    }
    private MethodDoc findImplMethod(MethodDoc interfaceMethod) {
        String name = interfaceMethod.name();
        String desc = Util.methodDescriptorOf(interfaceMethod);
        for (MethodDoc implMethod : implClass.methods()) {
            if (name.equals(implMethod.name()) &&
                desc.equals(Util.methodDescriptorOf(implMethod)))
            {
                return implMethod;
            }
        }
        return null;
    }
    private long computeInterfaceHash() {
        long hash = 0;
        ByteArrayOutputStream sink = new ByteArrayOutputStream(512);
        try {
            MessageDigest md = MessageDigest.getInstance("SHA");
            DataOutputStream out = new DataOutputStream(
                new DigestOutputStream(sink, md));
            out.writeInt(INTERFACE_HASH_STUB_VERSION);
            for (Method method : remoteMethods) {
                MethodDoc methodDoc = method.methodDoc();
                out.writeUTF(methodDoc.name());
                out.writeUTF(Util.methodDescriptorOf(methodDoc));
                ClassDoc exceptions[] = methodDoc.thrownExceptions();
                Arrays.sort(exceptions, new ClassDocComparator());
                for (ClassDoc ex : exceptions) {
                    out.writeUTF(Util.binaryNameOf(ex));
                }
            }
            out.flush();
            byte hashArray[] = md.digest();
            for (int i = 0; i < Math.min(8, hashArray.length); i++) {
                hash += ((long) (hashArray[i] & 0xFF)) << (i * 8);
            }
        } catch (IOException e) {
            throw new AssertionError(e);
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
        return hash;
    }
    private static class ClassDocComparator implements Comparator<ClassDoc> {
        public int compare(ClassDoc o1, ClassDoc o2) {
            return Util.binaryNameOf(o1).compareTo(Util.binaryNameOf(o2));
        }
    }
    final class Method implements Cloneable {
        private final MethodDoc methodDoc;
        private final String operationString;
        private final String nameAndDescriptor;
        private final long methodHash;
        private ClassDoc[] exceptionTypes;
        Method(MethodDoc methodDoc) {
            this.methodDoc = methodDoc;
            exceptionTypes = methodDoc.thrownExceptions();
            Arrays.sort(exceptionTypes, new ClassDocComparator());
            operationString = computeOperationString();
            nameAndDescriptor =
                methodDoc.name() + Util.methodDescriptorOf(methodDoc);
            methodHash = computeMethodHash();
        }
        MethodDoc methodDoc() {
            return methodDoc;
        }
        Type[] parameterTypes() {
            Parameter[] parameters = methodDoc.parameters();
            Type[] paramTypes = new Type[parameters.length];
            for (int i = 0; i < paramTypes.length; i++) {
                paramTypes[i] = parameters[i].type();
            }
            return paramTypes;
        }
        ClassDoc[] exceptionTypes() {
            return exceptionTypes.clone();
        }
        long methodHash() {
            return methodHash;
        }
        String operationString() {
            return operationString;
        }
        String nameAndDescriptor() {
            return nameAndDescriptor;
        }
        Method mergeWith(Method other) {
            if (!nameAndDescriptor().equals(other.nameAndDescriptor())) {
                throw new AssertionError(
                    "attempt to merge method \"" +
                    other.nameAndDescriptor() + "\" with \"" +
                    nameAndDescriptor());
            }
            List<ClassDoc> legalExceptions = new ArrayList<ClassDoc>();
            collectCompatibleExceptions(
                other.exceptionTypes, exceptionTypes, legalExceptions);
            collectCompatibleExceptions(
                exceptionTypes, other.exceptionTypes, legalExceptions);
            Method merged = clone();
            merged.exceptionTypes =
                legalExceptions.toArray(new ClassDoc[legalExceptions.size()]);
            return merged;
        }
        protected Method clone() {
            try {
                return (Method) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new AssertionError(e);
            }
        }
        private void collectCompatibleExceptions(ClassDoc[] froms,
                                                 ClassDoc[] withs,
                                                 List<ClassDoc> list)
        {
            for (ClassDoc from : froms) {
                if (!list.contains(from)) {
                    for (ClassDoc with : withs) {
                        if (from.subclassOf(with)) {
                            list.add(from);
                            break;
                        }
                    }
                }
            }
        }
        private long computeMethodHash() {
            long hash = 0;
            ByteArrayOutputStream sink = new ByteArrayOutputStream(512);
            try {
                MessageDigest md = MessageDigest.getInstance("SHA");
                DataOutputStream out = new DataOutputStream(
                    new DigestOutputStream(sink, md));
                String methodString = nameAndDescriptor();
                out.writeUTF(methodString);
                out.flush();
                byte hashArray[] = md.digest();
                for (int i = 0; i < Math.min(8, hashArray.length); i++) {
                    hash += ((long) (hashArray[i] & 0xFF)) << (i * 8);
                }
            } catch (IOException e) {
                throw new AssertionError(e);
            } catch (NoSuchAlgorithmException e) {
                throw new AssertionError(e);
            }
            return hash;
        }
        private String computeOperationString() {
            Type returnType = methodDoc.returnType();
            String op = returnType.qualifiedTypeName() + " " +
                methodDoc.name() + "(";
            Parameter[] parameters = methodDoc.parameters();
            for (int i = 0; i < parameters.length; i++) {
                if (i > 0) {
                    op += ", ";
                }
                op += parameters[i].type().toString();
            }
            op += ")" + returnType.dimension();
            return op;
        }
    }
}
