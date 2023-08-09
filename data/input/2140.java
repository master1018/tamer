public class OpacityChange {
    private final static int INITIAL_ALPHA = 125;
    public static void main(String argv[]) {
        Color color = new Color(20, 20, 20, INITIAL_ALPHA);
        System.out.println("Initial alpha: " + color.getAlpha());
        Color colorBrighter = color.brighter();
        System.out.println("New alpha (after brighter): " + colorBrighter.getAlpha());
        Color colorDarker = color.darker();
        System.out.println("New alpha (after darker): " + colorDarker.getAlpha());
        if (INITIAL_ALPHA != colorBrighter.getAlpha()) {
            throw new RuntimeException("Brighter color alpha has changed from : " +INITIAL_ALPHA + " to " + colorBrighter.getAlpha());
        }
        if (INITIAL_ALPHA != colorDarker.getAlpha()) {
            throw new RuntimeException("Darker color alpha has changed from : " +INITIAL_ALPHA + " to " + colorDarker.getAlpha());
        }
    }
}
