public class Anno extends AbstractProcessor {
    public boolean process(Set<? extends TypeElement> annotations,
                                    RoundEnvironment roundEnv) {
        return true;
    }
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }
}
