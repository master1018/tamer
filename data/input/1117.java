public abstract class ServerGmsImpl extends GmsImpl {
    protected ServerGmsImpl(GMS gms) {
        super(gms);
    }
    public void handleMergeRequest(Address sender, MergeId merge_id, Collection<? extends Address> mbrs) {
        merger.handleMergeRequest(sender, merge_id, mbrs);
    }
    public void handleMergeView(final MergeData data, final MergeId merge_id) {
        merger.handleMergeView(data, merge_id);
    }
    public void handleDigestResponse(Address sender, Digest digest) {
        merger.handleDigestResponse(sender, digest);
    }
}
