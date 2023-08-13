public class LeadingSlash {
    public static void main (String args[]) throws Exception {
        if (File.separatorChar == '\\') {       
            File file = null;
            try {
                file = File.createTempFile("bug", "4487368");
                new FileInputStream("\\" + file.getPath()).close();
                new FileOutputStream("\\" + file.getPath()).close();
            } finally {
                if (file != null)
                    file.delete();
            }
        }
    }
}
