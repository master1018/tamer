class ArrayIndexOutOfBoundsException extends IndexOutOfBoundsException {
    private static final long serialVersionUID = -5116101128118950844L;
    public ArrayIndexOutOfBoundsException() {
        super();
    }
    public ArrayIndexOutOfBoundsException(int index) {
        super("Array index out of range: " + index);
    }
    public ArrayIndexOutOfBoundsException(String s) {
        super(s);
    }
}
