public abstract class UrlRendererService extends Service {
    @SdkConstant(SdkConstantType.SERVICE_ACTION)
    public static final String SERVICE_INTERFACE =
            "android.service.urlrenderer.UrlRendererService";
    static final String TAG = "UrlRendererService";
    private static class InternalCallback implements UrlRenderer.Callback {
        private final IUrlRendererCallback mCallback;
        InternalCallback(IUrlRendererCallback cb) {
            mCallback = cb;
        }
        public void complete(String url, ParcelFileDescriptor result) {
            try {
                mCallback.complete(url, result);
            } catch (RemoteException ex) {
            }
        }
    }
    private final IUrlRendererService.Stub mBinderInterface =
            new IUrlRendererService.Stub() {
                public void render(List<String> urls, int width, int height,
                        IUrlRendererCallback cb) {
                    processRequest(urls, width, height,
                            new InternalCallback(cb));
                }
            };
    @Override
    public final android.os.IBinder onBind(android.content.Intent intent) {
        return mBinderInterface;
    }
    @Override
    public final boolean onUnbind(android.content.Intent intent) {
        stopSelf();
        return false;
    }
    public abstract void processRequest(List<String> urls, int width,
            int height, UrlRenderer.Callback cb);
}
