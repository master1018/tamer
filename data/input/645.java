public class ERXUnsafeReadWriteRouteController<T extends EOEnterpriseObject> extends ERXUnsafeReadOnlyRouteController<T> {
    public ERXUnsafeReadWriteRouteController(WORequest request) {
        super(request);
    }
    @Override
    protected boolean allowUpdates() {
        return true;
    }
}
