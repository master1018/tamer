public class StkMenuAdapter extends ArrayAdapter<Item> {
    private final LayoutInflater mInflater;
    private boolean mIcosSelfExplanatory = false;
    public StkMenuAdapter(Context context, List<Item> items,
            boolean icosSelfExplanatory) {
        super(context, 0, items);
        mInflater = LayoutInflater.from(context);
        mIcosSelfExplanatory = icosSelfExplanatory;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Item item = getItem(position);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.stk_menu_item, parent,
                    false);
        }
        if (!mIcosSelfExplanatory || (mIcosSelfExplanatory && item.icon == null)) {
            ((TextView) convertView.findViewById(R.id.text)).setText(item.text);
        }
        ImageView imageView = ((ImageView) convertView.findViewById(R.id.icon));
        if (item.icon == null) {
            imageView.setVisibility(View.GONE);
        } else {
            imageView.setImageBitmap(item.icon);
            imageView.setVisibility(View.VISIBLE);
        }
        return convertView;
    }
}
