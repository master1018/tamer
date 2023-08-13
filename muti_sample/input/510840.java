public class ClassReferenceProjection implements IClassReference {
    private final IClassReference original;
    private final Map<ITypeVariableDefinition, ITypeReference> mappings;
    public ClassReferenceProjection(IClassReference original,
            Map<ITypeVariableDefinition, ITypeReference> mappings) {
        this.original = original;
        this.mappings = mappings;
    }
    public IClassDefinition getClassDefinition() {
        return new ClassProjection(original.getClassDefinition(), mappings);
    }
    @Override
    public boolean equals(Object obj) {
        return SigClassReference.equals(this, obj);
    }
    @Override
    public int hashCode() {
        return SigClassReference.hashCode(this);
    }
    @Override
    public String toString() {
        return "(" + SigClassReference.toString(this) + " : " + mappings + " )";
    }
}
