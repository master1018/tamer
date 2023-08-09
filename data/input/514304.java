public class SigEnumConstantDelta extends SigMemberDelta<IEnumConstant>
        implements IEnumConstantDelta {
    private IValueDelta ordinalDelta;
    public SigEnumConstantDelta(IEnumConstant from, IEnumConstant to) {
        super(from, to);
    }
    public IValueDelta getOrdinalDelta() {
        return ordinalDelta;
    }
    public void setOrdinalDelta(IValueDelta ordinalDelta) {
        this.ordinalDelta = ordinalDelta;
    }
}
