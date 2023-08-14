public class AddRssItem extends Activity {
    private OnClickListener mClickListener = new OnClickListener(){
        public void onClick(View v){
            if(v.getId() == R.id.submit){
                String title = ((TextView) findViewById(R.id.title_textbox)).getText().toString();
                String url = ((TextView) findViewById(R.id.url_textbox)).getText().toString();
                if(TextUtils.isEmpty(title) || TextUtils.isEmpty(url)){
                    showAlert("Missing Values", 
                              "You must specify both a title and a URL value", 
                              "OK", 
                              null, false, null);
                    return;
                }
                Intent res = new Intent("Accepted");
                res.putExtra(RssContentProvider.TITLE, title);
                res.putExtra(RssContentProvider.URL, url);
                res.putExtra(RssContentProvider.LAST_UPDATED, 0);
                res.putExtra(RssContentProvider.CONTENT, "<html><body><h2>Not updated yet.</h2></body></html>");
                setResult(RESULT_OK, res);
            }
            else
                setResult(RESULT_CANCELED, (new Intent()).setAction("Canceled" + v.getId()));
            finish();
        }
    };
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_item);
        setTitle(getString(R.string.add_item_label));
        Button btn = (Button) findViewById(R.id.cancel);
        btn.setOnClickListener(mClickListener);
        btn = (Button) findViewById(R.id.submit);
        btn.setOnClickListener(mClickListener);       
    }
}
