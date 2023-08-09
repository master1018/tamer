class SolarisAclFileAttributeView
    extends AbstractAclFileAttributeView
{
    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private static final int MAX_ACL_ENTRIES = 1024;
    private static final short SIZEOF_ACE_T     = 12;
    private static final short OFFSETOF_UID     = 0;
    private static final short OFFSETOF_MASK    = 4;
    private static final short OFFSETOF_FLAGS   = 8;
    private static final short OFFSETOF_TYPE    = 10;
    private final UnixPath file;
    private final boolean followLinks;
    SolarisAclFileAttributeView(UnixPath file, boolean followLinks) {
        this.file = file;
        this.followLinks = followLinks;
    }
    private void checkAccess(UnixPath file,
                             boolean checkRead,
                             boolean checkWrite)
    {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            if (checkRead)
                file.checkRead();
            if (checkWrite)
                file.checkWrite();
            sm.checkPermission(new RuntimePermission("accessUserInformation"));
        }
    }
    private static void encode(List<AclEntry> acl, long address) {
        long offset = address;
        for (AclEntry ace: acl) {
            int flags = 0;
            UserPrincipal who = ace.principal();
            if (!(who instanceof UnixUserPrincipals.User))
                throw new ProviderMismatchException();
            UnixUserPrincipals.User user = (UnixUserPrincipals.User)who;
            int uid;
            if (user.isSpecial()) {
                uid = -1;
                if (who == UnixUserPrincipals.SPECIAL_OWNER)
                    flags |= ACE_OWNER;
                else if (who == UnixUserPrincipals.SPECIAL_GROUP)
                    flags |= (ACE_GROUP | ACE_IDENTIFIER_GROUP);
                else if (who == UnixUserPrincipals.SPECIAL_EVERYONE)
                    flags |= ACE_EVERYONE;
                else
                    throw new AssertionError("Unable to map special identifier");
            } else {
                if (user instanceof UnixUserPrincipals.Group) {
                    uid = user.gid();
                    flags |= ACE_IDENTIFIER_GROUP;
                } else {
                    uid = user.uid();
                }
            }
            int type;
            switch (ace.type()) {
                case ALLOW:
                    type = ACE_ACCESS_ALLOWED_ACE_TYPE;
                    break;
                case DENY:
                    type = ACE_ACCESS_DENIED_ACE_TYPE;
                    break;
                case AUDIT:
                    type = ACE_SYSTEM_AUDIT_ACE_TYPE;
                    break;
                case ALARM:
                    type = ACE_SYSTEM_ALARM_ACE_TYPE;
                    break;
                default:
                    throw new AssertionError("Unable to map ACE type");
            }
            Set<AclEntryPermission> aceMask = ace.permissions();
            int mask = 0;
            if (aceMask.contains(AclEntryPermission.READ_DATA))
                mask |= ACE_READ_DATA;
            if (aceMask.contains(AclEntryPermission.WRITE_DATA))
                mask |= ACE_WRITE_DATA;
            if (aceMask.contains(AclEntryPermission.APPEND_DATA))
                mask |= ACE_APPEND_DATA;
            if (aceMask.contains(AclEntryPermission.READ_NAMED_ATTRS))
                mask |= ACE_READ_NAMED_ATTRS;
            if (aceMask.contains(AclEntryPermission.WRITE_NAMED_ATTRS))
                mask |= ACE_WRITE_NAMED_ATTRS;
            if (aceMask.contains(AclEntryPermission.EXECUTE))
                mask |= ACE_EXECUTE;
            if (aceMask.contains(AclEntryPermission.DELETE_CHILD))
                mask |= ACE_DELETE_CHILD;
            if (aceMask.contains(AclEntryPermission.READ_ATTRIBUTES))
                mask |= ACE_READ_ATTRIBUTES;
            if (aceMask.contains(AclEntryPermission.WRITE_ATTRIBUTES))
                mask |= ACE_WRITE_ATTRIBUTES;
            if (aceMask.contains(AclEntryPermission.DELETE))
                mask |= ACE_DELETE;
            if (aceMask.contains(AclEntryPermission.READ_ACL))
                mask |= ACE_READ_ACL;
            if (aceMask.contains(AclEntryPermission.WRITE_ACL))
                mask |= ACE_WRITE_ACL;
            if (aceMask.contains(AclEntryPermission.WRITE_OWNER))
                mask |= ACE_WRITE_OWNER;
            if (aceMask.contains(AclEntryPermission.SYNCHRONIZE))
                mask |= ACE_SYNCHRONIZE;
            Set<AclEntryFlag> aceFlags = ace.flags();
            if (aceFlags.contains(AclEntryFlag.FILE_INHERIT))
                flags |= ACE_FILE_INHERIT_ACE;
            if (aceFlags.contains(AclEntryFlag.DIRECTORY_INHERIT))
                flags |= ACE_DIRECTORY_INHERIT_ACE;
            if (aceFlags.contains(AclEntryFlag.NO_PROPAGATE_INHERIT))
                flags |= ACE_NO_PROPAGATE_INHERIT_ACE;
            if (aceFlags.contains(AclEntryFlag.INHERIT_ONLY))
                flags |= ACE_INHERIT_ONLY_ACE;
            unsafe.putInt(offset + OFFSETOF_UID, uid);
            unsafe.putInt(offset + OFFSETOF_MASK, mask);
            unsafe.putShort(offset + OFFSETOF_FLAGS, (short)flags);
            unsafe.putShort(offset + OFFSETOF_TYPE, (short)type);
            offset += SIZEOF_ACE_T;
        }
    }
    private static List<AclEntry> decode(long address, int n) {
        ArrayList<AclEntry> acl = new ArrayList<>(n);
        for (int i=0; i<n; i++) {
            long offset = address + i*SIZEOF_ACE_T;
            int uid = unsafe.getInt(offset + OFFSETOF_UID);
            int mask = unsafe.getInt(offset + OFFSETOF_MASK);
            int flags = (int)unsafe.getShort(offset + OFFSETOF_FLAGS);
            int type = (int)unsafe.getShort(offset + OFFSETOF_TYPE);
            UnixUserPrincipals.User who = null;
            if (uid == -1) {
                if ((flags & ACE_OWNER) > 0)
                    who = UnixUserPrincipals.SPECIAL_OWNER;
                if ((flags & ACE_GROUP) > 0)
                    who = UnixUserPrincipals.SPECIAL_GROUP;
                if ((flags & ACE_EVERYONE) > 0)
                    who = UnixUserPrincipals.SPECIAL_EVERYONE;
                if (who == null)
                    throw new AssertionError("ACE who not handled");
            } else {
                if ((flags & ACE_IDENTIFIER_GROUP) > 0)
                    who = UnixUserPrincipals.fromGid(uid);
                else
                    who = UnixUserPrincipals.fromUid(uid);
            }
            AclEntryType aceType = null;
            switch (type) {
                case ACE_ACCESS_ALLOWED_ACE_TYPE:
                    aceType = AclEntryType.ALLOW;
                    break;
                case ACE_ACCESS_DENIED_ACE_TYPE:
                    aceType = AclEntryType.DENY;
                    break;
                case ACE_SYSTEM_AUDIT_ACE_TYPE:
                    aceType = AclEntryType.AUDIT;
                    break;
                case ACE_SYSTEM_ALARM_ACE_TYPE:
                    aceType = AclEntryType.ALARM;
                    break;
                default:
                    assert false;
            }
            Set<AclEntryPermission> aceMask = EnumSet.noneOf(AclEntryPermission.class);
            if ((mask & ACE_READ_DATA) > 0)
                aceMask.add(AclEntryPermission.READ_DATA);
            if ((mask & ACE_WRITE_DATA) > 0)
                aceMask.add(AclEntryPermission.WRITE_DATA);
            if ((mask & ACE_APPEND_DATA ) > 0)
                aceMask.add(AclEntryPermission.APPEND_DATA);
            if ((mask & ACE_READ_NAMED_ATTRS) > 0)
                aceMask.add(AclEntryPermission.READ_NAMED_ATTRS);
            if ((mask & ACE_WRITE_NAMED_ATTRS) > 0)
                aceMask.add(AclEntryPermission.WRITE_NAMED_ATTRS);
            if ((mask & ACE_EXECUTE) > 0)
                aceMask.add(AclEntryPermission.EXECUTE);
            if ((mask & ACE_DELETE_CHILD ) > 0)
                aceMask.add(AclEntryPermission.DELETE_CHILD);
            if ((mask & ACE_READ_ATTRIBUTES) > 0)
                aceMask.add(AclEntryPermission.READ_ATTRIBUTES);
            if ((mask & ACE_WRITE_ATTRIBUTES) > 0)
                aceMask.add(AclEntryPermission.WRITE_ATTRIBUTES);
            if ((mask & ACE_DELETE) > 0)
                aceMask.add(AclEntryPermission.DELETE);
            if ((mask & ACE_READ_ACL) > 0)
                aceMask.add(AclEntryPermission.READ_ACL);
            if ((mask & ACE_WRITE_ACL) > 0)
                aceMask.add(AclEntryPermission.WRITE_ACL);
            if ((mask & ACE_WRITE_OWNER) > 0)
                aceMask.add(AclEntryPermission.WRITE_OWNER);
            if ((mask & ACE_SYNCHRONIZE) > 0)
                aceMask.add(AclEntryPermission.SYNCHRONIZE);
            Set<AclEntryFlag> aceFlags = EnumSet.noneOf(AclEntryFlag.class);
            if ((flags & ACE_FILE_INHERIT_ACE) > 0)
                aceFlags.add(AclEntryFlag.FILE_INHERIT);
            if ((flags & ACE_DIRECTORY_INHERIT_ACE) > 0)
                aceFlags.add(AclEntryFlag.DIRECTORY_INHERIT);
            if ((flags & ACE_NO_PROPAGATE_INHERIT_ACE) > 0)
                aceFlags.add(AclEntryFlag.NO_PROPAGATE_INHERIT);
            if ((flags & ACE_INHERIT_ONLY_ACE) > 0)
                aceFlags.add(AclEntryFlag.INHERIT_ONLY);
            AclEntry ace = AclEntry.newBuilder()
                .setType(aceType)
                .setPrincipal(who)
                .setPermissions(aceMask).setFlags(aceFlags).build();
            acl.add(ace);
        }
        return acl;
    }
    private static boolean isAclsEnabled(int fd) {
        try {
            long enabled = fpathconf(fd, _PC_ACL_ENABLED);
            if (enabled == _ACL_ACE_ENABLED)
                return true;
        } catch (UnixException x) {
        }
        return false;
    }
    @Override
    public List<AclEntry> getAcl()
        throws IOException
    {
        checkAccess(file, true, false);
        int fd = file.openForAttributeAccess(followLinks);
        try {
            long address = unsafe.allocateMemory(SIZEOF_ACE_T * MAX_ACL_ENTRIES);
            try {
                int n = facl(fd, ACE_GETACL, MAX_ACL_ENTRIES, address);
                assert n >= 0;
                return decode(address, n);
            } catch (UnixException x) {
                if ((x.errno() == ENOSYS) || !isAclsEnabled(fd)) {
                    throw new FileSystemException(file.getPathForExecptionMessage(),
                        null, x.getMessage() + " (file system does not support NFSv4 ACLs)");
                }
                x.rethrowAsIOException(file);
                return null;    
            } finally {
                unsafe.freeMemory(address);
            }
        } finally {
            close(fd);
        }
    }
    @Override
    public void setAcl(List<AclEntry> acl) throws IOException {
        checkAccess(file, false, true);
        int fd = file.openForAttributeAccess(followLinks);
        try {
            acl = new ArrayList<AclEntry>(acl);
            int n = acl.size();
            long address = unsafe.allocateMemory(SIZEOF_ACE_T * n);
            try {
                encode(acl, address);
                facl(fd, ACE_SETACL, n, address);
            } catch (UnixException x) {
                if ((x.errno() == ENOSYS) || !isAclsEnabled(fd)) {
                    throw new FileSystemException(file.getPathForExecptionMessage(),
                        null, x.getMessage() + " (file system does not support NFSv4 ACLs)");
                }
                if (x.errno() == EINVAL && (n < 3))
                    throw new IOException("ACL must contain at least 3 entries");
                x.rethrowAsIOException(file);
            } finally {
                unsafe.freeMemory(address);
            }
        } finally {
            close(fd);
        }
    }
    @Override
    public UserPrincipal getOwner()
        throws IOException
    {
        checkAccess(file, true, false);
        try {
            UnixFileAttributes attrs =
                UnixFileAttributes.get(file, followLinks);
            return UnixUserPrincipals.fromUid(attrs.uid());
        } catch (UnixException x) {
            x.rethrowAsIOException(file);
            return null; 
        }
    }
    @Override
    public void setOwner(UserPrincipal owner) throws IOException {
        checkAccess(file, true, false);
        if (!(owner instanceof UnixUserPrincipals.User))
            throw new ProviderMismatchException();
        if (owner instanceof UnixUserPrincipals.Group)
            throw new IOException("'owner' parameter is a group");
        int uid = ((UnixUserPrincipals.User)owner).uid();
        try {
            if (followLinks) {
                lchown(file, uid, -1);
            } else {
                chown(file, uid, -1);
            }
        } catch (UnixException x) {
            x.rethrowAsIOException(file);
        }
    }
}
