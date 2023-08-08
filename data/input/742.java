public abstract class PagedExporter extends AbstractAction {
    protected Component parent;
    protected HieroglyphicTextModel data;
    protected Properties properties;
    double scale;
    public PagedExporter(HieroglyphicTextModel data, Component parent, String name) {
        super(name);
        this.data = data;
        this.parent = parent;
        properties = new Properties();
        scale = 1.0;
    }
    public void actionPerformed(ActionEvent e) {
        try {
            JFileChooser chooser = new JFileChooser();
            ExportFileType export = getExportFileType();
            JPanel panel = export.createOptionPanel(properties);
            int rc = JOptionPane.showConfirmDialog(parent, panel, getOptionsTitle(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (rc == JOptionPane.OK_OPTION) {
                export.applyChangedOptions(panel, properties);
            } else {
                return;
            }
            chooser.setFileFilter(export.getFileFilter());
            int returnval = chooser.showSaveDialog(parent);
            if (returnval != JFileChooser.APPROVE_OPTION) {
                return;
            }
            File outputFile = chooser.getSelectedFile();
            if (!export.fileHasValidExtension(outputFile)) {
                export.adjustFilename(outputFile, properties);
            }
            if (outputFile.exists()) {
                returnval = JOptionPane.showConfirmDialog(parent, Messages.getString("PagedExporter.File") + outputFile.getName() + Messages.getString("PagedExporter.exists.continue"), Messages.getString("PagedExporter.file.exists"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                if (returnval == JOptionPane.CANCEL_OPTION) return;
            }
            BaseGraphics2D g = getGraphics(outputFile);
            g.setProperties(properties);
            Dimension d = getDimension();
            PageFormat pageFormat = new PageFormat();
            Paper p = new Paper();
            p.setSize(d.getWidth(), d.getHeight());
            p.setImageableArea(0, 0, d.getWidth(), d.getHeight());
            pageFormat.setPaper(p);
            ViewPrinter printer = new ViewPrinter();
            printer.setModel(data.getModel());
            printer.setPageformat(pageFormat);
            Dimension dimension = new Dimension((int) pageFormat.getImageableWidth(), (int) pageFormat.getImageableWidth());
            g.scale(scale, scale);
            for (int i = 0; i < printer.getNumberOfPages(); i++) {
                try {
                    g.openPage(dimension, Messages.getString("PagedExporter.page") + (i + 1));
                    printer.print(g, pageFormat, i);
                } catch (PrinterException e2) {
                    e2.printStackTrace();
                } catch (BaseGraphics2DException e1) {
                    e1.printStackTrace();
                } finally {
                    try {
                        g.closePage();
                    } catch (BaseGraphics2DException e3) {
                        e3.printStackTrace();
                    }
                }
            }
            g.dispose();
        } catch (HeadlessException e1) {
            e1.printStackTrace();
        } catch (FileNotFoundException e1) {
            JOptionPane.showMessageDialog(parent, Messages.getString("PagedExporter.cant.open.file"), Messages.getString("PagedExporter.error"), JOptionPane.ERROR_MESSAGE);
        } catch (OutOfMemoryError err) {
            JOptionPane.showMessageDialog(parent, Messages.getString("PagedExporter.out.of.memory"), Messages.getString("PagedExporter.error"), JOptionPane.ERROR_MESSAGE);
        }
    }
    public abstract ExportFileType getExportFileType();
    protected abstract BaseGraphics2D getGraphics(File file) throws FileNotFoundException;
    protected abstract String getOptionsTitle();
    protected abstract Dimension getDimension();
    public double getScale() {
        return scale;
    }
    public void setScale(double d) {
        scale = d;
    }
    public Properties getProperties() {
        return properties;
    }
    public void setProperties(Properties properties) {
        this.properties = properties;
    }
}
