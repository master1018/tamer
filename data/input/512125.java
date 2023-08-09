public class Focus1 extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.focus_1);
        WebView webView = (WebView) findViewById(R.id.rssWebView);
        webView.loadData(
                        "<html><body>Can I focus?<br /><a href=\"#\">No I cannot!</a>.</body></html>",
                        "text/html", "utf-8");
        ListView listView = (ListView) findViewById(R.id.rssListView);
        listView.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, 
                new String[] {"Ars Technica", "Slashdot", "GameKult"}));
    }
}
