class SourceGenerator {
    public void generateSource(File baseDir,
            Map<String, ClassReader> classes,
            Filter filter) throws IOException {
        for (Entry<String, ClassReader> entry : classes.entrySet()) {
            ClassReader cr = entry.getValue();
            String name = classNameToJavaPath(cr.getClassName());
            FileWriter fw = null;
            try {
                fw = createWriter(baseDir, name);
                visitClassSource(fw, cr, filter);
            } finally {
                fw.close();
            }
        }
    }
    FileWriter createWriter(File baseDir, String name) throws IOException {
        File f = new File(baseDir, name);
        f.getParentFile().mkdirs();
        System.out.println("Writing " + f.getPath());
        return new FileWriter(f);
    }
    String classNameToJavaPath(String className) {
        return className.replace('.', '/').concat(".java");
    }
    void visitClassSource(Writer fw, ClassReader cr, Filter filter) {
        System.out.println("Dump " + cr.getClassName());
        ClassVisitor javaWriter = new ClassSourcer(new Output(fw));
        ClassVisitor classFilter = new FilterClassAdapter(javaWriter, filter);
        cr.accept(classFilter, 0 );
    }
}
