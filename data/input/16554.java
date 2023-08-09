class TLGlobals {
    final PropMap props;
    private final Map<String, Utf8Entry> utf8Entries;
    private final Map<String, ClassEntry> classEntries;
    private final Map<Object, LiteralEntry> literalEntries;
    private final Map<String, SignatureEntry> signatureEntries;
    private final Map<String, DescriptorEntry> descriptorEntries;
    private final Map<String, MemberEntry> memberEntries;
    TLGlobals() {
        utf8Entries = new HashMap<>();
        classEntries = new HashMap<>();
        literalEntries = new HashMap<>();
        signatureEntries = new HashMap<>();
        descriptorEntries = new HashMap<>();
        memberEntries = new HashMap<>();
        props = new PropMap();
    }
    SortedMap<Object, Object> getPropMap() {
        return props;
    }
    Map<String, Utf8Entry> getUtf8Entries() {
        return utf8Entries;
    }
    Map<String, ClassEntry> getClassEntries() {
        return classEntries;
    }
    Map<Object, LiteralEntry> getLiteralEntries() {
        return literalEntries;
    }
    Map<String, DescriptorEntry> getDescriptorEntries() {
         return descriptorEntries;
    }
    Map<String, SignatureEntry> getSignatureEntries() {
        return signatureEntries;
    }
    Map<String, MemberEntry> getMemberEntries() {
        return memberEntries;
    }
}
