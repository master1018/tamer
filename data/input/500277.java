public class CameraButtonIntentReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        CameraHolder holder = CameraHolder.instance();
        if (holder.tryOpen() == null) return;
        holder.keep();
        holder.release();
        Intent i = new Intent(Intent.ACTION_MAIN);
        i.setClass(context, Camera.class);
        i.addCategory(Intent.CATEGORY_LAUNCHER);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(i);
    }
}
