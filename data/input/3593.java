class SolarisFileSystem extends UnixFileSystem {
    private final boolean hasSolaris11Features;
    SolarisFileSystem(UnixFileSystemProvider provider, String dir) {
        super(provider, dir);
        String osversion = AccessController
            .doPrivileged(new GetPropertyAction("os.version"));
        String[] vers = Util.split(osversion, '.');
        assert vers.length >= 2;
        int majorVersion = Integer.parseInt(vers[0]);
        int minorVersion = Integer.parseInt(vers[1]);
        this.hasSolaris11Features =
            (majorVersion > 5 || (majorVersion == 5 && minorVersion >= 11));
    }
    @Override
    boolean isSolaris() {
        return true;
    }
    @Override
    public WatchService newWatchService()
        throws IOException
    {
        if (hasSolaris11Features) {
            return new SolarisWatchService(this);
        } else {
            return new PollingWatchService();
        }
    }
    private static class SupportedFileFileAttributeViewsHolder {
        static final Set<String> supportedFileAttributeViews =
            supportedFileAttributeViews();
        private static Set<String> supportedFileAttributeViews() {
            Set<String> result = new HashSet<>();
            result.addAll(standardFileAttributeViews());
            result.add("acl");
            result.add("user");
            return Collections.unmodifiableSet(result);
        }
    }
    @Override
    public Set<String> supportedFileAttributeViews() {
        return SupportedFileFileAttributeViewsHolder.supportedFileAttributeViews;
    }
    @Override
    void copyNonPosixAttributes(int ofd, int nfd) {
        SolarisUserDefinedFileAttributeView.copyExtendedAttributes(ofd, nfd);
    }
    @Override
    Iterable<UnixMountEntry> getMountEntries() {
        ArrayList<UnixMountEntry> entries = new ArrayList<>();
        try {
            UnixPath mnttab = new UnixPath(this, "/etc/mnttab");
            long fp = fopen(mnttab, "r");
            try {
                for (;;) {
                    UnixMountEntry entry = new UnixMountEntry();
                    int res = getextmntent(fp, entry);
                    if (res < 0)
                        break;
                    entries.add(entry);
                }
            } finally {
                fclose(fp);
            }
        } catch (UnixException x) {
        }
        return entries;
    }
    @Override
    FileStore getFileStore(UnixMountEntry entry) throws IOException {
        return new SolarisFileStore(this, entry);
    }
}
