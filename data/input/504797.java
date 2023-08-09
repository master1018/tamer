public class SigPackageDelta extends SigDelta<IPackage> implements
        IPackageDelta {
    private Set<IClassDefinitionDelta> classDeltas;
    private Set<IAnnotationDelta> annotationDeltas;
    public SigPackageDelta(IPackage from, IPackage to) {
        super(from, to);
    }
    public Set<IClassDefinitionDelta> getClassDeltas() {
        return classDeltas;
    }
    public void setClassDeltas(Set<IClassDefinitionDelta> classDeltas) {
        this.classDeltas = classDeltas;
    }
    public Set<IAnnotationDelta> getAnnotationDeltas() {
        return annotationDeltas;
    }
    public void setAnnotationDeltas(Set<IAnnotationDelta> annotationDeltas) {
        this.annotationDeltas = annotationDeltas;
    }
}
