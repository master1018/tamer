public class Test4343723 {
    public static void main(String[] args) {
        try {
            Beans.instantiate(Test4343723.class.getClassLoader(), "Test4343723");
        }
        catch (ClassNotFoundException exception) {
            if (null == exception.getCause())
                throw new Error("unexpected exception", exception);
        }
        catch (IOException exception) {
            throw new Error("unexpected exception", exception);
        }
    }
    protected Test4343723() {
    }
}
