package owo.pigeon.utils.animation;

public class AnimationValue {
    private float current;
    private float target;
    private float durationSeconds;
    private long startTime;
    private float startValue;
    private boolean animating;

    public AnimationValue(float initial, float durationSeconds) {
        this.current = initial;
        this.target = initial;
        this.durationSeconds = durationSeconds;
    }

    public void update(float delta) {
        if (!animating) return;

        long now = System.nanoTime();
        float elapsed = (now - startTime) / 1_000_000_000.0f;
        float progress = Math.min(elapsed / durationSeconds, 1.0f);

        current = startValue + (target - startValue) * easeInOutCubic(progress);

        if (progress >= 1.0f) {
            current = target;
            animating = false;
        }
    }

    public void setTarget(float target) {
        if (this.target == target) return;
        this.startValue = this.current;
        this.target = target;
        this.startTime = System.nanoTime();
        this.animating = true;
    }

    public void setDuration(float durationSeconds) {
        this.durationSeconds = Math.max(durationSeconds, 0.001f);
    }

    public void force(float value) {
        this.current = value;
        this.target = value;
        this.animating = false;
    }

    public float getValue() {
        return current;
    }

    public float getTarget() {
        return target;
    }

    public boolean isDone() {
        return !animating;
    }

    public boolean isCollapsed() {
        return current <= 0.0f && !animating;
    }

    public boolean isExpanded() {
        return target > 0.5f;
    }

    private static float easeInOutCubic(float t) {
        if (t < 0.5f) {
            return 4.0f * t * t * t;
        } else {
            return 1.0f - (float) Math.pow(-2.0f * t + 2.0f, 3.0f) / 2.0f;
        }
    }
}
