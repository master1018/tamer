public abstract class ImageGenerator
{
    public int width;
    public int height;
    private final BufferedImage bi;
    public ImageGenerator(int _width, int _height, Color bgColor)
    {
          width = _width;
          height = _height;
          bi = new BufferedImage(
              width,
              height,
              BufferedImage.TYPE_INT_ARGB);
          Graphics gr = bi.getGraphics();
          if(null==bgColor){
              bgColor = Color.WHITE;
          }
          gr.setColor(bgColor);
          gr.fillRect(0, 0, width, height);
          paint(gr);
          gr.dispose();
    }
    public Image getImage() { return bi; }
    public abstract void paint(Graphics gr);
}
