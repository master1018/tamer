public abstract class ComboBoxUI extends ComponentUI {
    public abstract void setPopupVisible( JComboBox c, boolean v );
    public abstract boolean isPopupVisible( JComboBox c );
    public abstract boolean isFocusTraversable( JComboBox c );
}
