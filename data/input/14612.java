public class ConnectorAddressLink {
    private static final String CONNECTOR_ADDRESS_COUNTER =
            "sun.management.JMXConnectorServer.address";
    private static final String REMOTE_CONNECTOR_COUNTER_PREFIX =
            "sun.management.JMXConnectorServer.";
    private static AtomicInteger counter = new AtomicInteger();
    public static void export(String address) {
        if (address == null || address.length() == 0) {
            throw new IllegalArgumentException("address not specified");
        }
        Perf perf = Perf.getPerf();
        perf.createString(
                CONNECTOR_ADDRESS_COUNTER, 1, Units.STRING.intValue(), address);
    }
    public static String importFrom(int vmid) throws IOException {
        Perf perf = Perf.getPerf();
        ByteBuffer bb;
        try {
            bb = perf.attach(vmid, "r");
        } catch (IllegalArgumentException iae) {
            throw new IOException(iae.getMessage());
        }
        List counters =
                new PerfInstrumentation(bb).findByPattern(CONNECTOR_ADDRESS_COUNTER);
        Iterator i = counters.iterator();
        if (i.hasNext()) {
            Counter c = (Counter) i.next();
            return (String) c.getValue();
        } else {
            return null;
        }
    }
    public static void exportRemote(Map<String, String> properties) {
        final int index = counter.getAndIncrement();
        Perf perf = Perf.getPerf();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            perf.createString(REMOTE_CONNECTOR_COUNTER_PREFIX + index + "." +
                    entry.getKey(), 1, Units.STRING.intValue(), entry.getValue());
        }
    }
    public static Map<String, String> importRemoteFrom(int vmid) throws IOException {
        Perf perf = Perf.getPerf();
        ByteBuffer bb;
        try {
            bb = perf.attach(vmid, "r");
        } catch (IllegalArgumentException iae) {
            throw new IOException(iae.getMessage());
        }
        List counters = new PerfInstrumentation(bb).getAllCounters();
        Map<String, String> properties = new HashMap<String, String>();
        for (Object c : counters) {
            String name = ((Counter) c).getName();
            if (name.startsWith(REMOTE_CONNECTOR_COUNTER_PREFIX) &&
                    !name.equals(CONNECTOR_ADDRESS_COUNTER)) {
                properties.put(name, ((Counter) c).getValue().toString());
            }
        }
        return properties;
    }
}
