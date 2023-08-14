public class DrmWrapper {
    private DrmRights mRight;
    private final DrmRawContent mDrmObject;
    private final Uri mDataUri;
    private final byte[] mData;
    private byte[] mDecryptedData;
    private static final String LOG_TAG = "DrmWrapper";
    private static final boolean DEBUG = false;
    private static final boolean LOCAL_LOGV = DEBUG ? Config.LOGD : Config.LOGV;
    public DrmWrapper(String drmContentType, Uri uri, byte[] drmData)
            throws DrmException, IOException {
        if ((drmContentType == null) || (drmData == null)) {
            throw new IllegalArgumentException(
                    "Content-Type or data may not be null.");
        }
        mDataUri = uri;
        mData = drmData;
        ByteArrayInputStream drmDataStream = new ByteArrayInputStream(drmData);
        mDrmObject = new DrmRawContent(drmDataStream, drmDataStream.available(),
                                       drmContentType);
        if (!isRightsInstalled()) {
            if (LOCAL_LOGV) {
                Log.v(LOG_TAG, "DRM rights not installed yet.");
            }
            installRights(drmData);
        }
    }
    private int getPermission() {
        String contentType = mDrmObject.getContentType();
        if (ContentType.isAudioType(contentType) ||
                ContentType.isVideoType(contentType)) {
            return DrmRights.DRM_PERMISSION_PLAY;
        }
        return DrmRights.DRM_PERMISSION_DISPLAY;
    }
    public byte[] getDecryptedData() throws IOException {
        if ((mDecryptedData == null) && (mRight != null)) {
            InputStream decryptedDataStream = mDrmObject.getContentInputStream(mRight);
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[256];
                int len;
                while ((len = decryptedDataStream.read(buffer)) > 0) {
                    baos.write(buffer, 0, len);
                }
                mDecryptedData = baos.toByteArray();
            } finally {
                try {
                    decryptedDataStream.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                }
            }
        }
        if (mDecryptedData != null) {
            byte[] decryptedData = new byte[mDecryptedData.length];
            System.arraycopy(mDecryptedData, 0, decryptedData, 0, mDecryptedData.length);
            return decryptedData;
        }
        return null;
    }
    public boolean consumeRights() {
        if (mRight == null) {
            return false;
        }
        return mRight.consumeRights(getPermission());
    }
    public void installRights(byte[] rightData) throws DrmException, IOException {
        if (rightData == null) {
            throw new DrmException("Right data may not be null.");
        }
        if (LOCAL_LOGV) {
            Log.v(LOG_TAG, "Installing DRM rights.");
        }
        ByteArrayInputStream rightDataStream = new ByteArrayInputStream(rightData);
        mRight = DrmRightsManager.getInstance().installRights(
                rightDataStream, rightData.length,
                DrmRawContent.DRM_MIMETYPE_MESSAGE_STRING);
    }
    public boolean isRightsInstalled() {
        if (mRight != null) {
            return true;
        }
        mRight = DrmRightsManager.getInstance().queryRights(mDrmObject);
        return mRight != null ? true : false;
    }
    public boolean isAllowedToForward() {
        if (DrmRawContent.DRM_SEPARATE_DELIVERY != mDrmObject.getRawType()) {
            return false;
        }
        return true;
    }
    public String getRightsAddress() {
        if (mDrmObject == null) {
            return null;
        }
        return mDrmObject.getRightsAddress();
    }
    public String getContentType() {
        return mDrmObject.getContentType();
    }
    public Uri getOriginalUri() {
        return mDataUri;
    }
    public byte[] getOriginalData() {
        return mData;
    }
}
