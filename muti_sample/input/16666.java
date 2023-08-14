class WindowsAclFileAttributeView
    extends AbstractAclFileAttributeView
{
    private static final short SIZEOF_SECURITY_DESCRIPTOR   = 20;
    private final WindowsPath file;
    private final boolean followLinks;
    WindowsAclFileAttributeView(WindowsPath file, boolean followLinks) {
        this.file = file;
        this.followLinks = followLinks;
    }
    private void checkAccess(WindowsPath file,
                             boolean checkRead,
                             boolean checkWrite)
    {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            if (checkRead)
                sm.checkRead(file.getPathForPermissionCheck());
            if (checkWrite)
                sm.checkWrite(file.getPathForPermissionCheck());
            sm.checkPermission(new RuntimePermission("accessUserInformation"));
        }
    }
    static NativeBuffer getFileSecurity(String path, int request)
        throws IOException
    {
        int size = 0;
        try {
            size = GetFileSecurity(path, request, 0L, 0);
        } catch (WindowsException x) {
            x.rethrowAsIOException(path);
        }
        assert size > 0;
        NativeBuffer buffer = NativeBuffers.getNativeBuffer(size);
        try {
            for (;;) {
                int newSize = GetFileSecurity(path, request, buffer.address(), size);
                if (newSize <= size)
                    return buffer;
                buffer.release();
                buffer = NativeBuffers.getNativeBuffer(newSize);
                size = newSize;
            }
        } catch (WindowsException x) {
            buffer.release();
            x.rethrowAsIOException(path);
            return null;
        }
    }
    @Override
    public UserPrincipal getOwner()
        throws IOException
    {
        checkAccess(file, true, false);
        String path = WindowsLinkSupport.getFinalPath(file, followLinks);
        NativeBuffer buffer = getFileSecurity(path, OWNER_SECURITY_INFORMATION);
        try {
            long sidAddress = GetSecurityDescriptorOwner(buffer.address());
            if (sidAddress == 0L)
                throw new IOException("no owner");
            return WindowsUserPrincipals.fromSid(sidAddress);
        } catch (WindowsException x) {
            x.rethrowAsIOException(file);
            return null;
        } finally {
            buffer.release();
        }
    }
    @Override
    public List<AclEntry> getAcl()
        throws IOException
    {
        checkAccess(file, true, false);
        String path = WindowsLinkSupport.getFinalPath(file, followLinks);
        NativeBuffer buffer = getFileSecurity(path, DACL_SECURITY_INFORMATION);
        try {
            return WindowsSecurityDescriptor.getAcl(buffer.address());
        } finally {
            buffer.release();
        }
    }
    @Override
    public void setOwner(UserPrincipal obj)
        throws IOException
    {
        if (obj == null)
            throw new NullPointerException("'owner' is null");
        if (!(obj instanceof WindowsUserPrincipals.User))
            throw new ProviderMismatchException();
        WindowsUserPrincipals.User owner = (WindowsUserPrincipals.User)obj;
        checkAccess(file, false, true);
        String path = WindowsLinkSupport.getFinalPath(file, followLinks);
        long pOwner = 0L;
        try {
            pOwner = ConvertStringSidToSid(owner.sidString());
        } catch (WindowsException x) {
            throw new IOException("Failed to get SID for " + owner.getName()
                + ": " + x.errorString());
        }
        try {
            NativeBuffer buffer = NativeBuffers.getNativeBuffer(SIZEOF_SECURITY_DESCRIPTOR);
            try {
                InitializeSecurityDescriptor(buffer.address());
                SetSecurityDescriptorOwner(buffer.address(), pOwner);
                WindowsSecurity.Privilege priv =
                    WindowsSecurity.enablePrivilege("SeRestorePrivilege");
                try {
                    SetFileSecurity(path,
                                    OWNER_SECURITY_INFORMATION,
                                    buffer.address());
                } finally {
                    priv.drop();
                }
            } catch (WindowsException x) {
                x.rethrowAsIOException(file);
            } finally {
                buffer.release();
            }
        } finally {
            LocalFree(pOwner);
        }
    }
    @Override
    public void setAcl(List<AclEntry> acl) throws IOException {
        checkAccess(file, false, true);
        String path = WindowsLinkSupport.getFinalPath(file, followLinks);
        WindowsSecurityDescriptor sd = WindowsSecurityDescriptor.create(acl);
        try {
            SetFileSecurity(path, DACL_SECURITY_INFORMATION, sd.address());
        } catch (WindowsException x) {
             x.rethrowAsIOException(file);
        } finally {
            sd.release();
        }
    }
}
