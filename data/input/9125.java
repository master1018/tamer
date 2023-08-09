public class GetInstanceProvider extends Provider {
    public GetInstanceProvider() {
        super("GetInstanceProvider",
                1,
                "GetInstanceProvider: Configuration.GetInstanceConfigSpi");
        AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                put("Configuration.GetInstanceConfigSpi",
                        "GetInstanceConfigSpi");
                return null;
            }
        });
    }
}
