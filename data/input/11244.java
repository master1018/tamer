public class Substring {
    public static void main(String[] args) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Guten Morgen!");
        if (buffer.substring(0).length() != 13)
            throw new RuntimeException();
    }
}
