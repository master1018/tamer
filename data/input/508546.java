public class FloatUtils {
    private static final float ANIMATION_SPEED = 4.0f;
    public static final float animate(float prevVal, float targetVal, float timeElapsed) {
        timeElapsed = timeElapsed * ANIMATION_SPEED;
        return animateAfterFactoringSpeed(prevVal, targetVal, timeElapsed);
    }
    public static final float animateWithMaxSpeed(float prevVal, float targetVal, float timeElapsed, float maxSpeed) {
        float newTargetVal = targetVal;
        float delta = newTargetVal - prevVal;
        if (Math.abs(delta) > maxSpeed) {
            newTargetVal = prevVal + (Math.signum(delta) * maxSpeed);
        }
        timeElapsed = timeElapsed * ANIMATION_SPEED;
        return animateAfterFactoringSpeed(prevVal, newTargetVal, timeElapsed);
    }
    public static final void animate(Vector3f animVal, Vector3f targetVal, float timeElapsed) {
        timeElapsed = timeElapsed * ANIMATION_SPEED;
        animVal.x = animateAfterFactoringSpeed(animVal.x, targetVal.x, timeElapsed);
        animVal.y = animateAfterFactoringSpeed(animVal.y, targetVal.y, timeElapsed);
        animVal.z = animateAfterFactoringSpeed(animVal.z, targetVal.z, timeElapsed);
    }
    public static final float clampMin(float val, float minVal) {
        if (val < minVal)
            return minVal; 
        else
            return val;
    }
    public static final float clampMax(float val, float maxVal) {
        if (val > maxVal)
            return maxVal;
        else
            return val;
    }
    public static final float clamp(float val, float minVal, float maxVal) {
        if (val < minVal)
            return minVal;
        else if (val > maxVal)
            return maxVal;
        else
            return val;
    }
    public static final int clamp(int val, int minVal, int maxVal) {
        if (val < minVal)
            return minVal;
        else if (val > maxVal)
            return maxVal;
        else
            return val;
    }
    public static final boolean boundsContainsPoint(float left, float right, float top, float bottom, float posX, float posY) {
        if (posX < left || posX > right || posY < top || posY > bottom)
            return false;
        else
            return true;
    }
    private static final float animateAfterFactoringSpeed(float prevVal, float targetVal, float timeElapsed) {
        if (prevVal == targetVal)
            return targetVal;
        float newVal = prevVal + ((targetVal - prevVal) * timeElapsed);
        if (Math.abs(newVal - prevVal) < 0.0001f)
            return targetVal;
        if (newVal == prevVal) {
            return targetVal;
        } else { 
            if (prevVal > targetVal && newVal < targetVal) {
                return targetVal;
            } else if (prevVal < targetVal && newVal > targetVal) {
                return targetVal;
            } else {
                return newVal;
            }
        }
    }
    public static final float max(float scaleX, float scaleY) {
        if (scaleX > scaleY)
            return scaleX;
        else
            return scaleY;
    }
}
