public abstract class JVM_MANAGEMENT_MIB extends SnmpMib implements Serializable {
    public JVM_MANAGEMENT_MIB() {
        mibName = "JVM_MANAGEMENT_MIB";
    }
    public void init() throws IllegalAccessException {
        if (isInitialized == true) {
            return ;
        }
        try  {
            populate(null, null);
        } catch(IllegalAccessException x)  {
            throw x;
        } catch(RuntimeException x)  {
            throw x;
        } catch(Exception x)  {
            throw new Error(x.getMessage());
        }
        isInitialized = true;
    }
    public ObjectName preRegister(MBeanServer server, ObjectName name)
            throws Exception {
        if (isInitialized == true) {
            throw new InstanceAlreadyExistsException();
        }
        this.server = server;
        populate(server, name);
        isInitialized = true;
        return name;
    }
    public void populate(MBeanServer server, ObjectName name)
        throws Exception {
        if (isInitialized == true) {
            return ;
        }
        if (objectserver == null)
            objectserver = new SnmpStandardObjectServer();
        initJvmOS(server);
        initJvmCompilation(server);
        initJvmRuntime(server);
        initJvmThreading(server);
        initJvmMemory(server);
        initJvmClassLoading(server);
        isInitialized = true;
    }
    protected void initJvmOS(MBeanServer server)
        throws Exception {
        final String oid = getGroupOid("JvmOS", "1.3.6.1.4.1.42.2.145.3.163.1.1.6");
        ObjectName objname = null;
        if (server != null) {
            objname = getGroupObjectName("JvmOS", oid, mibName + ":name=sun.management.snmp.jvmmib.JvmOS");
        }
        final JvmOSMeta meta = createJvmOSMetaNode("JvmOS", oid, objname, server);
        if (meta != null) {
            meta.registerTableNodes( this, server );
            final JvmOSMBean group = (JvmOSMBean) createJvmOSMBean("JvmOS", oid, objname, server);
            meta.setInstance( group );
            registerGroupNode("JvmOS", oid, objname, meta, group, server);
        }
    }
    protected JvmOSMeta createJvmOSMetaNode(String groupName,
                String groupOid, ObjectName groupObjname, MBeanServer server)  {
        return new JvmOSMeta(this, objectserver);
    }
    protected abstract Object createJvmOSMBean(String groupName,
                String groupOid,  ObjectName groupObjname, MBeanServer server);
    protected void initJvmCompilation(MBeanServer server)
        throws Exception {
        final String oid = getGroupOid("JvmCompilation", "1.3.6.1.4.1.42.2.145.3.163.1.1.5");
        ObjectName objname = null;
        if (server != null) {
            objname = getGroupObjectName("JvmCompilation", oid, mibName + ":name=sun.management.snmp.jvmmib.JvmCompilation");
        }
        final JvmCompilationMeta meta = createJvmCompilationMetaNode("JvmCompilation", oid, objname, server);
        if (meta != null) {
            meta.registerTableNodes( this, server );
            final JvmCompilationMBean group = (JvmCompilationMBean) createJvmCompilationMBean("JvmCompilation", oid, objname, server);
            meta.setInstance( group );
            registerGroupNode("JvmCompilation", oid, objname, meta, group, server);
        }
    }
    protected JvmCompilationMeta createJvmCompilationMetaNode(String groupName,
                String groupOid, ObjectName groupObjname, MBeanServer server)  {
        return new JvmCompilationMeta(this, objectserver);
    }
    protected abstract Object createJvmCompilationMBean(String groupName,
                String groupOid,  ObjectName groupObjname, MBeanServer server);
    protected void initJvmRuntime(MBeanServer server)
        throws Exception {
        final String oid = getGroupOid("JvmRuntime", "1.3.6.1.4.1.42.2.145.3.163.1.1.4");
        ObjectName objname = null;
        if (server != null) {
            objname = getGroupObjectName("JvmRuntime", oid, mibName + ":name=sun.management.snmp.jvmmib.JvmRuntime");
        }
        final JvmRuntimeMeta meta = createJvmRuntimeMetaNode("JvmRuntime", oid, objname, server);
        if (meta != null) {
            meta.registerTableNodes( this, server );
            final JvmRuntimeMBean group = (JvmRuntimeMBean) createJvmRuntimeMBean("JvmRuntime", oid, objname, server);
            meta.setInstance( group );
            registerGroupNode("JvmRuntime", oid, objname, meta, group, server);
        }
    }
    protected JvmRuntimeMeta createJvmRuntimeMetaNode(String groupName,
                String groupOid, ObjectName groupObjname, MBeanServer server)  {
        return new JvmRuntimeMeta(this, objectserver);
    }
    protected abstract Object createJvmRuntimeMBean(String groupName,
                String groupOid,  ObjectName groupObjname, MBeanServer server);
    protected void initJvmThreading(MBeanServer server)
        throws Exception {
        final String oid = getGroupOid("JvmThreading", "1.3.6.1.4.1.42.2.145.3.163.1.1.3");
        ObjectName objname = null;
        if (server != null) {
            objname = getGroupObjectName("JvmThreading", oid, mibName + ":name=sun.management.snmp.jvmmib.JvmThreading");
        }
        final JvmThreadingMeta meta = createJvmThreadingMetaNode("JvmThreading", oid, objname, server);
        if (meta != null) {
            meta.registerTableNodes( this, server );
            final JvmThreadingMBean group = (JvmThreadingMBean) createJvmThreadingMBean("JvmThreading", oid, objname, server);
            meta.setInstance( group );
            registerGroupNode("JvmThreading", oid, objname, meta, group, server);
        }
    }
    protected JvmThreadingMeta createJvmThreadingMetaNode(String groupName,
                String groupOid, ObjectName groupObjname, MBeanServer server)  {
        return new JvmThreadingMeta(this, objectserver);
    }
    protected abstract Object createJvmThreadingMBean(String groupName,
                String groupOid,  ObjectName groupObjname, MBeanServer server);
    protected void initJvmMemory(MBeanServer server)
        throws Exception {
        final String oid = getGroupOid("JvmMemory", "1.3.6.1.4.1.42.2.145.3.163.1.1.2");
        ObjectName objname = null;
        if (server != null) {
            objname = getGroupObjectName("JvmMemory", oid, mibName + ":name=sun.management.snmp.jvmmib.JvmMemory");
        }
        final JvmMemoryMeta meta = createJvmMemoryMetaNode("JvmMemory", oid, objname, server);
        if (meta != null) {
            meta.registerTableNodes( this, server );
            final JvmMemoryMBean group = (JvmMemoryMBean) createJvmMemoryMBean("JvmMemory", oid, objname, server);
            meta.setInstance( group );
            registerGroupNode("JvmMemory", oid, objname, meta, group, server);
        }
    }
    protected JvmMemoryMeta createJvmMemoryMetaNode(String groupName,
                String groupOid, ObjectName groupObjname, MBeanServer server)  {
        return new JvmMemoryMeta(this, objectserver);
    }
    protected abstract Object createJvmMemoryMBean(String groupName,
                String groupOid,  ObjectName groupObjname, MBeanServer server);
    protected void initJvmClassLoading(MBeanServer server)
        throws Exception {
        final String oid = getGroupOid("JvmClassLoading", "1.3.6.1.4.1.42.2.145.3.163.1.1.1");
        ObjectName objname = null;
        if (server != null) {
            objname = getGroupObjectName("JvmClassLoading", oid, mibName + ":name=sun.management.snmp.jvmmib.JvmClassLoading");
        }
        final JvmClassLoadingMeta meta = createJvmClassLoadingMetaNode("JvmClassLoading", oid, objname, server);
        if (meta != null) {
            meta.registerTableNodes( this, server );
            final JvmClassLoadingMBean group = (JvmClassLoadingMBean) createJvmClassLoadingMBean("JvmClassLoading", oid, objname, server);
            meta.setInstance( group );
            registerGroupNode("JvmClassLoading", oid, objname, meta, group, server);
        }
    }
    protected JvmClassLoadingMeta createJvmClassLoadingMetaNode(String groupName,
                String groupOid, ObjectName groupObjname, MBeanServer server)  {
        return new JvmClassLoadingMeta(this, objectserver);
    }
    protected abstract Object createJvmClassLoadingMBean(String groupName,
                String groupOid,  ObjectName groupObjname, MBeanServer server);
    public void registerTableMeta( String name, SnmpMibTable meta) {
        if (metadatas == null) return;
        if (name == null) return;
        metadatas.put(name,meta);
    }
    public SnmpMibTable getRegisteredTableMeta( String name ) {
        if (metadatas == null) return null;
        if (name == null) return null;
        return metadatas.get(name);
    }
    public SnmpStandardObjectServer getStandardObjectServer() {
        if (objectserver == null)
            objectserver = new SnmpStandardObjectServer();
        return objectserver;
    }
    private boolean isInitialized = false;
    protected SnmpStandardObjectServer objectserver;
    protected final Hashtable<String, SnmpMibTable> metadatas =
            new Hashtable<String, SnmpMibTable>();
}
