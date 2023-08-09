public class ZonePicker extends ListActivity {
    private ArrayAdapter<CharSequence> mFilterAdapter;
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mFilterAdapter = ArrayAdapter.createFromResource(this,
                R.array.timezone_filters, android.R.layout.simple_list_item_1);
        setListAdapter(mFilterAdapter);
    }
    protected void addItem(List<Map> data, String name, String zone) {
        HashMap temp = new HashMap();
        temp.put("title", name);
        temp.put("zone", zone);
        data.add(temp);
    }
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        String filter = (String) mFilterAdapter.getItem(position);
        if (filter.equals("All")) {
            filter = null;
        }
        Intent zoneList = new Intent();
        zoneList.setClass(this, ZoneList.class);
        zoneList.putExtra("filter", filter);
        startActivityForResult(zoneList, 0);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            finish();
        }
    }    
}
