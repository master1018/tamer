    public void setImage(String imageUrl) {
        try {
            InputStream url = ProjectManager.getCurrentProject().getUrl(imageUrl).openStream();
            Image im = new Image(getDisplay(), url);
            if (im != null) item.setImage(im);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }