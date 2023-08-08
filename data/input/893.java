public class WizardTimeSeriesAddRepositoryFolder extends Wizard implements IWorkbenchWizard {
    private PageSetFolderName m_pageFolderName;
    private final ZmlObservationRepository m_repos;
    private final File m_base;
    public WizardTimeSeriesAddRepositoryFolder(final ZmlObservationRepository repos, final File file) {
        m_repos = repos;
        m_base = file;
        setHelpAvailable(false);
    }
    @Override
    public void addPages() {
        setWindowTitle(Messages.WizardTimeSeriesAddRepositoryFolder_0);
        m_pageFolderName = new PageSetFolderName(m_base);
        addPage(m_pageFolderName);
    }
    public void init(final IWorkbench workbench, final IStructuredSelection selection) {
    }
    @Override
    public boolean performFinish() {
        final File file = new File(m_base, m_pageFolderName.m_tName.getText());
        try {
            m_repos.makeItem(file);
        } catch (final IOException e) {
            NofdpCorePlugin.getDefault().getLog().log(StatusUtilities.statusFromThrowable(e));
            return false;
        }
        return true;
    }
}
