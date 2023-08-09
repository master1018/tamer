class SynthComboPopup extends BasicComboPopup {
    public SynthComboPopup( JComboBox combo ) {
        super(combo);
    }
    protected void configureList() {
        list.setFont( comboBox.getFont() );
        list.setCellRenderer( comboBox.getRenderer() );
        list.setFocusable( false );
        list.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
        int selectedIndex = comboBox.getSelectedIndex();
        if ( selectedIndex == -1 ) {
            list.clearSelection();
        }
        else {
            list.setSelectedIndex( selectedIndex );
            list.ensureIndexIsVisible( selectedIndex );
        }
        installListListeners();
    }
}
