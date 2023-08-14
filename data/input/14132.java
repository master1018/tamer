public class AnnotationSecurityTest {
    public AnnotationSecurityTest() {
    }
    public static void main(String[] argv) {
        AnnotationSecurityTest test = new AnnotationSecurityTest();
        test.run();
    }
    public void run() {
        try {
            final String testSrc = System.getProperty("test.src");
            final String codeBase = System.getProperty("test.classes");
            final String policy = testSrc + File.separator +
                    "AnnotationSecurityTest.policy";
            final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            final File pf = new File(policy);
            if (!pf.exists())
                throw new IOException("policy file not found: " + policy);
            if (!pf.canRead())
                throw new IOException("policy file not readable: " + policy);
            System.out.println("Policy="+policy);
            System.setProperty("java.security.policy",policy);
            System.setSecurityManager(new SecurityManager());
            try {
                final Method m1 =
                        DescribedMBean.class.getMethod("getStringProp");
                final Method m2 =
                        DescribedMBean.class.getMethod("setStringProp",
                            String.class);
                m1.getAnnotations();
                m2.getAnnotations();
            } catch (SecurityException x) {
                System.err.println("ERROR: 6370080 not fixed.");
                throw new IllegalStateException("ERROR: 6370080 not fixed.",x);
            }
            final ObjectName name1 =
                    new ObjectName("defaultDomain:class=UnDescribed");
            UnDescribed unDescribedMBean = new UnDescribed();
            System.out.println("\nWe register an MBean where DescriptorKey is " +
                    "not used at all");
            mbs.registerMBean(unDescribedMBean, name1);
            System.out.println("\n\tOK - The MBean "
                    + name1 + " is registered = " + mbs.isRegistered(name1));
            final ObjectName name2 =
                    new ObjectName("defaultDomain:class=Described");
            final Described describedMBean = new Described();
            System.out.println("\nWe register an MBean with exactly the " +
                    "same management"
                    + " interface as above and where DescriptorKey is used.");
            mbs.registerMBean(describedMBean, name2);
            System.out.println("\n\tOK - The MBean "
                    + name2 + " is registered = " + mbs.isRegistered(name2));
            final ObjectName name3 =
                    new ObjectName("defaultDomain:class=DescribedMX");
            final DescribedMX describedMXBean = new DescribedMX();
            System.out.println("\nWe register an MXBean with exactly the " +
                    "same management"
                    + " interface as above and where DescriptorKey is used.");
            mbs.registerMBean(describedMXBean, name3);
            System.out.println("\n\tOK - The MXBean "
                    + name3 + " is registered = " + mbs.isRegistered(name3));
            System.out.println("\nAll three MBeans correctly registered...");
            try {
                System.err.println("Trying getStringProp() - should fail");
                mbs.getAttribute(name1,"StringProp");
                System.err.println("ERROR: didn't get expected SecurityException"
                        +"\n\t check security configuration & policy file: "+
                        policy);
                throw new RuntimeException("getStringProp() did not get a " +
                        "SecurityException!");
            } catch (SecurityException x) {
                System.err.println("getStringProp() - failed");
            }
         } catch (Exception t) {
            t.printStackTrace();
            if (t instanceof RuntimeException)
                throw (RuntimeException)t;
            else throw new RuntimeException(t);
        }
    }
}
