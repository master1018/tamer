public class PartRequest extends Request {
    public Attachment mAttachment;
    public String mDestination;
    public String mContentUriString;
    public String mLocation;
    public PartRequest(Attachment _att) {
        mMessageId = _att.mMessageKey;
        mAttachment = _att;
        mLocation = mAttachment.mLocation;
    }
    public PartRequest(Attachment _att, String _destination, String _contentUriString) {
        this(_att);
        mDestination = _destination;
        mContentUriString = _contentUriString;
    }
}
