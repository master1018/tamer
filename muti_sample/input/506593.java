public class UnderdevelopedSettings extends LauncherActivity {
    @Override
    protected Intent getTargetIntent() {
        Intent targetIntent = new Intent(Intent.ACTION_MAIN, null);
        targetIntent.addCategory(Intent.CATEGORY_DEVELOPMENT_PREFERENCE);
        return targetIntent;
    }
}
