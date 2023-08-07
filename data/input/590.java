public class ActivityImpl extends org.apache.shindig.social.core.model.ActivityImpl implements Activity {
    private List<String> recipients;
    public List<String> getRecipients() {
        return recipients;
    }
    public void setRecipients(List<String> userIds) {
        this.recipients = userIds;
    }
}
