public class CompositeDataStringTest {
    public static void main(String[] args) throws Exception {
        CompositeType basicCT = new CompositeType(
                "basicCT", "basic CompositeType",
                new String[] {"name", "value"},
                new String[] {"name", "value"},
                new OpenType<?>[] {SimpleType.STRING, SimpleType.INTEGER});
        CompositeType ct = new CompositeType(
                "noddy", "descr",
                new String[] {"strings", "ints", "cds"},
                new String[] {"string array", "int array", "composite data array"},
                new OpenType<?>[] {
                    ArrayType.getArrayType(SimpleType.STRING),
                    ArrayType.getPrimitiveArrayType(int[].class),
                    ArrayType.getArrayType(basicCT)
                });
        CompositeData basicCD1 = new CompositeDataSupport(
                basicCT, new String[] {"name", "value"}, new Object[] {"ceathar", 4});
        CompositeData basicCD2 = new CompositeDataSupport(
                basicCT, new String[] {"name", "value"}, new Object[] {"naoi", 9});
        CompositeData cd = new CompositeDataSupport(
                ct,
                new String[] {"strings", "ints", "cds"},
                new Object[] {
                    new String[] {"fred", "jim", "sheila"},
                    new int[] {2, 3, 5, 7},
                    new CompositeData[] {basicCD1, basicCD2}
                });
        String s = cd.toString();
        System.out.println("CompositeDataSupport.toString(): " + s);
        String[] expected = {
            "fred, jim, sheila",
            "2, 3, 5, 7",
            "ceathar",
            "naoi",
        };
        boolean ok = true;
        for (String expect : expected) {
            if (s.contains(expect))
                System.out.println("OK: string contains <" + expect + ">");
            else {
                ok = false;
                System.out.println("NOT OK: string does not contain <" +
                        expect + ">");
            }
        }
        if (ok)
            System.out.println("TEST PASSED");
        else
            throw new Exception("TEST FAILED: string did not contain expected substrings");
    }
}
