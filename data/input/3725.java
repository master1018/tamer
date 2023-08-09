public class x_xbitmap extends ContentHandler {
    public Object getContent(URLConnection urlc) throws java.io.IOException {
        return new URLImageSource(urlc);
    }
    public Object getContent(URLConnection urlc, Class[] classes) throws java.io.IOException {
        for (int i = 0; i < classes.length; i++) {
          if (classes[i].isAssignableFrom(URLImageSource.class)) {
                return new URLImageSource(urlc);
          }
          if (classes[i].isAssignableFrom(Image.class)) {
            Toolkit tk = Toolkit.getDefaultToolkit();
            return tk.createImage(new URLImageSource(urlc));
          }
        }
        return null;
    }
}
