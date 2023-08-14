public class SF2Modulator {
    public final static int SOURCE_NONE = 0;
    public final static int SOURCE_NOTE_ON_VELOCITY = 2;
    public final static int SOURCE_NOTE_ON_KEYNUMBER = 3;
    public final static int SOURCE_POLY_PRESSURE = 10;
    public final static int SOURCE_CHANNEL_PRESSURE = 13;
    public final static int SOURCE_PITCH_WHEEL = 14;
    public final static int SOURCE_PITCH_SENSITIVITY = 16;
    public final static int SOURCE_MIDI_CONTROL = 128 * 1;
    public final static int SOURCE_DIRECTION_MIN_MAX = 256 * 0;
    public final static int SOURCE_DIRECTION_MAX_MIN = 256 * 1;
    public final static int SOURCE_POLARITY_UNIPOLAR = 512 * 0;
    public final static int SOURCE_POLARITY_BIPOLAR = 512 * 1;
    public final static int SOURCE_TYPE_LINEAR = 1024 * 0;
    public final static int SOURCE_TYPE_CONCAVE = 1024 * 1;
    public final static int SOURCE_TYPE_CONVEX = 1024 * 2;
    public final static int SOURCE_TYPE_SWITCH = 1024 * 3;
    public final static int TRANSFORM_LINEAR = 0;
    public final static int TRANSFORM_ABSOLUTE = 2;
    protected int sourceOperator;
    protected int destinationOperator;
    protected short amount;
    protected int amountSourceOperator;
    protected int transportOperator;
    public short getAmount() {
        return amount;
    }
    public void setAmount(short amount) {
        this.amount = amount;
    }
    public int getAmountSourceOperator() {
        return amountSourceOperator;
    }
    public void setAmountSourceOperator(int amountSourceOperator) {
        this.amountSourceOperator = amountSourceOperator;
    }
    public int getTransportOperator() {
        return transportOperator;
    }
    public void setTransportOperator(int transportOperator) {
        this.transportOperator = transportOperator;
    }
    public int getDestinationOperator() {
        return destinationOperator;
    }
    public void setDestinationOperator(int destinationOperator) {
        this.destinationOperator = destinationOperator;
    }
    public int getSourceOperator() {
        return sourceOperator;
    }
    public void setSourceOperator(int sourceOperator) {
        this.sourceOperator = sourceOperator;
    }
}
