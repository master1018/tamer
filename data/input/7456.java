final class ImplicitActivationPolicyImpl
    extends org.omg.CORBA.LocalObject implements ImplicitActivationPolicy {
    public
        ImplicitActivationPolicyImpl(ImplicitActivationPolicyValue
                                     value) {
        this.value = value;
    }
    public ImplicitActivationPolicyValue value() {
        return value;
    }
    public int policy_type()
    {
        return IMPLICIT_ACTIVATION_POLICY_ID.value ;
    }
    public Policy copy() {
        return new ImplicitActivationPolicyImpl(value);
    }
    public void destroy() {
        value = null;
    }
    private ImplicitActivationPolicyValue value;
    public String toString()
    {
        return "ImplicitActivationPolicy[" +
            ((value.value() == ImplicitActivationPolicyValue._IMPLICIT_ACTIVATION) ?
                "IMPLICIT_ACTIVATION" : "NO_IMPLICIT_ACTIVATION" + "]") ;
    }
}
