public abstract class ClassType extends CompoundType {
    private ClassType parent;
    public ClassType getSuperclass() {
        return parent;
    }
    public void print ( IndentingWriter writer,
                        boolean useQualifiedNames,
                        boolean useIDLNames,
                        boolean globalIDLNames) throws IOException {
        if (isInner()) {
            writer.p("
        } else {
            writer.p("
        }
        writer.pln(" (" + getRepositoryID() + ")\n");
        printPackageOpen(writer,useIDLNames);
        if (!useIDLNames) {
            writer.p("public ");
        }
        String prefix = "";
        writer.p("class " + getTypeName(false,useIDLNames,false));
        if (printExtends(writer,useQualifiedNames,useIDLNames,globalIDLNames)) {
            prefix = ",";
        }
        printImplements(writer,prefix,useQualifiedNames,useIDLNames,globalIDLNames);
        writer.plnI(" {");
        printMembers(writer,useQualifiedNames,useIDLNames,globalIDLNames);
        writer.pln();
        printMethods(writer,useQualifiedNames,useIDLNames,globalIDLNames);
        if (useIDLNames) {
            writer.pOln("};");
        } else {
            writer.pOln("}");
        }
        printPackageClose(writer,useIDLNames);
    }
    protected void destroy () {
        if (!destroyed) {
            super.destroy();
            if (parent != null) {
                parent.destroy();
                parent = null;
    }
    }
        }
    protected ClassType(ContextStack stack, int typeCode, ClassDefinition classDef) {
        super(stack,typeCode,classDef); 
        if ((typeCode & TM_CLASS) == 0 && classDef.isInterface()) {
            throw new CompilerError("Not a class");
        }
        parent = null;
    }
    protected ClassType(int typeCode, ClassDefinition classDef,ContextStack stack) {
        super(stack,classDef,typeCode);
        if ((typeCode & TM_CLASS) == 0 && classDef.isInterface()) {
            throw new CompilerError("Not a class");
        }
        parent = null;
    }
    protected ClassType(ContextStack stack,
                        ClassDefinition classDef,
                        int typeCode) {
        super(stack,classDef,typeCode);
        if ((typeCode & TM_CLASS) == 0 && classDef.isInterface()) {
            throw new CompilerError("Not a class");
        }
        parent = null;
    }
    protected void swapInvalidTypes () {
        super.swapInvalidTypes();
        if (parent != null && parent.getStatus() != STATUS_VALID) {
            parent = (ClassType) getValidType(parent);
        }
    }
    public String addExceptionDescription (String typeDesc) {
        if (isException) {
            if (isCheckedException) {
                typeDesc = typeDesc + " - Checked Exception";
            } else {
                typeDesc = typeDesc + " - Unchecked Exception";
            }
        }
        return typeDesc;
    }
    protected boolean initParents(ContextStack stack) {
        stack.setNewContextCode(ContextStack.EXTENDS);
        BatchEnvironment env = stack.getEnv();
        boolean result = true;
        try {
            ClassDeclaration parentDecl = getClassDefinition().getSuperClass(env);
            if (parentDecl != null) {
                ClassDefinition parentDef = parentDecl.getClassDefinition(env);
                parent = (ClassType) makeType(parentDef.getType(),parentDef,stack);
                if (parent == null) {
                    result = false;
                }
            }
        } catch (ClassNotFound e) {
            classNotFound(stack,e);
            throw new CompilerError("ClassType constructor");
        }
        return result;
    }
}
