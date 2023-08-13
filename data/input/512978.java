public class DexFactory implements IApiLoader {
    public IApi loadApi(String name, Visibility visibility,
            Set<String> fileNames, Set<String> packageNames) throws
            IOException {
        DexToSigConverter converter = new DexToSigConverter();
        Set<DexFile> files = DexUtil.getDexFiles(fileNames);
        SigApi api = converter.convertApi(name, files, visibility);
        Iterator<IPackage> it = api.getPackages().iterator();
        while (it.hasNext()) {
            IPackage aPackage = it.next();
            boolean found = false;
            for (String packageName : packageNames) {
                if (aPackage.getName().equals(packageName)) {
                    found = true;
                }
            }
            if (!found) {
                it.remove();
            }
        }
        return api;
    }
}
