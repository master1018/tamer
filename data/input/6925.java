public class B5077773 {
    public static void main(String[] args) throws Exception {
        URLClassLoader loader =  new URLClassLoader (new URL[] {new URL("file:foo.jar")});
        InputStream is = loader.getResourceAsStream ("javax/swing/text/rtf/charsets/mac.txt");
        if (is == null) {
            System.out.println ("could not find mac.txt");
            return;
        }
        int c=0;
        while ((is.read()) != -1) {
            c++;
        }
        if (c == 26)   {
            throw new RuntimeException ("Wrong mac.txt file was loaded");
        }
    }
}
