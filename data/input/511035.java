public class DateTimeKeyListener extends NumberKeyListener
{
    public int getInputType() {
        return InputType.TYPE_CLASS_DATETIME
                | InputType.TYPE_DATETIME_VARIATION_NORMAL;
    }
    @Override
    protected char[] getAcceptedChars()
    {
        return CHARACTERS;
    }
    public static DateTimeKeyListener getInstance() {
        if (sInstance != null)
            return sInstance;
        sInstance = new DateTimeKeyListener();
        return sInstance;
    }
    public static final char[] CHARACTERS = new char[] {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'm',
            'p', ':', '/', '-', ' '
        };
    private static DateTimeKeyListener sInstance;
}
