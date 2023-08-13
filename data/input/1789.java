public class T6472751 {
    static class MyFileObject extends SimpleJavaFileObject {
        public MyFileObject() {
            super(URI.create("myfo:/Test.java"), JavaFileObject.Kind.SOURCE);
        }
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return "public enum Test { ABC, DEF; }";
        }
    }
    static Trees trees;
    static SourcePositions positions;
    public static void main(String[] args) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        JavacTask task = (JavacTask) compiler.getTask(null, null, null, null, null, List.of(new MyFileObject()));
        trees = Trees.instance(task);
        positions = trees.getSourcePositions();
        Iterable<? extends CompilationUnitTree> asts = task.parse();
        for (CompilationUnitTree ast : asts) {
            new MyVisitor().scan(ast, null);
        }
    }
    static class MyVisitor extends TreeScanner<Void,Void> {
        @Override
        public Void scan(Tree node, Void ignored) {
            if (node == null)
                return null;
            Kind k = node.getKind();
            long pos = positions.getStartPosition(null,node);
            System.out.format("%s: %s%n", k, pos);
            if (k != Kind.MODIFIERS && pos < 0)
                throw new Error("unexpected position found");
            return super.scan(node, ignored);
        }
    }
}
