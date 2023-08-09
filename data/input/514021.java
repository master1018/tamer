public class ListTakeFocusFromSide extends ListActivity {
    private class ThrashListAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private String[] mTitles = new String[100];
        public ThrashListAdapter(Context context) {
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mTitles = new String[100];
            int i;
            for (i = 0; i < 100; i++) {
                mTitles[i] = "[" + i + "]";
            }
        }
        public int getCount() {
            return mTitles.length;
        }
        public Object getItem(int position) {
            return position;
        }
        public long getItemId(int position) {
            return position;
        }
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView view;
            if (convertView == null) {
                view = (TextView) mInflater.inflate(android.R.layout.simple_list_item_1, null);
            } else {
                view = (TextView) convertView;
            }
            view.setText(mTitles[position]);
            return view;
        }
    }
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.list_take_focus_from_side);
        setListAdapter(new ThrashListAdapter(this));
    }
}
