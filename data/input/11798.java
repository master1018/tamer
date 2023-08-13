public class T6852595 {
    public static void main(String[] args) throws IOException {
        JavaFileObject sfo = new SimpleJavaFileObject(URI.create("myfo:/Test.java"),Kind.SOURCE) {
            public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                return "class BadName { Object o = j; }";
            }
        };
        List<? extends JavaFileObject> files = Arrays.asList(sfo);
        JavaCompiler tool = ToolProvider.getSystemJavaCompiler();
        JavacTask ct = (JavacTask)tool.getTask(null, null, null, null, null, files);
        Iterable<? extends CompilationUnitTree> compUnits = ct.parse();
        CompilationUnitTree cu = compUnits.iterator().next();
        ClassTree cdef = (ClassTree)cu.getTypeDecls().get(0);
        JCVariableDecl vdef = (JCVariableDecl)cdef.getMembers().get(0);
        TreePath path = TreePath.getPath(cu, vdef.init);
        Trees.instance(ct).getScope(path);
    }
}
