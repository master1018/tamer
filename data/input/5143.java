public class Test4076065 {
    public static void main(String[] args) {
        try {
            new VetoableChangeSupport(null);
        } catch (NullPointerException exception) {
            return;
        }
        throw new Error("didn't get expected NullPointerException");
    }
}
