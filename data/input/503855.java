public class GridScrollListener extends Activity implements AbsListView.OnScrollListener {
    Handler mHandler = new Handler();
    TextView mText;
    GridView mGridView;
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.grid_scroll_listener);
        String values[] = new String[1000];
        int i=0;
        for(i=0; i<1000; i++) {
            values[i] = ((Integer)i).toString();
        }
        mText = (TextView) findViewById(R.id.text);
        mGridView = (GridView) findViewById(R.id.grid);
        mGridView.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, values));
        mGridView.setOnScrollListener(this);
    }
    public GridView getGridView() {
        return mGridView;
    }
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        int lastItem = firstVisibleItem + visibleItemCount - 1;
        mText.setText("Showing " + firstVisibleItem + "-" + lastItem + "/" + totalItemCount);
    }
    public void onScrollStateChanged(AbsListView view, int scrollState) {        
    }
}
