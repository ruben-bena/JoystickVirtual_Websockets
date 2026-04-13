package com.rubenbellido.joystickvirtual_websockets;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

public class Main implements ApplicationListener {

    // Animaciones y sprites
    private static final int FRAME_COLS = 12, FRAME_ROWS = 1;
    Animation<TextureRegion> idleAnimation;
    Animation<TextureRegion> walkAnimation;
    Animation<TextureRegion> backAnimation;
    Texture spritesheet;
    SpriteBatch spriteBatch;
    TextureRegion currentFrame;

    // Control de movimiento
    float x = 0;
    float y = 0;
    float speed = 300;
    float dirX = 0;
    float dirY = 0;
    boolean isFacingLeftDirection = true;

    // A variable for tracking elapsed time for the animation
    float stateTime;

    Rectangle up, down, left, right, fire;
    final int IDLE=0, UP=1, DOWN=2, LEFT=3, RIGHT=4;
    ShapeRenderer shapeRenderer;

    @Override
    public void create() {

        // Load the sprite sheet as a Texture
        spritesheet = new Texture(Gdx.files.internal("spritesheet_ruben_fixed.png"));

        // Use the split utility method to create a 2D array of TextureRegions. This is
        // possible because this sprite sheet contains frames of equal size and they are
        // all aligned.
        TextureRegion[][] tmp = TextureRegion.split(spritesheet,
            spritesheet.getWidth() / FRAME_COLS,
            spritesheet.getHeight() / FRAME_ROWS);

        TextureRegion[] idleFrames = new TextureRegion[3];
        TextureRegion[] walkFrames = new TextureRegion[6];
        TextureRegion[] backFrames = new TextureRegion[3];
        int index = 0;
        for (int i = 0; i < FRAME_ROWS; i++) {
            for (int j = 0; j < FRAME_COLS; j++) {
                if (j<=2) {
                    idleFrames[index] = tmp[i][j];
                }
                else if (j<=8) {
                    walkFrames[index-3] = tmp[i][j];
                }
                else {
                    backFrames[index-9] = tmp[i][j];
                }
                index++;
            }
        }

        // Initialize the Animation with the frame interval and array of frames
        idleAnimation = new Animation<TextureRegion>(0.1f, idleFrames);
        walkAnimation = new Animation<TextureRegion>(0.1f, walkFrames);
        backAnimation = new Animation<TextureRegion>(0.1f, backFrames);

        // Instantiate a SpriteBatch for drawing and reset the elapsed animation
        // time to 0
        spriteBatch = new SpriteBatch();
        stateTime = 0f;

        // facilities per calcular el "touch"
        float width = Gdx.graphics.getWidth();
        float height = Gdx.graphics.getHeight();
        up = new Rectangle(0, height*2/3, width, height/3);
        down = new Rectangle(0, 0, width, height/3);
        left = new Rectangle(0, 0, width/3, height);
        right = new Rectangle(width*2/3, 0, width/3, height);
        shapeRenderer = new ShapeRenderer();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void render() {
        input();
        logic();
        draw();
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() { // SpriteBatches and Textures must always be disposed
        spriteBatch.dispose();
        spritesheet.dispose();
    }

    public void input() {
        // Gestión dirX
        if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.LEFT)) {
            if (!isFacingLeftDirection) {
                isFacingLeftDirection = true;
            }
            dirX = -1;
        }
        else if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.RIGHT)) {
            if (isFacingLeftDirection) {
                isFacingLeftDirection = false;
            }
            dirX = 1;
        }
        else {
            dirX = 0;
        }

        // Gestión dirY
        if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.UP)) {
            dirY = 1;
        }
        else if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.DOWN)) {
            dirY = -1;
        }
        else {
            dirY = 0;
        }
    }

    public void logic() {
        // Obtenemos el tiempo que ha pasado desde el último frame
        float deltaTime = Gdx.graphics.getDeltaTime();

        // Actualizamos la posición: Posición + (Dirección * Velocidad * Tiempo)
        x += dirX * speed * deltaTime;
        y += dirY * speed * deltaTime;

        // Decidimos animación en función de cómo nos movemos
        if (dirX == 0 && dirY == 0) { // Si estamos quietos...
            currentFrame = idleAnimation.getKeyFrame(stateTime, true);
        }
        else if (dirY == 1) { // Si vamos hacia arriba...
            currentFrame = backAnimation.getKeyFrame(stateTime, true);
        }
        else {
            currentFrame = walkAnimation.getKeyFrame(stateTime, true);
        }

        // Giramos sprite si hace falta
        // --> El sprite por defecto mira a la izquierda.
        // --> Si estamos mirando a la izquierda, NO deberíamos estar girados.
        if (!isFacingLeftDirection && !currentFrame.isFlipX()) {
            currentFrame.flip(true, false);
        }
        else if (isFacingLeftDirection && currentFrame.isFlipX()) {
            currentFrame.flip(true, false);
        }
    }

    public void draw() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); // Clear screen
        stateTime += Gdx.graphics.getDeltaTime(); // Accumulate elapsed animation time

        drawRects();

        spriteBatch.begin();
        spriteBatch.draw(currentFrame, x, y);
        spriteBatch.end();
    }

    private void drawRects() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(1, 0, 0, 0.1f);
        shapeRenderer.rect(up.x, up.y, up.width, up.height);
        shapeRenderer.setColor(0, 1, 0, 0.1f);
        shapeRenderer.rect(down.x, down.y, down.width, down.height);
        shapeRenderer.setColor(0, 0, 1, 0.1f);
        shapeRenderer.rect(left.x, left.y, left.width, left.height);
        shapeRenderer.setColor(1, 1, 0, 0.1f);
        shapeRenderer.rect(right.x, right.y, right.width, right.height);
        shapeRenderer.end();
    }
}
