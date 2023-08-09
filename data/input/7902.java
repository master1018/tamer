public class PropertiesTest {
    private static int NUM_MYPROPS = 3;
    public static void main(String[] argv) throws Exception {
        Properties props = System.getProperties();
        try {
            System.setProperties(new Properties(props));
            runTest(props.size());
        } finally {
            System.setProperties(props);
        }
    }
    private static void runTest(int sysPropsCount) throws Exception {
        Properties myProps = new Properties( System.getProperties() );
        myProps.put("good.property.1", "good.value.1");
        myProps.put("good.property.2", "good.value.2");
        myProps.put("good.property.3", "good.value.3");
        myProps.put("good.property.4", new Integer(4));
        myProps.put(new Integer(5), "good.value.5");
        myProps.put(new Object(), new Object());
        System.setProperties(myProps);
        Map<String,String> props =
            ManagementFactory.getRuntimeMXBean().getSystemProperties();
        int i=0;
        for (Map.Entry<String,String> e : props.entrySet()) {
            String key = e.getKey();
            String value = e.getValue();
            System.out.println(i++ + ": " + key + " : " + value);
        }
        if (props.size() != NUM_MYPROPS + sysPropsCount) {
            throw new RuntimeException("Test Failed: " +
                "Expected number of properties = " +
                NUM_MYPROPS + sysPropsCount +
                " but found = " + props.size());
        }
    }
}
