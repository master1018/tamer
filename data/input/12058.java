public class BasicWithSecurityMgr {
    public static ProviderFactory factory;
    public static BasicProvider bp;
    public static void main(String[] args) throws Exception {
        System.setSecurityManager(new SecurityManager());
        factory = ProviderFactory.getDefaultFactory();
        if (factory != null) {
            bp = factory.createProvider(BasicProvider.class);
        }
        testProviderFactory();
        testProbe();
        testProvider();
    }
    static void fail(String s) throws Exception {
        throw new Exception(s);
    }
    static void testProviderFactory() throws Exception {
        if (factory == null) {
            fail("ProviderFactory.getDefaultFactory: Did not create factory");
        }
        if (bp == null) {
            fail("ProviderFactory.createProvider: Did not create provider");
        }
        try {
            factory.createProvider(null);
            fail("ProviderFactory.createProvider: Did not throw NPE for null");
        } catch (NullPointerException e) {}
       try {
           factory.createProvider(InvalidProvider.class);
           fail("Factory.createProvider: Should error with non-void probes");
       } catch (IllegalArgumentException e) {}
    }
    public static void testProvider() throws Exception {
       bp.plainProbe();
       bp.probeWithArgs(42, (float)3.14, "spam", new Long(2L));
       bp.probeWithArgs(42, (float)3.14, null, null);
       bp.probeWithName();
       bp.overloadedProbe();
       bp.overloadedProbe(42);
       Method m = BasicProvider.class.getMethod("plainProbe");
       Probe p = bp.getProbe(m);
       if (p == null) {
           fail("Provider.getProbe: Did not return probe");
       }
       Method m2 = BasicWithSecurityMgr.class.getMethod("testProvider");
       p = bp.getProbe(m2);
       if (p != null) {
           fail("Provider.getProbe: Got probe with invalid spec");
       }
       bp.dispose();
       bp.plainProbe();
       bp.probeWithArgs(42, (float)3.14, "spam", new Long(2L));
       bp.probeWithArgs(42, (float)3.14, null, null);
       bp.probeWithName();
       bp.overloadedProbe();
       bp.overloadedProbe(42);
       if (bp.getProbe(m) != null) {
           fail("Provider.getProbe: Should return null after dispose()");
       }
       bp.dispose(); 
    }
    static void testProbe() throws Exception {
       Method m = BasicProvider.class.getMethod("plainProbe");
       Probe p = bp.getProbe(m);
       p.isEnabled(); 
       p.trigger();
       try {
         p.trigger(0);
         fail("Probe.trigger: too many arguments not caught");
       } catch (IllegalArgumentException e) {}
       p = bp.getProbe(BasicProvider.class.getMethod(
           "probeWithArgs", int.class, float.class, String.class, Long.class));
       try {
         p.trigger();
         fail("Probe.trigger: too few arguments not caught");
       } catch (IllegalArgumentException e) {}
       try {
         p.trigger((float)3.14, (float)3.14, "", new Long(0L));
         fail("Probe.trigger: wrong type primitive arguments not caught");
       } catch (IllegalArgumentException e) {}
    }
}
