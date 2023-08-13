public class BasicEditorPaneUI extends BasicTextUI {
    public static ComponentUI createUI(JComponent c) {
        return new BasicEditorPaneUI();
    }
    public BasicEditorPaneUI() {
        super();
    }
    protected String getPropertyPrefix() {
        return "EditorPane";
    }
    public void installUI(JComponent c) {
        super.installUI(c);
        updateDisplayProperties(c.getFont(),
                                c.getForeground());
    }
    public void uninstallUI(JComponent c) {
        cleanDisplayProperties();
        super.uninstallUI(c);
    }
    public EditorKit getEditorKit(JTextComponent tc) {
        JEditorPane pane = (JEditorPane) getComponent();
        return pane.getEditorKit();
    }
    ActionMap getActionMap() {
        ActionMap am = new ActionMapUIResource();
        am.put("requestFocus", new FocusAction());
        EditorKit editorKit = getEditorKit(getComponent());
        if (editorKit != null) {
            Action[] actions = editorKit.getActions();
            if (actions != null) {
                addActions(am, actions);
            }
        }
        am.put(TransferHandler.getCutAction().getValue(Action.NAME),
                TransferHandler.getCutAction());
        am.put(TransferHandler.getCopyAction().getValue(Action.NAME),
                TransferHandler.getCopyAction());
        am.put(TransferHandler.getPasteAction().getValue(Action.NAME),
                TransferHandler.getPasteAction());
        return am;
    }
    protected void propertyChange(PropertyChangeEvent evt) {
        super.propertyChange(evt);
        String name = evt.getPropertyName();
        if ("editorKit".equals(name)) {
            ActionMap map = SwingUtilities.getUIActionMap(getComponent());
            if (map != null) {
                Object oldValue = evt.getOldValue();
                if (oldValue instanceof EditorKit) {
                    Action[] actions = ((EditorKit)oldValue).getActions();
                    if (actions != null) {
                        removeActions(map, actions);
                    }
                }
                Object newValue = evt.getNewValue();
                if (newValue instanceof EditorKit) {
                    Action[] actions = ((EditorKit)newValue).getActions();
                    if (actions != null) {
                        addActions(map, actions);
                    }
                }
            }
            updateFocusTraversalKeys();
        } else if ("editable".equals(name)) {
            updateFocusTraversalKeys();
        } else if ("foreground".equals(name)
                   || "font".equals(name)
                   || "document".equals(name)
                   || JEditorPane.W3C_LENGTH_UNITS.equals(name)
                   || JEditorPane.HONOR_DISPLAY_PROPERTIES.equals(name)
                   ) {
            JComponent c = getComponent();
            updateDisplayProperties(c.getFont(), c.getForeground());
            if ( JEditorPane.W3C_LENGTH_UNITS.equals(name)
                 || JEditorPane.HONOR_DISPLAY_PROPERTIES.equals(name) ) {
                modelChanged();
            }
            if ("foreground".equals(name)) {
                Object honorDisplayPropertiesObject = c.
                    getClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES);
                boolean honorDisplayProperties = false;
                if (honorDisplayPropertiesObject instanceof Boolean) {
                    honorDisplayProperties =
                        ((Boolean)honorDisplayPropertiesObject).booleanValue();
                }
                if (honorDisplayProperties) {
                    modelChanged();
                }
            }
        }
    }
    void removeActions(ActionMap map, Action[] actions) {
        int n = actions.length;
        for (int i = 0; i < n; i++) {
            Action a = actions[i];
            map.remove(a.getValue(Action.NAME));
        }
    }
    void addActions(ActionMap map, Action[] actions) {
        int n = actions.length;
        for (int i = 0; i < n; i++) {
            Action a = actions[i];
            map.put(a.getValue(Action.NAME), a);
        }
    }
    void updateDisplayProperties(Font font, Color fg) {
        JComponent c = getComponent();
        Object honorDisplayPropertiesObject = c.
            getClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES);
        boolean honorDisplayProperties = false;
        Object w3cLengthUnitsObject = c.getClientProperty(JEditorPane.
                                                          W3C_LENGTH_UNITS);
        boolean w3cLengthUnits = false;
        if (honorDisplayPropertiesObject instanceof Boolean) {
            honorDisplayProperties =
                ((Boolean)honorDisplayPropertiesObject).booleanValue();
        }
        if (w3cLengthUnitsObject instanceof Boolean) {
            w3cLengthUnits = ((Boolean)w3cLengthUnitsObject).booleanValue();
        }
        if (this instanceof BasicTextPaneUI
            || honorDisplayProperties) {
            Document doc = getComponent().getDocument();
            if (doc instanceof StyledDocument) {
                if (doc instanceof HTMLDocument
                    && honorDisplayProperties) {
                    updateCSS(font, fg);
                } else {
                    updateStyle(font, fg);
                }
            }
        } else {
            cleanDisplayProperties();
        }
        if ( w3cLengthUnits ) {
            Document doc = getComponent().getDocument();
            if (doc instanceof HTMLDocument) {
                StyleSheet documentStyleSheet =
                    ((HTMLDocument)doc).getStyleSheet();
                documentStyleSheet.addRule("W3C_LENGTH_UNITS_ENABLE");
            }
        } else {
            Document doc = getComponent().getDocument();
            if (doc instanceof HTMLDocument) {
                StyleSheet documentStyleSheet =
                    ((HTMLDocument)doc).getStyleSheet();
                documentStyleSheet.addRule("W3C_LENGTH_UNITS_DISABLE");
            }
        }
    }
    private static final String FONT_ATTRIBUTE_KEY = "FONT_ATTRIBUTE_KEY";
    void cleanDisplayProperties() {
        Document document = getComponent().getDocument();
        if (document instanceof HTMLDocument) {
            StyleSheet documentStyleSheet =
                ((HTMLDocument)document).getStyleSheet();
            StyleSheet[] styleSheets = documentStyleSheet.getStyleSheets();
            if (styleSheets != null) {
                for (StyleSheet s : styleSheets) {
                    if (s instanceof StyleSheetUIResource) {
                        documentStyleSheet.removeStyleSheet(s);
                        documentStyleSheet.addRule("BASE_SIZE_DISABLE");
                        break;
                    }
                }
            }
            Style style = ((StyledDocument) document).getStyle(StyleContext.DEFAULT_STYLE);
            if (style.getAttribute(FONT_ATTRIBUTE_KEY) != null) {
                style.removeAttribute(FONT_ATTRIBUTE_KEY);
            }
        }
    }
    static class StyleSheetUIResource extends StyleSheet implements UIResource {
    }
    private void updateCSS(Font font, Color fg) {
        JTextComponent component = getComponent();
        Document document = component.getDocument();
        if (document instanceof HTMLDocument) {
            StyleSheet styleSheet = new StyleSheetUIResource();
            StyleSheet documentStyleSheet =
                ((HTMLDocument)document).getStyleSheet();
            StyleSheet[] styleSheets = documentStyleSheet.getStyleSheets();
            if (styleSheets != null) {
                for (StyleSheet s : styleSheets) {
                    if (s instanceof StyleSheetUIResource) {
                        documentStyleSheet.removeStyleSheet(s);
                    }
                }
            }
            String cssRule = sun.swing.
                SwingUtilities2.displayPropertiesToCSS(font,
                                                       fg);
            styleSheet.addRule(cssRule);
            documentStyleSheet.addStyleSheet(styleSheet);
            documentStyleSheet.addRule("BASE_SIZE " +
                                       component.getFont().getSize());
            Style style = ((StyledDocument) document).getStyle(StyleContext.DEFAULT_STYLE);
            if (! font.equals(style.getAttribute(FONT_ATTRIBUTE_KEY))) {
                style.addAttribute(FONT_ATTRIBUTE_KEY, font);
            }
        }
    }
    private void updateStyle(Font font, Color fg) {
        updateFont(font);
        updateForeground(fg);
    }
    private void updateForeground(Color color) {
        StyledDocument doc = (StyledDocument)getComponent().getDocument();
        Style style = doc.getStyle(StyleContext.DEFAULT_STYLE);
        if (style == null) {
            return;
        }
        if (color == null) {
            if (style.getAttribute(StyleConstants.Foreground) != null) {
                style.removeAttribute(StyleConstants.Foreground);
            }
        } else {
            if (! color.equals(StyleConstants.getForeground(style))) {
                StyleConstants.setForeground(style, color);
            }
        }
    }
    private void updateFont(Font font) {
        StyledDocument doc = (StyledDocument)getComponent().getDocument();
        Style style = doc.getStyle(StyleContext.DEFAULT_STYLE);
        if (style == null) {
            return;
        }
        String fontFamily = (String) style.getAttribute(StyleConstants.FontFamily);
        Integer fontSize = (Integer) style.getAttribute(StyleConstants.FontSize);
        Boolean isBold = (Boolean) style.getAttribute(StyleConstants.Bold);
        Boolean isItalic = (Boolean) style.getAttribute(StyleConstants.Italic);
        Font  fontAttribute = (Font) style.getAttribute(FONT_ATTRIBUTE_KEY);
        if (font == null) {
            if (fontFamily != null) {
                style.removeAttribute(StyleConstants.FontFamily);
            }
            if (fontSize != null) {
                style.removeAttribute(StyleConstants.FontSize);
            }
            if (isBold != null) {
                style.removeAttribute(StyleConstants.Bold);
            }
            if (isItalic != null) {
                style.removeAttribute(StyleConstants.Italic);
            }
            if (fontAttribute != null) {
                style.removeAttribute(FONT_ATTRIBUTE_KEY);
           }
        } else {
            if (! font.getName().equals(fontFamily)) {
                StyleConstants.setFontFamily(style, font.getName());
            }
            if (fontSize == null
                  || fontSize.intValue() != font.getSize()) {
                StyleConstants.setFontSize(style, font.getSize());
            }
            if (isBold == null
                  || isBold.booleanValue() != font.isBold()) {
                StyleConstants.setBold(style, font.isBold());
            }
            if (isItalic == null
                  || isItalic.booleanValue() != font.isItalic()) {
                StyleConstants.setItalic(style, font.isItalic());
            }
            if (! font.equals(fontAttribute)) {
                style.addAttribute(FONT_ATTRIBUTE_KEY, font);
            }
        }
    }
}
