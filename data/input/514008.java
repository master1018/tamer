public abstract class FontPicker extends ListActivity 
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
        List myData = new ArrayList<Bundle>(7);
        addItem(myData, "Sans",                 "sans-serif",   Typeface.NORMAL);
        addItem(myData, "Sans Bold",            "sans-serif",   Typeface.BOLD);
        addItem(myData, "Serif",                "serif",        Typeface.NORMAL);
        addItem(myData, "Serif Bold",           "serif",        Typeface.BOLD);
        addItem(myData, "Serif Italic",         "serif",        Typeface.ITALIC);
        addItem(myData, "Serif Bold Italic",    "serif",        Typeface.BOLD_ITALIC);
        addItem(myData, "Mono",                 "monospace",    Typeface.NORMAL);
        return myData;
    }
    protected void addItem(List<Bundle> data, String name, String fontName, int style)
    {
        Bundle temp = new Bundle();
        temp.putString("title", name);
        temp.putString("font", fontName);
        temp.putInt("style", style);
        data.add(temp);
    }
    protected void onListItemClick(ListView l, View v, int position, long id)
    {
        Bundle map = (Bundle) l.getItemAtPosition(position);
        setResult(RESULT_OK, (new Intent()).putExtras(map));
        finish();
    }
}
