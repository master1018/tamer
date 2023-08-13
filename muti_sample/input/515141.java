public class SpiEngUtils {
    public static final String[] invalidValues = {
            "",
            "BadAlgorithm",
            "Long message Long message Long message Long message Long message Long message Long message Long message Long message Long message Long message Long message Long message" };
    public static Provider isSupport(String algorithm, String service) {
        try {
            Provider[] provs = Security.getProviders(service.concat(".")
                    .concat(algorithm));
            if (provs == null) {
                return null;
            }
            return (provs.length == 0 ? null : provs[0]);
        } catch (Exception e) {
            return null;
        }
    }
    public class MyProvider extends Provider {
        public MyProvider(String name, String info, String key, String clName) {
            super(name, 1.0, info);
            put(key, clName);
        }
    }
}