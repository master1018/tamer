public class TestTargetNew {
    private final Originator originator;
    private Level level = null;
    private String notes = null;
    private ExecutableMemberDoc targetMethod = null;
    private ClassDoc targetClass = null;
    private String readMethodSignature = null;
    private String readMethodName = null;
    private ClassDoc readTargetClass = null;
    private boolean havingProblems = false;
    private TestTargetNew(Originator originator) {
        this.originator = originator;
    }
    public TestTargetNew(Originator originator, AnnotationDesc ttn,
            ClassDoc classLevelTargetClass) {
        this.originator = originator;
        parseTargetClassAndMethodSignature(ttn, classLevelTargetClass);
        if (readMethodName.startsWith("!")) {
            targetMethod = null;
            targetClass = readTargetClass;
            notes = "target: " + readMethodName
                    + (notes != null ? ", " + "notes: " + notes : "");
        } else if (level == Level.TODO) {
            notes = "TODO :" + notes;
            havingProblems = true;
        } else {
            int dotPos = readMethodName.lastIndexOf('.');
            if (dotPos != -1) {
                String prefixClassName = readMethodName.substring(0, dotPos);
                readMethodName = readMethodName.substring(dotPos + 1);
                ClassDoc[] iCs = readTargetClass.innerClasses();
                for (ClassDoc iC : iCs) {
                    if (iC.name().equals(prefixClassName)) {
                        readTargetClass = iC;
                        break;
                    }
                }
            }
            String methodAndSig = readMethodName + readMethodSignature;
            ExecutableMemberDoc tmeth = findMethodSignatureIn(methodAndSig,
                    readTargetClass);
            if (tmeth == null) {
                tmeth = findTargetMethodInSelfAndSupers(methodAndSig,
                        readTargetClass);
                if (tmeth != null) {
                    if (notes == null)
                        notes = "";
                    notes += "- targetmethod (" + tmeth + ") was found in a "
                            + "superclass/superinterface of the target<br>";
                }
            }
            if (tmeth != null) {
                targetMethod = tmeth;
            } else {
                havingProblems = true;
                notes = "From " + originator.asString()
                        + " -> could not resolve " + "targetMethod for class "
                        + readTargetClass + ", " + "annotation was:" + ttn
                        + ", testMethodSig " + "= " + methodAndSig + "<br>";
                System.err.println(">>> warning: " + notes);
            }
        }
    }
    private ExecutableMemberDoc findMethodSignatureIn(String sig,
            ClassDoc targetClass) {
        ExecutableMemberDoc targetMethod = null;
        for (ExecutableMemberDoc mdoc : targetClass.methods()) {
            if (equalsSignature(mdoc, sig)) {
                return mdoc;
            }
        }
        for (ExecutableMemberDoc mdoc : targetClass.constructors()) {
            if (equalsSignature(mdoc, sig)) {
                return mdoc;
            }
        }
        return null;
    }
    private ExecutableMemberDoc findTargetMethodInSelfAndSupers(String sig,
            ClassDoc targetClass) {
        ExecutableMemberDoc mem = findMethodSignatureIn(sig, targetClass);
        if (mem != null) {
            return mem;
        }
        ClassDoc[] ifs = targetClass.interfaces();
        for (int i = 0; i < ifs.length; i++) {
            ClassDoc iface = ifs[i];
            mem = findTargetMethodInSelfAndSupers(sig, iface);
            if (mem != null) {
                return mem;
            }
        }
        ClassDoc superclass = targetClass.superclass();
        if (superclass != null) {
            mem = findTargetMethodInSelfAndSupers(sig, superclass);
            if (mem != null) {
                return mem;
            }
        }
        return null;
    }
    private void parseTargetClassAndMethodSignature(AnnotationDesc targetAnnot,
            ClassDoc targetClass) {
        ElementValuePair[] pairs = targetAnnot.elementValues();
        String methodName = null;
        String args = "";
        for (ElementValuePair kval : pairs) {
            if (kval.element().name().equals("method")) {
                methodName = (String)kval.value().value();
            } else if (kval.element().name().equals("clazz")) {
                Object obj = kval.value().value();
                if (obj instanceof ClassDoc) {
                    targetClass = (ClassDoc)obj;
                } else if (obj instanceof ParameterizedType) {
                    targetClass = ((ParameterizedType)obj).asClassDoc();
                } else {
                    throw new RuntimeException("annotation elem value is of "
                            + "type " + obj.getClass().getName() + " target "
                            + "annotation = " + targetAnnot);
                }
            } else if (kval.element().name().equals("args")) {
                AnnotationValue[] vals = (AnnotationValue[])kval.value()
                        .value();
                for (int i = 0; i < vals.length; i++) {
                    AnnotationValue arg = vals[i];
                    String argV;
                    if (arg.value() instanceof ClassDoc) {
                        ClassDoc cd = (ClassDoc)arg.value();
                        argV = cd.qualifiedName();
                    } else { 
                        argV = arg.toString();
                    }
                    if (argV.endsWith(".class")) {
                        argV = argV.substring(0, argV.length() - 6);
                    }
                    args += (i > 0 ? "," : "") + argV;
                }
            } else if (kval.element().name().equals("level")) {
                AnnotationValue lev = kval.value();
                FieldDoc fd = (FieldDoc)lev.value();
                String slevel = fd.name();
                try {
                    level = Enum.valueOf(Level.class, slevel);
                } catch (IllegalArgumentException iae) {
                    throw new RuntimeException("COMPILE ERROR!!! enum "
                            + slevel + " used in targetMethod for class "
                            + "\"+targetClass+\", "
                            + "annotation was:\"+targetAnnot+\", "
                            + "testMethod = \"+methodDoc.toString()");
                }
            } else if (kval.element().name().equals("notes")) {
                notes = (String)kval.value().value();
                if (notes.equals("")) {
                    notes = null;
                }
            }
        }
        this.readTargetClass = targetClass;
        this.readMethodSignature = "(" + args + ")";
        this.readMethodName = methodName;
    }
    private boolean equalsSignature(ExecutableMemberDoc mdoc,
            String refSignature) {
        Parameter[] params = mdoc.parameters();
        String targs = "";
        for (int i = 0; i < params.length; i++) {
            Parameter parameter = params[i];
            Type ptype = parameter.type();
            TypeVariable typeVar = ptype.asTypeVariable();
            String ptname;
            if (typeVar != null) {
                ptname = "java.lang.Object"; 
                Type[] bounds = typeVar.bounds();
                if (bounds.length > 0) {
                    ClassDoc typeClass = bounds[0].asClassDoc();
                    ptname = typeClass.qualifiedName();
                }
                String dim = ptype.dimension();
                if (dim != null && dim.length() > 0) {
                    ptname += dim;
                }
            } else {
                ptname = parameter.type().toString();
                ptname = ptname.replaceAll("<.*>", "");
            }
            targs += (i > 0 ? "," : "") + ptname;
        }
        String methodName = mdoc.name();
        int lastDot = methodName.lastIndexOf('.');
        if (lastDot != -1) {
            methodName = methodName.substring(lastDot + 1);
        }
        String testSig = methodName + "(" + targs + ")";
        if (testSig.equals(refSignature)) {
            return true;
        } else {
            return false;
        }
    }
    public Level getLevel() {
        return level;
    }
    public boolean isHavingProblems() {
        return havingProblems;
    }
    public Originator getOriginator() {
        return originator;
    }
    TestTargetNew cloneMe(String extraNote) {
        TestTargetNew anew = new TestTargetNew(this.originator);
        anew.level = this.level;
        anew.notes = this.notes;
        anew.targetMethod = this.targetMethod;
        anew.readMethodSignature = this.readMethodSignature;
        anew.readTargetClass = this.readTargetClass;
        anew.notes = extraNote + (notes != null ? ", " + notes : "");
        return anew;
    }
    public ExecutableMemberDoc getTargetMethod() {
        return targetMethod;
    }
    public ClassDoc getTargetClass() {
        return targetClass;
    }
    public String getNotes() {
        return notes;
    }
}
