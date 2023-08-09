public class List8 extends ListActivity {
    PhotoAdapter mAdapter;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_8);
        getListView().setEmptyView(findViewById(R.id.empty));
        mAdapter = new PhotoAdapter(this);
        setListAdapter(mAdapter);
        Button clear = (Button) findViewById(R.id.clear);
        clear.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mAdapter.clearPhotos();
            } });
        Button add = (Button) findViewById(R.id.add);
        add.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mAdapter.addPhotos();
            } });
    }
    public class PhotoAdapter extends BaseAdapter {
        private Integer[] mPhotoPool = {
                R.drawable.sample_thumb_0, R.drawable.sample_thumb_1, R.drawable.sample_thumb_2,
                R.drawable.sample_thumb_3, R.drawable.sample_thumb_4, R.drawable.sample_thumb_5,
                R.drawable.sample_thumb_6, R.drawable.sample_thumb_7};
        private ArrayList<Integer> mPhotos = new ArrayList<Integer>();
        public PhotoAdapter(Context c) {
            mContext = c;
        }
        public int getCount() {
            return mPhotos.size();
        }
        public Object getItem(int position) {
            return position;
        }
        public long getItemId(int position) {
            return position;
        }
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView i = new ImageView(mContext);
            i.setImageResource(mPhotos.get(position));
            i.setAdjustViewBounds(true);
            i.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT));
            i.setBackgroundResource(R.drawable.picture_frame);
            return i;
        }
        private Context mContext;
        public void clearPhotos() {
            mPhotos.clear();
            notifyDataSetChanged();
        }
        public void addPhotos() {
            int whichPhoto = (int)Math.round(Math.random() * (mPhotoPool.length - 1));
            int newPhoto = mPhotoPool[whichPhoto];
            mPhotos.add(newPhoto);
            notifyDataSetChanged();
        }
    }
}
