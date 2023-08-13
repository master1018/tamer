class PendingThumbnailsRecord
{
    final IThumbnailReceiver receiver;   
    HashSet pendingRecords; 
    boolean finished;       
    PendingThumbnailsRecord(IThumbnailReceiver _receiver)
    {
        receiver = _receiver;
        pendingRecords = new HashSet();
        finished = false;
    }
}
