public class MetalPopupMenuSeparatorUI extends MetalSeparatorUI
{
    public static ComponentUI createUI( JComponent c )
    {
        return new MetalPopupMenuSeparatorUI();
    }
    public void paint( Graphics g, JComponent c )
    {
        Dimension s = c.getSize();
        g.setColor( c.getForeground() );
        g.drawLine( 0, 1, s.width, 1 );
        g.setColor( c.getBackground() );
        g.drawLine( 0, 2, s.width, 2 );
        g.drawLine( 0, 0, 0, 0 );
        g.drawLine( 0, 3, 0, 3 );
    }
    public Dimension getPreferredSize( JComponent c )
    {
        return new Dimension( 0, 4 );
    }
}
