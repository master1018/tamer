public class XORComposite implements Composite {
    Color xorcolor;
    public XORComposite(Color xorcolor){
        this.xorcolor = xorcolor;
    }
    public CompositeContext createContext(ColorModel srcCM, ColorModel dstCM,
            RenderingHints hints) {
        return new ICompositeContext(this, srcCM, dstCM);
    }
    public Color getXORColor(){
        return xorcolor;
    }
}
