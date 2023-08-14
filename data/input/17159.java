public class Bug7002398 {
    private static final int[] directions = {
        Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT,
        Bidi.DIRECTION_DEFAULT_RIGHT_TO_LEFT,
        Bidi.DIRECTION_LEFT_TO_RIGHT,
        Bidi.DIRECTION_RIGHT_TO_LEFT
    };
    private static final String str = "\u0627\u0660\u0710\u070F\u070D";
    private static final int[] expectedLevels = {1, 2, 1, 1, 1};
    public static void main(String[] args) {
        boolean err = false;
        for (int dir = 0; dir < directions.length; dir ++) {
            Bidi bidi = new Bidi(str, directions[dir]);
            for (int index = 0; index < str.length(); index ++) {
                int gotLevel = bidi.getLevelAt(index);
                if (gotLevel != expectedLevels[index]) {
                    err = true;
                    System.err.println("Unexpected level for the character 0x" +
                        Integer.toHexString(str.charAt(index)).toUpperCase() +
                        ": Expected level = " + expectedLevels[index] +
                        ", actual level = " + bidi.getLevelAt(index) +
                        " in direction = " + directions[dir] + ".");
                }
            }
        }
        if (err) {
            throw new RuntimeException("Failed.");
        }
    }
}
