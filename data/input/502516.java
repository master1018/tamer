public class Capture<T> implements Serializable {
    private static final long serialVersionUID = -4214363692271370781L;
    private CaptureType type;
    private final List<T> values = new ArrayList<T>(2);
    public Capture() {
        this(CaptureType.LAST);
    }
    public Capture(CaptureType type) {
        this.type = type;
    }
    public void reset() {
        values.clear();
    }
    public boolean hasCaptured() {
        return !values.isEmpty();
    }
    public T getValue() {
        if (values.isEmpty()) {
            throw new AssertionError("Nothing captured yet");
        }
        if (values.size() > 1) {
            throw new AssertionError("More than one value captured: "
                    + getValues());
        }
        return values.get(values.size() - 1);
    }
    public List<T> getValues() {
        return values;
    }
    public void setValue(T value) {
        switch (type) {
        case NONE:
            break;
        case ALL:
            values.add(value);
            break;
        case FIRST:
            if (!hasCaptured()) {
                values.add(value);
            }
            break;
        case LAST:
            if (hasCaptured()) {
                reset();
            }
            values.add(value);
            break;
        default:
            throw new IllegalArgumentException("Unknown capture type: " + type);
        }
    }
    @Override
    public String toString() {
        if (values.isEmpty()) {
            return "Nothing captured yet";
        }
        if (values.size() == 1) {
            return String.valueOf(values.get(0));
        }
        return values.toString();
    }
}
