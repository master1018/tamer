abstract class AbstractFileSystemProvider extends FileSystemProvider {
    protected AbstractFileSystemProvider() { }
    private static String[] split(String attribute) {
        String[] s = new String[2];
        int pos = attribute.indexOf(':');
        if (pos == -1) {
            s[0] = "basic";
            s[1] = attribute;
        } else {
            s[0] = attribute.substring(0, pos++);
            s[1] = (pos == attribute.length()) ? "" : attribute.substring(pos);
        }
        return s;
    }
    abstract DynamicFileAttributeView getFileAttributeView(Path file,
                                                           String name,
                                                           LinkOption... options);
    @Override
    public final void setAttribute(Path file,
                                   String attribute,
                                   Object value,
                                   LinkOption... options)
        throws IOException
    {
        String[] s = split(attribute);
        if (s[0].length() == 0)
            throw new IllegalArgumentException(attribute);
        DynamicFileAttributeView view = getFileAttributeView(file, s[0], options);
        if (view == null)
            throw new UnsupportedOperationException("View '" + s[0] + "' not available");
        view.setAttribute(s[1], value);
    }
    @Override
    public final Map<String,Object> readAttributes(Path file, String attributes, LinkOption... options)
        throws IOException
    {
        String[] s = split(attributes);
        if (s[0].length() == 0)
            throw new IllegalArgumentException(attributes);
        DynamicFileAttributeView view = getFileAttributeView(file, s[0], options);
        if (view == null)
            throw new UnsupportedOperationException("View '" + s[0] + "' not available");
        return view.readAttributes(s[1].split(","));
    }
    abstract boolean implDelete(Path file, boolean failIfNotExists) throws IOException;
    @Override
    public final void delete(Path file) throws IOException {
        implDelete(file, true);
    }
    @Override
    public final boolean deleteIfExists(Path file) throws IOException {
        return implDelete(file, false);
    }
}
