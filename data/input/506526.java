public class PsdFileFilter extends FileFilter {
    @Override
    public boolean accept(File f) {
        return f.isDirectory() || f.getName().toLowerCase().endsWith(".psd");
    }
    @Override
    public String getDescription() {
        return "Photoshop Document (*.psd)";
    }
}