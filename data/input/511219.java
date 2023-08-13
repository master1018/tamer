public abstract class TypedConstant
        extends Constant implements TypeBearer {
    public final TypeBearer getFrameType() {
        return this;
    }
    public final int getBasicType() {
        return getType().getBasicType();
    }
    public final int getBasicFrameType() {
        return getType().getBasicFrameType();
    }
    public final boolean isConstant() {
        return true;
    }
}
