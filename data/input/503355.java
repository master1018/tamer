public class ListRecyclerProfiling extends Activity {
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.list_recycler_profiling);
        String values[] = new String[1000];
        for (int i = 0; i < 1000; i++) {
            values[i] = ((Integer) i).toString();
        }
        ListView listView = (ListView) findViewById(R.id.list);
        ViewDebug.startRecyclerTracing("SimpleList", listView);
        listView.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, values));
        ImageButton stopProfiling = (ImageButton) findViewById(R.id.pause);
        stopProfiling.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ViewDebug.stopRecyclerTracing();
            }
        });
    }
}
