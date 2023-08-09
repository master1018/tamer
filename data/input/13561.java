public class SctpShutdown extends ShutdownNotification
    implements SctpNotification
{
    private Association association;
    private int assocId;
    private SctpShutdown(int assocId) {
        this.assocId = assocId;
    }
    @Override
    public int assocId() {
        return assocId;
    }
    @Override
    public void setAssociation(Association association) {
        this.association = association;
    }
    @Override
    public Association association() {
        assert association != null;
        return association;
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString()).append(" [");
        sb.append("Association:").append(association).append("]");
        return sb.toString();
    }
}
