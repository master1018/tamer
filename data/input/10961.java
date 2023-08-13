public class GetFileNameMap {
    public static void main(String[] args) throws Exception {
        FileNameMap map = URLConnection.getFileNameMap();
        String s = map.getContentTypeFor("test.pdf");
    }
}
