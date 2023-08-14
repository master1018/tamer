class WindowsSecurityDescriptor {
    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private static final short SIZEOF_ACL                   = 8;
    private static final short SIZEOF_ACCESS_ALLOWED_ACE    = 12;
    private static final short SIZEOF_ACCESS_DENIED_ACE     = 12;
    private static final short SIZEOF_SECURITY_DESCRIPTOR   = 20;
    private static final short OFFSETOF_TYPE                = 0;
    private static final short OFFSETOF_FLAGS               = 1;
    private static final short OFFSETOF_ACCESS_MASK         = 4;
    private static final short OFFSETOF_SID                 = 8;
    private static final WindowsSecurityDescriptor NULL_DESCRIPTOR =
        new WindowsSecurityDescriptor();
    private final List<Long> sidList;
    private final NativeBuffer aclBuffer, sdBuffer;
    private WindowsSecurityDescriptor() {
        this.sidList = null;
        this.aclBuffer = null;
        this.sdBuffer = null;
    }
    private WindowsSecurityDescriptor(List<AclEntry> acl) throws IOException {
        boolean initialized = false;
        acl = new ArrayList<AclEntry>(acl);
        sidList = new ArrayList<Long>(acl.size());
        try {
            int size = SIZEOF_ACL;
            for (AclEntry entry: acl) {
                UserPrincipal user = entry.principal();
                if (!(user instanceof WindowsUserPrincipals.User))
                    throw new ProviderMismatchException();
                String sidString = ((WindowsUserPrincipals.User)user).sidString();
                try {
                    long pSid = ConvertStringSidToSid(sidString);
                    sidList.add(pSid);
                    size += GetLengthSid(pSid) +
                        Math.max(SIZEOF_ACCESS_ALLOWED_ACE, SIZEOF_ACCESS_DENIED_ACE);
                } catch (WindowsException x) {
                    throw new IOException("Failed to get SID for " + user.getName()
                        + ": " + x.errorString());
                }
            }
            aclBuffer = NativeBuffers.getNativeBuffer(size);
            sdBuffer = NativeBuffers.getNativeBuffer(SIZEOF_SECURITY_DESCRIPTOR);
            InitializeAcl(aclBuffer.address(), size);
            int i = 0;
            while (i < acl.size()) {
                AclEntry entry = acl.get(i);
                long pSid = sidList.get(i);
                try {
                    encode(entry, pSid, aclBuffer.address());
                } catch (WindowsException x) {
                    throw new IOException("Failed to encode ACE: " +
                        x.errorString());
                }
                i++;
            }
            InitializeSecurityDescriptor(sdBuffer.address());
            SetSecurityDescriptorDacl(sdBuffer.address(), aclBuffer.address());
            initialized = true;
        } catch (WindowsException x) {
            throw new IOException(x.getMessage());
        } finally {
            if (!initialized)
                release();
        }
    }
    void release() {
        if (sdBuffer != null)
            sdBuffer.release();
        if (aclBuffer != null)
            aclBuffer.release();
        if (sidList != null) {
            for (Long sid: sidList) {
                LocalFree(sid);
            }
        }
    }
    long address() {
        return (sdBuffer == null) ? 0L : sdBuffer.address();
    }
    private static AclEntry decode(long aceAddress)
        throws IOException
    {
        byte aceType = unsafe.getByte(aceAddress + OFFSETOF_TYPE);
        if (aceType != ACCESS_ALLOWED_ACE_TYPE && aceType != ACCESS_DENIED_ACE_TYPE)
            return null;
        AclEntryType type;
        if (aceType == ACCESS_ALLOWED_ACE_TYPE) {
            type = AclEntryType.ALLOW;
        } else {
            type = AclEntryType.DENY;
        }
        byte aceFlags = unsafe.getByte(aceAddress + OFFSETOF_FLAGS);
        Set<AclEntryFlag> flags = EnumSet.noneOf(AclEntryFlag.class);
        if ((aceFlags & OBJECT_INHERIT_ACE) != 0)
            flags.add(AclEntryFlag.FILE_INHERIT);
        if ((aceFlags & CONTAINER_INHERIT_ACE) != 0)
            flags.add(AclEntryFlag.DIRECTORY_INHERIT);
        if ((aceFlags & NO_PROPAGATE_INHERIT_ACE) != 0)
            flags.add(AclEntryFlag.NO_PROPAGATE_INHERIT);
        if ((aceFlags & INHERIT_ONLY_ACE) != 0)
            flags.add(AclEntryFlag.INHERIT_ONLY);
        int mask = unsafe.getInt(aceAddress + OFFSETOF_ACCESS_MASK);
        Set<AclEntryPermission> perms = EnumSet.noneOf(AclEntryPermission.class);
        if ((mask & FILE_READ_DATA) > 0)
            perms.add(AclEntryPermission.READ_DATA);
        if ((mask & FILE_WRITE_DATA) > 0)
            perms.add(AclEntryPermission.WRITE_DATA);
        if ((mask & FILE_APPEND_DATA ) > 0)
            perms.add(AclEntryPermission.APPEND_DATA);
        if ((mask & FILE_READ_EA) > 0)
            perms.add(AclEntryPermission.READ_NAMED_ATTRS);
        if ((mask & FILE_WRITE_EA) > 0)
            perms.add(AclEntryPermission.WRITE_NAMED_ATTRS);
        if ((mask & FILE_EXECUTE) > 0)
            perms.add(AclEntryPermission.EXECUTE);
        if ((mask & FILE_DELETE_CHILD ) > 0)
            perms.add(AclEntryPermission.DELETE_CHILD);
        if ((mask & FILE_READ_ATTRIBUTES) > 0)
            perms.add(AclEntryPermission.READ_ATTRIBUTES);
        if ((mask & FILE_WRITE_ATTRIBUTES) > 0)
            perms.add(AclEntryPermission.WRITE_ATTRIBUTES);
        if ((mask & DELETE) > 0)
            perms.add(AclEntryPermission.DELETE);
        if ((mask & READ_CONTROL) > 0)
            perms.add(AclEntryPermission.READ_ACL);
        if ((mask & WRITE_DAC) > 0)
            perms.add(AclEntryPermission.WRITE_ACL);
        if ((mask & WRITE_OWNER) > 0)
            perms.add(AclEntryPermission.WRITE_OWNER);
        if ((mask & SYNCHRONIZE) > 0)
            perms.add(AclEntryPermission.SYNCHRONIZE);
        long sidAddress = aceAddress + OFFSETOF_SID;
        UserPrincipal user = WindowsUserPrincipals.fromSid(sidAddress);
        return AclEntry.newBuilder()
            .setType(type)
            .setPrincipal(user)
            .setFlags(flags).setPermissions(perms).build();
    }
    private static void encode(AclEntry ace, long sidAddress, long aclAddress)
        throws WindowsException
    {
        if (ace.type() != AclEntryType.ALLOW && ace.type() != AclEntryType.DENY)
            return;
        boolean allow = (ace.type() == AclEntryType.ALLOW);
        Set<AclEntryPermission> aceMask = ace.permissions();
        int mask = 0;
        if (aceMask.contains(AclEntryPermission.READ_DATA))
            mask |= FILE_READ_DATA;
        if (aceMask.contains(AclEntryPermission.WRITE_DATA))
            mask |= FILE_WRITE_DATA;
        if (aceMask.contains(AclEntryPermission.APPEND_DATA))
            mask |= FILE_APPEND_DATA;
        if (aceMask.contains(AclEntryPermission.READ_NAMED_ATTRS))
            mask |= FILE_READ_EA;
        if (aceMask.contains(AclEntryPermission.WRITE_NAMED_ATTRS))
            mask |= FILE_WRITE_EA;
        if (aceMask.contains(AclEntryPermission.EXECUTE))
            mask |= FILE_EXECUTE;
        if (aceMask.contains(AclEntryPermission.DELETE_CHILD))
            mask |= FILE_DELETE_CHILD;
        if (aceMask.contains(AclEntryPermission.READ_ATTRIBUTES))
            mask |= FILE_READ_ATTRIBUTES;
        if (aceMask.contains(AclEntryPermission.WRITE_ATTRIBUTES))
            mask |= FILE_WRITE_ATTRIBUTES;
        if (aceMask.contains(AclEntryPermission.DELETE))
            mask |= DELETE;
        if (aceMask.contains(AclEntryPermission.READ_ACL))
            mask |= READ_CONTROL;
        if (aceMask.contains(AclEntryPermission.WRITE_ACL))
            mask |= WRITE_DAC;
        if (aceMask.contains(AclEntryPermission.WRITE_OWNER))
            mask |= WRITE_OWNER;
        if (aceMask.contains(AclEntryPermission.SYNCHRONIZE))
            mask |= SYNCHRONIZE;
        Set<AclEntryFlag> aceFlags = ace.flags();
        byte flags = 0;
        if (aceFlags.contains(AclEntryFlag.FILE_INHERIT))
            flags |= OBJECT_INHERIT_ACE;
        if (aceFlags.contains(AclEntryFlag.DIRECTORY_INHERIT))
            flags |= CONTAINER_INHERIT_ACE;
        if (aceFlags.contains(AclEntryFlag.NO_PROPAGATE_INHERIT))
            flags |= NO_PROPAGATE_INHERIT_ACE;
        if (aceFlags.contains(AclEntryFlag.INHERIT_ONLY))
            flags |= INHERIT_ONLY_ACE;
        if (allow) {
            AddAccessAllowedAceEx(aclAddress, flags, mask, sidAddress);
        } else {
            AddAccessDeniedAceEx(aclAddress, flags, mask, sidAddress);
        }
    }
    static WindowsSecurityDescriptor create(List<AclEntry> acl)
        throws IOException
    {
        return new WindowsSecurityDescriptor(acl);
    }
    @SuppressWarnings("unchecked")
    static WindowsSecurityDescriptor fromAttribute(FileAttribute<?>... attrs)
        throws IOException
    {
        WindowsSecurityDescriptor sd = NULL_DESCRIPTOR;
        for (FileAttribute<?> attr: attrs) {
            if (sd != NULL_DESCRIPTOR)
                sd.release();
            if (attr == null)
                throw new NullPointerException();
            if (attr.name().equals("acl:acl")) {
                List<AclEntry> acl = (List<AclEntry>)attr.value();
                sd = new WindowsSecurityDescriptor(acl);
            } else {
                throw new UnsupportedOperationException("'" + attr.name() +
                   "' not supported as initial attribute");
            }
        }
        return sd;
    }
    static List<AclEntry> getAcl(long pSecurityDescriptor) throws IOException {
        long aclAddress = GetSecurityDescriptorDacl(pSecurityDescriptor);
        int aceCount = 0;
        if (aclAddress == 0L) {
            aceCount = 0;
        } else {
            AclInformation aclInfo = GetAclInformation(aclAddress);
            aceCount = aclInfo.aceCount();
        }
        ArrayList<AclEntry> result = new ArrayList<>(aceCount);
        for (int i=0; i<aceCount; i++) {
            long aceAddress = GetAce(aclAddress, i);
            AclEntry entry = decode(aceAddress);
            if (entry != null)
                result.add(entry);
        }
        return result;
    }
}
