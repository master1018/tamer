public class IDNTest {
    public static void main(String[] args) {
        try {
            Socket s = new Socket("\u67f4\u7530\u82b3\u6a39", 8000);
        } catch (UnknownHostException e) {
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        try {
            Socket s = new Socket("ho st", 8000);
        } catch (UnknownHostException e) {
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
