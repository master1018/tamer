class LinuxUserDefinedFileAttributeView
    extends AbstractUserDefinedFileAttributeView
{
    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private static final String USER_NAMESPACE = "user.";
    private static final int XATTR_NAME_MAX = 255;
    private byte[] nameAsBytes(UnixPath file, String name) throws IOException {
        if (name == null)
            throw new NullPointerException("'name' is null");
        name = USER_NAMESPACE + name;
        byte[] bytes = name.getBytes();
        if (bytes.length > XATTR_NAME_MAX) {
            throw new FileSystemException(file.getPathForExecptionMessage(),
                null, "'" + name + "' is too big");
        }
        return bytes;
    }
    private List<String> asList(long address, int size) {
        List<String> list = new ArrayList<>();
        int start = 0;
        int pos = 0;
        while (pos < size) {
            if (unsafe.getByte(address + pos) == 0) {
                int len = pos - start;
                byte[] value = new byte[len];
                unsafe.copyMemory(null, address+start, value,
                    Unsafe.ARRAY_BYTE_BASE_OFFSET, len);
                String s = new String(value);
                if (s.startsWith(USER_NAMESPACE)) {
                    s = s.substring(USER_NAMESPACE.length());
                    list.add(s);
                }
                start = pos + 1;
            }
            pos++;
        }
        return list;
    }
    private final UnixPath file;
    private final boolean followLinks;
    LinuxUserDefinedFileAttributeView(UnixPath file, boolean followLinks) {
        this.file = file;
        this.followLinks = followLinks;
    }
    @Override
    public List<String> list() throws IOException  {
        if (System.getSecurityManager() != null)
            checkAccess(file.getPathForPermissionCheck(), true, false);
        int fd = file.openForAttributeAccess(followLinks);
        NativeBuffer buffer = null;
        try {
            int size = 1024;
            buffer = NativeBuffers.getNativeBuffer(size);
            for (;;) {
                try {
                    int n = flistxattr(fd, buffer.address(), size);
                    List<String> list = asList(buffer.address(), n);
                    return Collections.unmodifiableList(list);
                } catch (UnixException x) {
                    if (x.errno() == ERANGE && size < 32*1024) {
                        buffer.release();
                        size *= 2;
                        buffer = null;
                        buffer = NativeBuffers.getNativeBuffer(size);
                        continue;
                    }
                    throw new FileSystemException(file.getPathForExecptionMessage(),
                        null, "Unable to get list of extended attributes: " +
                        x.getMessage());
                }
            }
        } finally {
            if (buffer != null)
                buffer.release();
            close(fd);
        }
    }
    @Override
    public int size(String name) throws IOException  {
        if (System.getSecurityManager() != null)
            checkAccess(file.getPathForPermissionCheck(), true, false);
        int fd = file.openForAttributeAccess(followLinks);
        try {
            return fgetxattr(fd, nameAsBytes(file,name), 0L, 0);
        } catch (UnixException x) {
            throw new FileSystemException(file.getPathForExecptionMessage(),
                null, "Unable to get size of extended attribute '" + name +
                "': " + x.getMessage());
        } finally {
            close(fd);
        }
    }
    @Override
    public int read(String name, ByteBuffer dst) throws IOException {
        if (System.getSecurityManager() != null)
            checkAccess(file.getPathForPermissionCheck(), true, false);
        if (dst.isReadOnly())
            throw new IllegalArgumentException("Read-only buffer");
        int pos = dst.position();
        int lim = dst.limit();
        assert (pos <= lim);
        int rem = (pos <= lim ? lim - pos : 0);
        NativeBuffer nb;
        long address;
        if (dst instanceof sun.nio.ch.DirectBuffer) {
            nb = null;
            address = ((sun.nio.ch.DirectBuffer)dst).address() + pos;
        } else {
            nb = NativeBuffers.getNativeBuffer(rem);
            address = nb.address();
        }
        int fd = file.openForAttributeAccess(followLinks);
        try {
            try {
                int n = fgetxattr(fd, nameAsBytes(file,name), address, rem);
                if (rem == 0) {
                    if (n > 0)
                        throw new UnixException(ERANGE);
                    return 0;
                }
                if (nb != null) {
                    int off = dst.arrayOffset() + pos + Unsafe.ARRAY_BYTE_BASE_OFFSET;
                    unsafe.copyMemory(null, address, dst.array(), off, n);
                }
                dst.position(pos + n);
                return n;
            } catch (UnixException x) {
                String msg = (x.errno() == ERANGE) ?
                    "Insufficient space in buffer" : x.getMessage();
                throw new FileSystemException(file.getPathForExecptionMessage(),
                    null, "Error reading extended attribute '" + name + "': " + msg);
            } finally {
                close(fd);
            }
        } finally {
            if (nb != null)
                nb.release();
        }
    }
    @Override
    public int write(String name, ByteBuffer src) throws IOException {
        if (System.getSecurityManager() != null)
            checkAccess(file.getPathForPermissionCheck(), false, true);
        int pos = src.position();
        int lim = src.limit();
        assert (pos <= lim);
        int rem = (pos <= lim ? lim - pos : 0);
        NativeBuffer nb;
        long address;
        if (src instanceof sun.nio.ch.DirectBuffer) {
            nb = null;
            address = ((sun.nio.ch.DirectBuffer)src).address() + pos;
        } else {
            nb = NativeBuffers.getNativeBuffer(rem);
            address = nb.address();
            if (src.hasArray()) {
                int off = src.arrayOffset() + pos + Unsafe.ARRAY_BYTE_BASE_OFFSET;
                unsafe.copyMemory(src.array(), off, null, address, rem);
            } else {
                byte[] tmp = new byte[rem];
                src.get(tmp);
                src.position(pos);  
                unsafe.copyMemory(tmp, Unsafe.ARRAY_BYTE_BASE_OFFSET, null,
                    address, rem);
            }
        }
        int fd = file.openForAttributeAccess(followLinks);
        try {
            try {
                fsetxattr(fd, nameAsBytes(file,name), address, rem);
                src.position(pos + rem);
                return rem;
            } catch (UnixException x) {
                throw new FileSystemException(file.getPathForExecptionMessage(),
                    null, "Error writing extended attribute '" + name + "': " +
                    x.getMessage());
            } finally {
                close(fd);
            }
        } finally {
            if (nb != null)
                nb.release();
        }
    }
    @Override
    public void delete(String name) throws IOException {
        if (System.getSecurityManager() != null)
            checkAccess(file.getPathForPermissionCheck(), false, true);
        int fd = file.openForAttributeAccess(followLinks);
        try {
            fremovexattr(fd, nameAsBytes(file,name));
        } catch (UnixException x) {
            throw new FileSystemException(file.getPathForExecptionMessage(),
                null, "Unable to delete extended attribute '" + name + "': " + x.getMessage());
        } finally {
            close(fd);
        }
    }
    static void copyExtendedAttributes(int ofd, int nfd) {
        NativeBuffer buffer = null;
        try {
            int size = 1024;
            buffer = NativeBuffers.getNativeBuffer(size);
            for (;;) {
                try {
                    size = flistxattr(ofd, buffer.address(), size);
                    break;
                } catch (UnixException x) {
                    if (x.errno() == ERANGE && size < 32*1024) {
                        buffer.release();
                        size *= 2;
                        buffer = null;
                        buffer = NativeBuffers.getNativeBuffer(size);
                        continue;
                    }
                    return;
                }
            }
            long address = buffer.address();
            int start = 0;
            int pos = 0;
            while (pos < size) {
                if (unsafe.getByte(address + pos) == 0) {
                    int len = pos - start;
                    byte[] name = new byte[len];
                    unsafe.copyMemory(null, address+start, name,
                        Unsafe.ARRAY_BYTE_BASE_OFFSET, len);
                    try {
                        copyExtendedAttribute(ofd, name, nfd);
                    } catch (UnixException ignore) {
                    }
                    start = pos + 1;
                }
                pos++;
            }
        } finally {
            if (buffer != null)
                buffer.release();
        }
    }
    private static void copyExtendedAttribute(int ofd, byte[] name, int nfd)
        throws UnixException
    {
        int size = fgetxattr(ofd, name, 0L, 0);
        NativeBuffer buffer = NativeBuffers.getNativeBuffer(size);
        try {
            long address = buffer.address();
            size = fgetxattr(ofd, name, address, size);
            fsetxattr(nfd, name, address, size);
        } finally {
            buffer.release();
        }
    }
}
