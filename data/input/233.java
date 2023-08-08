public class BooleanType extends PrimitiveType {
    public BooleanType() {
        this(null, 0, 0, 0, 0);
    }
    public BooleanType(String fn, int bl, int bc, int el, int ec) {
        super(boolean.class, fn, bl, bc, el, ec);
    }
}
