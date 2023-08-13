public abstract class AbstractPerfDataBuffer {
    protected PerfDataBufferImpl impl;
    public int getLocalVmId() {
        return impl.getLocalVmId();
    }
    public byte[] getBytes() {
        return impl.getBytes();
    }
    public int getCapacity() {
        return impl.getCapacity();
    }
    public Monitor findByName(String name) throws MonitorException {
        return impl.findByName(name);
    }
    public List<Monitor> findByPattern(String patternString) throws MonitorException {
        return impl.findByPattern(patternString);
    }
    public MonitorStatus getMonitorStatus() throws MonitorException {
        return impl.getMonitorStatus();
    }
    public ByteBuffer getByteBuffer() {
        return impl.getByteBuffer();
    }
    protected void createPerfDataBuffer(ByteBuffer bb, int lvmid)
                   throws MonitorException {
        int majorVersion = AbstractPerfDataBufferPrologue.getMajorVersion(bb);
        int minorVersion = AbstractPerfDataBufferPrologue.getMinorVersion(bb);
        String classname = "sun.jvmstat.perfdata.monitor.v"
                           + majorVersion + "_" + minorVersion
                           + ".PerfDataBuffer";
        try {
            Class<?> implClass = Class.forName(classname);
            Constructor cons = implClass.getConstructor(new Class[] {
                    Class.forName("java.nio.ByteBuffer"),
                    Integer.TYPE
            });
            impl = (PerfDataBufferImpl)cons.newInstance(new Object[] {
                     bb, new Integer(lvmid)
            });
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(
                    "Could not find " + classname + ": " + e.getMessage(), e);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(
                    "Expected constructor missing in " + classname + ": "
                    + e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(
                   "Unexpected constructor access in " + classname + ": "
                   + e.getMessage(), e);
        } catch (InstantiationException e) {
            throw new IllegalArgumentException(
                    classname + "is abstract: " + e.getMessage(), e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof MonitorException) {
                throw (MonitorException)cause;
            }
            throw new RuntimeException("Unexpected exception: "
                                       + e.getMessage() , e);
        }
    }
}
