class PolicyChecker extends PKIXCertPathChecker {
    private final Set<String> initPolicies;
    private final int certPathLen;
    private final boolean expPolicyRequired;
    private final boolean polMappingInhibited;
    private final boolean anyPolicyInhibited;
    private final boolean rejectPolicyQualifiers;
    private PolicyNodeImpl rootNode;
    private int explicitPolicy;
    private int policyMapping;
    private int inhibitAnyPolicy;
    private int certIndex;
    private Set<String> supportedExts;
    private static final Debug debug = Debug.getInstance("certpath");
    static final String ANY_POLICY = "2.5.29.32.0";
    PolicyChecker(Set<String> initialPolicies, int certPathLen,
        boolean expPolicyRequired, boolean polMappingInhibited,
        boolean anyPolicyInhibited, boolean rejectPolicyQualifiers,
        PolicyNodeImpl rootNode) throws CertPathValidatorException
    {
        if (initialPolicies.isEmpty()) {
            this.initPolicies = new HashSet<String>(1);
            this.initPolicies.add(ANY_POLICY);
        } else {
            this.initPolicies = new HashSet<String>(initialPolicies);
        }
        this.certPathLen = certPathLen;
        this.expPolicyRequired = expPolicyRequired;
        this.polMappingInhibited = polMappingInhibited;
        this.anyPolicyInhibited = anyPolicyInhibited;
        this.rejectPolicyQualifiers = rejectPolicyQualifiers;
        this.rootNode = rootNode;
        init(false);
    }
    public void init(boolean forward) throws CertPathValidatorException {
        if (forward) {
            throw new CertPathValidatorException
                                        ("forward checking not supported");
        }
        certIndex = 1;
        explicitPolicy = (expPolicyRequired ? 0 : certPathLen + 1);
        policyMapping = (polMappingInhibited ? 0 : certPathLen + 1);
        inhibitAnyPolicy = (anyPolicyInhibited ? 0 : certPathLen + 1);
    }
    public boolean isForwardCheckingSupported() {
        return false;
    }
    public Set<String> getSupportedExtensions() {
        if (supportedExts == null) {
            supportedExts = new HashSet<String>();
            supportedExts.add(PKIXExtensions.CertificatePolicies_Id.toString());
            supportedExts.add(PKIXExtensions.PolicyMappings_Id.toString());
            supportedExts.add(PKIXExtensions.PolicyConstraints_Id.toString());
            supportedExts.add(PKIXExtensions.InhibitAnyPolicy_Id.toString());
            supportedExts = Collections.unmodifiableSet(supportedExts);
        }
        return supportedExts;
    }
    public void check(Certificate cert, Collection<String> unresCritExts)
        throws CertPathValidatorException
    {
        checkPolicy((X509Certificate) cert);
        if (unresCritExts != null && !unresCritExts.isEmpty()) {
            unresCritExts.remove(PKIXExtensions.CertificatePolicies_Id.toString());
            unresCritExts.remove(PKIXExtensions.PolicyMappings_Id.toString());
            unresCritExts.remove(PKIXExtensions.PolicyConstraints_Id.toString());
            unresCritExts.remove(PKIXExtensions.InhibitAnyPolicy_Id.toString());
        }
    }
    private void checkPolicy(X509Certificate currCert)
        throws CertPathValidatorException
    {
        String msg = "certificate policies";
        if (debug != null) {
            debug.println("PolicyChecker.checkPolicy() ---checking " + msg
                + "...");
            debug.println("PolicyChecker.checkPolicy() certIndex = "
                + certIndex);
            debug.println("PolicyChecker.checkPolicy() BEFORE PROCESSING: "
                + "explicitPolicy = " + explicitPolicy);
            debug.println("PolicyChecker.checkPolicy() BEFORE PROCESSING: "
                + "policyMapping = " + policyMapping);
            debug.println("PolicyChecker.checkPolicy() BEFORE PROCESSING: "
                + "inhibitAnyPolicy = " + inhibitAnyPolicy);
            debug.println("PolicyChecker.checkPolicy() BEFORE PROCESSING: "
                + "policyTree = " + rootNode);
        }
        X509CertImpl currCertImpl = null;
        try {
            currCertImpl = X509CertImpl.toImpl(currCert);
        } catch (CertificateException ce) {
            throw new CertPathValidatorException(ce);
        }
        boolean finalCert = (certIndex == certPathLen);
        rootNode = processPolicies(certIndex, initPolicies, explicitPolicy,
            policyMapping, inhibitAnyPolicy, rejectPolicyQualifiers, rootNode,
            currCertImpl, finalCert);
        if (!finalCert) {
            explicitPolicy = mergeExplicitPolicy(explicitPolicy, currCertImpl,
                                                 finalCert);
            policyMapping = mergePolicyMapping(policyMapping, currCertImpl);
            inhibitAnyPolicy = mergeInhibitAnyPolicy(inhibitAnyPolicy,
                                                     currCertImpl);
        }
        certIndex++;
        if (debug != null) {
            debug.println("PolicyChecker.checkPolicy() AFTER PROCESSING: "
                + "explicitPolicy = " + explicitPolicy);
            debug.println("PolicyChecker.checkPolicy() AFTER PROCESSING: "
                + "policyMapping = " + policyMapping);
            debug.println("PolicyChecker.checkPolicy() AFTER PROCESSING: "
                + "inhibitAnyPolicy = " + inhibitAnyPolicy);
            debug.println("PolicyChecker.checkPolicy() AFTER PROCESSING: "
                + "policyTree = " + rootNode);
            debug.println("PolicyChecker.checkPolicy() " + msg + " verified");
        }
    }
    static int mergeExplicitPolicy(int explicitPolicy, X509CertImpl currCert,
        boolean finalCert) throws CertPathValidatorException
    {
        if ((explicitPolicy > 0) && !X509CertImpl.isSelfIssued(currCert)) {
            explicitPolicy--;
        }
        try {
            PolicyConstraintsExtension polConstExt
                = currCert.getPolicyConstraintsExtension();
            if (polConstExt == null)
                return explicitPolicy;
            int require = ((Integer)
                polConstExt.get(PolicyConstraintsExtension.REQUIRE)).intValue();
            if (debug != null) {
                debug.println("PolicyChecker.mergeExplicitPolicy() "
                   + "require Index from cert = " + require);
            }
            if (!finalCert) {
                if (require != -1) {
                    if ((explicitPolicy == -1) || (require < explicitPolicy)) {
                        explicitPolicy = require;
                    }
                }
            } else {
                if (require == 0)
                    explicitPolicy = require;
            }
        } catch (Exception e) {
            if (debug != null) {
                debug.println("PolicyChecker.mergeExplicitPolicy "
                              + "unexpected exception");
                e.printStackTrace();
            }
            throw new CertPathValidatorException(e);
        }
        return explicitPolicy;
    }
    static int mergePolicyMapping(int policyMapping, X509CertImpl currCert)
        throws CertPathValidatorException
    {
        if ((policyMapping > 0) && !X509CertImpl.isSelfIssued(currCert)) {
            policyMapping--;
        }
        try {
            PolicyConstraintsExtension polConstExt
                = currCert.getPolicyConstraintsExtension();
            if (polConstExt == null)
                return policyMapping;
            int inhibit = ((Integer)
                polConstExt.get(PolicyConstraintsExtension.INHIBIT)).intValue();
            if (debug != null)
                debug.println("PolicyChecker.mergePolicyMapping() "
                    + "inhibit Index from cert = " + inhibit);
            if (inhibit != -1) {
                if ((policyMapping == -1) || (inhibit < policyMapping)) {
                    policyMapping = inhibit;
                }
            }
        } catch (Exception e) {
            if (debug != null) {
                debug.println("PolicyChecker.mergePolicyMapping "
                              + "unexpected exception");
                e.printStackTrace();
            }
            throw new CertPathValidatorException(e);
        }
        return policyMapping;
    }
    static int mergeInhibitAnyPolicy(int inhibitAnyPolicy,
        X509CertImpl currCert) throws CertPathValidatorException
    {
        if ((inhibitAnyPolicy > 0) && !X509CertImpl.isSelfIssued(currCert)) {
            inhibitAnyPolicy--;
        }
        try {
            InhibitAnyPolicyExtension inhAnyPolExt = (InhibitAnyPolicyExtension)
                currCert.getExtension(PKIXExtensions.InhibitAnyPolicy_Id);
            if (inhAnyPolExt == null)
                return inhibitAnyPolicy;
            int skipCerts = ((Integer)
                inhAnyPolExt.get(InhibitAnyPolicyExtension.SKIP_CERTS)).intValue();
            if (debug != null)
                debug.println("PolicyChecker.mergeInhibitAnyPolicy() "
                    + "skipCerts Index from cert = " + skipCerts);
            if (skipCerts != -1) {
                if (skipCerts < inhibitAnyPolicy) {
                    inhibitAnyPolicy = skipCerts;
                }
            }
        } catch (Exception e) {
            if (debug != null) {
                debug.println("PolicyChecker.mergeInhibitAnyPolicy "
                              + "unexpected exception");
                e.printStackTrace();
            }
            throw new CertPathValidatorException(e);
        }
        return inhibitAnyPolicy;
    }
    static PolicyNodeImpl processPolicies(int certIndex, Set<String> initPolicies,
        int explicitPolicy, int policyMapping, int inhibitAnyPolicy,
        boolean rejectPolicyQualifiers, PolicyNodeImpl origRootNode,
        X509CertImpl currCert, boolean finalCert)
        throws CertPathValidatorException
    {
        boolean policiesCritical = false;
        List<PolicyInformation> policyInfo;
        PolicyNodeImpl rootNode = null;
        Set<PolicyQualifierInfo> anyQuals = new HashSet<PolicyQualifierInfo>();
        if (origRootNode == null)
            rootNode = null;
        else
            rootNode = origRootNode.copyTree();
        CertificatePoliciesExtension currCertPolicies
            = currCert.getCertificatePoliciesExtension();
        if ((currCertPolicies != null) && (rootNode != null)) {
            policiesCritical = currCertPolicies.isCritical();
            if (debug != null)
                debug.println("PolicyChecker.processPolicies() "
                    + "policiesCritical = " + policiesCritical);
            try {
                policyInfo = (List<PolicyInformation>)
                    currCertPolicies.get(CertificatePoliciesExtension.POLICIES);
            } catch (IOException ioe) {
                throw new CertPathValidatorException("Exception while "
                    + "retrieving policyOIDs", ioe);
            }
            if (debug != null)
                debug.println("PolicyChecker.processPolicies() "
                    + "rejectPolicyQualifiers = " + rejectPolicyQualifiers);
            boolean foundAnyPolicy = false;
            for (PolicyInformation curPolInfo : policyInfo) {
                String curPolicy =
                    curPolInfo.getPolicyIdentifier().getIdentifier().toString();
                if (curPolicy.equals(ANY_POLICY)) {
                    foundAnyPolicy = true;
                    anyQuals = curPolInfo.getPolicyQualifiers();
                } else {
                    if (debug != null)
                        debug.println("PolicyChecker.processPolicies() "
                                      + "processing policy: " + curPolicy);
                    Set<PolicyQualifierInfo> pQuals =
                                        curPolInfo.getPolicyQualifiers();
                    if (!pQuals.isEmpty() && rejectPolicyQualifiers &&
                        policiesCritical) {
                        throw new CertPathValidatorException(
                            "critical policy qualifiers present in certificate",
                            null, null, -1, PKIXReason.INVALID_POLICY);
                    }
                    boolean foundMatch = processParents(certIndex,
                        policiesCritical, rejectPolicyQualifiers, rootNode,
                        curPolicy, pQuals, false);
                    if (!foundMatch) {
                        processParents(certIndex, policiesCritical,
                            rejectPolicyQualifiers, rootNode, curPolicy,
                            pQuals, true);
                    }
                }
            }
            if (foundAnyPolicy) {
                if ((inhibitAnyPolicy > 0) ||
                        (!finalCert && X509CertImpl.isSelfIssued(currCert))) {
                    if (debug != null) {
                        debug.println("PolicyChecker.processPolicies() "
                            + "processing policy: " + ANY_POLICY);
                    }
                    processParents(certIndex, policiesCritical,
                        rejectPolicyQualifiers, rootNode, ANY_POLICY, anyQuals,
                        true);
                }
            }
            rootNode.prune(certIndex);
            if (!rootNode.getChildren().hasNext()) {
                rootNode = null;
            }
        } else if (currCertPolicies == null) {
            if (debug != null)
                debug.println("PolicyChecker.processPolicies() "
                              + "no policies present in cert");
            rootNode = null;
        }
        if (rootNode != null) {
            if (!finalCert) {
                rootNode = processPolicyMappings(currCert, certIndex,
                    policyMapping, rootNode, policiesCritical, anyQuals);
            }
        }
        if ((rootNode != null) && (!initPolicies.contains(ANY_POLICY))
            && (currCertPolicies != null)) {
            rootNode = removeInvalidNodes(rootNode, certIndex,
                                          initPolicies, currCertPolicies);
            if ((rootNode != null) && finalCert) {
                rootNode = rewriteLeafNodes(certIndex, initPolicies, rootNode);
            }
        }
        if (finalCert) {
            explicitPolicy = mergeExplicitPolicy(explicitPolicy, currCert,
                                             finalCert);
        }
        if ((explicitPolicy == 0) && (rootNode == null)) {
            throw new CertPathValidatorException
                ("non-null policy tree required and policy tree is null",
                 null, null, -1, PKIXReason.INVALID_POLICY);
        }
        return rootNode;
    }
    private static PolicyNodeImpl rewriteLeafNodes(int certIndex,
            Set<String> initPolicies, PolicyNodeImpl rootNode) {
        Set<PolicyNodeImpl> anyNodes =
                        rootNode.getPolicyNodesValid(certIndex, ANY_POLICY);
        if (anyNodes.isEmpty()) {
            return rootNode;
        }
        PolicyNodeImpl anyNode = anyNodes.iterator().next();
        PolicyNodeImpl parentNode = (PolicyNodeImpl)anyNode.getParent();
        parentNode.deleteChild(anyNode);
        Set<String> initial = new HashSet<String>(initPolicies);
        for (PolicyNodeImpl node : rootNode.getPolicyNodes(certIndex)) {
            initial.remove(node.getValidPolicy());
        }
        if (initial.isEmpty()) {
            rootNode.prune(certIndex);
            if (rootNode.getChildren().hasNext() == false) {
                rootNode = null;
            }
        } else {
            boolean anyCritical = anyNode.isCritical();
            Set<PolicyQualifierInfo> anyQualifiers =
                                                anyNode.getPolicyQualifiers();
            for (String policy : initial) {
                Set<String> expectedPolicies = Collections.singleton(policy);
                PolicyNodeImpl node = new PolicyNodeImpl(parentNode, policy,
                    anyQualifiers, anyCritical, expectedPolicies, false);
            }
        }
        return rootNode;
    }
    private static boolean processParents(int certIndex,
        boolean policiesCritical, boolean rejectPolicyQualifiers,
        PolicyNodeImpl rootNode, String curPolicy,
        Set<PolicyQualifierInfo> pQuals,
        boolean matchAny) throws CertPathValidatorException
    {
        boolean foundMatch = false;
        if (debug != null)
            debug.println("PolicyChecker.processParents(): matchAny = "
                + matchAny);
        Set<PolicyNodeImpl> parentNodes =
                rootNode.getPolicyNodesExpected(certIndex - 1,
                                                curPolicy, matchAny);
        for (PolicyNodeImpl curParent : parentNodes) {
            if (debug != null)
                debug.println("PolicyChecker.processParents() "
                              + "found parent:\n" + curParent.asString());
            foundMatch = true;
            String curParPolicy = curParent.getValidPolicy();
            PolicyNodeImpl curNode = null;
            Set<String> curExpPols = null;
            if (curPolicy.equals(ANY_POLICY)) {
                Set<String> parExpPols = curParent.getExpectedPolicies();
            parentExplicitPolicies:
                for (String curParExpPol : parExpPols) {
                    Iterator<PolicyNodeImpl> childIter =
                                        curParent.getChildren();
                    while (childIter.hasNext()) {
                        PolicyNodeImpl childNode = childIter.next();
                        String childPolicy = childNode.getValidPolicy();
                        if (curParExpPol.equals(childPolicy)) {
                            if (debug != null)
                                debug.println(childPolicy + " in parent's "
                                    + "expected policy set already appears in "
                                    + "child node");
                            continue parentExplicitPolicies;
                        }
                    }
                    Set<String> expPols = new HashSet<String>();
                    expPols.add(curParExpPol);
                    curNode = new PolicyNodeImpl
                        (curParent, curParExpPol, pQuals,
                         policiesCritical, expPols, false);
                }
            } else {
                curExpPols = new HashSet<String>();
                curExpPols.add(curPolicy);
                curNode = new PolicyNodeImpl
                    (curParent, curPolicy, pQuals,
                     policiesCritical, curExpPols, false);
            }
        }
        return foundMatch;
    }
    private static PolicyNodeImpl processPolicyMappings(X509CertImpl currCert,
        int certIndex, int policyMapping, PolicyNodeImpl rootNode,
        boolean policiesCritical, Set<PolicyQualifierInfo> anyQuals)
        throws CertPathValidatorException
    {
        PolicyMappingsExtension polMappingsExt
            = currCert.getPolicyMappingsExtension();
        if (polMappingsExt == null)
            return rootNode;
        if (debug != null)
            debug.println("PolicyChecker.processPolicyMappings() "
                + "inside policyMapping check");
        List<CertificatePolicyMap> maps = null;
        try {
            maps = (List<CertificatePolicyMap>)polMappingsExt.get
                                        (PolicyMappingsExtension.MAP);
        } catch (IOException e) {
            if (debug != null) {
                debug.println("PolicyChecker.processPolicyMappings() "
                    + "mapping exception");
                e.printStackTrace();
            }
            throw new CertPathValidatorException("Exception while checking "
                                                 + "mapping", e);
        }
        boolean childDeleted = false;
        for (int j = 0; j < maps.size(); j++) {
            CertificatePolicyMap polMap = maps.get(j);
            String issuerDomain
                = polMap.getIssuerIdentifier().getIdentifier().toString();
            String subjectDomain
                = polMap.getSubjectIdentifier().getIdentifier().toString();
            if (debug != null) {
                debug.println("PolicyChecker.processPolicyMappings() "
                              + "issuerDomain = " + issuerDomain);
                debug.println("PolicyChecker.processPolicyMappings() "
                              + "subjectDomain = " + subjectDomain);
            }
            if (issuerDomain.equals(ANY_POLICY)) {
                throw new CertPathValidatorException
                    ("encountered an issuerDomainPolicy of ANY_POLICY",
                     null, null, -1, PKIXReason.INVALID_POLICY);
            }
            if (subjectDomain.equals(ANY_POLICY)) {
                throw new CertPathValidatorException
                    ("encountered a subjectDomainPolicy of ANY_POLICY",
                     null, null, -1, PKIXReason.INVALID_POLICY);
            }
            Set<PolicyNodeImpl> validNodes =
                rootNode.getPolicyNodesValid(certIndex, issuerDomain);
            if (!validNodes.isEmpty()) {
                for (PolicyNodeImpl curNode : validNodes) {
                    if ((policyMapping > 0) || (policyMapping == -1)) {
                        curNode.addExpectedPolicy(subjectDomain);
                    } else if (policyMapping == 0) {
                        PolicyNodeImpl parentNode =
                            (PolicyNodeImpl) curNode.getParent();
                        if (debug != null)
                            debug.println("PolicyChecker.processPolicyMappings"
                                + "() before deleting: policy tree = "
                                + rootNode);
                        parentNode.deleteChild(curNode);
                        childDeleted = true;
                        if (debug != null)
                            debug.println("PolicyChecker.processPolicyMappings"
                                + "() after deleting: policy tree = "
                                + rootNode);
                    }
                }
            } else { 
                if ((policyMapping > 0) || (policyMapping == -1)) {
                    Set<PolicyNodeImpl> validAnyNodes =
                        rootNode.getPolicyNodesValid(certIndex, ANY_POLICY);
                    for (PolicyNodeImpl curAnyNode : validAnyNodes) {
                        PolicyNodeImpl curAnyNodeParent =
                            (PolicyNodeImpl) curAnyNode.getParent();
                        Set<String> expPols = new HashSet<String>();
                        expPols.add(subjectDomain);
                        PolicyNodeImpl curNode = new PolicyNodeImpl
                            (curAnyNodeParent, issuerDomain, anyQuals,
                             policiesCritical, expPols, true);
                    }
                }
            }
        }
        if (childDeleted) {
            rootNode.prune(certIndex);
            if (!rootNode.getChildren().hasNext()) {
                if (debug != null)
                    debug.println("setting rootNode to null");
                rootNode = null;
            }
        }
        return rootNode;
    }
    private static PolicyNodeImpl removeInvalidNodes(PolicyNodeImpl rootNode,
        int certIndex, Set<String> initPolicies,
        CertificatePoliciesExtension currCertPolicies)
        throws CertPathValidatorException
    {
        List<PolicyInformation> policyInfo = null;
        try {
            policyInfo = (List<PolicyInformation>)
                currCertPolicies.get(CertificatePoliciesExtension.POLICIES);
        } catch (IOException ioe) {
            throw new CertPathValidatorException("Exception while "
                + "retrieving policyOIDs", ioe);
        }
        boolean childDeleted = false;
        for (PolicyInformation curPolInfo : policyInfo) {
            String curPolicy =
                curPolInfo.getPolicyIdentifier().getIdentifier().toString();
            if (debug != null)
                debug.println("PolicyChecker.processPolicies() "
                              + "processing policy second time: " + curPolicy);
            Set<PolicyNodeImpl> validNodes =
                        rootNode.getPolicyNodesValid(certIndex, curPolicy);
            for (PolicyNodeImpl curNode : validNodes) {
                PolicyNodeImpl parentNode = (PolicyNodeImpl)curNode.getParent();
                if (parentNode.getValidPolicy().equals(ANY_POLICY)) {
                    if ((!initPolicies.contains(curPolicy)) &&
                        (!curPolicy.equals(ANY_POLICY))) {
                        if (debug != null)
                            debug.println("PolicyChecker.processPolicies() "
                                + "before deleting: policy tree = " + rootNode);
                        parentNode.deleteChild(curNode);
                        childDeleted = true;
                        if (debug != null)
                            debug.println("PolicyChecker.processPolicies() "
                                + "after deleting: policy tree = " + rootNode);
                    }
                }
            }
        }
        if (childDeleted) {
            rootNode.prune(certIndex);
            if (!rootNode.getChildren().hasNext()) {
                rootNode = null;
            }
        }
        return rootNode;
    }
    PolicyNode getPolicyTree() {
        if (rootNode == null)
            return null;
        else {
            PolicyNodeImpl policyTree = rootNode.copyTree();
            policyTree.setImmutable();
            return policyTree;
        }
    }
}
