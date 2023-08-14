class AddNewBookmark extends LinearLayout {
    private TextView    mTextView;
    private TextView    mUrlText;
    private ImageView   mImageView;
    AddNewBookmark(Context context) {
        super(context);
        setWillNotDraw(false);
        LayoutInflater factory = LayoutInflater.from(context);
        factory.inflate(R.layout.add_new_bookmark, this);
        mTextView = (TextView) findViewById(R.id.title);
        mUrlText = (TextView) findViewById(R.id.url);
        mImageView = (ImageView) findViewById(R.id.favicon);
    }
     void setUrl(String url) {
        mUrlText.setText(url);
    }
}
