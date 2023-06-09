package rvsn00p;

import rvsn00p.util.rv.RvParameters;
import rvsn00p.viewer.RvSnooperGUI;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Starts an instance of the RvSn00p console for off-line viewing.
 *
 * @author �rjan Lundberg
 * Based on work done on Logfactor5 Contributed by ThoughtWorks Inc. by
 * @author Brad Marlborough
 * @author Richard Hurst
 */
public class StartRvSnooper {

    /**
     * Main - starts a an instance of the RvSnoop console and configures
     * the console settings.
     */
    public static final void main(String[] args) {
        if (args.length != 0) if (args[0].compareToIgnoreCase("-h") == 0) {
            System.err.println(RvSnooperGUI.VERSION);
            System.err.println("Usage: rvsnoop.StartRvSnooper [Daemon|Service|Network|Subject] ...  ");
            System.err.println("Example: rvsnoop.StartRvSnooper \"tcp:7500|||a.>\" \"tcp:7500|||b.>\"  ");
            System.exit(-1);
        }
        Set setRvListenersParam = new HashSet();
        if (args.length > 0) {
            for (int iarg = 0; args.length > iarg; ++iarg) {
                RvParameters p = new RvParameters();
                p.configureByLineString(args[iarg]);
                setRvListenersParam.add(p);
            }
            System.out.println(RvSnooperGUI.VERSION);
        }
        RvSnooperGUI monitor = new RvSnooperGUI(MsgType.getAllDefaultLevels(), setRvListenersParam);
        monitor.setFrameSize(getDefaultMonitorWidth(), getDefaultMonitorHeight());
        monitor.setFontSize(12);
        monitor.show();
    }

    protected static int getDefaultMonitorWidth() {
        return (3 * getScreenWidth()) / 4;
    }

    protected static int getDefaultMonitorHeight() {
        return (3 * getScreenHeight()) / 4;
    }

    /**
     * @return the screen width from Toolkit.getScreenSize()
     * if possible, otherwise returns 800
     * @see java.awt.Toolkit
     */
    protected static int getScreenWidth() {
        try {
            return Toolkit.getDefaultToolkit().getScreenSize().width;
        } catch (Throwable t) {
            return 800;
        }
    }

    /**
     * @return the screen height from Toolkit.getScreenSize()
     * if possible, otherwise returns 600
     * @see java.awt.Toolkit
     */
    protected static int getScreenHeight() {
        try {
            return Toolkit.getDefaultToolkit().getScreenSize().height;
        } catch (Throwable t) {
            return 600;
        }
    }
}
