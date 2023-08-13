class ImageGetter {
    @SuppressWarnings("unused")
    private static final String TAG = "ImageGetter";
    private Thread mGetterThread;
    private int mCurrentSerial;
    private int mCurrentPosition = -1;
    private ImageGetterCallback mCB;
    private IImageList mImageList;
    private GetterHandler mHandler;
    private volatile boolean mCancel = true;
    private boolean mIdle = false;
    private boolean mDone = false;
    private ContentResolver mCr;
    private class ImageGetterRunnable implements Runnable {
        private Runnable callback(final int position, final int offset,
                                  final boolean isThumb,
                                  final RotateBitmap bitmap,
                                  final int requestSerial) {
            return new Runnable() {
                public void run() {
                    if (requestSerial == mCurrentSerial) {
                        mCB.imageLoaded(position, offset, bitmap, isThumb);
                    } else if (bitmap != null) {
                        bitmap.recycle();
                    }
                }
            };
        }
        private Runnable completedCallback(final int requestSerial) {
            return new Runnable() {
                public void run() {
                    if (requestSerial == mCurrentSerial) {
                        mCB.completed();
                    }
                }
            };
        }
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            while (true) {
                synchronized (ImageGetter.this) {
                    while (mCancel || mDone || mCurrentPosition == -1) {
                        if (mDone) return;
                        mIdle = true;
                        ImageGetter.this.notify();
                        try {
                            ImageGetter.this.wait();
                        } catch (InterruptedException ex) {
                        }
                        mIdle = false;
                    }
                }
                executeRequest();
                synchronized (ImageGetter.this) {
                    mCurrentPosition = -1;
                }
            }
        }
        private void executeRequest() {
            int imageCount = mImageList.getCount();
            int [] order = mCB.loadOrder();
            for (int i = 0; i < order.length; i++) {
                if (mCancel) return;
                int offset = order[i];
                int imageNumber = mCurrentPosition + offset;
                if (imageNumber >= 0 && imageNumber < imageCount) {
                    if (!mCB.wantsThumbnail(mCurrentPosition, offset)) {
                        continue;
                    }
                    IImage image = mImageList.getImageAt(imageNumber);
                    if (image == null) continue;
                    if (mCancel) return;
                    Bitmap b = image.thumbBitmap(IImage.NO_ROTATE);
                    if (b == null) continue;
                    if (mCancel) {
                        b.recycle();
                        return;
                    }
                    Runnable cb = callback(mCurrentPosition, offset,
                            true,
                            new RotateBitmap(b, image.getDegreesRotated()),
                            mCurrentSerial);
                    mHandler.postGetterCallback(cb);
                }
            }
            for (int i = 0; i < order.length; i++) {
                if (mCancel) return;
                int offset = order[i];
                int imageNumber = mCurrentPosition + offset;
                if (imageNumber >= 0 && imageNumber < imageCount) {
                    if (!mCB.wantsFullImage(mCurrentPosition, offset)) {
                        continue;
                    }
                    IImage image = mImageList.getImageAt(imageNumber);
                    if (image == null) continue;
                    if (image instanceof VideoObject) continue;
                    if (mCancel) return;
                    int sizeToUse = mCB.fullImageSizeToUse(
                            mCurrentPosition, offset);
                    Bitmap b = image.fullSizeBitmap(sizeToUse, 3 * 1024 * 1024,
                            IImage.NO_ROTATE, IImage.USE_NATIVE);
                    if (b == null) continue;
                    if (mCancel) {
                        b.recycle();
                        return;
                    }
                    RotateBitmap rb = new RotateBitmap(b,
                            image.getDegreesRotated());
                    Runnable cb = callback(mCurrentPosition, offset,
                            false, rb, mCurrentSerial);
                    mHandler.postGetterCallback(cb);
                }
            }
            mHandler.postGetterCallback(completedCallback(mCurrentSerial));
        }
    }
    public ImageGetter(ContentResolver cr) {
        mCr = cr;
        mGetterThread = new Thread(new ImageGetterRunnable());
        mGetterThread.setName("ImageGettter");
        mGetterThread.start();
    }
    public synchronized void cancelCurrent() {
        Util.Assert(mGetterThread != null);
        mCancel = true;
        BitmapManager.instance().cancelThreadDecoding(mGetterThread, mCr);
    }
    private synchronized void cancelCurrentAndWait() {
        cancelCurrent();
        while (mIdle != true) {
            try {
                wait();
            } catch (InterruptedException ex) {
            }
        }
    }
    public void stop() {
        synchronized (this) {
            cancelCurrentAndWait();
            mDone = true;
            notify();
        }
        try {
            mGetterThread.join();
        } catch (InterruptedException ex) {
        }
        mGetterThread = null;
    }
    public synchronized void setPosition(int position, ImageGetterCallback cb,
            IImageList imageList, GetterHandler handler) {
        cancelCurrentAndWait();
        mCurrentPosition = position;
        mCB = cb;
        mImageList = imageList;
        mHandler = handler;
        mCurrentSerial += 1;
        mCancel = false;
        BitmapManager.instance().allowThreadDecoding(mGetterThread);
        notify();
    }
}
class GetterHandler extends Handler {
    private static final int IMAGE_GETTER_CALLBACK = 1;
    @Override
    public void handleMessage(Message message) {
        switch(message.what) {
            case IMAGE_GETTER_CALLBACK:
                ((Runnable) message.obj).run();
                break;
        }
    }
    public void postGetterCallback(Runnable callback) {
       postDelayedGetterCallback(callback, 0);
    }
    public void postDelayedGetterCallback(Runnable callback, long delay) {
        if (callback == null) {
            throw new NullPointerException();
        }
        Message message = Message.obtain();
        message.what = IMAGE_GETTER_CALLBACK;
        message.obj = callback;
        sendMessageDelayed(message, delay);
    }
    public void removeAllGetterCallbacks() {
        removeMessages(IMAGE_GETTER_CALLBACK);
    }
}
