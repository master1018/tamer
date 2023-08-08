public class ReturnPointEffect extends EffectTemplate {
    @Override
    public void applyEffect(Effect effect) {
        ItemTemplate itemTemplate = effect.getItemTemplate();
        int worldId = itemTemplate.getReturnWorldId();
        String pointAlias = itemTemplate.getReturnAlias();
        TeleportService.teleportToPortalExit(((Player) effect.getEffector()), pointAlias, worldId, 500);
    }
    @Override
    public void calculate(Effect effect) {
        ItemTemplate itemTemplate = effect.getItemTemplate();
        if (itemTemplate != null) effect.addSucessEffect(this);
    }
}
