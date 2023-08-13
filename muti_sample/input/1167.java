public class NonCharacterMapping {
    private static final Locale ENGLISH = new Locale("en");
    private static final Locale TURKISH = new Locale("tr");
    public static void main(String[] args) {
        if (Character.toLowerCase('\uFFFF') != '\uFFFF') {
            throw new RuntimeException();
        }
        if (Character.toUpperCase('\uFFFF') != '\uFFFF') {
            throw new RuntimeException();
        }
        if (Character.toTitleCase('\uFFFF') != '\uFFFF') {
            throw new RuntimeException();
        }
        if (!"\uFFFF".toLowerCase(ENGLISH).equals("\uFFFF")) {
            throw new RuntimeException();
        }
        if (!"\uFFFF".toUpperCase(ENGLISH).equals("\uFFFF")) {
            throw new RuntimeException();
        }
        if (!"\uFFFF".toLowerCase(TURKISH).equals("\uFFFF")) {
            throw new RuntimeException();
        }
        if (!"\uFFFF".toUpperCase(TURKISH).equals("\uFFFF")) {
            throw new RuntimeException();
        }
        if (!"A\uFFFF".toLowerCase(ENGLISH).equals("a\uFFFF")) {
            throw new RuntimeException();
        }
        if (!"A\uFFFF".toUpperCase(ENGLISH).equals("A\uFFFF")) {
            throw new RuntimeException();
        }
        if (!"A\uFFFF".toLowerCase(TURKISH).equals("a\uFFFF")) {
            throw new RuntimeException();
        }
        if (!"A\uFFFF".toUpperCase(TURKISH).equals("A\uFFFF")) {
            throw new RuntimeException();
        }
        if (!"a\uFFFF".toLowerCase(ENGLISH).equals("a\uFFFF")) {
            throw new RuntimeException();
        }
        if (!"a\uFFFF".toUpperCase(ENGLISH).equals("A\uFFFF")) {
            throw new RuntimeException();
        }
        if (!"a\uFFFF".toLowerCase(TURKISH).equals("a\uFFFF")) {
            throw new RuntimeException();
        }
        if (!"a\uFFFF".toUpperCase(TURKISH).equals("A\uFFFF")) {
            throw new RuntimeException();
        }
    }
}
