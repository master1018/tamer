public class TextLayoutBounds {
    public static void main(String args[]) {
       FontRenderContext frc = new FontRenderContext(null, false, false);
       Font f = new Font("SansSerif",Font.BOLD,32);
       String s = new String("JAVA");
       TextLayout tl = new TextLayout(s, f, frc);
       Rectangle2D tlBounds = tl.getBounds();
       GlyphVector gv = f.createGlyphVector(frc, s);
       Rectangle2D gvvBounds = gv.getVisualBounds();
       Rectangle2D oBounds = tl.getOutline(null).getBounds2D();
       System.out.println("tlbounds="+tlBounds);
       System.out.println("gvbounds="+gvvBounds);
       System.out.println("outlineBounds="+oBounds);
       if (!gvvBounds.equals(tlBounds)) {
          throw new RuntimeException("Bounds differ [gvv != tl]");
       }
       if (!tlBounds.equals(oBounds)) {
          throw new RuntimeException("Bounds differ [tl != outline]");
       }
    }
}
