public final class javax_swing_plaf_BorderUIResource_CompoundBorderUIResource extends AbstractTest<CompoundBorderUIResource> {
    public static void main(String[] args) {
        new javax_swing_plaf_BorderUIResource_CompoundBorderUIResource().test(true);
    }
    protected CompoundBorderUIResource getObject() {
        return new CompoundBorderUIResource(null, new CompoundBorderUIResource(null, null));
    }
    protected CompoundBorderUIResource getAnotherObject() {
        return null; 
    }
}
