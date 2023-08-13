public class IndexOfEmptyInEmpty {
    public static void main(String[] args) throws Exception {
        int result = new String("").indexOf("");
        if (result != 0) {
            throw new Exception("new String(\"\").indexOf(\"\") must be 0, but got " + result);
        }
    }
}
