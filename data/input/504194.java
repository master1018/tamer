public class SigApiDelta extends SigDelta<IApi> implements IApiDelta {
    private Set<IPackageDelta> packageDeltas;
    public SigApiDelta(IApi from, IApi to) {
        super(from, to);
    }
    public Set<IPackageDelta> getPackageDeltas() {
        return packageDeltas;
    }
    public void setPackageDeltas(Set<IPackageDelta> packageDeltas) {
        this.packageDeltas = packageDeltas;
    }
}
