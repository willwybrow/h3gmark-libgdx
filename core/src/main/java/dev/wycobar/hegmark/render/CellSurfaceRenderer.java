package dev.wycobar.hegmark.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.math.Matrix4;
import dev.wycobar.hegmark.feature.elevation.ElevationFeature;
import dev.wycobar.hegmark.feature.elevation.ElevationStyle;
import dev.wycobar.hegmark.planet.CellId;
import dev.wycobar.hegmark.planet.PlanetGrid;
import dev.wycobar.hegmark.planet.PlanetModel;
import dev.wycobar.hegmark.planet.Planet;
import dev.wycobar.hegmark.planet.CartesianPoint;

import java.util.List;

public final class CellSurfaceRenderer implements Disposable {
    private static final String VERTEX_SHADER = """
        attribute vec3 a_position;
        attribute vec4 a_color;
        uniform mat4 u_projTrans;
        varying vec4 v_color;
        void main() {
            v_color = a_color;
            gl_Position = u_projTrans * vec4(a_position, 1.0);
        }
        """;
    private static final String FRAGMENT_SHADER = """
        #ifdef GL_ES
        precision mediump float;
        #endif
        varying vec4 v_color;
        void main() {
            gl_FragColor = v_color;
        }
        """;

    private final CellSurfaceMeshBuilder meshBuilder;
    private final ShaderProgram shader;
    private Mesh fillMesh;
    private Mesh lineMesh;
    private int cellCount;
    private CartesianPoint meshOrigin = new CartesianPoint(0.0, 0.0, 0.0);
    private final Matrix4 localProjectionView = new Matrix4();

    public CellSurfaceRenderer(
        Planet world,
        ElevationFeature elevationFeature,
        ElevationStyle style
    ) {
        meshBuilder = new CellSurfaceMeshBuilder(world, elevationFeature, style);
        ShaderProgram.pedantic = false;
        shader = new ShaderProgram(VERTEX_SHADER, FRAGMENT_SHADER);
        if (!shader.isCompiled()) throw new IllegalStateException("Cell shader failed to compile: " + shader.getLog());
    }

    public void rebuild(List<CellId> cells, CellId selectedCell) {
        SurfaceMeshData data = meshBuilder.build(cells, selectedCell);
        replaceMeshes(data.fillVertices(), data.lineVertices());
        cellCount = data.cellCount();
        meshOrigin = data.origin();
    }

    public int cellCount() {
        return cellCount;
    }

    public void render(Camera camera) {
        if (fillMesh == null) return;
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glDepthFunc(GL20.GL_LEQUAL);
        Gdx.gl.glEnable(GL20.GL_CULL_FACE);
        Gdx.gl.glCullFace(GL20.GL_BACK);
        shader.bind();
        localProjectionView.set(camera.combined).translate(
            (float) meshOrigin.x(),
            (float) meshOrigin.y(),
            (float) meshOrigin.z()
        );
        shader.setUniformMatrix("u_projTrans", localProjectionView);
        fillMesh.render(shader, GL20.GL_TRIANGLES);
        Gdx.gl.glDisable(GL20.GL_CULL_FACE);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        Gdx.gl.glLineWidth(1.0f);
        lineMesh.render(shader, GL20.GL_LINES);
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void replaceMeshes(float[] fills, float[] lines) {
        if (fillMesh != null) fillMesh.dispose();
        if (lineMesh != null) lineMesh.dispose();
        fillMesh = mesh(fills);
        lineMesh = mesh(lines);
    }

    private Mesh mesh(float[] vertices) {
        Mesh mesh = new Mesh(
            true,
            vertices.length / 4,
            0,
            new VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
            new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, ShaderProgram.COLOR_ATTRIBUTE)
        );
        mesh.setVertices(vertices);
        return mesh;
    }

    @Override
    public void dispose() {
        if (fillMesh != null) fillMesh.dispose();
        if (lineMesh != null) lineMesh.dispose();
        shader.dispose();
    }
}
