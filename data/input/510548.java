public class ClockPicker extends Activity implements
        AdapterView.OnItemSelectedListener, AdapterView.OnItemClickListener {
    private LayoutInflater mFactory;
    private Gallery mGallery;
    private SharedPreferences mPrefs;
    private View mClock;
    private ViewGroup mClockLayout;
    private int mPosition;
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mFactory = LayoutInflater.from(this);
        setContentView(R.layout.clockpicker);
        mGallery = (Gallery) findViewById(R.id.gallery);
        mGallery.setAdapter(new ClockAdapter());
        mGallery.setOnItemSelectedListener(this);
        mGallery.setOnItemClickListener(this);
        mPrefs = getSharedPreferences(AlarmClock.PREFERENCES, 0);
        int face = mPrefs.getInt(AlarmClock.PREF_CLOCK_FACE, 0);
        if (face < 0 || face >= AlarmClock.CLOCKS.length) face = 0;
        mClockLayout = (ViewGroup) findViewById(R.id.clock_layout);
        mClockLayout.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    selectClock(mPosition);
                }
        });
        mGallery.setSelection(face, false);
    }
    public void onItemSelected(AdapterView parent, View v, int position, long id) {
        if (mClock != null) {
            mClockLayout.removeView(mClock);
        }
        mClock = mFactory.inflate(AlarmClock.CLOCKS[position], null);
        mClockLayout.addView(mClock, 0);
        mPosition = position;
    }
    public void onItemClick(AdapterView parent, View v, int position, long id) {
        selectClock(position);
    }
    private synchronized void selectClock(int position) {
        SharedPreferences.Editor ed = mPrefs.edit();
        ed.putInt(AlarmClock.PREF_CLOCK_FACE, position);
        ed.commit();
        setResult(RESULT_OK);
        finish();
    }
    public void onNothingSelected(AdapterView parent) {
    }
    class ClockAdapter extends BaseAdapter {
        public ClockAdapter() {
        }
        public int getCount() {
            return AlarmClock.CLOCKS.length;
        }
        public Object getItem(int position) {
            return position;
        }
        public long getItemId(int position) {
            return position;
        }
        public View getView(final int position, View convertView, ViewGroup parent) {
            View clock = mFactory.inflate(AlarmClock.CLOCKS[position], null);
            return clock;
        }
    }
}
