public class PresenterFactory {
    private static final String TAG = "PresenterFactory";
    private static final String PRESENTER_PACKAGE = "com.android.mms.ui.";
    public static Presenter getPresenter(String className, Context context,
            ViewInterface view, Model model) {
        try {
            if (className.indexOf(".") == -1) {
                className = PRESENTER_PACKAGE + className;
            }
            Class c = Class.forName(className);
            Constructor constructor = c.getConstructor(
                    Context.class, ViewInterface.class, Model.class);
            return (Presenter) constructor.newInstance(context, view, model);
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "Type not found: " + className, e);
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "No such constructor.", e);
        } catch (InvocationTargetException e) {
            Log.e(TAG, "Unexpected InvocationTargetException", e);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "Unexpected IllegalAccessException", e);
        } catch (InstantiationException e) {
            Log.e(TAG, "Unexpected InstantiationException", e);
        }
        return null;
    }
}
