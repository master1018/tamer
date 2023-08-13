public class RequestPartitioningComponentImpl extends TaggedComponentBase
    implements RequestPartitioningComponent
{
    private static ORBUtilSystemException wrapper =
        ORBUtilSystemException.get( CORBALogDomains.OA_IOR ) ;
    private int partitionToUse;
    public boolean equals(Object obj)
    {
        if (!(obj instanceof RequestPartitioningComponentImpl))
            return false ;
        RequestPartitioningComponentImpl other =
            (RequestPartitioningComponentImpl)obj ;
        return partitionToUse == other.partitionToUse ;
    }
    public int hashCode()
    {
        return partitionToUse;
    }
    public String toString()
    {
        return "RequestPartitioningComponentImpl[partitionToUse=" + partitionToUse + "]" ;
    }
    public RequestPartitioningComponentImpl()
    {
        partitionToUse = 0;
    }
    public RequestPartitioningComponentImpl(int thePartitionToUse) {
        if (thePartitionToUse < ORBConstants.REQUEST_PARTITIONING_MIN_THREAD_POOL_ID ||
            thePartitionToUse > ORBConstants.REQUEST_PARTITIONING_MAX_THREAD_POOL_ID) {
            throw wrapper.invalidRequestPartitioningComponentValue(
                  new Integer(thePartitionToUse),
                  new Integer(ORBConstants.REQUEST_PARTITIONING_MIN_THREAD_POOL_ID),
                  new Integer(ORBConstants.REQUEST_PARTITIONING_MAX_THREAD_POOL_ID));
        }
        partitionToUse = thePartitionToUse;
    }
    public int getRequestPartitioningId()
    {
        return partitionToUse;
    }
    public void writeContents(OutputStream os)
    {
        os.write_ulong(partitionToUse);
    }
    public int getId()
    {
        return ORBConstants.TAG_REQUEST_PARTITIONING_ID;
    }
}
