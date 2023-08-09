final class IdUniquenessPolicyImpl
    extends org.omg.CORBA.LocalObject implements IdUniquenessPolicy {
    public IdUniquenessPolicyImpl(IdUniquenessPolicyValue value) {
        this.value = value;
    }
    public IdUniquenessPolicyValue value() {
        return value;
    }
    public int policy_type()
    {
        return ID_UNIQUENESS_POLICY_ID.value ;
    }
    public Policy copy() {
        return new IdUniquenessPolicyImpl(value);
    }
    public void destroy() {
        value = null;
    }
    private IdUniquenessPolicyValue value;
    public String toString()
    {
        return "IdUniquenessPolicy[" +
            ((value.value() == IdUniquenessPolicyValue._UNIQUE_ID) ?
                "UNIQUE_ID" : "MULTIPLE_ID" + "]") ;
    }
}
