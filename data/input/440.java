public class DataPropertySelector extends JPanel implements VerifiedInputEditor {
    private static final long serialVersionUID = 4118824886564461973L;
    protected List<OWLDataProperty> dataProperties = new ArrayList<OWLDataProperty>();
    protected StaticListModel<OWLDataProperty> dataPropertiesModel = new StaticListModel<OWLDataProperty>(this.dataProperties, null);
    protected MList dataPropertiesView = new MList();
    protected Set<InputVerificationStatusChangedListener> listeners = new HashSet<InputVerificationStatusChangedListener>();
    private OWLEditorKit kit;
    public void addStatusChangedListener(InputVerificationStatusChangedListener listener) {
        this.listeners.add(listener);
    }
    public void removeStatusChangedListener(InputVerificationStatusChangedListener listener) {
        this.listeners.remove(listener);
    }
    public DataPropertySelector(OWLEditorKit k) {
        super(new BorderLayout());
        this.kit = k;
        this.dataPropertiesView.setCellRenderer(new RenderableObjectCellRenderer(this.kit));
        this.dataPropertiesView.setModel(this.dataPropertiesModel);
        OWLObjectHierarchyProvider<OWLDataProperty> dpp = this.kit.getOWLModelManager().getOWLHierarchyManager().getOWLDataPropertyHierarchyProvider();
        for (OWLDataProperty dp : dpp.getRoots()) {
            this.dataProperties.add(dp);
            for (OWLDataProperty dpd : dpp.getDescendants(dp)) {
                this.dataProperties.add(dpd);
            }
        }
        this.dataPropertiesModel.init();
        this.dataPropertiesView.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    if (DataPropertySelector.this.dataPropertiesView.getSelectedIndex() > -1) {
                        notifyVerified();
                    }
                }
            }
        });
        JScrollPane spd = ComponentFactory.createScrollPane(this.dataPropertiesView);
        spd.setBorder(ComponentFactory.createTitledBorder("Data property selection"));
        this.add(spd, BorderLayout.CENTER);
    }
    protected Set<OWLClass> getOWLClasses(OWLObjectProperty op) {
        Set<OWLClass> ranges = new HashSet<OWLClass>();
        for (OWLDescription d : op.getRanges(this.kit.getOWLModelManager().getActiveOntology())) {
            if (d instanceof OWLClass) {
                ranges.add((OWLClass) d);
            }
        }
        return ranges;
    }
    public void clear() {
        this.dataPropertiesView.getSelectionModel().clearSelection();
    }
    @SuppressWarnings("unchecked")
    public PropertyChainCell getCell() {
        if (this.dataPropertiesView.getSelectedIndex() > -1) {
            OWLDataProperty prop = ((StaticListItem<OWLDataProperty>) this.dataPropertiesView.getSelectedValue()).getItem();
            return new PropertyChainCell(prop, null);
        }
        return null;
    }
    protected void notifyVerified() {
        for (InputVerificationStatusChangedListener i : this.listeners) {
            i.verifiedStatusChanged(true);
        }
    }
}
