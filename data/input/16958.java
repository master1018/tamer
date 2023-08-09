public class TypeSignature {
    static class SignatureException extends Exception {
        private static final long serialVersionUID = 1L;
        SignatureException(String reason) {
            super(reason);
        }
    }
    Elements elems;
    private static final String SIG_VOID                   = "V";
    private static final String SIG_BOOLEAN                = "Z";
    private static final String SIG_BYTE                   = "B";
    private static final String SIG_CHAR                   = "C";
    private static final String SIG_SHORT                  = "S";
    private static final String SIG_INT                    = "I";
    private static final String SIG_LONG                   = "J";
    private static final String SIG_FLOAT                  = "F";
    private static final String SIG_DOUBLE                 = "D";
    private static final String SIG_ARRAY                  = "[";
    private static final String SIG_CLASS                  = "L";
    public TypeSignature(Elements elems){
        this.elems = elems;
    }
    public String getTypeSignature(String javasignature) throws SignatureException {
        return getParamJVMSignature(javasignature);
    }
    public String getTypeSignature(String javasignature, TypeMirror returnType)
            throws SignatureException {
        String signature = null; 
        String typeSignature = null; 
        List<String> params = new ArrayList<String>(); 
        String paramsig = null; 
        String paramJVMSig = null; 
        String returnSig = null; 
        String returnJVMType = null; 
        int dimensions = 0; 
        int startIndex = -1;
        int endIndex = -1;
        StringTokenizer st = null;
        int i = 0;
        if (javasignature != null) {
            startIndex = javasignature.indexOf("(");
            endIndex = javasignature.indexOf(")");
        }
        if (((startIndex != -1) && (endIndex != -1))
            &&(startIndex+1 < javasignature.length())
            &&(endIndex < javasignature.length())) {
            signature = javasignature.substring(startIndex+1, endIndex);
        }
        if (signature != null) {
            if (signature.indexOf(",") != -1) {
                st = new StringTokenizer(signature, ",");
                if (st != null) {
                    while (st.hasMoreTokens()) {
                        params.add(st.nextToken());
                    }
                }
            } else {
                params.add(signature);
            }
        }
        typeSignature = "(";
        while (params.isEmpty() != true) {
            paramsig = params.remove(i).trim();
            paramJVMSig  = getParamJVMSignature(paramsig);
            if (paramJVMSig != null) {
                typeSignature += paramJVMSig;
            }
        }
        typeSignature += ")";
        returnJVMType = "";
        if (returnType != null) {
            dimensions = dimensions(returnType);
        }
        while (dimensions-- > 0) {
            returnJVMType += "[";
        }
        if (returnType != null) {
            returnSig = qualifiedTypeName(returnType);
            returnJVMType += getComponentType(returnSig);
        } else {
            System.out.println("Invalid return type.");
        }
        typeSignature += returnJVMType;
        return typeSignature;
    }
    private String getParamJVMSignature(String paramsig) throws SignatureException {
        String paramJVMSig = "";
        String componentType ="";
        if(paramsig != null){
            if(paramsig.indexOf("[]") != -1) {
                int endindex = paramsig.indexOf("[]");
                componentType = paramsig.substring(0, endindex);
                String dimensionString =  paramsig.substring(endindex);
                if(dimensionString != null){
                    while(dimensionString.indexOf("[]") != -1){
                        paramJVMSig += "[";
                        int beginindex = dimensionString.indexOf("]") + 1;
                        if(beginindex < dimensionString.length()){
                            dimensionString = dimensionString.substring(beginindex);
                        }else
                            dimensionString = "";
                    }
                }
            } else componentType = paramsig;
            paramJVMSig += getComponentType(componentType);
        }
        return paramJVMSig;
    }
    private String getComponentType(String componentType) throws SignatureException {
        String JVMSig = "";
        if(componentType != null){
            if(componentType.equals("void")) JVMSig += SIG_VOID ;
            else if(componentType.equals("boolean"))  JVMSig += SIG_BOOLEAN ;
            else if(componentType.equals("byte")) JVMSig += SIG_BYTE ;
            else if(componentType.equals("char"))  JVMSig += SIG_CHAR ;
            else if(componentType.equals("short"))  JVMSig += SIG_SHORT ;
            else if(componentType.equals("int"))  JVMSig += SIG_INT ;
            else if(componentType.equals("long"))  JVMSig += SIG_LONG ;
            else if(componentType.equals("float")) JVMSig += SIG_FLOAT ;
            else if(componentType.equals("double"))  JVMSig += SIG_DOUBLE ;
            else {
                if(!componentType.equals("")){
                    TypeElement classNameDoc = elems.getTypeElement(componentType);
                    if(classNameDoc == null){
                        throw new SignatureException(componentType);
                    }else {
                        String classname = classNameDoc.getQualifiedName().toString();
                        String newclassname = classname.replace('.', '/');
                        JVMSig += "L";
                        JVMSig += newclassname;
                        JVMSig += ";";
                    }
                }
            }
        }
        return JVMSig;
    }
    int dimensions(TypeMirror t) {
        if (t.getKind() != TypeKind.ARRAY)
            return 0;
        return 1 + dimensions(((ArrayType) t).getComponentType());
    }
    String qualifiedTypeName(TypeMirror type) {
        TypeVisitor<Name, Void> v = new SimpleTypeVisitor7<Name, Void>() {
            @Override
            public Name visitArray(ArrayType t, Void p) {
                return t.getComponentType().accept(this, p);
            }
            @Override
            public Name visitDeclared(DeclaredType t, Void p) {
                return ((TypeElement) t.asElement()).getQualifiedName();
            }
            @Override
            public Name visitPrimitive(PrimitiveType t, Void p) {
                return elems.getName(t.toString());
            }
            @Override
            public Name visitNoType(NoType t, Void p) {
                if (t.getKind() == TypeKind.VOID)
                    return elems.getName("void");
                return defaultAction(t, p);
            }
            @Override
            public Name visitTypeVariable(TypeVariable t, Void p) {
                return t.getUpperBound().accept(this, p);
            }
        };
        return v.visit(type).toString();
    }
}
