public class InvalidServiceTag {
    private final static int MAX_CONTAINER_LEN = 64 - 1;
    public static void main(String[] argv) throws Exception {
        ServiceTag st1 = ServiceTag.newInstance("product name",
                                                "product version",
                                                "product urn",
                                                "product parent",
                                                "product parent urn",
                                                "product defined instance ID",
                                                "product vendor",
                                                "platform arch",
                                                "container",
                                                "source");
        ServiceTag st2 = ServiceTag.newInstance("product name",
                                                "product version",
                                                "product urn",
                                                "product parent",
                                                "",
                                                "",
                                                "product vendor",
                                                "platform arch",
                                                "container",
                                                "source");
        setInvalidContainer("");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i <= MAX_CONTAINER_LEN; i++) {
            sb.append('x');
        }
        setInvalidContainer(sb.toString());
        System.out.println("Test passed.");
    }
    private static void setInvalidContainer(String container) {
        boolean exceptionThrown = false;
        try {
            ServiceTag st2 = ServiceTag.newInstance("product name",
                                                    "product version",
                                                    "product urn",
                                                    "product parent",
                                                    "product parent urn",
                                                    "product defined instance ID",
                                                    "product vendor",
                                                    "platform arch",
                                                    container,
                                                    "source");
        } catch (IllegalArgumentException iae) {
            iae.printStackTrace();
            exceptionThrown = true;
        }
        if (!exceptionThrown) {
            throw new RuntimeException("IllegalArgumentException not thrown");
        }
    }
}
