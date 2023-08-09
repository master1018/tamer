public class FilterSetting {
    private Set<Filter> filters;
    private String name;
    public FilterSetting() {
        this(null);
    }
    public FilterSetting(String name) {
        this.name = name;
        filters = new HashSet<Filter>();
    }
    public Set<Filter> getFilters() {
        return Collections.unmodifiableSet(filters);
    }
    public void addFilter(Filter f) {
        assert !filters.contains(f);
        filters.add(f);
    }
    public void removeFilter(Filter f) {
        assert filters.contains(f);
        filters.remove(f);
    }
    public boolean containsFilter(Filter f) {
        return filters.contains(f);
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getFilterCount() {
        return filters.size();
    }
    @Override
    public String toString() {
        return getName();
    }
}
