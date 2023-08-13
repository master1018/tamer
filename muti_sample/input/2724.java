public class CloneArray {
    static Object replacement;
    static class Resolver implements Serializable {
        private Object readResolve() throws ObjectStreamException {
            return replacement;
        }
    }
    private static void test(Object rep)
        throws IOException, ClassNotFoundException {
        try(ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            try(ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(new Resolver());
                oos.writeObject(new Resolver());
            }
            Object o1;
            Object o2;
            try(ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                ObjectInputStream ois = new ObjectInputStream(bais)) {
                replacement = rep;
                o1 = ois.readUnshared();
                o2 = ois.readUnshared();
            }
            if (o1 == o2)
                throw new AssertionError("o1 and o2 must not be identical");
        }
    }
    public static void main(String[] args)
        throws IOException, ClassNotFoundException {
        Object[] replacements = {
            new byte[]    {1},
            new char[]    {'2'},
            new short[]   {3},
            new int[]     {4},
            new long[]    {5},
            new float[]   {6.0f},
            new double[]  {7.0},
            new boolean[] {true},
            new Object[] {"A string."}
        };
        for(Object replacement : replacements) {
            test(replacement);
        }
    }
}
