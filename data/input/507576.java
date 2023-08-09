public class SigClassDefinition extends SigAnnotatableElement implements
        IClassDefinition, Serializable {
    private String name;
    private Kind kind = Kind.UNINITIALIZED;
    private ITypeReference superClass = Uninitialized.unset();
    private Set<ITypeReference> interfaces = Uninitialized.unset();
    private Set<Modifier> modifiers = Uninitialized.unset();
    private Set<IMethod> methods = Uninitialized.unset();
    private Set<IConstructor> constructors = Uninitialized.unset();
    private Set<IClassDefinition> innerClasses = Uninitialized.unset();
    private Set<IAnnotationField> annotationFields = Uninitialized.unset();
    private Set<IField> fields = Uninitialized.unset();
    private Set<IEnumConstant> enumConstants = Uninitialized.unset();
    private IClassDefinition declaringClass = Uninitialized.unset();
    private List<ITypeVariableDefinition> typeParameters = Uninitialized
            .unset();
    private String packageName;
    public SigClassDefinition(String packageName, String name) {
        this.packageName = packageName;
        this.name = name;
    }
    public Kind getKind() {
        return kind;
    }
    public void setKind(Kind kind) {
        this.kind = kind;
    }
    public String getName() {
        return name;
    }
    public String getPackageName() {
        return packageName;
    }
    public List<String> getPackageFragments() {
        return Arrays.asList(packageName.split("\\."));
    }
    public String getQualifiedName() {
        return packageName + "." + name;
    }
    public Set<Modifier> getModifiers() {
        return modifiers;
    }
    public void setModifiers(Set<Modifier> modifiers) {
        this.modifiers = modifiers;
    }
    public Set<IClassDefinition> getInnerClasses() {
        return innerClasses;
    }
    public void setInnerClasses(Set<IClassDefinition> innerClasses) {
        this.innerClasses = innerClasses;
    }
    public Set<ITypeReference> getInterfaces() {
        return interfaces;
    }
    public void setInterfaces(Set<ITypeReference> interfaces) {
        this.interfaces = interfaces;
    }
    public Set<IMethod> getMethods() {
        return methods;
    }
    public void setMethods(Set<IMethod> methods) {
        this.methods = methods;
    }
    public Set<IConstructor> getConstructors() {
        return constructors;
    }
    public void setConstructors(Set<IConstructor> constructors) {
        this.constructors = constructors;
    }
    public ITypeReference getSuperClass() {
        return superClass;
    }
    public void setSuperClass(ITypeReference superClass) {
        this.superClass = superClass;
    }
    public IClassDefinition getDeclaringClass() {
        return declaringClass;
    }
    public void setDeclaringClass(IClassDefinition declaringClass) {
        this.declaringClass = declaringClass;
    }
    public Set<IAnnotationField> getAnnotationFields() {
        return annotationFields;
    }
    public void setAnnotationFields(Set<IAnnotationField> annotationFields) {
        this.annotationFields = annotationFields;
    }
    public Set<IField> getFields() {
        return fields;
    }
    public void setFields(Set<IField> fields) {
        this.fields = fields;
    }
    public Set<IEnumConstant> getEnumConstants() {
        return enumConstants;
    }
    public void setEnumConstants(Set<IEnumConstant> enumConstants) {
        this.enumConstants = enumConstants;
    }
    public List<ITypeVariableDefinition> getTypeParameters() {
        return typeParameters;
    }
    public void setTypeParameters(
            List<ITypeVariableDefinition> typeParameters) {
        this.typeParameters = typeParameters;
    }
    @Override
    public int hashCode() {
        return SigClassDefinition.hashCode(this);
    }
    public static int hashCode(IClassDefinition definition) {
        final int prime = 31;
        int result = 1;
        result = prime
                + ((definition.getName() == null) ? 0 : definition.getName()
                        .hashCode());
        result = prime
                * result
                + ((definition.getPackageName() == null) ? 0 : definition
                        .getPackageName().hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        return SigClassDefinition.equals(this, obj);
    }
    public static boolean equals(IClassDefinition thiz, Object obj) {
        if (thiz == obj) return true;
        if (obj instanceof IClassDefinition) {
            IClassDefinition that = (IClassDefinition) obj;
            return thiz.getName().equals(that.getName())
                    && thiz.getPackageName().equals(that.getPackageName());
        }
        return false;
    }
    @Override
    public String toString() {
        return SigClassDefinition.toString(this);
    }
    public static String toString(IClassDefinition type) {
        StringBuilder builder = new StringBuilder();
        if (type.getAnnotations() != null && !type.getAnnotations().isEmpty()) {
            builder.append("\n");
        }
        builder.append(type.getQualifiedName());
        if (type.getTypeParameters() != null
                && (!type.getTypeParameters().isEmpty())) {
            builder.append("<");
            ModelUtil.separate(type.getTypeParameters(), ", ");
            builder.append(">");
        }
        return builder.toString();
    }
}
