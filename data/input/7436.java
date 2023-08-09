class ZoneNode extends NameNode {
    private SoftReference contentsRef = null;   
    private long serialNumber = -1;     
    private Date expiration = null;     
    ZoneNode(String label) {
        super(label);
    }
    protected NameNode newNameNode(String label) {
        return new ZoneNode(label);
    }
    synchronized void depopulate() {
        contentsRef = null;
        serialNumber = -1;
    }
    synchronized boolean isPopulated() {
        return (getContents() != null);
    }
    synchronized NameNode getContents() {
        return (contentsRef != null)
                ? (NameNode) contentsRef.get()
                : null;
    }
    synchronized boolean isExpired() {
        return ((expiration != null) && expiration.before(new Date()));
    }
    ZoneNode getDeepestPopulated(DnsName fqdn) {
        ZoneNode znode = this;
        ZoneNode popNode = isPopulated() ? this : null;
        for (int i = 1; i < fqdn.size(); i++) { 
            znode = (ZoneNode) znode.get(fqdn.getKey(i));
            if (znode == null) {
                break;
            } else if (znode.isPopulated()) {
                popNode = znode;
            }
        }
        return popNode;
    }
    NameNode populate(DnsName zone, ResourceRecords rrs) {
        NameNode newContents = new NameNode(null);
        for (int i = 0; i < rrs.answer.size(); i++) {
            ResourceRecord rr = (ResourceRecord) rrs.answer.elementAt(i);
            DnsName n = rr.getName();
            if ((n.size() > zone.size()) && n.startsWith(zone)) {
                NameNode nnode = newContents.add(n, zone.size());
                if (rr.getType() == ResourceRecord.TYPE_NS) {
                    nnode.setZoneCut(true);
                }
            }
        }
        ResourceRecord soa = (ResourceRecord) rrs.answer.firstElement();
        synchronized (this) {
            contentsRef = new SoftReference(newContents);
            serialNumber = getSerialNumber(soa);
            setExpiration(getMinimumTtl(soa));
            return newContents;
        }
    }
    private void setExpiration(long secsToExpiration) {
        expiration = new Date(System.currentTimeMillis() +
                              1000 * secsToExpiration);
    }
    private static long getMinimumTtl(ResourceRecord soa) {
        String rdata = (String) soa.getRdata();
        int pos = rdata.lastIndexOf(' ') + 1;
        return Long.parseLong(rdata.substring(pos));
    }
    int compareSerialNumberTo(ResourceRecord soa) {
        return ResourceRecord.compareSerialNumbers(serialNumber,
                                                   getSerialNumber(soa));
    }
    private static long getSerialNumber(ResourceRecord soa) {
        String rdata = (String) soa.getRdata();
        int beg = rdata.length();
        int end = -1;
        for (int i = 0; i < 5; i++) {
            end = beg;
            beg = rdata.lastIndexOf(' ', end - 1);
        }
        return Long.parseLong(rdata.substring(beg + 1, end));
    }
}
