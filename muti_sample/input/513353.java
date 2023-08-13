public class ImageFactory {
    private final Display mDisplay;
    private final HashMap<String, Image> mImages = new HashMap<String, Image>();
    public ImageFactory(Display display) {
        mDisplay = display;
    }
    public Image getImageByName(String imageName) {
        Image image = mImages.get(imageName);
        if (image != null) {
            return image;
        }
        InputStream stream = getClass().getResourceAsStream(imageName);
        if (stream != null) {
            try {
                image = new Image(mDisplay, stream);
            } catch (SWTException e) {
            } catch (IllegalArgumentException e) {
            }
        }
        mImages.put(imageName, image);
        return image;
    }
    public Image getImageForObject(Object object) {
        if (object == null) {
            return null;
        }
        String clz = object.getClass().getSimpleName();
        if (clz.endsWith(Package.class.getSimpleName())) {
            String name = clz.replaceFirst(Package.class.getSimpleName(), "").toLowerCase() + 
                            "_pkg_16.png";                                      
            return getImageByName(name);
        }
        if (object instanceof RepoSource) {
            return getImageByName("source_icon16.png");                         
        } else if (object instanceof RepoSourcesAdapter.RepoSourceError) {
            return getImageByName("error_icon16.png");                          
        } else if (object instanceof RepoSourcesAdapter.RepoSourceEmpty) {
            return getImageByName("nopkg_icon16.png");                          
        }
        if (object instanceof Archive) {
            if (((Archive) object).isCompatible()) {
                return getImageByName("archive_icon16.png");                    
            } else {
                return getImageByName("incompat_icon16.png");                   
            }
        }
        return null;
    }
    public void dispose() {
        Iterator<Image> it = mImages.values().iterator();
        while(it.hasNext()) {
            Image img = it.next();
            if (img != null && img.isDisposed() == false) {
                img.dispose();
            }
            it.remove();
        }
    }
}
