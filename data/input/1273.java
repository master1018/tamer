public class LayoutFTPTest {
    final int TESTS_NUMBER = 11;
    public static void main(String[] args) {
        LayoutFTPTest app = new LayoutFTPTest();
        app.start();
    }
    public void start() {
        try {
            Class clazz = null;
            AbstractPolicyTest test = null;
            for (int i = 1; i <= TESTS_NUMBER; i++) {
                clazz = Class.forName("PolicyTest" + i);
                if (clazz != null) {
                    test = (AbstractPolicyTest)clazz.newInstance();
                    System.out.print("Test " + i + " is in progress...");
                    test.testIt();
                    System.out.println(" passed.");
                }
            }
        } catch (RuntimeException rte) {
            throw rte;
        } catch (Exception e) {
            throw new RuntimeException("Error: unexpected exception cought!", e);
        }
    }
}
class PolicyTest1 extends AbstractPolicyTest {
    protected Frame createFrame() {
        JFrame jframe = (JFrame) registerComponent("jframe", new JFrame("Test Frame"));
        jframe.setLayout(new GridLayout(3, 1));
        for (int i = 0; i < 3; i++) {
            Container cont = (Container) registerComponent("jpanel" + i, new JPanel());
            for (int j = 0; j < 3; j++) {
                cont.add(registerComponent("btn " + (j + i*100), new JButton("jbutton")));
            }
            jframe.add(cont);
        }
        return jframe;
    }
    protected void customizeHierarchy() {
        ((Container)getComponent("jframe")).setFocusTraversalPolicy(new LayoutFocusTraversalPolicy());
    }
    protected Map<String, String> getForwardOrder() {
        Map<String, String> order = new HashMap<String, String>();
        order.put("btn 0", "btn 1");
        order.put("btn 1", "btn 2");
        order.put("btn 2", "btn 100");
        order.put("btn 100", "btn 101");
        order.put("btn 101", "btn 102");
        order.put("btn 102", "btn 200");
        order.put("btn 200", "btn 201");
        order.put("btn 201", "btn 202");
        order.put("btn 202", "btn 0");
        order.put("jpanel0", "btn 0");
        order.put("jpanel1", "btn 100");
        order.put("jpanel2", "btn 200");
        order.put("jframe", "btn 0");
        return order;
    }
    protected Map<String, String> getBackwardOrder() {
        Map<String, String> order = new HashMap<String, String>();
        order.put("btn 0", "btn 202");
        order.put("btn 1", "btn 0");
        order.put("btn 2", "btn 1");
        order.put("btn 100", "btn 2");
        order.put("btn 101", "btn 100");
        order.put("btn 102", "btn 101");
        order.put("btn 200", "btn 102");
        order.put("btn 201", "btn 200");
        order.put("btn 202", "btn 201");
        order.put("jpanel0", "btn 202");
        order.put("jpanel1", "btn 2");
        order.put("jpanel2", "btn 102");
        order.put("jframe", "btn 202");
        return order;
    }
    protected String[] getContainersToTest() {
        return new String[] {"jframe"};
    }
    protected String getDefaultComp(String focusCycleRoot_id) {
        return "btn 0";
    }
    protected String getFirstComp(String focusCycleRoot_id) {
        return "btn 0";
    }
    protected String getLastComp(String focusCycleRoot_id) {
        return "btn 202";
    }
}
class PolicyTest2 extends AbstractPolicyTest {
    protected Frame createFrame() {
        JFrame jframe = (JFrame) registerComponent("jframe", new JFrame("Test Frame"));
        jframe.setLayout(new FlowLayout());
        jframe.add(registerComponent("btn 1", new JButton("jbutton")));
        Container cont = (Container)registerComponent("jpanel", new JPanel());
        cont.add(registerComponent("btn 2", new JButton("jbutton")));
        cont.add(registerComponent("btn 3", new JButton("jbutton")));
        jframe.add(cont);
        jframe.add(registerComponent("btn 4", new JButton("jbutton")));
        return jframe;
    }
    protected void customizeHierarchy() {
        ((Container)getComponent("jframe")).setFocusTraversalPolicy(new LayoutFocusTraversalPolicy());
        ((Container)getComponent("jpanel")).setFocusTraversalPolicyProvider(true);
    }
    protected Map<String, String> getForwardOrder() {
        Map<String, String> order = new HashMap<String, String>();
        order.put("jframe", "btn 1");
        order.put("btn 1", "btn 2");
        order.put("btn 2", "btn 3");
        order.put("btn 3", "btn 4");
        order.put("btn 4", "btn 1");
        order.put("jpanel", "btn 2");
        return order;
    }
    protected Map<String, String> getBackwardOrder() {
        Map<String, String> order = new HashMap<String, String>();
        order.put("btn 4", "btn 3");
        order.put("btn 3", "btn 2");
        order.put("btn 2", "btn 1");
        order.put("btn 1", "btn 4");
        return order;
    }
    protected String[] getContainersToTest() {
        return new String[] {"jframe", "jpanel"};
    }
    protected String getDefaultComp(String focusCycleRoot_id) {
        if ("jframe".equals(focusCycleRoot_id)) {
            return "btn 1";
        } else if ("jpanel".equals(focusCycleRoot_id)) {
            return "btn 2";
        }
        return null;
    }
    protected String getFirstComp(String focusCycleRoot_id) {
        return getDefaultComp(focusCycleRoot_id);
    }
    protected String getLastComp(String focusCycleRoot_id) {
        if ("jframe".equals(focusCycleRoot_id)) {
            return "btn 4";
        } else if ("jpanel".equals(focusCycleRoot_id)) {
            return "btn 3";
        }
        return null;
    }
}
class PolicyTest3 extends AbstractPolicyTest {
    protected Frame createFrame() {
        JFrame jframe = (JFrame) registerComponent("jframe", new JFrame("Test Frame"));
        jframe.setLayout(new FlowLayout());
        jframe.add(registerComponent("btn 1", new JButton("jbutton")));
        Container cont = (Container)registerComponent("jpanel", new JPanel());
        cont.add(registerComponent("btn 2", new JButton("jbutton")));
        cont.add(registerComponent("btn 3", new JButton("jbutton")));
        jframe.add(cont);
        jframe.add(registerComponent("btn 4", new JButton("jbutton")));
        return jframe;
    }
    protected void customizeHierarchy() {
        ((Container)getComponent("jframe")).setFocusTraversalPolicy(new LayoutFocusTraversalPolicy());
        ((Container)getComponent("jpanel")).setFocusCycleRoot(true);
    }
    protected Map<String, String> getForwardOrder() {
        Map<String, String> order = new HashMap<String, String>();
        order.put("jframe", "btn 1");
        order.put("btn 1", "btn 2");
        order.put("btn 2", "btn 3");
        order.put("btn 3", "btn 2");
        order.put("btn 4", "btn 1");
        order.put("jpanel", "btn 2");
        return order;
    }
    protected Map<String, String> getBackwardOrder() {
        Map<String, String> order = new HashMap<String, String>();
        order.put("btn 4", "btn 2");
        order.put("btn 3", "btn 2");
        order.put("btn 2", "btn 3");
        order.put("btn 1", "btn 4");
        return order;
    }
    protected String[] getContainersToTest() {
        return new String[] {"jframe", "jpanel"};
    }
    protected String getDefaultComp(String focusCycleRoot_id) {
        if ("jframe".equals(focusCycleRoot_id)) {
            return "btn 1";
        } else if ("jpanel".equals(focusCycleRoot_id)) {
            return "btn 2";
        }
        return null;
    }
    protected String getFirstComp(String focusCycleRoot_id) {
        return getDefaultComp(focusCycleRoot_id);
    }
    protected String getLastComp(String focusCycleRoot_id) {
        if ("jframe".equals(focusCycleRoot_id)) {
            return "btn 4";
        } else if ("jpanel".equals(focusCycleRoot_id)) {
            return "btn 3";
        }
        return null;
    }
}
class PolicyTest4 extends AbstractPolicyTest {
    protected Frame createFrame() {
        JFrame jframe = (JFrame) registerComponent("jframe", new JFrame("Test Frame"));
        jframe.setLayout(new FlowLayout());
        Container cont = (Container)registerComponent("jpanel", new JPanel());
        cont.add(registerComponent("btn 1", new JButton("jbutton")));
        cont.add(registerComponent("btn 2", new JButton("jbutton")));
        jframe.add(cont);
        jframe.add(registerComponent("btn 3", new JButton("jbutton")));
        jframe.add(registerComponent("btn 4", new JButton("jbutton")));
        return jframe;
    }
    protected void customizeHierarchy() {
        ((Container)getComponent("jframe")).setFocusTraversalPolicy(new LayoutFocusTraversalPolicy());
        ((Container)getComponent("jpanel")).setFocusTraversalPolicyProvider(true);
        ((JButton)getComponent("btn 3")).setFocusable(false);
    }
    protected Map<String, String> getBackwardOrder() {
        Map<String, String> order = new HashMap<String, String>();
        order.put("btn 4", "btn 2");
        order.put("btn 2", "btn 1");
        order.put("btn 1", "btn 4");
        return order;
    }
    protected Map<String, String> getForwardOrder() {
        return null;
    }
    protected String[] getContainersToTest() {
        return null;
    }
    protected String getDefaultComp(String focusCycleRoot_id) {
        return null;
    }
    protected String getFirstComp(String focusCycleRoot_id) {
        return null;
    }
    protected String getLastComp(String focusCycleRoot_id) {
        return null;
    }
}
class PolicyTest5 extends AbstractPolicyTest {
    protected Frame createFrame() {
        JFrame jframe = (JFrame) registerComponent("jframe", new JFrame("Test Frame"));
        jframe.setLayout(new FlowLayout());
        Container cont = (Container)registerComponent("jpanel", new JPanel());
        cont.add(registerComponent("btn 1", new JButton("jbutton")));
        cont.add(registerComponent("btn 2", new JButton("jbutton")));
        jframe.add(cont);
        jframe.add(registerComponent("btn 3", new JButton("jbutton")));
        jframe.add(registerComponent("btn 4", new JButton("jbutton")));
        return jframe;
    }
    protected void customizeHierarchy() {
        ((Container)getComponent("jframe")).setFocusTraversalPolicy(new LayoutFocusTraversalPolicy());
        ((Container)getComponent("jpanel")).setFocusCycleRoot(true);
        ((JButton)getComponent("btn 3")).setFocusable(false);
    }
    protected Map<String, String> getBackwardOrder() {
        Map<String, String> order = new HashMap<String, String>();
        order.put("btn 4", "btn 1");
        order.put("btn 2", "btn 1");
        order.put("btn 1", "btn 2");
        return order;
    }
    protected Map<String, String> getForwardOrder() {
        return null;
    }
    protected String[] getContainersToTest() {
        return null;
    }
    protected String getDefaultComp(String focusCycleRoot_id) {
        return null;
    }
    protected String getFirstComp(String focusCycleRoot_id) {
        return null;
    }
    protected String getLastComp(String focusCycleRoot_id) {
        return null;
    }
}
class PolicyTest6 extends AbstractPolicyTest {
    protected Frame createFrame() {
        JFrame jframe = (JFrame) registerComponent("jframe", new JFrame("Test Frame"));
        jframe.setLayout(new FlowLayout());
        jframe.add(registerComponent("btn 1", new JButton("jbutton")));
        Container cont = (Container)registerComponent("jpanel", new JPanel());
        cont.add(registerComponent("btn 2", new JButton("jbutton")));
        cont.add(registerComponent("btn 3", new JButton("jbutton")));
        jframe.add(cont);
        jframe.add(registerComponent("btn 4", new JButton("jbutton")));
        return jframe;
    }
    protected void customizeHierarchy() {
        ((Container)getComponent("jframe")).setFocusTraversalPolicy(new LayoutFocusTraversalPolicy());
        ((Container)getComponent("jpanel")).setFocusTraversalPolicy(new LayoutFocusTraversalPolicy() {
                public Component getDefaultComponent(Container aContainer) {
                    return getComponent("btn 2");
                }
            });
        ((Container)getComponent("jpanel")).setFocusTraversalPolicyProvider(true);
        ((Container)getComponent("jpanel")).setFocusable(true);
    }
    protected Map<String, String> getForwardOrder() {
        Map<String, String> order = new HashMap<String, String>();
        order.put("jframe", "btn 1");
        order.put("btn 1", "jpanel");
        order.put("btn 2", "btn 3");
        order.put("btn 3", "btn 4");
        order.put("btn 4", "btn 1");
        order.put("jpanel", "btn 2");
        return order;
    }
    protected Map<String, String> getBackwardOrder() {
        Map<String, String> order = new HashMap<String, String>();
        order.put("btn 4", "btn 3");
        order.put("btn 3", "btn 2");
        order.put("btn 2", "jpanel");
        order.put("btn 1", "btn 4");
        order.put("jpanel", "btn 1");
        return order;
    }
    protected String[] getContainersToTest() {
        return new String[] {"jpanel"};
    }
    protected String getDefaultComp(String focusCycleRoot_id) {
        return "btn 2";
    }
    protected String getFirstComp(String focusCycleRoot_id) {
        return "jpanel";
    }
    protected String getLastComp(String focusCycleRoot_id) {
        return "btn 3";
    }
}
class PolicyTest7 extends AbstractPolicyTest {
    protected Frame createFrame() {
        JFrame jframe = (JFrame) registerComponent("jframe", new JFrame("Test Frame"));
        jframe.setLayout(new FlowLayout());
        jframe.add(registerComponent("btn 1", new JButton("jbutton")));
        Container cont = (Container)registerComponent("jpanel", new JPanel());
        cont.add(registerComponent("btn 2", new JButton("jbutton")));
        cont.add(registerComponent("btn 3", new JButton("jbutton")));
        jframe.add(cont);
        jframe.add(registerComponent("btn 4", new JButton("jbutton")));
        return jframe;
    }
    protected void customizeHierarchy() {
        ((Container)getComponent("jframe")).setFocusTraversalPolicy(new LayoutFocusTraversalPolicy());
        ((Container)getComponent("jpanel")).setFocusTraversalPolicy(new LayoutFocusTraversalPolicy() {
                public Component getDefaultComponent(Container aContainer) {
                    return getComponent("btn 2");
                }
            });
        ((Container)getComponent("jpanel")).setFocusCycleRoot(true);
        ((Container)getComponent("jpanel")).setFocusable(true);
    }
    protected Map<String, String> getForwardOrder() {
        Map<String, String> order = new HashMap<String, String>();
        order.put("jframe", "btn 1");
        order.put("btn 1", "jpanel");
        order.put("btn 2", "btn 3");
        order.put("btn 3", "jpanel");
        order.put("btn 4", "btn 1");
        order.put("jpanel", "btn 2");
        return order;
    }
    protected Map<String, String> getBackwardOrder() {
        Map<String, String> order = new HashMap<String, String>();
        order.put("btn 4", "btn 2");
        order.put("btn 3", "btn 2");
        order.put("btn 2", "jpanel");
        order.put("btn 1", "btn 4");
        order.put("jpanel", "btn 1");
        return order;
    }
    protected String[] getContainersToTest() {
        return new String[] {"jpanel"};
    }
    protected String getDefaultComp(String focusCycleRoot_id) {
        return "btn 2";
    }
    protected String getFirstComp(String focusCycleRoot_id) {
        return "jpanel";
    }
    protected String getLastComp(String focusCycleRoot_id) {
        return "btn 3";
    }
}
class PolicyTest8 extends AbstractPolicyTest {
    protected Frame createFrame() {
        JFrame jframe = (JFrame) registerComponent("frame", new JFrame("Test Frame"));
        jframe.setLayout(new FlowLayout());
        jframe.add(registerComponent("btn-1", new JButton("jbutton")));
        jframe.add(registerComponent("btn-2", new JButton("jbutton")));
        Container cont1 = (Container)registerComponent("panel-1", new JPanel());
        cont1.add(registerComponent("btn-3", new JButton("jbutton")));
        cont1.add(registerComponent("btn-4", new JButton("jbutton")));
        cont1.add(registerComponent("btn-5", new JButton("jbutton")));
        Container cont2 = (Container)registerComponent("panel-2", new JPanel());
        cont2.add(registerComponent("btn-6", new JButton("jbutton")));
        cont2.add(registerComponent("btn-7", new JButton("jbutton")));
        cont2.add(registerComponent("btn-8", new JButton("jbutton")));
        jframe.add(cont1);
        jframe.add(cont2);
        return jframe;
    }
    protected void customizeHierarchy() {
        ((Container)getComponent("panel-1")).setFocusTraversalPolicyProvider(true);
        ((Container)getComponent("panel-1")).setFocusTraversalPolicy(new LayoutFocusTraversalPolicy() {
                public Component getDefaultComponent(Container aContainer) {
                    return getComponent("btn-4");
                }
            });
        ((Container)getComponent("panel-2")).setFocusCycleRoot(true);
        ((Container)getComponent("panel-2")).setFocusTraversalPolicy(new LayoutFocusTraversalPolicy() {
                public Component getDefaultComponent(Container aContainer) {
                    return getComponent("btn-7");
                }
            });
    }
    protected Map<String, String> getForwardOrder() {
        Map<String, String> order = new HashMap<String, String>();
        order.put("frame", "btn-1");
        order.put("btn-1", "btn-2");
        order.put("btn-2", "btn-4");
        order.put("btn-3", "btn-4");
        order.put("btn-4", "btn-5");
        order.put("btn-5", "btn-7");
        order.put("btn-6", "btn-7");
        order.put("btn-7", "btn-8");
        order.put("btn-8", "btn-6");
        order.put("panel-1", "btn-4");
        order.put("panel-2", "btn-7");
        return order;
    }
    protected Map<String, String> getBackwardOrder() {
        Map<String, String> order = new HashMap<String, String>();
        order.put("btn-1", "btn-5");
        order.put("btn-2", "btn-1");
        order.put("btn-3", "btn-2");
        order.put("btn-4", "btn-3");
        order.put("btn-5", "btn-4");
        order.put("btn-6", "btn-8");
        order.put("btn-7", "btn-6");
        order.put("btn-8", "btn-7");
        return order;
    }
    protected String[] getContainersToTest() {
        return new String[] {"frame", "panel-1", "panel-2"};
    }
    protected String getDefaultComp(String focusCycleRoot_id) {
        if ("frame".equals(focusCycleRoot_id)) {
            return "btn-1";
        } else if ("panel-1".equals(focusCycleRoot_id)) {
            return "btn-4";
        } else if ("panel-2".equals(focusCycleRoot_id)) {
            return "btn-7";
        }
        return null;
    }
    protected String getFirstComp(String focusCycleRoot_id) {
        if ("frame".equals(focusCycleRoot_id)) {
            return "btn-1";
        } else if ("panel-1".equals(focusCycleRoot_id)) {
            return "btn-3";
        } else if ("panel-2".equals(focusCycleRoot_id)) {
            return "btn-6";
        }
        return null;
    }
    protected String getLastComp(String focusCycleRoot_id) {
        if ("frame".equals(focusCycleRoot_id)) {
            return "btn-5";
        } else if ("panel-1".equals(focusCycleRoot_id)) {
            return "btn-5";
        } else if ("panel-2".equals(focusCycleRoot_id)) {
            return "btn-8";
        }
        return null;
    }
}
class PolicyTest9 extends AbstractPolicyTest {
    protected Frame createFrame() {
        JFrame jframe = (JFrame) registerComponent("frame", new JFrame("Test Frame"));
        jframe.setLayout(new FlowLayout());
        jframe.add(registerComponent("btn-1", new JButton("jbutton")));
        jframe.add(registerComponent("btn-2", new JButton("jbutton")));
        Container cont1 = (Container)registerComponent("panel-1", new JPanel());
        cont1.add(registerComponent("btn-3", new JButton("jbutton")));
        cont1.add(registerComponent("btn-4", new JButton("jbutton")));
        cont1.add(registerComponent("btn-5", new JButton("jbutton")));
        Container cont2 = (Container)registerComponent("panel-2", new JPanel());
        cont2.add(registerComponent("btn-6", new JButton("jbutton")));
        cont2.add(registerComponent("btn-7", new JButton("jbutton")));
        cont2.add(registerComponent("btn-8", new JButton("jbutton")));
        jframe.add(cont1);
        jframe.add(cont2);
        return jframe;
    }
    protected void customizeHierarchy() {
        ((Container)getComponent("panel-1")).setFocusCycleRoot(true);
        ((Container)getComponent("panel-1")).setFocusTraversalPolicy(new LayoutFocusTraversalPolicy() {
                public Component getDefaultComponent(Container aContainer) {
                    return getComponent("btn-4");
                }
            });
        ((Container)getComponent("panel-2")).setFocusTraversalPolicyProvider(true);
        ((Container)getComponent("panel-2")).setFocusTraversalPolicy(new LayoutFocusTraversalPolicy() {
                public Component getDefaultComponent(Container aContainer) {
                    return getComponent("btn-7");
                }
            });
    }
    protected Map<String, String> getForwardOrder() {
        Map<String, String> order = new HashMap<String, String>();
        order.put("frame", "btn-1");
        order.put("btn-1", "btn-2");
        order.put("btn-2", "btn-4");
        order.put("btn-3", "btn-4");
        order.put("btn-4", "btn-5");
        order.put("btn-5", "btn-3");
        order.put("btn-6", "btn-7");
        order.put("btn-7", "btn-8");
        order.put("btn-8", "btn-1");
        order.put("panel-1", "btn-4");
        order.put("panel-2", "btn-7");
        return order;
    }
    protected Map<String, String> getBackwardOrder() {
        Map<String, String> order = new HashMap<String, String>();
        order.put("btn-1", "btn-8");
        order.put("btn-2", "btn-1");
        order.put("btn-3", "btn-5");
        order.put("btn-4", "btn-3");
        order.put("btn-5", "btn-4");
        order.put("btn-6", "btn-4");
        order.put("btn-7", "btn-6");
        order.put("btn-8", "btn-7");
        return order;
    }
    protected String[] getContainersToTest() {
        return new String[] {"frame", "panel-1", "panel-2"};
    }
    protected String getDefaultComp(String focusCycleRoot_id) {
        if ("frame".equals(focusCycleRoot_id)) {
            return "btn-1";
        } else if ("panel-1".equals(focusCycleRoot_id)) {
            return "btn-4";
        } else if ("panel-2".equals(focusCycleRoot_id)) {
            return "btn-7";
        }
        return null;
    }
    protected String getFirstComp(String focusCycleRoot_id) {
        if ("frame".equals(focusCycleRoot_id)) {
            return "btn-1";
        } else if ("panel-1".equals(focusCycleRoot_id)) {
            return "btn-3";
        } else if ("panel-2".equals(focusCycleRoot_id)) {
            return "btn-6";
        }
        return null;
    }
    protected String getLastComp(String focusCycleRoot_id) {
        if ("frame".equals(focusCycleRoot_id)) {
            return "btn-8";
        } else if ("panel-1".equals(focusCycleRoot_id)) {
            return "btn-5";
        } else if ("panel-2".equals(focusCycleRoot_id)) {
            return "btn-8";
        }
        return null;
    }
}
class PolicyTest10 extends AbstractPolicyTest {
    protected Frame createFrame() {
        JFrame jframe = (JFrame) registerComponent("frame", new JFrame("Test Frame"));
        jframe.setLayout(new GridLayout(2, 1));
        Container cont0 = new JPanel();
        cont0.add(registerComponent("btn-1", new JButton("jbutton")));
        cont0.add(registerComponent("btn-2", new JButton("jbutton")));
        Container cont1 = (Container)registerComponent("panel-1", new JPanel());
        cont1.add(registerComponent("btn-3", new JButton("jbutton")));
        cont1.add(registerComponent("btn-4", new JButton("jbutton")));
        Container cont2 = (Container)registerComponent("panel-2", new JPanel());
        cont2.add(registerComponent("btn-5", new JButton("jbutton")));
        cont2.add(registerComponent("btn-6", new JButton("jbutton")));
        cont1.add(cont2);
        jframe.add(cont0);
        jframe.add(cont1);
        return jframe;
    }
    protected void customizeHierarchy() {
        ((Container)getComponent("panel-1")).setFocusCycleRoot(true);
        ((Container)getComponent("panel-1")).setFocusTraversalPolicy(new LayoutFocusTraversalPolicy() {
                public Component getDefaultComponent(Container aContainer) {
                    return getComponent("panel-2");
                }
            });
        ((Container)getComponent("panel-2")).setFocusTraversalPolicyProvider(true);
        ((Container)getComponent("panel-2")).setFocusTraversalPolicy(new LayoutFocusTraversalPolicy());
    }
    protected Map<String, String> getForwardOrder() {
        Map<String, String> order = new HashMap<String, String>();
        order.put("frame", "btn-1");
        order.put("btn-1", "btn-2");
        order.put("btn-2", "panel-2");
        order.put("btn-3", "btn-4");
        order.put("btn-4", "btn-5");
        order.put("btn-5", "btn-6");
        order.put("btn-6", "btn-3");
        order.put("panel-1", "panel-2");
        order.put("panel-2", "btn-5");
        return order;
    }
    protected Map<String, String> getBackwardOrder() {
        Map<String, String> order = new HashMap<String, String>();
        order.put("btn-1", "btn-2");
        order.put("btn-2", "btn-1");
        order.put("btn-3", "btn-6");
        order.put("btn-4", "btn-3");
        order.put("btn-5", "btn-4");
        order.put("btn-6", "btn-5");
        return order;
    }
    protected String[] getContainersToTest() {
        return new String[] {"frame", "panel-1", "panel-2"};
    }
    protected String getDefaultComp(String focusCycleRoot_id) {
        if ("frame".equals(focusCycleRoot_id)) {
            return "btn-1";
        } else if ("panel-1".equals(focusCycleRoot_id)) {
            return "panel-2";
        } else if ("panel-2".equals(focusCycleRoot_id)) {
            return "btn-5";
        }
        return null;
    }
    protected String getFirstComp(String focusCycleRoot_id) {
        if ("frame".equals(focusCycleRoot_id)) {
            return "btn-1";
        } else if ("panel-1".equals(focusCycleRoot_id)) {
            return "btn-3";
        } else if ("panel-2".equals(focusCycleRoot_id)) {
            return "btn-5";
        }
        return null;
    }
    protected String getLastComp(String focusCycleRoot_id) {
        if ("frame".equals(focusCycleRoot_id)) {
            return "btn-2";
        } else {
            return "btn-6";
        }
    }
}
class PolicyTest11 extends AbstractPolicyTest {
    protected Frame createFrame() {
        JFrame jframe = (JFrame) registerComponent("jframe", new JFrame("Test Frame"));
        jframe.setLayout(new FlowLayout());
        Container cont = (Container)registerComponent("jpanel", new JPanel());
        cont.add(registerComponent("btn-1", new JButton("jbutton")));
        cont.add(registerComponent("btn-2", new JButton("jbutton")));
        jframe.add(cont);
        jframe.add(registerComponent("btn-3", new JButton("jbutton")));
        return jframe;
    }
    protected void customizeHierarchy() {
        ((Container)getComponent("jframe")).setFocusTraversalPolicy(new LayoutFocusTraversalPolicy());
        ((Container)getComponent("jpanel")).setFocusCycleRoot(true);
    }
    protected Map<String, String> getForwardOrder() {
        Map<String, String> order = new HashMap<String, String>();
        order.put("jframe", "btn-1");
        order.put("btn-1", "btn-2");
        order.put("btn-2", "btn-1");
        order.put("btn-3", "btn-1");
        return order;
    }
    protected Map<String, String> getBackwardOrder() {
        Map<String, String> order = new HashMap<String, String>();
        order.put("btn-3", "btn-1");
        order.put("btn-2", "btn-1");
        order.put("btn-1", "btn-2");
        order.put("jframe", "btn-3");
        return order;
    }
    protected String[] getContainersToTest() {
        return new String[] {"jframe"};
    }
    protected String getDefaultComp(String focusCycleRoot_id) {
        return "btn-1";
    }
    protected String getFirstComp(String focusCycleRoot_id) {
        return "btn-1";
    }
    protected String getLastComp(String focusCycleRoot_id) {
        return "btn-3";
    }
}
