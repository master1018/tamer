public class SetOverrideType implements org.omg.CORBA.portable.IDLEntity {
    public static final int _SET_OVERRIDE = 0;
    public static final int _ADD_OVERRIDE = 1;
    public static final SetOverrideType SET_OVERRIDE = new SetOverrideType(_SET_OVERRIDE);
    public static final SetOverrideType ADD_OVERRIDE = new SetOverrideType(_ADD_OVERRIDE);
    public int value() {
        return _value;
    }
    public static SetOverrideType from_int(int i)
    {
        switch (i) {
        case _SET_OVERRIDE:
            return SET_OVERRIDE;
        case _ADD_OVERRIDE:
            return ADD_OVERRIDE;
        default:
            throw new org.omg.CORBA.BAD_PARAM();
        }
    }
    protected SetOverrideType(int _value){
        this._value = _value;
    }
    private int _value;
}
