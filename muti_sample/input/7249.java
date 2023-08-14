public class SupplementaryCanDisplayUpToTest {
    private static String[][] DATA = {
        { "Meiryo", "\ud87e\udd45\ud87e\udd47\udb80\udc00", "4" },
        { "Meiryo", "\ud87e\udd45\ud87e\udd47\udb80Z", "4" },
        { "Meiryo", "\ud87e\udd45\ud87e\udd47\udb80", "4" },
        { "Meiryo", "\ud87e\udd45\ud87e\udd47\udc00", "4" },
        { "Meiryo", "\ud87e\udd45\ud87e\udd47", "-1" },
        { "AR PL UMing TW", "\ud87e\udc25\ud87e\udc3b\udb80\udc00", "4" },
        { "AR PL UMing TW", "\ud87e\udc25\ud87e\udc3b\udb80Z", "4" },
        { "AR PL UMing TW", "\ud87e\udc25\ud87e\udc3b\udb80", "4" },
        { "AR PL UMing TW", "\ud87e\udc25\ud87e\udc3b\udc00", "4" },
        { "AR PL UMing TW", "\ud87e\udc25\ud87e\udc3b", "-1" },
        { "FZMingTi", "\ud87e\udc25\ud87e\udc3b\udb80\udc00", "4" },
        { "FZMingTi", "\ud87e\udc25\ud87e\udc3b\udb80Z", "4" },
        { "FZMingTi", "\ud87e\udc25\ud87e\udc3b\udb80", "4" },
        { "FZMingTi", "\ud87e\udc25\ud87e\udc3b\udc00", "4" },
        { "FZMingTi", "\ud87e\udc25\ud87e\udc3b", "-1" },
    };
    private static int errorcount = 0;
    public static void main(String[] args) {
        for (String[] data : DATA) {
            String fontname = data[0];
            Font font = new Font(fontname, Font.PLAIN, 16);
            if (font.getFamily().equals(Font.DIALOG)) {
                continue;
            }
            System.out.printf("Testing with font '%s'... ", fontname);
            int errors = 0;
            String text = data[1];
            int expected = Integer.parseInt(data[2]);
            int result = font.canDisplayUpTo(text);
            if (result != expected) {
                System.err.println("canDisplayUpTo(String) returns " + result);
                errors++;
            }
            result = font.canDisplayUpTo(text.toCharArray(), 0, text.length());
            if (result != expected) {
                System.err.println("canDisplayUpTo(char[], int, int) returns " + result);
                errors++;
            }
            CharacterIterator iter = new StringCharacterIterator(text);
            result = font.canDisplayUpTo(iter, iter.getBeginIndex(), iter.getEndIndex());
            if (result != expected) {
                System.err.println("canDisplayUpTo(CharacterIterator, int, int) returns " + result);
                errors++;
            }
            if (errors == 0) {
                System.out.println("passed");
            } else {
                System.out.println("failed");
                errorcount += errors;
            }
        }
        if (errorcount > 0) {
            throw new RuntimeException("SupplementaryCanDisplayUpToTest: failed");
        }
    }
}
