class SourceNameReferenceTypeSpec implements ReferenceTypeSpec {
    final String sourceName;
    final int linenumber;
    SourceNameReferenceTypeSpec(String sourceName, int linenumber) {
        this.sourceName = sourceName;
        this.linenumber = linenumber;
    }
    @Override
    public boolean matches(ReferenceType refType) {
        try {
            if (refType.sourceName().equals(sourceName)) {
                try {
                    refType.locationsOfLine(linenumber);
                    return true;
                } catch(AbsentInformationException exc) {
                } catch(ObjectCollectedException  exc) {
                }
            }
        } catch(AbsentInformationException exc) {
        }
        return false;
    }
    @Override
    public int hashCode() {
        return sourceName.hashCode() + linenumber;
    }
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SourceNameReferenceTypeSpec) {
            SourceNameReferenceTypeSpec spec = (SourceNameReferenceTypeSpec)obj;
            return sourceName.equals(spec.sourceName) &&
                              (linenumber == spec.linenumber);
        } else {
            return false;
        }
    }
    @Override
    public String toString() {
        return sourceName + "@" + linenumber;
    }
}
