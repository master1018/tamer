public class TestAnonSourceNames extends JavacTestingAbstractProcessor {
   public boolean process(Set<? extends TypeElement> annotations,
                          RoundEnvironment roundEnv) {
       if (!roundEnv.processingOver()) {
           Trees trees = Trees.instance(processingEnv);
           for(Element rootElement : roundEnv.getRootElements()) {
               TreePath treePath = trees.getPath(rootElement);
               (new ClassTreeScanner(trees)).
                   scan(trees.getTree(rootElement),
                        treePath.getCompilationUnit());
           }
       }
       return true;
   }
   class ClassTreeScanner extends TreeScanner<Void, CompilationUnitTree> {
       private Trees trees;
       public ClassTreeScanner(Trees trees) {
           super();
           this.trees = trees;
       }
       @Override
       public Void visitClass(ClassTree node, CompilationUnitTree cu) {
                     Element element = trees.getElement(trees.getPath(cu, node));
           if (element == null) {
               processingEnv.getMessager().printMessage(ERROR,
                                                        "No element retrieved for node named ''" +
                                                        node.getSimpleName() + "''.");
           } else {
               System.out.println("\nVisiting class ``" + element.getSimpleName() +
                                  "'' of kind " + element.getKind());
                         if (element instanceof TypeElement) {
                   TypeElement typeElement = (TypeElement) element;
                   String s = typeElement.getQualifiedName().toString();
                   System.out.println("\tqualified name:" + s);
               } else {
                   throw new RuntimeException("TypeElement not gotten from ClassTree.");
               }
           }
           return super.visitClass(node, cu);
       }
   }
}
