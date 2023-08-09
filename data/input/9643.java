public class IllegalCharsetName {
    public static void main(String[] args) throws Exception {
        String[] illegalNames = {
            ".",
            "_",
            ":",
            "-",
            ".name",
            "_name",
            ":name",
            "-name",
            "name*name",
            "name?name"
        };
        for (int i = 0; i < illegalNames.length; i++) {
            try {
                Charset.forName(illegalNames[i]);
                throw new Exception("Charset.forName(): No exception thrown");
            } catch (IllegalCharsetNameException x) { 
            }
            try {
                Charset.isSupported(illegalNames[i]);
                throw new Exception("Charset.isSupported(): No exception thrown");
            } catch (IllegalCharsetNameException x) { 
            }
        }
    }
}
