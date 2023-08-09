public final class GestureLibraries {
    private GestureLibraries() {
    }
    public static GestureLibrary fromFile(String path) {
        return fromFile(new File(path));
    }
    public static GestureLibrary fromFile(File path) {
        return new FileGestureLibrary(path);
    }
    public static GestureLibrary fromPrivateFile(Context context, String name) {
        return fromFile(context.getFileStreamPath(name));
    }
    public static GestureLibrary fromRawResource(Context context, int resourceId) {
        return new ResourceGestureLibrary(context, resourceId);
    }
    private static class FileGestureLibrary extends GestureLibrary {
        private final File mPath;
        public FileGestureLibrary(File path) {
            mPath = path;
        }
        @Override
        public boolean isReadOnly() {
            return !mPath.canWrite();
        }
        public boolean save() {
            if (!mStore.hasChanged()) return true;
            final File file = mPath;
            final File parentFile = file.getParentFile();
            if (!parentFile.exists()) {
                if (!parentFile.mkdirs()) {
                    return false;
                }
            }
            boolean result = false;
            try {
                file.createNewFile();
                mStore.save(new FileOutputStream(file), true);
                result = true;
            } catch (FileNotFoundException e) {
                Log.d(LOG_TAG, "Could not save the gesture library in " + mPath, e);
            } catch (IOException e) {
                Log.d(LOG_TAG, "Could not save the gesture library in " + mPath, e);
            }
            return result;
        }
        public boolean load() {
            boolean result = false;
            final File file = mPath;
            if (file.exists() && file.canRead()) {
                try {
                    mStore.load(new FileInputStream(file), true);
                    result = true;
                } catch (FileNotFoundException e) {
                    Log.d(LOG_TAG, "Could not load the gesture library from " + mPath, e);
                } catch (IOException e) {
                    Log.d(LOG_TAG, "Could not load the gesture library from " + mPath, e);
                }
            }
            return result;
        }
    }
    private static class ResourceGestureLibrary extends GestureLibrary {
        private final WeakReference<Context> mContext;
        private final int mResourceId;
        public ResourceGestureLibrary(Context context, int resourceId) {
            mContext = new WeakReference<Context>(context);
            mResourceId = resourceId;
        }
        @Override
        public boolean isReadOnly() {
            return true;
        }
        public boolean save() {
            return false;
        }
        public boolean load() {
            boolean result = false;
            final Context context = mContext.get();
            if (context != null) {
                final InputStream in = context.getResources().openRawResource(mResourceId);
                try {
                    mStore.load(in, true);
                    result = true;
                } catch (IOException e) {
                    Log.d(LOG_TAG, "Could not load the gesture library from raw resource " +
                            context.getResources().getResourceName(mResourceId), e);
                }
            }
            return result;
        }
    }
}
