public class DrmPushReceiver extends BroadcastReceiver {
    private static final String TAG = "DrmPushReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(WAP_PUSH_RECEIVED_ACTION)) {
            String rightMimeType = intent.getType();
            if (DrmRightsManager.DRM_MIMETYPE_RIGHTS_XML_STRING.equals(rightMimeType) ||
                DrmRightsManager.DRM_MIMETYPE_RIGHTS_WBXML_STRING.equals(rightMimeType)) {
                byte[] rightData = (byte[]) intent.getExtra("data");
                if (rightData == null) {
                    Log.e(TAG, "The rights data is invalid.");
                    return;
                }
                ByteArrayInputStream rightDataStream = new ByteArrayInputStream(rightData);
                try {
                    DrmRightsManager.getInstance().installRights(rightDataStream,
                            rightData.length,
                            rightMimeType);
                } catch (DrmException e) {
                    Log.e(TAG, "Install drm rights failed.");
                    return;
                } catch (IOException e) {
                    Log.e(TAG, "IOException occurs when install drm rights.");
                    return;
                }
                Log.d(TAG, "Install drm rights successfully.");
                return;
            }
            Log.d(TAG, "This is not drm rights push mimetype.");
        }
        Log.d(TAG, "This is not wap push received action.");
    }
}
