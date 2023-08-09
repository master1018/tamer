public class WErrorGen extends JavacTestingAbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations,
                           RoundEnvironment roundEnv) {
        if (++round == 1) {
            try {
                JavaFileObject fo = filer.createSourceFile("Gen");
                Writer out = fo.openWriter();
                out.write("import java.util.*; class Gen { List l; }");
                out.close();
            } catch (IOException e) {
            }
        }
        return true;
    }
    int round = 0;
}
