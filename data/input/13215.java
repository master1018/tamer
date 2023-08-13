                            Iterable<String> classes);
    StandardJavaFileManager getStandardFileManager(
        DiagnosticListener<? super JavaFileObject> diagnosticListener,
        Locale locale,
        Charset charset);
    interface NativeHeaderTask extends Callable<Boolean> {
        void setLocale(Locale locale);
        Boolean call();
    }
}
