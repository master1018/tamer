public class CreatePlatformFile {
    public static void main(String argv[])  {
        String fileSep = System.getProperty("file.separator");
        String defaultEncoding = System.getProperty("file.encoding");
        if(defaultEncoding == null) {
            System.err.println("Default encoding not found; Error.");
            return;
        }
        if (defaultEncoding.equals("Cp1252") ) {
            String fileName = "i18nH\u00e9lloWorld.java";
            try {
                PrintWriter pw = new PrintWriter(new FileOutputStream("."+fileSep+fileName));
                pw.println("public class i18nH\u00e9lloWorld {");
                pw.println("    public static void main(String [] argv) {");
                pw.println("        System.out.println(\"Hello Cp1252 World\");");
                pw.println("    }");
                pw.println("}");
                pw.flush();
                pw.close();
            }
            catch (java.io.FileNotFoundException e) {
                System.err.println("Problem opening file; test fails");
            }
        } else {
            String fileName = "i18nHelloWorld.java";
            try {
                PrintWriter pw = new PrintWriter(new FileOutputStream("."+fileSep+fileName));
                pw.println("public class i18nHelloWorld {");
                pw.println("    public static void main(String [] argv) {");
                pw.println("        System.out.println(\"Warning: US-ASCII assumed; filenames with\");");
                pw.println("        System.out.println(\"non-ASCII characters will not be tested\");");
                pw.println("    }");
                pw.println("}");
                pw.flush();
                pw.close();
            }
            catch (java.io.FileNotFoundException e) {
                System.err.println("Problem opening file; test fails");
            }
        }
    }
}
