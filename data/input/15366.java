public class SunCertPathBuilderResult extends PKIXCertPathBuilderResult {
    private static final Debug debug = Debug.getInstance("certpath");
    private AdjacencyList adjList;
    SunCertPathBuilderResult(CertPath certPath,
        TrustAnchor trustAnchor, PolicyNode policyTree,
        PublicKey subjectPublicKey, AdjacencyList adjList)
    {
        super(certPath, trustAnchor, policyTree, subjectPublicKey);
        this.adjList = adjList;
    }
    public AdjacencyList getAdjacencyList() {
        return adjList;
    }
}
