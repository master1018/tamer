public class Installed {
    public static void main(String[] args) throws Exception {
        ServiceConfiguration.installServiceConfigurationFile();
        TestProvider.exerciseTestProvider(
            TestProvider2.loadClassReturn,
            TestProvider2.loadProxyClassReturn,
            TestProvider2.getClassLoaderReturn,
            TestProvider2.getClassAnnotationReturn,
            TestProvider2.invocations);
    }
}
