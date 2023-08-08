public class MiscGameSettingsController extends TabbedPaneController implements ActionListener, ChangeListener {
    private String workingCombatantLookup;
    private MultiScriptController scripts;
    private FileSelectorController fileSelector;
    private GameSettingsFrame frame;
    private static final String GAME_NAME = "gameName";
    private static final String INIT_EVERY_ROUND = "initEveryRound";
    private static final String INIT_DISPLAY_PROP = "initDisplayProp";
    private static final String MODIFIER_DISPLAY_PROP = "modDisplayProp";
    private static final String COMBATANT_LOOKUP_FILE = "combatantLookup";
    private static final String LOOKUP_BUTTON = "lookupButton";
    private static final String NEW_FILE_FLAG = "newFile";
    private static final String EXISTING_FILE_FLAG = "existingFile";
    private static final String NO_FILE_FLAG = "noFile";
    private static final String[] FILE_SELECTOR_COMPONENTS = { COMBATANT_LOOKUP_FILE, LOOKUP_BUTTON };
    private static final String RELOAD_BUTTON = "reload";
    private static final String SCRIPT_EVENT_COMBO_BOX = "event";
    private static final String SCRIPT_DESCRIPTION = "description";
    private static final String SCRIPT_NOTES = "notes";
    private static final String SCRIPT = "script";
    private static final String[] SCRIPT_COMPONENTS = { SCRIPT_EVENT_COMBO_BOX, SCRIPT_DESCRIPTION, SCRIPT, SCRIPT_NOTES };
    public static final String DATA_FILE_TYPE = ".rpdat";
    public static final FileFilter DATA_FILE_FILTER = new FileFilter() {
        @Override
        public String getDescription() {
            return "RPTools Data Files (*" + DATA_FILE_TYPE + ")";
        }
        @Override
        public boolean accept(File file) {
            if (file.isDirectory()) return true;
            String name = file.getName();
            if (name.endsWith(DATA_FILE_TYPE)) return true;
            return false;
        }
    };
    private static final Logger LOGGER = Logger.getLogger(MiscGameSettingsController.class.getName());
    public MiscGameSettingsController(GameSettingsFrame aView) {
        super(aView, new FormPanel("net/rptools/inittool/resources/forms/miscSettings.jfrm"));
        frame = aView;
        getPanel().getButton(RELOAD_BUTTON).addActionListener(this);
        getPanel().getRadioButton(NEW_FILE_FLAG).addActionListener(this);
        getPanel().getRadioButton(NO_FILE_FLAG).addActionListener(this);
        getPanel().getRadioButton(EXISTING_FILE_FLAG).addActionListener(this);
        scripts = new MultiScriptController(getPanel(), SCRIPT_COMPONENTS, frame.getInitToolSettings().getEventScripts());
        fileSelector = new FileSelectorController(getPanel(), FILE_SELECTOR_COMPONENTS, true, DATA_FILE_FILTER) {
            @Override
            protected boolean validateFile(String file) {
                URL url = null;
                try {
                    url = new URL(file);
                } catch (MalformedURLException e) {
                    LOGGER.log(Level.WARNING, "The URL '" + file + "' is not formatted properly.", e);
                    JOptionPane.showMessageDialog(getPanel(), "The URL '" + file + "' is not formatted properly.");
                    return false;
                }
                boolean valid = PropertyTableXML.getInstance().validateXmlFile(url);
                if (valid) workingCombatantLookup = file;
                return valid;
            }
        };
        aView.getTabbedPane().addChangeListener(this);
    }
    private boolean saveSettings(boolean commit) {
        InitToolGameSettings settings = frame.getInitToolSettings();
        boolean change = false;
        String gameName = getString(GAME_NAME);
        if (gameName == null) {
            throw new IllegalStateException("A game name is required but none was set.");
        }
        if (!gameName.equals(settings.getGameName())) change = true;
        URL url = null;
        if (getPanel().getRadioButton(NEW_FILE_FLAG).isSelected()) {
            try {
                url = fileSelector.getUrl();
                change = true;
            } catch (MalformedURLException e) {
                throw new IllegalStateException("The new url is invalid.");
            }
        }
        String initDisplay = getPanel().getText(INIT_DISPLAY_PROP);
        if (initDisplay == null || (initDisplay = initDisplay.trim()).length() == 0) initDisplay = null;
        checkPropertyName(initDisplay, "The initiative display property");
        if (initDisplay != settings.getInitDisplayProperty() && (initDisplay == null || !initDisplay.equals(settings.getInitDisplayProperty()))) change = true;
        String modDisplay = getPanel().getText(MODIFIER_DISPLAY_PROP);
        if (modDisplay == null || (modDisplay = modDisplay.trim()).length() == 0) modDisplay = null;
        checkPropertyName(modDisplay, "The initiative modifier display property");
        if (modDisplay != settings.getInitModifierDisplayProperty() && (modDisplay == null || !modDisplay.equals(settings.getInitModifierDisplayProperty()))) change = true;
        scripts.flushScript(null);
        change |= scripts.isChanged();
        change |= getPanel().getBoolean(INIT_EVERY_ROUND) != settings.isInitEachRound();
        if (!commit) return change;
        if (url != null) {
            try {
                File file = new File(System.getProperty("java.io.tmpdir") + File.separator + new GUID() + ".rpdat");
                OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
                InputStream is = url.openStream();
                FileUtil.copy(is, os);
                frame.getSettingsFile().importCombatantLookupFile(PropertySettings.getInstance().getDatabaseName(), file);
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Unable to copy the url file: " + url.toExternalForm(), e);
                throw new IllegalArgumentException("Unable to copy URL " + url.toExternalForm());
            }
        } else if (getPanel().getRadioButton(NO_FILE_FLAG).isSelected() && settings.isCombatantLookupAvailable()) {
            frame.getSettingsFile().removeCombatantLookupFile(PropertySettings.getInstance().getDatabaseName());
        }
        settings.getCustomPropertySet().setGame(gameName);
        settings.setInitEachRound(getPanel().getBoolean(INIT_EVERY_ROUND));
        settings.setInitDisplayProperty(initDisplay);
        settings.setInitModifierDisplayProperty(modDisplay);
        workingCombatantLookup = null;
        scripts.saveScripts(settings.getEventScripts());
        return change;
    }
    private void checkPropertyName(String propName, String message) {
        if (propName != null) {
            CustomPropertyController cpc = (CustomPropertyController) getView().getController(CustomPropertyController.CUSTOM_PROPERTIES_PANEL_NAME);
            PropertyDescriptor pd = PropertyDescriptorSet.get(propName, cpc.getProperties());
            if (pd == null) {
                throw new IllegalStateException(message + " '" + propName + "' is not a valid custom property.");
            }
        }
    }
    private void changedProperties(String fieldName) {
        String initDisplay = getPanel().getText(fieldName);
        if (initDisplay == null || (initDisplay = initDisplay.trim()).length() == 0) initDisplay = null;
        CustomPropertyController cpc = (CustomPropertyController) getView().getController(CustomPropertyController.CUSTOM_PROPERTIES_PANEL_NAME);
        PropertyDescriptor pd = initDisplay == null ? null : PropertyDescriptorSet.get(initDisplay, cpc.getProperties());
        if (pd != null) {
            getPanel().setText(fieldName, initDisplay);
        } else {
            getPanel().setText(fieldName, null);
        }
    }
    public void stateChanged(ChangeEvent aE) {
        if (getView().getTabbedPane().getSelectedComponent() == getPanel() || aE == null) {
            changedProperties(INIT_DISPLAY_PROP);
            changedProperties(MODIFIER_DISPLAY_PROP);
        }
    }
    @Override
    public void flushPanel() {
        if (saveSettings(false)) setChanged(true);
    }
    @Override
    public void setFields() {
        InitToolGameSettings settings = frame.getInitToolSettings();
        getPanel().setText(GAME_NAME, settings.getGameName());
        getPanel().getCheckBox(INIT_EVERY_ROUND).setSelected(settings.isInitEachRound());
        getPanel().setText(INIT_DISPLAY_PROP, settings.getInitDisplayProperty());
        getPanel().getRadioButton(EXISTING_FILE_FLAG).setEnabled(settings.isCombatantLookupAvailable());
        getPanel().getTextField(COMBATANT_LOOKUP_FILE).setEnabled(false);
        getPanel().getButton(LOOKUP_BUTTON).setEnabled(false);
        if (settings.isCombatantLookupAvailable()) {
            getPanel().getRadioButton(EXISTING_FILE_FLAG).setSelected(true);
            getPanel().getButton(RELOAD_BUTTON).setEnabled(true);
        } else {
            getPanel().getRadioButton(NO_FILE_FLAG).setSelected(true);
            getPanel().getButton(RELOAD_BUTTON).setEnabled(true);
        }
        getPanel().setText(COMBATANT_LOOKUP_FILE, null);
        scripts.setScriptsOwner(settings.getEventScripts());
    }
    @Override
    public void saveSettings() {
        saveSettings(true);
    }
    @Override
    public String getName() {
        return "IT Miscellaneous";
    }
    @Override
    public String getToolTip() {
        return "Set the game name and other miscellaneous properties";
    }
    @Override
    public int getMnemonic() {
        return KeyEvent.VK_I;
    }
    @Override
    public Icon getIcon() {
        return RPIconFactory.getInstance().get(ImageStorageType.createDescriptor(InitToolFrame.LOGO_ICON_NAME), 16, 16);
    }
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == getPanel().getRadioButton(NEW_FILE_FLAG) || event.getSource() == getPanel().getRadioButton(NO_FILE_FLAG) || event.getSource() == getPanel().getRadioButton(EXISTING_FILE_FLAG)) {
            if (getPanel().getBoolean(NO_FILE_FLAG) || getPanel().getBoolean(EXISTING_FILE_FLAG)) {
                getPanel().getButton(LOOKUP_BUTTON).setEnabled(false);
                getPanel().getTextComponent(COMBATANT_LOOKUP_FILE).setEnabled(false);
                getPanel().setText(COMBATANT_LOOKUP_FILE, null);
            } else if (getPanel().getBoolean(NEW_FILE_FLAG)) {
                getPanel().getTextComponent(COMBATANT_LOOKUP_FILE).setEnabled(true);
                getPanel().getButton(LOOKUP_BUTTON).setEnabled(true);
                getPanel().setText(COMBATANT_LOOKUP_FILE, workingCombatantLookup);
            }
            if (!getPanel().getBoolean(EXISTING_FILE_FLAG)) {
                getPanel().getButton(RELOAD_BUTTON).setEnabled(false);
            }
        }
    }
}
