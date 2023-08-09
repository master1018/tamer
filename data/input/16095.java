public class VisualBounds {
    public static void main(String args[]) {
        String s1 = "a";
        String s2 = s1+" ";
        String s3 = " "+s1;
        Font f = new Font("Dialog", Font.PLAIN, 12);
        FontRenderContext frc = new FontRenderContext(null, false, false);
        GlyphVector gv1 = f.createGlyphVector(frc, s1);
        GlyphVector gv2 = f.createGlyphVector(frc, s2);
        GlyphVector gv3 = f.createGlyphVector(frc, s3);
        Rectangle2D bds1 = gv1.getVisualBounds();
        Rectangle2D bds2 = gv2.getVisualBounds();
        Rectangle2D bds3 = gv3.getVisualBounds();
        GlyphVector gv4 = f.createGlyphVector(frc, " ");
        Rectangle2D bds4 = gv4.getVisualBounds();
        System.out.println(bds1);
        System.out.println(bds2);
        System.out.println(bds3);
        System.out.println(bds4);
        if (!bds1.equals(bds2)) {
          throw new RuntimeException("Trailing space: Visual bounds differ");
        }
        if (bds2.getWidth() != bds3.getWidth()) {
          throw new RuntimeException("Leading space: Visual widths differ");
       }
        if (!bds4.isEmpty()) {
          throw new RuntimeException("Non empty bounds for space");
       }
    }
}
