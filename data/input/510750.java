public class DeliveryReportListItem extends LinearLayout {
    private TextView mRecipientView;
    private TextView mStatusView;
    private ImageView mIconView;
    DeliveryReportListItem(Context context) {
        super(context);
    }
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mRecipientView = (TextView) findViewById(R.id.recipient);
        mStatusView = (TextView) findViewById(R.id.status);
        mIconView = (ImageView) findViewById(R.id.icon);
    }
    public DeliveryReportListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public final void bind(String recipient, String status) {
        if (!TextUtils.isEmpty(recipient)) {
            mRecipientView.setText(Contact.get(recipient, false).getName());
        } else {
            mRecipientView.setText("");
        }
        mStatusView.setText(status);
        Context context = getContext();
        String receivedStr = context.getString(R.string.status_received);
        String failedStr = context.getString(R.string.status_failed);
        String pendingStr = context.getString(R.string.status_pending);
        String rejectStr = context.getString(R.string.status_rejected);
        if (status.compareTo(receivedStr) == 0) {
            mIconView.setImageResource(R.drawable.ic_sms_mms_delivered);
        } else if (status.compareTo(failedStr) == 0) {
            mIconView.setImageResource(R.drawable.ic_sms_mms_not_delivered);
        } else if (status.compareTo(pendingStr) == 0) {
            mIconView.setImageResource(R.drawable.ic_sms_mms_pending);
        } else if (status.compareTo(rejectStr) == 0) {
            mIconView.setImageResource(R.drawable.ic_sms_mms_not_delivered);
        } else {
        }
    }
}
