public class StackBattleStub {
    private int shieldDP;
    private int armour512ths = 512;
    private int shipCount;
    private GridSquare gridSquare;
    private List vValidTargets;
    private DesignBattleStub design;
    private BattleOrders battleOrders;
    private boolean canRegenShields;
    private boolean takenArmourDamage = false;
    private RaceInterface ownerRace;
    public StackBattleStub(int armour512ths, int shipCount, boolean canRegenShields, RaceInterface ownerRace, DesignBattleStub design, BattleOrders battleOrders) {
        this.armour512ths = armour512ths;
        this.shipCount = shipCount;
        this.canRegenShields = canRegenShields;
        this.ownerRace = ownerRace;
        this.design = design;
        this.battleOrders = battleOrders;
        this.shieldDP = getShieldMaxDP();
    }
    public void InitalizeValidTarget() {
    }
    public void regenShields() {
        if (this.canRegenShields && this.takenArmourDamage == false) {
            this.shieldDP = Math.max(this.getShieldMaxDP(), this.getShieldDP() + (this.getShieldMaxDP() / 10));
        }
    }
    public boolean wouldKill(Shot shot) {
        return false;
    }
    public void applyDamage(Shot shot) {
    }
    public void move(GridSquare to) {
        if (to == null) {
            Log.error(this, "StackBattleStub.move() moving to null square");
            return;
        }
        if (this.gridSquare == null) this.gridSquare = to;
        if (to.getFleets().contains(this) == true) {
            this.gridSquare = to;
        } else {
            this.gridSquare.moveStack(this, to);
        }
    }
    public int getArmourDP() {
        return design.getArmour() * this.shipCount * this.armour512ths / 512;
    }
    public int getArmourMaxDP() {
        return design.getArmour() * this.shipCount;
    }
    public int getShieldDP() {
        return shieldDP;
    }
    public int getShieldMaxDP() {
        return design.getShields() * this.shipCount;
    }
    public boolean hasTakenArmourDamage() {
        return takenArmourDamage;
    }
    public RaceInterface getOwnerRace() {
        return ownerRace;
    }
    public int getShipCount() {
        return shipCount;
    }
    public List getValidTargets() {
        return vValidTargets;
    }
    public GridSquare getGridSquare() {
        return this.gridSquare;
    }
    public BattleOrders getBattleOrders() {
        return this.battleOrders;
    }
    public boolean canCanRegenShields() {
        return this.canRegenShields;
    }
    public DesignBattleStub getDesign() {
        return this.design;
    }
}
