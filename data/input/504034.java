 class SpecialJavaFileManager extends
        ForwardingJavaFileManager<StandardJavaFileManager> {
    private Map<String, MemoryByteCode> store;
    public SpecialJavaFileManager(StandardJavaFileManager sjfm) {
        super(sjfm);
        store = new HashMap<String, MemoryByteCode>();
    }
    public JavaFileObject getJavaFileForOutput(Location location, String name,
            JavaFileObject.Kind kind, FileObject sibling) {
        MemoryByteCode mbc = new MemoryByteCode(name);
        store.put(name, mbc);
        return mbc;
    }
    public Set<MemoryByteCode> getAllMemoryByteCodes() {
        return new HashSet<MemoryByteCode>(store.values());
    }
    public MemoryByteCode getMemoryByteCode(String className) {
        return store.get(className);
    }
}
