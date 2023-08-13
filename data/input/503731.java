public class IconListAdapter extends ArrayAdapter<IconListAdapter.IconListItem> {
    protected LayoutInflater mInflater;
    private static final int mResource = R.layout.icon_list_item;
    public IconListAdapter(Context context,
            List<IconListItem> items) {
        super(context, mResource, items);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView text;
        ImageView image;
        View view;
        if (convertView == null) {
            view = mInflater.inflate(mResource, parent, false);
        } else {
            view = convertView;
        }
        text = (TextView) view.findViewById(R.id.text1);
        text.setText(getItem(position).getTitle());
        image = (ImageView) view.findViewById(R.id.icon);
        image.setImageResource(getItem(position).getResource());
        return view;
    }
    public static class IconListItem {
        private final String mTitle;
        private final int mResource;
        public IconListItem(String title, int resource) {
            mResource = resource;
            mTitle = title;
        }
        public String getTitle() {
            return mTitle;
        }
        public int getResource() {
            return mResource;
        }
    }
}
