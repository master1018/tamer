public class BasicToolBarSeparatorUI extends BasicSeparatorUI
{
    public static ComponentUI createUI( JComponent c )
    {
        return new BasicToolBarSeparatorUI();
    }
    protected void installDefaults( JSeparator s )
    {
        Dimension size = ( (JToolBar.Separator)s ).getSeparatorSize();
        if ( size == null || size instanceof UIResource )
        {
            JToolBar.Separator sep = (JToolBar.Separator)s;
            size = (Dimension)(UIManager.get("ToolBar.separatorSize"));
            if (size != null) {
                if (sep.getOrientation() == JSeparator.HORIZONTAL) {
                    size = new Dimension(size.height, size.width);
                }
                sep.setSeparatorSize(size);
            }
        }
    }
    public void paint( Graphics g, JComponent c )
    {
    }
    public Dimension getPreferredSize( JComponent c )
    {
        Dimension size = ( (JToolBar.Separator)c ).getSeparatorSize();
        if ( size != null )
        {
            return size.getSize();
        }
        else
        {
            return null;
        }
    }
}
