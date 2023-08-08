public final class MerchantListingViewInitializer extends AbstractDynamicViewInitializer {
    final ModelKey ispRef;
    public MerchantListingViewInitializer(ModelKey ispRef) {
        super(MerchantListingView.klas);
        this.ispRef = ispRef;
    }
    @Override
    protected int getViewId() {
        return ispRef.hashCode();
    }
    public ModelKey getIspRef() {
        return ispRef;
    }
}
