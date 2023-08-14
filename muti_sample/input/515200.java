public class NoSystemFunctionPermissionTest extends AndroidTestCase {
    @SmallTest
    public void testRestartPackage() {
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(
                Context.ACTIVITY_SERVICE);
        try {
            activityManager.restartPackage("packageName");
            fail("ActivityManager.restartPackage() didn't throw SecurityException as expected.");
        } catch (SecurityException e) {
        }
    }
    @SmallTest
    public void testSetTimeZone() {
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(
                Context.ALARM_SERVICE);
        String[] timeZones = TimeZone.getAvailableIDs();
        String timeZone = timeZones[0];
        try {
            alarmManager.setTimeZone(timeZone);
            fail("AlarmManager.setTimeZone() did not throw SecurityException as expected.");
        } catch (SecurityException e) {
        }
    }
    @SmallTest
    public void testSetWallpaper() throws IOException {
        Bitmap bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.RGB_565);
        try {
            mContext.setWallpaper(bitmap);
            fail("Context.setWallpaper(BitMap) did not throw SecurityException as expected.");
        } catch (SecurityException e) {
        }
        try {
            mContext.setWallpaper((InputStream) null);
            fail("Context.setWallpaper(InputStream) did not throw SecurityException as expected.");
        } catch (SecurityException e) {
        }
        try {
            mContext.clearWallpaper();
            fail("Context.clearWallpaper() did not throw SecurityException as expected.");
        } catch (SecurityException e) {
        }
    }
    @SmallTest
    public void testVibrator() {
        Vibrator vibrator = (Vibrator)getContext().getSystemService(Context.VIBRATOR_SERVICE);
        try {
            vibrator.cancel();
            fail("Vibrator.cancel() did not throw SecurityException as expected.");
        } catch (SecurityException e) {
        }
        try {
            vibrator.vibrate(1);
            fail("Vibrator.vibrate(long) did not throw SecurityException as expected.");
        } catch (SecurityException e) {
        }
        long[] testPattern = {1, 1, 1, 1, 1};
        try {
            vibrator.vibrate(testPattern, 1);
            fail("Vibrator.vibrate(long[], int) not throw SecurityException as expected.");
        } catch (SecurityException e) {
        }
    }
    @SmallTest
    public void testSendSms() {
        SmsManager smsManager = SmsManager.getDefault();
        byte[] testData = new byte[10];
        try {
            smsManager.sendDataMessage("1233", "1233", (short) 0, testData, null, null);
            fail("SmsManager.sendDataMessage() did not throw SecurityException as expected.");
        } catch (SecurityException e) {
        }
    }
}
