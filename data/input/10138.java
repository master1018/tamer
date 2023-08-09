public class ImmutableArrayFieldTest {
    public static void main(String[] args) throws Exception {
        boolean ok = true;
        ImmutableDescriptor d = new ImmutableDescriptor(
                new String[] {
                    "strings", "ints", "booleans",
                },
                new Object[] {
                    new String[] {"foo"},
                    new int[] {5},
                    new boolean[] {false},
                });
        String[] strings = (String[]) d.getFieldValue("strings");
        strings[0] = "bar";
        strings = (String[]) d.getFieldValue("strings");
        if (!strings[0].equals("foo")) {
            System.out.println("FAILED: modified string array field");
            ok = false;
        }
        int[] ints = (int[]) d.getFieldValue("ints");
        ints[0] = 0;
        ints = (int[]) d.getFieldValue("ints");
        if (ints[0] != 5) {
            System.out.println("FAILED: modified int array field");
            ok = false;
        }
        boolean[] bools = (boolean[]) d.getFieldValue("booleans");
        bools[0] = true;
        bools = (boolean[]) d.getFieldValue("booleans");
        if (bools[0]) {
            System.out.println("FAILED: modified boolean array field");
            ok = false;
        }
        Object[] values = d.getFieldValues("strings", "ints", "booleans");
        ((String[]) values[0])[0] = "bar";
        ((int[]) values[1])[0] = 0;
        ((boolean[]) values[2])[0] = true;
        values = d.getFieldValues("strings", "ints", "booleans");
        if (!((String[]) values[0])[0].equals("foo") ||
                ((int[]) values[1])[0] != 5 ||
                ((boolean[]) values[2])[0]) {
            System.out.println("FAILED: getFieldValues modifiable: " +
                    Arrays.deepToString(values));
        }
        if (ok)
            System.out.println("TEST PASSED");
        else
            throw new Exception("Array field values were modifiable");
    }
}
