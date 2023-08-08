public class InstallableImpl<T extends Installable<T>> implements Installable<T> {
    private final List<Installer<T>> installers = new ArrayList<Installer<T>>();
    private final T installable;
    public InstallableImpl(T installable) {
        this.installable = installable;
    }
    public void add(Installer<T> installer) {
        if (installer != null) installers.add(installer);
    }
    public void setup() {
        for (Installer<T> installer : installers) installer.install(installable);
    }
    public void nextPeriod(T nextInstallable) {
        for (Installer<T> installer : installers) nextInstallable.add(installer);
    }
}
