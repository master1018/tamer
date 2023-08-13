    public static class BadSurfaceTypeException extends RuntimeException {
        public BadSurfaceTypeException() {
        }
        public BadSurfaceTypeException(String name) {
            super(name);
        }
    }
    public interface Callback {
        public void surfaceCreated(SurfaceHolder holder);
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                int height);
        public void surfaceDestroyed(SurfaceHolder holder);
    }
    public void addCallback(Callback callback);
    public void removeCallback(Callback callback);
    public boolean isCreating();
    public void setType(int type);
    public void setFixedSize(int width, int height);
    public void setSizeFromLayout();
    public void setFormat(int format);
    public void setKeepScreenOn(boolean screenOn);
    public Canvas lockCanvas();
    public Canvas lockCanvas(Rect dirty);
    public void unlockCanvasAndPost(Canvas canvas);
    public Rect getSurfaceFrame();
    public Surface getSurface();
}
