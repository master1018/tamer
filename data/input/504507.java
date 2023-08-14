public class SigClassDefinitionDelta extends
        SigTypeDefinitionDelta<IClassDefinition> implements
        IClassDefinitionDelta {
    public SigClassDefinitionDelta(IClassDefinition from, IClassDefinition to) {
        super(from, to);
    }
    private Set<IAnnotationFieldDelta> annotationFieldDeltas;
    private Set<IConstructorDelta> constructorDeltas;
    private Set<IEnumConstantDelta> enumConstantDeltas;
    private Set<IFieldDelta> fieldDeltas;
    private Set<ITypeReferenceDelta<?>> interfaceDeltas;
    private Set<IMethodDelta> methodDeltas;
    private Set<IModifierDelta> modifierDeltas;
    private ITypeReferenceDelta<?> superClassDelta;
    private Set<IAnnotationDelta> annotationDeltas;
    private Set<ITypeVariableDefinitionDelta> typeVariableDeltas;
    public Set<IAnnotationFieldDelta> getAnnotationFieldDeltas() {
        return annotationFieldDeltas;
    }
    public void setAnnotationFieldDeltas(
            Set<IAnnotationFieldDelta> annotationFieldDeltas) {
        this.annotationFieldDeltas = annotationFieldDeltas;
    }
    public Set<IConstructorDelta> getConstructorDeltas() {
        return constructorDeltas;
    }
    public void setConstructorDeltas(
            Set<IConstructorDelta> constructorDeltas) {
        this.constructorDeltas = constructorDeltas;
    }
    public Set<IEnumConstantDelta> getEnumConstantDeltas() {
        return enumConstantDeltas;
    }
    public void setEnumConstantDeltas(
            Set<IEnumConstantDelta> enumConstantDeltas) {
        this.enumConstantDeltas = enumConstantDeltas;
    }
    public Set<IFieldDelta> getFieldDeltas() {
        return fieldDeltas;
    }
    public void setFieldDeltas(Set<IFieldDelta> fieldDeltas) {
        this.fieldDeltas = fieldDeltas;
    }
    public Set<ITypeReferenceDelta<?>> getInterfaceDeltas() {
        return interfaceDeltas;
    }
    public void setInterfaceDeltas(
            Set<ITypeReferenceDelta<?>> interfaceDeltas) {
        this.interfaceDeltas = interfaceDeltas;
    }
    public Set<IMethodDelta> getMethodDeltas() {
        return methodDeltas;
    }
    public void setMethodDeltas(Set<IMethodDelta> methodDeltas) {
        this.methodDeltas = methodDeltas;
    }
    public Set<IModifierDelta> getModifierDeltas() {
        return modifierDeltas;
    }
    public void setModifierDeltas(Set<IModifierDelta> modifierDeltas) {
        this.modifierDeltas = modifierDeltas;
    }
    public ITypeReferenceDelta<?> getSuperClassDelta() {
        return superClassDelta;
    }
    public void setSuperClassDelta(ITypeReferenceDelta<?> superClassDelta) {
        this.superClassDelta = superClassDelta;
    }
    public Set<IAnnotationDelta> getAnnotationDeltas() {
        return annotationDeltas;
    }
    public void setAnnotationDeltas(Set<IAnnotationDelta> annotationDeltas) {
        this.annotationDeltas = annotationDeltas;
    }
    public Set<ITypeVariableDefinitionDelta> getTypeVariableDeltas() {
        return typeVariableDeltas;
    }
    public void setTypeVariableDeltas(
            Set<ITypeVariableDefinitionDelta> typeVariableDeltas) {
        this.typeVariableDeltas = typeVariableDeltas;
    }
}
