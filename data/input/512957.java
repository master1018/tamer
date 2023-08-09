public class ClipRegion extends Rectangle {
    private final MultiRectArea clip;
    public ClipRegion(final MultiRectArea clip) {
        this.clip = new MultiRectArea(clip);
        setBounds(clip.getBounds());
    }
    public MultiRectArea getClip() {
        return clip;
    }
    @Override
    public String toString() {
        String str = clip.toString();
        int i = str.indexOf('[');
        str = str.substring(i);
        if (clip.getRectCount() == 1) {
            str = str.substring(1, str.length() - 1);
        }
        return getClass().getName() + str;
    }
    public void convertRegion(final Component child, final Component parent) {
        convertRegion(child, clip, parent);
    }
    public void intersect(final Rectangle rect) {
        clip.intersect(rect);
    }
    @Override
    public boolean isEmpty() {
        return clip.isEmpty();
    }
    public static void convertRegion(final Component child,
                                     final MultiRectArea region,
                                     final Component parent) {
        int x = 0, y = 0;
        Component c = child;
        if (c == null) {
            throw new IllegalArgumentException(Messages.getString("awt.51")); 
        }
        region.translate(x, y);
    }
}
