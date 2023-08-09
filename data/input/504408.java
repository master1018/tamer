public class Grid2 extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grid_2);
        GridView g = (GridView) findViewById(R.id.myGrid);
        g.setAdapter(new ImageAdapter(this));
    }
    public class ImageAdapter extends BaseAdapter {
        public ImageAdapter(Context c) {
            mContext = c;
        }
        public int getCount() {
            return mThumbIds.length;
        }
        public Object getItem(int position) {
            return position;
        }
        public long getItemId(int position) {
            return position;
        }
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(45, 45));
                imageView.setAdjustViewBounds(false);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(8, 8, 8, 8);
            } else {
                imageView = (ImageView) convertView;
            }
            imageView.setImageResource(mThumbIds[position]);
            return imageView;
        }
        private Context mContext;
        private Integer[] mThumbIds = {
                R.drawable.sample_thumb_0, R.drawable.sample_thumb_1,
                R.drawable.sample_thumb_2, R.drawable.sample_thumb_3,
                R.drawable.sample_thumb_4, R.drawable.sample_thumb_5,
                R.drawable.sample_thumb_6, R.drawable.sample_thumb_7,
                R.drawable.sample_thumb_0, R.drawable.sample_thumb_1,
                R.drawable.sample_thumb_2, R.drawable.sample_thumb_3,
                R.drawable.sample_thumb_4, R.drawable.sample_thumb_5,
                R.drawable.sample_thumb_6, R.drawable.sample_thumb_7,
                R.drawable.sample_thumb_0, R.drawable.sample_thumb_1,
                R.drawable.sample_thumb_2, R.drawable.sample_thumb_3,
                R.drawable.sample_thumb_4, R.drawable.sample_thumb_5,
                R.drawable.sample_thumb_6, R.drawable.sample_thumb_7,
                R.drawable.sample_thumb_0, R.drawable.sample_thumb_1,
                R.drawable.sample_thumb_2, R.drawable.sample_thumb_3,
                R.drawable.sample_thumb_4, R.drawable.sample_thumb_5,
                R.drawable.sample_thumb_6, R.drawable.sample_thumb_7,
                R.drawable.sample_thumb_0, R.drawable.sample_thumb_1,
                R.drawable.sample_thumb_2, R.drawable.sample_thumb_3,
                R.drawable.sample_thumb_4, R.drawable.sample_thumb_5,
                R.drawable.sample_thumb_6, R.drawable.sample_thumb_7,
                R.drawable.sample_thumb_0, R.drawable.sample_thumb_1,
                R.drawable.sample_thumb_2, R.drawable.sample_thumb_3,
                R.drawable.sample_thumb_4, R.drawable.sample_thumb_5,
                R.drawable.sample_thumb_6, R.drawable.sample_thumb_7,
                R.drawable.sample_thumb_0, R.drawable.sample_thumb_1,
                R.drawable.sample_thumb_2, R.drawable.sample_thumb_3,
                R.drawable.sample_thumb_4, R.drawable.sample_thumb_5,
                R.drawable.sample_thumb_6, R.drawable.sample_thumb_7,
                R.drawable.sample_thumb_0, R.drawable.sample_thumb_1,
                R.drawable.sample_thumb_2, R.drawable.sample_thumb_3,
                R.drawable.sample_thumb_4, R.drawable.sample_thumb_5,
                R.drawable.sample_thumb_6, R.drawable.sample_thumb_7,
                R.drawable.sample_thumb_0, R.drawable.sample_thumb_1,
                R.drawable.sample_thumb_2, R.drawable.sample_thumb_3,
                R.drawable.sample_thumb_4, R.drawable.sample_thumb_5,
                R.drawable.sample_thumb_6, R.drawable.sample_thumb_7,
                R.drawable.sample_thumb_0, R.drawable.sample_thumb_1,
                R.drawable.sample_thumb_2, R.drawable.sample_thumb_3,
                R.drawable.sample_thumb_4, R.drawable.sample_thumb_5,
                R.drawable.sample_thumb_6, R.drawable.sample_thumb_7,
                R.drawable.sample_thumb_0, R.drawable.sample_thumb_1,
                R.drawable.sample_thumb_2, R.drawable.sample_thumb_3,
                R.drawable.sample_thumb_4, R.drawable.sample_thumb_5,
                R.drawable.sample_thumb_6, R.drawable.sample_thumb_7,
                R.drawable.sample_thumb_0, R.drawable.sample_thumb_1,
                R.drawable.sample_thumb_2, R.drawable.sample_thumb_3,
                R.drawable.sample_thumb_4, R.drawable.sample_thumb_5,
                R.drawable.sample_thumb_6, R.drawable.sample_thumb_7,
                R.drawable.sample_thumb_0, R.drawable.sample_thumb_1,
                R.drawable.sample_thumb_2, R.drawable.sample_thumb_3,
                R.drawable.sample_thumb_4, R.drawable.sample_thumb_5,
                R.drawable.sample_thumb_6, R.drawable.sample_thumb_7,
                R.drawable.sample_thumb_0, R.drawable.sample_thumb_1,
                R.drawable.sample_thumb_2, R.drawable.sample_thumb_3,
                R.drawable.sample_thumb_4, R.drawable.sample_thumb_5,
                R.drawable.sample_thumb_6, R.drawable.sample_thumb_7,
                R.drawable.sample_thumb_0, R.drawable.sample_thumb_1,
                R.drawable.sample_thumb_2, R.drawable.sample_thumb_3,
                R.drawable.sample_thumb_4, R.drawable.sample_thumb_5,
                R.drawable.sample_thumb_6, R.drawable.sample_thumb_7,
                R.drawable.sample_thumb_0, R.drawable.sample_thumb_1,
                R.drawable.sample_thumb_2, R.drawable.sample_thumb_3,
                R.drawable.sample_thumb_4, R.drawable.sample_thumb_5,
                R.drawable.sample_thumb_6, R.drawable.sample_thumb_7,
                R.drawable.sample_thumb_0, R.drawable.sample_thumb_1,
                R.drawable.sample_thumb_2, R.drawable.sample_thumb_3,
                R.drawable.sample_thumb_4, R.drawable.sample_thumb_5,
                R.drawable.sample_thumb_6, R.drawable.sample_thumb_7,
                R.drawable.sample_thumb_0, R.drawable.sample_thumb_1,
                R.drawable.sample_thumb_2, R.drawable.sample_thumb_3,
                R.drawable.sample_thumb_4, R.drawable.sample_thumb_5,
                R.drawable.sample_thumb_6, R.drawable.sample_thumb_7,
                R.drawable.sample_thumb_0, R.drawable.sample_thumb_1,
                R.drawable.sample_thumb_2, R.drawable.sample_thumb_3,
                R.drawable.sample_thumb_4, R.drawable.sample_thumb_5,
                R.drawable.sample_thumb_6, R.drawable.sample_thumb_7,
                R.drawable.sample_thumb_0, R.drawable.sample_thumb_1,
                R.drawable.sample_thumb_2, R.drawable.sample_thumb_3,
                R.drawable.sample_thumb_4, R.drawable.sample_thumb_5,
                R.drawable.sample_thumb_6, R.drawable.sample_thumb_7,
        };
    }
}
