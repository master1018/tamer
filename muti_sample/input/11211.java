public class ValueHandlerImpl_1_3 extends com.sun.corba.se.impl.io.ValueHandlerImpl {
    public ValueHandlerImpl_1_3(){
        super();
    }
    public ValueHandlerImpl_1_3(boolean isInputStream) {
        super(isInputStream);
    }
    public void writeValue(org.omg.CORBA.portable.OutputStream _out, java.io.Serializable value) {
        super.writeValue(_out, value);
    }
    public java.io.Serializable readValue(org.omg.CORBA.portable.InputStream _in,
                                          int offset,
                                          java.lang.Class clazz,
                                          String repositoryID,
                                          org.omg.SendingContext.RunTime _sender)
    {
        return super.readValue(_in, offset, clazz, repositoryID, _sender);
    }
    public java.lang.String getRMIRepositoryID(java.lang.Class clz) {
        return RepositoryId_1_3.createForJavaType(clz);
    }
    public boolean isCustomMarshaled(java.lang.Class clz) {
        return super.isCustomMarshaled(clz);
    }
    public org.omg.SendingContext.RunTime getRunTimeCodeBase() {
        return super.getRunTimeCodeBase();
    }
    public java.io.Serializable writeReplace(java.io.Serializable value) {
        return super.writeReplace(value);
    }
     public boolean useFullValueDescription(Class clazz, String repositoryID)
        throws IOException
     {
        return RepositoryId_1_3.useFullValueDescription(clazz, repositoryID);
     }
     public String getClassName(String id)
     {
        RepositoryId_1_3 repID = RepositoryId_1_3.cache.getId(id);
        return repID.getClassName();
     }
     public Class getClassFromType(String id)
        throws ClassNotFoundException
     {
        RepositoryId_1_3 repId = RepositoryId_1_3.cache.getId(id);
        return repId.getClassFromType();
     }
     public Class getAnyClassFromType(String id)
        throws ClassNotFoundException
     {
        RepositoryId_1_3 repId = RepositoryId_1_3.cache.getId(id);
        return repId.getAnyClassFromType();
     }
     public String createForAnyType(Class cl)
     {
        return RepositoryId_1_3.createForAnyType(cl);
     }
     public String getDefinedInId(String id)
     {
        RepositoryId_1_3 repId = RepositoryId_1_3.cache.getId(id);
        return repId.getDefinedInId();
     }
     public String getUnqualifiedName(String id)
     {
        RepositoryId_1_3 repId = RepositoryId_1_3.cache.getId(id);
        return repId.getUnqualifiedName();
     }
     public String getSerialVersionUID(String id)
     {
        RepositoryId_1_3 repId = RepositoryId_1_3.cache.getId(id);
        return repId.getSerialVersionUID();
     }
     public boolean isAbstractBase(Class clazz)
     {
        return RepositoryId_1_3.isAbstractBase(clazz);
     }
     public boolean isSequence(String id)
     {
        RepositoryId_1_3 repId = RepositoryId_1_3.cache.getId(id);
        return repId.isSequence();
     }
    protected void writeCharArray(org.omg.CORBA_2_3.portable.OutputStream out,
                                char[] array,
                                int offset,
                                int length)
    {
        out.write_char_array(array, offset, length);
    }
    protected void readCharArray(org.omg.CORBA_2_3.portable.InputStream in,
                                 char[] array,
                                 int offset,
                                 int length)
    {
        in.read_char_array(array, offset, length);
    }
    protected final String getOutputStreamClassName() {
        return "com.sun.corba.se.impl.orbutil.IIOPOutputStream_1_3";
    }
    protected final String getInputStreamClassName() {
        return "com.sun.corba.se.impl.orbutil.IIOPInputStream_1_3";
    }
    protected TCKind getJavaCharTCKind() {
        return TCKind.tk_char;
    }
}
