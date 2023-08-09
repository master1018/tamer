public class GridInVertical extends Activity {
    Handler mHandler = new Handler();
    TextView mText;
    GridView mGridView;
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.grid_in_vertical);
        String values[] = new String[1000];
        int i=0;
        for(i=0; i<1000; i++) {
            values[i] = ((Integer)i).toString();
        }
        mText = (TextView) findViewById(R.id.text);
        mGridView = (GridView) findViewById(R.id.grid);
        mGridView.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, values));
    }
    public GridView getGridView() {
        return mGridView;
    }
}
