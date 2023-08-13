class SnmpEntryOid extends SnmpOid {
    private static final long serialVersionUID = 9212653887791059564L;
    public SnmpEntryOid(long[] oid, int start) {
        final int subLength = oid.length - start;
        final long[] subOid = new long[subLength];
        java.lang.System.arraycopy(oid, start, subOid, 0, subLength) ;
        components = subOid;
        componentCount = subLength;
    }
}
