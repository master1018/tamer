public class DeliveryReportAdapter extends ArrayAdapter<DeliveryReportItem> {
    static final String LOG_TAG = "DeliveryReportAdapter";
    public DeliveryReportAdapter(Context context, List<DeliveryReportItem> items) {
        super(context, R.layout.delivery_report_list_item, R.id.recipient, items);
    }
    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        DeliveryReportListItem listItem;
        DeliveryReportItem item = this.getItem(position);
        if (view == null) {
            LayoutInflater factory = LayoutInflater.from(getContext());
            listItem = (DeliveryReportListItem) factory.inflate(
                    R.layout.delivery_report_list_item, viewGroup, false);
        } else {
            if (view instanceof DeliveryReportListItem) {
                listItem = (DeliveryReportListItem) view;
            } else {
                return view;
            }
        }
        listItem.bind(item.recipient, item.status);
        return listItem;
    }
}
