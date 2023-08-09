public class NullArgs {
    public static void main(String[] args) throws Exception {
        for (int i = 0;; i++) {
            try {
                switch (i) {
                case 0:  new File((String)null);  break;
                case 1:  new File((String)null, null);  break;
                case 2:  new File((File)null, null);  break;
                case 3:  File.createTempFile(null, null, null);  break;
                case 4:  File.createTempFile(null, null);  break;
                case 5:  new File("foo").compareTo(null);  break;
                case 6:  new File("foo").renameTo(null);  break;
                default:
                    System.err.println();
                    return;
                }
            } catch (NullPointerException x) {
                System.err.print(i + " ");
                continue;
            }
            throw new Exception("NullPointerException not thrown (case " +
                                i + ")");
        }
    }
}
