public final class javax_swing_Box_Filler extends AbstractTest<Filler> {
    public static void main(String[] args) {
        new javax_swing_Box_Filler().test(true);
    }
    protected Filler getObject() {
        return new Filler(
                new Dimension(1, 2),
                new Dimension(3, 4),
                new Dimension(5, 6));
    }
    protected Filler getAnotherObject() {
        return null; 
    }
}
