package org.posper.graphics.editorkeys;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;

/**
 * (Authors section added Dec 2009)
 * @author Jamie Campbell <jamie@parit.ca>
 */
public abstract class JEditorText extends JEditorAbstract {

    protected String m_svalue;

    public static final int MODE_Abc1 = 0;

    public static final int MODE_abc1 = 1;

    public static final int MODE_ABC1 = 2;

    public static final int MODE_123 = 3;

    public int m_iMode;

    protected int m_iTicks;

    protected char m_cLastChar;

    protected long m_lcount;

    private Timer m_jtimer;

    private static final char[] CHAR_abc1_1 = { '.', '?', '!', ',', '1', ';', ':' };

    private static final char[] CHAR_abc1_2 = { 'a', 'b', 'c', '2', ' ' };

    private static final char[] CHAR_abc1_3 = { 'd', 'e', 'f', '3', '‚' };

    private static final char[] CHAR_abc1_4 = { 'g', 'h', 'i', '4', '¡' };

    private static final char[] CHAR_abc1_5 = { 'j', 'k', 'l', '5' };

    private static final char[] CHAR_abc1_6 = { 'm', 'n', 'o', '6', '¤', '¢' };

    private static final char[] CHAR_abc1_7 = { 'p', 'q', 'r', 's', '7' };

    private static final char[] CHAR_abc1_8 = { 't', 'u', 'v', '8', '£', '�' };

    private static final char[] CHAR_abc1_9 = { 'w', 'x', 'y', 'z', '9' };

    private static final char[] CHAR_abc1_0 = { ' ', '0' };

    private static final char[] CHAR_ABC1_1 = { '.', '?', '!', ',', '1', ';', ':' };

    private static final char[] CHAR_ABC1_2 = { 'A', 'B', 'C', '2', 'µ' };

    private static final char[] CHAR_ABC1_3 = { 'D', 'E', 'F', '3', 'É' };

    private static final char[] CHAR_ABC1_4 = { 'G', 'H', 'I', '4', 'Í' };

    private static final char[] CHAR_ABC1_5 = { 'J', 'K', 'L', '5' };

    private static final char[] CHAR_ABC1_6 = { 'M', 'N', 'O', '6', 'Ñ', 'Ó' };

    private static final char[] CHAR_ABC1_7 = { 'P', 'Q', 'R', 'S', '7' };

    private static final char[] CHAR_ABC1_8 = { 'T', 'U', 'V', '8', 'Ú', 'Ü' };

    private static final char[] CHAR_ABC1_9 = { 'W', 'X', 'Y', 'Z', '9' };

    private static final char[] CHAR_ABC1_0 = { ' ', '0' };

    /** Creates a new instance of JEditorString */
    public JEditorText() {
        m_svalue = null;
        m_iTicks = 0;
        m_cLastChar = ' ';
        m_jtimer = new javax.swing.Timer(1000, new TimerAction());
        m_lcount = 0L;
        m_iMode = getStartMode();
        m_jtimer.start();
    }

    protected abstract int getStartMode();

    public final void reset() {
        String sOldText = getText();
        m_iMode = getStartMode();
        m_svalue = null;
        m_iTicks = 0;
        m_cLastChar = ' ';
        reprintText();
        firePropertyChange("Text", sOldText, getText());
    }

    public final void setText(String sText) {
        String sOldText = getText();
        m_svalue = sText;
        m_iTicks = 0;
        m_cLastChar = ' ';
        reprintText();
        firePropertyChange("Text", sOldText, getText());
    }

    public final void setEditModeEnum(int iMode) {
        m_iMode = iMode;
        m_iTicks = 0;
        m_cLastChar = ' ';
        reprintText();
    }

    public final Object getData() {
        return getText();
    }

    public final String getText() {
        if (m_cLastChar == ' ') {
            return m_svalue;
        } else {
            return appendChar2Value(getKeyChar());
        }
    }

    protected final int getAlignment() {
        return javax.swing.SwingConstants.LEFT;
    }

    protected final String getEditMode() {
        switch(m_iMode) {
            case MODE_Abc1:
                return "Abc1";
            case MODE_abc1:
                return "abc1";
            case MODE_ABC1:
                return "ABC1";
            case MODE_123:
                return "123";
            default:
                return null;
        }
    }

    protected String getTextEdit() {
        StringBuffer s = new StringBuffer();
        s.append("<html>");
        if (m_svalue != null) {
            s.append(m_svalue);
        }
        if (m_cLastChar != ' ') {
            s.append("<font color=\"#a0a0a0\">");
            s.append(getKeyChar());
            s.append("</font>");
        }
        s.append("<font color=\"#a0a0a0\">_</font>");
        return s.toString();
    }

    protected String getTextFormat() throws Exception {
        return (m_svalue == null) ? "<html>" : "<html>" + m_svalue;
    }

    protected void typeCharInternal(char c) {
        String sOldText = getText();
        if (c == '') {
            if (m_cLastChar == ' ') {
                if (m_svalue != null && m_svalue.length() > 0) {
                    m_svalue = m_svalue.substring(0, m_svalue.length() - 1);
                }
            } else {
                m_iTicks = 0;
                m_cLastChar = ' ';
            }
        } else if (c == '') {
            m_svalue = null;
            m_iTicks = 0;
            m_cLastChar = ' ';
        } else if (c >= ' ') {
            if (m_cLastChar != ' ') {
                char ckey = getKeyChar();
                m_svalue = appendChar2Value(ckey);
                acceptKeyChar(ckey);
            }
            m_iTicks = 0;
            m_cLastChar = ' ';
            m_svalue = appendChar2Value(c);
        }
        m_jtimer.restart();
        firePropertyChange("Text", sOldText, getText());
    }

    protected void transCharInternal(char c) {
        String sOldText = getText();
        if (c == '-') {
            if (m_cLastChar == ' ') {
                if (m_svalue != null && m_svalue.length() > 0) {
                    m_svalue = m_svalue.substring(0, m_svalue.length() - 1);
                }
            } else {
                m_iTicks = 0;
                m_cLastChar = ' ';
            }
        } else if (c == '') {
            m_svalue = null;
            m_iTicks = 0;
            m_cLastChar = ' ';
        } else if (c == '.' || c == ',') {
            if (m_cLastChar != ' ') {
                m_svalue = appendChar2Value(getKeyChar());
            }
            m_iTicks = 0;
            m_cLastChar = ' ';
            m_iMode = (m_iMode + 1) % 4;
        } else if (c == '1' || c == '2' || c == '3' || c == '4' || c == '5' || c == '6' || c == '7' || c == '8' || c == '9' || c == '0') {
            if (m_iMode == MODE_123) {
                m_svalue = appendChar2Value(c);
            } else if (c == m_cLastChar) {
                m_iTicks++;
            } else {
                if (m_cLastChar != ' ') {
                    char ckey = getKeyChar();
                    m_svalue = appendChar2Value(ckey);
                    acceptKeyChar(ckey);
                }
                m_iTicks = 0;
                m_cLastChar = c;
            }
        }
        m_jtimer.restart();
        firePropertyChange("Text", sOldText, getText());
    }

    private void acceptKeyChar(char c) {
        if (m_iMode == MODE_Abc1 && c != ' ') {
            m_iMode = MODE_abc1;
        } else if (m_iMode == MODE_abc1 && (c == '.' || c == ',')) {
            m_iMode = MODE_Abc1;
        }
    }

    protected char getKeyChar() {
        char[] clist = null;
        switch(m_iMode) {
            case MODE_abc1:
                switch(m_cLastChar) {
                    case '1':
                        clist = CHAR_abc1_1;
                        break;
                    case '2':
                        clist = CHAR_abc1_2;
                        break;
                    case '3':
                        clist = CHAR_abc1_3;
                        break;
                    case '4':
                        clist = CHAR_abc1_4;
                        break;
                    case '5':
                        clist = CHAR_abc1_5;
                        break;
                    case '6':
                        clist = CHAR_abc1_6;
                        break;
                    case '7':
                        clist = CHAR_abc1_7;
                        break;
                    case '8':
                        clist = CHAR_abc1_8;
                        break;
                    case '9':
                        clist = CHAR_abc1_9;
                        break;
                    case '0':
                        clist = CHAR_abc1_0;
                        break;
                }
                break;
            case MODE_Abc1:
            case MODE_ABC1:
                switch(m_cLastChar) {
                    case '1':
                        clist = CHAR_ABC1_1;
                        break;
                    case '2':
                        clist = CHAR_ABC1_2;
                        break;
                    case '3':
                        clist = CHAR_ABC1_3;
                        break;
                    case '4':
                        clist = CHAR_ABC1_4;
                        break;
                    case '5':
                        clist = CHAR_ABC1_5;
                        break;
                    case '6':
                        clist = CHAR_ABC1_6;
                        break;
                    case '7':
                        clist = CHAR_ABC1_7;
                        break;
                    case '8':
                        clist = CHAR_ABC1_8;
                        break;
                    case '9':
                        clist = CHAR_ABC1_9;
                        break;
                    case '0':
                        clist = CHAR_ABC1_0;
                        break;
                }
                break;
        }
        if (clist == null) {
            return m_cLastChar;
        } else {
            return clist[m_iTicks % clist.length];
        }
    }

    private class TimerAction implements ActionListener {

        public void actionPerformed(ActionEvent evt) {
            if (m_cLastChar != ' ') {
                char ckey = getKeyChar();
                m_svalue = appendChar2Value(ckey);
                acceptKeyChar(ckey);
                m_iTicks = 0;
                m_cLastChar = ' ';
                m_jtimer.restart();
                reprintText();
            }
        }
    }

    private String appendChar2Value(char c) {
        StringBuffer s = new StringBuffer();
        if (m_svalue != null) {
            s.append(m_svalue);
        }
        s.append(c);
        return s.toString();
    }
}
