final class PolicyNodeImpl implements PolicyNode {
    private static final String ANY_POLICY = "2.5.29.32.0";
    private PolicyNodeImpl mParent;
    private HashSet<PolicyNodeImpl> mChildren;
    private String mValidPolicy;
    private HashSet<PolicyQualifierInfo> mQualifierSet;
    private boolean mCriticalityIndicator;
    private HashSet<String> mExpectedPolicySet;
    private boolean mOriginalExpectedPolicySet;
    private int mDepth;
    private boolean isImmutable = false;
    PolicyNodeImpl(PolicyNodeImpl parent, String validPolicy,
                Set<PolicyQualifierInfo> qualifierSet,
                boolean criticalityIndicator, Set<String> expectedPolicySet,
                boolean generatedByPolicyMapping) {
        mParent = parent;
        mChildren = new HashSet<PolicyNodeImpl>();
        if (validPolicy != null)
            mValidPolicy = validPolicy;
        else
            mValidPolicy = "";
        if (qualifierSet != null)
            mQualifierSet = new HashSet<PolicyQualifierInfo>(qualifierSet);
        else
            mQualifierSet = new HashSet<PolicyQualifierInfo>();
        mCriticalityIndicator = criticalityIndicator;
        if (expectedPolicySet != null)
            mExpectedPolicySet = new HashSet<String>(expectedPolicySet);
        else
            mExpectedPolicySet = new HashSet<String>();
        mOriginalExpectedPolicySet = !generatedByPolicyMapping;
        if (mParent != null) {
            mDepth = mParent.getDepth() + 1;
            mParent.addChild(this);
        } else {
            mDepth = 0;
        }
    }
    PolicyNodeImpl(PolicyNodeImpl parent, PolicyNodeImpl node) {
        this(parent, node.mValidPolicy, node.mQualifierSet,
             node.mCriticalityIndicator, node.mExpectedPolicySet, false);
    }
    public PolicyNode getParent() {
        return mParent;
    }
    public Iterator<PolicyNodeImpl> getChildren() {
        return Collections.unmodifiableSet(mChildren).iterator();
    }
    public int getDepth() {
        return mDepth;
    }
    public String getValidPolicy() {
        return mValidPolicy;
    }
    public Set<PolicyQualifierInfo> getPolicyQualifiers() {
        return Collections.unmodifiableSet(mQualifierSet);
    }
    public Set<String> getExpectedPolicies() {
        return Collections.unmodifiableSet(mExpectedPolicySet);
    }
    public boolean isCritical() {
        return mCriticalityIndicator;
    }
    public String toString() {
        StringBuffer buffer = new StringBuffer(this.asString());
        Iterator<PolicyNodeImpl> it = getChildren();
        while (it.hasNext()) {
            buffer.append(it.next());
        }
        return buffer.toString();
    }
    boolean isImmutable() {
        return isImmutable;
    }
    void setImmutable() {
        if (isImmutable)
            return;
        for (PolicyNodeImpl node : mChildren) {
            node.setImmutable();
        }
        isImmutable = true;
    }
    private void addChild(PolicyNodeImpl child) {
        if (isImmutable) {
            throw new IllegalStateException("PolicyNode is immutable");
        }
        mChildren.add(child);
    }
    void addExpectedPolicy(String expectedPolicy) {
        if (isImmutable) {
            throw new IllegalStateException("PolicyNode is immutable");
        }
        if (mOriginalExpectedPolicySet) {
            mExpectedPolicySet.clear();
            mOriginalExpectedPolicySet = false;
        }
        mExpectedPolicySet.add(expectedPolicy);
    }
    void prune(int depth) {
        if (isImmutable)
            throw new IllegalStateException("PolicyNode is immutable");
        if (mChildren.size() == 0)
            return;
        Iterator<PolicyNodeImpl> it = mChildren.iterator();
        while (it.hasNext()) {
            PolicyNodeImpl node = it.next();
            node.prune(depth);
            if ((node.mChildren.size() == 0) && (depth > mDepth + 1))
                it.remove();
        }
    }
    void deleteChild(PolicyNode childNode) {
        if (isImmutable) {
            throw new IllegalStateException("PolicyNode is immutable");
        }
        mChildren.remove(childNode);
    }
    PolicyNodeImpl copyTree() {
        return copyTree(null);
    }
    private PolicyNodeImpl copyTree(PolicyNodeImpl parent) {
        PolicyNodeImpl newNode = new PolicyNodeImpl(parent, this);
        for (PolicyNodeImpl node : mChildren) {
            node.copyTree(newNode);
        }
        return newNode;
    }
    Set<PolicyNodeImpl> getPolicyNodes(int depth) {
        Set<PolicyNodeImpl> set = new HashSet<PolicyNodeImpl>();
        getPolicyNodes(depth, set);
        return set;
    }
    private void getPolicyNodes(int depth, Set<PolicyNodeImpl> set) {
        if (mDepth == depth) {
            set.add(this);
        } else {
            for (PolicyNodeImpl node : mChildren) {
                node.getPolicyNodes(depth, set);
            }
        }
    }
    Set<PolicyNodeImpl> getPolicyNodesExpected(int depth,
        String expectedOID, boolean matchAny) {
        if (expectedOID.equals(ANY_POLICY)) {
            return getPolicyNodes(depth);
        } else {
            return getPolicyNodesExpectedHelper(depth, expectedOID, matchAny);
        }
    }
    private Set<PolicyNodeImpl> getPolicyNodesExpectedHelper(int depth,
        String expectedOID, boolean matchAny) {
        HashSet<PolicyNodeImpl> set = new HashSet<PolicyNodeImpl>();
        if (mDepth < depth) {
            for (PolicyNodeImpl node : mChildren) {
                set.addAll(node.getPolicyNodesExpectedHelper(depth,
                                                             expectedOID,
                                                             matchAny));
            }
        } else {
            if (matchAny) {
                if (mExpectedPolicySet.contains(ANY_POLICY))
                    set.add(this);
            } else {
                if (mExpectedPolicySet.contains(expectedOID))
                    set.add(this);
            }
        }
        return set;
    }
    Set<PolicyNodeImpl> getPolicyNodesValid(int depth, String validOID) {
        HashSet<PolicyNodeImpl> set = new HashSet<PolicyNodeImpl>();
        if (mDepth < depth) {
            for (PolicyNodeImpl node : mChildren) {
                set.addAll(node.getPolicyNodesValid(depth, validOID));
            }
        } else {
            if (mValidPolicy.equals(validOID))
                set.add(this);
        }
        return set;
    }
    private static String policyToString(String oid) {
        if (oid.equals(ANY_POLICY)) {
            return "anyPolicy";
        } else {
            return oid;
        }
    }
    String asString() {
        if (mParent == null) {
            return "anyPolicy  ROOT\n";
        } else {
            StringBuffer sb = new StringBuffer();
            for (int i = 0, n = getDepth(); i < n; i++) {
                sb.append("  ");
            }
            sb.append(policyToString(getValidPolicy()));
            sb.append("  CRIT: ");
            sb.append(isCritical());
            sb.append("  EP: ");
            for (String policy : getExpectedPolicies()) {
                sb.append(policyToString(policy));
                sb.append(" ");
            }
            sb.append(" (");
            sb.append(getDepth());
            sb.append(")\n");
            return sb.toString();
        }
    }
}
