public class BasicPopupMenuSeparatorUI extends BasicSeparatorUI
{
    public static ComponentUI createUI( JComponent c )
    {
        return new BasicPopupMenuSeparatorUI();
    }
    public void paint( Graphics g, JComponent c )
    {
        Dimension s = c.getSize();
        g.setColor( c.getForeground() );
        g.drawLine( 0, 0, s.width, 0 );
        g.setColor( c.getBackground() );
        g.drawLine( 0, 1, s.width, 1 );
    }
    public Dimension getPreferredSize( JComponent c )
    {
        return new Dimension( 0, 2 );
    }
}
