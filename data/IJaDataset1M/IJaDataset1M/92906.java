package freemind.preferences.layout;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * In conjunction with the <code>KeyEventWorkaround</code>, hides some 
 * warts in the AWT key event API.
 *
 * @author Slava Pestov
 * @version $Id: KeyEventTranslator.java,v 1.2 2007/08/07 17:37:47 dpolivaev Exp $
 */
public class KeyEventTranslator {

    /**
	 * Adds a keyboard translation.
	 * @param key1 Translate this key
	 * @param key2 Into this key
	 * @since jEdit 4.2pre3
	 */
    public static void addTranslation(Key key1, Key key2) {
        transMap.put(key1, key2);
    }

    /**
	 * Pass this an event from {@link
	 * KeyEventWorkaround#processKeyEvent(java.awt.event.KeyEvent)}.
	 * @since jEdit 4.2pre3
	 */
    public static Key translateKeyEvent(KeyEvent evt) {
        int modifiers = evt.getModifiers();
        Key returnValue = null;
        switch(evt.getID()) {
            case KeyEvent.KEY_PRESSED:
                int keyCode = evt.getKeyCode();
                if ((keyCode >= KeyEvent.VK_0 && keyCode <= KeyEvent.VK_9) || (keyCode >= KeyEvent.VK_A && keyCode <= KeyEvent.VK_Z)) {
                    if (KeyEventWorkaround.ALTERNATIVE_DISPATCHER) return null; else {
                        returnValue = new Key(modifiersToString(modifiers), '\0', Character.toUpperCase((char) keyCode));
                    }
                } else {
                    if (keyCode == KeyEvent.VK_TAB) {
                        evt.consume();
                        returnValue = new Key(modifiersToString(modifiers), keyCode, '\0');
                    } else if (keyCode == KeyEvent.VK_SPACE) {
                        if ((modifiers & ~InputEvent.SHIFT_MASK) == 0) returnValue = null; else {
                            returnValue = new Key(modifiersToString(modifiers), 0, ' ');
                        }
                    } else {
                        returnValue = new Key(modifiersToString(modifiers), keyCode, '\0');
                    }
                }
                break;
            case KeyEvent.KEY_TYPED:
                char ch = evt.getKeyChar();
                switch(ch) {
                    case '\n':
                    case '\t':
                    case '\b':
                        return null;
                    case ' ':
                        if ((modifiers & ~InputEvent.SHIFT_MASK) != 0) return null;
                }
                int ignoreMods;
                if (KeyEventWorkaround.ALT_KEY_PRESSED_DISABLED) {
                    ignoreMods = (InputEvent.SHIFT_MASK | InputEvent.ALT_GRAPH_MASK | InputEvent.ALT_MASK);
                } else {
                    ignoreMods = (InputEvent.SHIFT_MASK | InputEvent.ALT_GRAPH_MASK);
                }
                if ((modifiers & InputEvent.ALT_GRAPH_MASK) == 0 && evt.getWhen() - KeyEventWorkaround.lastKeyTime < 750 && (KeyEventWorkaround.modifiers & ~ignoreMods) != 0) {
                    if (KeyEventWorkaround.ALTERNATIVE_DISPATCHER) {
                        returnValue = new Key(modifiersToString(modifiers), 0, ch);
                    } else return null;
                } else {
                    if (ch == ' ') {
                        returnValue = new Key(modifiersToString(modifiers), 0, ch);
                    } else returnValue = new Key(null, 0, ch);
                }
                break;
            default:
                return null;
        }
        Key trans = (Key) transMap.get(returnValue);
        if (trans == null) return returnValue; else return trans;
    }

    /**
	 * Converts a string to a keystroke. The string should be of the
	 * form <i>modifiers</i>+<i>shortcut</i> where <i>modifiers</i>
	 * is any combination of A for Alt, C for Control, S for Shift
	 * or M for Meta, and <i>shortcut</i> is either a single character,
	 * or a keycode name from the <code>KeyEvent</code> class, without
	 * the <code>VK_</code> prefix.
	 * @param keyStroke A string description of the key stroke
	 * @since jEdit 4.2pre3
	 */
    public static Key parseKey(String keyStroke) {
        if (keyStroke == null) return null;
        int index = keyStroke.indexOf('+');
        int modifiers = 0;
        if (index != -1) {
            for (int i = 0; i < index; i++) {
                switch(Character.toUpperCase(keyStroke.charAt(i))) {
                    case 'A':
                        modifiers |= a;
                        break;
                    case 'C':
                        modifiers |= c;
                        break;
                    case 'M':
                        modifiers |= m;
                        break;
                    case 'S':
                        modifiers |= s;
                        break;
                }
            }
        }
        String key = keyStroke.substring(index + 1);
        if (key.length() == 1) {
            return new Key(modifiersToString(modifiers), 0, key.charAt(0));
        } else if (key.length() == 0) {
            return null;
        } else if (key.equals("SPACE")) {
            return new Key(modifiersToString(modifiers), 0, ' ');
        } else {
            int ch;
            try {
                ch = KeyEvent.class.getField("VK_".concat(key)).getInt(null);
            } catch (Exception e) {
                return null;
            }
            return new Key(modifiersToString(modifiers), ch, '\0');
        }
    }

    /**
	 * Changes the mapping between symbolic modifier key names
	 * (<code>C</code>, <code>A</code>, <code>M</code>, <code>S</code>) and
	 * Java modifier flags.
	 *
	 * You can map more than one Java modifier to a symobolic modifier, for 
	 * example :
	 * <p><code><pre>
	 *	setModifierMapping(
	 *		InputEvent.CTRL_MASK,
	 *		InputEvent.ALT_MASK | InputEvent.META_MASK,
	 *		0,
	 *		InputEvent.SHIFT_MASK);
	 *<pre></code></p>
	 *
	 * You cannot map a Java modifer to more than one symbolic modifier.
	 *
	 * @param c The modifier(s) to map the <code>C</code> modifier to
	 * @param a The modifier(s) to map the <code>A</code> modifier to
	 * @param m The modifier(s) to map the <code>M</code> modifier to
	 * @param s The modifier(s) to map the <code>S</code> modifier to
	 *
	 * @since jEdit 4.2pre3
	 */
    public static void setModifierMapping(int c, int a, int m, int s) {
        int duplicateMapping = ((c & a) | (c & m) | (c & s) | (a & m) | (a & s) | (m & s));
        if ((duplicateMapping & InputEvent.CTRL_MASK) != 0) {
            throw new IllegalArgumentException("CTRL is mapped to more than one modifier");
        }
        if ((duplicateMapping & InputEvent.ALT_MASK) != 0) {
            throw new IllegalArgumentException("ALT is mapped to more than one modifier");
        }
        if ((duplicateMapping & InputEvent.META_MASK) != 0) {
            throw new IllegalArgumentException("META is mapped to more than one modifier");
        }
        if ((duplicateMapping & InputEvent.SHIFT_MASK) != 0) {
            throw new IllegalArgumentException("SHIFT is mapped to more than one modifier");
        }
        KeyEventTranslator.c = c;
        KeyEventTranslator.a = a;
        KeyEventTranslator.m = m;
        KeyEventTranslator.s = s;
    }

    /**
	 * Returns a the symbolic modifier name for the specified Java modifier
	 * flag.
	 *
	 * @param mod A modifier constant from <code>InputEvent</code>
	 *
	 * @since jEdit 4.2pre3
	 */
    public static String getSymbolicModifierName(int mod) {
        if ((mod & c) != 0) return "control"; else if ((mod & a) != 0) return "alt"; else if ((mod & m) != 0) return "meta"; else if ((mod & s) != 0) return "shift"; else return "";
    }

    public static String modifiersToString(int mods) {
        StringBuffer buf = null;
        if ((mods & InputEvent.CTRL_MASK) != 0) {
            if (buf == null) buf = new StringBuffer(); else buf.append(GrabKeyDialog.MODIFIER_SEPARATOR);
            buf.append(getSymbolicModifierName(InputEvent.CTRL_MASK));
        }
        if ((mods & InputEvent.ALT_MASK) != 0) {
            if (buf == null) buf = new StringBuffer(); else buf.append(GrabKeyDialog.MODIFIER_SEPARATOR);
            buf.append(getSymbolicModifierName(InputEvent.ALT_MASK));
        }
        if ((mods & InputEvent.META_MASK) != 0) {
            if (buf == null) buf = new StringBuffer(); else buf.append(GrabKeyDialog.MODIFIER_SEPARATOR);
            buf.append(getSymbolicModifierName(InputEvent.META_MASK));
        }
        if ((mods & InputEvent.SHIFT_MASK) != 0) {
            if (buf == null) buf = new StringBuffer(); else buf.append(GrabKeyDialog.MODIFIER_SEPARATOR);
            buf.append(getSymbolicModifierName(InputEvent.SHIFT_MASK));
        }
        if (buf == null) return null; else return buf.toString();
    }

    /**
	 * Returns a string containing symbolic modifier names set in the
	 * specified event.
	 *
	 * @param evt The event
	 *
	 * @since jEdit 4.2pre3
	 */
    public static String getModifierString(InputEvent evt) {
        StringBuffer buf = new StringBuffer();
        if (evt.isControlDown()) buf.append(getSymbolicModifierName(InputEvent.CTRL_MASK));
        if (evt.isAltDown()) buf.append(getSymbolicModifierName(InputEvent.ALT_MASK));
        if (evt.isMetaDown()) buf.append(getSymbolicModifierName(InputEvent.META_MASK));
        if (evt.isShiftDown()) buf.append(getSymbolicModifierName(InputEvent.SHIFT_MASK));
        return (buf.length() == 0 ? null : buf.toString());
    }

    static int c, a, m, s;

    private static Map transMap = new HashMap();

    static {
        if (GrabKeyDialog.isMacOS()) {
            setModifierMapping(InputEvent.META_MASK, InputEvent.CTRL_MASK, InputEvent.ALT_MASK, InputEvent.SHIFT_MASK);
        } else {
            setModifierMapping(InputEvent.CTRL_MASK, InputEvent.ALT_MASK, InputEvent.META_MASK, InputEvent.SHIFT_MASK);
        }
    }

    public static class Key {

        public String modifiers;

        public int key;

        public char input;

        public Key(String modifiers, int key, char input) {
            this.modifiers = modifiers;
            this.key = key;
            this.input = input;
        }

        public int hashCode() {
            return key + input;
        }

        public boolean equals(Object o) {
            if (o instanceof Key) {
                Key k = (Key) o;
                if ((modifiers.equals(k.modifiers)) && key == k.key && input == k.input) {
                    return true;
                }
            }
            return false;
        }

        public String toString() {
            return (modifiers == null ? "" : modifiers) + "<" + Integer.toString(key, 16) + "," + Integer.toString(input, 16) + ">";
        }
    }
}
