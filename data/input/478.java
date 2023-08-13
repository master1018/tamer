public class LLNI extends Gen {
    protected final char  innerDelim = '$';     
    protected Set<String>  doneHandleTypes;
    List<VariableElement> fields;
    List<ExecutableElement> methods;
    private boolean       doubleAlign;
    private int           padFieldNum = 0;
    LLNI(boolean doubleAlign, Util util) {
        super(util);
        this.doubleAlign = doubleAlign;
    }
    protected String getIncludes() {
        return "";
    }
    protected void write(OutputStream o, TypeElement clazz) throws Util.Exit {
        try {
            String cname     = mangleClassName(clazz.getQualifiedName().toString());
            PrintWriter pw   = wrapWriter(o);
            fields = ElementFilter.fieldsIn(clazz.getEnclosedElements());
            methods = ElementFilter.methodsIn(clazz.getEnclosedElements());
            generateDeclsForClass(pw, clazz, cname);
        } catch (TypeSignature.SignatureException e) {
            util.error("llni.sigerror", e.getMessage());
        }
    }
    protected void generateDeclsForClass(PrintWriter pw,
            TypeElement clazz, String cname)
            throws TypeSignature.SignatureException, Util.Exit {
        doneHandleTypes  = new HashSet<String>();
        genHandleType(null, "java.lang.Class");
        genHandleType(null, "java.lang.ClassLoader");
        genHandleType(null, "java.lang.Object");
        genHandleType(null, "java.lang.String");
        genHandleType(null, "java.lang.Thread");
        genHandleType(null, "java.lang.ThreadGroup");
        genHandleType(null, "java.lang.Throwable");
        pw.println("" + lineSep);
        pw.println("#ifndef _Included_" + cname);
        pw.println("#define _Included_" + cname);
        pw.println("#include \"typedefs.h\"");
        pw.println("#include \"llni.h\"");
        pw.println("#include \"jni.h\"" + lineSep);
        forwardDecls(pw, clazz);
        structSectionForClass(pw, clazz, cname);
        methodSectionForClass(pw, clazz, cname);
        pw.println("#endif");
    }
    protected void genHandleType(PrintWriter pw, String clazzname) {
        String cname = mangleClassName(clazzname);
        if (!doneHandleTypes.contains(cname)) {
            doneHandleTypes.add(cname);
            if (pw != null) {
                pw.println("#ifndef DEFINED_" + cname);
                pw.println("    #define DEFINED_" + cname);
                pw.println("    GEN_HANDLE_TYPES(" + cname + ");");
                pw.println("#endif" + lineSep);
            }
        }
    }
    protected String mangleClassName(String s) {
        return s.replace('.', '_')
            .replace('/', '_')
            .replace(innerDelim, '_');
    }
    protected void forwardDecls(PrintWriter pw, TypeElement clazz)
            throws TypeSignature.SignatureException {
        TypeElement object = elems.getTypeElement("java.lang.Object");
        if (clazz.equals(object))
            return;
        genHandleType(pw, clazz.getQualifiedName().toString());
        TypeElement superClass = (TypeElement) (types.asElement(clazz.getSuperclass()));
        if (superClass != null) {
            String superClassName = superClass.getQualifiedName().toString();
            forwardDecls(pw, superClass);
        }
        for (VariableElement field: fields) {
            if (!field.getModifiers().contains(Modifier.STATIC)) {
                TypeMirror t = types.erasure(field.asType());
                TypeSignature newTypeSig = new TypeSignature(elems);
                String tname = newTypeSig.qualifiedTypeName(t);
                String sig = newTypeSig.getTypeSignature(tname);
                if (sig.charAt(0) != '[')
                    forwardDeclsFromSig(pw, sig);
            }
        }
        for (ExecutableElement method: methods) {
            if (method.getModifiers().contains(Modifier.NATIVE)) {
                TypeMirror retType = types.erasure(method.getReturnType());
                String typesig = signature(method);
                TypeSignature newTypeSig = new TypeSignature(elems);
                String sig = newTypeSig.getTypeSignature(typesig, retType);
                if (sig.charAt(0) != '[')
                    forwardDeclsFromSig(pw, sig);
            }
        }
    }
    protected void forwardDeclsFromSig(PrintWriter pw, String sig) {
        int    len = sig.length();
        int    i   = sig.charAt(0) == '(' ? 1 : 0;
        while (i < len) {
            if (sig.charAt(i) == 'L') {
                int j = i + 1;
                while (sig.charAt(j) != ';') j++;
                genHandleType(pw, sig.substring(i + 1, j));
                i = j + 1;
            } else {
                i++;
            }
        }
    }
    protected void structSectionForClass(PrintWriter pw,
                                         TypeElement jclazz, String cname) {
        String jname = jclazz.getQualifiedName().toString();
        if (cname.equals("java_lang_Object")) {
            pw.println("");
            pw.println();
            return;
        }
        pw.println("#if !defined(__i386)");
        pw.println("#pragma pack(4)");
        pw.println("#endif");
        pw.println();
        pw.println("struct " + cname + " {");
        pw.println("    ObjHeader h;");
        pw.print(fieldDefs(jclazz, cname));
        if (jname.equals("java.lang.Class"))
            pw.println("    Class *LLNI_mask(cClass);" +
                       "  ");
        pw.println("};" + lineSep + lineSep + "#pragma pack()");
        pw.println();
        return;
    }
    private static class FieldDefsRes {
        public String className;        
        public FieldDefsRes parent;
        public String s;
        public int byteSize;
        public boolean bottomMost;
        public boolean printedOne = false;
        FieldDefsRes(TypeElement clazz, FieldDefsRes parent, boolean bottomMost) {
            this.className = clazz.getQualifiedName().toString();
            this.parent = parent;
            this.bottomMost = bottomMost;
            int byteSize = 0;
            if (parent == null) this.s = "";
            else this.s = parent.s;
        }
    }
    private boolean doField(FieldDefsRes res, VariableElement field,
                            String cname, boolean padWord) {
        String fieldDef = addStructMember(field, cname, padWord);
        if (fieldDef != null) {
            if (!res.printedOne) { 
                if (res.bottomMost) {
                    if (res.s.length() != 0)
                        res.s = res.s + "    " + lineSep;
                } else {
                    res.s = res.s + "    " + lineSep;
                }
                res.printedOne = true;
            }
            res.s = res.s + fieldDef;
            return true;
        }
        return false;
    }
    private int doTwoWordFields(FieldDefsRes res, TypeElement clazz,
                                int offset, String cname, boolean padWord) {
        boolean first = true;
        List<VariableElement> fields = ElementFilter.fieldsIn(clazz.getEnclosedElements());
        for (VariableElement field: fields) {
            TypeKind tk = field.asType().getKind();
            boolean twoWords = (tk == TypeKind.LONG || tk == TypeKind.DOUBLE);
            if (twoWords && doField(res, field, cname, first && padWord)) {
                offset += 8; first = false;
            }
        }
        return offset;
    }
    String fieldDefs(TypeElement clazz, String cname) {
        FieldDefsRes res = fieldDefs(clazz, cname, true);
        return res.s;
    }
    FieldDefsRes fieldDefs(TypeElement clazz, String cname,
                                     boolean bottomMost){
        FieldDefsRes res;
        int offset;
        boolean didTwoWordFields = false;
        TypeElement superclazz = (TypeElement) types.asElement(clazz.getSuperclass());
        if (superclazz != null) {
            String supername = superclazz.getQualifiedName().toString();
            res = new FieldDefsRes(clazz,
                                   fieldDefs(superclazz, cname, false),
                                   bottomMost);
            offset = res.parent.byteSize;
        } else {
            res = new FieldDefsRes(clazz, null, bottomMost);
            offset = 0;
        }
        List<VariableElement> fields = ElementFilter.fieldsIn(clazz.getEnclosedElements());
        for (VariableElement field: fields) {
            if (doubleAlign && !didTwoWordFields && (offset % 8) == 0) {
                offset = doTwoWordFields(res, clazz, offset, cname, false);
                didTwoWordFields = true;
            }
            TypeKind tk = field.asType().getKind();
            boolean twoWords = (tk == TypeKind.LONG || tk == TypeKind.DOUBLE);
            if (!doubleAlign || !twoWords) {
                if (doField(res, field, cname, false)) offset += 4;
            }
        }
        if (doubleAlign && !didTwoWordFields) {
            if ((offset % 8) != 0) offset += 4;
            offset = doTwoWordFields(res, clazz, offset, cname, true);
        }
        res.byteSize = offset;
        return res;
    }
    protected String addStructMember(VariableElement member, String cname,
                                     boolean padWord) {
        String res = null;
        if (member.getModifiers().contains(Modifier.STATIC)) {
            res = addStaticStructMember(member, cname);
        } else {
            TypeMirror mt = types.erasure(member.asType());
            if (padWord) res = "    java_int padWord" + padFieldNum++ + ";" + lineSep;
            res = "    " + llniType(mt, false, false) + " " + llniFieldName(member);
            if (isLongOrDouble(mt)) res = res + "[2]";
            res = res + ";" + lineSep;
        }
        return res;
    }
    static private final boolean isWindows =
        System.getProperty("os.name").startsWith("Windows");
    protected String addStaticStructMember(VariableElement field, String cname) {
        String res = null;
        Object exp = null;
        if (!field.getModifiers().contains(Modifier.STATIC))
            return res;
        if (!field.getModifiers().contains(Modifier.FINAL))
            return res;
        exp = field.getConstantValue();
        if (exp != null) {
            String     cn     = cname + "_" + field.getSimpleName();
            String     suffix = null;
            long           val = 0;
            if (exp instanceof Byte
                || exp instanceof Short
                || exp instanceof Integer) {
                suffix = "L";
                val = ((Number)exp).intValue();
            }
            else if (exp instanceof Long) {
                suffix = isWindows ? "i64" : "LL";
                val = ((Long)exp).longValue();
            }
            else if (exp instanceof Float)  suffix = "f";
            else if (exp instanceof Double) suffix = "";
            else if (exp instanceof Character) {
                suffix = "L";
                Character ch = (Character) exp;
                val = ((int) ch) & 0xffff;
            }
            if (suffix != null) {
                if ((suffix.equals("L") && (val == Integer.MIN_VALUE)) ||
                    (suffix.equals("LL") && (val == Long.MIN_VALUE))) {
                    res = "    #undef  " + cn + lineSep
                        + "    #define " + cn
                        + " (" + (val + 1) + suffix + "-1)" + lineSep;
                } else if (suffix.equals("L") || suffix.endsWith("LL")) {
                    res = "    #undef  " + cn + lineSep
                        + "    #define " + cn + " " + val + suffix + lineSep;
                } else {
                    res = "    #undef  " + cn + lineSep
                        + "    #define " + cn + " " + exp + suffix + lineSep;
                }
            }
        }
        return res;
    }
    protected void methodSectionForClass(PrintWriter pw,
            TypeElement clazz, String cname)
            throws TypeSignature.SignatureException, Util.Exit {
        String methods = methodDecls(clazz, cname);
        if (methods.length() != 0) {
            pw.println("" + lineSep);
            pw.println("#ifdef __cplusplus");
            pw.println("extern \"C\" {");
            pw.println("#endif" + lineSep);
            pw.println(methods);
            pw.println("#ifdef __cplusplus");
            pw.println("}");
            pw.println("#endif");
        }
    }
    protected String methodDecls(TypeElement clazz, String cname)
            throws TypeSignature.SignatureException, Util.Exit {
        String res = "";
        for (ExecutableElement method: methods) {
            if (method.getModifiers().contains(Modifier.NATIVE))
                res = res + methodDecl(method, clazz, cname);
        }
        return res;
    }
    protected String methodDecl(ExecutableElement method,
                                TypeElement clazz, String cname)
            throws TypeSignature.SignatureException, Util.Exit {
        String res = null;
        TypeMirror retType = types.erasure(method.getReturnType());
        String typesig = signature(method);
        TypeSignature newTypeSig = new TypeSignature(elems);
        String sig = newTypeSig.getTypeSignature(typesig, retType);
        boolean longName = needLongName(method, clazz);
        if (sig.charAt(0) != '(')
            util.error("invalid.method.signature", sig);
        res = "JNIEXPORT " + jniType(retType) + " JNICALL" + lineSep + jniMethodName(method, cname, longName)
            + "(JNIEnv *, " + cRcvrDecl(method, cname);
        List<? extends VariableElement> params = method.getParameters();
        List<TypeMirror> argTypes = new ArrayList<TypeMirror>();
        for (VariableElement p: params){
            argTypes.add(types.erasure(p.asType()));
        }
        for (TypeMirror argType: argTypes)
            res = res + ", " + jniType(argType);
        res = res + ");" + lineSep;
        return res;
    }
    protected final boolean needLongName(ExecutableElement method,
                                         TypeElement clazz) {
        Name methodName = method.getSimpleName();
        for (ExecutableElement memberMethod: methods) {
            if ((memberMethod != method) &&
                memberMethod.getModifiers().contains(Modifier.NATIVE) &&
                    (methodName.equals(memberMethod.getSimpleName())))
                return true;
        }
        return false;
    }
    protected final String jniMethodName(ExecutableElement method, String cname,
                                         boolean longName)
                throws TypeSignature.SignatureException {
        String res = "Java_" + cname + "_" + method.getSimpleName();
        if (longName) {
            TypeMirror mType =  types.erasure(method.getReturnType());
            List<? extends VariableElement> params = method.getParameters();
            List<TypeMirror> argTypes = new ArrayList<TypeMirror>();
            for (VariableElement param: params) {
                argTypes.add(types.erasure(param.asType()));
            }
            res = res + "__";
            for (TypeMirror t: argTypes) {
                String tname = t.toString();
                TypeSignature newTypeSig = new TypeSignature(elems);
                String sig = newTypeSig.getTypeSignature(tname);
                res = res + nameToIdentifier(sig);
            }
        }
        return res;
    }
    protected final String jniType(TypeMirror t) throws Util.Exit {
        TypeElement throwable = elems.getTypeElement("java.lang.Throwable");
        TypeElement jClass = elems.getTypeElement("java.lang.Class");
        TypeElement jString = elems.getTypeElement("java.lang.String");
        Element tclassDoc = types.asElement(t);
        switch (t.getKind()) {
            case ARRAY: {
                TypeMirror ct = ((ArrayType) t).getComponentType();
                switch (ct.getKind()) {
                    case BOOLEAN:  return "jbooleanArray";
                    case BYTE:     return "jbyteArray";
                    case CHAR:     return "jcharArray";
                    case SHORT:    return "jshortArray";
                    case INT:      return "jintArray";
                    case LONG:     return "jlongArray";
                    case FLOAT:    return "jfloatArray";
                    case DOUBLE:   return "jdoubleArray";
                    case ARRAY:
                    case DECLARED: return "jobjectArray";
                    default: throw new Error(ct.toString());
                }
            }
            case VOID:     return "void";
            case BOOLEAN:  return "jboolean";
            case BYTE:     return "jbyte";
            case CHAR:     return "jchar";
            case SHORT:    return "jshort";
            case INT:      return "jint";
            case LONG:     return "jlong";
            case FLOAT:    return "jfloat";
            case DOUBLE:   return "jdouble";
            case DECLARED: {
                if (tclassDoc.equals(jString))
                    return "jstring";
                else if (types.isAssignable(t, throwable.asType()))
                    return "jthrowable";
                else if (types.isAssignable(t, jClass.asType()))
                    return "jclass";
                else
                    return "jobject";
            }
        }
        util.bug("jni.unknown.type");
        return null; 
    }
    protected String llniType(TypeMirror t, boolean handleize, boolean longDoubleOK) {
        String res = null;
        switch (t.getKind()) {
            case ARRAY: {
                TypeMirror ct = ((ArrayType) t).getComponentType();
                switch (ct.getKind()) {
                    case BOOLEAN:  res = "IArrayOfBoolean"; break;
                    case BYTE:     res = "IArrayOfByte";    break;
                    case CHAR:     res = "IArrayOfChar";    break;
                    case SHORT:    res = "IArrayOfShort";   break;
                    case INT:      res = "IArrayOfInt";     break;
                    case LONG:     res = "IArrayOfLong";    break;
                    case FLOAT:    res = "IArrayOfFloat";   break;
                    case DOUBLE:   res = "IArrayOfDouble";  break;
                    case ARRAY:
                    case DECLARED: res = "IArrayOfRef";     break;
                    default: throw new Error(ct.getKind() + " " + ct);
                }
                if (!handleize) res = "DEREFERENCED_" + res;
                break;
            }
            case VOID:
                res = "void";
                break;
            case BOOLEAN:
            case BYTE:
            case CHAR:
            case SHORT:
            case INT:
                res = "java_int" ;
                break;
            case LONG:
                res = longDoubleOK ? "java_long" : "val32 ";
                break;
            case FLOAT:
                res =  "java_float";
                break;
            case DOUBLE:
                res = longDoubleOK ? "java_double" : "val32 ";
                break;
            case DECLARED:
                TypeElement e  = (TypeElement) types.asElement(t);
                res = "I" +  mangleClassName(e.getQualifiedName().toString());
                if (!handleize) res = "DEREFERENCED_" + res;
                break;
            default:
                throw new Error(t.getKind() + " " + t); 
        }
        return res;
    }
    protected final String cRcvrDecl(Element field, String cname) {
        return (field.getModifiers().contains(Modifier.STATIC) ? "jclass" : "jobject");
    }
    protected String maskName(String s) {
        return "LLNI_mask(" + s + ")";
    }
    protected String llniFieldName(VariableElement field) {
        return maskName(field.getSimpleName().toString());
    }
    protected final boolean isLongOrDouble(TypeMirror t) {
        TypeVisitor<Boolean,Void> v = new SimpleTypeVisitor7<Boolean,Void>() {
            public Boolean defaultAction(TypeMirror t, Void p){
                return false;
            }
            public Boolean visitArray(ArrayType t, Void p) {
                return visit(t.getComponentType(), p);
            }
            public Boolean visitPrimitive(PrimitiveType t, Void p) {
                TypeKind tk = t.getKind();
                return (tk == TypeKind.LONG || tk == TypeKind.DOUBLE);
            }
        };
        return v.visit(t, null);
    }
    protected final String nameToIdentifier(String name) {
        int len = name.length();
        StringBuffer buf = new StringBuffer(len);
        for (int i = 0; i < len; i++) {
            char c = name.charAt(i);
            if (isASCIILetterOrDigit(c))
                buf.append(c);
            else if (c == '/')
                buf.append('_');
            else if (c == '.')
                buf.append('_');
            else if (c == '_')
                buf.append("_1");
            else if (c == ';')
                buf.append("_2");
            else if (c == '[')
                buf.append("_3");
            else
                buf.append("_0" + ((int)c));
        }
        return new String(buf);
    }
    protected final boolean isASCIILetterOrDigit(char c) {
        if (((c >= 'A') && (c <= 'Z')) ||
            ((c >= 'a') && (c <= 'z')) ||
            ((c >= '0') && (c <= '9')))
            return true;
        else
            return false;
    }
}
