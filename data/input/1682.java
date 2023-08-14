public class EmptyAttrString {
    public static void main(String[] args) {
        BufferedImage bi =
           new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bi.createGraphics();
        Font f = new Font( "Dialog", Font.PLAIN, 12 );
        Map map = new HashMap();
        map.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
        f = f.deriveFont(map);
        g.setFont(f);
        g.drawString("", 50, 50);
        g.drawString("", 50f, 50f);
        char[] chs = { } ;
        g.drawChars(chs, 0, 0, 50, 50);
        byte[] bytes = { } ;
        g.drawBytes(bytes, 0, 0, 50, 50);
        AttributedString astr = new AttributedString("");
        g.drawString(astr.getIterator(), 50, 50);
        g.drawString(astr.getIterator(), 50f, 50f);
        return;
    }
}
