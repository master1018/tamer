public class XMCDACategories extends XMCDAHelperWithVarious {
    private final Set<Category> m_categories = CollectionVariousUtils.newLinkedHashSetNoNull();
    private final Set<Alternative> m_profiles = CollectionVariousUtils.newHashSetNoNull();
    private final Set<Category> m_inactiveCategories = CollectionVariousUtils.newHashSetNoNull();
    public Set<Category> getInactiveCategories() {
        return Sets.newHashSet(m_inactiveCategories);
    }
    public NavigableSet<Category> read(XCategories xCategories) throws InvalidInputException {
        checkNotNull(xCategories);
        m_categories.clear();
        m_inactiveCategories.clear();
        final TreeMap<Double, Category> byRank = Maps.newTreeMap();
        final List<XCategory> xCategoryList = xCategories.getCategoryList();
        for (XCategory xCategory : xCategoryList) {
            final String id = xCategory.getId();
            if (id == null) {
                error("Has no id: " + xCategory + ".");
                continue;
            }
            final Category category = new Category(id);
            if (!xCategory.isSetRank()) {
                error("Has no rank: " + xCategory + ".");
                continue;
            }
            final XValue xRank = xCategory.getRank();
            final Double rank = readDouble(xRank);
            if (rank == null) {
                continue;
            }
            if (xCategory.isSetActive() && !xCategory.getActive()) {
                m_inactiveCategories.add(category);
                continue;
            }
            byRank.put(rank, category);
        }
        final CatsAndProfs catsAndProfs = new CatsAndProfs();
        for (Category category : byRank.descendingMap().values()) {
            catsAndProfs.addCategory(category);
        }
        m_categories.addAll(catsAndProfs.getCategories());
        return catsAndProfs.getCategories();
    }
    public ICatsAndProfs readUsingCategories(XCategoriesProfiles xCategoriesProfiles) throws InvalidInputException {
        Preconditions.checkNotNull(xCategoriesProfiles);
        final BiMap<Alternative, Category> upCategories = HashBiMap.create();
        final BiMap<Alternative, Category> downCategories = HashBiMap.create();
        final List<XCategoryProfile> xCategoryProfileList = xCategoriesProfiles.getCategoryProfileList();
        for (XCategoryProfile xCategoryProfile : xCategoryProfileList) {
            read(xCategoryProfile, downCategories, upCategories);
        }
        assert downCategories.keySet().equals(upCategories.keySet());
        final CatsAndProfs catsAndProfs = new CatsAndProfs();
        for (Category category : m_categories) {
            catsAndProfs.addCategory(category);
        }
        for (Alternative profile : downCategories.keySet()) {
            final Category upper = upCategories.get(profile);
            final Category lower = downCategories.get(profile);
            if (!m_categories.contains(upper)) {
                error("Not found: " + upper + " for " + profile + ".");
                continue;
            }
            if (!m_categories.contains(lower)) {
                error("Not found: " + lower + " for " + profile + ".");
                continue;
            }
            catsAndProfs.setProfileUp(lower.getName(), profile);
            final Category expectedUp = catsAndProfs.getCategoryUp(profile);
            if (!upper.equals(expectedUp)) {
                error("Unexpected upper category: " + upper + ", expected " + expectedUp + ".");
                continue;
            }
        }
        return catsAndProfs;
    }
    private Alternative read(XCategoryProfile xCategoryProfile, Map<Alternative, Category> downCategories, Map<Alternative, Category> upCategories) throws InvalidInputException {
        Alternative profile;
        final List<String> xIdList = xCategoryProfile.getAlternativeIDList();
        if (xIdList.size() > 1) {
            error("More than one id found at " + xCategoryProfile + ".");
            return null;
        }
        if (xIdList.size() == 0) {
            error("No id found at " + xCategoryProfile + ".");
            return null;
        }
        final String profileId = xIdList.get(0);
        if (profileId == null || profileId.isEmpty()) {
            error("Expected profile id at " + xCategoryProfile + ".");
            return null;
        }
        profile = new Alternative(profileId);
        final List<XCategoryProfile.Limits> xLimitsList = xCategoryProfile.getLimitsList();
        final Limits xLimits = getUnique(xLimitsList, xCategoryProfile.toString());
        if (xLimits == null) {
            return null;
        }
        final XCategoryProfile.Limits.LowerCategory xLower = xLimits.getLowerCategory();
        final XCategoryProfile.Limits.UpperCategory xUpper = xLimits.getUpperCategory();
        final String lowerId = xLower.getCategoryID();
        if (lowerId == null || lowerId.isEmpty()) {
            error("Expected lower category id at " + xCategoryProfile + ".");
            return null;
        }
        final Category lower = new Category(lowerId);
        final String upperId = xUpper.getCategoryID();
        if (upperId == null || upperId.isEmpty()) {
            error("Expected upper category id at " + xCategoryProfile + ".");
            return null;
        }
        final Category upper = new Category(upperId);
        if (downCategories.containsKey(profile)) {
            error("Duplicate " + profile + " at " + xCategoryProfile + ".");
            return null;
        }
        if (upCategories.containsKey(profile)) {
            error("Duplicate " + profile + " at " + xCategoryProfile + ".");
            return null;
        }
        if (downCategories.containsValue(lower)) {
            error("Duplicate " + lower + " at " + xCategoryProfile + ".");
            return null;
        }
        if (upCategories.containsValue(upper)) {
            error("Duplicate " + upper + " at " + xCategoryProfile + ".");
            return null;
        }
        downCategories.put(profile, lower);
        upCategories.put(profile, upper);
        return profile;
    }
    public XCategories write(NavigableSet<Category> categories) {
        Preconditions.checkNotNull(categories);
        final XCategories xCategories = XMCDA.Factory.newInstance().addNewCategories();
        int rank = 1;
        for (Category category : categories.descendingSet()) {
            final XCategory xCategory = xCategories.addNewCategory();
            xCategory.setId(category.getName());
            final XValue xRank = xCategory.addNewRank();
            xRank.setInteger(rank);
            ++rank;
        }
        return xCategories;
    }
    public XCategoriesProfiles write(ICatsAndProfs catsAndProfs) {
        Preconditions.checkNotNull(catsAndProfs);
        final XCategoriesProfiles xCategoriesProfiles = XMCDA.Factory.newInstance().addNewCategoriesProfiles();
        for (Alternative profile : catsAndProfs.getProfiles()) {
            final Category down = catsAndProfs.getCategoryDown(profile);
            final Category up = catsAndProfs.getCategoryUp(profile);
            if (down == null) {
                throw new IllegalArgumentException("" + profile);
            }
            if (up == null) {
                throw new IllegalArgumentException("" + profile);
            }
            final XCategoryProfile xCategoryProfile = xCategoriesProfiles.addNewCategoryProfile();
            xCategoryProfile.addAlternativeID(profile.getId());
            final XCategoryProfile.Limits xLimits = xCategoryProfile.addNewLimits();
            final XCategoryProfile.Limits.LowerCategory xLower = xLimits.addNewLowerCategory();
            xLower.setCategoryID(down.getName());
            final XCategoryProfile.Limits.UpperCategory xUpper = xLimits.addNewUpperCategory();
            xUpper.setCategoryID(up.getName());
        }
        return xCategoriesProfiles;
    }
    public Set<Category> getCategories() {
        return m_categories;
    }
    public void setCategories(Set<Category> categories) {
        Preconditions.checkNotNull(categories);
        m_categories.clear();
        m_categories.addAll(categories);
    }
    public Set<Alternative> getProfiles() {
        return m_profiles;
    }
    public void setProfiles(Set<Alternative> profiles) {
        Preconditions.checkNotNull(profiles);
        m_profiles.clear();
        m_profiles.addAll(profiles);
    }
    public ICatsAndProfs read(XCategoriesProfiles xCategoriesProfiles) throws InvalidInputException {
        Preconditions.checkNotNull(xCategoriesProfiles);
        final BiMap<Alternative, Category> upCategories = HashBiMap.create();
        final BiMap<Alternative, Category> downCategories = HashBiMap.create();
        final List<XCategoryProfile> xCategoryProfileList = xCategoriesProfiles.getCategoryProfileList();
        for (XCategoryProfile xCategoryProfile : xCategoryProfileList) {
            read(xCategoryProfile, downCategories, upCategories);
        }
        assert downCategories.keySet().equals(upCategories.keySet());
        final CatsAndProfs catsAndProfs = new CatsAndProfs();
        if (downCategories.isEmpty()) {
            return catsAndProfs;
        }
        final Category cStart = downCategories.values().iterator().next();
        Category worstCategory = cStart;
        while (upCategories.values().contains(worstCategory)) {
            final Alternative previousProfile = upCategories.inverse().get(worstCategory);
            final Category down = downCategories.get(previousProfile);
            worstCategory = down;
        }
        Category category = worstCategory;
        catsAndProfs.addCategory(category.getName());
        while (downCategories.containsValue(category)) {
            final Alternative nextProfile = downCategories.inverse().get(category);
            catsAndProfs.setProfileUp(category.getName(), nextProfile);
            final Category nextCategory = upCategories.get(nextProfile);
            category = nextCategory;
            catsAndProfs.setCategoryUp(nextProfile, category);
        }
        if (catsAndProfs.getProfiles().size() != downCategories.size()) {
            final Set<Alternative> leftOvers = Sets.difference(downCategories.keySet(), catsAndProfs.getProfiles());
            assert leftOvers.size() >= 1;
            error("The graph of neighbors from " + cStart + " is not connected to the whole set of profiles and categories: " + leftOvers.iterator().next() + " is unreachable.");
            return new CatsAndProfs();
        }
        return catsAndProfs;
    }
    public XCategoriesComparisons writeComparisons(NavigableSet<Category> categories) {
        Preconditions.checkNotNull(categories);
        final XCategoriesComparisons xComparisons = XMCDA.Factory.newInstance().addNewCategoriesComparisons();
        final Pairs xPairs = xComparisons.addNewPairs();
        final Iterator<Category> iterator = categories.iterator();
        Category init = iterator.hasNext() ? iterator.next() : null;
        while (iterator.hasNext()) {
            final Category term = iterator.next();
            final Pair xPair = xPairs.addNewPair();
            final XCategoryReference xInitial = xPair.addNewInitial();
            xInitial.setCategoryID(init.getName());
            xPair.addNewTerminal().setCategoryID(term.getName());
            init = term;
        }
        return xComparisons;
    }
    public XMCDACategories() {
        super();
    }
    public XMCDACategories(XMCDAErrorsManager errorsManager) {
        super(errorsManager);
    }
}
