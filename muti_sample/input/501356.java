class BookmarkItem extends LinearLayout {
    protected TextView    mTextView;
    protected TextView    mUrlText;
    protected ImageView   mImageView;
    protected String      mUrl;
    protected String      mTitle;
    BookmarkItem(Context context) {
        super(context);
        LayoutInflater factory = LayoutInflater.from(context);
        factory.inflate(R.layout.history_item, this);
        mTextView = (TextView) findViewById(R.id.title);
        mUrlText = (TextView) findViewById(R.id.url);
        mImageView = (ImageView) findViewById(R.id.favicon);
        View star = findViewById(R.id.star);
        star.setVisibility(View.GONE);
    }
     void copyTo(BookmarkItem item) {
        item.mTextView.setText(mTextView.getText());
        item.mUrlText.setText(mUrlText.getText());
        item.mImageView.setImageDrawable(mImageView.getDrawable());
    }
     String getName() {
        return mTitle;
    }
     TextView getNameTextView() {
        return mTextView;
    }
     String getUrl() {
        return mUrl;
    }
     void setFavicon(Bitmap b) {
        if (b != null) {
            mImageView.setImageBitmap(b);
        } else {
            mImageView.setImageResource(R.drawable.app_web_browser_sm);
        }
    }
     void setName(String name) {
        if (name == null) {
            return;
        }
        mTitle = name;
        if (name.length() > BrowserSettings.MAX_TEXTVIEW_LEN) {
            name = name.substring(0, BrowserSettings.MAX_TEXTVIEW_LEN);
        }
        mTextView.setText(name);
    }
     void setUrl(String url) {
        if (url == null) {
            return;
        }
        mUrl = url;
        if (url.length() > BrowserSettings.MAX_TEXTVIEW_LEN) {
            url = url.substring(0, BrowserSettings.MAX_TEXTVIEW_LEN);
        }
        mUrlText.setText(url);
    }
}
