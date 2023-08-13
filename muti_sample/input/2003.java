public class DummyProcessor extends JavacTestingAbstractProcessor {
   public boolean process(Set<? extends TypeElement> annotations,
                  RoundEnvironment roundEnv) {
       return true;
   }
}
