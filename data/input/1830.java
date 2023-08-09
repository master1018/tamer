public class EncodeDecode {
    public static void main(String[] args) {
        String str = "fds@$";
        String encStr = URLEncoder.encode(str);
        String decStr = URLDecoder.decode(encStr);
    }
}
