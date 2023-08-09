class StubGenerator {
    public void generateStubbedJar(File destJar,
            Map<String, ClassReader> classes,
            Filter filter) throws IOException {
        TreeMap<String, byte[]> all = new TreeMap<String, byte[]>();
        for (Entry<String, ClassReader> entry : classes.entrySet()) {
            ClassReader cr = entry.getValue();
            byte[] b = visitClassStubber(cr, filter);
            String name = classNameToEntryPath(cr.getClassName());
            all.put(name, b);
        }
        createJar(new FileOutputStream(destJar), all);
        System.out.println(String.format("Wrote %s", destJar.getPath()));
    }
    String classNameToEntryPath(String className) {
        return className.replaceAll("\\.", "/").concat(".class");
    }
    void createJar(FileOutputStream outStream, Map<String,byte[]> all) throws IOException {
        JarOutputStream jar = new JarOutputStream(outStream);
        for (Entry<String, byte[]> entry : all.entrySet()) {
            String name = entry.getKey();
            JarEntry jar_entry = new JarEntry(name);
            jar.putNextEntry(jar_entry);
            jar.write(entry.getValue());
            jar.closeEntry();
        }
        jar.flush();
        jar.close();
    }
    byte[] visitClassStubber(ClassReader cr, Filter filter) {
        System.out.println("Stub " + cr.getClassName());
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        ClassVisitor stubWriter = new ClassStubber(cw);
        ClassVisitor classFilter = new FilterClassAdapter(stubWriter, filter);
        cr.accept(classFilter, 0 );
        return cw.toByteArray();
    }
}
