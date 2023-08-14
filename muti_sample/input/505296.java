 class HistoryItem extends BookmarkItem {
    private CompoundButton  mStar;      
    private CompoundButton.OnCheckedChangeListener  mListener;
     HistoryItem(Context context) {
        super(context);
        mStar = (CompoundButton) findViewById(R.id.star);
        mStar.setVisibility(View.VISIBLE);
        mListener = new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView,
                    boolean isChecked) {
                if (isChecked) {
                    Bookmarks.addBookmark(mContext,
                            mContext.getContentResolver(), mUrl, getName(), null, true);
                    LogTag.logBookmarkAdded(mUrl, "history");
                } else {
                    Bookmarks.removeFromBookmarks(mContext,
                            mContext.getContentResolver(), mUrl, getName());
                }
            }
        };
    }
     void copyTo(HistoryItem item) {
        item.mTextView.setText(mTextView.getText());
        item.mUrlText.setText(mUrlText.getText());
        item.setIsBookmark(mStar.isChecked());
        item.mImageView.setImageDrawable(mImageView.getDrawable());
    }
     boolean isBookmark() {
        return mStar.isChecked();
    }
     void setIsBookmark(boolean isBookmark) {
        mStar.setOnCheckedChangeListener(null);
        mStar.setChecked(isBookmark);
        mStar.setOnCheckedChangeListener(mListener);
    }
}
