public abstract class BackgroundPicker extends ListActivity 
{
    public void onCreate(Bundle icicle) 
    {
        super.onCreate(icicle);
        setListAdapter(new SimpleAdapter(this,
                getData(),
                android.R.layout.simple_list_item_1,
                new String[] {"title"},
                new int[] {android.R.id.text1}));
    }
    protected List getData()
    {
        List myData = new ArrayList<Bundle>();
        addItem(myData, "Solid White", 0, 0xFFFFFFFF, 0xFF000000);
        addItem(myData, "Solid Light Gray", 0, 0xFFBFBFBF, 0xFF000000);
        addItem(myData, "Solid Dark Gray", 0, 0xFF404040, 0xFFFFFFFF);
        addItem(myData, "Solid Black", 0, 0xFF000000, 0xFFFFFFFF);
        addItem(myData, "Solid Blue", 0, 0xFF1a387a, 0xFFFFFFFF);
        addItem(myData, "Textured White", 0, 0, 0xFF000000);
        return myData;
    }
    protected void addItem(List<Bundle> data, String name, int textureRes, int bgColor, int textColor)
    {
        Bundle temp = new Bundle();
        temp.putString("title", name);
        if (textureRes != 0) {
            temp.putInt("texture", textureRes);
        }
        temp.putInt("bgcolor", bgColor);
        temp.putInt("text", textColor);
        data.add(temp);
    }
    protected void onListItemClick(ListView l, View v, int position, long id)
    {
        Bundle map = (Bundle) l.getItemAtPosition(position);
        setResult(RESULT_OK, (new Intent()).putExtras(map));
        finish();
    }
}
