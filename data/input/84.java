public class SoftBody {
    public Vector tmp_massPoints;
    public Vector tmp_springs;
    public Vector tmp_objects;
    public Spring[] springs;
    public MassPoint[] points;
    public MassPoint[] drawPoints;
    public MassPoint centerPoint;
    public int gasPressure;
    private Vector2 centerPos, groundDir, fixedCenterPos;
    public static final int MASK_WAS_COLLIDING = 1 << 0, MASK_WAS_COLLIDING_WITH_WORLD = 1 << 1, MASK_WAS_COLLIDING_WITH_STATICS = 1 << 2, MASK_WAS_COLLIDING_WITH_MOVABLES = 1 << 3;
    public int collInfo;
    public int friction;
    public static final int MASK_IS_STICKY = 1 << 0, MASK_IS_RIGID_AND_CONVEX = 1 << 1, MASK_PRESERVE_VOLUME = 1 << 2, MASK_CAN_STICK = 1 << 3, MASK_VISIBLE = 1 << 4, MASK_DEAD = 1 << 5, MASK_DRAW_SMOOTH = 1 << 6, MASK_CENTER_FIXED = 1 << 7;
    public int properties;
    public int initialVolume;
    public int initialPerimeter;
    private int[] aabb = new int[4];
    private boolean aabbInvalid = true;
    public Spring[] stickySprings;
    public int stickySprings_len;
    public int springsIterations;
    public Spring[] externalSprings;
    public MassPoint[] externalMassPoints;
    public SoftBody[] neighbours;
    public int _id;
    public int userType = 0;
    public Object userData;
    public SoftBody(int pGasPressure, boolean pPreserveVolume, boolean pCanStick, boolean pFixedCenter, boolean pDrawSmooth) {
        tmp_massPoints = new Vector();
        tmp_springs = new Vector();
        tmp_objects = new Vector();
        gasPressure = pGasPressure;
        properties = 0;
        if (pPreserveVolume) properties |= MASK_PRESERVE_VOLUME;
        if (pCanStick) properties |= MASK_CAN_STICK;
        if (pDrawSmooth) properties |= MASK_DRAW_SMOOTH;
        if (pFixedCenter) properties |= MASK_CENTER_FIXED;
        friction = MathUtils.SHIFT_KOEF;
        centerPoint = null;
        springsIterations = 1;
    }
    public void setRigidAndConvex(boolean pNew) {
        if (pNew) properties |= MASK_IS_RIGID_AND_CONVEX; else properties &= ~MASK_IS_RIGID_AND_CONVEX;
    }
    public void setSticky(boolean pNew) {
        if ((properties & MASK_CAN_STICK) == 0) System.out.println("Can't stick nonsticky body!");
        if (pNew) properties |= MASK_IS_STICKY; else properties &= ~MASK_IS_STICKY;
    }
    public int[] getAABB() {
        if (aabbInvalid) {
            aabb[0] = aabb[1] = Integer.MAX_VALUE;
            aabb[2] = aabb[3] = Integer.MIN_VALUE;
            int len = points.length;
            for (int i = 0; i < len; i++) {
                Vector2 pt = points[i].pos;
                if (pt.x < aabb[0]) aabb[0] = pt.x;
                if (pt.x > aabb[2]) aabb[2] = pt.x;
                if (pt.y < aabb[1]) aabb[1] = pt.y;
                if (pt.y > aabb[3]) aabb[3] = pt.y;
            }
            aabbInvalid = false;
        }
        return aabb;
    }
    public void addMassPoint(MassPoint pPoint) {
        tmp_massPoints.addElement(pPoint);
    }
    public void addMassPoints(MassPoint[] pPoints) {
        for (int i = 0; i < pPoints.length; i++) tmp_massPoints.addElement(pPoints[i]);
    }
    public void addMassPoints(MassPoint[] pPoints, Vector2 pOffset) {
        for (int i = 0; i < pPoints.length; i++) {
            pPoints[i].translatePosition(pOffset);
            tmp_massPoints.addElement(pPoints[i]);
        }
    }
    public void addMassPoints(Vector2[] pPoints, Vector2 pOffset, int pMass) {
        for (int i = 0; i < pPoints.length; i++) tmp_massPoints.addElement(new MassPoint(pPoints[i].plus(pOffset), pMass));
    }
    public void addMassPoints(Vector2[] pPoints, int pMass) {
        for (int i = 0; i < pPoints.length; i++) tmp_massPoints.addElement(new MassPoint(pPoints[i], pMass));
    }
    public void setCenterMassPoint(MassPoint pPoint) {
        centerPoint = pPoint;
    }
    public void addSpring(Spring pSpring) {
        tmp_springs.addElement(pSpring);
    }
    public boolean addStickySpring(Spring pSpring) {
        if (stickySprings_len == stickySprings.length) {
            return false;
        }
        stickySprings[stickySprings_len++] = pSpring;
        return true;
    }
    public void addExternalSpring(Spring pSpring) {
        tmp_springs.addElement(pSpring);
    }
    public void addExternalMassPoint(MassPoint pPoint) {
        tmp_massPoints.addElement(pPoint);
    }
    public void addNeighbour(SoftBody pBody) {
        if (pBody != null && !tmp_objects.contains(pBody)) tmp_objects.addElement(pBody);
    }
    public void _preCollisionInit() {
        collInfo = 0;
        int len = points.length;
        for (int k = 0; k < len; k++) points[k].info &= ~(MassPoint.MASK_WAS_COLLIDING | MassPoint.MASK_WAS_COLLIDING_WITH_WORLD | MassPoint.MASK_WAS_COLLIDING_WITH_MOVABLES);
    }
    public void buildShapeSprings(int pK, int pK2, int pBreakLen, int pBreakLen2) {
        int len = tmp_massPoints.size();
        int len2 = len >> 1;
        for (int i = 0; i < len2; i++) {
            Spring spr = new Spring((MassPoint) tmp_massPoints.elementAt(2 * i), (MassPoint) tmp_massPoints.elementAt(2 * i + 1), pK, pBreakLen, -1);
            addSpring(spr);
        }
        for (int i = 0; i < len2; i++) {
            int index2 = 2 * i + 2;
            if (index2 >= len) index2 = 0;
            Spring spr = new Spring((MassPoint) tmp_massPoints.elementAt(2 * i + 1), (MassPoint) tmp_massPoints.elementAt(index2), pK2, pBreakLen2, -1);
            addSpring(spr);
        }
        if (len % 2 != 0) {
            addSpring(new Spring((MassPoint) tmp_massPoints.elementAt(len - 1), (MassPoint) tmp_massPoints.elementAt(0), pK, pBreakLen, -1));
        }
    }
    public void buildStructuralSprings(int pK, int pK2, int pBreakLen, int pBreakLen2) {
        int len = tmp_massPoints.size();
        int len2 = len >> 1;
        for (int i = 0; i < len2; i++) {
            Spring spr = new Spring((MassPoint) tmp_massPoints.elementAt(2 * i), centerPoint, pK, pBreakLen, -1);
            addSpring(spr);
        }
        for (int i = 0; i < len2; i++) {
            Spring spr = new Spring((MassPoint) tmp_massPoints.elementAt(2 * i + 1), centerPoint, pK2, pBreakLen2, -1);
            addSpring(spr);
        }
        if (len % 2 != 0) {
            addSpring(new Spring((MassPoint) tmp_massPoints.elementAt(len - 1), centerPoint, pK, pBreakLen));
        }
    }
    public void buildStructuralSprings2(int pK, int pBreakLen) {
        int len = tmp_massPoints.size();
        int len2 = len >> 1;
        for (int i = 0; i < len2; i++) {
            Spring spr = new Spring((MassPoint) tmp_massPoints.elementAt(i), (MassPoint) tmp_massPoints.elementAt(len2 + i), pK, pBreakLen, -1);
            addSpring(spr);
        }
    }
    public void buildStructuralSprings3(int pK, int pBreakLen) {
        int len = tmp_massPoints.size();
        for (int i = 0; i < len; i++) for (int j = i + 2; j < len; j++) {
            Spring spr = new Spring((MassPoint) tmp_massPoints.elementAt(i), (MassPoint) tmp_massPoints.elementAt(j), pK, pBreakLen, -1);
            addSpring(spr);
        }
    }
    public void buildStructuralSprings4(int pK, int pBreakLen) {
        buildStructuralSprings2(pK / 6, pBreakLen);
        int len = tmp_massPoints.size();
        for (int i = 0, j = len - 1, k = len - 2; i < len; k = j, j = i, i++) {
            Spring spr = new Spring((MassPoint) tmp_massPoints.elementAt(i), (MassPoint) tmp_massPoints.elementAt(k), pK, pBreakLen, -1);
            addSpring(spr);
        }
    }
    public void finishInit() {
        points = new MassPoint[tmp_massPoints.size()];
        for (int i = 0; i < points.length; i++) points[i] = (MassPoint) tmp_massPoints.elementAt(i);
        tmp_massPoints.removeAllElements();
        springs = new Spring[tmp_springs.size()];
        for (int i = 0; i < springs.length; i++) springs[i] = (Spring) tmp_springs.elementAt(i);
        tmp_springs.removeAllElements();
        if ((properties & MASK_PRESERVE_VOLUME) != 0) {
            initialVolume = computeVolume();
            if (initialVolume < 0) initialVolume = -initialVolume;
            initialPerimeter = computePerimeter();
        }
        if ((properties & MASK_CAN_STICK) != 0) {
            stickySprings = new Spring[points.length];
            stickySprings_len = 0;
        }
        if ((properties & MASK_DRAW_SMOOTH) != 0) {
            drawPoints = new MassPoint[points.length];
            for (int i = 0; i < drawPoints.length; i++) drawPoints[i] = new MassPoint(new Vector2(points[i].pos), MathUtils.INFINITY);
        }
        if ((properties & MASK_CENTER_FIXED) != 0) {
            fixedCenterPos = new Vector2();
            for (int i = 0; i < points.length; i++) fixedCenterPos.add(points[i].pos);
            fixedCenterPos.divide(points.length);
        }
    }
    public void finishSpringsInit() {
        int len = tmp_springs.size();
        if (len == 0) externalSprings = null; else {
            externalSprings = new Spring[len];
            for (int i = 0; i < len; i++) externalSprings[i] = (Spring) tmp_springs.elementAt(i);
            tmp_springs.removeAllElements();
        }
        tmp_springs = null;
        len = tmp_massPoints.size();
        if (len == 0) externalMassPoints = null; else {
            externalMassPoints = new MassPoint[len];
            for (int i = 0; i < len; i++) externalMassPoints[i] = (MassPoint) tmp_massPoints.elementAt(i);
            tmp_massPoints.removeAllElements();
        }
        tmp_massPoints = null;
        len = tmp_objects.size();
        if (len == 0) neighbours = null; else {
            neighbours = new SoftBody[len];
            for (int i = 0; i < len; i++) neighbours[i] = (SoftBody) tmp_objects.elementAt(i);
            tmp_objects.removeAllElements();
        }
        tmp_objects = null;
    }
    public MassPoint[] getDrawPoints() {
        if (drawPoints != null) return drawPoints;
        return points;
    }
    public void updateDrawPoints() {
        if (drawPoints == null) return;
        boolean found = false;
        for (int i = 0; i < points.length; i++) {
            int dx = (points[i].pos.x >> MathUtils.SHIFT_COUNT) - (drawPoints[i].pos.x >> MathUtils.SHIFT_COUNT);
            int dy = (points[i].pos.y >> MathUtils.SHIFT_COUNT) - (drawPoints[i].pos.y >> MathUtils.SHIFT_COUNT);
            if (dx < -1 || dx > 1 || dy < -1 || dy > 1) {
                found = true;
                break;
            }
        }
        if (found) {
            for (int i = 0; i < points.length; i++) {
                drawPoints[i].pos.x = points[i].pos.x;
                drawPoints[i].pos.y = points[i].pos.y;
            }
        }
    }
    public boolean isSticked() {
        return stickySprings_len != 0;
    }
    public boolean isStickedToWorld() {
        for (int i = 0; i < stickySprings_len; i++) if (stickySprings[i].p2_pos != null) return true;
        return false;
    }
    public void unstick() {
        for (int i = 0; i < stickySprings_len; i++) {
            stickySprings[i].notifyPointsToUnstick();
            stickySprings[i] = null;
        }
        stickySprings_len = 0;
    }
    public void deleteExternalSprings() {
        if (externalMassPoints != null) for (int i = 0; i < externalMassPoints.length; i++) if (externalMassPoints[i] != null) {
            externalMassPoints[i].info |= MassPoint.MASK_DEAD;
            externalMassPoints[i] = null;
        }
        if (externalSprings != null) for (int i = 0; i < externalSprings.length; i++) if (externalSprings[i] != null) {
            externalSprings[i].die();
            externalSprings[i] = null;
        }
        if (neighbours != null) for (int i = 0; i < neighbours.length; i++) if ((neighbours[i].properties & SoftBody.MASK_DEAD) == 0 && neighbours[i].externalSprings != null) for (int j = 0; j < neighbours[i].externalSprings.length; j++) if (neighbours[i].externalSprings[j] != null) for (int k = 0; k < points.length; k++) if (neighbours[i].externalSprings[j].p1 == points[k] || neighbours[i].externalSprings[j].p2 == points[k]) {
            neighbours[i].externalSprings[j].die();
            neighbours[i].externalSprings[j] = null;
        }
    }
    public void addForceTimesMassToExternalMassPoints(Vector2 pForce) {
        for (int i = 0; i < externalMassPoints.length; i++) if (externalMassPoints[i] != null) externalMassPoints[i].addForceTimesMass(pForce);
    }
    public void _integrate() {
        if (externalMassPoints != null) for (int i = 0; i < externalMassPoints.length; i++) if (externalMassPoints[i] != null) {
            if ((externalMassPoints[i].info & MassPoint.MASK_DEAD) != 0) externalMassPoints[i] = null; else externalMassPoints[i].integrate();
        }
        _invalidateVisualData();
        for (int i = 0; i < points.length; i++) points[i].integrate();
        if (centerPoint != null) centerPoint.integrate();
    }
    public void _applyExternalSprings() {
        for (int i = 0; i < externalSprings.length; i++) {
            if (externalSprings[i] != null && externalSprings[i].apply(true)) {
                externalSprings[i].die();
                externalSprings[i] = null;
            }
        }
    }
    public boolean _applyInternalSprings() {
        boolean cond = (collInfo & MASK_WAS_COLLIDING_WITH_STATICS) != 0 && (collInfo & MASK_WAS_COLLIDING_WITH_MOVABLES) != 0;
        for (int l = 0; l < springsIterations; l++) for (int i = 0; i < springs.length; i++) if (springs[i].apply(cond)) {
            properties |= MASK_DEAD;
            return true;
        }
        if ((properties & MASK_CENTER_FIXED) != 0) {
            int cX = 0, cY = 0;
            for (int i = 0; i < points.length; i++) {
                cX += points[i].pos.x;
                cY += points[i].pos.y;
            }
            cX = fixedCenterPos.x - cX / points.length;
            cY = fixedCenterPos.y - cY / points.length;
            for (int i = 0; i < points.length; i++) {
                points[i].pos.x += cX;
                points[i].pos.y += cY;
            }
        }
        return false;
    }
    public void _applyStickySprings() {
        Spring spr;
        for (int i = 0; i < stickySprings_len; i++) {
            spr = stickySprings[i];
            if (spr.apply(true)) {
                if (i < stickySprings_len - 1) stickySprings[i] = stickySprings[stickySprings_len - 1];
                stickySprings_len--;
                stickySprings[stickySprings_len] = null;
                i--;
                spr.notifyPointsToUnstick();
                spr.p1 = null;
                spr = null;
            }
        }
    }
    public int computeVolume() {
        int volume = 0;
        int len = points.length;
        for (int j = 0, i = len - 1, k = len - 2; j < len; k = i, i = j, j++) volume += (long) points[i].pos.x * (points[j].pos.y - points[k].pos.y) >> MathUtils.SHIFT_COUNT;
        volume >>= 1;
        return volume;
    }
    public int computePerimeter() {
        int res = 0;
        for (int i = 0, j = points.length - 1; i < points.length; j = i, i++) {
            res += points[i].pos.minus(points[j].pos).length();
        }
        return res;
    }
    private void accumulateGasPressureForce() {
        if (gasPressure > 0) {
            int volume = computeVolume();
            if (volume < 0) volume = -volume;
            final int minThreshold = 1 << MathUtils.SHIFT_COUNT * 2 - 5;
            if (volume < minThreshold) {
                volume = minThreshold;
            }
            int len = points.length;
            MassPoint pt_prev;
            MassPoint pt_i;
            for (int i = 0, prevI = len - 1; i < len; prevI = i, i++) {
                pt_prev = points[prevI];
                pt_i = points[i];
                int normal_x = pt_i.pos.y - pt_prev.pos.y;
                int normal_y = pt_prev.pos.x - pt_i.pos.x;
                int df_x = (int) ((long) normal_x * gasPressure / volume);
                int df_y = (int) ((long) normal_y * gasPressure / volume);
                pt_prev.force.x += df_x;
                pt_prev.force.y += df_y;
                pt_i.force.x += df_x;
                pt_i.force.y += df_y;
            }
        }
    }
    public void _preserveVolume() {
        if ((properties & MASK_PRESERVE_VOLUME) == 0) return;
        int volume = computeVolume();
        int A = initialVolume - (volume < 0 ? -volume : volume);
        if (A == 0) return;
        long d = ((long) A << MathUtils.SHIFT_COUNT) / initialPerimeter;
        int normalLen;
        int len = points.length;
        int prev_x = points[len - 1].pos.x;
        int prev_y = points[len - 1].pos.y;
        int first_x = points[0].pos.x;
        int first_y = points[0].pos.y;
        int next_x, next_y;
        for (int i = 0; i < len; i++) {
            if (i == len - 1) {
                next_x = first_x;
                next_y = first_y;
            } else {
                next_x = points[i + 1].pos.x;
                next_y = points[i + 1].pos.y;
            }
            int normal_y = prev_x - next_x;
            int normal_x = next_y - prev_y;
            normalLen = MathUtils.vectorLength(normal_x, normal_y);
            prev_x = points[i].pos.x;
            prev_y = points[i].pos.y;
            if (normalLen != 0) {
                points[i].pos.x += (int) (d * normal_x / normalLen);
                points[i].pos.y += (int) (d * normal_y / normalLen);
            }
        }
    }
    public void accumulateInternalForces() {
        if (gasPressure > 0) accumulateGasPressureForce();
    }
    public void addForce(Vector2 pForce) {
        for (int i = 0; i < points.length; i++) {
            if (points[i].mass != MathUtils.INFINITY) {
                points[i].force.x += pForce.x;
                points[i].force.y += pForce.y;
            }
        }
        if (centerPoint != null && centerPoint.mass != MathUtils.INFINITY) {
            centerPoint.force.x += pForce.x;
            centerPoint.force.y += pForce.y;
        }
    }
    public void addForceToNonColliding(Vector2 pForce) {
        for (int i = 0; i < points.length; i++) {
            MassPoint pt = points[i];
            if ((pt.info & (MassPoint.MASK_WAS_COLLIDING | MassPoint.MASK_IS_STICKED)) == 0) pt.addForce(pForce);
        }
    }
    public void addForceTimesMass(Vector2 pForce) {
        for (int i = 0; i < points.length; i++) points[i].addForceTimesMass(pForce);
        if (centerPoint != null) centerPoint.addForceTimesMass(pForce);
    }
    public boolean containsPoint(Vector2 pP) {
        boolean inside = false;
        for (int i = 0, j = points.length - 1; i < points.length; j = i, i++) {
            Vector2 pt_j = points[j].pos;
            Vector2 pt_i = points[i].pos;
            if ((pt_j.y <= pP.y && pt_i.y > pP.y) || (pt_j.y > pP.y && pt_i.y <= pP.y)) {
                int c1 = (int) ((long) (pt_i.x - pt_j.x) * (pP.y - pt_j.y) >> MathUtils.SHIFT_COUNT);
                int c2 = (int) ((long) (pt_i.y - pt_j.y) * (pP.x - pt_j.x) >> MathUtils.SHIFT_COUNT);
                if ((pt_i.y >= pt_j.y && c1 >= c2) || (pt_i.y < pt_j.y && c1 <= c2)) inside = !inside;
            }
        }
        return inside;
    }
    public void _invalidateVisualData() {
        centerPos = null;
        groundDir = null;
    }
    public void _invalidateAABB() {
        aabbInvalid = true;
    }
    public void _updateAABB(ImplicitGrid pIG) {
        int minX = aabb[0];
        int minY = aabb[1];
        int maxX = aabb[2];
        int maxY = aabb[3];
        aabbInvalid = true;
        getAABB();
        pIG.updateObject(_id, minX, minY, maxX, maxY, aabb[0], aabb[1], aabb[2], aabb[3]);
    }
    public void _clampPointVelocities() {
        int len = points.length;
        Vector2 v = new Vector2();
        for (int i = 0; i < len; i++) {
            MassPoint pt = points[i];
            v.x = pt.pos.x - pt.last_pos.x;
            v.y = pt.pos.y - pt.last_pos.y;
            if ((long) v.x * v.x + (long) v.y * v.y > SoftWorld.MAX_SPEED_SQUARED) {
                v.normaliseWithShift();
                pt.last_pos.x = pt.pos.x - (v.x * SoftWorld.MAX_SPEED >> MathUtils.SHIFT_COUNT);
                pt.last_pos.y = pt.pos.y - (v.y * SoftWorld.MAX_SPEED >> MathUtils.SHIFT_COUNT);
            }
        }
    }
    public Vector2 getCenterPos() {
        if (centerPos == null) {
            centerPos = new Vector2();
            int len = points.length;
            int num = 0;
            for (int i = 0; i < len; i++) {
                MassPoint pt = points[i];
                if ((pt.info & MassPoint.MASK_DEAD) == 0) {
                    centerPos.x += pt.pos.x;
                    centerPos.y += pt.pos.y;
                    num++;
                }
            }
            centerPos.x /= num;
            centerPos.y /= num;
        }
        return centerPos;
    }
    public Vector2 getGroundDir() {
        if (groundDir == null) {
            groundDir = new Vector2();
            int count = 0;
            if ((collInfo & MASK_WAS_COLLIDING_WITH_WORLD) != 0) {
                int len = points.length;
                for (int i = 0; i < len; i++) if ((points[i].info & MassPoint.MASK_WAS_COLLIDING_WITH_WORLD) != 0) {
                    count++;
                    groundDir.add(points[i].pos);
                }
            } else {
                int len = points.length;
                for (int i = 0; i < len; i++) if ((points[i].info & (MassPoint.MASK_WAS_COLLIDING | MassPoint.MASK_IS_STICKED)) != 0) {
                    count++;
                    groundDir.add(points[i].pos);
                }
            }
            if (count == points.length) {
                groundDir.x = 0;
                groundDir.y = MathUtils.SHIFT_KOEF;
            } else if (count != 0) {
                groundDir.divide(count);
                groundDir.subtract(getCenterPos());
            }
        }
        return groundDir;
    }
    public Vector2 getLinearVelocity() {
        Vector2 v = new Vector2();
        int len = points.length;
        for (int i = 0; i < len; i++) {
            MassPoint pt = points[i];
            v.x += pt.pos.x - pt.last_pos.x;
            v.y += pt.pos.y - pt.last_pos.y;
        }
        v.divide(points.length);
        return v;
    }
    public boolean checkConvexity() {
        int len = points.length;
        Vector2 pt_j = points[len - 1].pos;
        Vector2 pt_k = points[len - 2].pos;
        for (int i = 0; i < len; i++) {
            Vector2 pt_i = points[i].pos;
            long edge1_x = pt_j.x - pt_k.x, edge1_y = pt_j.y - pt_k.y;
            long edge2_x = pt_i.x - pt_j.x, edge2_y = pt_i.y - pt_j.y;
            if ((int) (edge1_x * edge2_y - edge1_y * edge2_x >> MathUtils.SHIFT_COUNT) < 0) {
                return false;
            }
            pt_k = pt_j;
            pt_j = pt_i;
        }
        return true;
    }
    public void applyBuyoancy(int pWaterSurfaceY, int pGravity, int pInvDamping) {
        int ratio = (int) (((long) (aabb[3] - pWaterSurfaceY) << MathUtils.SHIFT_COUNT) / (long) (aabb[3] - aabb[1]));
        if (ratio > MathUtils.SHIFT_KOEF) ratio = MathUtils.SHIFT_KOEF;
        if (ratio > 0) {
            int force = (int) ((long) -pGravity * ratio >> MathUtils.SHIFT_COUNT - 1);
            force -= getLinearVelocity().y >> pInvDamping;
            Vector2 toAdd = new Vector2(0, force);
            if (points.length > 4) {
                for (int i = 0; i < points.length; i++) if (points[i].pos.y >= pWaterSurfaceY) points[i].addForceTimesMass(toAdd);
            } else {
                addForceTimesMass(toAdd);
            }
        }
    }
    public void drawShape(Graphics pGraph, Vector2 pOffset, int pEdgesColor) {
        MassPoint[] pts = getDrawPoints();
        int len = pts.length;
        pGraph.setColor(pEdgesColor);
        int oldX = pts[len - 1].pos.x + pOffset.x >> Game.DRAW_SHIFT;
        int oldY = pts[len - 1].pos.y + pOffset.y >> Game.DRAW_SHIFT;
        for (int i = 0; i < len; i++) {
            Vector2 pt_i = pts[i].pos;
            int x = pt_i.x + pOffset.x >> Game.DRAW_SHIFT;
            int y = pt_i.y + pOffset.y >> Game.DRAW_SHIFT;
            pGraph.drawLine(x, y, oldX, oldY);
            oldX = x;
            oldY = y;
        }
    }
    public void d_drawShape(Graphics pGraph, Vector2 pOffset, int pEdgesColor, int pStructSpringsColor) {
        int len = points.length;
        if (pStructSpringsColor != 0) {
            pGraph.setColor(pStructSpringsColor);
            int len2 = springs.length;
            int pos1X, pos1Y, pos2X, pos2Y;
            for (int i = points.length; i < len2; i++) {
                pos1X = pOffset.x + springs[i].p1.pos.x >> Game.DRAW_SHIFT;
                pos1Y = pOffset.y + springs[i].p1.pos.y >> Game.DRAW_SHIFT;
                if (springs[i].p2_pos != null) {
                    pos2X = pOffset.x + springs[i].p2_pos.x >> Game.DRAW_SHIFT;
                    pos2Y = pOffset.y + springs[i].p2_pos.y >> Game.DRAW_SHIFT;
                } else {
                    pos2X = pOffset.x + springs[i].p2.pos.x >> Game.DRAW_SHIFT;
                    pos2Y = pOffset.y + springs[i].p2.pos.y >> Game.DRAW_SHIFT;
                }
                pGraph.drawLine(pos1X, pos1Y, pos2X, pos2Y);
            }
        }
        pGraph.setColor(pEdgesColor);
        for (int i = 0, j = len - 1; i < len; j = i, i++) {
            Vector2 pt_i = points[i].pos;
            Vector2 pt_j = points[j].pos;
            pGraph.drawLine(pt_i.x + pOffset.x >> Game.DRAW_SHIFT, pt_i.y + pOffset.y >> Game.DRAW_SHIFT, pt_j.x + pOffset.x >> Game.DRAW_SHIFT, pt_j.y + pOffset.y >> Game.DRAW_SHIFT);
        }
    }
    public void d_drawVertices(Graphics pGraph, Vector2 pOffset, int pVerticesColor, int pVelocitiesColor) {
        int len = points.length;
        pGraph.setColor(pVelocitiesColor);
        for (int i = 0; i < len; i++) {
            Vector2 pt_i = points[i].last_pos;
            Vector2 pt_j = points[i].pos;
            pGraph.drawLine(pt_i.x + pOffset.x >> Game.DRAW_SHIFT, pt_i.y + pOffset.y >> Game.DRAW_SHIFT, pt_j.x + pOffset.x >> Game.DRAW_SHIFT, pt_j.y + pOffset.y >> Game.DRAW_SHIFT);
        }
        if (pVerticesColor != 0) {
            pGraph.setColor(pVerticesColor);
            for (int i = 0; i < points.length; i++) {
                Vector2 pt_i = points[i].pos;
                pGraph.fillRect(pt_i.x + pOffset.x >> Game.DRAW_SHIFT, pt_i.y + pOffset.y >> Game.DRAW_SHIFT, 1, 1);
            }
            if (centerPoint != null) {
                pGraph.fillRect(centerPoint.pos.x + pOffset.x >> Game.DRAW_SHIFT, centerPoint.pos.y + pOffset.y >> Game.DRAW_SHIFT, 1, 1);
            }
        }
    }
    public void clean() {
        springs = null;
        points = null;
        centerPoint = null;
        centerPos = null;
        groundDir = null;
        stickySprings = null;
        externalMassPoints = null;
        externalSprings = null;
        neighbours = null;
        drawPoints = null;
        fixedCenterPos = null;
    }
}
