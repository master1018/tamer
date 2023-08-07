public class NarcExplosiveHandler extends MissileWeaponHandler {
    private static final long serialVersionUID = -1655014339855184419L;
    public NarcExplosiveHandler(ToHitData t, WeaponAttackAction w, IGame g, Server s) {
        super(t, w, g, s);
        sSalvoType = " explosive pod ";
    }
    @Override
    protected int calcHits(Vector<Report> vPhaseReport) {
        getAMSHitsMod(vPhaseReport);
        if ((target instanceof Infantry) && !(target instanceof BattleArmor)) {
            if (ae instanceof BattleArmor) {
                bSalvo = true;
                return ((BattleArmor) ae).getShootingStrength();
            }
            return 1;
        }
        bSalvo = true;
        if (ae instanceof BattleArmor) {
            if (amsEnganged) {
                return Compute.missilesHit(((BattleArmor) ae).getShootingStrength(), -2);
            }
            return Compute.missilesHit(((BattleArmor) ae).getShootingStrength());
        }
        if (amsEnganged) {
            Report r = new Report(3235);
            r.subject = subjectId;
            vPhaseReport.add(r);
            r = new Report(3230);
            r.indent(1);
            r.subject = subjectId;
            vPhaseReport.add(r);
            int destroyRoll = Compute.d6();
            if (destroyRoll <= 3) {
                r = new Report(3240);
                r.subject = subjectId;
                r.add("pod");
                r.add(destroyRoll);
                vPhaseReport.add(r);
                return 0;
            }
            r = new Report(3241);
            r.add("pod");
            r.add(destroyRoll);
            r.subject = subjectId;
            vPhaseReport.add(r);
        }
        return 1;
    }
    @Override
    protected int calcnCluster() {
        return 1;
    }
    @Override
    protected int calcDamagePerHit() {
        AmmoType atype = (AmmoType) ammo.getType();
        double toReturn;
        if (atype.getAmmoType() == AmmoType.T_INARC) {
            toReturn = 6;
        } else {
            toReturn = 4;
        }
        if ((target instanceof Infantry) && !(target instanceof BattleArmor)) {
            toReturn = Compute.directBlowInfantryDamage(toReturn, bDirect ? toHit.getMoS() / 3 : 0, WeaponType.WEAPON_DIRECT_FIRE, ((Infantry) target).isMechanized());
            toReturn = Math.ceil(toReturn);
        }
        if (bGlancing) {
            return (int) Math.floor(toReturn / 2.0);
        }
        return (int) toReturn;
    }
}
