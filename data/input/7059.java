public final class AclEntry {
    private final AclEntryType type;
    private final UserPrincipal who;
    private final Set<AclEntryPermission> perms;
    private final Set<AclEntryFlag> flags;
    private volatile int hash;
    private AclEntry(AclEntryType type,
                     UserPrincipal who,
                     Set<AclEntryPermission> perms,
                     Set<AclEntryFlag> flags)
    {
        this.type = type;
        this.who = who;
        this.perms = perms;
        this.flags = flags;
    }
    public static final class Builder {
        private AclEntryType type;
        private UserPrincipal who;
        private Set<AclEntryPermission> perms;
        private Set<AclEntryFlag> flags;
        private Builder(AclEntryType type,
                        UserPrincipal who,
                        Set<AclEntryPermission> perms,
                        Set<AclEntryFlag> flags)
        {
            assert perms != null && flags != null;
            this.type = type;
            this.who = who;
            this.perms = perms;
            this.flags = flags;
        }
        public AclEntry build() {
            if (type == null)
                throw new IllegalStateException("Missing type component");
            if (who == null)
                throw new IllegalStateException("Missing who component");
            return new AclEntry(type, who, perms, flags);
        }
        public Builder setType(AclEntryType type) {
            if (type == null)
                throw new NullPointerException();
            this.type = type;
            return this;
        }
        public Builder setPrincipal(UserPrincipal who) {
            if (who == null)
                throw new NullPointerException();
            this.who = who;
            return this;
        }
        private static void checkSet(Set<?> set, Class<?> type) {
            for (Object e: set) {
                if (e == null)
                    throw new NullPointerException();
                type.cast(e);
            }
        }
        public Builder setPermissions(Set<AclEntryPermission> perms) {
            perms = EnumSet.copyOf(perms);
            checkSet(perms, AclEntryPermission.class);
            this.perms = perms;
            return this;
        }
        public Builder setPermissions(AclEntryPermission... perms) {
            Set<AclEntryPermission> set = EnumSet.noneOf(AclEntryPermission.class);
            for (AclEntryPermission p: perms) {
                if (p == null)
                    throw new NullPointerException();
                set.add(p);
            }
            this.perms = set;
            return this;
        }
        public Builder setFlags(Set<AclEntryFlag> flags) {
            flags = EnumSet.copyOf(flags);
            checkSet(flags, AclEntryFlag.class);
            this.flags = flags;
            return this;
        }
        public Builder setFlags(AclEntryFlag... flags) {
            Set<AclEntryFlag> set = EnumSet.noneOf(AclEntryFlag.class);
            for (AclEntryFlag f: flags) {
                if (f == null)
                    throw new NullPointerException();
                set.add(f);
            }
            this.flags = set;
            return this;
        }
    }
    public static Builder newBuilder() {
        Set<AclEntryPermission> perms = Collections.emptySet();
        Set<AclEntryFlag> flags = Collections.emptySet();
        return new Builder(null, null, perms, flags);
    }
    public static Builder newBuilder(AclEntry entry) {
        return new Builder(entry.type, entry.who, entry.perms, entry.flags);
    }
    public AclEntryType type() {
        return type;
    }
    public UserPrincipal principal() {
        return who;
    }
    public Set<AclEntryPermission> permissions() {
        return new HashSet<AclEntryPermission>(perms);
    }
    public Set<AclEntryFlag> flags() {
        return new HashSet<AclEntryFlag>(flags);
    }
    @Override
    public boolean equals(Object ob) {
        if (ob == this)
            return true;
        if (ob == null || !(ob instanceof AclEntry))
            return false;
        AclEntry other = (AclEntry)ob;
        if (this.type != other.type)
            return false;
        if (!this.who.equals(other.who))
            return false;
        if (!this.perms.equals(other.perms))
            return false;
        if (!this.flags.equals(other.flags))
            return false;
        return true;
    }
    private static int hash(int h, Object o) {
        return h * 127 + o.hashCode();
    }
    @Override
    public int hashCode() {
        if (hash != 0)
            return hash;
        int h = type.hashCode();
        h = hash(h, who);
        h = hash(h, perms);
        h = hash(h, flags);
        hash = h;
        return hash;
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(who.getName());
        sb.append(':');
        for (AclEntryPermission perm: perms) {
            sb.append(perm.name());
            sb.append('/');
        }
        sb.setLength(sb.length()-1); 
        sb.append(':');
        if (!flags.isEmpty()) {
            for (AclEntryFlag flag: flags) {
                sb.append(flag.name());
                sb.append('/');
            }
            sb.setLength(sb.length()-1);  
            sb.append(':');
        }
        sb.append(type.name());
        return sb.toString();
    }
}
