public class Test4067824 {
    public static void main(String[] args) throws Exception {
        ClassLoader cl = Test4067824.class.getClassLoader();
        try {
            Beans.instantiate(cl, "Test4067824");
        }
        catch (ClassNotFoundException exception) {
            if (exception.toString().indexOf("IllegalAccessException") < 0)
                throw new Error("unexpected exception", exception);
        }
        FileOutputStream fout = new FileOutputStream("foo.ser");
        fout.write(new byte [] {1, 2, 3, 4, 5});
        fout.close();
        try {
            Beans.instantiate(cl, "foo");
            throw new Error("Instantiated corrupt .ser file OK!!??");
        }
        catch (ClassNotFoundException exception) {
        }
        catch (StreamCorruptedException exception) {
        }
    }
    private Test4067824() {
    }
}
