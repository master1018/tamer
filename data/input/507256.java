public class URLSpan extends ClickableSpan implements ParcelableSpan {
    private final String mURL;
    public URLSpan(String url) {
        mURL = url;
    }
    public URLSpan(Parcel src) {
        mURL = src.readString();
    }
    public int getSpanTypeId() {
        return TextUtils.URL_SPAN;
    }
    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mURL);
    }
    public String getURL() {
        return mURL;
    }
    @Override
    public void onClick(View widget) {
        Uri uri = Uri.parse(getURL());
        Context context = widget.getContext();
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.putExtra(Browser.EXTRA_APPLICATION_ID, context.getPackageName());
        context.startActivity(intent);
    }
}
