public class GridPadding extends Activity {
    private GridView mGridView;
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.grid_padding);
        String values[] = new String[1000];
        for(int i = 0; i < 1000; i++) {
            values[i] = String.valueOf(i);
        }
        mGridView = (GridView) findViewById(R.id.grid);
        mGridView.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, values));
    }
    public GridView getGridView() {
        return mGridView;
    }
}
