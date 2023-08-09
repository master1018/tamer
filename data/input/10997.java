public final class javax_swing_border_EtchedBorder extends AbstractTest<EtchedBorder> {
    public static void main(String[] args) {
        new javax_swing_border_EtchedBorder().test(true);
    }
    protected EtchedBorder getObject() {
        return new EtchedBorder(EtchedBorder.RAISED, Color.RED, Color.BLUE);
    }
    protected EtchedBorder getAnotherObject() {
        return null; 
    }
}
