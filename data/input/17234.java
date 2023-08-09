public final class SunCertPathBuilder extends CertPathBuilderSpi {
    private static final Debug debug = Debug.getInstance("certpath");
    private PKIXBuilderParameters buildParams;
    private CertificateFactory cf;
    private boolean pathCompleted = false;
    private X500Principal targetSubjectDN;
    private PolicyNode policyTreeResult;
    private TrustAnchor trustAnchor;
    private PublicKey finalPublicKey;
    private X509CertSelector targetSel;
    private List<CertStore> orderedCertStores;
    private boolean onlyEECert = false;
    public SunCertPathBuilder() throws CertPathBuilderException {
        try {
            cf = CertificateFactory.getInstance("X.509");
        } catch (CertificateException e) {
            throw new CertPathBuilderException(e);
        }
        onlyEECert = AccessController.doPrivileged(
            new GetBooleanSecurityPropertyAction
                ("com.sun.security.onlyCheckRevocationOfEECert"));
    }
    public CertPathBuilderResult engineBuild(CertPathParameters params)
        throws CertPathBuilderException, InvalidAlgorithmParameterException {
        if (debug != null) {
            debug.println("SunCertPathBuilder.engineBuild(" + params + ")");
        }
        if (!(params instanceof PKIXBuilderParameters)) {
            throw new InvalidAlgorithmParameterException("inappropriate " +
                "parameter type, must be an instance of PKIXBuilderParameters");
        }
        boolean buildForward = true;
        if (params instanceof SunCertPathBuilderParameters) {
            buildForward =
                ((SunCertPathBuilderParameters)params).getBuildForward();
        }
        buildParams = (PKIXBuilderParameters)params;
        for (TrustAnchor anchor : buildParams.getTrustAnchors()) {
            if (anchor.getNameConstraints() != null) {
                throw new InvalidAlgorithmParameterException
                    ("name constraints in trust anchor not supported");
            }
        }
        CertSelector sel = buildParams.getTargetCertConstraints();
        if (!(sel instanceof X509CertSelector)) {
            throw new InvalidAlgorithmParameterException("the "
                + "targetCertConstraints parameter must be an "
                + "X509CertSelector");
        }
        targetSel = (X509CertSelector)sel;
        targetSubjectDN = targetSel.getSubject();
        if (targetSubjectDN == null) {
            X509Certificate targetCert = targetSel.getCertificate();
            if (targetCert != null) {
                targetSubjectDN = targetCert.getSubjectX500Principal();
            }
        }
        orderedCertStores =
            new ArrayList<CertStore>(buildParams.getCertStores());
        Collections.sort(orderedCertStores, new CertStoreComparator());
        if (targetSubjectDN == null) {
            targetSubjectDN = getTargetSubjectDN(orderedCertStores, targetSel);
        }
        if (targetSubjectDN == null) {
            throw new InvalidAlgorithmParameterException
                ("Could not determine unique target subject");
        }
        List<List<Vertex>> adjList = new ArrayList<List<Vertex>>();
        CertPathBuilderResult result =
            buildCertPath(buildForward, false, adjList);
        if (result == null) {
            if (debug != null) {
                debug.println("SunCertPathBuilder.engineBuild: 2nd pass");
            }
            adjList.clear();
            result = buildCertPath(buildForward, true, adjList);
            if (result == null) {
                throw new SunCertPathBuilderException("unable to find valid "
                    + "certification path to requested target",
                    new AdjacencyList(adjList));
            }
        }
        return result;
    }
    private CertPathBuilderResult buildCertPath(boolean buildForward,
        boolean searchAllCertStores, List<List<Vertex>> adjList)
        throws CertPathBuilderException {
        pathCompleted = false;
        trustAnchor = null;
        finalPublicKey = null;
        policyTreeResult = null;
        LinkedList<X509Certificate> certPathList =
            new LinkedList<X509Certificate>();
        try {
            if (buildForward) {
                buildForward(adjList, certPathList, searchAllCertStores);
            } else {
                buildReverse(adjList, certPathList);
            }
        } catch (Exception e) {
            if (debug != null) {
                debug.println("SunCertPathBuilder.engineBuild() exception in "
                    + "build");
                e.printStackTrace();
            }
            throw new SunCertPathBuilderException("unable to find valid "
                + "certification path to requested target", e,
                new AdjacencyList(adjList));
        }
        try {
            if (pathCompleted) {
                if (debug != null)
                    debug.println("SunCertPathBuilder.engineBuild() "
                                  + "pathCompleted");
                Collections.reverse(certPathList);
                return new SunCertPathBuilderResult(
                    cf.generateCertPath(certPathList), this.trustAnchor,
                    policyTreeResult, finalPublicKey,
                    new AdjacencyList(adjList));
            }
        } catch (Exception e) {
            if (debug != null) {
                debug.println("SunCertPathBuilder.engineBuild() exception "
                              + "in wrap-up");
                e.printStackTrace();
            }
            throw new SunCertPathBuilderException("unable to find valid "
                + "certification path to requested target", e,
                new AdjacencyList(adjList));
        }
        return null;
    }
    private void buildReverse(List<List<Vertex>> adjacencyList,
        LinkedList<X509Certificate> certPathList) throws Exception
    {
        if (debug != null) {
            debug.println("SunCertPathBuilder.buildReverse()...");
            debug.println("SunCertPathBuilder.buildReverse() InitialPolicies: "
                + buildParams.getInitialPolicies());
        }
        ReverseState currentState = new ReverseState();
        adjacencyList.clear();
        adjacencyList.add(new LinkedList<Vertex>());
        Iterator<TrustAnchor> iter = buildParams.getTrustAnchors().iterator();
        while (iter.hasNext()) {
            TrustAnchor anchor = iter.next();
            if (anchorIsTarget(anchor, targetSel)) {
                this.trustAnchor = anchor;
                this.pathCompleted = true;
                this.finalPublicKey = anchor.getTrustedCert().getPublicKey();
                break;
            }
            currentState.initState(buildParams.getMaxPathLength(),
                       buildParams.isExplicitPolicyRequired(),
                       buildParams.isPolicyMappingInhibited(),
                       buildParams.isAnyPolicyInhibited(),
                       buildParams.getCertPathCheckers());
            currentState.updateState(anchor);
            currentState.crlChecker =
                new CrlRevocationChecker(null, buildParams, null, onlyEECert);
            currentState.algorithmChecker = new AlgorithmChecker(anchor);
            try {
                depthFirstSearchReverse(null, currentState,
                new ReverseBuilder(buildParams, targetSubjectDN), adjacencyList,
                certPathList);
            } catch (Exception e) {
                if (iter.hasNext())
                    continue;
                else
                    throw e;
            }
            break;
        }
        if (debug != null) {
            debug.println("SunCertPathBuilder.buildReverse() returned from "
                + "depthFirstSearchReverse()");
            debug.println("SunCertPathBuilder.buildReverse() "
                + "certPathList.size: " + certPathList.size());
        }
    }
    private void buildForward(List<List<Vertex>> adjacencyList,
        LinkedList<X509Certificate> certPathList, boolean searchAllCertStores)
        throws GeneralSecurityException, IOException
    {
        if (debug != null) {
            debug.println("SunCertPathBuilder.buildForward()...");
        }
        ForwardState currentState = new ForwardState();
        currentState.initState(buildParams.getCertPathCheckers());
        adjacencyList.clear();
        adjacencyList.add(new LinkedList<Vertex>());
        currentState.crlChecker
            = new CrlRevocationChecker(null, buildParams, null, onlyEECert);
        depthFirstSearchForward(targetSubjectDN, currentState,
          new ForwardBuilder
              (buildParams, targetSubjectDN, searchAllCertStores, onlyEECert),
          adjacencyList, certPathList);
    }
    void depthFirstSearchForward(X500Principal dN, ForwardState currentState,
        ForwardBuilder builder, List<List<Vertex>> adjList,
        LinkedList<X509Certificate> certPathList)
        throws GeneralSecurityException, IOException
    {
        if (debug != null) {
            debug.println("SunCertPathBuilder.depthFirstSearchForward(" + dN
                + ", " + currentState.toString() + ")");
        }
        List<Vertex> vertices = addVertices
           (builder.getMatchingCerts(currentState, orderedCertStores), adjList);
        if (debug != null) {
            debug.println("SunCertPathBuilder.depthFirstSearchForward(): "
                + "certs.size=" + vertices.size());
        }
               vertices:
        for (Vertex vertex : vertices) {
            ForwardState nextState = (ForwardState) currentState.clone();
            X509Certificate cert = (X509Certificate) vertex.getCertificate();
            try {
                builder.verifyCert(cert, nextState, certPathList);
            } catch (GeneralSecurityException gse) {
                if (debug != null) {
                    debug.println("SunCertPathBuilder.depthFirstSearchForward()"
                        + ": validation failed: " + gse);
                    gse.printStackTrace();
                }
                vertex.setThrowable(gse);
                continue;
            }
            if (builder.isPathCompleted(cert)) {
                BasicChecker basicChecker = null;
                if (debug != null)
                    debug.println("SunCertPathBuilder.depthFirstSearchForward()"
                        + ": commencing final verification");
                ArrayList<X509Certificate> appendedCerts =
                    new ArrayList<X509Certificate>(certPathList);
                if (builder.trustAnchor.getTrustedCert() == null) {
                    appendedCerts.add(0, cert);
                }
                HashSet<String> initExpPolSet = new HashSet<String>(1);
                initExpPolSet.add(PolicyChecker.ANY_POLICY);
                PolicyNodeImpl rootNode = new PolicyNodeImpl(null,
                    PolicyChecker.ANY_POLICY, null, false, initExpPolSet, false);
                PolicyChecker policyChecker
                    = new PolicyChecker(buildParams.getInitialPolicies(),
                                appendedCerts.size(),
                                buildParams.isExplicitPolicyRequired(),
                                buildParams.isPolicyMappingInhibited(),
                                buildParams.isAnyPolicyInhibited(),
                                buildParams.getPolicyQualifiersRejected(),
                                rootNode);
                List<PKIXCertPathChecker> userCheckers = new
                    ArrayList<PKIXCertPathChecker>
                        (buildParams.getCertPathCheckers());
                int mustCheck = 0;
                userCheckers.add(mustCheck, policyChecker);
                mustCheck++;
                userCheckers.add(mustCheck,
                        new AlgorithmChecker(builder.trustAnchor));
                mustCheck++;
                if (nextState.keyParamsNeeded()) {
                    PublicKey rootKey = cert.getPublicKey();
                    if (builder.trustAnchor.getTrustedCert() == null) {
                        rootKey = builder.trustAnchor.getCAPublicKey();
                        if (debug != null)
                            debug.println(
                                "SunCertPathBuilder.depthFirstSearchForward " +
                                "using buildParams public key: " +
                                rootKey.toString());
                    }
                    TrustAnchor anchor = new TrustAnchor
                        (cert.getSubjectX500Principal(), rootKey, null);
                    basicChecker = new BasicChecker(anchor,
                                           builder.date,
                                           buildParams.getSigProvider(),
                                           true);
                    userCheckers.add(mustCheck, basicChecker);
                    mustCheck++;
                    if (buildParams.isRevocationEnabled()) {
                        userCheckers.add(mustCheck, new CrlRevocationChecker
                            (anchor, buildParams, null, onlyEECert));
                        mustCheck++;
                    }
                }
                for (int i=0; i<appendedCerts.size(); i++) {
                    X509Certificate currCert = appendedCerts.get(i);
                    if (debug != null)
                        debug.println("current subject = "
                                      + currCert.getSubjectX500Principal());
                    Set<String> unresCritExts =
                        currCert.getCriticalExtensionOIDs();
                    if (unresCritExts == null) {
                        unresCritExts = Collections.<String>emptySet();
                    }
                    for (int j=0; j<userCheckers.size(); j++) {
                        PKIXCertPathChecker currChecker = userCheckers.get(j);
                        if (j < mustCheck ||
                            !currChecker.isForwardCheckingSupported()) {
                            if (i == 0) {
                                currChecker.init(false);
                                if (j >= mustCheck &&
                                    currChecker instanceof AlgorithmChecker) {
                                    ((AlgorithmChecker)currChecker).
                                        trySetTrustAnchor(builder.trustAnchor);
                                }
                            }
                            try {
                                currChecker.check(currCert, unresCritExts);
                            } catch (CertPathValidatorException cpve) {
                                if (debug != null)
                                    debug.println
                                    ("SunCertPathBuilder.depthFirstSearchForward(): " +
                                    "final verification failed: " + cpve);
                                vertex.setThrowable(cpve);
                                continue vertices;
                            }
                        }
                    }
                    for (PKIXCertPathChecker checker :
                         buildParams.getCertPathCheckers())
                    {
                        if (checker.isForwardCheckingSupported()) {
                            Set<String> suppExts =
                                checker.getSupportedExtensions();
                            if (suppExts != null) {
                                unresCritExts.removeAll(suppExts);
                            }
                        }
                    }
                    if (!unresCritExts.isEmpty()) {
                        unresCritExts.remove
                            (PKIXExtensions.BasicConstraints_Id.toString());
                        unresCritExts.remove
                            (PKIXExtensions.NameConstraints_Id.toString());
                        unresCritExts.remove
                            (PKIXExtensions.CertificatePolicies_Id.toString());
                        unresCritExts.remove
                            (PKIXExtensions.PolicyMappings_Id.toString());
                        unresCritExts.remove
                            (PKIXExtensions.PolicyConstraints_Id.toString());
                        unresCritExts.remove
                            (PKIXExtensions.InhibitAnyPolicy_Id.toString());
                        unresCritExts.remove(PKIXExtensions.
                            SubjectAlternativeName_Id.toString());
                        unresCritExts.remove
                            (PKIXExtensions.KeyUsage_Id.toString());
                        unresCritExts.remove
                            (PKIXExtensions.ExtendedKeyUsage_Id.toString());
                        if (!unresCritExts.isEmpty()) {
                            throw new CertPathValidatorException
                                ("unrecognized critical extension(s)", null,
                                 null, -1, PKIXReason.UNRECOGNIZED_CRIT_EXT);
                        }
                    }
                }
                if (debug != null)
                    debug.println("SunCertPathBuilder.depthFirstSearchForward()"
                        + ": final verification succeeded - path completed!");
                pathCompleted = true;
                if (builder.trustAnchor.getTrustedCert() == null)
                    builder.addCertToPath(cert, certPathList);
                this.trustAnchor = builder.trustAnchor;
                if (basicChecker != null) {
                    finalPublicKey = basicChecker.getPublicKey();
                } else {
                    Certificate finalCert;
                    if (certPathList.size() == 0) {
                        finalCert = builder.trustAnchor.getTrustedCert();
                    } else {
                        finalCert = certPathList.get(certPathList.size()-1);
                    }
                    finalPublicKey = finalCert.getPublicKey();
                }
                policyTreeResult = policyChecker.getPolicyTree();
                return;
            } else {
                builder.addCertToPath(cert, certPathList);
            }
            nextState.updateState(cert);
            adjList.add(new LinkedList<Vertex>());
            vertex.setIndex(adjList.size() - 1);
            depthFirstSearchForward(cert.getIssuerX500Principal(), nextState, builder,
                adjList, certPathList);
            if (pathCompleted) {
                return;
            } else {
                if (debug != null)
                    debug.println("SunCertPathBuilder.depthFirstSearchForward()"
                        + ": backtracking");
                builder.removeFinalCertFromPath(certPathList);
            }
        }
    }
    void depthFirstSearchReverse(X500Principal dN, ReverseState currentState,
        ReverseBuilder builder, List<List<Vertex>> adjList,
        LinkedList<X509Certificate> certPathList)
        throws GeneralSecurityException, IOException
    {
        if (debug != null)
            debug.println("SunCertPathBuilder.depthFirstSearchReverse(" + dN
                + ", " + currentState.toString() + ")");
        List<Vertex> vertices = addVertices
           (builder.getMatchingCerts(currentState, orderedCertStores), adjList);
        if (debug != null)
            debug.println("SunCertPathBuilder.depthFirstSearchReverse(): "
                + "certs.size=" + vertices.size());
        for (Vertex vertex : vertices) {
            ReverseState nextState = (ReverseState) currentState.clone();
            X509Certificate cert = (X509Certificate) vertex.getCertificate();
            try {
                builder.verifyCert(cert, nextState, certPathList);
            } catch (GeneralSecurityException gse) {
                if (debug != null)
                    debug.println("SunCertPathBuilder.depthFirstSearchReverse()"
                        + ": validation failed: " + gse);
                vertex.setThrowable(gse);
                continue;
            }
            if (!currentState.isInitial())
                builder.addCertToPath(cert, certPathList);
            this.trustAnchor = currentState.trustAnchor;
            if (builder.isPathCompleted(cert)) {
                if (debug != null)
                    debug.println("SunCertPathBuilder.depthFirstSearchReverse()"
                        + ": path completed!");
                pathCompleted = true;
                PolicyNodeImpl rootNode = nextState.rootNode;
                if (rootNode == null)
                    policyTreeResult = null;
                else {
                    policyTreeResult = rootNode.copyTree();
                    ((PolicyNodeImpl)policyTreeResult).setImmutable();
                }
                finalPublicKey = cert.getPublicKey();
                if (finalPublicKey instanceof DSAPublicKey &&
                    ((DSAPublicKey)finalPublicKey).getParams() == null)
                {
                    finalPublicKey =
                        BasicChecker.makeInheritedParamsKey
                            (finalPublicKey, currentState.pubKey);
                }
                return;
            }
            nextState.updateState(cert);
            adjList.add(new LinkedList<Vertex>());
            vertex.setIndex(adjList.size() - 1);
            depthFirstSearchReverse(cert.getSubjectX500Principal(), nextState,
                builder, adjList, certPathList);
            if (pathCompleted) {
                return;
            } else {
                if (debug != null)
                    debug.println("SunCertPathBuilder.depthFirstSearchReverse()"
                        + ": backtracking");
                if (!currentState.isInitial())
                    builder.removeFinalCertFromPath(certPathList);
            }
        }
        if (debug != null)
            debug.println("SunCertPathBuilder.depthFirstSearchReverse() all "
                + "certs in this adjacency list checked");
    }
    private List<Vertex> addVertices(Collection<X509Certificate> certs,
        List<List<Vertex>> adjList) {
        List<Vertex> l = adjList.get(adjList.size() - 1);
        for (X509Certificate cert : certs) {
           Vertex v = new Vertex(cert);
           l.add(v);
        }
        return l;
    }
    private boolean anchorIsTarget(TrustAnchor anchor, X509CertSelector sel) {
        X509Certificate anchorCert = anchor.getTrustedCert();
        if (anchorCert != null) {
            return sel.match(anchorCert);
        }
        return false;
    }
    private static class CertStoreComparator implements Comparator<CertStore> {
        public int compare(CertStore store1, CertStore store2) {
            if (Builder.isLocalCertStore(store1)) {
                return -1;
            } else {
                return 1;
            }
        }
    }
    private X500Principal getTargetSubjectDN(List<CertStore> stores,
        X509CertSelector targetSel) {
        for (CertStore store : stores) {
            try {
                Collection<? extends Certificate> targetCerts =
                    (Collection<? extends Certificate>)
                        store.getCertificates(targetSel);
                if (!targetCerts.isEmpty()) {
                    X509Certificate targetCert =
                        (X509Certificate)targetCerts.iterator().next();
                    return targetCert.getSubjectX500Principal();
                }
            } catch (CertStoreException e) {
                if (debug != null) {
                    debug.println("SunCertPathBuilder.getTargetSubjectDN: " +
                        "non-fatal exception retrieving certs: " + e);
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
