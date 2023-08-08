public final class AntTasksFactory {
    private AntTasksFactory() {
    }
    public static AntTasksIface create(final String instanceName) {
        try {
            return (AntTasksIface) Class.forName("com.c4j.ant.AntTasksImpl").getConstructor(Class.forName("java.lang.String")).newInstance(instanceName);
        } catch (Exception e) {
            throw new C4JRuntimeException(String.format("Could not create component instance ‘%s’.", instanceName), e);
        }
    }
}
