public class RssReader extends ListActivity {
    private RSSListAdapter mAdapter;
    private EditText mUrlText;
    private TextView mStatusText;
    private Handler mHandler;
    private RSSWorker mWorker;
    public static final int SNIPPET_LENGTH = 90;
    public static final String STRINGS_KEY = "strings";
    public static final String SELECTION_KEY = "selection";
    public static final String URL_KEY = "url";
    public static final String STATUS_KEY = "status";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rss_layout);
        List<RssItem> items = new ArrayList<RssItem>();
        mAdapter = new RSSListAdapter(this, items);
        getListView().setAdapter(mAdapter);
        mUrlText = (EditText)findViewById(R.id.urltext);
        mStatusText = (TextView)findViewById(R.id.statustext);
        Button download = (Button)findViewById(R.id.download);
        download.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                doRSS(mUrlText.getText());
            }
        });
        mHandler = new Handler();
    }
    private class RSSListAdapter extends ArrayAdapter<RssItem> {
        private LayoutInflater mInflater;
        public RSSListAdapter(Context context, List<RssItem> objects) {
            super(context, 0, objects);
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TwoLineListItem view;
            if (convertView == null) {
                view = (TwoLineListItem) mInflater.inflate(android.R.layout.simple_list_item_2,
                        null);
            } else {
                view = (TwoLineListItem) convertView;
            }
            RssItem item = this.getItem(position);
            view.getText1().setText(item.getTitle());
            String descr = item.getDescription().toString();
            descr = removeTags(descr);
            view.getText2().setText(descr.substring(0, Math.min(descr.length(), SNIPPET_LENGTH)));
            return view;
        }
    }
    public String removeTags(String str) {
        str = str.replaceAll("<.*?>", " ");
        str = str.replaceAll("\\s+", " ");
        return str;
    }
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        RssItem item = mAdapter.getItem(position);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.getLink().toString()));
        startActivity(intent);
    }
    public void resetUI() {
        List<RssItem> items = new ArrayList<RssItem>();
        mAdapter = new RSSListAdapter(this, items);
        getListView().setAdapter(mAdapter);
        mStatusText.setText("");
        mUrlText.requestFocus();
    }
    public synchronized void setCurrentWorker(RSSWorker worker) {
        if (mWorker != null) mWorker.interrupt();
        mWorker = worker;
    }
    public synchronized boolean isCurrentWorker(RSSWorker worker) {
        return (mWorker == worker);
    }
    private void doRSS(CharSequence rssUrl) {
        RSSWorker worker = new RSSWorker(rssUrl);
        setCurrentWorker(worker);
        resetUI();
        mStatusText.setText("Downloading\u2026");
        worker.start();
    }
    private class ItemAdder implements Runnable {
        RssItem mItem;
        ItemAdder(RssItem item) {
            mItem = item;
        }
        public void run() {
            mAdapter.add(mItem);
        }
    }
    private class RSSWorker extends Thread {
        private CharSequence mUrl;
        public RSSWorker(CharSequence url) {
            mUrl = url;
        }
        @Override
        public void run() {
            String status = "";
            try {
                URL url = new URL(mUrl.toString());
                URLConnection connection = url.openConnection();
                connection.setConnectTimeout(10000);
                connection.connect();
                InputStream in = connection.getInputStream();
                parseRSS(in, mAdapter);
                status = "done";
            } catch (Exception e) {
                status = "failed:" + e.getMessage();
            }
            final String temp = status;
            if (isCurrentWorker(this)) {
                mHandler.post(new Runnable() {
                    public void run() {
                        mStatusText.setText(temp);
                    }
                });
            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, 0, 0, "Slashdot")
            .setOnMenuItemClickListener(new RSSMenu("http:
        menu.add(0, 0, 0, "Google News")
            .setOnMenuItemClickListener(new RSSMenu("http:
        menu.add(0, 0, 0, "News.com")
            .setOnMenuItemClickListener(new RSSMenu("http:
        menu.add(0, 0, 0, "Bad Url")
            .setOnMenuItemClickListener(new RSSMenu("http:
        menu.add(0, 0, 0, "Reset")
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                resetUI();
                return true;
            }
        });
        return true;
    }
    private class RSSMenu implements MenuItem.OnMenuItemClickListener {
        private CharSequence mUrl;
        RSSMenu(CharSequence url) {
            mUrl = url;
        }
        public boolean onMenuItemClick(MenuItem item) {
            mUrlText.setText(mUrl);
            mUrlText.requestFocus();
            return true;
        }
    }
    @SuppressWarnings("unchecked")
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        int count = mAdapter.getCount();
        ArrayList<CharSequence> strings = new ArrayList<CharSequence>();
        for (int i = 0; i < count; i++) {
            RssItem item = mAdapter.getItem(i);
            strings.add(item.getTitle());
            strings.add(item.getLink());
            strings.add(item.getDescription());
        }
        outState.putSerializable(STRINGS_KEY, strings);
        if (getListView().hasFocus()) {
            outState.putInt(SELECTION_KEY, Integer.valueOf(getListView().getSelectedItemPosition()));
        }
        outState.putString(URL_KEY, mUrlText.getText().toString());
        outState.putCharSequence(STATUS_KEY, mStatusText.getText());
    }
    @SuppressWarnings("unchecked")
    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        if (state == null) return;
        List<CharSequence> strings = (ArrayList<CharSequence>)state.getSerializable(STRINGS_KEY);
        List<RssItem> items = new ArrayList<RssItem>();
        for (int i = 0; i < strings.size(); i += 3) {
            items.add(new RssItem(strings.get(i), strings.get(i + 1), strings.get(i + 2)));
        }
        mAdapter = new RSSListAdapter(this, items);
        getListView().setAdapter(mAdapter);
        if (state.containsKey(SELECTION_KEY)) {
            getListView().requestFocus(View.FOCUS_FORWARD);
            getListView().setSelection(state.getInt(SELECTION_KEY));
        }
        mUrlText.setText(state.getCharSequence(URL_KEY));
        mStatusText.setText(state.getCharSequence(STATUS_KEY));
    }
    void parseRSS(InputStream in, RSSListAdapter adapter) throws IOException,
            XmlPullParserException {
        XmlPullParser xpp = Xml.newPullParser();
        xpp.setInput(in, null);  
        int eventType;
        String title = "";
        String link = "";
        String description = "";
        eventType = xpp.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                String tag = xpp.getName();
                if (tag.equals("item")) {
                    title = link = description = "";
                } else if (tag.equals("title")) {
                    xpp.next(); 
                    title = xpp.getText();
                } else if (tag.equals("link")) {
                    xpp.next();
                    link = xpp.getText();
                } else if (tag.equals("description")) {
                    xpp.next();
                    description = xpp.getText();
                }
            } else if (eventType == XmlPullParser.END_TAG) {
                String tag = xpp.getName();
                if (tag.equals("item")) {
                    RssItem item = new RssItem(title, link, description);
                    mHandler.post(new ItemAdder(item));
                }
            }
            eventType = xpp.next();
        }
    }
}
