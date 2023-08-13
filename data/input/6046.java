public class Sasl {
    private Sasl() {
    }
    public static final String QOP = "javax.security.sasl.qop";
    public static final String STRENGTH = "javax.security.sasl.strength";
    public static final String SERVER_AUTH =
    "javax.security.sasl.server.authentication";
    public static final String MAX_BUFFER = "javax.security.sasl.maxbuffer";
    public static final String RAW_SEND_SIZE = "javax.security.sasl.rawsendsize";
     public static final String REUSE = "javax.security.sasl.reuse";
    public static final String POLICY_NOPLAINTEXT =
    "javax.security.sasl.policy.noplaintext";
    public static final String POLICY_NOACTIVE =
    "javax.security.sasl.policy.noactive";
    public static final String POLICY_NODICTIONARY =
    "javax.security.sasl.policy.nodictionary";
    public static final String POLICY_NOANONYMOUS =
    "javax.security.sasl.policy.noanonymous";
    public static final String POLICY_FORWARD_SECRECY =
    "javax.security.sasl.policy.forward";
    public static final String POLICY_PASS_CREDENTIALS =
    "javax.security.sasl.policy.credentials";
    public static final String CREDENTIALS = "javax.security.sasl.credentials";
    public static SaslClient createSaslClient(
        String[] mechanisms,
        String authorizationId,
        String protocol,
        String serverName,
        Map<String,?> props,
        CallbackHandler cbh) throws SaslException {
        SaslClient mech = null;
        SaslClientFactory fac;
        String className;
        String mechName;
        for (int i = 0; i < mechanisms.length; i++) {
            if ((mechName=mechanisms[i]) == null) {
                throw new NullPointerException(
                    "Mechanism name cannot be null");
            } else if (mechName.length() == 0) {
                continue;
            }
            String mechFilter = "SaslClientFactory." + mechName;
            Provider[] provs = Security.getProviders(mechFilter);
            for (int j = 0; provs != null && j < provs.length; j++) {
                className = provs[j].getProperty(mechFilter);
                if (className == null) {
                    continue;
                }
                fac = (SaslClientFactory) loadFactory(provs[j], className);
                if (fac != null) {
                    mech = fac.createSaslClient(
                        new String[]{mechanisms[i]}, authorizationId,
                        protocol, serverName, props, cbh);
                    if (mech != null) {
                        return mech;
                    }
                }
            }
        }
        return null;
    }
    private static Object loadFactory(Provider p, String className)
        throws SaslException {
        try {
            ClassLoader cl = p.getClass().getClassLoader();
            Class implClass;
            implClass = Class.forName(className, true, cl);
            return implClass.newInstance();
        } catch (ClassNotFoundException e) {
            throw new SaslException("Cannot load class " + className, e);
        } catch (InstantiationException e) {
            throw new SaslException("Cannot instantiate class " + className, e);
        } catch (IllegalAccessException e) {
            throw new SaslException("Cannot access class " + className, e);
        } catch (SecurityException e) {
            throw new SaslException("Cannot access class " + className, e);
        }
    }
    public static SaslServer
        createSaslServer(String mechanism,
                    String protocol,
                    String serverName,
                    Map<String,?> props,
                    javax.security.auth.callback.CallbackHandler cbh)
        throws SaslException {
        SaslServer mech = null;
        SaslServerFactory fac;
        String className;
        if (mechanism == null) {
            throw new NullPointerException("Mechanism name cannot be null");
        } else if (mechanism.length() == 0) {
            return null;
        }
        String mechFilter = "SaslServerFactory." + mechanism;
        Provider[] provs = Security.getProviders(mechFilter);
        for (int j = 0; provs != null && j < provs.length; j++) {
            className = provs[j].getProperty(mechFilter);
            if (className == null) {
                throw new SaslException("Provider does not support " +
                    mechFilter);
            }
            fac = (SaslServerFactory) loadFactory(provs[j], className);
            if (fac != null) {
                mech = fac.createSaslServer(
                    mechanism, protocol, serverName, props, cbh);
                if (mech != null) {
                    return mech;
                }
            }
        }
        return null;
    }
    public static Enumeration<SaslClientFactory> getSaslClientFactories() {
        Set<Object> facs = getFactories("SaslClientFactory");
        final Iterator<Object> iter = facs.iterator();
        return new Enumeration<SaslClientFactory>() {
            public boolean hasMoreElements() {
                return iter.hasNext();
            }
            public SaslClientFactory nextElement() {
                return (SaslClientFactory)iter.next();
            }
        };
    }
    public static Enumeration<SaslServerFactory> getSaslServerFactories() {
        Set<Object> facs = getFactories("SaslServerFactory");
        final Iterator<Object> iter = facs.iterator();
        return new Enumeration<SaslServerFactory>() {
            public boolean hasMoreElements() {
                return iter.hasNext();
            }
            public SaslServerFactory nextElement() {
                return (SaslServerFactory)iter.next();
            }
        };
    }
    private static Set<Object> getFactories(String serviceName) {
        HashSet<Object> result = new HashSet<Object>();
        if ((serviceName == null) || (serviceName.length() == 0) ||
            (serviceName.endsWith("."))) {
            return result;
        }
        Provider[] providers = Security.getProviders();
        HashSet<String> classes = new HashSet<String>();
        Object fac;
        for (int i = 0; i < providers.length; i++) {
            classes.clear();
            for (Enumeration e = providers[i].keys(); e.hasMoreElements(); ) {
                String currentKey = (String)e.nextElement();
                if (currentKey.startsWith(serviceName)) {
                    if (currentKey.indexOf(" ") < 0) {
                        String className = providers[i].getProperty(currentKey);
                        if (!classes.contains(className)) {
                            classes.add(className);
                            try {
                                fac = loadFactory(providers[i], className);
                                if (fac != null) {
                                    result.add(fac);
                                }
                            }catch (Exception ignore) {
                            }
                        }
                    }
                }
            }
        }
        return Collections.unmodifiableSet(result);
    }
}
