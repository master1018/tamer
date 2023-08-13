public final class AnimationTestUtils {
    private static final long TIMEOUT_DELTA = 1000;
    private AnimationTestUtils() {
    }
    public static void assertRunAnimation(final Instrumentation instrumentation,
            final View view, final Animation animation) {
        assertRunAnimation(instrumentation, view, animation, animation.getDuration());
    }
    public static void assertRunAnimation(final Instrumentation instrumentation,
            final View view, final Animation animation, final long duration) {
        instrumentation.runOnMainSync(new Runnable() {
            public void run() {
                view.startAnimation(animation);
            }
        });
        new DelayedCheck() {
            @Override
            protected boolean check() {
                return animation.hasStarted();
            }
        }.run();
        new DelayedCheck(duration + TIMEOUT_DELTA) {
            @Override
            protected boolean check() {
                return animation.hasEnded();
            }
        }.run();
    }
    public static void assertRunController(final Instrumentation instrumentation,
            final ViewGroup view, final LayoutAnimationController controller,
            final long duration) throws InterruptedException {
        instrumentation.runOnMainSync(new Runnable() {
           public void run() {
                view.setLayoutAnimation(controller);
                view.requestLayout();
           }
        });
        Thread.sleep(duration + TIMEOUT_DELTA);
    }
}
