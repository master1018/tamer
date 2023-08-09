public class ListThrasher extends ListActivity implements AdapterView.OnItemSelectedListener {
    Handler mHandler = new Handler();
    ThrashListAdapter mAdapter;
    Random mRandomizer = new Random();
    TextView mText;
    Runnable mThrash = new Runnable() {
        public void run() {
            mAdapter.bumpVersion();
            mHandler.postDelayed(mThrash, 500);
        }
    };
    private class ThrashListAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private String[] mTitles = new String[100];
        private int[] mVersion = new int[100];
        public ThrashListAdapter(Context context) {
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mTitles = new String[100];
            mVersion = new int[100];
            int i;
            for (i = 0; i < 100; i++) {
                mTitles[i] = "[" + i + "]";
                mVersion[i] = 0;
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
            view.setText(mTitles[position] + " " + mVersion[position]);
            return view;
        }
        public void bumpVersion() {
            int position = mRandomizer.nextInt(getCount());
            mVersion[position]++;
            notifyDataSetChanged();
        }
    }
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.list_thrasher);
        mText = (TextView) findViewById(R.id.text);
        mAdapter = new ThrashListAdapter(this);
        setListAdapter(mAdapter);
        mHandler.postDelayed(mThrash, 5000);
        getListView().setOnItemSelectedListener(this);
    }
    public void onItemSelected(AdapterView parent, View v, int position, long id) {
        mText.setText("Position " + position);
    }
    public void onNothingSelected(AdapterView parent) {
        mText.setText("Nothing");
    }
}
