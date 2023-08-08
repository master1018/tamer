public class DockingPanel extends JPanel {
    private String panelTitle = "";
    private String panelLongTitle = "";
    private String panelDescription = "";
    private Icon panelSmallIcon = null;
    private Icon panelLargeIcon = null;
    protected DockingPanelContainer panelContainer = null;
    public DockingPanel() {
        super();
    }
    public String getTitle() {
        return panelTitle;
    }
    public String getLongTitle() {
        return panelLongTitle;
    }
    public String getDescription() {
        return panelDescription;
    }
    public Icon getSmallIcon() {
        return panelSmallIcon;
    }
    public Icon getLargeIcon() {
        return panelLargeIcon;
    }
    public DockingPanelContainer getContainer() {
        return panelContainer;
    }
    public JMenuItem[] getMenuItems() {
        return null;
    }
    protected void setTitle(String title) {
        if (title != null) {
            panelTitle = title;
        }
        if (panelContainer != null) {
            panelContainer.panelInformationUpdated(this);
        }
    }
    protected void setLongTitle(String longTitle) {
        if (panelLongTitle != null) {
            panelLongTitle = longTitle;
        }
        if (panelContainer != null) {
            panelContainer.panelInformationUpdated(this);
        }
    }
    protected void setDescription(String desc) {
        if (desc != null) {
            panelDescription = desc;
        }
        if (panelContainer != null) {
            panelContainer.panelInformationUpdated(this);
        }
    }
    protected void setSmallIcon(Icon icon) {
        if (icon != null) {
            panelSmallIcon = icon;
        }
        if (panelContainer != null) {
            panelContainer.panelInformationUpdated(this);
        }
    }
    protected void setLargeIcon(Icon icon) {
        if (icon != null) {
            panelLargeIcon = icon;
        }
        if (panelContainer != null) {
            panelContainer.panelInformationUpdated(this);
        }
    }
    public void setContainer(DockingPanelContainer container) {
        panelContainer = container;
    }
}
