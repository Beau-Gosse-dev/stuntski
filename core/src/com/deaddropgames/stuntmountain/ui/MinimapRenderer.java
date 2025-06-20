package com.deaddropgames.stuntmountain.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.deaddropgames.stuntmountain.level.Level;
import com.deaddropgames.stuntmountain.level.Point;
import com.deaddropgames.stuntmountain.level.PolyLine;
import com.deaddropgames.stuntmountain.level.Tree;
import com.deaddropgames.stuntmountain.sim.SimpleBiped;

import java.util.List;

public class MinimapRenderer implements Disposable {
    private static final int MINIMAP_WIDTH = 300;
    private static final int MINIMAP_HEIGHT = 100;
    private static final float MINIMAP_PADDING = 20f;
    private static final float WORLD_VIEW_WIDTH = 200f; // How many meters of world to show
    private static final float MINIMAP_ALPHA = 0.7f;
    
    private final FrameBuffer frameBuffer;
    private final OrthographicCamera minimapCamera;
    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final GlyphLayout glyphLayout;
    
    private float minimapX;
    private float minimapY;
    
    public MinimapRenderer() {
        frameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, MINIMAP_WIDTH, MINIMAP_HEIGHT, false);
        minimapCamera = new OrthographicCamera();
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.setColor(1f, 1f, 1f, 0.9f); // White with slight transparency
        glyphLayout = new GlyphLayout();
        
        updatePosition();
    }
    
    private void updatePosition() {
        minimapX = Gdx.graphics.getWidth() - MINIMAP_WIDTH - MINIMAP_PADDING;
        minimapY = Gdx.graphics.getHeight() - MINIMAP_HEIGHT - MINIMAP_PADDING;
    }
    
    public void render(Level level, SimpleBiped skier, OrthographicCamera worldCamera) {
        Vector2 skierPos = skier.getUpperBody().getWorldCenter();
        
        // Update minimap position on screen resize
        updatePosition();
        
        // Set up minimap camera to show area ahead of player
        minimapCamera.setToOrtho(false, WORLD_VIEW_WIDTH, WORLD_VIEW_WIDTH * MINIMAP_HEIGHT / MINIMAP_WIDTH);
        // Position camera ahead of skier to show upcoming terrain
        float lookAhead = WORLD_VIEW_WIDTH * 0.3f; // Show 30% behind, 70% ahead
        minimapCamera.position.set(skierPos.x + lookAhead, skierPos.y, 0);
        minimapCamera.update();
        
        // Render to framebuffer
        frameBuffer.begin();
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1f); // Dark blue background
        Gdx.gl.glClear(Gdx.gl.GL_COLOR_BUFFER_BIT);
        
        shapeRenderer.setProjectionMatrix(minimapCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Draw terrain
        drawTerrain(level, skierPos);
        
        // Draw trees
        drawTrees(level, skierPos);
        
        // Draw finish line
        drawFinishLine(level, skierPos);
        
        // Draw player
        drawPlayer(skierPos);
        
        shapeRenderer.end();
        frameBuffer.end();
        
        // Draw framebuffer to screen with transparency
        batch.begin();
        batch.setColor(1, 1, 1, MINIMAP_ALPHA);
        Texture minimapTexture = frameBuffer.getColorBufferTexture();
        batch.draw(minimapTexture, minimapX, minimapY, MINIMAP_WIDTH, MINIMAP_HEIGHT, 
                   0, 0, 1, 1); // Flip Y coordinate
        batch.setColor(1, 1, 1, 1); // Reset color
        batch.end();
        
        // Draw border
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.3f, 0.3f, 0.3f, 0.8f);
        shapeRenderer.rect(minimapX, minimapY, MINIMAP_WIDTH, MINIMAP_HEIGHT);
        shapeRenderer.end();
        
        // Draw map name
        if (level.name != null && !level.name.isEmpty()) {
            batch.begin();
            glyphLayout.setText(font, level.name);
            float textX = minimapX + (MINIMAP_WIDTH - glyphLayout.width) / 2f;
            float textY = minimapY + MINIMAP_HEIGHT - 5f;
            font.draw(batch, level.name, textX, textY);
            batch.end();
        }
    }
    
    private void drawTerrain(Level level, Vector2 centerPos) {
        shapeRenderer.setColor(0.8f, 0.8f, 0.9f, 1f); // Light gray/white for snow
        
        if (level.polyLines == null) return;
        
        for (PolyLine polyLine : level.polyLines) {
            if (polyLine.points == null || polyLine.points.length < 2) continue;
            
            // Draw filled terrain below the line
            for (int i = 0; i < polyLine.points.length - 1; i++) {
                Point p1 = polyLine.points[i];
                Point p2 = polyLine.points[i + 1];
                
                // Only draw if within view range
                if (Math.abs(p1.x - centerPos.x) < WORLD_VIEW_WIDTH &&
                    Math.abs(p2.x - centerPos.x) < WORLD_VIEW_WIDTH) {
                    
                    // Draw filled quad from line down to bottom
                    float bottomY = centerPos.y - WORLD_VIEW_WIDTH/2;
                    shapeRenderer.triangle(p1.x, p1.y, p2.x, p2.y, p1.x, bottomY);
                    shapeRenderer.triangle(p2.x, p2.y, p1.x, bottomY, p2.x, bottomY);
                }
            }
        }
    }
    
    private void drawTrees(Level level, Vector2 centerPos) {
        shapeRenderer.setColor(0.2f, 0.5f, 0.2f, 1f); // Dark green for trees
        
        if (level.trees == null) return;
        
        for (Tree tree : level.trees) {
            if (Math.abs(tree.location.x - centerPos.x) < WORLD_VIEW_WIDTH) {
                // Draw tree as small triangle
                float x = tree.location.x;
                float y = tree.location.y;
                float size = 2f; // Small triangle size
                
                shapeRenderer.triangle(x - size/2, y, x + size/2, y, x, y + size * 1.5f);
            }
        }
    }
    
    private void drawFinishLine(Level level, Vector2 centerPos) {
        if (level.endX <= 0f) return;
        
        if (Math.abs(level.endX - centerPos.x) < WORLD_VIEW_WIDTH) {
            // Draw vertical line at finish position
            float lineWidth = 3f;
            float lineHeight = 80f; // Extra tall so always visible
            float segmentHeight = 5f; // Height of each black/white segment
            
            // Find ground level by checking terrain at finish x position
            float groundY = 0f;
            if (level.polyLines != null && level.polyLines.length > 0) {
                // Just use the first polyline for simplicity
                PolyLine polyLine = level.polyLines[0];
                if (polyLine.points != null && polyLine.points.length > 0) {
                    // Find the closest point
                    for (Point p : polyLine.points) {
                        if (p.x <= level.endX) {
                            groundY = p.y;
                        }
                    }
                }
            }
            
            // Draw alternating black and white segments
            int numSegments = (int)(lineHeight / segmentHeight);
            for (int i = 0; i < numSegments; i++) {
                if (i % 2 == 0) {
                    shapeRenderer.setColor(1f, 1f, 1f, 1f); // White
                } else {
                    shapeRenderer.setColor(0f, 0f, 0f, 1f); // Black
                }
                shapeRenderer.rect(level.endX - lineWidth/2, 
                                 groundY + i * segmentHeight, 
                                 lineWidth, segmentHeight);
            }
        }
    }
    
    private void drawPlayer(Vector2 skierPos) {
        shapeRenderer.setColor(1f, 0f, 0f, 1f); // Red for player
        
        // Draw player as arrow pointing in movement direction
        float size = 3f;
        shapeRenderer.circle(skierPos.x, skierPos.y, size);
    }
    
    @Override
    public void dispose() {
        frameBuffer.dispose();
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
    }
}