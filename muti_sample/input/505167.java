public class ReverseGeocoderTask extends AsyncTask<Void, Void, String> {
    private static final String TAG = "ReverseGeocoder";
    public static interface Callback {
        public void onComplete(String location);
    }
    private Geocoder mGeocoder;
    private float mLat;
    private float mLng;
    private Callback mCallback;
    public ReverseGeocoderTask(Geocoder geocoder, float[] latlng,
            Callback callback) {
        mGeocoder = geocoder;
        mLat = latlng[0];
        mLng = latlng[1];
        mCallback = callback;
    }
    @Override
    protected String doInBackground(Void... params) {
        String value = MenuHelper.EMPTY_STRING;
        try {
            List<Address> address =
                    mGeocoder.getFromLocation(mLat, mLng, 1);
            StringBuilder sb = new StringBuilder();
            for (Address addr : address) {
                int index = addr.getMaxAddressLineIndex();
                sb.append(addr.getAddressLine(index));
            }
            value = sb.toString();
        } catch (IOException ex) {
            value = MenuHelper.EMPTY_STRING;
            Log.e(TAG, "Geocoder exception: ", ex);
        } catch (RuntimeException ex) {
            value = MenuHelper.EMPTY_STRING;
            Log.e(TAG, "Geocoder exception: ", ex);
        }
        return value;
    }
    @Override
    protected void onPostExecute(String location) {
        mCallback.onComplete(location);
    }
}
