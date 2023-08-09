public class FilterNode extends CheckNode implements LookupListener, ChangedListener<FilterTopComponent> {
    private Filter filter;
    private Lookup.Result result;
    public FilterNode(Filter filter) {
        this(filter, new InstanceContent());
    }
    private FilterNode(Filter filter, InstanceContent content) {
        super(Children.LEAF, new AbstractLookup(content));
        content.add(filter);
        content.add(filter.getEditor());
        this.filter = filter;
        filter.getChangedEvent().addListener(new ChangedListener<Filter>() {
            public void changed(Filter source) {
                update();
            }
        });
        update();
        Lookup.Template<FilterChain> tpl = new Lookup.Template<FilterChain>(FilterChain.class);
        result = Utilities.actionsGlobalContext().lookup(tpl);
        result.addLookupListener(this);
        FilterTopComponent.findInstance().getFilterSettingsChangedEvent().addListener(this);
        resultChanged(null);
    }
    private void update() {
        this.setDisplayName(filter.getName());
    }
    public Filter getFilter() {
        return filter;
    }
    @Override
    protected Sheet createSheet() {
        Sheet s = super.createSheet();
        PropertiesSheet.initializeSheet(getFilter().getProperties(), s);
        return s;
    }
    @Override
    public Action[] getActions(boolean b) {
        return new Action[]{(Action) OpenAction.findObject(OpenAction.class, true), (Action) MoveFilterUpAction.findObject(MoveFilterUpAction.class, true), (Action) MoveFilterDownAction.findObject(MoveFilterDownAction.class, true), (Action) RemoveFilterAction.findObject(RemoveFilterAction.class, true)};
    }
    @Override
    public Action getPreferredAction() {
        return OpenAction.get(OpenAction.class).createContextAwareInstance(Utilities.actionsGlobalContext());
    }
    public void resultChanged(LookupEvent lookupEvent) {
        changed(FilterTopComponent.findInstance());
    }
    public void changed(FilterTopComponent source) {
        setSelected(source.getFilterChain().containsFilter(filter));
    }
}
