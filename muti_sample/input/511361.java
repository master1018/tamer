final class GcEventContainer extends EventContainer {
    public final static int GC_EVENT_TAG = 20001;
    private String processId;
    private long gcTime;
    private long bytesFreed;
    private long objectsFreed;
    private long actualSize;
    private long allowedSize;
    private long softLimit;
    private long objectsAllocated;
    private long bytesAllocated;
    private long zActualSize;
    private long zAllowedSize;
    private long zObjectsAllocated;
    private long zBytesAllocated;
    private long dlmallocFootprint;
    private long mallinfoTotalAllocatedSpace;
    private long externalLimit;
    private long externalBytesAllocated;
    GcEventContainer(LogEntry entry, int tag, Object data) {
        super(entry, tag, data);
        init(data);
    }
    GcEventContainer(int tag, int pid, int tid, int sec, int nsec, Object data) {
        super(tag, pid, tid, sec, nsec, data);
        init(data);
    }
    private void init(Object data) {
        if (data instanceof Object[]) {
            Object[] values = (Object[])data;
            for (int i = 0; i < values.length; i++) {
                if (values[i] instanceof Long) {
                    parseDvmHeapInfo((Long)values[i], i);
                }
            }
        }
    }
    @Override
    public EventValueType getType() {
        return EventValueType.LIST;
    }
    @Override
    public boolean testValue(int index, Object value, CompareMethod compareMethod)
            throws InvalidTypeException {
        if (index == 0) {
            if ((value instanceof String) == false) {
                throw new InvalidTypeException();
            }
        } else if ((value instanceof Long) == false) {
            throw new InvalidTypeException();
        }
        switch (compareMethod) {
            case EQUAL_TO:
                if (index == 0) {
                    return processId.equals(value);
                } else {
                    return getValueAsLong(index) == ((Long)value).longValue();
                }
            case LESSER_THAN:
                return getValueAsLong(index) <= ((Long)value).longValue();
            case LESSER_THAN_STRICT:
                return getValueAsLong(index) < ((Long)value).longValue();
            case GREATER_THAN:
                return getValueAsLong(index) >= ((Long)value).longValue();
            case GREATER_THAN_STRICT:
                return getValueAsLong(index) > ((Long)value).longValue();
            case BIT_CHECK:
                return (getValueAsLong(index) & ((Long)value).longValue()) != 0;
        }
        throw new ArrayIndexOutOfBoundsException();
    }
    @Override
    public Object getValue(int valueIndex) {
        if (valueIndex == 0) {
            return processId;
        }
        try {
            return new Long(getValueAsLong(valueIndex));
        } catch (InvalidTypeException e) {
        }
        return null;
    }
    @Override
    public double getValueAsDouble(int valueIndex) throws InvalidTypeException {
        return (double)getValueAsLong(valueIndex);
    }
    @Override
    public String getValueAsString(int valueIndex) {
        switch (valueIndex) {
            case 0:
                return processId;
            default:
                try {
                    return Long.toString(getValueAsLong(valueIndex));
                } catch (InvalidTypeException e) {
                }
        }
        throw new ArrayIndexOutOfBoundsException();
    }
    static EventValueDescription[] getValueDescriptions() {
        try {
            return new EventValueDescription[] {
                    new EventValueDescription("Process Name", EventValueType.STRING),
                    new EventValueDescription("GC Time", EventValueType.LONG,
                            ValueType.MILLISECONDS),
                    new EventValueDescription("Freed Objects", EventValueType.LONG,
                            ValueType.OBJECTS),
                    new EventValueDescription("Freed Bytes", EventValueType.LONG, ValueType.BYTES),
                    new EventValueDescription("Soft Limit", EventValueType.LONG, ValueType.BYTES),
                    new EventValueDescription("Actual Size (aggregate)", EventValueType.LONG,
                            ValueType.BYTES),
                    new EventValueDescription("Allowed Size (aggregate)", EventValueType.LONG,
                            ValueType.BYTES),
                    new EventValueDescription("Allocated Objects (aggregate)",
                            EventValueType.LONG, ValueType.OBJECTS),
                    new EventValueDescription("Allocated Bytes (aggregate)", EventValueType.LONG,
                            ValueType.BYTES),
                    new EventValueDescription("Actual Size", EventValueType.LONG, ValueType.BYTES),
                    new EventValueDescription("Allowed Size", EventValueType.LONG, ValueType.BYTES),
                    new EventValueDescription("Allocated Objects", EventValueType.LONG,
                            ValueType.OBJECTS),
                    new EventValueDescription("Allocated Bytes", EventValueType.LONG,
                            ValueType.BYTES),
                    new EventValueDescription("Actual Size (zygote)", EventValueType.LONG,
                            ValueType.BYTES),
                    new EventValueDescription("Allowed Size (zygote)", EventValueType.LONG,
                            ValueType.BYTES),
                    new EventValueDescription("Allocated Objects (zygote)", EventValueType.LONG,
                            ValueType.OBJECTS),
                    new EventValueDescription("Allocated Bytes (zygote)", EventValueType.LONG,
                            ValueType.BYTES),
                    new EventValueDescription("External Allocation Limit", EventValueType.LONG,
                            ValueType.BYTES),
                    new EventValueDescription("External Bytes Allocated", EventValueType.LONG,
                            ValueType.BYTES),
                    new EventValueDescription("dlmalloc Footprint", EventValueType.LONG,
                            ValueType.BYTES),
                    new EventValueDescription("Malloc Info: Total Allocated Space",
                            EventValueType.LONG, ValueType.BYTES),
                  };
        } catch (InvalidValueTypeException e) {
            assert false;
        }
        return null;
    }
    private void parseDvmHeapInfo(long data, int index) {
        switch (index) {
            case 0:
                gcTime = float12ToInt((int)((data >> 12) & 0xFFFL));
                bytesFreed = float12ToInt((int)(data & 0xFFFL));
                byte[] dataArray = new byte[8];
                put64bitsToArray(data, dataArray, 0);
                processId = new String(dataArray, 0, 5);
                break;
            case 1:
                objectsFreed = float12ToInt((int)((data >> 48) & 0xFFFL));
                actualSize = float12ToInt((int)((data >> 36) & 0xFFFL));
                allowedSize = float12ToInt((int)((data >> 24) & 0xFFFL));
                objectsAllocated = float12ToInt((int)((data >> 12) & 0xFFFL));
                bytesAllocated = float12ToInt((int)(data & 0xFFFL));
                break;
            case 2:
                softLimit = float12ToInt((int)((data >> 48) & 0xFFFL));
                zActualSize = float12ToInt((int)((data >> 36) & 0xFFFL));
                zAllowedSize = float12ToInt((int)((data >> 24) & 0xFFFL));
                zObjectsAllocated = float12ToInt((int)((data >> 12) & 0xFFFL));
                zBytesAllocated = float12ToInt((int)(data & 0xFFFL));
                break;
            case 3:
                dlmallocFootprint = float12ToInt((int)((data >> 36) & 0xFFFL));
                mallinfoTotalAllocatedSpace = float12ToInt((int)((data >> 24) & 0xFFFL));
                externalLimit = float12ToInt((int)((data >> 12) & 0xFFFL));
                externalBytesAllocated = float12ToInt((int)(data & 0xFFFL));
                break;
            default:
                break;
        }
    }
    private static long float12ToInt(int f12) {
        return (f12 & 0x1FF) << ((f12 >>> 9) * 4);
    }
    private static void put64bitsToArray(long value, byte[] dest, int offset) {
        dest[offset + 7] = (byte)(value & 0x00000000000000FFL);
        dest[offset + 6] = (byte)((value & 0x000000000000FF00L) >> 8);
        dest[offset + 5] = (byte)((value & 0x0000000000FF0000L) >> 16);
        dest[offset + 4] = (byte)((value & 0x00000000FF000000L) >> 24);
        dest[offset + 3] = (byte)((value & 0x000000FF00000000L) >> 32);
        dest[offset + 2] = (byte)((value & 0x0000FF0000000000L) >> 40);
        dest[offset + 1] = (byte)((value & 0x00FF000000000000L) >> 48);
        dest[offset + 0] = (byte)((value & 0xFF00000000000000L) >> 56);
    }
    private final long getValueAsLong(int valueIndex) throws InvalidTypeException {
        switch (valueIndex) {
            case 0:
                throw new InvalidTypeException();
            case 1:
                return gcTime;
            case 2:
                return objectsFreed;
            case 3:
                return bytesFreed;
            case 4:
                return softLimit;
            case 5:
                return actualSize;
            case 6:
                return allowedSize;
            case 7:
                return objectsAllocated;
            case 8:
                return bytesAllocated;
            case 9:
                return actualSize - zActualSize;
            case 10:
                return allowedSize - zAllowedSize;
            case 11:
                return objectsAllocated - zObjectsAllocated;
            case 12:
                return bytesAllocated - zBytesAllocated;
            case 13:
               return zActualSize;
            case 14:
                return zAllowedSize;
            case 15:
                return zObjectsAllocated;
            case 16:
                return zBytesAllocated;
            case 17:
                return externalLimit;
            case 18:
                return externalBytesAllocated;
            case 19:
                return dlmallocFootprint;
            case 20:
                return mallinfoTotalAllocatedSpace;
        }
        throw new ArrayIndexOutOfBoundsException();
    }
}
