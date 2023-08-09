public class RssService extends Service implements Runnable{
    private Logger mLogger = Logger.getLogger(this.getPackageName());
    public static final String REQUERY_KEY = "Requery_All"; 
    public static final String RSS_URL = "RSS_URL"; 
    private NotificationManager mNM;
    private Cursor mCur;                        
    private GregorianCalendar mLastCheckedTime; 
    private final String LAST_CHECKED_PREFERENCE = "last_checked";
    static final int UPDATE_FREQUENCY_IN_MINUTES = 60;
    private Handler mHandler;           
    private final int NOTIFY_ID = 1;    
    @Override
    protected void onCreate(){
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Intent clickIntent = new Intent(Intent.ACTION_MAIN);
        clickIntent.setClassName(MyRssReader5.class.getName());
        Notification note = new Notification(this, R.drawable.rss_icon, "RSS Service",
                clickIntent, null);
        mNM.notify(NOTIFY_ID, note);
        mHandler = new Handler();
        Intent intent = new Intent(MyRssReader.class);
        ContentResolver rslv = getContentResolver();
        mCur = rslv.query(RssContentProvider.CONTENT_URI, null, null, null, null);
        SharedPreferences pref = getSharedPreferences("", 0);
        mLastCheckedTime = new GregorianCalendar();
        mLastCheckedTime.setTimeInMillis(pref.getLong(LAST_CHECKED_PREFERENCE, 0));
        Thread thr = new Thread(null, this, "rss_service_thread");
        thr.start();
        mLogger.info("RssService created");
    }
    @Override
    protected void onStart(Intent intent, int startId){
        super.onStart(startId, arguments);
        Bundle arguments = intent.getExtras();
        if(arguments != null) {
            if(arguments.containsKey(REQUERY_KEY)) {
                queryRssItems();
            }
            if(arguments.containsKey(RSS_URL)) {
                queryItem(arguments.getString(RSS_URL));
            }
        }    
    }
    @Override
    protected void onDestroy(){
      mNM.cancel(NOTIFY_ID);
    }
    public void queryIfPeriodicRefreshRequired() {
        GregorianCalendar nextCheckTime = new GregorianCalendar();
        nextCheckTime = (GregorianCalendar) mLastCheckedTime.clone();
        nextCheckTime.add(GregorianCalendar.MINUTE, UPDATE_FREQUENCY_IN_MINUTES);
        mLogger.info("last checked time:" + mLastCheckedTime.toString() + "  Next checked time: " + nextCheckTime.toString());
        if(mLastCheckedTime.before(nextCheckTime)) {
            queryRssItems();
        } else {
            long timeTillNextUpdate = mLastCheckedTime.getTimeInMillis() - GregorianCalendar.getInstance().getTimeInMillis();
            mHandler.postDelayed(this, 1000 * 60 * UPDATE_FREQUENCY_IN_MINUTES);
        }
    }
    void queryRssItems(){
        mLogger.info("Querying Rss feeds...");
        mCur.requery();
        while (mCur.next()){
             int urlColumnIndex = mCur.getColumnIndex(RssContentProvider.URL);
             String url = mCur.getString(urlColumnIndex);
             queryItem(url);
        }
        mLastCheckedTime.setTimeInMillis(System.currentTimeMillis());
        mHandler.postDelayed(this, 1000 * 60 * UPDATE_FREQUENCY_IN_MINUTES);
    }
    private boolean queryItem(String url) {
        try {
            URL wrappedUrl = new URL(url);
            String rssFeed = readRss(wrappedUrl);
            mLogger.info("RSS Feed " + url + ":\n " + rssFeed);
            if(TextUtils.isEmpty(rssFeed)) {
                return false;
            }
            GregorianCalendar feedPubDate = parseRssDocPubDate(rssFeed);
            GregorianCalendar lastUpdated = new GregorianCalendar();
            int lastUpdatedColumnIndex = mCur.getColumnIndex(RssContentProvider.LAST_UPDATED);
            lastUpdated.setTimeInMillis(mCur.getLong(lastUpdatedColumnIndex));
            if(lastUpdated.getTimeInMillis() == 0 ||
                lastUpdated.before(feedPubDate) && !TextUtils.isEmpty(rssFeed)) {
                int contentColumnIndex = mCur.getColumnIndex(RssContentProvider.CONTENT);
                int updatedColumnIndex = mCur.getColumnIndex(RssContentProvider.HAS_BEEN_READ);
                mCur.updateString(contentColumnIndex, rssFeed);
                mCur.updateLong(lastUpdatedColumnIndex, feedPubDate.getTimeInMillis());
                mCur.updateInt(updatedColumnIndex, 0);
                mCur.commitUpdates();
            }
        } catch (MalformedURLException ex) {
              mLogger.warning("Error in queryItem: Bad url");
              return false;
        }
        return true;
    }  
    private GregorianCalendar parseRssDocPubDate(String xml){
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeInMillis(0);
        String patt ="<[\\s]*pubDate[\\s]*>(.+?)</pubDate[\\s]*>";
        Pattern p = Pattern.compile(patt);
        Matcher m = p.matcher(xml);
        try {
            if(m.find()) {
                mLogger.info("pubDate: " + m.group());
                SimpleDateFormat pubDate = new SimpleDateFormat();
                cal.setTime(pubDate.parse(m.group(1)));
            }
       } catch(ParseException ex) {
            mLogger.warning("parseRssDocPubDate couldn't find a <pubDate> tag. Returning default value.");
       }
        return cal;
    }    
    String readRss(URL url){
      String html = "<html><body><h2>No data</h2></body></html>";
      try {
          mLogger.info("URL is:" + url.toString());
          BufferedReader inStream =
              new BufferedReader(new InputStreamReader(url.openStream()),
                      1024);
          String line;
          StringBuilder rssFeed = new StringBuilder();
          while ((line = inStream.readLine()) != null){
              rssFeed.append(line);
          }
          html = rssFeed.toString();
      } catch(IOException ex) {
          mLogger.warning("Couldn't open an RSS stream");
      }
      return html;
    }
    public void run() {
        queryIfPeriodicRefreshRequired();
    }
    @Override
    public IBinder onBind(Intent intent){
        return mBinder;
    }
    private final IBinder mBinder = new Binder()  {
        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) {
            return super.onTransact(code, data, reply, flags);
        }
    };
}
