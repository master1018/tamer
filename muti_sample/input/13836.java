public class TestSourceVersionWarnings extends AbstractProcessor {
    @Override
    public SourceVersion getSupportedSourceVersion() {
        String sourceVersion = processingEnv.getOptions().get("SourceVersion");
        if (sourceVersion == null) {
            processingEnv.getMessager().printMessage(WARNING,
                                                     "No SourceVersion option given");
            return SourceVersion.RELEASE_6;
        } else {
            return SourceVersion.valueOf(sourceVersion);
        }
    }
    public boolean process(Set<? extends TypeElement> annotations,
                           RoundEnvironment roundEnvironment) {
        return true;
    }
}
