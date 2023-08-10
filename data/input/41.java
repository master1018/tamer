public class AddRosterEntryToEcos extends AbstractAction {
    public AddRosterEntryToEcos(String s, EcosSystemConnectionMemo memo) {
        super(s);
        adaptermemo = memo;
    }
    EcosSystemConnectionMemo adaptermemo;
    JComboBox rosterEntry = new JComboBox();
    JComboBox selections;
    Roster roster;
    public void actionPerformed(ActionEvent event) {
        roster = Roster.instance();
        rosterEntryUpdate();
        int retval = JOptionPane.showOptionDialog(null, "Select the roster entry to add to the Ecos\nThe Drop down list only shows locos that have not already been added. ", "Add to Ecos", 0, JOptionPane.INFORMATION_MESSAGE, null, new Object[] { "Cancel", "OK", rosterEntry }, null);
        log.debug("Dialog value " + retval + " selected, " + rosterEntry.getSelectedIndex() + ":" + rosterEntry.getSelectedItem());
        if (retval != 1) {
            return;
        }
        String selEntry = (String) rosterEntry.getSelectedItem();
        RosterEntry re = roster.entryFromTitle(selEntry);
        RosterToEcos rosterToEcos = new RosterToEcos();
        rosterToEcos.createEcosLoco(re, adaptermemo);
        actionPerformed(event);
    }
    void rosterEntryUpdate() {
        if (rosterEntry != null) rosterEntry.removeAllItems();
        for (int i = 0; i < roster.numEntries(); i++) {
            RosterEntry r = roster.getEntry(i);
            if (r.getAttribute(adaptermemo.getPreferenceManager().getRosterAttribute()) == null) rosterEntry.addItem(r.titleString());
        }
    }
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AddRosterEntryToEcos.class.getName());
}
