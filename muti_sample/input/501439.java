public class LayoutSelectorAdapter extends IconListAdapter {
    public LayoutSelectorAdapter(Context context) {
        super(context, getData(context));
    }
    protected static List<IconListItem> getData(Context context) {
        List<IconListItem> data = new ArrayList<IconListItem>(2);
         addItem(data, context.getString(R.string.select_top_text),
                R.drawable.ic_mms_text_top);
         addItem(data, context.getString(R.string.select_bottom_text),
                R.drawable.ic_mms_text_bottom);
        return data;
    }
    protected static void addItem(List<IconListItem> data, String title, int resource) {
        IconListItem temp = new IconListItem(title, resource);
        data.add(temp);
    }
}
