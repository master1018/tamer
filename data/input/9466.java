final class SnmpMibTree {
    public SnmpMibTree() {
      defaultAgent= null;
      root= new TreeNode(-1, null, null);
    }
    public void setDefaultAgent(SnmpMibAgent def) {
        defaultAgent= def;
        root.agent= def;
    }
    public SnmpMibAgent getDefaultAgent() {
        return defaultAgent;
    }
    public void register(SnmpMibAgent agent) {
        root.registerNode(agent);
    }
    public void register(SnmpMibAgent agent, long[] oid) {
      root.registerNode(oid, 0, agent);
    }
    public SnmpMibAgent getAgentMib(SnmpOid oid) {
        TreeNode node= root.retrieveMatchingBranch(oid.longValue(), 0);
        if (node == null)
            return defaultAgent;
        else
            if(node.getAgentMib() == null)
                return defaultAgent;
            else
                return node.getAgentMib();
    }
    public void unregister(SnmpMibAgent agent, SnmpOid[] oids) {
        for(int i = 0; i < oids.length; i++) {
            long[] oid = oids[i].longValue();
            TreeNode node = root.retrieveMatchingBranch(oid, 0);
            if (node == null)
                continue;
            node.removeAgent(agent);
        }
    }
    public void unregister(SnmpMibAgent agent) {
        root.removeAgentFully(agent);
    }
    public void printTree() {
        root.printTree(">");
    }
    private SnmpMibAgent defaultAgent;
    private TreeNode root;
    final class TreeNode {
        void registerNode(SnmpMibAgent agent) {
            long[] oid= agent.getRootOid();
            registerNode(oid, 0, agent);
        }
        TreeNode retrieveMatchingBranch(long[] oid, int cursor) {
            TreeNode node= retrieveChild(oid, cursor);
            if (node == null)
                return this;
            if (children.size() == 0) {
                return node;
            }
            if( cursor + 1 == oid.length) {
                return node;
            }
            TreeNode n = node.retrieveMatchingBranch(oid, cursor + 1);
            return n.agent == null ? this : n;
        }
        SnmpMibAgent getAgentMib() {
            return agent;
        }
        public void printTree(String ident) {
            StringBuffer buff= new StringBuffer();
            if (agents == null) {
                return;
            }
            for(Enumeration e= agents.elements(); e.hasMoreElements(); ) {
                SnmpMibAgent mib= (SnmpMibAgent) e.nextElement();
                if (mib == null)
                    buff.append("empty ");
                else
                    buff.append(mib.getMibName() + " ");
            }
            ident+= " ";
            if (children == null) {
                return;
            }
            for(Enumeration e= children.elements(); e.hasMoreElements(); ) {
                TreeNode node= (TreeNode) e.nextElement();
                node.printTree(ident);
            }
        }
        private TreeNode(long nodeValue, SnmpMibAgent agent, TreeNode sup) {
            this.nodeValue= nodeValue;
            this.parent= sup;
            agents.addElement(agent);
        }
        private void removeAgentFully(SnmpMibAgent agent) {
            Vector<TreeNode> v = new Vector<TreeNode>();
            for(Enumeration<TreeNode> e= children.elements();
                e.hasMoreElements(); ) {
                TreeNode node= e.nextElement();
                node.removeAgentFully(agent);
                if(node.agents.isEmpty())
                    v.add(node);
            }
            for(Enumeration<TreeNode> e= v.elements(); e.hasMoreElements(); ) {
                children.removeElement(e.nextElement());
            }
            removeAgent(agent);
        }
        private void removeAgent(SnmpMibAgent mib) {
            if (!agents.contains(mib))
                return;
            agents.removeElement(mib);
            if (!agents.isEmpty())
                agent= agents.firstElement();
        }
      private void setAgent(SnmpMibAgent agent) {
        this.agent = agent;
      }
        private void registerNode(long[] oid, int cursor, SnmpMibAgent agent) {
            if (cursor >= oid.length)
                return;
            TreeNode child = retrieveChild(oid, cursor);
            if (child == null) {
                long theValue= oid[cursor];
                child= new TreeNode(theValue, agent, this);
                children.addElement(child);
            }
            else
                if (agents.contains(agent) == false) {
                    agents.addElement(agent);
                }
            if(cursor == (oid.length - 1)) {
              child.setAgent(agent);
            }
            else
              child.registerNode(oid, cursor+1, agent);
        }
        private TreeNode retrieveChild(long[] oid, int current) {
            long theValue= oid[current];
            for(Enumeration e= children.elements(); e.hasMoreElements(); ) {
                TreeNode node= (TreeNode) e.nextElement();
                if (node.match(theValue))
                    return node;
            }
            return null;
        }
        final private boolean match(long value) {
            return (nodeValue == value) ? true : false;
        }
        private Vector<TreeNode> children= new Vector<TreeNode>();
        private Vector<SnmpMibAgent> agents= new Vector<SnmpMibAgent>();
        private long nodeValue;
        private SnmpMibAgent agent;
        private TreeNode parent;
    }; 
}
