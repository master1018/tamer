public class EventContainer {
    public enum CompareMethod {
        EQUAL_TO("equals", "=="),
        LESSER_THAN("less than or equals to", "<="),
        LESSER_THAN_STRICT("less than", "<"),
        GREATER_THAN("greater than or equals to", ">="),
        GREATER_THAN_STRICT("greater than", ">"),
        BIT_CHECK("bit check", "&");
        private final String mName;
        private final String mTestString;
        private CompareMethod(String name, String testString) {
            mName = name;
            mTestString = testString;
        }
        @Override
        public String toString() {
            return mName;
        }
        public String testString() {
            return mTestString;
        }
    }
    public static enum EventValueType {
        UNKNOWN(0),
        INT(1),
        LONG(2),
        STRING(3),
        LIST(4),
        TREE(5);
        private final static Pattern STORAGE_PATTERN = Pattern.compile("^(\\d+)@(.*)$"); 
        private int mValue;
        static EventValueType getEventValueType(int value) {
            for (EventValueType type : values()) {
                if (type.mValue == value) {
                    return type;
                }
            }
            return null;
        }
        public static String getStorageString(Object object) {
            if (object instanceof String) {
                return STRING.mValue + "@" + (String)object; 
            } else if (object instanceof Integer) {
                return INT.mValue + "@" + object.toString(); 
            } else if (object instanceof Long) {
                return LONG.mValue + "@" + object.toString(); 
            }
            return null;
        }
        public static Object getObjectFromStorageString(String value) {
            Matcher m = STORAGE_PATTERN.matcher(value);
            if (m.matches()) {
                try {
                    EventValueType type = getEventValueType(Integer.parseInt(m.group(1)));
                    if (type == null) {
                        return null;
                    }
                    switch (type) {
                        case STRING:
                            return m.group(2);
                        case INT:
                            return Integer.valueOf(m.group(2));
                        case LONG:
                            return Long.valueOf(m.group(2));
                    }
                } catch (NumberFormatException nfe) {
                    return null;
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
        private EventValueType(int value) {
            mValue = value;
        }
    }
    public int mTag;
    public int pid;    
    public int tid;    
    public int sec;    
    public int nsec;   
    private Object mData; 
    EventContainer(LogEntry entry, int tag, Object data) {
        getType(data);
        mTag = tag;
        mData = data;
        pid = entry.pid;
        tid = entry.tid;
        sec = entry.sec;
        nsec = entry.nsec;
    }
    EventContainer(int tag, int pid, int tid, int sec, int nsec, Object data) {
        getType(data);
        mTag = tag;
        mData = data;
        this.pid = pid;
        this.tid = tid;
        this.sec = sec;
        this.nsec = nsec;
    }
    public final Integer getInt() throws InvalidTypeException {
        if (getType(mData) == EventValueType.INT) {
            return (Integer)mData;
        }
        throw new InvalidTypeException();
    }
    public final Long getLong() throws InvalidTypeException {
        if (getType(mData) == EventValueType.LONG) {
            return (Long)mData;
        }
        throw new InvalidTypeException();
    }
    public final String getString() throws InvalidTypeException {
        if (getType(mData) == EventValueType.STRING) {
            return (String)mData;
        }
        throw new InvalidTypeException();
    }
    public Object getValue(int valueIndex) {
        return getValue(mData, valueIndex, true);
    }
    public double getValueAsDouble(int valueIndex) throws InvalidTypeException {
        return getValueAsDouble(mData, valueIndex, true);
    }
    public String getValueAsString(int valueIndex) throws InvalidTypeException {
        return getValueAsString(mData, valueIndex, true);
    }
    public EventValueType getType() {
        return getType(mData);
    }
    public final EventValueType getType(Object data) {
        if (data instanceof Integer) {
            return EventValueType.INT;
        } else if (data instanceof Long) {
            return EventValueType.LONG;
        } else if (data instanceof String) {
            return EventValueType.STRING;
        } else if (data instanceof Object[]) {
            Object[] objects = (Object[])data;
            for (Object obj : objects) {
                EventValueType type = getType(obj);
                if (type == EventValueType.LIST || type == EventValueType.TREE) {
                    return EventValueType.TREE;
                }
            }
            return EventValueType.LIST;
        }
        return EventValueType.UNKNOWN;
    }
    public boolean testValue(int index, Object value,
            CompareMethod compareMethod) throws InvalidTypeException {
        EventValueType type = getType(mData);
        if (index > 0 && type != EventValueType.LIST) {
            throw new InvalidTypeException();
        }
        Object data = mData;
        if (type == EventValueType.LIST) {
            data = ((Object[])mData)[index];
        }
        if (data.getClass().equals(data.getClass()) == false) {
            throw new InvalidTypeException();
        }
        switch (compareMethod) {
            case EQUAL_TO:
                return data.equals(value);
            case LESSER_THAN:
                if (data instanceof Integer) {
                    return (((Integer)data).compareTo((Integer)value) <= 0);
                } else if (data instanceof Long) {
                    return (((Long)data).compareTo((Long)value) <= 0);
                }
                throw new InvalidTypeException();
            case LESSER_THAN_STRICT:
                if (data instanceof Integer) {
                    return (((Integer)data).compareTo((Integer)value) < 0);
                } else if (data instanceof Long) {
                    return (((Long)data).compareTo((Long)value) < 0);
                }
                throw new InvalidTypeException();
            case GREATER_THAN:
                if (data instanceof Integer) {
                    return (((Integer)data).compareTo((Integer)value) >= 0);
                } else if (data instanceof Long) {
                    return (((Long)data).compareTo((Long)value) >= 0);
                }
                throw new InvalidTypeException();
            case GREATER_THAN_STRICT:
                if (data instanceof Integer) {
                    return (((Integer)data).compareTo((Integer)value) > 0);
                } else if (data instanceof Long) {
                    return (((Long)data).compareTo((Long)value) > 0);
                }
                throw new InvalidTypeException();
            case BIT_CHECK:
                if (data instanceof Integer) {
                    return (((Integer)data).intValue() & ((Integer)value).intValue()) != 0;
                } else if (data instanceof Long) {
                    return (((Long)data).longValue() & ((Long)value).longValue()) != 0;
                }
                throw new InvalidTypeException();
            default :
                throw new InvalidTypeException();
        }
    }
    private final Object getValue(Object data, int valueIndex, boolean recursive) {
        EventValueType type = getType(data);
        switch (type) {
            case INT:
            case LONG:
            case STRING:
                return data;
            case LIST:
                if (recursive) {
                    Object[] list = (Object[]) data;
                    if (valueIndex >= 0 && valueIndex < list.length) {
                        return getValue(list[valueIndex], valueIndex, false);
                    }
                }
        }
        return null;
    }
    private final double getValueAsDouble(Object data, int valueIndex, boolean recursive)
            throws InvalidTypeException {
        EventValueType type = getType(data);
        switch (type) {
            case INT:
                return ((Integer)data).doubleValue();
            case LONG:
                return ((Long)data).doubleValue();
            case STRING:
                throw new InvalidTypeException();
            case LIST:
                if (recursive) {
                    Object[] list = (Object[]) data;
                    if (valueIndex >= 0 && valueIndex < list.length) {
                        return getValueAsDouble(list[valueIndex], valueIndex, false);
                    }
                }
        }
        throw new InvalidTypeException();
    }
    private final String getValueAsString(Object data, int valueIndex, boolean recursive)
            throws InvalidTypeException {
        EventValueType type = getType(data);
        switch (type) {
            case INT:
                return ((Integer)data).toString();
            case LONG:
                return ((Long)data).toString();
            case STRING:
                return (String)data;
            case LIST:
                if (recursive) {
                    Object[] list = (Object[]) data;
                    if (valueIndex >= 0 && valueIndex < list.length) {
                        return getValueAsString(list[valueIndex], valueIndex, false);
                    }
                } else {
                    throw new InvalidTypeException(
                            "getValueAsString() doesn't support EventValueType.TREE");
                }
        }
        throw new InvalidTypeException(
                "getValueAsString() unsupported type:" + type);
    }
}
