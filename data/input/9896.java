public class RegistrationData {
    private final Map<String, String> environment = initEnvironment();
    private final Map<String, ServiceTag> svcTagMap =
        new LinkedHashMap<String, ServiceTag>();
    private final String urn;
    public RegistrationData() {
        this(Util.generateURN());
        SystemEnvironment sysEnv = SystemEnvironment.getSystemEnvironment();
        setEnvironment(ST_NODE_HOSTNAME, sysEnv.getHostname());
        setEnvironment(ST_NODE_HOST_ID, sysEnv.getHostId());
        setEnvironment(ST_NODE_OS_NAME, sysEnv.getOsName());
        setEnvironment(ST_NODE_OS_VERSION, sysEnv.getOsVersion());
        setEnvironment(ST_NODE_OS_ARCH, sysEnv.getOsArchitecture());
        setEnvironment(ST_NODE_SYSTEM_MODEL, sysEnv.getSystemModel());
        setEnvironment(ST_NODE_SYSTEM_MANUFACTURER, sysEnv.getSystemManufacturer());
        setEnvironment(ST_NODE_CPU_MANUFACTURER, sysEnv.getCpuManufacturer());
        setEnvironment(ST_NODE_SERIAL_NUMBER, sysEnv.getSerialNumber());
    }
    RegistrationData(String urn) {
        this.urn = urn;
    }
    private Map<String, String> initEnvironment() {
        Map<String, String> map = new LinkedHashMap<String, String>();
        map.put(ST_NODE_HOSTNAME, "");
        map.put(ST_NODE_HOST_ID, "");
        map.put(ST_NODE_OS_NAME, "");
        map.put(ST_NODE_OS_VERSION, "");
        map.put(ST_NODE_OS_ARCH, "");
        map.put(ST_NODE_SYSTEM_MODEL, "");
        map.put(ST_NODE_SYSTEM_MANUFACTURER, "");
        map.put(ST_NODE_CPU_MANUFACTURER, "");
        map.put(ST_NODE_SERIAL_NUMBER, "");
        return map;
    }
    public String getRegistrationURN() {
        return urn;
    }
    public Map<String, String> getEnvironmentMap() {
        return new LinkedHashMap<String,String>(environment);
    }
    public void setEnvironment(String name, String value) {
        if (name == null) {
            throw new NullPointerException("name is null");
        }
        if (value == null) {
            throw new NullPointerException("value is null");
        }
        if (environment.containsKey(name)) {
            if (name.equals(ST_NODE_HOSTNAME) || name.equals(ST_NODE_OS_NAME)) {
                if (value.length() == 0) {
                    throw new IllegalArgumentException("\"" +
                        name + "\" requires non-empty value.");
                }
            }
            environment.put(name, value);
        } else {
            throw new IllegalArgumentException("\"" +
                 name + "\" is not an environment element.");
        }
    }
    public Set<ServiceTag> getServiceTags() {
        return new HashSet<ServiceTag>(svcTagMap.values());
    }
    public synchronized ServiceTag addServiceTag(ServiceTag st) {
        ServiceTag svcTag = ServiceTag.newInstanceWithUrnTimestamp(st);
        String instanceURN = svcTag.getInstanceURN();
        if (svcTagMap.containsKey(instanceURN)) {
            throw new IllegalArgumentException("Instance_urn = " + instanceURN +
                    " already exists in the registration data.");
        } else {
            svcTagMap.put(instanceURN, svcTag);
        }
        return svcTag;
    }
    public synchronized ServiceTag getServiceTag(String instanceURN) {
        if (instanceURN == null) {
            throw new NullPointerException("instanceURN is null");
        }
        return svcTagMap.get(instanceURN);
    }
    public synchronized ServiceTag removeServiceTag(String instanceURN) {
        if (instanceURN == null) {
            throw new NullPointerException("instanceURN is null");
        }
        ServiceTag svcTag = null;
        if (svcTagMap.containsKey(instanceURN)) {
            svcTag = svcTagMap.remove(instanceURN);
        }
        return svcTag;
    }
    public synchronized ServiceTag updateServiceTag(String instanceURN,
                                                    String productDefinedInstanceID) {
        ServiceTag svcTag = getServiceTag(instanceURN);
        if (svcTag == null) {
            return null;
        }
        svcTag = ServiceTag.newInstanceWithUrnTimestamp(svcTag);
        svcTag.setProductDefinedInstanceID(productDefinedInstanceID);
        svcTagMap.put(instanceURN, svcTag);
        return svcTag;
    }
    public static RegistrationData loadFromXML(InputStream in) throws IOException {
        try {
            return RegistrationDocument.load(in);
        } finally {
            in.close();
        }
    }
    public void storeToXML(OutputStream os) throws IOException {
        RegistrationDocument.store(os, this);
        os.flush();
    }
    public byte[] toXML() {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            storeToXML(out);
            return out.toByteArray();
        } catch (IOException e) {
            return new byte[0];
        }
    }
    @Override
    public String toString() {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            storeToXML(out);
            return out.toString("UTF-8");
        } catch (IOException e) {
            return "Error creating the return string.";
        }
    }
}
