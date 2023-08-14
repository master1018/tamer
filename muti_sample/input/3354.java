public final class javax_swing_border_CompoundBorder extends AbstractTest<CompoundBorder> {
    public static void main(String[] args) {
        new javax_swing_border_CompoundBorder().test(true);
    }
    protected CompoundBorder getObject() {
        return new CompoundBorder(null, new CompoundBorder());
    }
    protected CompoundBorder getAnotherObject() {
        return null; 
    }
}
