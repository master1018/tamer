public class QuakeViewNoData extends View {
    public QuakeViewNoData(Context context, int reason)
    {
        super(context);
        mReason = reason;
    }
    public static final int E_NODATA = 1;
    public static final int E_INITFAILED = 2;
    @Override
    protected void onDraw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(0xffffffff);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
        paint.setColor(0xff000000);
        switch(mReason)
        {
            case E_NODATA:
                canvas.drawText("Missing data files. Looking for one of:",
                        10.0f, 20.0f, paint);
                canvas.drawText("/sdcard/data/quake/id1/pak0.pak",
                        10.0f, 35.0f, paint);
                canvas.drawText("/data/quake/id1/pak0.pak",
                        10.0f, 50.0f, paint);
                canvas.drawText("Please copy a pak file to the device and reboot.",
                        10.0f, 65.0f, paint);
                break;
            case E_INITFAILED:
                canvas.drawText("Quake C library initialization failed.",
                        10.0f, 20.0f, paint);
                canvas.drawText("Try stopping and restarting the simulator.",
                        10.0f, 35.0f, paint);
                break;
        }
    }
    private int mReason;
}
