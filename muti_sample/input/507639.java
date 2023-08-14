public class LinearLayout9 extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.linear_layout_9);
        ListView list = (ListView) findViewById(R.id.list);
        list.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, AutoComplete1.COUNTRIES));
    }
}
