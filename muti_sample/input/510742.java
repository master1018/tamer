public abstract class CommonGraphicsEnvironment extends GraphicsEnvironment {
    @Override
    public Graphics2D createGraphics(BufferedImage bufferedImage) {
        return new BufferedImageGraphics2D(bufferedImage);
    }
    @Override
    public String[] getAvailableFontFamilyNames(Locale locale) {
        Font[] fonts = getAllFonts();
        ArrayList<String> familyNames = new ArrayList<String>();
        for (Font element : fonts) {
            String name = element.getFamily(locale);
            if (!familyNames.contains(name)) {
                familyNames.add(name);
            }
        }
        return familyNames.toArray(new String[familyNames.size()]);
    }
    @Override
    public Font[] getAllFonts() {
        return CommonGraphics2DFactory.inst.getFontManager().getAllFonts();
    }
    @Override
    public String[] getAvailableFontFamilyNames() {
        return CommonGraphics2DFactory.inst.getFontManager().getAllFamilies();
    }
}
