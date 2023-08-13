public class GroupLayout implements LayoutManager2 {
    private static final int MIN_SIZE = 0;
    private static final int PREF_SIZE = 1;
    private static final int MAX_SIZE = 2;
    private static final int SPECIFIC_SIZE = 3;
    private static final int UNSET = Integer.MIN_VALUE;
    public static final int DEFAULT_SIZE = -1;
    public static final int PREFERRED_SIZE = -2;
    private boolean autocreatePadding;
    private boolean autocreateContainerPadding;
    private Group horizontalGroup;
    private Group verticalGroup;
    private Map<Component,ComponentInfo> componentInfos;
    private Container host;
    private Set<Spring> tmpParallelSet;
    private boolean springsChanged;
    private boolean isValid;
    private boolean hasPreferredPaddingSprings;
    private LayoutStyle layoutStyle;
    private boolean honorsVisibility;
    public enum Alignment {
        LEADING,
        TRAILING,
        CENTER,
        BASELINE
    }
    private static void checkSize(int min, int pref, int max,
            boolean isComponentSpring) {
        checkResizeType(min, isComponentSpring);
        if (!isComponentSpring && pref < 0) {
            throw new IllegalArgumentException("Pref must be >= 0");
        } else if (isComponentSpring) {
            checkResizeType(pref, true);
        }
        checkResizeType(max, isComponentSpring);
        checkLessThan(min, pref);
        checkLessThan(pref, max);
    }
    private static void checkResizeType(int type, boolean isComponentSpring) {
        if (type < 0 && ((isComponentSpring && type != DEFAULT_SIZE &&
                type != PREFERRED_SIZE) ||
                (!isComponentSpring && type != PREFERRED_SIZE))) {
            throw new IllegalArgumentException("Invalid size");
        }
    }
    private static void checkLessThan(int min, int max) {
        if (min >= 0 && max >= 0 && min > max) {
            throw new IllegalArgumentException(
                    "Following is not met: min<=pref<=max");
        }
    }
    public GroupLayout(Container host) {
        if (host == null) {
            throw new IllegalArgumentException("Container must be non-null");
        }
        honorsVisibility = true;
        this.host = host;
        setHorizontalGroup(createParallelGroup(Alignment.LEADING, true));
        setVerticalGroup(createParallelGroup(Alignment.LEADING, true));
        componentInfos = new HashMap<Component,ComponentInfo>();
        tmpParallelSet = new HashSet<Spring>();
    }
    public void setHonorsVisibility(boolean honorsVisibility) {
        if (this.honorsVisibility != honorsVisibility) {
            this.honorsVisibility = honorsVisibility;
            springsChanged = true;
            isValid = false;
            invalidateHost();
        }
    }
    public boolean getHonorsVisibility() {
        return honorsVisibility;
    }
    public void setHonorsVisibility(Component component,
            Boolean honorsVisibility) {
        if (component == null) {
            throw new IllegalArgumentException("Component must be non-null");
        }
        getComponentInfo(component).setHonorsVisibility(honorsVisibility);
        springsChanged = true;
        isValid = false;
        invalidateHost();
    }
    public void setAutoCreateGaps(boolean autoCreatePadding) {
        if (this.autocreatePadding != autoCreatePadding) {
            this.autocreatePadding = autoCreatePadding;
            invalidateHost();
        }
    }
    public boolean getAutoCreateGaps() {
        return autocreatePadding;
    }
    public void setAutoCreateContainerGaps(boolean autoCreateContainerPadding){
        if (this.autocreateContainerPadding != autoCreateContainerPadding) {
            this.autocreateContainerPadding = autoCreateContainerPadding;
            horizontalGroup = createTopLevelGroup(getHorizontalGroup());
            verticalGroup = createTopLevelGroup(getVerticalGroup());
            invalidateHost();
        }
    }
    public boolean getAutoCreateContainerGaps() {
        return autocreateContainerPadding;
    }
    public void setHorizontalGroup(Group group) {
        if (group == null) {
            throw new IllegalArgumentException("Group must be non-null");
        }
        horizontalGroup = createTopLevelGroup(group);
        invalidateHost();
    }
    private Group getHorizontalGroup() {
        int index = 0;
        if (horizontalGroup.springs.size() > 1) {
            index = 1;
        }
        return (Group)horizontalGroup.springs.get(index);
    }
    public void setVerticalGroup(Group group) {
        if (group == null) {
            throw new IllegalArgumentException("Group must be non-null");
        }
        verticalGroup = createTopLevelGroup(group);
        invalidateHost();
    }
    private Group getVerticalGroup() {
        int index = 0;
        if (verticalGroup.springs.size() > 1) {
            index = 1;
        }
        return (Group)verticalGroup.springs.get(index);
    }
    private Group createTopLevelGroup(Group specifiedGroup) {
        SequentialGroup group = createSequentialGroup();
        if (getAutoCreateContainerGaps()) {
            group.addSpring(new ContainerAutoPreferredGapSpring());
            group.addGroup(specifiedGroup);
            group.addSpring(new ContainerAutoPreferredGapSpring());
        } else {
            group.addGroup(specifiedGroup);
        }
        return group;
    }
    public SequentialGroup createSequentialGroup() {
        return new SequentialGroup();
    }
    public ParallelGroup createParallelGroup() {
        return createParallelGroup(Alignment.LEADING);
    }
    public ParallelGroup createParallelGroup(Alignment alignment) {
        return createParallelGroup(alignment, true);
    }
    public ParallelGroup createParallelGroup(Alignment alignment,
            boolean resizable){
        if (alignment == null) {
            throw new IllegalArgumentException("alignment must be non null");
        }
        if (alignment == Alignment.BASELINE) {
            return new BaselineGroup(resizable);
        }
        return new ParallelGroup(alignment, resizable);
    }
    public ParallelGroup createBaselineGroup(boolean resizable,
            boolean anchorBaselineToTop) {
        return new BaselineGroup(resizable, anchorBaselineToTop);
    }
    public void linkSize(Component... components) {
        linkSize(SwingConstants.HORIZONTAL, components);
        linkSize(SwingConstants.VERTICAL, components);
    }
    public void linkSize(int axis, Component... components) {
        if (components == null) {
            throw new IllegalArgumentException("Components must be non-null");
        }
        for (int counter = components.length - 1; counter >= 0; counter--) {
            Component c = components[counter];
            if (components[counter] == null) {
                throw new IllegalArgumentException(
                        "Components must be non-null");
            }
            getComponentInfo(c);
        }
        int glAxis;
        if (axis == SwingConstants.HORIZONTAL) {
            glAxis = HORIZONTAL;
        } else if (axis == SwingConstants.VERTICAL) {
            glAxis = VERTICAL;
        } else {
            throw new IllegalArgumentException("Axis must be one of " +
                    "SwingConstants.HORIZONTAL or SwingConstants.VERTICAL");
        }
        LinkInfo master = getComponentInfo(
                components[components.length - 1]).getLinkInfo(glAxis);
        for (int counter = components.length - 2; counter >= 0; counter--) {
            master.add(getComponentInfo(components[counter]));
        }
        invalidateHost();
    }
    public void replace(Component existingComponent, Component newComponent) {
        if (existingComponent == null || newComponent == null) {
            throw new IllegalArgumentException("Components must be non-null");
        }
        if (springsChanged) {
            registerComponents(horizontalGroup, HORIZONTAL);
            registerComponents(verticalGroup, VERTICAL);
        }
        ComponentInfo info = componentInfos.remove(existingComponent);
        if (info == null) {
            throw new IllegalArgumentException("Component must already exist");
        }
        host.remove(existingComponent);
        if (newComponent.getParent() != host) {
            host.add(newComponent);
        }
        info.setComponent(newComponent);
        componentInfos.put(newComponent, info);
        invalidateHost();
    }
    public void setLayoutStyle(LayoutStyle layoutStyle) {
        this.layoutStyle = layoutStyle;
        invalidateHost();
    }
    public LayoutStyle getLayoutStyle() {
        return layoutStyle;
    }
    private LayoutStyle getLayoutStyle0() {
        LayoutStyle layoutStyle = getLayoutStyle();
        if (layoutStyle == null) {
            layoutStyle = LayoutStyle.getInstance();
        }
        return layoutStyle;
    }
    private void invalidateHost() {
        if (host instanceof JComponent) {
            ((JComponent)host).revalidate();
        } else {
            host.invalidate();
        }
        host.repaint();
    }
    public void addLayoutComponent(String name, Component component) {
    }
    public void removeLayoutComponent(Component component) {
        ComponentInfo info = componentInfos.remove(component);
        if (info != null) {
            info.dispose();
            springsChanged = true;
            isValid = false;
        }
    }
    public Dimension preferredLayoutSize(Container parent) {
        checkParent(parent);
        prepare(PREF_SIZE);
        return adjustSize(horizontalGroup.getPreferredSize(HORIZONTAL),
                verticalGroup.getPreferredSize(VERTICAL));
    }
    public Dimension minimumLayoutSize(Container parent) {
        checkParent(parent);
        prepare(MIN_SIZE);
        return adjustSize(horizontalGroup.getMinimumSize(HORIZONTAL),
                verticalGroup.getMinimumSize(VERTICAL));
    }
    public void layoutContainer(Container parent) {
        prepare(SPECIFIC_SIZE);
        Insets insets = parent.getInsets();
        int width = parent.getWidth() - insets.left - insets.right;
        int height = parent.getHeight() - insets.top - insets.bottom;
        boolean ltr = isLeftToRight();
        if (getAutoCreateGaps() || getAutoCreateContainerGaps() ||
                hasPreferredPaddingSprings) {
            calculateAutopadding(horizontalGroup, HORIZONTAL, SPECIFIC_SIZE, 0,
                    width);
            calculateAutopadding(verticalGroup, VERTICAL, SPECIFIC_SIZE, 0,
                    height);
        }
        horizontalGroup.setSize(HORIZONTAL, 0, width);
        verticalGroup.setSize(VERTICAL, 0, height);
        for (ComponentInfo info : componentInfos.values()) {
            info.setBounds(insets, width, ltr);
        }
    }
    public void addLayoutComponent(Component component, Object constraints) {
    }
    public Dimension maximumLayoutSize(Container parent) {
        checkParent(parent);
        prepare(MAX_SIZE);
        return adjustSize(horizontalGroup.getMaximumSize(HORIZONTAL),
                verticalGroup.getMaximumSize(VERTICAL));
    }
    public float getLayoutAlignmentX(Container parent) {
        checkParent(parent);
        return .5f;
    }
    public float getLayoutAlignmentY(Container parent) {
        checkParent(parent);
        return .5f;
    }
    public void invalidateLayout(Container parent) {
        checkParent(parent);
        synchronized(parent.getTreeLock()) {
            isValid = false;
        }
    }
    private void prepare(int sizeType) {
        boolean visChanged = false;
        if (!isValid) {
            isValid = true;
            horizontalGroup.setSize(HORIZONTAL, UNSET, UNSET);
            verticalGroup.setSize(VERTICAL, UNSET, UNSET);
            for (ComponentInfo ci : componentInfos.values()) {
                if (ci.updateVisibility()) {
                    visChanged = true;
                }
                ci.clearCachedSize();
            }
        }
        if (springsChanged) {
            registerComponents(horizontalGroup, HORIZONTAL);
            registerComponents(verticalGroup, VERTICAL);
        }
        if (springsChanged || visChanged) {
            checkComponents();
            horizontalGroup.removeAutopadding();
            verticalGroup.removeAutopadding();
            if (getAutoCreateGaps()) {
                insertAutopadding(true);
            } else if (hasPreferredPaddingSprings ||
                    getAutoCreateContainerGaps()) {
                insertAutopadding(false);
            }
            springsChanged = false;
        }
        if (sizeType != SPECIFIC_SIZE && (getAutoCreateGaps() ||
                getAutoCreateContainerGaps() || hasPreferredPaddingSprings)) {
            calculateAutopadding(horizontalGroup, HORIZONTAL, sizeType, 0, 0);
            calculateAutopadding(verticalGroup, VERTICAL, sizeType, 0, 0);
        }
    }
    private void calculateAutopadding(Group group, int axis, int sizeType,
            int origin, int size) {
        group.unsetAutopadding();
        switch(sizeType) {
            case MIN_SIZE:
                size = group.getMinimumSize(axis);
                break;
            case PREF_SIZE:
                size = group.getPreferredSize(axis);
                break;
            case MAX_SIZE:
                size = group.getMaximumSize(axis);
                break;
            default:
                break;
        }
        group.setSize(axis, origin, size);
        group.calculateAutopadding(axis);
    }
    private void checkComponents() {
        for (ComponentInfo info : componentInfos.values()) {
            if (info.horizontalSpring == null) {
                throw new IllegalStateException(info.component +
                        " is not attached to a horizontal group");
            }
            if (info.verticalSpring == null) {
                throw new IllegalStateException(info.component +
                        " is not attached to a vertical group");
            }
        }
    }
    private void registerComponents(Group group, int axis) {
        List<Spring> springs = group.springs;
        for (int counter = springs.size() - 1; counter >= 0; counter--) {
            Spring spring = springs.get(counter);
            if (spring instanceof ComponentSpring) {
                ((ComponentSpring)spring).installIfNecessary(axis);
            } else if (spring instanceof Group) {
                registerComponents((Group)spring, axis);
            }
        }
    }
    private Dimension adjustSize(int width, int height) {
        Insets insets = host.getInsets();
        return new Dimension(width + insets.left + insets.right,
                height + insets.top + insets.bottom);
    }
    private void checkParent(Container parent) {
        if (parent != host) {
            throw new IllegalArgumentException(
                    "GroupLayout can only be used with one Container at a time");
        }
    }
    private ComponentInfo getComponentInfo(Component component) {
        ComponentInfo info = componentInfos.get(component);
        if (info == null) {
            info = new ComponentInfo(component);
            componentInfos.put(component, info);
            if (component.getParent() != host) {
                host.add(component);
            }
        }
        return info;
    }
    private void insertAutopadding(boolean insert) {
        horizontalGroup.insertAutopadding(HORIZONTAL,
                new ArrayList<AutoPreferredGapSpring>(1),
                new ArrayList<AutoPreferredGapSpring>(1),
                new ArrayList<ComponentSpring>(1),
                new ArrayList<ComponentSpring>(1), insert);
        verticalGroup.insertAutopadding(VERTICAL,
                new ArrayList<AutoPreferredGapSpring>(1),
                new ArrayList<AutoPreferredGapSpring>(1),
                new ArrayList<ComponentSpring>(1),
                new ArrayList<ComponentSpring>(1), insert);
    }
    private boolean areParallelSiblings(Component source, Component target,
            int axis) {
        ComponentInfo sourceInfo = getComponentInfo(source);
        ComponentInfo targetInfo = getComponentInfo(target);
        Spring sourceSpring;
        Spring targetSpring;
        if (axis == HORIZONTAL) {
            sourceSpring = sourceInfo.horizontalSpring;
            targetSpring = targetInfo.horizontalSpring;
        } else {
            sourceSpring = sourceInfo.verticalSpring;
            targetSpring = targetInfo.verticalSpring;
        }
        Set<Spring> sourcePath = tmpParallelSet;
        sourcePath.clear();
        Spring spring = sourceSpring.getParent();
        while (spring != null) {
            sourcePath.add(spring);
            spring = spring.getParent();
        }
        spring = targetSpring.getParent();
        while (spring != null) {
            if (sourcePath.contains(spring)) {
                sourcePath.clear();
                while (spring != null) {
                    if (spring instanceof ParallelGroup) {
                        return true;
                    }
                    spring = spring.getParent();
                }
                return false;
            }
            spring = spring.getParent();
        }
        sourcePath.clear();
        return false;
    }
    private boolean isLeftToRight() {
        return host.getComponentOrientation().isLeftToRight();
    }
    public String toString() {
        if (springsChanged) {
            registerComponents(horizontalGroup, HORIZONTAL);
            registerComponents(verticalGroup, VERTICAL);
        }
        StringBuffer buffer = new StringBuffer();
        buffer.append("HORIZONTAL\n");
        createSpringDescription(buffer, horizontalGroup, "  ", HORIZONTAL);
        buffer.append("\nVERTICAL\n");
        createSpringDescription(buffer, verticalGroup, "  ", VERTICAL);
        return buffer.toString();
    }
    private void createSpringDescription(StringBuffer buffer, Spring spring,
            String indent, int axis) {
        String origin = "";
        String padding = "";
        if (spring instanceof ComponentSpring) {
            ComponentSpring cSpring = (ComponentSpring)spring;
            origin = Integer.toString(cSpring.getOrigin()) + " ";
            String name = cSpring.getComponent().getName();
            if (name != null) {
                origin = "name=" + name + ", ";
            }
        }
        if (spring instanceof AutoPreferredGapSpring) {
            AutoPreferredGapSpring paddingSpring =
                    (AutoPreferredGapSpring)spring;
            padding = ", userCreated=" + paddingSpring.getUserCreated() +
                    ", matches=" + paddingSpring.getMatchDescription();
        }
        buffer.append(indent + spring.getClass().getName() + " " +
                Integer.toHexString(spring.hashCode()) + " " +
                origin +
                ", size=" + spring.getSize() +
                ", alignment=" + spring.getAlignment() +
                " prefs=[" + spring.getMinimumSize(axis) +
                " " + spring.getPreferredSize(axis) +
                " " + spring.getMaximumSize(axis) +
                padding + "]\n");
        if (spring instanceof Group) {
            List<Spring> springs = ((Group)spring).springs;
            indent += "  ";
            for (int counter = 0; counter < springs.size(); counter++) {
                createSpringDescription(buffer, springs.get(counter), indent,
                        axis);
            }
        }
    }
    private abstract class Spring {
        private int size;
        private int min;
        private int max;
        private int pref;
        private Spring parent;
        private Alignment alignment;
        Spring() {
            min = pref = max = UNSET;
        }
        abstract int calculateMinimumSize(int axis);
        abstract int calculatePreferredSize(int axis);
        abstract int calculateMaximumSize(int axis);
        void setParent(Spring parent) {
            this.parent = parent;
        }
        Spring getParent() {
            return parent;
        }
        void setAlignment(Alignment alignment) {
            this.alignment = alignment;
        }
        Alignment getAlignment() {
            return alignment;
        }
        final int getMinimumSize(int axis) {
            if (min == UNSET) {
                min = constrain(calculateMinimumSize(axis));
            }
            return min;
        }
        final int getPreferredSize(int axis) {
            if (pref == UNSET) {
                pref = constrain(calculatePreferredSize(axis));
            }
            return pref;
        }
        final int getMaximumSize(int axis) {
            if (max == UNSET) {
                max = constrain(calculateMaximumSize(axis));
            }
            return max;
        }
        void setSize(int axis, int origin, int size) {
            this.size = size;
            if (size == UNSET) {
                unset();
            }
        }
        void unset() {
            size = min = pref = max = UNSET;
        }
        int getSize() {
            return size;
        }
        int constrain(int value) {
            return Math.min(value, Short.MAX_VALUE);
        }
        int getBaseline() {
            return -1;
        }
        BaselineResizeBehavior getBaselineResizeBehavior() {
            return BaselineResizeBehavior.OTHER;
        }
        final boolean isResizable(int axis) {
            int min = getMinimumSize(axis);
            int pref = getPreferredSize(axis);
            return (min != pref || pref != getMaximumSize(axis));
        }
        abstract boolean willHaveZeroSize(boolean treatAutopaddingAsZeroSized);
    }
    public abstract class Group extends Spring {
        List<Spring> springs;
        Group() {
            springs = new ArrayList<Spring>();
        }
        public Group addGroup(Group group) {
            return addSpring(group);
        }
        public Group addComponent(Component component) {
            return addComponent(component, DEFAULT_SIZE, DEFAULT_SIZE,
                    DEFAULT_SIZE);
        }
        public Group addComponent(Component component, int min, int pref,
                int max) {
            return addSpring(new ComponentSpring(component, min, pref, max));
        }
        public Group addGap(int size) {
            return addGap(size, size, size);
        }
        public Group addGap(int min, int pref, int max) {
            return addSpring(new GapSpring(min, pref, max));
        }
        Spring getSpring(int index) {
            return springs.get(index);
        }
        int indexOf(Spring spring) {
            return springs.indexOf(spring);
        }
        Group addSpring(Spring spring) {
            springs.add(spring);
            spring.setParent(this);
            if (!(spring instanceof AutoPreferredGapSpring) ||
                    !((AutoPreferredGapSpring)spring).getUserCreated()) {
                springsChanged = true;
            }
            return this;
        }
        void setSize(int axis, int origin, int size) {
            super.setSize(axis, origin, size);
            if (size == UNSET) {
                for (int counter = springs.size() - 1; counter >= 0;
                counter--) {
                    getSpring(counter).setSize(axis, origin, size);
                }
            } else {
                setValidSize(axis, origin, size);
            }
        }
        abstract void setValidSize(int axis, int origin, int size);
        int calculateMinimumSize(int axis) {
            return calculateSize(axis, MIN_SIZE);
        }
        int calculatePreferredSize(int axis) {
            return calculateSize(axis, PREF_SIZE);
        }
        int calculateMaximumSize(int axis) {
            return calculateSize(axis, MAX_SIZE);
        }
        int calculateSize(int axis, int type) {
            int count = springs.size();
            if (count == 0) {
                return 0;
            }
            if (count == 1) {
                return getSpringSize(getSpring(0), axis, type);
            }
            int size = constrain(operator(getSpringSize(getSpring(0), axis,
                    type), getSpringSize(getSpring(1), axis, type)));
            for (int counter = 2; counter < count; counter++) {
                size = constrain(operator(size, getSpringSize(
                        getSpring(counter), axis, type)));
            }
            return size;
        }
        int getSpringSize(Spring spring, int axis, int type) {
            switch(type) {
                case MIN_SIZE:
                    return spring.getMinimumSize(axis);
                case PREF_SIZE:
                    return spring.getPreferredSize(axis);
                case MAX_SIZE:
                    return spring.getMaximumSize(axis);
            }
            assert false;
            return 0;
        }
        abstract int operator(int a, int b);
        abstract void insertAutopadding(int axis,
                List<AutoPreferredGapSpring> leadingPadding,
                List<AutoPreferredGapSpring> trailingPadding,
                List<ComponentSpring> leading, List<ComponentSpring> trailing,
                boolean insert);
        void removeAutopadding() {
            unset();
            for (int counter = springs.size() - 1; counter >= 0; counter--) {
                Spring spring = springs.get(counter);
                if (spring instanceof AutoPreferredGapSpring) {
                    if (((AutoPreferredGapSpring)spring).getUserCreated()) {
                        ((AutoPreferredGapSpring)spring).reset();
                    } else {
                        springs.remove(counter);
                    }
                } else if (spring instanceof Group) {
                    ((Group)spring).removeAutopadding();
                }
            }
        }
        void unsetAutopadding() {
            unset();
            for (int counter = springs.size() - 1; counter >= 0; counter--) {
                Spring spring = springs.get(counter);
                if (spring instanceof AutoPreferredGapSpring) {
                    spring.unset();
                } else if (spring instanceof Group) {
                    ((Group)spring).unsetAutopadding();
                }
            }
        }
        void calculateAutopadding(int axis) {
            for (int counter = springs.size() - 1; counter >= 0; counter--) {
                Spring spring = springs.get(counter);
                if (spring instanceof AutoPreferredGapSpring) {
                    spring.unset();
                    ((AutoPreferredGapSpring)spring).calculatePadding(axis);
                } else if (spring instanceof Group) {
                    ((Group)spring).calculateAutopadding(axis);
                }
            }
            unset();
        }
        @Override
        boolean willHaveZeroSize(boolean treatAutopaddingAsZeroSized) {
            for (int i = springs.size() - 1; i >= 0; i--) {
                Spring spring = springs.get(i);
                if (!spring.willHaveZeroSize(treatAutopaddingAsZeroSized)) {
                    return false;
                }
            }
            return true;
        }
    }
    public class SequentialGroup extends Group {
        private Spring baselineSpring;
        SequentialGroup() {
        }
        public SequentialGroup addGroup(Group group) {
            return (SequentialGroup)super.addGroup(group);
        }
        public SequentialGroup addGroup(boolean useAsBaseline, Group group) {
            super.addGroup(group);
            if (useAsBaseline) {
                baselineSpring = group;
            }
            return this;
        }
        public SequentialGroup addComponent(Component component) {
            return (SequentialGroup)super.addComponent(component);
        }
        public SequentialGroup addComponent(boolean useAsBaseline,
                Component component) {
            super.addComponent(component);
            if (useAsBaseline) {
                baselineSpring = springs.get(springs.size() - 1);
            }
            return this;
        }
        public SequentialGroup addComponent(Component component, int min,
                int pref, int max) {
            return (SequentialGroup)super.addComponent(
                    component, min, pref, max);
        }
        public SequentialGroup addComponent(boolean useAsBaseline,
                Component component, int min, int pref, int max) {
            super.addComponent(component, min, pref, max);
            if (useAsBaseline) {
                baselineSpring = springs.get(springs.size() - 1);
            }
            return this;
        }
        public SequentialGroup addGap(int size) {
            return (SequentialGroup)super.addGap(size);
        }
        public SequentialGroup addGap(int min, int pref, int max) {
            return (SequentialGroup)super.addGap(min, pref, max);
        }
        public SequentialGroup addPreferredGap(JComponent comp1,
                JComponent comp2, ComponentPlacement type) {
            return addPreferredGap(comp1, comp2, type, DEFAULT_SIZE,
                    PREFERRED_SIZE);
        }
        public SequentialGroup addPreferredGap(JComponent comp1,
                JComponent comp2, ComponentPlacement type, int pref,
                int max) {
            if (type == null) {
                throw new IllegalArgumentException("Type must be non-null");
            }
            if (comp1 == null || comp2 == null) {
                throw new IllegalArgumentException(
                        "Components must be non-null");
            }
            checkPreferredGapValues(pref, max);
            return (SequentialGroup)addSpring(new PreferredGapSpring(
                    comp1, comp2, type, pref, max));
        }
        public SequentialGroup addPreferredGap(ComponentPlacement type) {
            return addPreferredGap(type, DEFAULT_SIZE, DEFAULT_SIZE);
        }
        public SequentialGroup addPreferredGap(ComponentPlacement type,
                int pref, int max) {
            if (type != ComponentPlacement.RELATED &&
                    type != ComponentPlacement.UNRELATED) {
                throw new IllegalArgumentException(
                        "Type must be one of " +
                        "LayoutStyle.ComponentPlacement.RELATED or " +
                        "LayoutStyle.ComponentPlacement.UNRELATED");
            }
            checkPreferredGapValues(pref, max);
            hasPreferredPaddingSprings = true;
            return (SequentialGroup)addSpring(new AutoPreferredGapSpring(
                    type, pref, max));
        }
        public SequentialGroup addContainerGap() {
            return addContainerGap(DEFAULT_SIZE, DEFAULT_SIZE);
        }
        public SequentialGroup addContainerGap(int pref, int max) {
            if ((pref < 0 && pref != DEFAULT_SIZE) ||
                    (max < 0 && max != DEFAULT_SIZE && max != PREFERRED_SIZE)||
                    (pref >= 0 && max >= 0 && pref > max)) {
                throw new IllegalArgumentException(
                        "Pref and max must be either DEFAULT_VALUE " +
                        "or >= 0 and pref <= max");
            }
            hasPreferredPaddingSprings = true;
            return (SequentialGroup)addSpring(
                    new ContainerAutoPreferredGapSpring(pref, max));
        }
        int operator(int a, int b) {
            return constrain(a) + constrain(b);
        }
        void setValidSize(int axis, int origin, int size) {
            int pref = getPreferredSize(axis);
            if (size == pref) {
                for (Spring spring : springs) {
                    int springPref = spring.getPreferredSize(axis);
                    spring.setSize(axis, origin, springPref);
                    origin += springPref;
                }
            } else if (springs.size() == 1) {
                Spring spring = getSpring(0);
                spring.setSize(axis, origin, Math.min(
                        Math.max(size, spring.getMinimumSize(axis)),
                        spring.getMaximumSize(axis)));
            } else if (springs.size() > 1) {
                setValidSizeNotPreferred(axis, origin, size);
            }
        }
        private void setValidSizeNotPreferred(int axis, int origin, int size) {
            int delta = size - getPreferredSize(axis);
            assert delta != 0;
            boolean useMin = (delta < 0);
            int springCount = springs.size();
            if (useMin) {
                delta *= -1;
            }
            List<SpringDelta> resizable = buildResizableList(axis, useMin);
            int resizableCount = resizable.size();
            if (resizableCount > 0) {
                int sDelta = delta / resizableCount;
                int slop = delta - sDelta * resizableCount;
                int[] sizes = new int[springCount];
                int sign = useMin ? -1 : 1;
                for (int counter = 0; counter < resizableCount; counter++) {
                    SpringDelta springDelta = resizable.get(counter);
                    if ((counter + 1) == resizableCount) {
                        sDelta += slop;
                    }
                    springDelta.delta = Math.min(sDelta, springDelta.delta);
                    delta -= springDelta.delta;
                    if (springDelta.delta != sDelta && counter + 1 <
                            resizableCount) {
                        sDelta = delta / (resizableCount - counter - 1);
                        slop = delta - sDelta * (resizableCount - counter - 1);
                    }
                    sizes[springDelta.index] = sign * springDelta.delta;
                }
                for (int counter = 0; counter < springCount; counter++) {
                    Spring spring = getSpring(counter);
                    int sSize = spring.getPreferredSize(axis) + sizes[counter];
                    spring.setSize(axis, origin, sSize);
                    origin += sSize;
                }
            } else {
                for (int counter = 0; counter < springCount; counter++) {
                    Spring spring = getSpring(counter);
                    int sSize;
                    if (useMin) {
                        sSize = spring.getMinimumSize(axis);
                    } else {
                        sSize = spring.getMaximumSize(axis);
                    }
                    spring.setSize(axis, origin, sSize);
                    origin += sSize;
                }
            }
        }
        private List<SpringDelta> buildResizableList(int axis,
                boolean useMin) {
            int size = springs.size();
            List<SpringDelta> sorted = new ArrayList<SpringDelta>(size);
            for (int counter = 0; counter < size; counter++) {
                Spring spring = getSpring(counter);
                int sDelta;
                if (useMin) {
                    sDelta = spring.getPreferredSize(axis) -
                            spring.getMinimumSize(axis);
                } else {
                    sDelta = spring.getMaximumSize(axis) -
                            spring.getPreferredSize(axis);
                }
                if (sDelta > 0) {
                    sorted.add(new SpringDelta(counter, sDelta));
                }
            }
            Collections.sort(sorted);
            return sorted;
        }
        private int indexOfNextNonZeroSpring(
                int index, boolean treatAutopaddingAsZeroSized) {
            while (index < springs.size()) {
                Spring spring = springs.get(index);
                if (!spring.willHaveZeroSize(treatAutopaddingAsZeroSized)) {
                    return index;
                }
                index++;
            }
            return index;
        }
        @Override
        void insertAutopadding(int axis,
                List<AutoPreferredGapSpring> leadingPadding,
                List<AutoPreferredGapSpring> trailingPadding,
                List<ComponentSpring> leading, List<ComponentSpring> trailing,
                boolean insert) {
            List<AutoPreferredGapSpring> newLeadingPadding =
                    new ArrayList<AutoPreferredGapSpring>(leadingPadding);
            List<AutoPreferredGapSpring> newTrailingPadding =
                    new ArrayList<AutoPreferredGapSpring>(1);
            List<ComponentSpring> newLeading =
                    new ArrayList<ComponentSpring>(leading);
            List<ComponentSpring> newTrailing = null;
            int counter = 0;
            while (counter < springs.size()) {
                Spring spring = getSpring(counter);
                if (spring instanceof AutoPreferredGapSpring) {
                    if (newLeadingPadding.size() == 0) {
                        AutoPreferredGapSpring padding =
                            (AutoPreferredGapSpring)spring;
                        padding.setSources(newLeading);
                        newLeading.clear();
                        counter = indexOfNextNonZeroSpring(counter + 1, true);
                        if (counter == springs.size()) {
                            if (!(padding instanceof
                                  ContainerAutoPreferredGapSpring)) {
                                trailingPadding.add(padding);
                            }
                        } else {
                            newLeadingPadding.clear();
                            newLeadingPadding.add(padding);
                        }
                    } else {
                        counter = indexOfNextNonZeroSpring(counter + 1, true);
                    }
                } else {
                    if (newLeading.size() > 0 && insert) {
                        AutoPreferredGapSpring padding =
                                new AutoPreferredGapSpring();
                        springs.add(counter, padding);
                        continue;
                    }
                    if (spring instanceof ComponentSpring) {
                        ComponentSpring cSpring = (ComponentSpring)spring;
                        if (!cSpring.isVisible()) {
                            counter++;
                            continue;
                        }
                        for (AutoPreferredGapSpring gapSpring : newLeadingPadding) {
                            gapSpring.addTarget(cSpring, axis);
                        }
                        newLeading.clear();
                        newLeadingPadding.clear();
                        counter = indexOfNextNonZeroSpring(counter + 1, false);
                        if (counter == springs.size()) {
                            trailing.add(cSpring);
                        } else {
                            newLeading.add(cSpring);
                        }
                    } else if (spring instanceof Group) {
                        if (newTrailing == null) {
                            newTrailing = new ArrayList<ComponentSpring>(1);
                        } else {
                            newTrailing.clear();
                        }
                        newTrailingPadding.clear();
                        ((Group)spring).insertAutopadding(axis,
                                newLeadingPadding, newTrailingPadding,
                                newLeading, newTrailing, insert);
                        newLeading.clear();
                        newLeadingPadding.clear();
                        counter = indexOfNextNonZeroSpring(
                                    counter + 1, (newTrailing.size() == 0));
                        if (counter == springs.size()) {
                            trailing.addAll(newTrailing);
                            trailingPadding.addAll(newTrailingPadding);
                        } else {
                            newLeading.addAll(newTrailing);
                            newLeadingPadding.addAll(newTrailingPadding);
                        }
                    } else {
                        newLeadingPadding.clear();
                        newLeading.clear();
                        counter++;
                    }
                }
            }
        }
        int getBaseline() {
            if (baselineSpring != null) {
                int baseline = baselineSpring.getBaseline();
                if (baseline >= 0) {
                    int size = 0;
                    for (Spring spring : springs) {
                        if (spring == baselineSpring) {
                            return size + baseline;
                        } else {
                            size += spring.getPreferredSize(VERTICAL);
                        }
                    }
                }
            }
            return -1;
        }
        BaselineResizeBehavior getBaselineResizeBehavior() {
            if (isResizable(VERTICAL)) {
                if (!baselineSpring.isResizable(VERTICAL)) {
                    boolean leadingResizable = false;
                    for (Spring spring : springs) {
                        if (spring == baselineSpring) {
                            break;
                        } else if (spring.isResizable(VERTICAL)) {
                            leadingResizable = true;
                            break;
                        }
                    }
                    boolean trailingResizable = false;
                    for (int i = springs.size() - 1; i >= 0; i--) {
                        Spring spring = springs.get(i);
                        if (spring == baselineSpring) {
                            break;
                        }
                        if (spring.isResizable(VERTICAL)) {
                            trailingResizable = true;
                            break;
                        }
                    }
                    if (leadingResizable && !trailingResizable) {
                        return BaselineResizeBehavior.CONSTANT_DESCENT;
                    } else if (!leadingResizable && trailingResizable) {
                        return BaselineResizeBehavior.CONSTANT_ASCENT;
                    }
                } else {
                    BaselineResizeBehavior brb = baselineSpring.getBaselineResizeBehavior();
                    if (brb == BaselineResizeBehavior.CONSTANT_ASCENT) {
                        for (Spring spring : springs) {
                            if (spring == baselineSpring) {
                                return BaselineResizeBehavior.CONSTANT_ASCENT;
                            }
                            if (spring.isResizable(VERTICAL)) {
                                return BaselineResizeBehavior.OTHER;
                            }
                        }
                    } else if (brb == BaselineResizeBehavior.CONSTANT_DESCENT) {
                        for (int i = springs.size() - 1; i >= 0; i--) {
                            Spring spring = springs.get(i);
                            if (spring == baselineSpring) {
                                return BaselineResizeBehavior.CONSTANT_DESCENT;
                            }
                            if (spring.isResizable(VERTICAL)) {
                                return BaselineResizeBehavior.OTHER;
                            }
                        }
                    }
                }
                return BaselineResizeBehavior.OTHER;
            }
            return BaselineResizeBehavior.CONSTANT_ASCENT;
        }
        private void checkPreferredGapValues(int pref, int max) {
            if ((pref < 0 && pref != DEFAULT_SIZE && pref != PREFERRED_SIZE) ||
                    (max < 0 && max != DEFAULT_SIZE && max != PREFERRED_SIZE)||
                    (pref >= 0 && max >= 0 && pref > max)) {
                throw new IllegalArgumentException(
                        "Pref and max must be either DEFAULT_SIZE, " +
                        "PREFERRED_SIZE, or >= 0 and pref <= max");
            }
        }
    }
    private static final class SpringDelta implements Comparable<SpringDelta> {
        public final int index;
        public int delta;
        public SpringDelta(int index, int delta) {
            this.index = index;
            this.delta = delta;
        }
        public int compareTo(SpringDelta o) {
            return delta - o.delta;
        }
        public String toString() {
            return super.toString() + "[index=" + index + ", delta=" +
                    delta + "]";
        }
    }
    public class ParallelGroup extends Group {
        private final Alignment childAlignment;
        private final boolean resizable;
        ParallelGroup(Alignment childAlignment, boolean resizable) {
            this.childAlignment = childAlignment;
            this.resizable = resizable;
        }
        public ParallelGroup addGroup(Group group) {
            return (ParallelGroup)super.addGroup(group);
        }
        public ParallelGroup addComponent(Component component) {
            return (ParallelGroup)super.addComponent(component);
        }
        public ParallelGroup addComponent(Component component, int min, int pref,
                int max) {
            return (ParallelGroup)super.addComponent(component, min, pref, max);
        }
        public ParallelGroup addGap(int pref) {
            return (ParallelGroup)super.addGap(pref);
        }
        public ParallelGroup addGap(int min, int pref, int max) {
            return (ParallelGroup)super.addGap(min, pref, max);
        }
        public ParallelGroup addGroup(Alignment alignment, Group group) {
            checkChildAlignment(alignment);
            group.setAlignment(alignment);
            return (ParallelGroup)addSpring(group);
        }
        public ParallelGroup addComponent(Component component,
                Alignment alignment) {
            return addComponent(component, alignment, DEFAULT_SIZE, DEFAULT_SIZE,
                    DEFAULT_SIZE);
        }
        public ParallelGroup addComponent(Component component,
                Alignment alignment, int min, int pref, int max) {
            checkChildAlignment(alignment);
            ComponentSpring spring = new ComponentSpring(component,
                    min, pref, max);
            spring.setAlignment(alignment);
            return (ParallelGroup)addSpring(spring);
        }
        boolean isResizable() {
            return resizable;
        }
        int operator(int a, int b) {
            return Math.max(a, b);
        }
        int calculateMinimumSize(int axis) {
            if (!isResizable()) {
                return getPreferredSize(axis);
            }
            return super.calculateMinimumSize(axis);
        }
        int calculateMaximumSize(int axis) {
            if (!isResizable()) {
                return getPreferredSize(axis);
            }
            return super.calculateMaximumSize(axis);
        }
        void setValidSize(int axis, int origin, int size) {
            for (Spring spring : springs) {
                setChildSize(spring, axis, origin, size);
            }
        }
        void setChildSize(Spring spring, int axis, int origin, int size) {
            Alignment alignment = spring.getAlignment();
            int springSize = Math.min(
                    Math.max(spring.getMinimumSize(axis), size),
                    spring.getMaximumSize(axis));
            if (alignment == null) {
                alignment = childAlignment;
            }
            switch (alignment) {
                case TRAILING:
                    spring.setSize(axis, origin + size - springSize,
                            springSize);
                    break;
                case CENTER:
                    spring.setSize(axis, origin +
                            (size - springSize) / 2,springSize);
                    break;
                default: 
                    spring.setSize(axis, origin, springSize);
                    break;
            }
        }
        @Override
        void insertAutopadding(int axis,
                List<AutoPreferredGapSpring> leadingPadding,
                List<AutoPreferredGapSpring> trailingPadding,
                List<ComponentSpring> leading, List<ComponentSpring> trailing,
                boolean insert) {
            for (Spring spring : springs) {
                if (spring instanceof ComponentSpring) {
                    if (((ComponentSpring)spring).isVisible()) {
                        for (AutoPreferredGapSpring gapSpring :
                                 leadingPadding) {
                            gapSpring.addTarget((ComponentSpring)spring, axis);
                        }
                        trailing.add((ComponentSpring)spring);
                    }
                } else if (spring instanceof Group) {
                    ((Group)spring).insertAutopadding(axis, leadingPadding,
                            trailingPadding, leading, trailing, insert);
                } else if (spring instanceof AutoPreferredGapSpring) {
                    ((AutoPreferredGapSpring)spring).setSources(leading);
                    trailingPadding.add((AutoPreferredGapSpring)spring);
                }
            }
        }
        private void checkChildAlignment(Alignment alignment) {
            checkChildAlignment(alignment, (this instanceof BaselineGroup));
        }
        private void checkChildAlignment(Alignment alignment,
                boolean allowsBaseline) {
            if (alignment == null) {
                throw new IllegalArgumentException("Alignment must be non-null");
            }
            if (!allowsBaseline && alignment == Alignment.BASELINE) {
                throw new IllegalArgumentException("Alignment must be one of:" +
                        "LEADING, TRAILING or CENTER");
            }
        }
    }
    private class BaselineGroup extends ParallelGroup {
        private boolean allSpringsHaveBaseline;
        private int prefAscent;
        private int prefDescent;
        private boolean baselineAnchorSet;
        private boolean baselineAnchoredToTop;
        private boolean calcedBaseline;
        BaselineGroup(boolean resizable) {
            super(Alignment.LEADING, resizable);
            prefAscent = prefDescent = -1;
            calcedBaseline = false;
        }
        BaselineGroup(boolean resizable, boolean baselineAnchoredToTop) {
            this(resizable);
            this.baselineAnchoredToTop = baselineAnchoredToTop;
            baselineAnchorSet = true;
        }
        void unset() {
            super.unset();
            prefAscent = prefDescent = -1;
            calcedBaseline = false;
        }
        void setValidSize(int axis, int origin, int size) {
            checkAxis(axis);
            if (prefAscent == -1) {
                super.setValidSize(axis, origin, size);
            } else {
                baselineLayout(origin, size);
            }
        }
        int calculateSize(int axis, int type) {
            checkAxis(axis);
            if (!calcedBaseline) {
                calculateBaselineAndResizeBehavior();
            }
            if (type == MIN_SIZE) {
                return calculateMinSize();
            }
            if (type == MAX_SIZE) {
                return calculateMaxSize();
            }
            if (allSpringsHaveBaseline) {
                return prefAscent + prefDescent;
            }
            return Math.max(prefAscent + prefDescent,
                    super.calculateSize(axis, type));
        }
        private void calculateBaselineAndResizeBehavior() {
            prefAscent = 0;
            prefDescent = 0;
            int baselineSpringCount = 0;
            BaselineResizeBehavior resizeBehavior = null;
            for (Spring spring : springs) {
                if (spring.getAlignment() == null ||
                        spring.getAlignment() == Alignment.BASELINE) {
                    int baseline = spring.getBaseline();
                    if (baseline >= 0) {
                        if (spring.isResizable(VERTICAL)) {
                            BaselineResizeBehavior brb = spring.
                                    getBaselineResizeBehavior();
                            if (resizeBehavior == null) {
                                resizeBehavior = brb;
                            } else if (brb != resizeBehavior) {
                                resizeBehavior = BaselineResizeBehavior.
                                        CONSTANT_ASCENT;
                            }
                        }
                        prefAscent = Math.max(prefAscent, baseline);
                        prefDescent = Math.max(prefDescent, spring.
                                getPreferredSize(VERTICAL) - baseline);
                        baselineSpringCount++;
                    }
                }
            }
            if (!baselineAnchorSet) {
                if (resizeBehavior == BaselineResizeBehavior.CONSTANT_DESCENT){
                    this.baselineAnchoredToTop = false;
                } else {
                    this.baselineAnchoredToTop = true;
                }
            }
            allSpringsHaveBaseline = (baselineSpringCount == springs.size());
            calcedBaseline = true;
        }
        private int calculateMaxSize() {
            int maxAscent = prefAscent;
            int maxDescent = prefDescent;
            int nonBaselineMax = 0;
            for (Spring spring : springs) {
                int baseline;
                int springMax = spring.getMaximumSize(VERTICAL);
                if ((spring.getAlignment() == null ||
                        spring.getAlignment() == Alignment.BASELINE) &&
                        (baseline = spring.getBaseline()) >= 0) {
                    int springPref = spring.getPreferredSize(VERTICAL);
                    if (springPref != springMax) {
                        switch (spring.getBaselineResizeBehavior()) {
                            case CONSTANT_ASCENT:
                                if (baselineAnchoredToTop) {
                                    maxDescent = Math.max(maxDescent,
                                            springMax - baseline);
                                }
                                break;
                            case CONSTANT_DESCENT:
                                if (!baselineAnchoredToTop) {
                                    maxAscent = Math.max(maxAscent,
                                            springMax - springPref + baseline);
                                }
                                break;
                            default: 
                                break;
                        }
                    }
                } else {
                    nonBaselineMax = Math.max(nonBaselineMax, springMax);
                }
            }
            return Math.max(nonBaselineMax, maxAscent + maxDescent);
        }
        private int calculateMinSize() {
            int minAscent = 0;
            int minDescent = 0;
            int nonBaselineMin = 0;
            if (baselineAnchoredToTop) {
                minAscent = prefAscent;
            } else {
                minDescent = prefDescent;
            }
            for (Spring spring : springs) {
                int springMin = spring.getMinimumSize(VERTICAL);
                int baseline;
                if ((spring.getAlignment() == null ||
                        spring.getAlignment() == Alignment.BASELINE) &&
                        (baseline = spring.getBaseline()) >= 0) {
                    int springPref = spring.getPreferredSize(VERTICAL);
                    BaselineResizeBehavior brb = spring.
                            getBaselineResizeBehavior();
                    switch (brb) {
                        case CONSTANT_ASCENT:
                            if (baselineAnchoredToTop) {
                                minDescent = Math.max(springMin - baseline,
                                        minDescent);
                            } else {
                                minAscent = Math.max(baseline, minAscent);
                            }
                            break;
                        case CONSTANT_DESCENT:
                            if (!baselineAnchoredToTop) {
                                minAscent = Math.max(
                                        baseline - (springPref - springMin),
                                        minAscent);
                            } else {
                                minDescent = Math.max(springPref - baseline,
                                        minDescent);
                            }
                            break;
                        default:
                            minAscent = Math.max(baseline, minAscent);
                            minDescent = Math.max(springPref - baseline,
                                    minDescent);
                            break;
                    }
                } else {
                    nonBaselineMin = Math.max(nonBaselineMin, springMin);
                }
            }
            return Math.max(nonBaselineMin, minAscent + minDescent);
        }
        private void baselineLayout(int origin, int size) {
            int ascent;
            int descent;
            if (baselineAnchoredToTop) {
                ascent = prefAscent;
                descent = size - ascent;
            } else {
                ascent = size - prefDescent;
                descent = prefDescent;
            }
            for (Spring spring : springs) {
                Alignment alignment = spring.getAlignment();
                if (alignment == null || alignment == Alignment.BASELINE) {
                    int baseline = spring.getBaseline();
                    if (baseline >= 0) {
                        int springMax = spring.getMaximumSize(VERTICAL);
                        int springPref = spring.getPreferredSize(VERTICAL);
                        int height = springPref;
                        int y;
                        switch(spring.getBaselineResizeBehavior()) {
                            case CONSTANT_ASCENT:
                                y = origin + ascent - baseline;
                                height = Math.min(descent, springMax -
                                        baseline) + baseline;
                                break;
                            case CONSTANT_DESCENT:
                                height = Math.min(ascent, springMax -
                                        springPref + baseline) +
                                        (springPref - baseline);
                                y = origin + ascent +
                                        (springPref - baseline) - height;
                                break;
                            default: 
                                y = origin + ascent - baseline;
                                break;
                        }
                        spring.setSize(VERTICAL, y, height);
                    } else {
                        setChildSize(spring, VERTICAL, origin, size);
                    }
                } else {
                    setChildSize(spring, VERTICAL, origin, size);
                }
            }
        }
        int getBaseline() {
            if (springs.size() > 1) {
                getPreferredSize(VERTICAL);
                return prefAscent;
            } else if (springs.size() == 1) {
                return springs.get(0).getBaseline();
            }
            return -1;
        }
        BaselineResizeBehavior getBaselineResizeBehavior() {
            if (springs.size() == 1) {
                return springs.get(0).getBaselineResizeBehavior();
            }
            if (baselineAnchoredToTop) {
                return BaselineResizeBehavior.CONSTANT_ASCENT;
            }
            return BaselineResizeBehavior.CONSTANT_DESCENT;
        }
        private void checkAxis(int axis) {
            if (axis == HORIZONTAL) {
                throw new IllegalStateException(
                        "Baseline must be used along vertical axis");
            }
        }
    }
    private final class ComponentSpring extends Spring {
        private Component component;
        private int origin;
        private final int min;
        private final int pref;
        private final int max;
        private int baseline = -1;
        private boolean installed;
        private ComponentSpring(Component component, int min, int pref,
                int max) {
            this.component = component;
            if (component == null) {
                throw new IllegalArgumentException(
                        "Component must be non-null");
            }
            checkSize(min, pref, max, true);
            this.min = min;
            this.max = max;
            this.pref = pref;
            getComponentInfo(component);
        }
        int calculateMinimumSize(int axis) {
            if (isLinked(axis)) {
                return getLinkSize(axis, MIN_SIZE);
            }
            return calculateNonlinkedMinimumSize(axis);
        }
        int calculatePreferredSize(int axis) {
            if (isLinked(axis)) {
                return getLinkSize(axis, PREF_SIZE);
            }
            int min = getMinimumSize(axis);
            int pref = calculateNonlinkedPreferredSize(axis);
            int max = getMaximumSize(axis);
            return Math.min(max, Math.max(min, pref));
        }
        int calculateMaximumSize(int axis) {
            if (isLinked(axis)) {
                return getLinkSize(axis, MAX_SIZE);
            }
            return Math.max(getMinimumSize(axis),
                    calculateNonlinkedMaximumSize(axis));
        }
        boolean isVisible() {
            return getComponentInfo(getComponent()).isVisible();
        }
        int calculateNonlinkedMinimumSize(int axis) {
            if (!isVisible()) {
                return 0;
            }
            if (min >= 0) {
                return min;
            }
            if (min == PREFERRED_SIZE) {
                return calculateNonlinkedPreferredSize(axis);
            }
            assert (min == DEFAULT_SIZE);
            return getSizeAlongAxis(axis, component.getMinimumSize());
        }
        int calculateNonlinkedPreferredSize(int axis) {
            if (!isVisible()) {
                return 0;
            }
            if (pref >= 0) {
                return pref;
            }
            assert (pref == DEFAULT_SIZE || pref == PREFERRED_SIZE);
            return getSizeAlongAxis(axis, component.getPreferredSize());
        }
        int calculateNonlinkedMaximumSize(int axis) {
            if (!isVisible()) {
                return 0;
            }
            if (max >= 0) {
                return max;
            }
            if (max == PREFERRED_SIZE) {
                return calculateNonlinkedPreferredSize(axis);
            }
            assert (max == DEFAULT_SIZE);
            return getSizeAlongAxis(axis, component.getMaximumSize());
        }
        private int getSizeAlongAxis(int axis, Dimension size) {
            return (axis == HORIZONTAL) ? size.width : size.height;
        }
        private int getLinkSize(int axis, int type) {
            if (!isVisible()) {
                return 0;
            }
            ComponentInfo ci = getComponentInfo(component);
            return ci.getLinkSize(axis, type);
        }
        void setSize(int axis, int origin, int size) {
            super.setSize(axis, origin, size);
            this.origin = origin;
            if (size == UNSET) {
                baseline = -1;
            }
        }
        int getOrigin() {
            return origin;
        }
        void setComponent(Component component) {
            this.component = component;
        }
        Component getComponent() {
            return component;
        }
        int getBaseline() {
            if (baseline == -1) {
                Spring horizontalSpring = getComponentInfo(component).
                        horizontalSpring;
                int width = horizontalSpring.getPreferredSize(HORIZONTAL);
                int height = getPreferredSize(VERTICAL);
                if (width > 0 && height > 0) {
                    baseline = component.getBaseline(width, height);
                }
            }
            return baseline;
        }
        BaselineResizeBehavior getBaselineResizeBehavior() {
            return getComponent().getBaselineResizeBehavior();
        }
        private boolean isLinked(int axis) {
            return getComponentInfo(component).isLinked(axis);
        }
        void installIfNecessary(int axis) {
            if (!installed) {
                installed = true;
                if (axis == HORIZONTAL) {
                    getComponentInfo(component).horizontalSpring = this;
                } else {
                    getComponentInfo(component).verticalSpring = this;
                }
            }
        }
        @Override
        boolean willHaveZeroSize(boolean treatAutopaddingAsZeroSized) {
            return !isVisible();
        }
    }
    private class PreferredGapSpring extends Spring {
        private final JComponent source;
        private final JComponent target;
        private final ComponentPlacement type;
        private final int pref;
        private final int max;
        PreferredGapSpring(JComponent source, JComponent target,
                ComponentPlacement type, int pref, int max) {
            this.source = source;
            this.target = target;
            this.type = type;
            this.pref = pref;
            this.max = max;
        }
        int calculateMinimumSize(int axis) {
            return getPadding(axis);
        }
        int calculatePreferredSize(int axis) {
            if (pref == DEFAULT_SIZE || pref == PREFERRED_SIZE) {
                return getMinimumSize(axis);
            }
            int min = getMinimumSize(axis);
            int max = getMaximumSize(axis);
            return Math.min(max, Math.max(min, pref));
        }
        int calculateMaximumSize(int axis) {
            if (max == PREFERRED_SIZE || max == DEFAULT_SIZE) {
                return getPadding(axis);
            }
            return Math.max(getMinimumSize(axis), max);
        }
        private int getPadding(int axis) {
            int position;
            if (axis == HORIZONTAL) {
                position = SwingConstants.EAST;
            } else {
                position = SwingConstants.SOUTH;
            }
            return getLayoutStyle0().getPreferredGap(source,
                    target, type, position, host);
        }
        @Override
        boolean willHaveZeroSize(boolean treatAutopaddingAsZeroSized) {
            return false;
        }
    }
    private class GapSpring extends Spring {
        private final int min;
        private final int pref;
        private final int max;
        GapSpring(int min, int pref, int max) {
            checkSize(min, pref, max, false);
            this.min = min;
            this.pref = pref;
            this.max = max;
        }
        int calculateMinimumSize(int axis) {
            if (min == PREFERRED_SIZE) {
                return getPreferredSize(axis);
            }
            return min;
        }
        int calculatePreferredSize(int axis) {
            return pref;
        }
        int calculateMaximumSize(int axis) {
            if (max == PREFERRED_SIZE) {
                return getPreferredSize(axis);
            }
            return max;
        }
        @Override
        boolean willHaveZeroSize(boolean treatAutopaddingAsZeroSized) {
            return false;
        }
    }
    private class AutoPreferredGapSpring extends Spring {
        List<ComponentSpring> sources;
        ComponentSpring source;
        private List<AutoPreferredGapMatch> matches;
        int size;
        int lastSize;
        private final int pref;
        private final int max;
        private ComponentPlacement type;
        private boolean userCreated;
        private AutoPreferredGapSpring() {
            this.pref = PREFERRED_SIZE;
            this.max = PREFERRED_SIZE;
            this.type = ComponentPlacement.RELATED;
        }
        AutoPreferredGapSpring(int pref, int max) {
            this.pref = pref;
            this.max = max;
        }
        AutoPreferredGapSpring(ComponentPlacement type, int pref, int max) {
            this.type = type;
            this.pref = pref;
            this.max = max;
            this.userCreated = true;
        }
        public void setSource(ComponentSpring source) {
            this.source = source;
        }
        public void setSources(List<ComponentSpring> sources) {
            this.sources = new ArrayList<ComponentSpring>(sources);
        }
        public void setUserCreated(boolean userCreated) {
            this.userCreated = userCreated;
        }
        public boolean getUserCreated() {
            return userCreated;
        }
        void unset() {
            lastSize = getSize();
            super.unset();
            size = 0;
        }
        public void reset() {
            size = 0;
            sources = null;
            source = null;
            matches = null;
        }
        public void calculatePadding(int axis) {
            size = UNSET;
            int maxPadding = UNSET;
            if (matches != null) {
                LayoutStyle p = getLayoutStyle0();
                int position;
                if (axis == HORIZONTAL) {
                    if (isLeftToRight()) {
                        position = SwingConstants.EAST;
                    } else {
                        position = SwingConstants.WEST;
                    }
                } else {
                    position = SwingConstants.SOUTH;
                }
                for (int i = matches.size() - 1; i >= 0; i--) {
                    AutoPreferredGapMatch match = matches.get(i);
                    maxPadding = Math.max(maxPadding,
                            calculatePadding(p, position, match.source,
                            match.target));
                }
            }
            if (size == UNSET) {
                size = 0;
            }
            if (maxPadding == UNSET) {
                maxPadding = 0;
            }
            if (lastSize != UNSET) {
                size += Math.min(maxPadding, lastSize);
            }
        }
        private int calculatePadding(LayoutStyle p, int position,
                ComponentSpring source,
                ComponentSpring target) {
            int delta = target.getOrigin() - (source.getOrigin() +
                    source.getSize());
            if (delta >= 0) {
                int padding;
                if ((source.getComponent() instanceof JComponent) &&
                        (target.getComponent() instanceof JComponent)) {
                    padding = p.getPreferredGap(
                            (JComponent)source.getComponent(),
                            (JComponent)target.getComponent(), type, position,
                            host);
                } else {
                    padding = 10;
                }
                if (padding > delta) {
                    size = Math.max(size, padding - delta);
                }
                return padding;
            }
            return 0;
        }
        public void addTarget(ComponentSpring spring, int axis) {
            int oAxis = (axis == HORIZONTAL) ? VERTICAL : HORIZONTAL;
            if (source != null) {
                if (areParallelSiblings(source.getComponent(),
                        spring.getComponent(), oAxis)) {
                    addValidTarget(source, spring);
                }
            } else {
                Component component = spring.getComponent();
                for (int counter = sources.size() - 1; counter >= 0;
                         counter--){
                    ComponentSpring source = sources.get(counter);
                    if (areParallelSiblings(source.getComponent(),
                            component, oAxis)) {
                        addValidTarget(source, spring);
                    }
                }
            }
        }
        private void addValidTarget(ComponentSpring source,
                ComponentSpring target) {
            if (matches == null) {
                matches = new ArrayList<AutoPreferredGapMatch>(1);
            }
            matches.add(new AutoPreferredGapMatch(source, target));
        }
        int calculateMinimumSize(int axis) {
            return size;
        }
        int calculatePreferredSize(int axis) {
            if (pref == PREFERRED_SIZE || pref == DEFAULT_SIZE) {
                return size;
            }
            return Math.max(size, pref);
        }
        int calculateMaximumSize(int axis) {
            if (max >= 0) {
                return Math.max(getPreferredSize(axis), max);
            }
            return size;
        }
        String getMatchDescription() {
            return (matches == null) ? "" : matches.toString();
        }
        public String toString() {
            return super.toString() + getMatchDescription();
        }
        @Override
        boolean willHaveZeroSize(boolean treatAutopaddingAsZeroSized) {
            return treatAutopaddingAsZeroSized;
        }
    }
    private final static class AutoPreferredGapMatch {
        public final ComponentSpring source;
        public final ComponentSpring target;
        AutoPreferredGapMatch(ComponentSpring source, ComponentSpring target) {
            this.source = source;
            this.target = target;
        }
        private String toString(ComponentSpring spring) {
            return spring.getComponent().getName();
        }
        public String toString() {
            return "[" + toString(source) + "-" + toString(target) + "]";
        }
    }
    private class ContainerAutoPreferredGapSpring extends
            AutoPreferredGapSpring {
        private List<ComponentSpring> targets;
        ContainerAutoPreferredGapSpring() {
            super();
            setUserCreated(true);
        }
        ContainerAutoPreferredGapSpring(int pref, int max) {
            super(pref, max);
            setUserCreated(true);
        }
        public void addTarget(ComponentSpring spring, int axis) {
            if (targets == null) {
                targets = new ArrayList<ComponentSpring>(1);
            }
            targets.add(spring);
        }
        public void calculatePadding(int axis) {
            LayoutStyle p = getLayoutStyle0();
            int maxPadding = 0;
            int position;
            size = 0;
            if (targets != null) {
                if (axis == HORIZONTAL) {
                    if (isLeftToRight()) {
                        position = SwingConstants.WEST;
                    } else {
                        position = SwingConstants.EAST;
                    }
                } else {
                    position = SwingConstants.SOUTH;
                }
                for (int i = targets.size() - 1; i >= 0; i--) {
                    ComponentSpring targetSpring = targets.get(i);
                    int padding = 10;
                    if (targetSpring.getComponent() instanceof JComponent) {
                        padding = p.getContainerGap(
                                (JComponent)targetSpring.getComponent(),
                                position, host);
                        maxPadding = Math.max(padding, maxPadding);
                        padding -= targetSpring.getOrigin();
                    } else {
                        maxPadding = Math.max(padding, maxPadding);
                    }
                    size = Math.max(size, padding);
                }
            } else {
                if (axis == HORIZONTAL) {
                    if (isLeftToRight()) {
                        position = SwingConstants.EAST;
                    } else {
                        position = SwingConstants.WEST;
                    }
                } else {
                    position = SwingConstants.SOUTH;
                }
                if (sources != null) {
                    for (int i = sources.size() - 1; i >= 0; i--) {
                        ComponentSpring sourceSpring = sources.get(i);
                        maxPadding = Math.max(maxPadding,
                                updateSize(p, sourceSpring, position));
                    }
                } else if (source != null) {
                    maxPadding = updateSize(p, source, position);
                }
            }
            if (lastSize != UNSET) {
                size += Math.min(maxPadding, lastSize);
            }
        }
        private int updateSize(LayoutStyle p, ComponentSpring sourceSpring,
                int position) {
            int padding = 10;
            if (sourceSpring.getComponent() instanceof JComponent) {
                padding = p.getContainerGap(
                        (JComponent)sourceSpring.getComponent(), position,
                        host);
            }
            int delta = Math.max(0, getParent().getSize() -
                    sourceSpring.getSize() - sourceSpring.getOrigin());
            size = Math.max(size, padding - delta);
            return padding;
        }
        String getMatchDescription() {
            if (targets != null) {
                return "leading: " + targets.toString();
            }
            if (sources != null) {
                return "trailing: " + sources.toString();
            }
            return "--";
        }
    }
    private static class LinkInfo {
        private final int axis;
        private final List<ComponentInfo> linked;
        private int size;
        LinkInfo(int axis) {
            linked = new ArrayList<ComponentInfo>();
            size = UNSET;
            this.axis = axis;
        }
        public void add(ComponentInfo child) {
            LinkInfo childMaster = child.getLinkInfo(axis, false);
            if (childMaster == null) {
                linked.add(child);
                child.setLinkInfo(axis, this);
            } else if (childMaster != this) {
                linked.addAll(childMaster.linked);
                for (ComponentInfo childInfo : childMaster.linked) {
                    childInfo.setLinkInfo(axis, this);
                }
            }
            clearCachedSize();
        }
        public void remove(ComponentInfo info) {
            linked.remove(info);
            info.setLinkInfo(axis, null);
            if (linked.size() == 1) {
                linked.get(0).setLinkInfo(axis, null);
            }
            clearCachedSize();
        }
        public void clearCachedSize() {
            size = UNSET;
        }
        public int getSize(int axis) {
            if (size == UNSET) {
                size = calculateLinkedSize(axis);
            }
            return size;
        }
        private int calculateLinkedSize(int axis) {
            int size = 0;
            for (ComponentInfo info : linked) {
                ComponentSpring spring;
                if (axis == HORIZONTAL) {
                    spring = info.horizontalSpring;
                } else {
                    assert (axis == VERTICAL);
                    spring = info.verticalSpring;
                }
                size = Math.max(size,
                        spring.calculateNonlinkedPreferredSize(axis));
            }
            return size;
        }
    }
    private class ComponentInfo {
        private Component component;
        ComponentSpring horizontalSpring;
        ComponentSpring verticalSpring;
        private LinkInfo horizontalMaster;
        private LinkInfo verticalMaster;
        private boolean visible;
        private Boolean honorsVisibility;
        ComponentInfo(Component component) {
            this.component = component;
            updateVisibility();
        }
        public void dispose() {
            removeSpring(horizontalSpring);
            horizontalSpring = null;
            removeSpring(verticalSpring);
            verticalSpring = null;
            if (horizontalMaster != null) {
                horizontalMaster.remove(this);
            }
            if (verticalMaster != null) {
                verticalMaster.remove(this);
            }
        }
        void setHonorsVisibility(Boolean honorsVisibility) {
            this.honorsVisibility = honorsVisibility;
        }
        private void removeSpring(Spring spring) {
            if (spring != null) {
                ((Group)spring.getParent()).springs.remove(spring);
            }
        }
        public boolean isVisible() {
            return visible;
        }
        boolean updateVisibility() {
            boolean honorsVisibility;
            if (this.honorsVisibility == null) {
                honorsVisibility = GroupLayout.this.getHonorsVisibility();
            } else {
                honorsVisibility = this.honorsVisibility;
            }
            boolean newVisible = (honorsVisibility) ?
                component.isVisible() : true;
            if (visible != newVisible) {
                visible = newVisible;
                return true;
            }
            return false;
        }
        public void setBounds(Insets insets, int parentWidth, boolean ltr) {
            int x = horizontalSpring.getOrigin();
            int w = horizontalSpring.getSize();
            int y = verticalSpring.getOrigin();
            int h = verticalSpring.getSize();
            if (!ltr) {
                x = parentWidth - x - w;
            }
            component.setBounds(x + insets.left, y + insets.top, w, h);
        }
        public void setComponent(Component component) {
            this.component = component;
            if (horizontalSpring != null) {
                horizontalSpring.setComponent(component);
            }
            if (verticalSpring != null) {
                verticalSpring.setComponent(component);
            }
        }
        public Component getComponent() {
            return component;
        }
        public boolean isLinked(int axis) {
            if (axis == HORIZONTAL) {
                return horizontalMaster != null;
            }
            assert (axis == VERTICAL);
            return (verticalMaster != null);
        }
        private void setLinkInfo(int axis, LinkInfo linkInfo) {
            if (axis == HORIZONTAL) {
                horizontalMaster = linkInfo;
            } else {
                assert (axis == VERTICAL);
                verticalMaster = linkInfo;
            }
        }
        public LinkInfo getLinkInfo(int axis) {
            return getLinkInfo(axis, true);
        }
        private LinkInfo getLinkInfo(int axis, boolean create) {
            if (axis == HORIZONTAL) {
                if (horizontalMaster == null && create) {
                    new LinkInfo(HORIZONTAL).add(this);
                }
                return horizontalMaster;
            } else {
                assert (axis == VERTICAL);
                if (verticalMaster == null && create) {
                    new LinkInfo(VERTICAL).add(this);
                }
                return verticalMaster;
            }
        }
        public void clearCachedSize() {
            if (horizontalMaster != null) {
                horizontalMaster.clearCachedSize();
            }
            if (verticalMaster != null) {
                verticalMaster.clearCachedSize();
            }
        }
        int getLinkSize(int axis, int type) {
            if (axis == HORIZONTAL) {
                return horizontalMaster.getSize(axis);
            } else {
                assert (axis == VERTICAL);
                return verticalMaster.getSize(axis);
            }
        }
    }
}
