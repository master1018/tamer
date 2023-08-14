public final class EventValueDescription {
    public static enum ValueType {
        NOT_APPLICABLE(0),
        OBJECTS(1),
        BYTES(2),
        MILLISECONDS(3),
        ALLOCATIONS(4),
        ID(5),
        PERCENT(6);
        private int mValue;
        public void checkType(EventValueType type) throws InvalidValueTypeException {
            if ((type != EventValueType.INT && type != EventValueType.LONG)
                    && this != NOT_APPLICABLE) {
                throw new InvalidValueTypeException(
                        String.format("%1$s doesn't support type %2$s", type, this));
            }
        }
        public static ValueType getValueType(int value) {
            for (ValueType type : values()) {
                if (type.mValue == value) {
                    return type;
                }
            }
            return null;
        }
        public int getValue() {
            return mValue;
        }
        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
        private ValueType(int value) {
            mValue = value;
        }
    }
    private String mName;
    private EventValueType mEventValueType;
    private ValueType mValueType;
    EventValueDescription(String name, EventValueType type) {
        mName = name;
        mEventValueType = type;
        if (mEventValueType == EventValueType.INT || mEventValueType == EventValueType.LONG) {
            mValueType = ValueType.BYTES;
        } else {
            mValueType = ValueType.NOT_APPLICABLE;
        }
    }
    EventValueDescription(String name, EventValueType type, ValueType valueType)
            throws InvalidValueTypeException {
        mName = name;
        mEventValueType = type;
        mValueType = valueType;
        mValueType.checkType(mEventValueType);
    }
    public String getName() {
        return mName;
    }
    public EventValueType getEventValueType() {
        return mEventValueType;
    }
    public ValueType getValueType() {
        return mValueType;
    }
    @Override
    public String toString() {
        if (mValueType != ValueType.NOT_APPLICABLE) {
            return String.format("%1$s (%2$s, %3$s)", mName, mEventValueType.toString(),
                    mValueType.toString());
        }
        return String.format("%1$s (%2$s)", mName, mEventValueType.toString());
    }
    public boolean checkForType(Object value) {
        switch (mEventValueType) {
            case INT:
                return value instanceof Integer;
            case LONG:
                return value instanceof Long;
            case STRING:
                return value instanceof String;
            case LIST:
                return value instanceof Object[];
        }
        return false;
    }
    public Object getObjectFromString(String value) {
        switch (mEventValueType) {
            case INT:
                try {
                    return Integer.valueOf(value);
                } catch (NumberFormatException e) {
                    return null;
                }
            case LONG:
                try {
                    return Long.valueOf(value);
                } catch (NumberFormatException e) {
                    return null;
                }
            case STRING:
                return value;
        }
        return null;
    }
}
