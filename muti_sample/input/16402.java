class ActionUtilities
{
    ActionUtilities()
    {
    }
    public ImageIcon getIcon(String name)
    {
        String imagePath = "/toolbarButtonGraphics/" + name;
        java.net.URL url = getClass().getResource(imagePath);
        if(url != null)
            return new ImageIcon(url);
        else
            return null;
    }
    public static final String IMAGE_DIR = "/toolbarButtonGraphics/";
}
