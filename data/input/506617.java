public class AndroidCharacter
{
    public static final int EAST_ASIAN_WIDTH_NEUTRAL = 0;
    public static final int EAST_ASIAN_WIDTH_AMBIGUOUS = 1;
    public static final int EAST_ASIAN_WIDTH_HALF_WIDTH = 2;
    public static final int EAST_ASIAN_WIDTH_FULL_WIDTH = 3;
    public static final int EAST_ASIAN_WIDTH_NARROW = 4;
    public static final int EAST_ASIAN_WIDTH_WIDE = 5;
    public native static void getDirectionalities(char[] src, byte[] dest,
                                                  int count);
    public native static int getEastAsianWidth(char input);
    public native static void getEastAsianWidths(char[] src, int start,
                                                 int count, byte[] dest);
    public native static boolean mirror(char[] text, int start, int count);
    public native static char getMirror(char ch);
}
