public class CreateFiles {
    static void output(FileOutputStream fos, String s) throws Exception {
        fos.write( s.getBytes("UTF8") );
        fos.write( "\n".getBytes("UTF8") );
    }
    public static void main(String [] args) throws Exception {
        File f;
        FileOutputStream fos;
        String name = "\u20ac";
        f = new File(name + ".java");
        fos = new FileOutputStream(f);
        output(fos, "import java.lang.instrument.Instrumentation;" );
        output(fos, "public class " +name + " {" );
        output(fos, "    public static void premain(String ops, Instrumentation ins) {" );
        output(fos, "        System.out.println(\"premain running\"); ");
        output(fos, "    }");
        output(fos, "}");
        fos.close();
        f = new File("agent.mf");
        fos = new FileOutputStream(f);
        output(fos, "Manifest-Version: 1.0");
        output(fos, "Premain-Class: " + name);
        output(fos, "");
        fos.close();
    }
}
