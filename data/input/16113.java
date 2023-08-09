public final class CompletionStatus implements org.omg.CORBA.portable.IDLEntity
{
    public static final int _COMPLETED_YES = 0,
        _COMPLETED_NO = 1,
        _COMPLETED_MAYBE = 2;
    public static final CompletionStatus COMPLETED_YES   = new CompletionStatus(_COMPLETED_YES);
    public static final CompletionStatus COMPLETED_NO    = new CompletionStatus(_COMPLETED_NO);
    public static final CompletionStatus COMPLETED_MAYBE = new CompletionStatus(_COMPLETED_MAYBE);
    public int value() { return _value; }
    public static CompletionStatus from_int(int i)  {
        switch (i) {
        case _COMPLETED_YES:
            return COMPLETED_YES;
        case _COMPLETED_NO:
            return COMPLETED_NO;
        case _COMPLETED_MAYBE:
            return COMPLETED_MAYBE;
        default:
            throw new org.omg.CORBA.BAD_PARAM();
        }
    }
    private CompletionStatus(int _value) {
        this._value = _value;
    }
    private int _value;
}
