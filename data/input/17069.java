public final class RepIdDelegator
    implements RepositoryIdStrings,
               RepositoryIdUtility,
               RepositoryIdInterface
{
    public String createForAnyType(Class type) {
        return RepositoryId.createForAnyType(type);
    }
    public String createForJavaType(Serializable ser)
        throws TypeMismatchException
    {
        return RepositoryId.createForJavaType(ser);
    }
    public String createForJavaType(Class clz)
        throws TypeMismatchException
    {
        return RepositoryId.createForJavaType(clz);
    }
    public String createSequenceRepID(java.lang.Object ser) {
        return RepositoryId.createSequenceRepID(ser);
    }
    public String createSequenceRepID(Class clazz) {
        return RepositoryId.createSequenceRepID(clazz);
    }
    public RepositoryIdInterface getFromString(String repIdString) {
        return new RepIdDelegator(RepositoryId.cache.getId(repIdString));
    }
    public boolean isChunkedEncoding(int valueTag) {
        return RepositoryId.isChunkedEncoding(valueTag);
    }
    public boolean isCodeBasePresent(int valueTag) {
        return RepositoryId.isCodeBasePresent(valueTag);
    }
    public String getClassDescValueRepId() {
        return RepositoryId.kClassDescValueRepID;
    }
    public String getWStringValueRepId() {
        return RepositoryId.kWStringValueRepID;
    }
    public int getTypeInfo(int valueTag) {
        return RepositoryId.getTypeInfo(valueTag);
    }
    public int getStandardRMIChunkedNoRepStrId() {
        return RepositoryId.kPreComputed_StandardRMIChunked_NoRep;
    }
    public int getCodeBaseRMIChunkedNoRepStrId() {
        return RepositoryId.kPreComputed_CodeBaseRMIChunked_NoRep;
    }
    public int getStandardRMIChunkedId() {
        return RepositoryId.kPreComputed_StandardRMIChunked;
    }
    public int getCodeBaseRMIChunkedId() {
        return RepositoryId.kPreComputed_CodeBaseRMIChunked;
    }
    public int getStandardRMIUnchunkedId() {
        return RepositoryId.kPreComputed_StandardRMIUnchunked;
    }
    public int getCodeBaseRMIUnchunkedId() {
        return RepositoryId.kPreComputed_CodeBaseRMIUnchunked;
    }
    public int getStandardRMIUnchunkedNoRepStrId() {
        return RepositoryId.kPreComputed_StandardRMIUnchunked_NoRep;
    }
    public int getCodeBaseRMIUnchunkedNoRepStrId() {
        return RepositoryId.kPreComputed_CodeBaseRMIUnchunked_NoRep;
    }
    public Class getClassFromType() throws ClassNotFoundException {
        return delegate.getClassFromType();
    }
    public Class getClassFromType(String codebaseURL)
        throws ClassNotFoundException, MalformedURLException
    {
        return delegate.getClassFromType(codebaseURL);
    }
    public Class getClassFromType(Class expectedType,
                                  String codebaseURL)
        throws ClassNotFoundException, MalformedURLException
    {
        return delegate.getClassFromType(expectedType, codebaseURL);
    }
    public String getClassName() {
        return delegate.getClassName();
    }
    public RepIdDelegator() {}
    private RepIdDelegator(RepositoryId _delegate) {
        this.delegate = _delegate;
    }
    private RepositoryId delegate;
    public String toString() {
        if (delegate != null)
            return delegate.toString();
        else
            return this.getClass().getName();
    }
    public boolean equals(Object obj) {
        if (delegate != null)
            return delegate.equals(obj);
        else
            return super.equals(obj);
    }
}
