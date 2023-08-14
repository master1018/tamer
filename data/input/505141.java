class EmulatedFields {
    static class ObjectSlot {
        ObjectStreamField field;
        Object fieldValue;
        boolean defaulted = true;
        public ObjectStreamField getField() {
            return field;
        }
        public Object getFieldValue() {
            return fieldValue;
        }
    }
    private ObjectSlot[] slotsToSerialize;
    private ObjectStreamField[] declaredFields;
    public EmulatedFields(ObjectStreamField[] fields,
            ObjectStreamField[] declared) {
        super();
        buildSlots(fields);
        declaredFields = declared;
    }
    private void buildSlots(ObjectStreamField[] fields) {
        slotsToSerialize = new ObjectSlot[fields.length];
        for (int i = 0; i < fields.length; i++) {
            ObjectSlot s = new ObjectSlot();
            slotsToSerialize[i] = s;
            s.field = fields[i];
        }
    }
    public boolean defaulted(String name) throws IllegalArgumentException {
        ObjectSlot slot = findSlot(name, null);
        if (slot == null) {
            throw new IllegalArgumentException("no field '" + name + "'");
        }
        return slot.defaulted;
    }
    private ObjectSlot findSlot(String fieldName, Class<?> fieldType) {
        boolean isPrimitive = fieldType != null && fieldType.isPrimitive();
        for (int i = 0; i < slotsToSerialize.length; i++) {
            ObjectSlot slot = slotsToSerialize[i];
            if (slot.field.getName().equals(fieldName)) {
                if (isPrimitive) {
                    if (slot.field.getType() == fieldType) {
                        return slot;
                    }
                } else {
                    if (fieldType == null) {
                        return slot; 
                    }
                    if (slot.field.getType().isAssignableFrom(fieldType)) {
                        return slot;
                    }
                }
            }
        }
        if (declaredFields != null) {
            for (int i = 0; i < declaredFields.length; i++) {
                ObjectStreamField field = declaredFields[i];
                if (field.getName().equals(fieldName)) {
                    if (isPrimitive ? field.getType() == fieldType
                            : fieldType == null
                                    || field.getType().isAssignableFrom(
                                            fieldType)) {
                        ObjectSlot slot = new ObjectSlot();
                        slot.field = field;
                        slot.defaulted = true;
                        return slot;
                    }
                }
            }
        }
        return null;
    }
    public byte get(String name, byte defaultValue)
            throws IllegalArgumentException {
        ObjectSlot slot = findSlot(name, Byte.TYPE);
        if (slot == null) {
            throw new IllegalArgumentException("no byte field '" + name + "'");
        }
        return slot.defaulted ? defaultValue : ((Byte) slot.fieldValue)
                .byteValue();
    }
    public char get(String name, char defaultValue)
            throws IllegalArgumentException {
        ObjectSlot slot = findSlot(name, Character.TYPE);
        if (slot == null) {
            throw new IllegalArgumentException("no char field '" + name + "'");
        }
        return slot.defaulted ? defaultValue : ((Character) slot.fieldValue)
                .charValue();
    }
    public double get(String name, double defaultValue)
            throws IllegalArgumentException {
        ObjectSlot slot = findSlot(name, Double.TYPE);
        if (slot == null) {
            throw new IllegalArgumentException("no double field '" + name + "'");
        }
        return slot.defaulted ? defaultValue : ((Double) slot.fieldValue)
                .doubleValue();
    }
    public float get(String name, float defaultValue)
            throws IllegalArgumentException {
        ObjectSlot slot = findSlot(name, Float.TYPE);
        if (slot == null) {
            throw new IllegalArgumentException("no float field '" + name + "'");
        }
        return slot.defaulted ? defaultValue : ((Float) slot.fieldValue)
                .floatValue();
    }
    public int get(String name, int defaultValue)
            throws IllegalArgumentException {
        ObjectSlot slot = findSlot(name, Integer.TYPE);
        if (slot == null) {
            throw new IllegalArgumentException("no int field '" + name + "'");
        }
        return slot.defaulted ? defaultValue : ((Integer) slot.fieldValue)
                .intValue();
    }
    public long get(String name, long defaultValue)
            throws IllegalArgumentException {
        ObjectSlot slot = findSlot(name, Long.TYPE);
        if (slot == null) {
            throw new IllegalArgumentException("no long field '" + name + "'");
        }
        return slot.defaulted ? defaultValue : ((Long) slot.fieldValue)
                .longValue();
    }
    public Object get(String name, Object defaultValue)
            throws IllegalArgumentException {
        ObjectSlot slot = findSlot(name, null);
        if (slot == null || slot.field.getType().isPrimitive()) {
            throw new IllegalArgumentException("no Object field '" + name + "'");
        }
        return slot.defaulted ? defaultValue : slot.fieldValue;
    }
    public short get(String name, short defaultValue)
            throws IllegalArgumentException {
        ObjectSlot slot = findSlot(name, Short.TYPE);
        if (slot == null) {
            throw new IllegalArgumentException("no short field '" + name + "'");
        }
        return slot.defaulted ? defaultValue : ((Short) slot.fieldValue)
                .shortValue();
    }
    public boolean get(String name, boolean defaultValue)
            throws IllegalArgumentException {
        ObjectSlot slot = findSlot(name, Boolean.TYPE);
        if (slot == null) {
            throw new IllegalArgumentException("no boolean field '" + name + "'");
        }
        return slot.defaulted ? defaultValue : ((Boolean) slot.fieldValue)
                .booleanValue();
    }
    public void put(String name, byte value) throws IllegalArgumentException {
        ObjectSlot slot = findSlot(name, Byte.TYPE);
        if (slot == null) {
            throw new IllegalArgumentException("no byte field '" + name + "'");
        }
        slot.fieldValue = Byte.valueOf(value);
        slot.defaulted = false; 
    }
    public void put(String name, char value) throws IllegalArgumentException {
        ObjectSlot slot = findSlot(name, Character.TYPE);
        if (slot == null) {
            throw new IllegalArgumentException("no char field '" + name + "'");
        }
        slot.fieldValue = Character.valueOf(value);
        slot.defaulted = false; 
    }
    public void put(String name, double value) throws IllegalArgumentException {
        ObjectSlot slot = findSlot(name, Double.TYPE);
        if (slot == null) {
            throw new IllegalArgumentException("no double field '" + name + "'");
        }
        slot.fieldValue = Double.valueOf(value);
        slot.defaulted = false; 
    }
    public void put(String name, float value) throws IllegalArgumentException {
        ObjectSlot slot = findSlot(name, Float.TYPE);
        if (slot == null) {
            throw new IllegalArgumentException("no float field '" + name + "'");
        }
        slot.fieldValue = Float.valueOf(value);
        slot.defaulted = false; 
    }
    public void put(String name, int value) throws IllegalArgumentException {
        ObjectSlot slot = findSlot(name, Integer.TYPE);
        if (slot == null) {
            throw new IllegalArgumentException("no integer field '" + name + "'");
        }
        slot.fieldValue = Integer.valueOf(value);
        slot.defaulted = false; 
    }
    public void put(String name, long value) throws IllegalArgumentException {
        ObjectSlot slot = findSlot(name, Long.TYPE);
        if (slot == null) {
            throw new IllegalArgumentException("no long field '" + name + "'");
        }
        slot.fieldValue = Long.valueOf(value);
        slot.defaulted = false; 
    }
    public void put(String name, Object value) throws IllegalArgumentException {
        Class<?> valueClass = null;
        if (value != null) {
            valueClass = value.getClass();
        }
        ObjectSlot slot = findSlot(name, valueClass);
        if (slot == null) {
            throw new IllegalArgumentException("no Object field '" + name + "'");
        }
        slot.fieldValue = value;
        slot.defaulted = false; 
    }
    public void put(String name, short value) throws IllegalArgumentException {
        ObjectSlot slot = findSlot(name, Short.TYPE);
        if (slot == null) {
            throw new IllegalArgumentException("no short field '" + name + "'");
        }
        slot.fieldValue = Short.valueOf(value);
        slot.defaulted = false; 
    }
    public void put(String name, boolean value) throws IllegalArgumentException {
        ObjectSlot slot = findSlot(name, Boolean.TYPE);
        if (slot == null) {
            throw new IllegalArgumentException("no boolean field '" + name + "'");
        }
        slot.fieldValue = Boolean.valueOf(value);
        slot.defaulted = false; 
    }
    public ObjectSlot[] slots() {
        return slotsToSerialize;
    }
}
