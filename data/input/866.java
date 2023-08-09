public class JvmContextFactory implements SnmpUserDataFactory {
    public Object allocateUserData(SnmpPdu requestPdu)
        throws SnmpStatusException {
        return Collections.synchronizedMap(new HashMap<Object, Object>());
    }
    public void releaseUserData(Object userData, SnmpPdu responsePdu)
        throws SnmpStatusException {
        ((Map<?, ?>)userData).clear();
    }
    public static Map<Object, Object> getUserData() {
        final Object userData =
            com.sun.jmx.snmp.ThreadContext.get("SnmpUserData");
        if (userData instanceof Map<?, ?>) return Util.cast(userData);
        else return null;
    }
}
