public final class Font2DTestApplet extends JApplet {
    public void init() {
        SecurityManager security = System.getSecurityManager();
        if ( security != null ) {
            try {
                security.checkPermission( new AWTPermission( "showWindowWithoutWarningBanner" ));
            }
            catch ( SecurityException e ) {
                System.out.println( "NOTE: showWindowWithoutWarningBanner AWTPermission not given.\n" +
                                    "Zoom window will contain warning banner at bottom when shown\n" );
            }
            try {
                security.checkPrintJobAccess();
            }
            catch ( SecurityException e ) {
                System.out.println( "NOTE: queuePrintJob RuntimePermission not given.\n" +
                                    "Printing feature will not be available\n" );
            }
        }
        final JFrame f = new JFrame( "Font2DTest" );
        final Font2DTest f2dt = new Font2DTest( f, true );
        f.addWindowListener( new WindowAdapter() {
            public void windowClosing( WindowEvent e ) { f.dispose(); }
        });
        f.getContentPane().add( f2dt );
        f.pack();
        f.show();
    }
}
