public class DelegateNameWildcardNameTest {
    public static void main(String[] args) throws Exception {
        System.out.println(
            "Test that <MBeanServerDelegate.DELEGATE_NAME> equals " +
            "<new ObjectName(\"JMImplementation:type=MBeanServerDelegate\")>");
        final ObjectName delegateName =
                new ObjectName("JMImplementation:type=MBeanServerDelegate");
        if (!delegateName.equals(MBeanServerDelegate.DELEGATE_NAME))
            throw new AssertionError("Unexpected value: " +
                    "MBeanServerDelegate.DELEGATE_NAME = " +
                    MBeanServerDelegate.DELEGATE_NAME);
        System.out.println("MBeanServerDelegate.DELEGATE_NAME = " +
                "new ObjectName(\"" + delegateName + "\")");
        System.out.println("Test that <ObjectName.WILDCARD> " +
                           "equals <new ObjectName(\"*:*\")>");
        final ObjectName wildcardName = new ObjectName("*:*");
        if (!wildcardName.equals(ObjectName.WILDCARD))
            throw new AssertionError("Unexpected value: " +
                    "ObjectName.WILDCARD = " +
                    ObjectName.WILDCARD);
        System.out.println("ObjectName.WILDCARD = " +
                "new ObjectName(\"" + wildcardName + "\")");
        System.out.println("Test passes: constants were initialized properly");
    }
}
