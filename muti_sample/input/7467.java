public class MetalSeparatorUI extends BasicSeparatorUI
{
    public static ComponentUI createUI( JComponent c )
    {
        return new MetalSeparatorUI();
    }
    protected void installDefaults( JSeparator s )
    {
        LookAndFeel.installColors( s, "Separator.background", "Separator.foreground" );
    }
    public void paint( Graphics g, JComponent c )
    {
        Dimension s = c.getSize();
        if ( ((JSeparator)c).getOrientation() == JSeparator.VERTICAL )
        {
          g.setColor( c.getForeground() );
          g.drawLine( 0, 0, 0, s.height );
          g.setColor( c.getBackground() );
          g.drawLine( 1, 0, 1, s.height );
        }
        else  
        {
          g.setColor( c.getForeground() );
          g.drawLine( 0, 0, s.width, 0 );
          g.setColor( c.getBackground() );
          g.drawLine( 0, 1, s.width, 1 );
        }
    }
    public Dimension getPreferredSize( JComponent c )
    {
        if ( ((JSeparator)c).getOrientation() == JSeparator.VERTICAL )
            return new Dimension( 2, 0 );
        else
            return new Dimension( 0, 2 );
    }
}
