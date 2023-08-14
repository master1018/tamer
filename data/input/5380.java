public class ToggleActionPropertyChangeListener
    implements PropertyChangeListener
{
    public ToggleActionPropertyChangeListener(AbstractButton button)
    {
        this.button = button;
    }
    public void propertyChange(PropertyChangeEvent evt)
    {
        String propertyName = evt.getPropertyName();
        if(propertyName.equals("selected"))
        {
            Boolean selected = (Boolean)evt.getNewValue();
            button.setSelected(selected.booleanValue());
        }
    }
    private AbstractButton button;
}
