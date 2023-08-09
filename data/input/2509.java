public abstract class FileChooserUI extends ComponentUI
{
    public abstract FileFilter getAcceptAllFileFilter(JFileChooser fc);
    public abstract FileView getFileView(JFileChooser fc);
    public abstract String getApproveButtonText(JFileChooser fc);
    public abstract String getDialogTitle(JFileChooser fc);
    public abstract void rescanCurrentDirectory(JFileChooser fc);
    public abstract void ensureFileIsVisible(JFileChooser fc, File f);
    public JButton getDefaultButton(JFileChooser fc) {
        return null;
    }
}
