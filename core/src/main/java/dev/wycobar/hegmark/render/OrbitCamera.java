package dev.wycobar.hegmark.render;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.MathUtils;
import dev.wycobar.hegmark.editor.CameraStepPolicy;

public final class OrbitCamera {
    private final PerspectiveCamera camera = new PerspectiveCamera(50.0f, 1.0f, 1.0f);
    private final CameraStepPolicy stepPolicy = new CameraStepPolicy();
    private float yawDegrees = 35.0f;
    private float pitchDegrees = 20.0f;
    private float distance = 2.8f;
    private boolean dirty = true;

    public OrbitCamera() {
        camera.near = 0.05f;
        camera.far = 20.0f;
        update(1, 1);
    }

    public PerspectiveCamera camera() {
        return camera;
    }

    public float distance() {
        return distance;
    }

    public void orbit(float deltaX, float deltaY) {
        yawDegrees -= deltaX * 0.35f;
        pitchDegrees = MathUtils.clamp(pitchDegrees + deltaY * 0.3f, -85.0f, 85.0f);
        dirty = true;
    }

    public void zoom(float amount) {
        float surfaceGap = distance - 1.0f;
        surfaceGap *= Math.max(0.1f, 1.0f + amount * 0.15f);
        distance = 1.0f + MathUtils.clamp(surfaceGap, 0.000001f, 5.0f);
        dirty = true;
    }

    public void rotateLongitude(int direction) {
        if (direction == 0) return;
        yawDegrees += Math.signum(direction) * rotationStepDegrees();
        dirty = true;
    }

    public void rotateLatitude(int direction) {
        if (direction == 0) return;
        pitchDegrees = MathUtils.clamp(
            pitchDegrees + Math.signum(direction) * rotationStepDegrees(),
            -85.0f,
            85.0f
        );
        dirty = true;
    }

    private float rotationStepDegrees() {
        return stepPolicy.longitudeDegrees(distance - 1.0f);
    }

    public boolean update(int viewportWidth, int viewportHeight) {
        if (!dirty && camera.viewportWidth == viewportWidth && camera.viewportHeight == viewportHeight) return false;
        float yaw = yawDegrees * MathUtils.degreesToRadians;
        float pitch = pitchDegrees * MathUtils.degreesToRadians;
        float horizontal = MathUtils.cos(pitch) * distance;
        camera.position.set(
            MathUtils.cos(yaw) * horizontal,
            MathUtils.sin(pitch) * distance,
            MathUtils.sin(yaw) * horizontal
        );
        camera.up.set(0.0f, 1.0f, 0.0f);
        camera.lookAt(0.0f, 0.0f, 0.0f);
        camera.viewportWidth = Math.max(1, viewportWidth);
        camera.viewportHeight = Math.max(1, viewportHeight);
        camera.near = Math.max(0.0000001f, (distance - 1.0f) * 0.05f);
        camera.update();
        dirty = false;
        return true;
    }
}
