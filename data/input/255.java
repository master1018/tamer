public class SctpAssociationImpl extends Association {
    public SctpAssociationImpl(int associationID,
                               int maxInStreams,
                               int maxOutStreams) {
        super(associationID, maxInStreams, maxOutStreams);
    }
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(super.toString());
        return sb.append("[associationID:")
                 .append(associationID())
                 .append(", maxIn:")
                 .append(maxInboundStreams())
                 .append(", maxOut:")
                 .append(maxOutboundStreams())
                 .append("]")
                 .toString();
    }
}
