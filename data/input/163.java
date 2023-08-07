public class NewTransitionRecord {
    private int fromstate;
    private int tostate;
    private double rate;
    private int transition;
    private char isFromTangible;
    public NewTransitionRecord(int from, int to, double r, int t, boolean isFTan) {
        fromstate = from;
        tostate = to;
        rate = r;
        transition = t;
        if (isFTan) {
            isFromTangible = 'T';
        } else {
            isFromTangible = 'V';
        }
    }
    public NewTransitionRecord(Marking from, Marking to, double r) {
        fromstate = from.getIDNum();
        tostate = to.getIDNum();
        rate = r;
    }
    public NewTransitionRecord(int from, int to, double r) {
        fromstate = from;
        tostate = to;
        rate = r;
    }
    public NewTransitionRecord(int from, int to, double r, int t) {
        fromstate = from;
        tostate = to;
        rate = r;
        transition = t;
    }
    public NewTransitionRecord() {
        fromstate = 0;
        tostate = 0;
        rate = 0.0;
    }
    public void write(MappedByteBuffer outputBuf) throws IOException {
        outputBuf.putInt(fromstate);
        outputBuf.putInt(tostate);
        outputBuf.putDouble(rate);
        outputBuf.putInt(transition);
        outputBuf.putChar(isFromTangible);
    }
    public boolean read(MappedByteBuffer inputBuf) throws IOException {
        fromstate = inputBuf.getInt();
        tostate = inputBuf.getInt();
        rate = inputBuf.getDouble();
        transition = inputBuf.getInt();
        isFromTangible = inputBuf.getChar();
        return true;
    }
    public void updateRate(double r) {
        rate *= r;
    }
    public int getFromState() {
        return fromstate;
    }
    public int getTransitionNo() {
        return transition;
    }
    public int getToState() {
        return tostate;
    }
    public double getRate() {
        return rate;
    }
    public boolean getIsFromTan() {
        if (isFromTangible == 'T') return true; else return false;
    }
    public int getRecordSize() {
        return 3 * 4 + 8 + 2;
    }
    public boolean equals(TransitionRecord test) {
        return (fromstate == test.getFromState() && tostate == test.getToState());
    }
}
