public class UnionTest {
    public static void main(String[] args) throws Exception {
        ImmutableDescriptor immutableEmpty = new ImmutableDescriptor();
        DescriptorSupport mutableEmpty = new DescriptorSupport();
        checkEmpty(union());
        checkEmpty(union(immutableEmpty));
        checkEmpty(union(mutableEmpty));
        checkEmpty(union(EMPTY_DESCRIPTOR, immutableEmpty, mutableEmpty));
        checkEmpty(union(null, immutableEmpty, null));
        ImmutableDescriptor immutableNumbers =
            new ImmutableDescriptor(new String[] {"one", "two", "three"},
                                    new Object[] {1, 2, 3});
        final String[] noNames = null;
        DescriptorSupport mutableNumbers =
            new DescriptorSupport(immutableNumbers.getFieldNames(),
                                  immutableNumbers.getFieldValues(noNames));
        ImmutableDescriptor immutableOne =
            new ImmutableDescriptor(Collections.singletonMap("one", 1));
        DescriptorSupport mutableOne =
            new DescriptorSupport(new String[] {"one"}, new Object[] {1});
        ImmutableDescriptor immutableTwo =
            new ImmutableDescriptor(Collections.singletonMap("two", 2));
        DescriptorSupport mutableTwo =
            new DescriptorSupport(new String[] {"two"}, new Object[] {2});
        ImmutableDescriptor immutableOneTwo =
            new ImmutableDescriptor(new String[] {"one", "two"},
                                    new Object[] {1, 2});
        checkEqual(union(immutableNumbers), immutableNumbers);
        checkEqual(union(immutableNumbers, mutableNumbers), immutableNumbers);
        checkEqual(union(mutableNumbers, immutableNumbers), immutableNumbers);
        checkEqual(union(mutableEmpty, immutableEmpty, immutableNumbers,
                         mutableNumbers, immutableOne), immutableNumbers);
        checkEqual(union(immutableOne, immutableTwo, immutableNumbers),
                   immutableNumbers);
        checkEquivalent(union(immutableOne, mutableNumbers), immutableNumbers);
        checkEquivalent(union(immutableOne, immutableTwo), immutableOneTwo);
        checkEquivalent(union(mutableOne, mutableTwo), immutableOneTwo);
        if (failure != null)
            throw new Exception("TEST FAILED: " + failure);
        System.out.println("TEST PASSED");
    }
    private static void checkEmpty(ImmutableDescriptor d) {
        if (d != EMPTY_DESCRIPTOR) {
            failure = "Union of empty descriptors should be " +
                "ImmutableDescriptor.EMPTY";
            System.err.println("FAILED: " + failure);
            Thread.dumpStack();
        }
    }
    private static void checkEqual(ImmutableDescriptor d,
                                   ImmutableDescriptor e) {
        if (d != e) {
            failure = "Union should produce one of its arguments but does not";
            System.err.println("FAILED: " + failure);
            Thread.dumpStack();
        }
    }
    private static void checkEquivalent(ImmutableDescriptor d,
                                        ImmutableDescriptor e) {
        if (!d.equals(e)) {
            failure = "Union produced this: " + d + "; but should have " +
                "produced this: " + e;
            System.err.println("FAILED: " + failure);
            Thread.dumpStack();
        }
    }
    private static String failure;
}
