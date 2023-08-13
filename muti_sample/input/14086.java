public class ImmutableDescriptorSetFieldsTest {
    public static void main(String[] args) throws Exception {
        boolean ok = true;
        ImmutableDescriptor d = new ImmutableDescriptor("k=v");
        try {
            System.out.println(
                "Call ImmutableDescriptor.setFields(fieldNames,fieldValues) " +
                "with empty name in field names array");
            String fieldNames[] = { "a", "", "c" };
            Object fieldValues[] = { 1, 2, 3 };
            d.setFields(fieldNames, fieldValues);
            System.out.println("Didn't get expected exception");
            ok = false;
        } catch (RuntimeOperationsException e) {
            if (e.getCause() instanceof IllegalArgumentException) {
                System.out.println("Got expected exception:");
                ok = true;
            } else {
                System.out.println("Got unexpected exception:");
                ok = false;
            }
            e.printStackTrace(System.out);
        } catch (Exception e) {
            System.out.println("Got unexpected exception:");
            ok = false;
            e.printStackTrace(System.out);
        }
        try {
            System.out.println(
                "Call ImmutableDescriptor.setFields(fieldNames,fieldValues) " +
                "with null name in field names array");
            String fieldNames[] = { "a", null, "c" };
            Object fieldValues[] = { 1, 2, 3 };
            d.setFields(fieldNames, fieldValues);
            System.out.println("Didn't get expected exception");
            ok = false;
        } catch (RuntimeOperationsException e) {
            if (e.getCause() instanceof IllegalArgumentException) {
                System.out.println("Got expected exception:");
                ok = true;
            } else {
                System.out.println("Got unexpected exception:");
                ok = false;
            }
            e.printStackTrace(System.out);
        } catch (Exception e) {
            System.out.println("Got unexpected exception:");
            ok = false;
            e.printStackTrace(System.out);
        }
        if (ok) {
            System.out.println("TEST PASSED");
        } else {
            System.out.println("TEST FAILED");
            throw new Exception("Got unexpected exceptions");
        }
    }
}
