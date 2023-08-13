public class SigUpperBoundsDelta extends SigDelta<List<ITypeReference>>
        implements IUpperBoundsDelta {
    private ITypeReferenceDelta<?> firstUpperBoundDelta;
    private Set<ITypeReferenceDelta<?>> remainingUpperBoundDeltas;
    public SigUpperBoundsDelta(List<ITypeReference> from,
            List<ITypeReference> to) {
        super(from, to);
    }
    public ITypeReferenceDelta<?> getFirstUpperBoundDelta() {
        return firstUpperBoundDelta;
    }
    public void setFirstUpperBoundDelta(
            ITypeReferenceDelta<?> firstUpperBoundDelta) {
        this.firstUpperBoundDelta = firstUpperBoundDelta;
    }
    public Set<ITypeReferenceDelta<?>> getRemainingUpperBoundDeltas() {
        return remainingUpperBoundDeltas;
    }
    public void setRemainingUpperBoundDeltas(
            Set<ITypeReferenceDelta<?>> remainingUpperBoundDeltas) {
        this.remainingUpperBoundDeltas = remainingUpperBoundDeltas;
    }
}
