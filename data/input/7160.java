class LinuxDosFileAttributeView
    extends UnixFileAttributeViews.Basic implements DosFileAttributeView
{
    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private static final String READONLY_NAME = "readonly";
    private static final String ARCHIVE_NAME = "archive";
    private static final String SYSTEM_NAME = "system";
    private static final String HIDDEN_NAME = "hidden";
    private static final String DOS_XATTR_NAME = "user.DOSATTRIB";
    private static final byte[] DOS_XATTR_NAME_AS_BYTES = DOS_XATTR_NAME.getBytes();
    private static final int DOS_XATTR_READONLY = 0x01;
    private static final int DOS_XATTR_HIDDEN   = 0x02;
    private static final int DOS_XATTR_SYSTEM   = 0x04;
    private static final int DOS_XATTR_ARCHIVE  = 0x20;
    private static final Set<String> dosAttributeNames =
        Util.newSet(basicAttributeNames, READONLY_NAME, ARCHIVE_NAME, SYSTEM_NAME, HIDDEN_NAME);
    LinuxDosFileAttributeView(UnixPath file, boolean followLinks) {
        super(file, followLinks);
    }
    @Override
    public String name() {
        return "dos";
    }
    @Override
    public void setAttribute(String attribute, Object value)
        throws IOException
    {
        if (attribute.equals(READONLY_NAME)) {
            setReadOnly((Boolean)value);
            return;
        }
        if (attribute.equals(ARCHIVE_NAME)) {
            setArchive((Boolean)value);
            return;
        }
        if (attribute.equals(SYSTEM_NAME)) {
            setSystem((Boolean)value);
            return;
        }
        if (attribute.equals(HIDDEN_NAME)) {
            setHidden((Boolean)value);
            return;
        }
        super.setAttribute(attribute, value);
    }
    @Override
    public Map<String,Object> readAttributes(String[] attributes)
        throws IOException
    {
        AttributesBuilder builder =
            AttributesBuilder.create(dosAttributeNames, attributes);
        DosFileAttributes attrs = readAttributes();
        addRequestedBasicAttributes(attrs, builder);
        if (builder.match(READONLY_NAME))
            builder.add(READONLY_NAME, attrs.isReadOnly());
        if (builder.match(ARCHIVE_NAME))
            builder.add(ARCHIVE_NAME, attrs.isArchive());
        if (builder.match(SYSTEM_NAME))
            builder.add(SYSTEM_NAME, attrs.isSystem());
        if (builder.match(HIDDEN_NAME))
            builder.add(HIDDEN_NAME, attrs.isHidden());
        return builder.unmodifiableMap();
    }
    @Override
    public DosFileAttributes readAttributes() throws IOException {
        file.checkRead();
        int fd = file.openForAttributeAccess(followLinks);
        try {
             final UnixFileAttributes attrs = UnixFileAttributes.get(fd);
             final int dosAttribute = getDosAttribute(fd);
             return new DosFileAttributes() {
                @Override
                public FileTime lastModifiedTime() {
                    return attrs.lastModifiedTime();
                }
                @Override
                public FileTime lastAccessTime() {
                    return attrs.lastAccessTime();
                }
                @Override
                public FileTime creationTime() {
                    return attrs.creationTime();
                }
                @Override
                public boolean isRegularFile() {
                    return attrs.isRegularFile();
                }
                @Override
                public boolean isDirectory() {
                    return attrs.isDirectory();
                }
                @Override
                public boolean isSymbolicLink() {
                    return attrs.isSymbolicLink();
                }
                @Override
                public boolean isOther() {
                    return attrs.isOther();
                }
                @Override
                public long size() {
                    return attrs.size();
                }
                @Override
                public Object fileKey() {
                    return attrs.fileKey();
                }
                @Override
                public boolean isReadOnly() {
                    return (dosAttribute & DOS_XATTR_READONLY) != 0;
                }
                @Override
                public boolean isHidden() {
                    return (dosAttribute & DOS_XATTR_HIDDEN) != 0;
                }
                @Override
                public boolean isArchive() {
                    return (dosAttribute & DOS_XATTR_ARCHIVE) != 0;
                }
                @Override
                public boolean isSystem() {
                    return (dosAttribute & DOS_XATTR_SYSTEM) != 0;
                }
             };
        } catch (UnixException x) {
            x.rethrowAsIOException(file);
            return null;    
        } finally {
            close(fd);
        }
    }
    @Override
    public void setReadOnly(boolean value) throws IOException {
        updateDosAttribute(DOS_XATTR_READONLY, value);
    }
    @Override
    public void setHidden(boolean value) throws IOException {
        updateDosAttribute(DOS_XATTR_HIDDEN, value);
    }
    @Override
    public void setArchive(boolean value) throws IOException {
        updateDosAttribute(DOS_XATTR_ARCHIVE, value);
    }
    @Override
    public void setSystem(boolean value) throws IOException {
        updateDosAttribute(DOS_XATTR_SYSTEM, value);
    }
    private int getDosAttribute(int fd) throws UnixException {
        final int size = 24;
        NativeBuffer buffer = NativeBuffers.getNativeBuffer(size);
        try {
            int len = LinuxNativeDispatcher
                .fgetxattr(fd, DOS_XATTR_NAME_AS_BYTES, buffer.address(), size);
            if (len > 0) {
                if (unsafe.getByte(buffer.address()+len-1) == 0)
                    len--;
                byte[] buf = new byte[len];
                unsafe.copyMemory(null, buffer.address(), buf,
                    Unsafe.ARRAY_BYTE_BASE_OFFSET, len);
                String value = new String(buf); 
                if (value.length() >= 3 && value.startsWith("0x")) {
                    try {
                        return Integer.parseInt(value.substring(2), 16);
                    } catch (NumberFormatException x) {
                    }
                }
            }
            throw new UnixException("Value of " + DOS_XATTR_NAME + " attribute is invalid");
        } catch (UnixException x) {
            if (x.errno() == ENODATA)
                return 0;
            throw x;
        } finally {
            buffer.release();
        }
    }
    private void updateDosAttribute(int flag, boolean enable) throws IOException {
        file.checkWrite();
        int fd = file.openForAttributeAccess(followLinks);
        try {
            int oldValue = getDosAttribute(fd);
            int newValue = oldValue;
            if (enable) {
                newValue |= flag;
            } else {
                newValue &= ~flag;
            }
            if (newValue != oldValue) {
                byte[] value = ("0x" + Integer.toHexString(newValue)).getBytes();
                NativeBuffer buffer = NativeBuffers.asNativeBuffer(value);
                try {
                    LinuxNativeDispatcher.fsetxattr(fd, DOS_XATTR_NAME_AS_BYTES,
                        buffer.address(), value.length+1);
                } finally {
                    buffer.release();
                }
            }
        } catch (UnixException x) {
            x.rethrowAsIOException(file);
        } finally {
            close(fd);
        }
    }
}
