public class BridgeAssetManager extends AssetManager {
     static AssetManager initSystem() {
        if (!(AssetManager.sSystem instanceof BridgeAssetManager)) {
            AssetManager.sSystem = new BridgeAssetManager();
            AssetManager.sSystem.makeStringBlocks(false);
        }
        return AssetManager.sSystem;
    }
     static void clearSystem() {
        AssetManager.sSystem = null;
    }
    private BridgeAssetManager() {
    }
}
