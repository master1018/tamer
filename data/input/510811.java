public class ClassProjection implements IClassDefinition {
    private final IClassDefinition original;
    private final Map<ITypeVariableDefinition, ITypeReference> substitutions;
    public ClassProjection(IClassDefinition original,
            Map<ITypeVariableDefinition, ITypeReference> mapping) {
        this.original = original;
        this.substitutions = mapping;
    }
    public Set<IAnnotationField> getAnnotationFields() {
        throw new UnsupportedOperationException();
    }
    public Set<IAnnotation> getAnnotations() {
        throw new UnsupportedOperationException();
    }
    public Set<IConstructor> getConstructors() {
        throw new UnsupportedOperationException();
    }
    public IClassDefinition getDeclaringClass() {
        throw new UnsupportedOperationException();
    }
    public Set<IEnumConstant> getEnumConstants() {
        throw new UnsupportedOperationException();
    }
    public Set<IField> getFields() {
        throw new UnsupportedOperationException();
    }
    public Set<IClassDefinition> getInnerClasses() {
        throw new UnsupportedOperationException();
    }
    Set<ITypeReference> interfaces = null;
    public Set<ITypeReference> getInterfaces() {
        if (interfaces == null) {
            Set<ITypeReference> originalInterfaces = original.getInterfaces();
            if (originalInterfaces == null) {
                interfaces = Collections.emptySet();
            } else {
                interfaces = new HashSet<ITypeReference>();
                for (ITypeReference interfaze : originalInterfaces) {
                    interfaces.add(ViewpointAdapter.substitutedTypeReference(
                            interfaze, substitutions));
                }
                interfaces = Collections.unmodifiableSet(interfaces);
            }
        }
        return interfaces;
    }
    public Kind getKind() {
        return original.getKind();
    }
    Set<IMethod> methods = null;
    public Set<IMethod> getMethods() {
        if (methods == null) {
            Set<IMethod> originalMethods = original.getMethods();
            if (originalMethods == null) {
                methods = Collections.emptySet();
            } else {
                methods = new HashSet<IMethod>();
                for (IMethod m : original.getMethods()) {
                    methods.add(new MethodProjection(m, substitutions));
                }
                methods = Collections.unmodifiableSet(methods);
            }
        }
        return methods;
    }
    public Set<Modifier> getModifiers() {
        return original.getModifiers();
    }
    public String getName() {
        return original.getName();
    }
    public List<String> getPackageFragments() {
        return original.getPackageFragments();
    }
    public String getPackageName() {
        return original.getPackageName();
    }
    public String getQualifiedName() {
        return original.getQualifiedName();
    }
    private boolean superClassInit = false;
    private ITypeReference superClass = null;
    public ITypeReference getSuperClass() {
        if (!superClassInit) {
            ITypeReference originalSuperClass = original.getSuperClass();
            if (originalSuperClass != null) {
                superClass = ViewpointAdapter.substitutedTypeReference(original
                        .getSuperClass(), substitutions);
            }
            superClassInit = true;
        }
        return superClass;
    }
    public List<ITypeVariableDefinition> getTypeParameters() {
        return original.getTypeParameters();
    }
    @Override
    public int hashCode() {
        return SigClassDefinition.hashCode(this);
    }
    @Override
    public boolean equals(Object obj) {
        return SigClassDefinition.equals(this, obj);
    }
    @Override
    public String toString() {
        return "(" + SigClassDefinition.toString(this) + " : " + substitutions
                + " )";
    }
}
