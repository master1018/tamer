public class BasicColorChooserUI extends ColorChooserUI
{
    protected JColorChooser chooser;
    JTabbedPane tabbedPane;
    JPanel singlePanel;
    JPanel previewPanelHolder;
    JComponent previewPanel;
    boolean isMultiPanel = false;
    private static TransferHandler defaultTransferHandler = new ColorTransferHandler();
    protected AbstractColorChooserPanel[] defaultChoosers;
    protected ChangeListener previewListener;
    protected PropertyChangeListener propertyChangeListener;
    private Handler handler;
    public static ComponentUI createUI(JComponent c) {
        return new BasicColorChooserUI();
    }
    protected AbstractColorChooserPanel[] createDefaultChoosers() {
        AbstractColorChooserPanel[] panels = ColorChooserComponentFactory.getDefaultChooserPanels();
        return panels;
    }
    protected void uninstallDefaultChoosers() {
        AbstractColorChooserPanel[] choosers = chooser.getChooserPanels();
        for( int i = 0 ; i < choosers.length; i++) {
            chooser.removeChooserPanel( choosers[i] );
        }
    }
    public void installUI( JComponent c ) {
        chooser = (JColorChooser)c;
        super.installUI( c );
        installDefaults();
        installListeners();
        tabbedPane = new JTabbedPane();
        tabbedPane.setName("ColorChooser.tabPane");
        tabbedPane.setInheritsPopupMenu(true);
        singlePanel = new JPanel(new CenterLayout());
        singlePanel.setName("ColorChooser.panel");
        singlePanel.setInheritsPopupMenu(true);
        chooser.setLayout( new BorderLayout() );
        defaultChoosers = createDefaultChoosers();
        chooser.setChooserPanels(defaultChoosers);
        previewPanelHolder = new JPanel(new CenterLayout());
        previewPanelHolder.setName("ColorChooser.previewPanelHolder");
        if (DefaultLookup.getBoolean(chooser, this,
                                  "ColorChooser.showPreviewPanelText", true)) {
            String previewString = UIManager.getString(
                "ColorChooser.previewText", chooser.getLocale());
            previewPanelHolder.setBorder(new TitledBorder(previewString));
        }
        previewPanelHolder.setInheritsPopupMenu(true);
        installPreviewPanel();
        chooser.applyComponentOrientation(c.getComponentOrientation());
    }
    public void uninstallUI( JComponent c ) {
        chooser.remove(tabbedPane);
        chooser.remove(singlePanel);
        chooser.remove(previewPanelHolder);
        uninstallDefaultChoosers();
        uninstallListeners();
        uninstallPreviewPanel();
        uninstallDefaults();
        previewPanelHolder = null;
        previewPanel = null;
        defaultChoosers = null;
        chooser = null;
        tabbedPane = null;
        handler = null;
    }
    protected void installPreviewPanel() {
        JComponent previewPanel = this.chooser.getPreviewPanel();
        if (previewPanel == null) {
            previewPanel = ColorChooserComponentFactory.getPreviewPanel();
        }
        else if (JPanel.class.equals(previewPanel.getClass()) && (0 == previewPanel.getComponentCount())) {
            previewPanel = null;
        }
        this.previewPanel = previewPanel;
        if (previewPanel != null) {
            chooser.add(previewPanelHolder, BorderLayout.SOUTH);
            previewPanel.setForeground(chooser.getColor());
            previewPanelHolder.add(previewPanel);
            previewPanel.addMouseListener(getHandler());
            previewPanel.setInheritsPopupMenu(true);
        }
    }
    protected void uninstallPreviewPanel() {
        if (this.previewPanel != null) {
            this.previewPanel.removeMouseListener(getHandler());
            this.previewPanelHolder.remove(this.previewPanel);
        }
        this.chooser.remove(this.previewPanelHolder);
    }
    protected void installDefaults() {
        LookAndFeel.installColorsAndFont(chooser, "ColorChooser.background",
                                              "ColorChooser.foreground",
                                              "ColorChooser.font");
        LookAndFeel.installProperty(chooser, "opaque", Boolean.TRUE);
        TransferHandler th = chooser.getTransferHandler();
        if (th == null || th instanceof UIResource) {
            chooser.setTransferHandler(defaultTransferHandler);
        }
    }
    protected void uninstallDefaults() {
        if (chooser.getTransferHandler() instanceof UIResource) {
            chooser.setTransferHandler(null);
        }
    }
    protected void installListeners() {
        propertyChangeListener = createPropertyChangeListener();
        chooser.addPropertyChangeListener( propertyChangeListener );
        previewListener = getHandler();
        chooser.getSelectionModel().addChangeListener(previewListener);
    }
    private Handler getHandler() {
        if (handler == null) {
            handler = new Handler();
        }
        return handler;
    }
    protected PropertyChangeListener createPropertyChangeListener() {
        return getHandler();
    }
    protected void uninstallListeners() {
        chooser.removePropertyChangeListener( propertyChangeListener );
        chooser.getSelectionModel().removeChangeListener(previewListener);
        previewListener = null;
    }
    private void selectionChanged(ColorSelectionModel model) {
        JComponent previewPanel = this.chooser.getPreviewPanel();
        if (previewPanel != null) {
            previewPanel.setForeground(model.getSelectedColor());
            previewPanel.repaint();
        }
        AbstractColorChooserPanel[] panels = this.chooser.getChooserPanels();
        if (panels != null) {
            for (AbstractColorChooserPanel panel : panels) {
                if (panel != null) {
                    panel.updateChooser();
                }
            }
        }
    }
    private class Handler implements ChangeListener, MouseListener,
            PropertyChangeListener {
        public void stateChanged(ChangeEvent evt) {
            selectionChanged((ColorSelectionModel) evt.getSource());
        }
        public void mousePressed(MouseEvent evt) {
            if (chooser.getDragEnabled()) {
                TransferHandler th = chooser.getTransferHandler();
                th.exportAsDrag(chooser, evt, TransferHandler.COPY);
            }
        }
        public void mouseReleased(MouseEvent evt) {}
        public void mouseClicked(MouseEvent evt) {}
        public void mouseEntered(MouseEvent evt) {}
        public void mouseExited(MouseEvent evt) {}
        public void propertyChange(PropertyChangeEvent evt) {
            String prop = evt.getPropertyName();
            if (prop == JColorChooser.CHOOSER_PANELS_PROPERTY) {
                AbstractColorChooserPanel[] oldPanels =
                    (AbstractColorChooserPanel[])evt.getOldValue();
                AbstractColorChooserPanel[] newPanels =
                    (AbstractColorChooserPanel[])evt.getNewValue();
                for (int i = 0; i < oldPanels.length; i++) {  
                   Container wrapper = oldPanels[i].getParent();
                    if (wrapper != null) {
                      Container parent = wrapper.getParent();
                      if (parent != null)
                          parent.remove(wrapper);  
                      oldPanels[i].uninstallChooserPanel(chooser); 
                    }
                }
                int numNewPanels = newPanels.length;
                if (numNewPanels == 0) {  
                    chooser.remove(tabbedPane);
                    return;
                }
                else if (numNewPanels == 1) {  
                    chooser.remove(tabbedPane);
                    JPanel centerWrapper = new JPanel( new CenterLayout() );
                    centerWrapper.setInheritsPopupMenu(true);
                    centerWrapper.add(newPanels[0]);
                    singlePanel.add(centerWrapper, BorderLayout.CENTER);
                    chooser.add(singlePanel);
                }
                else {   
                    if ( oldPanels.length < 2 ) {
                        chooser.remove(singlePanel);
                        chooser.add(tabbedPane, BorderLayout.CENTER);
                    }
                    for (int i = 0; i < newPanels.length; i++) {
                        JPanel centerWrapper = new JPanel( new CenterLayout() );
                        centerWrapper.setInheritsPopupMenu(true);
                        String name = newPanels[i].getDisplayName();
                        int mnemonic = newPanels[i].getMnemonic();
                        centerWrapper.add(newPanels[i]);
                        tabbedPane.addTab(name, centerWrapper);
                        if (mnemonic > 0) {
                            tabbedPane.setMnemonicAt(i, mnemonic);
                            int index = newPanels[i].getDisplayedMnemonicIndex();
                            if (index >= 0) {
                                tabbedPane.setDisplayedMnemonicIndexAt(i, index);
                            }
                        }
                    }
                }
                chooser.applyComponentOrientation(chooser.getComponentOrientation());
                for (int i = 0; i < newPanels.length; i++) {
                    newPanels[i].installChooserPanel(chooser);
                }
            }
            else if (prop == JColorChooser.PREVIEW_PANEL_PROPERTY) {
                uninstallPreviewPanel();
                installPreviewPanel();
            }
            else if (prop == JColorChooser.SELECTION_MODEL_PROPERTY) {
                ColorSelectionModel oldModel = (ColorSelectionModel) evt.getOldValue();
                oldModel.removeChangeListener(previewListener);
                ColorSelectionModel newModel = (ColorSelectionModel) evt.getNewValue();
                newModel.addChangeListener(previewListener);
                selectionChanged(newModel);
            }
            else if (prop == "componentOrientation") {
                ComponentOrientation o =
                    (ComponentOrientation)evt.getNewValue();
                JColorChooser cc = (JColorChooser)evt.getSource();
                if (o != (ComponentOrientation)evt.getOldValue()) {
                    cc.applyComponentOrientation(o);
                    cc.updateUI();
                }
            }
        }
    }
    public class PropertyHandler implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent e) {
            getHandler().propertyChange(e);
        }
    }
    static class ColorTransferHandler extends TransferHandler implements UIResource {
        ColorTransferHandler() {
            super("color");
        }
    }
}
