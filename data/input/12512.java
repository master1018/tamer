class BRView extends InlineView {
    public BRView(Element elem) {
        super(elem);
    }
    public int getBreakWeight(int axis, float pos, float len) {
        if (axis == X_AXIS) {
            return ForcedBreakWeight;
        } else {
            return super.getBreakWeight(axis, pos, len);
        }
    }
}
