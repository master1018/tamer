public class StructuredViewAction extends CallableSystemAction {
    private static JButton dropDownButton;
    private static ButtonGroup buttonGroup;
    private static JPopupMenu popup;
    private MyMenuItemListener menuItemListener;
    private Map<JMenuItem, GroupOrganizer> map;
    public StructuredViewAction() {
        putValue(Action.SHORT_DESCRIPTION, "Cluster nodes into blocks");
    }
    @Override
    public Component getToolbarPresenter() {
        Image iconImage = Utilities.loadImage("com/sun/hotspot/igv/coordinator/images/structure.gif");
        ImageIcon icon = new ImageIcon(iconImage);
        popup = new JPopupMenu();
        menuItemListener = new MyMenuItemListener();
        buttonGroup = new ButtonGroup();
        Collection<? extends GroupOrganizer> organizersCollection = Lookup.getDefault().lookupAll(GroupOrganizer.class);
        List<GroupOrganizer> organizers = new ArrayList<GroupOrganizer>(organizersCollection);
        Collections.sort(organizers, new Comparator<GroupOrganizer>() {
            public int compare(GroupOrganizer a, GroupOrganizer b) {
                return a.getName().compareTo(b.getName());
            }
        });
        map = new HashMap<JMenuItem, GroupOrganizer>();
        boolean first = true;
        for(GroupOrganizer organizer : organizers) {
            JCheckBoxMenuItem item = new JCheckBoxMenuItem(organizer.getName());
            map.put(item, organizer);
            item.addActionListener(menuItemListener);
            buttonGroup.add(item);
            popup.add(item);
            if(first) {
                item.setSelected(true);
                first = false;
            }
        }
        dropDownButton = DropDownButtonFactory.createDropDownButton(
                new ImageIcon(
                new BufferedImage(32, 32, BufferedImage.TYPE_BYTE_GRAY)),
                popup);
        dropDownButton.setIcon(icon);
        dropDownButton.setToolTipText("Insert Layer Registration");
        dropDownButton.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                int state = e.getStateChange();
                if (state == ItemEvent.SELECTED) {
                    performAction();
                }
            }
        });
        dropDownButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                performAction();
            }
        });
        popup.addPopupMenuListener(new PopupMenuListener() {
            public void popupMenuCanceled(PopupMenuEvent e) {
                dropDownButton.setSelected(false);
            }
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                dropDownButton.setSelected(false);
            }
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                dropDownButton.setSelected(true);
            }
        });
        return dropDownButton;
    }
    private class MyMenuItemListener implements ActionListener {
        public void actionPerformed(ActionEvent ev) {
            JMenuItem item = (JMenuItem) ev.getSource();
            GroupOrganizer organizer = map.get(item);
            assert organizer != null : "Organizer must exist!";
            OutlineTopComponent.findInstance().setOrganizer(organizer);
        }
    }
    @Override
    public void performAction() {
        popup.show(dropDownButton, 0, dropDownButton.getHeight());
    }
    public String getName() {
        return "Structured View";
    }
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    @Override
    protected boolean asynchronous() {
        return false;
    }
}
