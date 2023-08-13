public final class RepIdDelegator_1_3
    implements RepositoryIdStrings,
               RepositoryIdUtility,
               RepositoryIdInterface
{
    public String createForAnyType(Class type) {
        return RepositoryId_1_3.createForAnyType(type);
    }
    public String createForJavaType(Serializable ser)
        throws TypeMismatchException
    {
        return RepositoryId_1_3.createForJavaType(ser);
    }
    public String createForJavaType(Class clz)
        throws TypeMismatchException
    {
        return RepositoryId_1_3.createForJavaType(clz);
    }
    public String createSequenceRepID(java.lang.Object ser) {
        return RepositoryId_1_3.createSequenceRepID(ser);
    }
    public String createSequenceRepID(Class clazz) {
        return RepositoryId_1_3.createSequenceRepID(clazz);
    }
    public RepositoryIdInterface getFromString(String repIdString) {
        return new RepIdDelegator_1_3(RepositoryId_1_3.cache.getId(repIdString));
    }
    public boolean isChunkedEncoding(int valueTag) {
        return RepositoryId.isChunkedEncoding(valueTag);
    }
    public boolean isCodeBasePresent(int valueTag) {
        return RepositoryId.isCodeBasePresent(valueTag);
    }
    public String getClassDescValueRepId() {
        return RepositoryId_1_3.kClassDescValueRepID;
    }
    public String getWStringValueRepId() {
        return RepositoryId_1_3.kWStringValueRepID;
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
    public RepIdDelegator_1_3() {}
    private RepIdDelegator_1_3(RepositoryId_1_3 _delegate) {
        this.delegate = _delegate;
    }
    private RepositoryId_1_3 delegate = null;
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
