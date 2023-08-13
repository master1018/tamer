public class Address implements Parcelable {
    private Locale mLocale;
    private String mFeatureName;
    private HashMap<Integer, String> mAddressLines;
    private int mMaxAddressLineIndex = -1;
    private String mAdminArea;
    private String mSubAdminArea;
    private String mLocality;
    private String mSubLocality;
    private String mThoroughfare;
    private String mSubThoroughfare;
    private String mPremises;
    private String mPostalCode;
    private String mCountryCode;
    private String mCountryName;
    private double mLatitude;
    private double mLongitude;
    private boolean mHasLatitude = false;
    private boolean mHasLongitude = false;
    private String mPhone;
    private String mUrl;
    private Bundle mExtras = null;
    public Address(Locale locale) {
        mLocale = locale;
    }
    public Locale getLocale() {
        return mLocale;
    }
    public int getMaxAddressLineIndex() {
        return mMaxAddressLineIndex;
    }
    public String getAddressLine(int index) {
        if (index < 0) {
            throw new IllegalArgumentException("index = " + index + " < 0");
        }
        return mAddressLines == null? null :  mAddressLines.get(index);
    }
    public void setAddressLine(int index, String line) {
        if (index < 0) {
            throw new IllegalArgumentException("index = " + index + " < 0");
        }
        if (mAddressLines == null) {
            mAddressLines = new HashMap<Integer, String>();
        }
        mAddressLines.put(index, line);
        if (line == null) {
            mMaxAddressLineIndex = -1;
            for (Integer i : mAddressLines.keySet()) {
                mMaxAddressLineIndex = Math.max(mMaxAddressLineIndex, i);
            }
        } else {
            mMaxAddressLineIndex = Math.max(mMaxAddressLineIndex, index);
        }
    }
    public String getFeatureName() {
        return mFeatureName;
    }
    public void setFeatureName(String featureName) {
        mFeatureName = featureName;
    }
    public String getAdminArea() {
        return mAdminArea;
    }
    public void setAdminArea(String adminArea) {
        this.mAdminArea = adminArea;
    }
    public String getSubAdminArea() {
        return mSubAdminArea;
    }
    public void setSubAdminArea(String subAdminArea) {
        this.mSubAdminArea = subAdminArea;
    }
    public String getLocality() {
        return mLocality;
    }
    public void setLocality(String locality) {
        mLocality = locality;
    }
    public String getSubLocality() {
        return mSubLocality;
    }
    public void setSubLocality(String sublocality) {
        mSubLocality = sublocality;
    }
    public String getThoroughfare() {
        return mThoroughfare;
    }
    public void setThoroughfare(String thoroughfare) {
        this.mThoroughfare = thoroughfare;
    }
    public String getSubThoroughfare() {
        return mSubThoroughfare;
    }
    public void setSubThoroughfare(String subthoroughfare) {
        this.mSubThoroughfare = subthoroughfare;
    }
    public String getPremises() {
        return mPremises;
    }
    public void setPremises(String premises) {
        mPremises = premises;
    }
    public String getPostalCode() {
        return mPostalCode;
    }
    public void setPostalCode(String postalCode) {
        mPostalCode = postalCode;
    }
    public String getCountryCode() {
        return mCountryCode;
    }
    public void setCountryCode(String countryCode) {
        mCountryCode = countryCode;
    }
    public String getCountryName() {
        return mCountryName;
    }
    public void setCountryName(String countryName) {
        mCountryName = countryName;
    }
    public boolean hasLatitude() {
        return mHasLatitude;
    }
    public double getLatitude() {
        if (mHasLatitude) {
            return mLatitude;
        } else {
            throw new IllegalStateException();
        }
    }
    public void setLatitude(double latitude) {
        mLatitude = latitude;
        mHasLatitude = true;
    }
    public void clearLatitude() {
        mHasLatitude = false;
    }
    public boolean hasLongitude() {
        return mHasLongitude;
    }
    public double getLongitude() {
        if (mHasLongitude) {
            return mLongitude;
        } else {
            throw new IllegalStateException();
        }
    }
    public void setLongitude(double longitude) {
        mLongitude = longitude;
        mHasLongitude = true;
    }
    public void clearLongitude() {
        mHasLongitude = false;
    }
    public String getPhone() {
        return mPhone;
    }
    public void setPhone(String phone) {
        mPhone = phone;
    }
    public String getUrl() {
        return mUrl;
    }
    public void setUrl(String Url) {
        mUrl = Url;
    }
    public Bundle getExtras() {
        return mExtras;
    }
    public void setExtras(Bundle extras) {
        mExtras = (extras == null) ? null : new Bundle(extras);
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Address[addressLines=[");
        for (int i = 0; i <= mMaxAddressLineIndex; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(i);
            sb.append(':');
            String line = mAddressLines.get(i);
            if (line == null) {
                sb.append("null");
            } else {
                sb.append('\"');
                sb.append(line);
                sb.append('\"');
            }
        }
        sb.append(']');
        sb.append(",feature=");
        sb.append(mFeatureName);
        sb.append(",admin=");
        sb.append(mAdminArea);
        sb.append(",sub-admin=");
        sb.append(mSubAdminArea);
        sb.append(",locality=");
        sb.append(mLocality);
        sb.append(",thoroughfare=");
        sb.append(mThoroughfare);
        sb.append(",postalCode=");
        sb.append(mPostalCode);
        sb.append(",countryCode=");
        sb.append(mCountryCode);
        sb.append(",countryName=");
        sb.append(mCountryName);
        sb.append(",hasLatitude=");
        sb.append(mHasLatitude);
        sb.append(",latitude=");
        sb.append(mLatitude);
        sb.append(",hasLongitude=");
        sb.append(mHasLongitude);
        sb.append(",longitude=");
        sb.append(mLongitude);
        sb.append(",phone=");
        sb.append(mPhone);
        sb.append(",url=");
        sb.append(mUrl);
        sb.append(",extras=");
        sb.append(mExtras);
        sb.append(']');
        return sb.toString();
    }
    public static final Parcelable.Creator<Address> CREATOR =
        new Parcelable.Creator<Address>() {
        public Address createFromParcel(Parcel in) {
            String language = in.readString();
            String country = in.readString();
            Locale locale = country.length() > 0 ?
                new Locale(language, country) :
                new Locale(language);
            Address a = new Address(locale);
            int N = in.readInt();
            if (N > 0) {
                a.mAddressLines = new HashMap<Integer, String>(N);
                for (int i = 0; i < N; i++) {
                    int index = in.readInt();
                    String line = in.readString();
                    a.mAddressLines.put(index, line);
                    a.mMaxAddressLineIndex =
                        Math.max(a.mMaxAddressLineIndex, index);
                }
            } else {
                a.mAddressLines = null;
                a.mMaxAddressLineIndex = -1;
            }
            a.mFeatureName = in.readString();
            a.mAdminArea = in.readString();
            a.mSubAdminArea = in.readString();
            a.mLocality = in.readString();
            a.mThoroughfare = in.readString();
            a.mPostalCode = in.readString();
            a.mCountryCode = in.readString();
            a.mCountryName = in.readString();
            a.mHasLatitude = in.readInt() == 0 ? false : true;
            if (a.mHasLatitude) {
                a.mLatitude = in.readDouble();
            }
            a.mHasLongitude = in.readInt() == 0 ? false : true;
            if (a.mHasLongitude) {
                a.mLongitude = in.readDouble();
            }
            a.mPhone = in.readString();
            a.mUrl = in.readString();
            a.mExtras = in.readBundle();
            return a;
        }
        public Address[] newArray(int size) {
            return new Address[size];
        }
    };
    public int describeContents() {
        return (mExtras != null) ? mExtras.describeContents() : 0;
    }
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(mLocale.getLanguage());
        parcel.writeString(mLocale.getCountry());
        if (mAddressLines == null) {
            parcel.writeInt(0);
        } else {
            Set<Map.Entry<Integer, String>> entries = mAddressLines.entrySet();
            parcel.writeInt(entries.size());
            for (Map.Entry<Integer, String> e : entries) {
                parcel.writeInt(e.getKey());
                parcel.writeString(e.getValue());
            }
        }
        parcel.writeString(mFeatureName);
        parcel.writeString(mAdminArea);
        parcel.writeString(mSubAdminArea);
        parcel.writeString(mLocality);
        parcel.writeString(mThoroughfare);
        parcel.writeString(mPostalCode);
        parcel.writeString(mCountryCode);
        parcel.writeString(mCountryName);
        parcel.writeInt(mHasLatitude ? 1 : 0);
        if (mHasLatitude) {
            parcel.writeDouble(mLatitude);
        }
        parcel.writeInt(mHasLongitude ? 1 : 0);
        if (mHasLongitude){
            parcel.writeDouble(mLongitude);
        }
        parcel.writeString(mPhone);
        parcel.writeString(mUrl);
        parcel.writeBundle(mExtras);
    }
}
