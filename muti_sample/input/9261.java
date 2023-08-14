    public abstract void addClassesSummary(ClassDoc[] classes, String label,
            String tableSummary, String[] tableHeader, Content summaryContentTree);
    public abstract void addPackageDescription(Content packageContentTree);
    public abstract void addPackageTags(Content packageContentTree);
    public abstract void addPackageFooter(Content contentTree);
    public abstract void printDocument(Content contentTree);
    public abstract void close() throws IOException;
}
