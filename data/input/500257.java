public final class UrlRenderer {
    public interface Callback {
        public void complete(String url, ParcelFileDescriptor result);
    }
    private IUrlRendererService mService;
    public UrlRenderer(IBinder service) {
        mService = IUrlRendererService.Stub.asInterface(service);
    }
    private static class InternalCallback extends IUrlRendererCallback.Stub {
        private final Callback mCallback;
        InternalCallback(Callback cb) {
            mCallback = cb;
        }
        public void complete(String url, ParcelFileDescriptor result) {
            mCallback.complete(url, result);
        }
    }
    public void render(List<String> urls, int width, int height,
            Callback callback) {
        if (mService != null) {
            try {
                mService.render(urls, width, height,
                        new InternalCallback(callback));
            } catch (RemoteException ex) {
            }
        }
    }
}
