public class AsmAnalyzer {
    private final Log mLog;
    private final List<String> mOsSourceJar;
    private final AsmGenerator mGen;
    private final String[] mDeriveFrom;
    private final String[] mIncludeGlobs;
    public AsmAnalyzer(Log log, List<String> osJarPath, AsmGenerator gen,
            String[] deriveFrom, String[] includeGlobs) {
        mLog = log;
        mGen = gen;
        mOsSourceJar = osJarPath != null ? osJarPath : new ArrayList<String>();
        mDeriveFrom = deriveFrom != null ? deriveFrom : new String[0];
        mIncludeGlobs = includeGlobs != null ? includeGlobs : new String[0];
    }
    public void analyze() throws IOException, LogAbortException {
        AsmAnalyzer visitor = this;
        Map<String, ClassReader> zipClasses = parseZip(mOsSourceJar);
        mLog.info("Found %d classes in input JAR%s.", zipClasses.size(),
                mOsSourceJar.size() > 1 ? "s" : "");
        Map<String, ClassReader> found = findIncludes(zipClasses);
        Map<String, ClassReader> deps = findDeps(zipClasses, found);
        if (mGen != null) {
            mGen.setKeep(found);
            mGen.setDeps(deps);
        }
    }
    Map<String,ClassReader> parseZip(List<String> jarPathList) throws IOException {
        TreeMap<String, ClassReader> classes = new TreeMap<String, ClassReader>();
        for (String jarPath : jarPathList) {
            ZipFile zip = new ZipFile(jarPath);
            Enumeration<? extends ZipEntry> entries = zip.entries();
            ZipEntry entry;
            while (entries.hasMoreElements()) {
                entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    ClassReader cr = new ClassReader(zip.getInputStream(entry));
                    String className = classReaderToClassName(cr);
                    classes.put(className, cr);
                }
            }
        }
        return classes;
    }
    static String classReaderToClassName(ClassReader classReader) {
        if (classReader == null) {
            return null;
        } else {
            return classReader.getClassName().replace('/', '.');
        }
    }
    static String internalToBinaryClassName(String className) {
        if (className == null) {
            return null;
        } else {
            return className.replace('/', '.');
        }
    }
    Map<String, ClassReader> findIncludes(Map<String, ClassReader> zipClasses)
            throws LogAbortException {
        TreeMap<String, ClassReader> found = new TreeMap<String, ClassReader>();
        mLog.debug("Find classes to include.");
        for (String s : mIncludeGlobs) {
            findGlobs(s, zipClasses, found);
        }
        for (String s : mDeriveFrom) {
            findClassesDerivingFrom(s, zipClasses, found);
        }
        return found;
    }
    ClassReader findClass(String className, Map<String, ClassReader> zipClasses,
            Map<String, ClassReader> inOutFound) throws LogAbortException {
        ClassReader classReader = zipClasses.get(className);
        if (classReader == null) {
            throw new LogAbortException("Class %s not found by ASM in %s",
                    className, mOsSourceJar);
        }
        inOutFound.put(className, classReader);
        return classReader;
    }
    void findGlobs(String globPattern, Map<String, ClassReader> zipClasses,
            Map<String, ClassReader> inOutFound) throws LogAbortException {
        globPattern = globPattern.replaceAll("\\$", "\\\\\\$");
        globPattern = globPattern.replaceAll("\\.", "\\\\.");
        globPattern = globPattern.replaceAll("\\*\\*", "@");
        globPattern = globPattern.replaceAll("\\*", "[^.]*");
        globPattern = globPattern.replaceAll("@", ".*");
        globPattern += "$";
        Pattern regexp = Pattern.compile(globPattern);
        for (Entry<String, ClassReader> entry : zipClasses.entrySet()) {
            String class_name = entry.getKey();
            if (regexp.matcher(class_name).matches()) {
                findClass(class_name, zipClasses, inOutFound);
            }
        }
    }
    void findClassesDerivingFrom(String super_name, Map<String, ClassReader> zipClasses,
            Map<String, ClassReader> inOutFound) throws LogAbortException {
        ClassReader super_clazz = findClass(super_name, zipClasses, inOutFound);
        for (Entry<String, ClassReader> entry : zipClasses.entrySet()) {
            String className = entry.getKey();
            if (super_name.equals(className)) {
                continue;
            }
            ClassReader classReader = entry.getValue();
            ClassReader parent_cr = classReader;
            while (parent_cr != null) {
                String parent_name = internalToBinaryClassName(parent_cr.getSuperName());
                if (parent_name == null) {
                    break;
                } else if (super_name.equals(parent_name)) {
                    inOutFound.put(className, classReader);
                    break;
                }
                parent_cr = zipClasses.get(parent_name);
            }
        }
    }
    DependencyVisitor getVisitor(Map<String, ClassReader> zipClasses,
            Map<String, ClassReader> inKeep,
            Map<String, ClassReader> outKeep,
            Map<String, ClassReader> inDeps,
            Map<String, ClassReader> outDeps) {
        return new DependencyVisitor(zipClasses, inKeep, outKeep, inDeps, outDeps);
    }
    Map<String, ClassReader> findDeps(Map<String, ClassReader> zipClasses,
            Map<String, ClassReader> inOutKeepClasses) {
        TreeMap<String, ClassReader> deps = new TreeMap<String, ClassReader>();
        TreeMap<String, ClassReader> new_deps = new TreeMap<String, ClassReader>();
        TreeMap<String, ClassReader> new_keep = new TreeMap<String, ClassReader>();
        TreeMap<String, ClassReader> temp = new TreeMap<String, ClassReader>();
        DependencyVisitor visitor = getVisitor(zipClasses,
                inOutKeepClasses, new_keep,
                deps, new_deps);
        for (ClassReader cr : inOutKeepClasses.values()) {
            cr.accept(visitor, 0 );
        }
        while (new_deps.size() > 0 || new_keep.size() > 0) {
            deps.putAll(new_deps);
            inOutKeepClasses.putAll(new_keep);
            temp.clear();
            temp.putAll(new_deps);
            temp.putAll(new_keep);
            new_deps.clear();
            new_keep.clear();
            mLog.debug("Found %1$d to keep, %2$d dependencies.",
                    inOutKeepClasses.size(), deps.size());
            for (ClassReader cr : temp.values()) {
                cr.accept(visitor, 0 );
            }
        }
        mLog.info("Found %1$d classes to keep, %2$d class dependencies.",
                inOutKeepClasses.size(), deps.size());
        return deps;
    }
    public class DependencyVisitor
        implements ClassVisitor, FieldVisitor, MethodVisitor, SignatureVisitor, AnnotationVisitor {
        private final Map<String, ClassReader> mZipClasses;
        private final Map<String, ClassReader> mInKeep;
        private final Map<String, ClassReader> mInDeps;
        private final Map<String, ClassReader> mOutDeps;
        private final Map<String, ClassReader> mOutKeep;
        public DependencyVisitor(Map<String, ClassReader> zipClasses,
                Map<String, ClassReader> inKeep,
                Map<String, ClassReader> outKeep,
                Map<String,ClassReader> inDeps,
                Map<String,ClassReader> outDeps) {
            mZipClasses = zipClasses;
            mInKeep = inKeep;
            mOutKeep = outKeep;
            mInDeps = inDeps;
            mOutDeps = outDeps;
        }
        public void considerName(String className) {
            if (className == null) {
                return;
            }
            className = internalToBinaryClassName(className);
            if (mInKeep.containsKey(className) ||
                    mOutKeep.containsKey(className) ||
                    mInDeps.containsKey(className) ||
                    mOutDeps.containsKey(className)) {
                return;
            }
            ClassReader cr = mZipClasses.get(className);
            if (cr == null) {
                return;
            }
            try {
                if (getClass().getClassLoader().loadClass(className) != null) {
                    return;
                }
            } catch (ClassNotFoundException e) {
            }
            if (className.indexOf("android") >= 0) {  
                mOutDeps.put(className, cr);
            } else {
                mOutKeep.put(className, cr);
            }
        }
        public void considerNames(String[] classNames) {
            if (classNames != null) {
                for (String className : classNames) {
                    considerName(className);
                }
            }
        }
        public void considerSignature(String signature) {
            if (signature != null) {
                SignatureReader sr = new SignatureReader(signature);
                sr.accept(this);
            }
        }
        public void considerType(Type t) {
            if (t != null) {
                if (t.getSort() == Type.ARRAY) {
                    t = t.getElementType();
                }
                if (t.getSort() == Type.OBJECT) {
                    considerName(t.getInternalName());
                }
            }
        }
        public void considerDesc(String desc) {
            if (desc != null) {
                try {
                    Type t = Type.getType(desc);
                    considerType(t);
                } catch (ArrayIndexOutOfBoundsException e) {
                }
            }
        }
        public void visit(int version, int access, String name,
                String signature, String superName, String[] interfaces) {
            if (signature != null) {
                considerSignature(signature);
            }
            considerName(superName);
            considerNames(interfaces);
        }
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            considerDesc(desc);
            return this; 
        }
        public void visitAttribute(Attribute attr) {
        }
        public void visitEnd() {
        }
        public FieldVisitor visitField(int access, String name, String desc,
                String signature, Object value) {
            considerDesc(desc);
            considerSignature(signature);
            return this; 
        }
        public void visitInnerClass(String name, String outerName, String innerName, int access) {
            considerName(name);
        }
        public MethodVisitor visitMethod(int access, String name, String desc,
                String signature, String[] exceptions) {
            considerDesc(desc);
            considerSignature(signature);
            return this; 
        }
        public void visitOuterClass(String owner, String name, String desc) {
        }
        public void visitSource(String source, String debug) {
        }
        public AnnotationVisitor visitAnnotationDefault() {
            return this; 
        }
        public void visitCode() {
        }
        public void visitFieldInsn(int opcode, String owner, String name, String desc) {
            considerName(name);
            considerDesc(desc);
        }
        public void visitFrame(int type, int local, Object[] local2, int stack, Object[] stack2) {
        }
        public void visitIincInsn(int var, int increment) {
        }
        public void visitInsn(int opcode) {
        }
        public void visitIntInsn(int opcode, int operand) {
        }
        public void visitJumpInsn(int opcode, Label label) {
        }
        public void visitLabel(Label label) {
        }
        public void visitLdcInsn(Object cst) {
            if (cst instanceof Type) {
                considerType((Type) cst);
            }
        }
        public void visitLineNumber(int line, Label start) {
        }
        public void visitLocalVariable(String name, String desc,
                String signature, Label start, Label end, int index) {
            considerDesc(desc);
            considerSignature(signature);
        }
        public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        }
        public void visitMaxs(int maxStack, int maxLocals) {
        }
        public void visitMethodInsn(int opcode, String owner, String name, String desc) {
            considerName(owner);
            considerDesc(desc);
        }
        public void visitMultiANewArrayInsn(String desc, int dims) {
            considerDesc(desc);
        }
        public AnnotationVisitor visitParameterAnnotation(int parameter, String desc,
                boolean visible) {
            considerDesc(desc);
            return this; 
        }
        public void visitTableSwitchInsn(int min, int max, Label dflt, Label[] labels) {
        }
        public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
            considerName(type);
        }
        public void visitTypeInsn(int opcode, String type) {
            considerName(type);
        }
        public void visitVarInsn(int opcode, int var) {
        }
        private String mCurrentSignatureClass = null;
        public void visitClassType(String name) {
            mCurrentSignatureClass = name;
            considerName(name);
        }
        public void visitInnerClassType(String name) {
            if (mCurrentSignatureClass != null) {
                mCurrentSignatureClass += "$" + name;
                considerName(mCurrentSignatureClass);
            }
        }
        public SignatureVisitor visitArrayType() {
            return this; 
        }
        public void visitBaseType(char descriptor) {
        }
        public SignatureVisitor visitClassBound() {
            return this; 
        }
        public SignatureVisitor visitExceptionType() {
            return this; 
        }
        public void visitFormalTypeParameter(String name) {
        }
        public SignatureVisitor visitInterface() {
            return this; 
        }
        public SignatureVisitor visitInterfaceBound() {
            return this; 
        }
        public SignatureVisitor visitParameterType() {
            return this; 
        }
        public SignatureVisitor visitReturnType() {
            return this; 
        }
        public SignatureVisitor visitSuperclass() {
            return this; 
        }
        public SignatureVisitor visitTypeArgument(char wildcard) {
            return this; 
        }
        public void visitTypeVariable(String name) {
        }
        public void visitTypeArgument() {
        }
        public void visit(String name, Object value) {
            if (value instanceof Type) {
                considerType((Type) value);
            }
        }
        public AnnotationVisitor visitAnnotation(String name, String desc) {
            considerDesc(desc);
            return this; 
        }
        public AnnotationVisitor visitArray(String name) {
            return this; 
        }
        public void visitEnum(String name, String desc, String value) {
            considerDesc(desc);
        }
    }
}
