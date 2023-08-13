public abstract class RenderScriptWallpaper<T extends RenderScriptScene> extends WallpaperService {
    public Engine onCreateEngine() {
        return new RenderScriptEngine();
    }
    protected abstract T createScene(int width, int height);
    private class RenderScriptEngine extends Engine {
        private RenderScriptGL mRs;
        private T mRenderer;
        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            setTouchEventsEnabled(true);
            surfaceHolder.setSizeFromLayout();
        }
        @Override
        public void onDestroy() {
            super.onDestroy();
            destroyRenderer();
        }
        private void destroyRenderer() {
            if (mRenderer != null) {
                mRenderer.stop();
                mRenderer = null;
            }
            if (mRs != null) {
                mRs.destroy();
                mRs = null;
            }
        }
        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            if (mRenderer != null) {
                if (visible) {
                    mRenderer.start();
                } else {
                    mRenderer.stop();
                }
            }
        }
        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            if (mRs != null) {
                mRs.contextSetSurface(width, height, holder.getSurface());
            }
            if (mRenderer == null) {
                mRenderer = createScene(width, height);
                mRenderer.init(mRs, getResources(), isPreview());
                mRenderer.start();
            } else {
                mRenderer.resize(width, height);
            }
        }
        @Override
        public void onTouchEvent(MotionEvent event) {
            mRenderer.onTouchEvent(event);
        }
        @Override
        public void onOffsetsChanged(float xOffset, float yOffset,
                float xStep, float yStep, int xPixels, int yPixels) {
            mRenderer.setOffset(xOffset, yOffset, xStep, yStep, xPixels, yPixels);
        }
        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
            Surface surface = null;
            while (surface == null) {
                surface = holder.getSurface();
            }
            mRs = new RenderScriptGL(false, false);
            mRs.contextSetPriority(RenderScript.Priority.LOW);
        }
        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            destroyRenderer();
        }
    }
}
