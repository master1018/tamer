public class ViewDebug {
    public static final String CONSISTENCY_LOG_TAG = "ViewConsistency";
    public static final int CONSISTENCY_LAYOUT = 0x1;
    public static final int CONSISTENCY_DRAWING = 0x2;
    public static final boolean TRACE_HIERARCHY = false;
    public static final boolean TRACE_RECYCLER = false;
    public static final boolean TRACE_MOTION_EVENTS = false;
    static final String SYSTEM_PROPERTY_CAPTURE_VIEW = "debug.captureview";
    static final String SYSTEM_PROPERTY_CAPTURE_EVENT = "debug.captureevent";
    @Debug.DebugProperty
    public static boolean profileDrawing = false;
    @Debug.DebugProperty
    public static boolean profileLayout = false;
    @Debug.DebugProperty
    public static boolean showFps = false;
    @Debug.DebugProperty
    public static boolean consistencyCheckEnabled = false;
    static {
        if (Config.DEBUG) {        
	        Debug.setFieldsOn(ViewDebug.class, true);
	    }
    }
    @Target({ ElementType.FIELD, ElementType.METHOD })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ExportedProperty {
        boolean resolveId() default false;
        IntToString[] mapping() default { };
        IntToString[] indexMapping() default { };
        FlagToString[] flagMapping() default { };
        boolean deepExport() default false;
        String prefix() default "";
    }
    @Target({ ElementType.TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface IntToString {
        int from();
        String to();
    }
    @Target({ ElementType.TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface FlagToString {
        int mask();
        int equals();
        String name();
        boolean outputIf() default true;
    }
    @Target({ ElementType.FIELD, ElementType.METHOD })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface CapturedViewProperty {
        boolean retrieveReturn() default false;
    }
    private static HashMap<Class<?>, Method[]> mCapturedViewMethodsForClasses = null;
    private static HashMap<Class<?>, Field[]> mCapturedViewFieldsForClasses = null;
    private static final int CAPTURE_TIMEOUT = 4000;
    private static final String REMOTE_COMMAND_CAPTURE = "CAPTURE";
    private static final String REMOTE_COMMAND_DUMP = "DUMP";
    private static final String REMOTE_COMMAND_INVALIDATE = "INVALIDATE";
    private static final String REMOTE_COMMAND_REQUEST_LAYOUT = "REQUEST_LAYOUT";
    private static final String REMOTE_PROFILE = "PROFILE";
    private static final String REMOTE_COMMAND_CAPTURE_LAYERS = "CAPTURE_LAYERS";
    private static HashMap<Class<?>, Field[]> sFieldsForClasses;
    private static HashMap<Class<?>, Method[]> sMethodsForClasses;
    private static HashMap<AccessibleObject, ExportedProperty> sAnnotations;
    public enum HierarchyTraceType {
        INVALIDATE,
        INVALIDATE_CHILD,
        INVALIDATE_CHILD_IN_PARENT,
        REQUEST_LAYOUT,
        ON_LAYOUT,
        ON_MEASURE,
        DRAW,
        BUILD_CACHE
    }
    private static BufferedWriter sHierarchyTraces;
    private static ViewRoot sHierarhcyRoot;
    private static String sHierarchyTracePrefix;
    public enum RecyclerTraceType {
        NEW_VIEW,
        BIND_VIEW,
        RECYCLE_FROM_ACTIVE_HEAP,
        RECYCLE_FROM_SCRAP_HEAP,
        MOVE_TO_SCRAP_HEAP,
        MOVE_FROM_ACTIVE_TO_SCRAP_HEAP
    }
    private static class RecyclerTrace {
        public int view;
        public RecyclerTraceType type;
        public int position;
        public int indexOnScreen;
    }
    private static View sRecyclerOwnerView;
    private static List<View> sRecyclerViews;
    private static List<RecyclerTrace> sRecyclerTraces;
    private static String sRecyclerTracePrefix;
    public enum MotionEventTraceType {
        DISPATCH,
        ON_INTERCEPT,
        ON_TOUCH
    }
    private static BufferedWriter sMotionEventTraces;
    private static ViewRoot sMotionEventRoot;
    private static String sMotionEventTracePrefix;
    public static long getViewInstanceCount() {
        return View.sInstanceCount;
    }
    public static long getViewRootInstanceCount() {
        return ViewRoot.getInstanceCount();
    }
    public static void trace(View view, RecyclerTraceType type, int... parameters) {
        if (sRecyclerOwnerView == null || sRecyclerViews == null) {
            return;
        }
        if (!sRecyclerViews.contains(view)) {
            sRecyclerViews.add(view);
        }
        final int index = sRecyclerViews.indexOf(view);
        RecyclerTrace trace = new RecyclerTrace();
        trace.view = index;
        trace.type = type;
        trace.position = parameters[0];
        trace.indexOnScreen = parameters[1];
        sRecyclerTraces.add(trace);
    }
    public static void startRecyclerTracing(String prefix, View view) {
        if (!TRACE_RECYCLER) {
            return;
        }
        if (sRecyclerOwnerView != null) {
            throw new IllegalStateException("You must call stopRecyclerTracing() before running" +
                " a new trace!");
        }
        sRecyclerTracePrefix = prefix;
        sRecyclerOwnerView = view;
        sRecyclerViews = new ArrayList<View>();
        sRecyclerTraces = new LinkedList<RecyclerTrace>();
    }
    public static void stopRecyclerTracing() {
        if (!TRACE_RECYCLER) {
            return;
        }
        if (sRecyclerOwnerView == null || sRecyclerViews == null) {
            throw new IllegalStateException("You must call startRecyclerTracing() before" +
                " stopRecyclerTracing()!");
        }
        File recyclerDump = new File(Environment.getExternalStorageDirectory(), "view-recycler/");
        recyclerDump.mkdirs();
        recyclerDump = new File(recyclerDump, sRecyclerTracePrefix + ".recycler");
        try {
            final BufferedWriter out = new BufferedWriter(new FileWriter(recyclerDump), 8 * 1024);
            for (View view : sRecyclerViews) {
                final String name = view.getClass().getName();
                out.write(name);
                out.newLine();
            }
            out.close();
        } catch (IOException e) {
            Log.e("View", "Could not dump recycler content");
            return;
        }
        recyclerDump = new File(Environment.getExternalStorageDirectory(), "view-recycler/");
        recyclerDump = new File(recyclerDump, sRecyclerTracePrefix + ".traces");
        try {
            if (recyclerDump.exists()) {
                recyclerDump.delete();
            }
            final FileOutputStream file = new FileOutputStream(recyclerDump);
            final DataOutputStream out = new DataOutputStream(file);
            for (RecyclerTrace trace : sRecyclerTraces) {
                out.writeInt(trace.view);
                out.writeInt(trace.type.ordinal());
                out.writeInt(trace.position);
                out.writeInt(trace.indexOnScreen);
                out.flush();
            }
            out.close();
        } catch (IOException e) {
            Log.e("View", "Could not dump recycler traces");
            return;
        }
        sRecyclerViews.clear();
        sRecyclerViews = null;
        sRecyclerTraces.clear();
        sRecyclerTraces = null;
        sRecyclerOwnerView = null;
    }
    public static void trace(View view, HierarchyTraceType type) {
        if (sHierarchyTraces == null) {
            return;
        }
        try {
            sHierarchyTraces.write(type.name());
            sHierarchyTraces.write(' ');
            sHierarchyTraces.write(view.getClass().getName());
            sHierarchyTraces.write('@');
            sHierarchyTraces.write(Integer.toHexString(view.hashCode()));
            sHierarchyTraces.newLine();
        } catch (IOException e) {
            Log.w("View", "Error while dumping trace of type " + type + " for view " + view);
        }
    }
    public static void startHierarchyTracing(String prefix, View view) {
        if (!TRACE_HIERARCHY) {
            return;
        }
        if (sHierarhcyRoot != null) {
            throw new IllegalStateException("You must call stopHierarchyTracing() before running" +
                " a new trace!");
        }
        File hierarchyDump = new File(Environment.getExternalStorageDirectory(), "view-hierarchy/");
        hierarchyDump.mkdirs();
        hierarchyDump = new File(hierarchyDump, prefix + ".traces");
        sHierarchyTracePrefix = prefix;
        try {
            sHierarchyTraces = new BufferedWriter(new FileWriter(hierarchyDump), 8 * 1024);
        } catch (IOException e) {
            Log.e("View", "Could not dump view hierarchy");
            return;
        }
        sHierarhcyRoot = (ViewRoot) view.getRootView().getParent();
    }
    public static void stopHierarchyTracing() {
        if (!TRACE_HIERARCHY) {
            return;
        }
        if (sHierarhcyRoot == null || sHierarchyTraces == null) {
            throw new IllegalStateException("You must call startHierarchyTracing() before" +
                " stopHierarchyTracing()!");
        }
        try {
            sHierarchyTraces.close();
        } catch (IOException e) {
            Log.e("View", "Could not write view traces");
        }
        sHierarchyTraces = null;
        File hierarchyDump = new File(Environment.getExternalStorageDirectory(), "view-hierarchy/");
        hierarchyDump.mkdirs();
        hierarchyDump = new File(hierarchyDump, sHierarchyTracePrefix + ".tree");
        BufferedWriter out;
        try {
            out = new BufferedWriter(new FileWriter(hierarchyDump), 8 * 1024);
        } catch (IOException e) {
            Log.e("View", "Could not dump view hierarchy");
            return;
        }
        View view = sHierarhcyRoot.getView();
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            dumpViewHierarchy(group, out, 0);
            try {
                out.close();
            } catch (IOException e) {
                Log.e("View", "Could not dump view hierarchy");
            }
        }
        sHierarhcyRoot = null;
    }
    public static void trace(View view, MotionEvent event, MotionEventTraceType type) {
        if (sMotionEventTraces == null) {
            return;
        }
        try {
            sMotionEventTraces.write(type.name());
            sMotionEventTraces.write(' ');
            sMotionEventTraces.write(event.getAction());
            sMotionEventTraces.write(' ');
            sMotionEventTraces.write(view.getClass().getName());
            sMotionEventTraces.write('@');
            sMotionEventTraces.write(Integer.toHexString(view.hashCode()));
            sHierarchyTraces.newLine();
        } catch (IOException e) {
            Log.w("View", "Error while dumping trace of event " + event + " for view " + view);
        }
    }
    public static void startMotionEventTracing(String prefix, View view) {
        if (!TRACE_MOTION_EVENTS) {
            return;
        }
        if (sMotionEventRoot != null) {
            throw new IllegalStateException("You must call stopMotionEventTracing() before running" +
                " a new trace!");
        }
        File hierarchyDump = new File(Environment.getExternalStorageDirectory(), "motion-events/");
        hierarchyDump.mkdirs();
        hierarchyDump = new File(hierarchyDump, prefix + ".traces");
        sMotionEventTracePrefix = prefix;
        try {
            sMotionEventTraces = new BufferedWriter(new FileWriter(hierarchyDump), 32 * 1024);
        } catch (IOException e) {
            Log.e("View", "Could not dump view hierarchy");
            return;
        }
        sMotionEventRoot = (ViewRoot) view.getRootView().getParent();
    }
    public static void stopMotionEventTracing() {
        if (!TRACE_MOTION_EVENTS) {
            return;
        }
        if (sMotionEventRoot == null || sMotionEventTraces == null) {
            throw new IllegalStateException("You must call startMotionEventTracing() before" +
                " stopMotionEventTracing()!");
        }
        try {
            sMotionEventTraces.close();
        } catch (IOException e) {
            Log.e("View", "Could not write view traces");
        }
        sMotionEventTraces = null;
        File hierarchyDump = new File(Environment.getExternalStorageDirectory(), "motion-events/");
        hierarchyDump.mkdirs();
        hierarchyDump = new File(hierarchyDump, sMotionEventTracePrefix + ".tree");
        BufferedWriter out;
        try {
            out = new BufferedWriter(new FileWriter(hierarchyDump), 8 * 1024);
        } catch (IOException e) {
            Log.e("View", "Could not dump view hierarchy");
            return;
        }
        View view = sMotionEventRoot.getView();
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            dumpViewHierarchy(group, out, 0);
            try {
                out.close();
            } catch (IOException e) {
                Log.e("View", "Could not dump view hierarchy");
            }
        }
        sHierarhcyRoot = null;
    }
    static void dispatchCommand(View view, String command, String parameters,
            OutputStream clientStream) throws IOException {
        view = view.getRootView();
        if (REMOTE_COMMAND_DUMP.equalsIgnoreCase(command)) {
            dump(view, clientStream);
        } else if (REMOTE_COMMAND_CAPTURE_LAYERS.equalsIgnoreCase(command)) {
            captureLayers(view, new DataOutputStream(clientStream));
        } else {
            final String[] params = parameters.split(" ");
            if (REMOTE_COMMAND_CAPTURE.equalsIgnoreCase(command)) {
                capture(view, clientStream, params[0]);
            } else if (REMOTE_COMMAND_INVALIDATE.equalsIgnoreCase(command)) {
                invalidate(view, params[0]);
            } else if (REMOTE_COMMAND_REQUEST_LAYOUT.equalsIgnoreCase(command)) {
                requestLayout(view, params[0]);
            } else if (REMOTE_PROFILE.equalsIgnoreCase(command)) {
                profile(view, clientStream, params[0]);
            }
        }
    }
    private static View findView(View root, String parameter) {
        if (parameter.indexOf('@') != -1) {
            final String[] ids = parameter.split("@");
            final String className = ids[0];
            final int hashCode = (int) Long.parseLong(ids[1], 16);
            View view = root.getRootView();
            if (view instanceof ViewGroup) {
                return findView((ViewGroup) view, className, hashCode);
            }
        } else {
            final int id = root.getResources().getIdentifier(parameter, null, null);
            return root.getRootView().findViewById(id);
        }
        return null;
    }
    private static void invalidate(View root, String parameter) {
        final View view = findView(root, parameter);
        if (view != null) {
            view.postInvalidate();
        }
    }
    private static void requestLayout(View root, String parameter) {
        final View view = findView(root, parameter);
        if (view != null) {
            root.post(new Runnable() {
                public void run() {
                    view.requestLayout();
                }
            });
        }
    }
    private static void profile(View root, OutputStream clientStream, String parameter)
            throws IOException {
        final View view = findView(root, parameter);
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(clientStream), 32 * 1024);
            if (view != null) {
                final long durationMeasure = profileViewOperation(view, new ViewOperation<Void>() {
                    public Void[] pre() {
                        forceLayout(view);
                        return null;
                    }
                    private void forceLayout(View view) {
                        view.forceLayout();
                        if (view instanceof ViewGroup) {
                            ViewGroup group = (ViewGroup) view;
                            final int count = group.getChildCount();
                            for (int i = 0; i < count; i++) {
                                forceLayout(group.getChildAt(i));
                            }
                        }
                    }
                    public void run(Void... data) {
                        view.measure(view.mOldWidthMeasureSpec, view.mOldHeightMeasureSpec);
                    }
                    public void post(Void... data) {
                    }
                });
                final long durationLayout = profileViewOperation(view, new ViewOperation<Void>() {
                    public Void[] pre() {
                        return null;
                    }
                    public void run(Void... data) {
                        view.layout(view.mLeft, view.mTop, view.mRight, view.mBottom);
                    }
                    public void post(Void... data) {
                    }
                });
                final long durationDraw = profileViewOperation(view, new ViewOperation<Object>() {
                    public Object[] pre() {
                        final DisplayMetrics metrics = view.getResources().getDisplayMetrics();
                        final Bitmap bitmap = Bitmap.createBitmap(metrics.widthPixels,
                                metrics.heightPixels, Bitmap.Config.RGB_565);
                        final Canvas canvas = new Canvas(bitmap);
                        return new Object[] { bitmap, canvas };
                    }
                    public void run(Object... data) {
                        view.draw((Canvas) data[1]);
                    }
                    public void post(Object... data) {
                        ((Bitmap) data[0]).recycle();
                    }
                });
                out.write(String.valueOf(durationMeasure));
                out.write(' ');
                out.write(String.valueOf(durationLayout));
                out.write(' ');
                out.write(String.valueOf(durationDraw));
                out.newLine();
            } else {
                out.write("-1 -1 -1");
                out.newLine();
            }
        } catch (Exception e) {
            android.util.Log.w("View", "Problem profiling the view:", e);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }
    interface ViewOperation<T> {
        T[] pre();
        void run(T... data);
        void post(T... data);
    }
    private static <T> long profileViewOperation(View view, final ViewOperation<T> operation) {
        final CountDownLatch latch = new CountDownLatch(1);
        final long[] duration = new long[1];
        view.post(new Runnable() {
            public void run() {
                try {
                    T[] data = operation.pre();
                    long start = Debug.threadCpuTimeNanos();
                    operation.run(data);
                    duration[0] = Debug.threadCpuTimeNanos() - start;
                    operation.post(data);
                } finally {
                    latch.countDown();
                }
            }
        });
        try {
            latch.await(CAPTURE_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Log.w("View", "Could not complete the profiling of the view " + view);
            Thread.currentThread().interrupt();
            return -1;
        }
        return duration[0];
    }
    private static void captureLayers(View root, final DataOutputStream clientStream)
            throws IOException {
        try {
            Rect outRect = new Rect();
            try {
                root.mAttachInfo.mSession.getDisplayFrame(root.mAttachInfo.mWindow, outRect);
            } catch (RemoteException e) {
            }
            clientStream.writeInt(outRect.width());
            clientStream.writeInt(outRect.height());
            captureViewLayer(root, clientStream, true);
            clientStream.write(2);
        } finally {
            clientStream.close();
        }
    }
    private static void captureViewLayer(View view, DataOutputStream clientStream, boolean visible)
            throws IOException {
        final boolean localVisible = view.getVisibility() == View.VISIBLE && visible;
        if ((view.mPrivateFlags & View.SKIP_DRAW) != View.SKIP_DRAW) {
            final int id = view.getId();
            String name = view.getClass().getSimpleName();
            if (id != View.NO_ID) {
                name = resolveId(view.getContext(), id).toString();
            }
            clientStream.write(1);
            clientStream.writeUTF(name);
            clientStream.writeByte(localVisible ? 1 : 0);
            int[] position = new int[2];
            view.getLocationInWindow(position);
            clientStream.writeInt(position[0]);
            clientStream.writeInt(position[1]);
            clientStream.flush();
            Bitmap b = performViewCapture(view, true);
            if (b != null) {
                ByteArrayOutputStream arrayOut = new ByteArrayOutputStream(b.getWidth() *
                        b.getHeight() * 2);
                b.compress(Bitmap.CompressFormat.PNG, 100, arrayOut);
                clientStream.writeInt(arrayOut.size());
                arrayOut.writeTo(clientStream);
            }
            clientStream.flush();
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            int count = group.getChildCount();
            for (int i = 0; i < count; i++) {
                captureViewLayer(group.getChildAt(i), clientStream, localVisible);
            }
        }
    }
    private static void capture(View root, final OutputStream clientStream, String parameter)
            throws IOException {
        final View captureView = findView(root, parameter);
        Bitmap b = performViewCapture(captureView, false);
        if (b != null) {
            BufferedOutputStream out = null;
            try {
                out = new BufferedOutputStream(clientStream, 32 * 1024);
                b.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.flush();
            } finally {
                if (out != null) {
                    out.close();
                }
                b.recycle();
            }
        } else {
            Log.w("View", "Failed to create capture bitmap!");
            clientStream.close();
        }
    }
    private static Bitmap performViewCapture(final View captureView, final boolean skpiChildren) {
        if (captureView != null) {
            final CountDownLatch latch = new CountDownLatch(1);
            final Bitmap[] cache = new Bitmap[1];
            captureView.post(new Runnable() {
                public void run() {
                    try {
                        cache[0] = captureView.createSnapshot(
                                Bitmap.Config.ARGB_8888, 0, skpiChildren);
                    } catch (OutOfMemoryError e) {
                        try {
                            cache[0] = captureView.createSnapshot(
                                    Bitmap.Config.ARGB_4444, 0, skpiChildren);
                        } catch (OutOfMemoryError e2) {
                            Log.w("View", "Out of memory for bitmap");
                        }
                    } finally {
                        latch.countDown();
                    }
                }
            });
            try {
                latch.await(CAPTURE_TIMEOUT, TimeUnit.MILLISECONDS);
                return cache[0];
            } catch (InterruptedException e) {
                Log.w("View", "Could not complete the capture of the view " + captureView);
                Thread.currentThread().interrupt();
            }
        }
        return null;
    }
    private static void dump(View root, OutputStream clientStream) throws IOException {
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(clientStream, "utf-8"), 32 * 1024);
            View view = root.getRootView();
            if (view instanceof ViewGroup) {
                ViewGroup group = (ViewGroup) view;
                dumpViewHierarchyWithProperties(group.getContext(), group, out, 0);
            }
            out.write("DONE.");
            out.newLine();
        } catch (Exception e) {
            android.util.Log.w("View", "Problem dumping the view:", e);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }
    private static View findView(ViewGroup group, String className, int hashCode) {
        if (isRequestedView(group, className, hashCode)) {
            return group;
        }
        final int count = group.getChildCount();
        for (int i = 0; i < count; i++) {
            final View view = group.getChildAt(i);
            if (view instanceof ViewGroup) {
                final View found = findView((ViewGroup) view, className, hashCode);
                if (found != null) {
                    return found;
                }
            } else if (isRequestedView(view, className, hashCode)) {
                return view;
            }
        }
        return null;
    }
    private static boolean isRequestedView(View view, String className, int hashCode) {
        return view.getClass().getName().equals(className) && view.hashCode() == hashCode;
    }
    private static void dumpViewHierarchyWithProperties(Context context, ViewGroup group,
            BufferedWriter out, int level) {
        if (!dumpViewWithProperties(context, group, out, level)) {
            return;
        }
        final int count = group.getChildCount();
        for (int i = 0; i < count; i++) {
            final View view = group.getChildAt(i);
            if (view instanceof ViewGroup) {
                dumpViewHierarchyWithProperties(context, (ViewGroup) view, out, level + 1);
            } else {
                dumpViewWithProperties(context, view, out, level + 1);
            }
        }
    }
    private static boolean dumpViewWithProperties(Context context, View view,
            BufferedWriter out, int level) {
        try {
            for (int i = 0; i < level; i++) {
                out.write(' ');
            }
            out.write(view.getClass().getName());
            out.write('@');
            out.write(Integer.toHexString(view.hashCode()));
            out.write(' ');
            dumpViewProperties(context, view, out);
            out.newLine();
        } catch (IOException e) {
            Log.w("View", "Error while dumping hierarchy tree");
            return false;
        }
        return true;
    }
    private static Field[] getExportedPropertyFields(Class<?> klass) {
        if (sFieldsForClasses == null) {
            sFieldsForClasses = new HashMap<Class<?>, Field[]>();
        }
        if (sAnnotations == null) {
            sAnnotations = new HashMap<AccessibleObject, ExportedProperty>(512);
        }
        final HashMap<Class<?>, Field[]> map = sFieldsForClasses;
        final HashMap<AccessibleObject, ExportedProperty> annotations = sAnnotations;
        Field[] fields = map.get(klass);
        if (fields != null) {
            return fields;
        }
        final ArrayList<Field> foundFields = new ArrayList<Field>();
        fields = klass.getDeclaredFields();
        int count = fields.length;
        for (int i = 0; i < count; i++) {
            final Field field = fields[i];
            if (field.isAnnotationPresent(ExportedProperty.class)) {
                field.setAccessible(true);
                foundFields.add(field);
                annotations.put(field, field.getAnnotation(ExportedProperty.class));
            }
        }
        fields = foundFields.toArray(new Field[foundFields.size()]);
        map.put(klass, fields);
        return fields;
    }
    private static Method[] getExportedPropertyMethods(Class<?> klass) {
        if (sMethodsForClasses == null) {
            sMethodsForClasses = new HashMap<Class<?>, Method[]>(100);
        }
        if (sAnnotations == null) {
            sAnnotations = new HashMap<AccessibleObject, ExportedProperty>(512);
        }
        final HashMap<Class<?>, Method[]> map = sMethodsForClasses;
        final HashMap<AccessibleObject, ExportedProperty> annotations = sAnnotations;
        Method[] methods = map.get(klass);
        if (methods != null) {
            return methods;
        }
        final ArrayList<Method> foundMethods = new ArrayList<Method>();
        methods = klass.getDeclaredMethods();
        int count = methods.length;
        for (int i = 0; i < count; i++) {
            final Method method = methods[i];
            if (method.getParameterTypes().length == 0 &&
                    method.isAnnotationPresent(ExportedProperty.class) &&
                    method.getReturnType() != Void.class) {
                method.setAccessible(true);
                foundMethods.add(method);
                annotations.put(method, method.getAnnotation(ExportedProperty.class));
            }
        }
        methods = foundMethods.toArray(new Method[foundMethods.size()]);
        map.put(klass, methods);
        return methods;
    }
    private static void dumpViewProperties(Context context, Object view,
            BufferedWriter out) throws IOException {
        dumpViewProperties(context, view, out, "");
    }
    private static void dumpViewProperties(Context context, Object view,
            BufferedWriter out, String prefix) throws IOException {
        Class<?> klass = view.getClass();
        do {
            exportFields(context, view, out, klass, prefix);
            exportMethods(context, view, out, klass, prefix);
            klass = klass.getSuperclass();
        } while (klass != Object.class);
    }
    private static void exportMethods(Context context, Object view, BufferedWriter out,
            Class<?> klass, String prefix) throws IOException {
        final Method[] methods = getExportedPropertyMethods(klass);
        int count = methods.length;
        for (int i = 0; i < count; i++) {
            final Method method = methods[i];
            try {
                Object methodValue = method.invoke(view, (Object[]) null);
                final Class<?> returnType = method.getReturnType();
                if (returnType == int.class) {
                    final ExportedProperty property = sAnnotations.get(method);
                    if (property.resolveId() && context != null) {
                        final int id = (Integer) methodValue;
                        methodValue = resolveId(context, id);
                    } else {
                        final FlagToString[] flagsMapping = property.flagMapping();
                        if (flagsMapping.length > 0) {
                            final int intValue = (Integer) methodValue;
                            final String valuePrefix = prefix + method.getName() + '_';
                            exportUnrolledFlags(out, flagsMapping, intValue, valuePrefix);
                        }
                        final IntToString[] mapping = property.mapping();
                        if (mapping.length > 0) {
                            final int intValue = (Integer) methodValue;
                            boolean mapped = false;
                            int mappingCount = mapping.length;
                            for (int j = 0; j < mappingCount; j++) {
                                final IntToString mapper = mapping[j];
                                if (mapper.from() == intValue) {
                                    methodValue = mapper.to();
                                    mapped = true;
                                    break;
                                }
                            }
                            if (!mapped) {
                                methodValue = intValue;
                            }
                        }
                    }
                } else if (returnType == int[].class) {
                    final ExportedProperty property = sAnnotations.get(method);
                    final int[] array = (int[]) methodValue;
                    final String valuePrefix = prefix + method.getName() + '_';
                    final String suffix = "()";
                    exportUnrolledArray(context, out, property, array, valuePrefix, suffix);
                } else if (!returnType.isPrimitive()) {
                    final ExportedProperty property = sAnnotations.get(method);
                    if (property.deepExport()) {
                        dumpViewProperties(context, methodValue, out, prefix + property.prefix());
                        continue;
                    }
                }
                writeEntry(out, prefix, method.getName(), "()", methodValue);
            } catch (IllegalAccessException e) {
            } catch (InvocationTargetException e) {
            }
        }
    }
    private static void exportFields(Context context, Object view, BufferedWriter out,
            Class<?> klass, String prefix) throws IOException {
        final Field[] fields = getExportedPropertyFields(klass);
        int count = fields.length;
        for (int i = 0; i < count; i++) {
            final Field field = fields[i];
            try {
                Object fieldValue = null;
                final Class<?> type = field.getType();
                if (type == int.class) {
                    final ExportedProperty property = sAnnotations.get(field);
                    if (property.resolveId() && context != null) {
                        final int id = field.getInt(view);
                        fieldValue = resolveId(context, id);
                    } else {
                        final FlagToString[] flagsMapping = property.flagMapping();
                        if (flagsMapping.length > 0) {
                            final int intValue = field.getInt(view);
                            final String valuePrefix = prefix + field.getName() + '_';
                            exportUnrolledFlags(out, flagsMapping, intValue, valuePrefix);
                        }
                        final IntToString[] mapping = property.mapping();
                        if (mapping.length > 0) {
                            final int intValue = field.getInt(view);
                            int mappingCount = mapping.length;
                            for (int j = 0; j < mappingCount; j++) {
                                final IntToString mapped = mapping[j];
                                if (mapped.from() == intValue) {
                                    fieldValue = mapped.to();
                                    break;
                                }
                            }
                            if (fieldValue == null) {
                                fieldValue = intValue;
                            }
                        }
                    }
                } else if (type == int[].class) {
                    final ExportedProperty property = sAnnotations.get(field);
                    final int[] array = (int[]) field.get(view);
                    final String valuePrefix = prefix + field.getName() + '_';
                    final String suffix = "";
                    exportUnrolledArray(context, out, property, array, valuePrefix, suffix);
                    return;
                } else if (!type.isPrimitive()) {
                    final ExportedProperty property = sAnnotations.get(field);
                    if (property.deepExport()) {
                        dumpViewProperties(context, field.get(view), out,
                                prefix + property.prefix());
                        continue;
                    }
                }
                if (fieldValue == null) {
                    fieldValue = field.get(view);
                }
                writeEntry(out, prefix, field.getName(), "", fieldValue);
            } catch (IllegalAccessException e) {
            }
        }
    }
    private static void writeEntry(BufferedWriter out, String prefix, String name,
            String suffix, Object value) throws IOException {
        out.write(prefix);
        out.write(name);
        out.write(suffix);
        out.write("=");
        writeValue(out, value);
        out.write(' ');
    }
    private static void exportUnrolledFlags(BufferedWriter out, FlagToString[] mapping,
            int intValue, String prefix) throws IOException {
        final int count = mapping.length;
        for (int j = 0; j < count; j++) {
            final FlagToString flagMapping = mapping[j];
            final boolean ifTrue = flagMapping.outputIf();
            final int maskResult = intValue & flagMapping.mask();
            final boolean test = maskResult == flagMapping.equals();
            if ((test && ifTrue) || (!test && !ifTrue)) {
                final String name = flagMapping.name();
                final String value = "0x" + Integer.toHexString(maskResult);
                writeEntry(out, prefix, name, "", value);
            }
        }
    }
    private static void exportUnrolledArray(Context context, BufferedWriter out,
            ExportedProperty property, int[] array, String prefix, String suffix)
            throws IOException {
        final IntToString[] indexMapping = property.indexMapping();
        final boolean hasIndexMapping = indexMapping.length > 0;
        final IntToString[] mapping = property.mapping();
        final boolean hasMapping = mapping.length > 0;
        final boolean resolveId = property.resolveId() && context != null;
        final int valuesCount = array.length;
        for (int j = 0; j < valuesCount; j++) {
            String name;
            String value = null;
            final int intValue = array[j];
            name = String.valueOf(j);
            if (hasIndexMapping) {
                int mappingCount = indexMapping.length;
                for (int k = 0; k < mappingCount; k++) {
                    final IntToString mapped = indexMapping[k];
                    if (mapped.from() == j) {
                        name = mapped.to();
                        break;
                    }
                }
            }
            if (hasMapping) {
                int mappingCount = mapping.length;
                for (int k = 0; k < mappingCount; k++) {
                    final IntToString mapped = mapping[k];
                    if (mapped.from() == intValue) {
                        value = mapped.to();
                        break;
                    }
                }
            }
            if (resolveId) {
                if (value == null) value = (String) resolveId(context, intValue);
            } else {
                value = String.valueOf(intValue);
            }
            writeEntry(out, prefix, name, suffix, value);
        }
    }
    static Object resolveId(Context context, int id) {
        Object fieldValue;
        final Resources resources = context.getResources();
        if (id >= 0) {
            try {
                fieldValue = resources.getResourceTypeName(id) + '/' +
                        resources.getResourceEntryName(id);
            } catch (Resources.NotFoundException e) {
                fieldValue = "id/0x" + Integer.toHexString(id);
            }
        } else {
            fieldValue = "NO_ID";
        }
        return fieldValue;
    }
    private static void writeValue(BufferedWriter out, Object value) throws IOException {
        if (value != null) {
            String output = value.toString().replace("\n", "\\n");
            out.write(String.valueOf(output.length()));
            out.write(",");
            out.write(output);
        } else {
            out.write("4,null");
        }
    }
    private static void dumpViewHierarchy(ViewGroup group, BufferedWriter out, int level) {
        if (!dumpView(group, out, level)) {
            return;
        }
        final int count = group.getChildCount();
        for (int i = 0; i < count; i++) {
            final View view = group.getChildAt(i);
            if (view instanceof ViewGroup) {
                dumpViewHierarchy((ViewGroup) view, out, level + 1);
            } else {
                dumpView(view, out, level + 1);
            }
        }
    }
    private static boolean dumpView(Object view, BufferedWriter out, int level) {
        try {
            for (int i = 0; i < level; i++) {
                out.write(' ');
            }
            out.write(view.getClass().getName());
            out.write('@');
            out.write(Integer.toHexString(view.hashCode()));
            out.newLine();
        } catch (IOException e) {
            Log.w("View", "Error while dumping hierarchy tree");
            return false;
        }
        return true;
    }
    private static Field[] capturedViewGetPropertyFields(Class<?> klass) {
        if (mCapturedViewFieldsForClasses == null) {
            mCapturedViewFieldsForClasses = new HashMap<Class<?>, Field[]>();
        }
        final HashMap<Class<?>, Field[]> map = mCapturedViewFieldsForClasses;
        Field[] fields = map.get(klass);
        if (fields != null) {
            return fields;
        }
        final ArrayList<Field> foundFields = new ArrayList<Field>();
        fields = klass.getFields();
        int count = fields.length;
        for (int i = 0; i < count; i++) {
            final Field field = fields[i];
            if (field.isAnnotationPresent(CapturedViewProperty.class)) {
                field.setAccessible(true);
                foundFields.add(field);
            }
        }
        fields = foundFields.toArray(new Field[foundFields.size()]);
        map.put(klass, fields);
        return fields;
    }
    private static Method[] capturedViewGetPropertyMethods(Class<?> klass) {
        if (mCapturedViewMethodsForClasses == null) {
            mCapturedViewMethodsForClasses = new HashMap<Class<?>, Method[]>();
        }
        final HashMap<Class<?>, Method[]> map = mCapturedViewMethodsForClasses;
        Method[] methods = map.get(klass);
        if (methods != null) {
            return methods;
        }
        final ArrayList<Method> foundMethods = new ArrayList<Method>();
        methods = klass.getMethods();
        int count = methods.length;
        for (int i = 0; i < count; i++) {
            final Method method = methods[i];
            if (method.getParameterTypes().length == 0 &&
                    method.isAnnotationPresent(CapturedViewProperty.class) &&
                    method.getReturnType() != Void.class) {
                method.setAccessible(true);
                foundMethods.add(method);
            }
        }
        methods = foundMethods.toArray(new Method[foundMethods.size()]);
        map.put(klass, methods);
        return methods;
    }
    private static String capturedViewExportMethods(Object obj, Class<?> klass,
            String prefix) {
        if (obj == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder();
        final Method[] methods = capturedViewGetPropertyMethods(klass);
        int count = methods.length;
        for (int i = 0; i < count; i++) {
            final Method method = methods[i];
            try {
                Object methodValue = method.invoke(obj, (Object[]) null);
                final Class<?> returnType = method.getReturnType();
                CapturedViewProperty property = method.getAnnotation(CapturedViewProperty.class);
                if (property.retrieveReturn()) {
                    sb.append(capturedViewExportMethods(methodValue, returnType, method.getName() + "#"));
                } else {
                    sb.append(prefix);
                    sb.append(method.getName());
                    sb.append("()=");
                    if (methodValue != null) {
                        final String value = methodValue.toString().replace("\n", "\\n");
                        sb.append(value);
                    } else {
                        sb.append("null");
                    }
                    sb.append("; ");
                }
              } catch (IllegalAccessException e) {
              } catch (InvocationTargetException e) {
              }
        }
        return sb.toString();
    }
    private static String capturedViewExportFields(Object obj, Class<?> klass, String prefix) {
        if (obj == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder();
        final Field[] fields = capturedViewGetPropertyFields(klass);
        int count = fields.length;
        for (int i = 0; i < count; i++) {
            final Field field = fields[i];
            try {
                Object fieldValue = field.get(obj);
                sb.append(prefix);
                sb.append(field.getName());
                sb.append("=");
                if (fieldValue != null) {
                    final String value = fieldValue.toString().replace("\n", "\\n");
                    sb.append(value);
                } else {
                    sb.append("null");
                }
                sb.append(' ');
            } catch (IllegalAccessException e) {
            }
        }
        return sb.toString();
    }
    public static void dumpCapturedView(String tag, Object view) {
        Class<?> klass = view.getClass();
        StringBuilder sb = new StringBuilder(klass.getName() + ": ");
        sb.append(capturedViewExportFields(view, klass, ""));
        sb.append(capturedViewExportMethods(view, klass, ""));
        Log.d(tag, sb.toString());
    }
}
