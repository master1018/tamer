public class Enormous {
    public static void main(String[] args) throws Exception {
        new String(new char[16777217]).getBytes("ASCII");
        byte[] bytes = new byte[16777217];
        new String(bytes,"ASCII");
        new sun.misc.BASE64Encoder().encode(bytes);
    }
}
