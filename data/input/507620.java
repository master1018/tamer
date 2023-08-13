public class ContactsAsyncHelper extends Handler {
    private static final boolean DBG = false;
    private static final String LOG_TAG = "ContactsAsyncHelper";
    public interface OnImageLoadCompleteListener {
        public void onImageLoadComplete(int token, Object cookie, ImageView iView,
                boolean imagePresent);
    }
    private static final int EVENT_LOAD_IMAGE = 1;
    private static final int DEFAULT_TOKEN = -1;
    private static Handler sThreadHandler;
    private static ContactsAsyncHelper sInstance;
    static {
        sInstance = new ContactsAsyncHelper();
    }
    private static final class WorkerArgs {
        public Context context;
        public ImageView view;
        public Uri uri;
        public int defaultResource;
        public Object result;
        public Object cookie;
        public OnImageLoadCompleteListener listener;
        public CallerInfo info;
    }
    public static class ImageTracker {
        public static final int DISPLAY_UNDEFINED = 0;
        public static final int DISPLAY_IMAGE = -1;
        public static final int DISPLAY_DEFAULT = -2;
        private CallerInfo mCurrentCallerInfo;
        private int displayMode;
        public ImageTracker() {
            mCurrentCallerInfo = null;
            displayMode = DISPLAY_UNDEFINED;
        }
        public boolean isDifferentImageRequest(CallerInfo ci) {
            return (mCurrentCallerInfo != ci);
        }
        public boolean isDifferentImageRequest(Connection connection) {
            if (connection == null) {
                if (DBG) Log.d(LOG_TAG, "isDifferentImageRequest: connection is null");
                return (mCurrentCallerInfo != null);
            }
            Object o = connection.getUserData();
            boolean runQuery = true;
            if (o instanceof CallerInfo) {
                runQuery = isDifferentImageRequest((CallerInfo) o);
            }
            return runQuery;
        }
        public void setPhotoRequest(CallerInfo ci) {
            mCurrentCallerInfo = ci;
        }
        public Uri getPhotoUri() {
            if (mCurrentCallerInfo != null) {
                return ContentUris.withAppendedId(Contacts.CONTENT_URI,
                        mCurrentCallerInfo.person_id);
            }
            return null;
        }
        public void setPhotoState(int state) {
            displayMode = state;
        }
        public int getPhotoState() {
            return displayMode;
        }
    }
    private class WorkerHandler extends Handler {
        public WorkerHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            WorkerArgs args = (WorkerArgs) msg.obj;
            switch (msg.arg1) {
                case EVENT_LOAD_IMAGE:
                    InputStream inputStream = null;
                    try {
                        inputStream = Contacts.openContactPhotoInputStream(
                                args.context.getContentResolver(), args.uri);
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Error opening photo input stream", e);
                    }
                    if (inputStream != null) {
                        args.result = Drawable.createFromStream(inputStream, args.uri.toString());
                        if (DBG) Log.d(LOG_TAG, "Loading image: " + msg.arg1 +
                                " token: " + msg.what + " image URI: " + args.uri);
                    } else {
                        args.result = null;
                        if (DBG) Log.d(LOG_TAG, "Problem with image: " + msg.arg1 +
                                " token: " + msg.what + " image URI: " + args.uri +
                                ", using default image.");
                    }
                    break;
                default:
            }
            Message reply = ContactsAsyncHelper.this.obtainMessage(msg.what);
            reply.arg1 = msg.arg1;
            reply.obj = msg.obj;
            reply.sendToTarget();
        }
    }
    private ContactsAsyncHelper() {
        HandlerThread thread = new HandlerThread("ContactsAsyncWorker");
        thread.start();
        sThreadHandler = new WorkerHandler(thread.getLooper());
    }
    public static final void updateImageViewWithContactPhotoAsync(Context context,
            ImageView imageView, Uri person, int placeholderImageResource) {
        updateImageViewWithContactPhotoAsync (null, DEFAULT_TOKEN, null, null, context,
                imageView, person, placeholderImageResource);
    }
    public static final void updateImageViewWithContactPhotoAsync(CallerInfo info, Context context,
            ImageView imageView, Uri person, int placeholderImageResource) {
        updateImageViewWithContactPhotoAsync (info, DEFAULT_TOKEN, null, null, context,
                imageView, person, placeholderImageResource);
    }
    public static final void updateImageViewWithContactPhotoAsync(CallerInfo info, int token,
            OnImageLoadCompleteListener listener, Object cookie, Context context,
            ImageView imageView, Uri person, int placeholderImageResource) {
        if (person == null) {
            if (DBG) Log.d(LOG_TAG, "target image is null, just display placeholder.");
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageResource(placeholderImageResource);
            return;
        }
        WorkerArgs args = new WorkerArgs();
        args.cookie = cookie;
        args.context = context;
        args.view = imageView;
        args.uri = person;
        args.defaultResource = placeholderImageResource;
        args.listener = listener;
        args.info = info;
        Message msg = sThreadHandler.obtainMessage(token);
        msg.arg1 = EVENT_LOAD_IMAGE;
        msg.obj = args;
        if (DBG) Log.d(LOG_TAG, "Begin loading image: " + args.uri +
                ", displaying default image for now.");
        if (placeholderImageResource != -1) {
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageResource(placeholderImageResource);
        } else {
            imageView.setVisibility(View.INVISIBLE);
        }
        sThreadHandler.sendMessage(msg);
    }
    @Override
    public void handleMessage(Message msg) {
        WorkerArgs args = (WorkerArgs) msg.obj;
        switch (msg.arg1) {
            case EVENT_LOAD_IMAGE:
                boolean imagePresent = false;
                if (args.result != null) {
                    args.view.setVisibility(View.VISIBLE);
                    args.view.setImageDrawable((Drawable) args.result);
                    if (args.info != null) {
                        args.info.cachedPhoto = (Drawable) args.result;
                    }
                    imagePresent = true;
                } else if (args.defaultResource != -1) {
                    args.view.setVisibility(View.VISIBLE);
                    args.view.setImageResource(args.defaultResource);
                }
                if (args.info != null) {
                    args.info.isCachedPhotoCurrent = true;
                }
                if (args.listener != null) {
                    if (DBG) Log.d(LOG_TAG, "Notifying listener: " + args.listener.toString() +
                            " image: " + args.uri + " completed");
                    args.listener.onImageLoadComplete(msg.what, args.cookie, args.view,
                            imagePresent);
                }
                break;
            default:
        }
    }
}
