public class Replace {
    public static void main(String[] arg) throws Exception {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < 200; i++) {
            sb.replace(i, i+1, "a");
        }
    }
}
