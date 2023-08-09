public abstract class QueryEval implements Serializable {
    private static final long serialVersionUID = 2675899265640874796L;
    private static ThreadLocal<MBeanServer> server =
        new InheritableThreadLocal<MBeanServer>();
    public void setMBeanServer(MBeanServer s) {
        server.set(s);
    }
    public static MBeanServer getMBeanServer() {
        return server.get();
    }
}
