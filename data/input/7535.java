final class ArrayElementHandler extends NewElementHandler {
    private Integer length;
    @Override
    public void addAttribute(String name, String value) {
        if (name.equals("length")) { 
            this.length = Integer.valueOf(value);
        } else {
            super.addAttribute(name, value);
        }
    }
    @Override
    public void startElement() {
        if (this.length != null) {
            getValueObject();
        }
    }
    @Override
    protected ValueObject getValueObject(Class<?> type, Object[] args) {
        if (type == null) {
            type = Object.class;
        }
        if (this.length != null) {
            return ValueObjectImpl.create(Array.newInstance(type, this.length));
        }
        Object array = Array.newInstance(type, args.length);
        for (int i = 0; i < args.length; i++) {
            Array.set(array, i, args[i]);
        }
        return ValueObjectImpl.create(array);
    }
}
