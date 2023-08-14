public class TestGetTree extends AbstractProcessor {
    public boolean process(Set<? extends TypeElement> annotations,
                           RoundEnvironment roundEnvironment)
    {
        final Trees trees = Trees.instance(processingEnv);
        for (TypeElement e : typesIn(roundEnvironment.getRootElements())) {
            ClassTree node = trees.getTree(e);
            System.out.println(node.toString());
        }
        return true;
    }
}
