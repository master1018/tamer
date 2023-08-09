public class ListInHorizontal extends Activity {
    private ListView mListView;
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.list_in_horizontal);
        String values[] = new String[1000];
        for (int i = 0; i < 1000; i++) {
            values[i] = ((Integer) i).toString();
        }
        mListView = (ListView) findViewById(R.id.list);
        mListView.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, values));
    }
    public ListView getListView() {
        return mListView;
    }
}
