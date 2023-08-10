public class PreferencesEditor extends Box {
    private static final long serialVersionUID = 1L;
    private JTabbedPane tabbedPane;
    private Hashtable<JToggleButton, JComponent> components;
    private Hashtable<JToggleButton, ProfileSelector> profileSelectors;
    private Hashtable<ProfileType, ButtonGroup> buttonGroups;
    private Hashtable<ProfileType, Box> buttonBoxes;
    private Hashtable<ProfileType, Box> contentBoxes;
    public PreferencesEditor() {
        super(BoxLayout.Y_AXIS);
        this.components = new Hashtable<JToggleButton, JComponent>();
        this.profileSelectors = new Hashtable<JToggleButton, ProfileSelector>();
        this.buttonGroups = new Hashtable<ProfileType, ButtonGroup>();
        this.buttonBoxes = new Hashtable<ProfileType, Box>();
        this.contentBoxes = new Hashtable<ProfileType, Box>();
        this.tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        ProfileType type = null;
        Iterator<ProfileType> iter = ProfileType.iterator();
        while (iter.hasNext()) {
            type = iter.next();
            buttonGroups.put(type, new ButtonGroup());
            Box buttonsBox = new Box(BoxLayout.Y_AXIS);
            JScrollPane buttonsScrollPane = new JScrollPane(buttonsBox);
            buttonBoxes.put(type, buttonsBox);
            Box contentBox = new Box(BoxLayout.Y_AXIS);
            contentBoxes.put(type, contentBox);
            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, buttonsScrollPane, contentBox);
            splitPane.setDividerLocation(200);
            tabbedPane.addTab(type.toString(), splitPane);
        }
        this.add(tabbedPane);
        this.setSize(600, 400);
    }
    public void selectTab(ProfileType type) {
        int idx = tabbedPane.indexOfTab(type.toString());
        if (idx != -1) {
            tabbedPane.setSelectedIndex(idx);
        }
    }
    public void selectTab(ProfileType type, JToggleButton button) {
        selectTab(type);
        if (button != null) {
            button.setSelected(true);
            button.requestFocusInWindow();
        }
    }
    public void addPreferenceComponent(ProfileType profileType, JComponent comp, JToggleButton button) {
        boolean selected = button.isSelected();
        components.put(button, comp);
        buttonGroups.get(profileType).add(button);
        buttonBoxes.get(profileType).add(button);
        button.addItemListener(new PreferenceSetButtonListener(profileType, button, this));
        if (selected) {
            button.setSelected(false);
            button.setSelected(true);
        }
    }
    public void setProfileSelector(JToggleButton button, ProfileSelector selector) {
        if (button != null && selector != null) profileSelectors.put(button, selector);
    }
    public void setProfile(JToggleButton button, Profile profile) {
        if (profileSelectors.containsKey(button) && profile != null) {
            profileSelectors.get(button).setProfile(profile);
        }
    }
    public Profile getProfile(JToggleButton button) {
        if (profileSelectors.containsKey(button)) return profileSelectors.get(button).getProfile(); else return null;
    }
    private static class PreferenceSetButtonListener implements ItemListener {
        private ProfileType profileType;
        private JToggleButton button;
        private PreferencesEditor owner;
        PreferenceSetButtonListener(ProfileType profileType, JToggleButton button, PreferencesEditor owner) {
            this.profileType = profileType;
            this.button = button;
            this.owner = owner;
        }
        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                Box box = owner.contentBoxes.get(profileType);
                box.removeAll();
                box.add(owner.components.get(button));
                owner.repaint();
            }
        }
    }
}
