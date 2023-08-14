public class CheckDefaultGroupName {
    public static void main(String[] args) {
        System.out.println("\n\nRegression test for, 4252236\n\n");
        ActivationGroupDesc groupDesc =
            new ActivationGroupDesc(null, null);
        String className = groupDesc.getClassName();
        if (className != null) {
            TestLibrary.bomb("ActivationGroupDesc had incorrect default" +
                             " group implementation class name: " + className);
        } else {
            System.err.println("test passed, had correct default group" +
                               " implementation class name: " + className +
                               "\n\n");
        }
    }
}
