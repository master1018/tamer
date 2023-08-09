public class VMInternalFrame extends MaximizableInternalFrame {
    private VMPanel vmPanel;
    public VMInternalFrame(VMPanel vmPanel) {
        super("", true, true, true, true);
        this.vmPanel = vmPanel;
        setAccessibleDescription(this,
                                 getText("VMInternalFrame.accessibleDescription"));
        getContentPane().add(vmPanel, BorderLayout.CENTER);
        pack();
        vmPanel.updateFrameTitle();
    }
    public VMPanel getVMPanel() {
        return vmPanel;
    }
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        JDesktopPane desktop = getDesktopPane();
        if (desktop != null) {
            Dimension desktopSize = desktop.getSize();
            if (desktopSize.width > 0 && desktopSize.height > 0) {
                d.width  = Math.min(desktopSize.width  - 40, d.width);
                d.height = Math.min(desktopSize.height - 40, d.height);
            }
        }
        return d;
    }
}
