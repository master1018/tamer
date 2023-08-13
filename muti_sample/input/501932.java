class EmulatedFieldsForDumping extends ObjectOutputStream.PutField {
    private EmulatedFields emulatedFields;
    EmulatedFieldsForDumping(ObjectStreamClass streamClass) {
        super();
        emulatedFields = new EmulatedFields(streamClass.fields(),
                (ObjectStreamField[]) null);
    }
    EmulatedFields emulatedFields() {
        return emulatedFields;
    }
    @Override
    public void put(String name, byte value) {
        emulatedFields.put(name, value);
    }
    @Override
    public void put(String name, char value) {
        emulatedFields.put(name, value);
    }
    @Override
    public void put(String name, double value) {
        emulatedFields.put(name, value);
    }
    @Override
    public void put(String name, float value) {
        emulatedFields.put(name, value);
    }
    @Override
    public void put(String name, int value) {
        emulatedFields.put(name, value);
    }
    @Override
    public void put(String name, long value) {
        emulatedFields.put(name, value);
    }
    @Override
    public void put(String name, Object value) {
        emulatedFields.put(name, value);
    }
    @Override
    public void put(String name, short value) {
        emulatedFields.put(name, value);
    }
    @Override
    public void put(String name, boolean value) {
        emulatedFields.put(name, value);
    }
    @Override
    @Deprecated
    public void write(ObjectOutput output) throws IOException {
        EmulatedFields.ObjectSlot[] slots = emulatedFields.slots();
        for (int i = 0; i < slots.length; i++) {
            EmulatedFields.ObjectSlot slot = slots[i];
            Object fieldValue = slot.getFieldValue();
            Class<?> type = slot.getField().getType();
            if (type == Integer.TYPE) {
                output.writeInt(fieldValue != null ? ((Integer) fieldValue)
                        .intValue() : 0);
            } else if (type == Byte.TYPE) {
                output.writeByte(fieldValue != null ? ((Byte) fieldValue)
                        .byteValue() : (byte) 0);
            } else if (type == Character.TYPE) {
                output.writeChar(fieldValue != null ? ((Character) fieldValue)
                        .charValue() : (char) 0);
            } else if (type == Short.TYPE) {
                output.writeShort(fieldValue != null ? ((Short) fieldValue)
                        .shortValue() : (short) 0);
            } else if (type == Boolean.TYPE) {
                output.writeBoolean(fieldValue != null ? ((Boolean) fieldValue)
                        .booleanValue() : false);
            } else if (type == Long.TYPE) {
                output.writeLong(fieldValue != null ? ((Long) fieldValue)
                        .longValue() : (long) 0);
            } else if (type == Float.TYPE) {
                output.writeFloat(fieldValue != null ? ((Float) fieldValue)
                        .floatValue() : (float) 0);
            } else if (type == Double.TYPE) {
                output.writeDouble(fieldValue != null ? ((Double) fieldValue)
                        .doubleValue() : (double) 0);
            } else {
                output.writeObject(fieldValue);
            }
        }
    }
}
