class AppletImageRef extends sun.misc.Ref {
    URL url;
    AppletImageRef(URL url) {
        this.url = url;
    }
    public void flush() {
        super.flush();
    }
    public Object reconstitute() {
        Image img = Toolkit.getDefaultToolkit().createImage(new URLImageSource(url));
        return img;
    }
}
