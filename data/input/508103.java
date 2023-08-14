public class GalleryStubActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery_test);
        Gallery gallery = (Gallery) findViewById(R.id.gallery_test);
        ImageAdapter adapter = new ImageAdapter(this);
        gallery.setAdapter(adapter);
    }
    private static class ImageAdapter extends BaseAdapter {
        public ImageAdapter(Context c) {
            mContext = c;
        }
        public int getCount() {
            return mImageIds.length;
        }
        public Object getItem(int position) {
            return position;
        }
        public long getItemId(int position) {
            return position;
        }
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView i = new ImageView(mContext);
            i.setImageResource(mImageIds[position]);
            i.setScaleType(ImageView.ScaleType.FIT_XY);
            i.setLayoutParams(new Gallery.LayoutParams(136, 88));
            return i;
        }
        private Context mContext;
        private Integer[] mImageIds = {
                R.drawable.faces,
                R.drawable.scenery,
                R.drawable.testimage,
                R.drawable.faces,
                R.drawable.scenery,
                R.drawable.testimage,
                R.drawable.faces,
                R.drawable.scenery,
                R.drawable.testimage,
        };
    }
}
