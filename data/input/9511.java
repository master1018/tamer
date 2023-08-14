public class MonitoringManagerFactoryImpl implements MonitoringManagerFactory {
    private HashMap monitoringManagerTable = new HashMap();
    public synchronized MonitoringManager createMonitoringManager(
        String nameOfTheRoot, String description )
    {
        MonitoringManagerImpl m = null;
        m = (MonitoringManagerImpl)monitoringManagerTable.get(nameOfTheRoot);
        if (m == null) {
            m = new MonitoringManagerImpl( nameOfTheRoot, description );
            monitoringManagerTable.put(nameOfTheRoot, m);
        }
        return m;
    }
}
