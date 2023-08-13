class MembershipRegistry {
    private Map<InetAddress,List<MembershipKeyImpl>> groups = null;
    MembershipRegistry() {
    }
    MembershipKey checkMembership(InetAddress group, NetworkInterface interf,
                                  InetAddress source)
    {
        if (groups != null) {
            List<MembershipKeyImpl> keys = groups.get(group);
            if (keys != null) {
                for (MembershipKeyImpl key: keys) {
                    if (key.networkInterface().equals(interf)) {
                        if (source == null) {
                            if (key.sourceAddress() == null)
                                return key;
                            throw new IllegalStateException("Already a member to receive all packets");
                        }
                        if (key.sourceAddress() == null)
                            throw new IllegalStateException("Already have source-specific membership");
                        if (source.equals(key.sourceAddress()))
                            return key;
                    }
                }
            }
        }
        return null;
    }
    void add(MembershipKeyImpl key) {
        InetAddress group = key.group();
        List<MembershipKeyImpl> keys;
        if (groups == null) {
            groups = new HashMap<InetAddress,List<MembershipKeyImpl>>();
            keys = null;
        } else {
            keys = groups.get(group);
        }
        if (keys == null) {
            keys = new LinkedList<MembershipKeyImpl>();
            groups.put(group, keys);
        }
        keys.add(key);
    }
    void remove(MembershipKeyImpl key) {
        InetAddress group = key.group();
        List<MembershipKeyImpl> keys = groups.get(group);
        if (keys != null) {
            Iterator<MembershipKeyImpl> i = keys.iterator();
            while (i.hasNext()) {
                if (i.next() == key) {
                    i.remove();
                    break;
                }
            }
            if (keys.isEmpty()) {
                groups.remove(group);
            }
        }
    }
    void invalidateAll() {
        if (groups != null) {
            for (InetAddress group: groups.keySet()) {
                for (MembershipKeyImpl key: groups.get(group)) {
                    key.invalidate();
                }
            }
        }
    }
}
