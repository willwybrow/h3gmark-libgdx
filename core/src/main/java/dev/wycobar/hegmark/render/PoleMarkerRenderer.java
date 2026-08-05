package dev.wycobar.hegmark.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Disposable;
import dev.wycobar.hegmark.planet.PlanetModel;

public final class PoleMarkerRenderer implements Disposable {
    private static final float INNER_RADIUS_FACTOR = 0.92f;
    private static final float OUTER_RADIUS_FACTOR = 1.18f;

    private final ShapeRenderer shapes = new ShapeRenderer();
    private final float polarRadius;

    public PoleMarkerRenderer(PlanetModel planet) {
        polarRadius = (float) planet.renderPolarRadius();
    }

    public void render(Camera camera) {
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glDepthFunc(GL20.GL_LEQUAL);
        Gdx.gl.glLineWidth(3.0f);
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(Color.RED);
        shapes.line(
            0.0f,
            polarRadius * INNER_RADIUS_FACTOR,
            0.0f,
            0.0f,
            polarRadius * OUTER_RADIUS_FACTOR,
            0.0f
        );
        shapes.setColor(Color.WHITE);
        shapes.line(
            0.0f,
            -polarRadius * INNER_RADIUS_FACTOR,
            0.0f,
            0.0f,
            -polarRadius * OUTER_RADIUS_FACTOR,
            0.0f
        );
        shapes.end();
        Gdx.gl.glLineWidth(1.0f);
    }

    @Override
    public void dispose() {
        shapes.dispose();
    }
}
