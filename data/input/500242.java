public final class Bridge implements ILayoutBridge {
    private static final int DEFAULT_TITLE_BAR_HEIGHT = 25;
    private static final int DEFAULT_STATUS_BAR_HEIGHT = 25;
    public static class StaticMethodNotImplementedException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        public StaticMethodNotImplementedException(String msg) {
            super(msg);
        }
    }
    private final static Map<Integer, String[]> sRMap = new HashMap<Integer, String[]>();
    private final static Map<int[], String> sRArrayMap = new HashMap<int[], String>();
    private final static Map<String, Map<String, Integer>> sRFullMap =
        new HashMap<String, Map<String,Integer>>();
    private final static Map<Object, Map<String, SoftReference<Bitmap>>> sProjectBitmapCache =
        new HashMap<Object, Map<String, SoftReference<Bitmap>>>();
    private final static Map<Object, Map<String, SoftReference<NinePatch>>> sProject9PatchCache =
        new HashMap<Object, Map<String, SoftReference<NinePatch>>>();
    private final static Map<String, SoftReference<Bitmap>> sFrameworkBitmapCache =
        new HashMap<String, SoftReference<Bitmap>>();
    private final static Map<String, SoftReference<NinePatch>> sFramework9PatchCache =
        new HashMap<String, SoftReference<NinePatch>>();
    private static Map<String, Map<String, Integer>> sEnumValueMap;
    private final static ILayoutLog sDefaultLogger = new ILayoutLog() {
        public void error(String message) {
            System.err.println(message);
        }
        public void error(Throwable t) {
            String message = t.getMessage();
            if (message == null) {
                message = t.getClass().getName();
            }
            System.err.println(message);
        }
        public void warning(String message) {
            System.out.println(message);
        }
    };
    private static ILayoutLog sLogger = sDefaultLogger;
    public int getApiLevel() {
        return API_CURRENT;
    }
    public boolean init(
            String fontOsLocation, Map<String, Map<String, Integer>> enumValueMap) {
        return sinit(fontOsLocation, enumValueMap);
    }
    private static synchronized boolean sinit(String fontOsLocation,
            Map<String, Map<String, Integer>> enumValueMap) {
        final String debug = System.getenv("DEBUG_LAYOUT");
        if (debug != null && !debug.equals("0") && !debug.equals("false")) {
            OverrideMethod.setDefaultListener(new MethodAdapter() {
                @Override
                public void onInvokeV(String signature, boolean isNative, Object caller) {
                    if (sLogger != null) {
                        synchronized (sDefaultLogger) {
                            sLogger.error("Missing Stub: " + signature +
                                    (isNative ? " (native)" : ""));
                        }
                    }
                    if (debug.equalsIgnoreCase("throw")) {
                        throw new StaticMethodNotImplementedException(signature);
                    }
                }
            });
        }
        OverrideMethod.setMethodListener("android.view.View#isInEditMode()Z",
            new MethodAdapter() {
                @Override
                public int onInvokeI(String signature, boolean isNative, Object caller) {
                    return 1;
                }
            }
        );
        FontLoader fontLoader = FontLoader.create(fontOsLocation);
        if (fontLoader != null) {
            Typeface.init(fontLoader);
        } else {
            return false;
        }
        sEnumValueMap = enumValueMap;
        try {
            Class<?> r = com.android.internal.R.class;
            for (Class<?> inner : r.getDeclaredClasses()) {
                String resType = inner.getSimpleName();
                Map<String, Integer> fullMap = new HashMap<String, Integer>();
                sRFullMap.put(resType, fullMap);
                for (Field f : inner.getDeclaredFields()) {
                    int modifiers = f.getModifiers();
                    if (Modifier.isStatic(modifiers)) {
                        Class<?> type = f.getType();
                        if (type.isArray() && type.getComponentType() == int.class) {
                            sRArrayMap.put((int[]) f.get(null), f.getName());
                        } else if (type == int.class) {
                            Integer value = (Integer) f.get(null);
                            sRMap.put(value, new String[] { f.getName(), resType });
                            fullMap.put(f.getName(), value);
                        } else {
                            assert false;
                        }
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return false;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    @Deprecated
    public ILayoutResult computeLayout(IXmlPullParser layoutDescription,
            Object projectKey,
            int screenWidth, int screenHeight, String themeName,
            Map<String, Map<String, IResourceValue>> projectResources,
            Map<String, Map<String, IResourceValue>> frameworkResources,
            IProjectCallback customViewLoader, ILayoutLog logger) {
        boolean isProjectTheme = false;
        if (themeName.charAt(0) == '*') {
            themeName = themeName.substring(1);
            isProjectTheme = true;
        }
        return computeLayout(layoutDescription, projectKey,
                screenWidth, screenHeight, DisplayMetrics.DENSITY_DEFAULT,
                DisplayMetrics.DENSITY_DEFAULT, DisplayMetrics.DENSITY_DEFAULT,
                themeName, isProjectTheme,
                projectResources, frameworkResources, customViewLoader, logger);
    }
    @Deprecated
    public ILayoutResult computeLayout(IXmlPullParser layoutDescription, Object projectKey,
            int screenWidth, int screenHeight, String themeName, boolean isProjectTheme,
            Map<String, Map<String, IResourceValue>> projectResources,
            Map<String, Map<String, IResourceValue>> frameworkResources,
            IProjectCallback customViewLoader, ILayoutLog logger) {
        return computeLayout(layoutDescription, projectKey,
                screenWidth, screenHeight, DisplayMetrics.DENSITY_DEFAULT,
                DisplayMetrics.DENSITY_DEFAULT, DisplayMetrics.DENSITY_DEFAULT,
                themeName, isProjectTheme,
                projectResources, frameworkResources, customViewLoader, logger);
    }
    public ILayoutResult computeLayout(IXmlPullParser layoutDescription, Object projectKey,
            int screenWidth, int screenHeight, int density, float xdpi, float ydpi,
            String themeName, boolean isProjectTheme,
            Map<String, Map<String, IResourceValue>> projectResources,
            Map<String, Map<String, IResourceValue>> frameworkResources,
            IProjectCallback customViewLoader, ILayoutLog logger) {
        return computeLayout(layoutDescription, projectKey,
                screenWidth, screenHeight, false ,
                density, xdpi, ydpi, themeName, isProjectTheme,
                projectResources, frameworkResources, customViewLoader, logger);
    }
    public ILayoutResult computeLayout(IXmlPullParser layoutDescription, Object projectKey,
            int screenWidth, int screenHeight, boolean renderFullSize,
            int density, float xdpi, float ydpi,
            String themeName, boolean isProjectTheme,
            Map<String, Map<String, IResourceValue>> projectResources,
            Map<String, Map<String, IResourceValue>> frameworkResources,
            IProjectCallback customViewLoader, ILayoutLog logger) {
        if (logger == null) {
            logger = sDefaultLogger;
        }
        synchronized (sDefaultLogger) {
            sLogger = logger;
        }
        Map<IStyleResourceValue, IStyleResourceValue> styleParentMap =
            new HashMap<IStyleResourceValue, IStyleResourceValue>();
        IStyleResourceValue currentTheme = computeStyleMaps(themeName, isProjectTheme,
                projectResources.get(BridgeConstants.RES_STYLE),
                frameworkResources.get(BridgeConstants.RES_STYLE), styleParentMap);
        BridgeContext context = null;
        try {
            DisplayMetrics metrics = new DisplayMetrics();
            metrics.densityDpi = density;
            metrics.density = density / (float) DisplayMetrics.DENSITY_DEFAULT;
            metrics.scaledDensity = metrics.density;
            metrics.widthPixels = screenWidth;
            metrics.heightPixels = screenHeight;
            metrics.xdpi = xdpi;
            metrics.ydpi = ydpi;
            context = new BridgeContext(projectKey, metrics, currentTheme, projectResources,
                    frameworkResources, styleParentMap, customViewLoader, logger);
            BridgeInflater inflater = new BridgeInflater(context, customViewLoader);
            context.setBridgeInflater(inflater);
            IResourceValue windowBackground = null;
            int screenOffset = 0;
            if (currentTheme != null) {
                windowBackground = context.findItemInStyle(currentTheme, "windowBackground");
                windowBackground = context.resolveResValue(windowBackground);
                screenOffset = getScreenOffset(frameworkResources, currentTheme, context);
            }
            if (Looper.myLooper() == null) {
                Looper.prepare();
            }
            BridgeXmlBlockParser parser = new BridgeXmlBlockParser(layoutDescription,
                    context, false );
            ViewGroup root = new FrameLayout(context);
            View view = inflater.inflate(parser, root);
            postInflateProcess(view, customViewLoader);
            AttachInfo info = new AttachInfo(new WindowSession(), new Window(),
                    new Handler(), null);
            info.mHasWindowFocus = true;
            info.mWindowVisibility = View.VISIBLE;
            info.mInTouchMode = false; 
            root.dispatchAttachedToWindow(info, 0);
            if (windowBackground != null) {
                Drawable d = ResourceHelper.getDrawable(windowBackground,
                        context, true );
                root.setBackgroundDrawable(d);
            }
            int w_spec, h_spec;
            if (renderFullSize) {
                w_spec = MeasureSpec.makeMeasureSpec(screenWidth,
                        MeasureSpec.UNSPECIFIED); 
                h_spec = MeasureSpec.makeMeasureSpec(screenHeight - screenOffset,
                        MeasureSpec.UNSPECIFIED); 
                view.measure(w_spec, h_spec);
                int neededWidth = root.getChildAt(0).getMeasuredWidth();
                if (neededWidth > screenWidth) {
                    screenWidth = neededWidth;
                }
                int neededHeight = root.getChildAt(0).getMeasuredHeight();
                if (neededHeight > screenHeight - screenOffset) {
                    screenHeight = neededHeight + screenOffset;
                }
            }
            w_spec = MeasureSpec.makeMeasureSpec(screenWidth, MeasureSpec.EXACTLY);
            h_spec = MeasureSpec.makeMeasureSpec(screenHeight - screenOffset,
                    MeasureSpec.EXACTLY);
            view.measure(w_spec, h_spec);
            view.layout(0, screenOffset, screenWidth, screenHeight);
            Canvas canvas = new Canvas(screenWidth, screenHeight - screenOffset, logger);
            root.draw(canvas);
            canvas.dispose();
            return new LayoutResult(visit(((ViewGroup)view).getChildAt(0), context),
                    canvas.getImage());
        } catch (PostInflateException e) {
            return new LayoutResult(ILayoutResult.ERROR, "Error during post inflation process:\n"
                    + e.getMessage());
        } catch (Throwable e) {
            Throwable t = e;
            while (t.getCause() != null) {
                t = t.getCause();
            }
            logger.error(t);
            return new LayoutResult(ILayoutResult.ERROR,
                    t.getClass().getSimpleName() + ": " + t.getMessage());
        } finally {
            BridgeResources.clearSystem();
            BridgeAssetManager.clearSystem();
            synchronized (sDefaultLogger) {
                sLogger = sDefaultLogger;
            }
        }
    }
    public void clearCaches(Object projectKey) {
        if (projectKey != null) {
            sProjectBitmapCache.remove(projectKey);
            sProject9PatchCache.remove(projectKey);
        }
    }
    public static String[] resolveResourceValue(int value) {
        return sRMap.get(value);
    }
    public static String resolveResourceValue(int[] array) {
        return sRArrayMap.get(array);
    }
    public static Integer getResourceValue(String type, String name) {
        Map<String, Integer> map = sRFullMap.get(type);
        if (map != null) {
            return map.get(name);
        }
        return null;
    }
    static Map<String, Integer> getEnumValues(String attributeName) {
        if (sEnumValueMap != null) {
            return sEnumValueMap.get(attributeName);
        }
        return null;
    }
    private ILayoutViewInfo visit(View view, BridgeContext context) {
        if (view == null) {
            return null;
        }
        LayoutViewInfo result = new LayoutViewInfo(view.getClass().getName(),
                context.getViewKey(view),
                view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
        if (view instanceof ViewGroup) {
            ViewGroup group = ((ViewGroup) view);
            int n = group.getChildCount();
            ILayoutViewInfo[] children = new ILayoutViewInfo[n];
            for (int i = 0; i < group.getChildCount(); i++) {
                children[i] = visit(group.getChildAt(i), context);
            }
            result.setChildren(children);
        }
        return result;
    }
    private IStyleResourceValue computeStyleMaps(
            String themeName, boolean isProjectTheme, Map<String,
            IResourceValue> inProjectStyleMap, Map<String, IResourceValue> inFrameworkStyleMap,
            Map<IStyleResourceValue, IStyleResourceValue> outInheritanceMap) {
        if (inProjectStyleMap != null && inFrameworkStyleMap != null) {
            IResourceValue theme = null;
            if (isProjectTheme) {
                theme = inProjectStyleMap.get(themeName);
            } else {
                theme = inFrameworkStyleMap.get(themeName);
            }
            if (theme instanceof IStyleResourceValue) {
                computeStyleInheritance(inProjectStyleMap.values(), inProjectStyleMap,
                        inFrameworkStyleMap, outInheritanceMap);
                computeStyleInheritance(inFrameworkStyleMap.values(), null ,
                        inFrameworkStyleMap, outInheritanceMap);
                return (IStyleResourceValue)theme;
            }
        }
        return null;
    }
    private void computeStyleInheritance(Collection<IResourceValue> styles,
            Map<String, IResourceValue> inProjectStyleMap,
            Map<String, IResourceValue> inFrameworkStyleMap,
            Map<IStyleResourceValue, IStyleResourceValue> outInheritanceMap) {
        for (IResourceValue value : styles) {
            if (value instanceof IStyleResourceValue) {
                IStyleResourceValue style = (IStyleResourceValue)value;
                IStyleResourceValue parentStyle = null;
                String parentName = style.getParentStyle();
                if (parentName == null) {
                    parentName = getParentName(value.getName());
                }
                if (parentName != null) {
                    parentStyle = getStyle(parentName, inProjectStyleMap, inFrameworkStyleMap);
                    if (parentStyle != null) {
                        outInheritanceMap.put(style, parentStyle);
                    }
                }
            }
        }
    }
    private IStyleResourceValue getStyle(String parentName,
            Map<String, IResourceValue> inProjectStyleMap,
            Map<String, IResourceValue> inFrameworkStyleMap) {
        boolean frameworkOnly = false;
        String name = parentName;
        if (name.startsWith(BridgeConstants.PREFIX_RESOURCE_REF)) {
            name = name.substring(BridgeConstants.PREFIX_RESOURCE_REF.length());
        }
        if (name.startsWith(BridgeConstants.PREFIX_ANDROID)) {
            frameworkOnly = true;
            name = name.substring(BridgeConstants.PREFIX_ANDROID.length());
        }
        if (name.startsWith(BridgeConstants.REFERENCE_STYLE)) {
            name = name.substring(BridgeConstants.REFERENCE_STYLE.length());
        } else if (name.indexOf('/') != -1) {
            return null;
        }
        IResourceValue parent = null;
        if (frameworkOnly == false && inProjectStyleMap != null) {
            parent = inProjectStyleMap.get(name);
        }
        if (parent == null) {
            parent = inFrameworkStyleMap.get(name);
        }
        if (parent instanceof IStyleResourceValue) {
            return (IStyleResourceValue)parent;
        }
        sLogger.error(String.format("Unable to resolve parent style name: %s", parentName));
        return null;
    }
    private String getParentName(String styleName) {
        int index = styleName.lastIndexOf('.');
        if (index != -1) {
            return styleName.substring(0, index);
        }
        return null;
    }
    private int getScreenOffset(Map<String, Map<String, IResourceValue>> frameworkResources,
            IStyleResourceValue currentTheme, BridgeContext context) {
        int offset = 0;
        IResourceValue value = context.findItemInStyle(currentTheme, "windowNoTitle");
        value = context.resolveResValue(value);
        if (value == null || value.getValue() == null ||
                XmlUtils.convertValueToBoolean(value.getValue(), false ) == false) {
            int defaultOffset = DEFAULT_TITLE_BAR_HEIGHT;
            value = context.findItemInStyle(currentTheme, "windowTitleSize");
            value = context.resolveResValue(value);
            if (value != null) {
                TypedValue typedValue = ResourceHelper.getValue(value.getValue());
                if (typedValue != null) {
                    defaultOffset = (int)typedValue.getDimension(context.getResources().mMetrics);
                }
            }
            offset += defaultOffset;
        }
        value = context.findItemInStyle(currentTheme, "windowFullscreen");
        value = context.resolveResValue(value);
        if (value == null || value.getValue() == null ||
                XmlUtils.convertValueToBoolean(value.getValue(), false ) == false) {
            int defaultOffset = DEFAULT_STATUS_BAR_HEIGHT;
            Map<String, IResourceValue> dimens = frameworkResources.get(BridgeConstants.RES_DIMEN);
            value = dimens.get("status_bar_height");
            if (value != null) {
                TypedValue typedValue = ResourceHelper.getValue(value.getValue());
                if (typedValue != null) {
                    defaultOffset = (int)typedValue.getDimension(context.getResources().mMetrics);
                }
            }
            offset += defaultOffset;
        }
        return offset;
    }
    private void postInflateProcess(View view, IProjectCallback projectCallback)
            throws PostInflateException {
        if (view instanceof TabHost) {
            setupTabHost((TabHost)view, projectCallback);
        } else if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup)view;
            final int count = group.getChildCount();
            for (int c = 0 ; c < count ; c++) {
                View child = group.getChildAt(c);
                postInflateProcess(child, projectCallback);
            }
        }
    }
    private void setupTabHost(TabHost tabHost, IProjectCallback projectCallback)
            throws PostInflateException {
        View v = tabHost.findViewById(android.R.id.tabs);
        if (v == null) {
            throw new PostInflateException(
                    "TabHost requires a TabWidget with id \"android:id/tabs\".\n");
        }
        if ((v instanceof TabWidget) == false) {
            throw new PostInflateException(String.format(
                    "TabHost requires a TabWidget with id \"android:id/tabs\".\n" +
                    "View found with id 'tabs' is '%s'", v.getClass().getCanonicalName()));
        }
        v = tabHost.findViewById(android.R.id.tabcontent);
        if (v == null) {
            throw new PostInflateException(
                    "TabHost requires a FrameLayout with id \"android:id/tabcontent\".");
        }
        if ((v instanceof FrameLayout) == false) {
            throw new PostInflateException(String.format(
                    "TabHost requires a FrameLayout with id \"android:id/tabcontent\".\n" +
                    "View found with id 'tabcontent' is '%s'", v.getClass().getCanonicalName()));
        }
        FrameLayout content = (FrameLayout)v;
        final int count = content.getChildCount();
        if (count == 0) {
            throw new PostInflateException(
                    "The FrameLayout for the TabHost has no content. Rendering failed.\n");
        }
        tabHost.setup();
        for (int i = 0 ; i < count ; i++) {
            View child = content.getChildAt(i);
            String tabSpec = String.format("tab_spec%d", i+1);
            int id = child.getId();
            String[] resource = projectCallback.resolveResourceValue(id);
            String name;
            if (resource != null) {
                name = resource[0]; 
            } else {
                name = String.format("Tab %d", i+1); 
            }
            tabHost.addTab(tabHost.newTabSpec(tabSpec).setIndicator(name).setContent(id));
        }
    }
    static Bitmap getCachedBitmap(String value, Object projectKey) {
        if (projectKey != null) {
            Map<String, SoftReference<Bitmap>> map = sProjectBitmapCache.get(projectKey);
            if (map != null) {
                SoftReference<Bitmap> ref = map.get(value);
                if (ref != null) {
                    return ref.get();
                }
            }
        } else {
            SoftReference<Bitmap> ref = sFrameworkBitmapCache.get(value);
            if (ref != null) {
                return ref.get();
            }
        }
        return null;
    }
    static void setCachedBitmap(String value, Bitmap bmp, Object projectKey) {
        if (projectKey != null) {
            Map<String, SoftReference<Bitmap>> map = sProjectBitmapCache.get(projectKey);
            if (map == null) {
                map = new HashMap<String, SoftReference<Bitmap>>();
                sProjectBitmapCache.put(projectKey, map);
            }
            map.put(value, new SoftReference<Bitmap>(bmp));
        } else {
            sFrameworkBitmapCache.put(value, new SoftReference<Bitmap>(bmp));
        }
    }
    static NinePatch getCached9Patch(String value, Object projectKey) {
        if (projectKey != null) {
            Map<String, SoftReference<NinePatch>> map = sProject9PatchCache.get(projectKey);
            if (map != null) {
                SoftReference<NinePatch> ref = map.get(value);
                if (ref != null) {
                    return ref.get();
                }
            }
        } else {
            SoftReference<NinePatch> ref = sFramework9PatchCache.get(value);
            if (ref != null) {
                return ref.get();
            }
        }
        return null;
    }
    static void setCached9Patch(String value, NinePatch ninePatch, Object projectKey) {
        if (projectKey != null) {
            Map<String, SoftReference<NinePatch>> map = sProject9PatchCache.get(projectKey);
            if (map == null) {
                map = new HashMap<String, SoftReference<NinePatch>>();
                sProject9PatchCache.put(projectKey, map);
            }
            map.put(value, new SoftReference<NinePatch>(ninePatch));
        } else {
            sFramework9PatchCache.put(value, new SoftReference<NinePatch>(ninePatch));
        }
    }
    private static final class PostInflateException extends Exception {
        private static final long serialVersionUID = 1L;
        public PostInflateException(String message) {
            super(message);
        }
    }
    private static final class WindowSession implements IWindowSession {
        @SuppressWarnings("unused")
        public int add(IWindow arg0, LayoutParams arg1, int arg2, Rect arg3)
                throws RemoteException {
            return 0;
        }
        @SuppressWarnings("unused")
        public void finishDrawing(IWindow arg0) throws RemoteException {
        }
        @SuppressWarnings("unused")
        public void finishKey(IWindow arg0) throws RemoteException {
        }
        @SuppressWarnings("unused")
        public boolean getInTouchMode() throws RemoteException {
            return false;
        }
        @SuppressWarnings("unused")
        public boolean performHapticFeedback(IWindow window, int effectId, boolean always) {
            return false;
        }
        @SuppressWarnings("unused")
        public MotionEvent getPendingPointerMove(IWindow arg0) throws RemoteException {
            return null;
        }
        @SuppressWarnings("unused")
        public MotionEvent getPendingTrackballMove(IWindow arg0) throws RemoteException {
            return null;
        }
        @SuppressWarnings("unused")
        public int relayout(IWindow arg0, LayoutParams arg1, int arg2, int arg3, int arg4,
                boolean arg4_5, Rect arg5, Rect arg6, Rect arg7, Configuration arg7b, Surface arg8)
                throws RemoteException {
            return 0;
        }
        public void getDisplayFrame(IWindow window, Rect outDisplayFrame) {
        }
        @SuppressWarnings("unused")
        public void remove(IWindow arg0) throws RemoteException {
        }
        @SuppressWarnings("unused")
        public void setInTouchMode(boolean arg0) throws RemoteException {
        }
        @SuppressWarnings("unused")
        public void setTransparentRegion(IWindow arg0, Region arg1) throws RemoteException {
        }
        @SuppressWarnings("unused")
        public void setInsets(IWindow window, int touchable, Rect contentInsets,
                Rect visibleInsets) {
        }
        @SuppressWarnings("unused")
        public void setWallpaperPosition(IBinder window, float x, float y,
            float xStep, float yStep) {
        }
        @SuppressWarnings("unused")
        public void wallpaperOffsetsComplete(IBinder window) {
        }
        @SuppressWarnings("unused")
        public Bundle sendWallpaperCommand(IBinder window, String action, int x, int y,
                int z, Bundle extras, boolean sync) {
            return null;
        }
        @SuppressWarnings("unused")
        public void wallpaperCommandComplete(IBinder window, Bundle result) {
        }
        @SuppressWarnings("unused")
        public void closeSystemDialogs(String reason) {
        }
        public IBinder asBinder() {
            return null;
        }
    }
    private static final class Window implements IWindow {
        @SuppressWarnings("unused")
        public void dispatchAppVisibility(boolean arg0) throws RemoteException {
        }
        @SuppressWarnings("unused")
        public void dispatchGetNewSurface() throws RemoteException {
        }
        @SuppressWarnings("unused")
        public void dispatchKey(KeyEvent arg0) throws RemoteException {
        }
        @SuppressWarnings("unused")
        public void dispatchPointer(MotionEvent arg0, long arg1, boolean arg2) throws RemoteException {
        }
        @SuppressWarnings("unused")
        public void dispatchTrackball(MotionEvent arg0, long arg1, boolean arg2) throws RemoteException {
        }
        @SuppressWarnings("unused")
        public void executeCommand(String arg0, String arg1, ParcelFileDescriptor arg2)
                throws RemoteException {
        }
        @SuppressWarnings("unused")
        public void resized(int arg0, int arg1, Rect arg2, Rect arg3, boolean arg4, Configuration arg5)
                throws RemoteException {
        }
        @SuppressWarnings("unused")
        public void windowFocusChanged(boolean arg0, boolean arg1) throws RemoteException {
        }
        @SuppressWarnings("unused")
        public void dispatchWallpaperOffsets(float x, float y, float xStep, float yStep,
                boolean sync) {
        }
        @SuppressWarnings("unused")
        public void dispatchWallpaperCommand(String action, int x, int y,
                int z, Bundle extras, boolean sync) {
        }
        @SuppressWarnings("unused")
        public void closeSystemDialogs(String reason) {
        }
        public IBinder asBinder() {
            return null;
        }
    }
}
