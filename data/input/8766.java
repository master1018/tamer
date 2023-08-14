public class ExecEmptyString {
    public static void main(String[] args) throws Exception {
        try {
            Runtime.getRuntime().exec("");
            throw new Exception("Expected IllegalArgumentException not thrown");
        } catch (IllegalArgumentException e) {} 
        try {
            Runtime.getRuntime().exec(new String());
            throw new Exception("Expected IllegalArgumentException not thrown");
        } catch (IllegalArgumentException e) {} 
    }
}
