public class T6458749<T> extends AbstractProcessor {
    public boolean process(Set<? extends TypeElement> tes, RoundEnvironment renv) {
        if (!renv.processingOver()) {
            for(TypeElement e : ElementFilter.typesIn(renv.getRootElements())) {
                System.out.printf("Element %s:%n", e.toString());
                try {
                    for (TypeParameterElement tp : e.getTypeParameters()) {
                        System.out.printf("Type param %s", tp.toString());
                        if (! tp.getEnclosedElements().isEmpty()) {
                            throw new AssertionError("TypeParameterElement.getEnclosedElements() should return empty list");
                        }
                    }
                } catch (NullPointerException npe) {
                    throw new AssertionError("NPE from TypeParameterElement.getEnclosedElements()", npe);
                }
            }
        }
        return true;
    }
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }
}
