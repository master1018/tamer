class ReplaceStringsVisitor extends ASTVisitor {
    private static final String CLASS_ANDROID_CONTEXT    = "android.content.Context"; 
    private static final String CLASS_JAVA_CHAR_SEQUENCE = "java.lang.CharSequence";  
    private static final String CLASS_JAVA_STRING        = "java.lang.String";        
    private final AST mAst;
    private final ASTRewrite mRewriter;
    private final String mOldString;
    private final String mRQualifier;
    private final String mXmlId;
    private final ArrayList<TextEditGroup> mEditGroups;
    public ReplaceStringsVisitor(AST ast,
            ASTRewrite astRewrite,
            ArrayList<TextEditGroup> editGroups,
            String oldString,
            String rQualifier,
            String xmlId) {
        mAst = ast;
        mRewriter = astRewrite;
        mEditGroups = editGroups;
        mOldString = oldString;
        mRQualifier = rQualifier;
        mXmlId = xmlId;
    }
    @SuppressWarnings("unchecked")    
    @Override
    public boolean visit(StringLiteral node) {
        if (node.getLiteralValue().equals(mOldString)) {
            boolean useGetResource = false;
            useGetResource = examineVariableDeclaration(node) ||
                                examineMethodInvocation(node);
            Name qualifierName = mAst.newName(mRQualifier + ".string");     
            SimpleName idName = mAst.newSimpleName(mXmlId);
            ASTNode newNode = mAst.newQualifiedName(qualifierName, idName);
            String title = "Replace string by ID";
            if (useGetResource) {
                Expression context = methodHasContextArgument(node);
                if (context == null && !isClassDerivedFromContext(node)) {
                    context = findContextFieldOrMethod(node);
                    if (context == null) {
                        context = mAst.newSimpleName("Context");            
                    }
                }
                MethodInvocation mi2 = mAst.newMethodInvocation();
                mi2.setName(mAst.newSimpleName("getString"));               
                mi2.setExpression(context);
                mi2.arguments().add(newNode);
                newNode = mi2;
                title = "Replace string by Context.getString(R.string...)";
            }
            TextEditGroup editGroup = new TextEditGroup(title);
            mEditGroups.add(editGroup);
            mRewriter.replace(node, newNode, editGroup);
        }
        return super.visit(node);
    }
    private boolean examineVariableDeclaration(StringLiteral node) {
        VariableDeclarationFragment fragment = findParentClass(node,
                VariableDeclarationFragment.class);
        if (fragment != null) {
            ASTNode parent = fragment.getParent();
            Type type = null;
            if (parent instanceof VariableDeclarationStatement) {
                type = ((VariableDeclarationStatement) parent).getType();
            } else if (parent instanceof VariableDeclarationExpression) {
                type = ((VariableDeclarationExpression) parent).getType();
            }
            if (type instanceof SimpleType) {
                return isJavaString(type.resolveBinding());
            }
        }
        return false;
    }
    @SuppressWarnings("unchecked")  
    private boolean examineMethodInvocation(StringLiteral node) {
        ASTNode parent = null;
        List arguments = null;
        IMethodBinding methodBinding = null;
        MethodInvocation invoke = findParentClass(node, MethodInvocation.class);
        if (invoke != null) {
            parent = invoke;
            arguments = invoke.arguments();
            methodBinding = invoke.resolveMethodBinding();
        } else {
            ClassInstanceCreation newclass = findParentClass(node, ClassInstanceCreation.class);
            if (newclass != null) {
                parent = newclass;
                arguments = newclass.arguments();
                methodBinding = newclass.resolveConstructorBinding();
            }
        }
        if (parent != null && arguments != null && methodBinding != null) {
            ASTNode child = null;
            for (ASTNode n = node; n != parent; ) {
                ASTNode p = n.getParent();
                if (p == parent) {
                    child = n;
                    break;
                }
                n = p;
            }
            if (child == null) {
                return false;
            }
            int index = 0;
            for (Object arg : arguments) {
                if (arg == child) {
                    break;
                }
                index++;
            }
            if (index == arguments.size()) {
                return false;
            }
            boolean useStringType = false;
            ITypeBinding[] types = methodBinding.getParameterTypes();
            if (index < types.length) {
                ITypeBinding type = types[index];
                useStringType = isJavaString(type);
            }
            if (useStringType) {
                String name = methodBinding.getName();
                ITypeBinding clazz = methodBinding.getDeclaringClass();
                nextMethod: for (IMethodBinding mb2 : clazz.getDeclaredMethods()) {
                    if (methodBinding == mb2 || !mb2.getName().equals(name)) {
                        continue;
                    }
                    ITypeBinding[] types2 = mb2.getParameterTypes();
                    int len2 = types2.length;
                    if (types.length == len2) {
                        for (int i = 0; i < len2; i++) {
                            if (i == index) {
                                ITypeBinding type2 = types2[i];
                                if (!("int".equals(type2.getQualifiedName()))) {   
                                    continue nextMethod;
                                }
                            } else if (!types[i].equals(types2[i])) {
                                continue nextMethod;
                            }
                        }
                        useStringType = false;
                        break;
                    }
                }
            }
            return useStringType;
        }
        return false;
    }
    private SimpleName methodHasContextArgument(StringLiteral node) {
        MethodDeclaration decl = findParentClass(node, MethodDeclaration.class);
        if (decl != null) {
            for (Object obj : decl.parameters()) {
                if (obj instanceof SingleVariableDeclaration) {
                    SingleVariableDeclaration var = (SingleVariableDeclaration) obj;
                    if (isAndroidContext(var.getType())) {
                        return mAst.newSimpleName(var.getName().getIdentifier());
                    }
                }
            }
        }
        return null;
    }
    private boolean isClassDerivedFromContext(StringLiteral node) {
        TypeDeclaration clazz = findParentClass(node, TypeDeclaration.class);
        if (clazz != null) {
            return isAndroidContext(clazz.getSuperclassType());
        }
        return false;
    }
    private Expression findContextFieldOrMethod(StringLiteral node) {
        TypeDeclaration clazz = findParentClass(node, TypeDeclaration.class);
        ITypeBinding clazzType = clazz == null ? null : clazz.resolveBinding();
        return findContextFieldOrMethod(clazzType);
    }
    private Expression findContextFieldOrMethod(ITypeBinding clazzType) {
        TreeMap<Integer, Expression> results = new TreeMap<Integer, Expression>();
        findContextCandidates(results, clazzType, 0 );
        if (results.size() > 0) {
            Integer bestRating = results.keySet().iterator().next();
            return results.get(bestRating);
        }
        return null;
    }
    private void findContextCandidates(TreeMap<Integer, Expression> results,
            ITypeBinding clazzType,
            int superType) {
        for (IMethodBinding mb : clazzType.getDeclaredMethods()) {
            if (superType != 0 && Modifier.isPrivate(mb.getModifiers())) {
                continue;
            }
            if (isAndroidContext(mb.getReturnType())) {
                int argsLen = mb.getParameterTypes().length;
                if (argsLen == 0) {
                    MethodInvocation mi = mAst.newMethodInvocation();
                    mi.setName(mAst.newSimpleName(mb.getName()));
                    results.put(Integer.MIN_VALUE, mi);
                    return;
                } else {
                    Integer rating = Integer.valueOf(10000 + 1000 * superType + argsLen);
                    if (!results.containsKey(rating)) {
                        MethodInvocation mi = mAst.newMethodInvocation();
                        mi.setName(mAst.newSimpleName(mb.getName()));
                        results.put(rating, mi);
                    }
                }
            }
        }
        for (IVariableBinding var : clazzType.getDeclaredFields()) {
            if (superType != 0 && Modifier.isPrivate(var.getModifiers())) {
                continue;
            }
            if (isAndroidContext(var.getType())) {
                Integer rating = Integer.valueOf(superType);
                results.put(rating, mAst.newSimpleName(var.getName()));
                break;
            }
        }
        clazzType = clazzType.getSuperclass();
        if (clazzType != null) {
            findContextCandidates(results, clazzType, superType + 1);
        }
    }
    @SuppressWarnings("unchecked")
    private <T extends ASTNode> T findParentClass(ASTNode node, Class<T> clazz) {
        for (node = node.getParent(); node != null; node = node.getParent()) {
            if (node.getClass().equals(clazz)) {
                return (T) node;
            }
        }
        return null;
    }
    private boolean isAndroidContext(Type type) {
        if (type != null) {
            return isAndroidContext(type.resolveBinding());
        }
        return false;
    }
    private boolean isAndroidContext(ITypeBinding type) {
        for (; type != null; type = type.getSuperclass()) {
            if (CLASS_ANDROID_CONTEXT.equals(type.getQualifiedName())) {
                return true;
            }
        }
        return false;
    }
    private boolean isJavaString(ITypeBinding type) {
        for (; type != null; type = type.getSuperclass()) {
            if (CLASS_JAVA_STRING.equals(type.getQualifiedName()) ||
                CLASS_JAVA_CHAR_SEQUENCE.equals(type.getQualifiedName())) {
                return true;
            }
        }
        return false;
    }
}
