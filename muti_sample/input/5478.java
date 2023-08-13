                                       String className,
                                       Kind kind)
        throws IOException;
    JavaFileObject getJavaFileForOutput(Location location,
                                        String className,
                                        Kind kind,
                                        FileObject sibling)
        throws IOException;
    FileObject getFileForInput(Location location,
                               String packageName,
                               String relativeName)
        throws IOException;
    FileObject getFileForOutput(Location location,
                                String packageName,
                                String relativeName,
                                FileObject sibling)
        throws IOException;
    void flush() throws IOException;
    void close() throws IOException;
}
