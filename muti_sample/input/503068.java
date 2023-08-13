public final class MediaItem {
    public static final int MEDIA_TYPE_IMAGE = 0;
    public static final int MEDIA_TYPE_VIDEO = 1;
    public static final long MIN_VALID_DATE_IN_MS = 157680000000L;
    public static final long MIN_VALID_DATE_IN_SEC = 157680000L;
    public static final long MAX_VALID_DATE_IN_MS = 2049840000000L;
    public static final long MAX_VALID_DATE_IN_SEC = 2049840000L;
    public static final String ID = new String("id");
    public long mId;
    public String mGuid;
    public String mCaption;
    public String mEditUri;
    public String mContentUri;
    public String mThumbnailUri;
    public String mScreennailUri;
    public String mMicroThumbnailUri;
    public String mWeblink;
    public String mMimeType;
    private String mDisplayMimeType;
    private int mMediaType = -1;
    public String mRole;
    public String mDescription;
    public double mLatitude;
    public double mLongitude;
    public String mReverseGeocodedLocation;
    public long mDateTakenInMs = 0;
    public Date mLocaltime;
    public boolean mTriedRetrievingExifDateTaken = false;
    public long mDateModifiedInSec = 0;
    public long mDateAddedInSec = 0;
    public int mDurationInSec;
    public int mPrimingState = 0;
    public static final int NOT_PRIMED = 0;
    public static final int STARTED_PRIMING = 1;
    public static final int PRIMED = 2;
    public int mClusteringState = 0;
    public static final int NOT_CLUSTERED = 0;
    public static final int CLUSTERED = 1;
    public float mRotation;
    public long mThumbnailId;
    public int mThumbnailFocusX;
    public int mThumbnailFocusY;
    public String mFilePath;
    public MediaSet mParentMediaSet;
    public boolean mFlagForDelete;
    public MediaItem() {
        mCaption = "";
    }
    public boolean isWellFormed() {
        return true;
    }
    @Override
    public String toString() {
        return mCaption;
    }
    public boolean isLatLongValid() {
        return (mLatitude != 0.0 || mLongitude != 0.0);
    }
    public boolean isDateTakenValid() {
        return (mDateTakenInMs > MIN_VALID_DATE_IN_MS && mDateTakenInMs < MAX_VALID_DATE_IN_MS);
    }
    public boolean isDateModifiedValid() {
        return (mDateModifiedInSec > MIN_VALID_DATE_IN_SEC && mDateModifiedInSec < MAX_VALID_DATE_IN_SEC);
    }
    public boolean isDateAddedValid() {
        return (mDateAddedInSec > MIN_VALID_DATE_IN_SEC && mDateAddedInSec < MAX_VALID_DATE_IN_SEC);
    }
    public boolean isPicassaItem() {
        return (mParentMediaSet != null && mParentMediaSet.isPicassaAlbum());
    }
    private static final String VIDEO = "video/";
    public int getMediaType() {
        if (mMediaType == -1) {
            mMediaType = (mMimeType != null && mMimeType.startsWith(VIDEO)) ? MediaItem.MEDIA_TYPE_VIDEO
                    : MediaItem.MEDIA_TYPE_IMAGE;
        }
        return mMediaType;
    }
    public void setMediaType(int mediaType) {
        mMediaType = mediaType;
    }
    public String getDisplayMimeType() {
        if (mDisplayMimeType == null && mMimeType != null) {
            int slashPos = mMimeType.indexOf('/');
            if (slashPos != -1 && slashPos + 1 < mMimeType.length()) {
                mDisplayMimeType = mMimeType.substring(slashPos + 1).toUpperCase();
            } else {
                mDisplayMimeType = mMimeType.toUpperCase();
            }
        }
        return (mDisplayMimeType == null) ? "" : mDisplayMimeType;
    }
    public void setDisplayMimeType(final String displayMimeType) {
        mDisplayMimeType = displayMimeType;
    }
    public String getReverseGeocodedLocation(ReverseGeocoder reverseGeocoder) {
        if (mReverseGeocodedLocation != null) {
            return mReverseGeocodedLocation;
        }
        if (reverseGeocoder == null || !isLatLongValid()) {
            return null;
        }
        mReverseGeocodedLocation = reverseGeocoder.getReverseGeocodedLocation(mLatitude, mLongitude, 2);
        return mReverseGeocodedLocation;
    }
}
