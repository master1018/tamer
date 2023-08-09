public class SolarisFileSystemProvider extends UnixFileSystemProvider {
    public SolarisFileSystemProvider() {
        super();
    }
    @Override
    SolarisFileSystem newFileSystem(String dir) {
        return new SolarisFileSystem(this, dir);
    }
    @Override
    SolarisFileStore getFileStore(UnixPath path) throws IOException {
        return new SolarisFileStore(path);
    }
    @Override
    @SuppressWarnings("unchecked")
    public <V extends FileAttributeView> V getFileAttributeView(Path obj,
                                                                Class<V> type,
                                                                LinkOption... options)
    {
        if (type == AclFileAttributeView.class) {
            return (V) new SolarisAclFileAttributeView(UnixPath.toUnixPath(obj),
                                                       Util.followLinks(options));
        }
        if (type == UserDefinedFileAttributeView.class) {
            return(V) new SolarisUserDefinedFileAttributeView(UnixPath.toUnixPath(obj),
                                                              Util.followLinks(options));
        }
        return super.getFileAttributeView(obj, type, options);
    }
    @Override
    public DynamicFileAttributeView getFileAttributeView(Path obj,
                                                         String name,
                                                         LinkOption... options)
    {
        if (name.equals("acl"))
            return new SolarisAclFileAttributeView(UnixPath.toUnixPath(obj),
                                                   Util.followLinks(options));
        if (name.equals("user"))
            return new SolarisUserDefinedFileAttributeView(UnixPath.toUnixPath(obj),
                                                           Util.followLinks(options));
        return super.getFileAttributeView(obj, name, options);
    }
}
