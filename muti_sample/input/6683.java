public abstract class AbstractPolicyTest {
    protected AbstractPolicyTest() {
    }
    Map<String, Component> registered_comps = new HashMap<String, Component>();
    protected abstract Frame createFrame();
    protected abstract void customizeHierarchy();
    protected abstract Map<String, String> getForwardOrder();
    protected abstract Map<String, String> getBackwardOrder();
    protected abstract String[] getContainersToTest();
    protected abstract String getDefaultComp(String focusCycleRoot_id);
    protected abstract String getFirstComp(String focusCycleRoot_id);
    protected abstract String getLastComp(String focusCycleRoot_id);
    protected final Component registerComponent(final String id, final Component comp) {
        if (registered_comps.containsKey(id)) {
            throw new RuntimeException("The component with id (" + id + "), already registered.");
        }
        comp.setName(id);
        registered_comps.put(id, comp);
        return comp;
    }
    public void testIt() {
        Frame frame = createFrame();
        customizeHierarchy();
        try {
            frame.pack();
            frame.setVisible(true);
            testPolicy(getForwardOrder(), getBackwardOrder());
        } finally {
            frame.dispose();
        }
    }
    void testPolicy(final Map<String, String> forward_order, final Map<String, String> backward_order)
    {
        if (getContainersToTest() != null)
            for (String cont_id : getContainersToTest()) {
                final Container cont = (Container) getComponent(cont_id);
                FocusTraversalPolicy policy = cont.getFocusTraversalPolicy();
                assertEquals(cont_id, "Test default component", getComponent(getDefaultComp(cont_id)), policy.getDefaultComponent(cont));
                assertEquals(cont_id, "Test first component", getComponent(getFirstComp(cont_id)), policy.getFirstComponent(cont));
                assertEquals(cont_id, "Test last component", getComponent(getLastComp(cont_id)), policy.getLastComponent(cont));
            }
        if (forward_order != null)
            for (String key : forward_order.keySet()) {
                final Component current = getComponent(key);
                final Component next = getComponent(forward_order.get(key));
                Container focusCycleRoot = current.getParent() == null ? (Container)current : current.getFocusCycleRootAncestor();
                FocusTraversalPolicy policy = focusCycleRoot.getFocusTraversalPolicy();
                assertEquals(null, "Test getComponentAfter() for " + key, next, policy.getComponentAfter(focusCycleRoot, current));
            }
        if (backward_order != null)
            for (String key : backward_order.keySet()) {
                final Component current = getComponent(key);
                final Component previous = getComponent(backward_order.get(key));
                Container focusCycleRoot = current.getParent() == null ? (Container)current : current.getFocusCycleRootAncestor();
                FocusTraversalPolicy policy = focusCycleRoot.getFocusTraversalPolicy();
                assertEquals(null, "Test getComponentBefore() for " + key, previous, policy.getComponentBefore(focusCycleRoot, current));
            }
    }
    protected final Component getComponent(final String id) {
        if (!registered_comps.containsKey(id)) {
            throw new RuntimeException("There is no registered component with given id(" + id +")");
        }
        return registered_comps.get(id);
    }
    void assertEquals(final String message, final Object expected, final Object actual) {
        assertEquals(null, message, expected, actual);
    }
    void assertEquals(final String cont_id, final String message, final Object expected, final Object actual) {
        if (actual == null && expected == null
            || actual != null && actual.equals(expected))
        {
            return;
        }
        throw new RuntimeException((cont_id != null ? (cont_id + ": ") : "") + message +
                                   "(actual = " + actual + ", expected = " + expected + ")");
    }
}
