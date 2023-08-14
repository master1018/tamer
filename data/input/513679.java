public class SigWildcardTypeDelta extends SigTypeDelta<IWildcardType>
        implements IWildcardTypeDelta {
    private ITypeReferenceDelta<?> lowerBoundDelta;
    private IUpperBoundsDelta upperBoundDelta;
    public SigWildcardTypeDelta(IWildcardType from, IWildcardType to) {
        super(from, to);
    }
    public ITypeReferenceDelta<?> getLowerBoundDelta() {
        return lowerBoundDelta;
    }
    public void setLowerBoundDelta(ITypeReferenceDelta<?> lowerBoundDelta) {
        this.lowerBoundDelta = lowerBoundDelta;
    }
    public IUpperBoundsDelta getUpperBoundDelta() {
        return upperBoundDelta;
    }
    public void setUpperBoundDelta(IUpperBoundsDelta upperBoundDelta) {
        this.upperBoundDelta = upperBoundDelta;
    }
}
