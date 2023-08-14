public class TypeSafeEnum implements Serializable, ObjectInputValidation {
    private static int numWriteObject = 0;
    private static int numReadObject = 0;
    private String value;
    private TypeSafeEnum(String value) {
        this.value = value;
    }
    final public static TypeSafeEnum FIRST = new TypeSafeEnum("First");
    final public static TypeSafeEnum SECOND = new TypeSafeEnum("Second");
    final public static TypeSafeEnum THIRD = new TypeSafeEnum("Third");
    static int numReadResolve = 0;
    static int numWriteReplace = 0;
    static boolean verbose = false;
    private Object writeReplace() throws IOException {
        numWriteReplace++;
        if (verbose) {
            System.out.println("TypeSafeEnum.writeReplace() " +
                               this.toString());
        }
        return this;
    }
    private Object readResolve() throws IOException {
        numReadResolve++;
        if (verbose) {
            System.out.println("readResolve called on " + this.toString());
        }
        if (value.equals(FIRST.value)) {
            return FIRST;
        } else if (value.equals(SECOND.value)) {
            return SECOND;
        } else if (value.equals(THIRD.value)) {
            return THIRD;
        } else {
            return this;
        }
    }
    private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        numReadObject++;
        in.defaultReadObject();
        if (verbose) {
            System.out.println("TypeSafeEnum.readObject() " + this.toString());
        }
        if (value == null) {
            in.registerValidation(this, 0);
        }
    }
    public void validateObject() throws InvalidObjectException {
        if (numWriteObject != 4) {
            throw new Error("Expected 4 calls to writeObject, only " +
                            numWriteObject + " made");
        }
        if (numReadObject != 4) {
            throw new Error("Expected 4 calls to readObject, only " +
                            numReadObject + " made");
        }
        if (numWriteReplace != 4) {
            throw new Error("Expected 4 calls to writeReplace, only " +
                            numWriteReplace + " made");
        }
        if (numReadResolve != 4) {
            throw new Error("Expected 4 calls to readResolve, only " +
                            numReadResolve + " made");
        }
    }
    private void writeObject(ObjectOutputStream out) throws IOException
    {
        numWriteObject++;
        out.defaultWriteObject();
        if (verbose) {
            System.out.println("TypeSafeEnum.writeObject() " +
                               this.toString());
        }
    }
    public String toString() {
        return super.toString() + " value=" + value;
    }
    public static void main(String args[])
        throws IOException, ClassNotFoundException
    {
        if (args.length > 0 && args[0].equals("-verbose"))
            verbose = true;
        TypeSafeEnum[] writeArray = new TypeSafeEnum[7];
        writeArray[0] = FIRST;
        writeArray[1] = SECOND;
        writeArray[2] = THIRD;
        writeArray[3] = FIRST;
        writeArray[4] = SECOND;
        writeArray[5] = THIRD;
        writeArray[6] = new TypeSafeEnum("Third");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(baos);
        os.writeObject(writeArray);
        os.close();
        TypeSafeEnum[] readArray;
        ObjectInputStream in =
           new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
        readArray = (TypeSafeEnum[])in.readObject();
        in.close();
        for (int i= 0; i < writeArray.length - 1 ; i++) {
            if (writeArray[i] != readArray[i]) {
                throw new Error("Serializa/deserialize did not preserve " +
                                "singleton for " +
                                readArray[i].toString() +
                                " and " + writeArray[i].toString());
            }
        }
   }
};
