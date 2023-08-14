class AppletFrame extends Frame {
    public static void startApplet(String className,
            String title,
            String args[]) {
        Applet a;
        Dimension appletSize;
        try {
            a = (Applet) Class.forName(className).newInstance();
        } catch (ClassNotFoundException e) {
            return;
        } catch (InstantiationException e) {
            return;
        } catch (IllegalAccessException e) {
            return;
        }
        a.init();
        a.start();
        AppletFrame f = new AppletFrame(title);
        f.add("Center", a);
        appletSize = a.getSize();
        f.pack();
        f.setSize(appletSize);
        f.setVisible(true);
    }  
    public AppletFrame(String name) {
        super(name);
    }
    @Override
    public void processEvent(AWTEvent e) {
        if (e.getID() == Event.WINDOW_DESTROY) {
            System.exit(0);
        }
    }  
}   
