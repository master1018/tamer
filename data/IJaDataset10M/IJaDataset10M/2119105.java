package dream.gui.tasks;

import javax.swing.*;
import javax.swing.border.*;
import drm.agentbase.*;
import dream.gui.*;

/**
 * Task for displaying log messages.
 *
 * @version 0.1
 * @author  mike
 */
public class StartExperiment extends javax.swing.JPanel implements ConsoleTask, ILogListener {

    private static final String LABEL_TEXT = "Messages";

    private ConsoleTaskHandler handler = null;

    /** Creates new form Messages */
    public StartExperiment() {
        initComponents();
        Logger.addListener(this);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        setLayout(new java.awt.BorderLayout());
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        jTextArea1.setEditable(false);
        jTextArea1.setLineWrap(true);
        jTextArea1.setWrapStyleWord(true);
        jTextArea1.setBorder(new javax.swing.border.TitledBorder(LABEL_TEXT));
        jScrollPane1.setViewportView(jTextArea1);
        add(jScrollPane1, java.awt.BorderLayout.CENTER);
    }

    /** Returns an icon that visually represents the task (if available).
     * The icons height should be near the given heightInPixels parameter.  */
    public Icon getIcon(int heightInPixels) {
        Icon defaultIcon = new ImageIcon(getClass().getResource("/dream/resources/icons/messages-default-16.gif"));
        return defaultIcon;
    }

    /** Returns the tooltip for this task, may be null.  */
    public String getTooltip() {
        return "see log messages";
    }

    /** Returns the menues this module provides.  */
    public JMenu[] getMenues() {
        return null;
    }

    /** Returns the toolbars this module provides.  */
    public JToolBar[] getToolBars() {
        return null;
    }

    /** Returns a string that is used to identify the task in the task list.  */
    public String getLabelText() {
        return LABEL_TEXT;
    }

    /** Returns the main window the module provides, this is plugged into
     * the right hand panel of the console.  */
    public JComponent getVisual() {
        return this;
    }

    /** This method is called by the handler if this task gets passive.  */
    public void onPassive() {
    }

    /** This method is called by the handler if this task gets active.  */
    public void onActive() {
    }

    /**
     * Reaction when diagnostic information is emitted by
     * an entity (the base or an agent).
     * @param type The type of the information. The constants defined
     * in this interface are used by the base.
     * @param sender Identifies the sender, e.g. the
     * name of the function that sends the message. Must not be null.
     * @param comment An optional explanatory message. Might be null.
     * @param thr An optional Throwable object that caused this event.
     * Might be null.
     */
    public void handleLogMessage(int type, String sender, String comment, Throwable thr) {
        final String s1 = (sender == null ? "" : sender);
        final String s2 = (comment == null ? "" : comment);
        final String s3 = (thr == null ? "" : thr.toString());
        switch(type) {
            case PANIC:
                jTextArea1.append("PANIC! ");
                jTextArea1.append(s1 + ": " + s2 + " " + s3 + "\n");
                break;
            case ILogListener.ERROR:
                jTextArea1.append("ERROR! ");
                jTextArea1.append(s1 + ": " + s2 + " " + s3 + "\n");
                break;
            case WARNING:
                jTextArea1.append("WARNING! ");
                jTextArea1.append(s1 + ": " + s2 + " " + s3 + "\n");
                break;
            case DEBUG:
                jTextArea1.append("DEBUG! ");
                jTextArea1.append(s1 + ": " + s2 + " " + s3 + "\n");
                break;
            case INFO:
            default:
                jTextArea1.append(s1 + ": " + s2 + " " + s3 + "\n");
                break;
        }
    }

    /** The console task must return its handler, previously set
     * handler.  */
    public ConsoleTaskHandler getConsoleTaskHandler() {
        return handler;
    }

    /** The console task must accept this setting to memorize the appropriate
     *  handler. */
    public void setConsoleTaskHandler(ConsoleTaskHandler handler) {
        this.handler = handler;
    }

    private javax.swing.JScrollPane jScrollPane1;

    private javax.swing.JTextArea jTextArea1;
}