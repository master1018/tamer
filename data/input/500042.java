class PngFileFilter extends FileFilter {
    @Override
    public boolean accept(File f) {
        return f.isDirectory() || f.getName().toLowerCase().endsWith(".png");
    }
    @Override
    public String getDescription() {
        return "PNG Image (*.png)";
    }
}
