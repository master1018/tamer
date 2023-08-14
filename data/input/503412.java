public class InstrumentationTestCase extends TestCase {
    private Instrumentation mInstrumentation;
    public void injectInstrumentation(Instrumentation instrumentation) {
        mInstrumentation = instrumentation;
    }
    @Deprecated
    public void injectInsrumentation(Instrumentation instrumentation) {
        injectInstrumentation(instrumentation);
    }
    public Instrumentation getInstrumentation() {
        return mInstrumentation;
    }
    public final <T extends Activity> T launchActivity(
            String pkg,
            Class<T> activityCls,
            Bundle extras) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        if (extras != null) {
            intent.putExtras(extras);
        }
        return launchActivityWithIntent(pkg, activityCls, intent);
    }
    @SuppressWarnings("unchecked")
    public final <T extends Activity> T launchActivityWithIntent(
            String pkg,
            Class<T> activityCls,
            Intent intent) {
        intent.setClassName(pkg, activityCls.getName());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        T activity = (T) getInstrumentation().startActivitySync(intent);
        getInstrumentation().waitForIdleSync();
        return activity;
    }
    public void runTestOnUiThread(final Runnable r) throws Throwable {
        final Throwable[] exceptions = new Throwable[1];
        getInstrumentation().runOnMainSync(new Runnable() {
            public void run() {
                try {
                    r.run();
                } catch (Throwable throwable) {
                    exceptions[0] = throwable;
                }
            }
        });
        if (exceptions[0] != null) {
            throw exceptions[0];
        }
    }
    @Override
    protected void runTest() throws Throwable {
        String fName = getName();
        assertNotNull(fName);
        Method method = null;
        try {
            method = getClass().getMethod(fName, (Class[]) null);
        } catch (NoSuchMethodException e) {
            fail("Method \""+fName+"\" not found");
        }
        if (!Modifier.isPublic(method.getModifiers())) {
            fail("Method \""+fName+"\" should be public");
        }
        int runCount = 1;
        if (method.isAnnotationPresent(FlakyTest.class)) {
            runCount = method.getAnnotation(FlakyTest.class).tolerance();
        }
        if (method.isAnnotationPresent(UiThreadTest.class)) {
            final int tolerance = runCount;
            final Method testMethod = method;
            final Throwable[] exceptions = new Throwable[1];
            getInstrumentation().runOnMainSync(new Runnable() {
                public void run() {
                    try {
                        runMethod(testMethod, tolerance);
                    } catch (Throwable throwable) {
                        exceptions[0] = throwable;
                    }
                }
            });
            if (exceptions[0] != null) {
                throw exceptions[0];
            }
        } else {
            runMethod(method, runCount);
        }
    }
    private void runMethod(Method runMethod, int tolerance) throws Throwable {
        Throwable exception = null;
        int runCount = 0;
        do {
            try {
                runMethod.invoke(this, (Object[]) null);
                exception = null;
            } catch (InvocationTargetException e) {
                e.fillInStackTrace();
                exception = e.getTargetException();
            } catch (IllegalAccessException e) {
                e.fillInStackTrace();
                exception = e;
            } finally {
                runCount++;
            }
        } while ((runCount < tolerance) && (exception != null));
        if (exception != null) {
            throw exception;
        }
    }
    public void sendKeys(String keysSequence) {
        final String[] keys = keysSequence.split(" ");
        final int count = keys.length;
        final Instrumentation instrumentation = getInstrumentation();
        for (int i = 0; i < count; i++) {
            String key = keys[i];
            int repeater = key.indexOf('*');
            int keyCount;
            try {
                keyCount = repeater == -1 ? 1 : Integer.parseInt(key.substring(0, repeater));
            } catch (NumberFormatException e) {
                Log.w("ActivityTestCase", "Invalid repeat count: " + key);
                continue;
            }
            if (repeater != -1) {
                key = key.substring(repeater + 1);
            }
            for (int j = 0; j < keyCount; j++) {
                try {
                    final Field keyCodeField = KeyEvent.class.getField("KEYCODE_" + key);
                    final int keyCode = keyCodeField.getInt(null);
                    try {
                        instrumentation.sendKeyDownUpSync(keyCode);
                    } catch (SecurityException e) {
                    }
                } catch (NoSuchFieldException e) {
                    Log.w("ActivityTestCase", "Unknown keycode: KEYCODE_" + key);
                    break;
                } catch (IllegalAccessException e) {
                    Log.w("ActivityTestCase", "Unknown keycode: KEYCODE_" + key);
                    break;
                }
            }
        }
        instrumentation.waitForIdleSync();
    }
    public void sendKeys(int... keys) {
        final int count = keys.length;
        final Instrumentation instrumentation = getInstrumentation();
        for (int i = 0; i < count; i++) {
            try {
                instrumentation.sendKeyDownUpSync(keys[i]);
            } catch (SecurityException e) {
            }
        }
        instrumentation.waitForIdleSync();
    }
    public void sendRepeatedKeys(int... keys) {
        final int count = keys.length;
        if ((count & 0x1) == 0x1) {
            throw new IllegalArgumentException("The size of the keys array must "
                    + "be a multiple of 2");
        }
        final Instrumentation instrumentation = getInstrumentation();
        for (int i = 0; i < count; i += 2) {
            final int keyCount = keys[i];
            final int keyCode = keys[i + 1];
            for (int j = 0; j < keyCount; j++) {
                try {
                    instrumentation.sendKeyDownUpSync(keyCode);
                } catch (SecurityException e) {
                }
            }
        }
        instrumentation.waitForIdleSync();
    }
    @Override
    protected void tearDown() throws Exception {
        Runtime.getRuntime().gc();
        Runtime.getRuntime().runFinalization();
        Runtime.getRuntime().gc();
        super.tearDown();
    }
}
