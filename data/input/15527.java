public class StartTlsRequest implements ExtendedRequest {
    public static final String OID = "1.3.6.1.4.1.1466.20037";
    public StartTlsRequest() {
    }
    public String getID() {
        return OID;
    }
    public byte[] getEncodedValue() {
        return null;
    }
    public ExtendedResponse createExtendedResponse(String id, byte[] berValue,
        int offset, int length) throws NamingException {
        if ((id != null) && (!id.equals(OID))) {
            throw new ConfigurationException(
                "Start TLS received the following response instead of " +
                OID + ": " + id);
        }
        StartTlsResponse resp = null;
        ServiceLoader<StartTlsResponse> sl = ServiceLoader.load(
                StartTlsResponse.class, getContextClassLoader());
        Iterator<StartTlsResponse> iter = sl.iterator();
        while (resp == null && privilegedHasNext(iter)) {
            resp = iter.next();
        }
        if (resp != null) {
            return resp;
        }
        try {
            VersionHelper helper = VersionHelper.getVersionHelper();
            Class clas = helper.loadClass(
                "com.sun.jndi.ldap.ext.StartTlsResponseImpl");
            resp = (StartTlsResponse) clas.newInstance();
        } catch (IllegalAccessException e) {
            throw wrapException(e);
        } catch (InstantiationException e) {
            throw wrapException(e);
        } catch (ClassNotFoundException e) {
            throw wrapException(e);
        }
        return resp;
    }
    private ConfigurationException wrapException(Exception e) {
        ConfigurationException ce = new ConfigurationException(
            "Cannot load implementation of javax.naming.ldap.StartTlsResponse");
        ce.setRootCause(e);
        return ce;
    }
    private final ClassLoader getContextClassLoader() {
        return (ClassLoader) AccessController.doPrivileged(
            new PrivilegedAction() {
                public Object run() {
                    return Thread.currentThread().getContextClassLoader();
                }
            }
        );
    }
    private final static boolean privilegedHasNext(final Iterator iter) {
        Boolean answer = (Boolean) AccessController.doPrivileged(
            new PrivilegedAction() {
            public Object run() {
                return Boolean.valueOf(iter.hasNext());
            }
        });
        return answer.booleanValue();
    }
    private static final long serialVersionUID = 4441679576360753397L;
}
