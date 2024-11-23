package com.sanfi.galaxypilot;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

enum GameState {
    HOME_SCREEN,
    PLAYING_GROUND,
    GAME_OVER_SCREEN
}

class GameScreen implements Screen {
    private GameState gameState;

    // Screen setup.
    private final Camera camera;
    private final Viewport viewport;

    // World parameter.
    private final float WORLD_WIDTH = 108;
    private final float WORLD_HEIGHT = 192;

    // Graphics.
    private final SpriteBatch batch;
    private final TextureRegion[] backgrounds;
    private final TextureRegion
            playerShipTextureRegion,
            playerShieldTextureRegion,
            playerLaserTextureRegion,
            shieldTextureRegion,
            powerTextureRegion,
            enemyShipTextureRegion,
            enemyShieldTextureRegion,
            enemyLaserTextureRegion,
            superEnemyShipTextureRegion,
            superEliteEnemyShipTextureRegion,
            livesIconTextureRegion,
            shieldIconTextureRegion;

    private final Texture explosionTexture;
    private static final float SHIELD_SPAWN_INTERVAL = 20f; // in seconds
    private float timeSinceLastShieldSpawn = 0f;
    private static final float POWER_SPAWN_INTERVAL = 25f;
    private float timeSinceLastPowerSpawn = 0f;
    private final float backgroundHeight;
    
    // Timing
    private final float backgroundMaxScrollingSpeed;
    private final float[] backgroundOffsets = {0, 0, 0, 0};
    private float enemySpawnTimer = 0;
    private float superEnemySpawnTimer = 0;
    private float superEliteEnemySpawnTimer = 0;

    //Game objects
    private PlayerShip playerShip;
    private final LinkedList<Shield> shieldList;
    private final LinkedList<Power> powerList;
    private final LinkedList<EnemyShip> enemyShipList;
    private final LinkedList<Laser> playerLaserList, enemyLaserList;
    private final LinkedList<Explosion> explosionList;

    // Heads-Up Display.
    // Game font & display message
    BitmapFont fontH1, fontH2, fontH3;
    float
            hudVerticalMargin,
            hudLeftX,
            hudRightX,
            hudCenterX,
            hudRow1Y,
            hudRow2Y,
            hudSectionWidth,
            hudDisplayRow1Y,
            hudDisplayRow2Y,
            hudDisplayRow3Y,
            buttonRow1Y,
            buttonRow2Y;

    private final Sound gameOpeningSound, playingGroundSound, gameOverSound;
    private boolean isGameOpeningSoundPlaying, isPlayingGroundSoundPlaying, isGameOverSoundPlaying;
    private final Rectangle startTextBounds = new Rectangle();
    private final Rectangle exitTextBounds = new Rectangle();
    private boolean scoreFetched = false;
    private int score = 0;
    private int playerScore;
    private boolean playerDead = false;
    private List<Integer> fetchedScores;
    boolean scoreUpdated;
    boolean scoreCompared = false;
    private FirebaseInterface _FB;
    GameScreen(FirebaseInterface firebaseInterface) {

        _FB = firebaseInterface;

        // Setting up default screen.
        gameState = GameState.HOME_SCREEN;

        // Setting up camera and view port,
        camera = new OrthographicCamera();
        viewport = new StretchViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);

        // Texture Atlas (All image stuff)
        TextureAtlas textureAtlas = new TextureAtlas("images.atlas");

        // Load background image.
        backgrounds = new TextureRegion[4];
        backgrounds[0] = textureAtlas.findRegion("Starscape00");
        backgrounds[1] = textureAtlas.findRegion("Starscape01");
        backgrounds[2] = textureAtlas.findRegion("Starscape02");
        backgrounds[3] = textureAtlas.findRegion("Starscape03");

        backgroundHeight = WORLD_HEIGHT * 2;
        backgroundMaxScrollingSpeed = WORLD_HEIGHT / 4;

        // Initialize player texture regions
        playerShipTextureRegion = textureAtlas.findRegion("playerShip3_green");
        playerShieldTextureRegion = textureAtlas.findRegion("shield2");
        playerLaserTextureRegion = textureAtlas.findRegion("laserGreen14");

        // Initialize enemy texture regions
        enemyShipTextureRegion = textureAtlas.findRegion("enemyRed2");
        enemyShieldTextureRegion = textureAtlas.findRegion("shield1");
        enemyShieldTextureRegion.flip(false, true);
        enemyLaserTextureRegion = textureAtlas.findRegion("laserRed08");

        // Initial super & elite enemy
        superEnemyShipTextureRegion = textureAtlas.findRegion("enemyBlack1");
        superEliteEnemyShipTextureRegion = textureAtlas.findRegion("enemyBlack3");

        // Initial shield
        shieldTextureRegion = textureAtlas.findRegion("powerupGreen_shield");

        // Initial powerUps
        powerTextureRegion = textureAtlas.findRegion("powerupGreen_bolt");

        // Initialize explosion texture regions
        explosionTexture = new Texture("explosion.png");

        // Player lives and shield icon.
        livesIconTextureRegion = textureAtlas.findRegion("playerLife3_green");
        shieldIconTextureRegion = textureAtlas.findRegion("shield_silver");

        // Initialize player ship, enemy ship, laser & explosion
        spawnPlayerShip();

        // Initialize game objects
        enemyShipList = new LinkedList<>();
        playerLaserList = new LinkedList<>();
        enemyLaserList = new LinkedList<>();
        explosionList = new LinkedList<>();
        shieldList = new LinkedList<>();
        powerList = new LinkedList<>();

        // Load fonts & preparing screen.
        batch = new SpriteBatch();
        setUpFonts();
        setUpGameScreen();

        // Initialize sounds.
        gameOpeningSound = Gdx.audio.newSound(Gdx.files.internal("sounds/game_opening.mp3"));
        playingGroundSound = Gdx.audio.newSound(Gdx.files.internal("sounds/playing_ground.mp3"));
        gameOverSound = Gdx.audio.newSound(Gdx.files.internal("sounds/game_over.mp3"));

        // Sound stats
        isGameOpeningSoundPlaying = false;
        isGameOverSoundPlaying = false;
        isPlayingGroundSoundPlaying = false;
    }

    private void fetchScores() {
        _FB.fetchScoresFromDatabase(new FirebaseInterface.ScoresCallback() {
            @Override
            public void onScoresFetched(List<Integer> scores) {
                Collections.sort(scores, Collections.reverseOrder());
                fetchedScores = scores;
                // Print the fetched scores
                for (int i = 0; i < fetchedScores.size(); i++) {
                    System.out.println("Fetched Score " + (i + 1) + ": " + fetchedScores.get(i));
                }
            }
        });
    }

    private void compareScore() {
        if (fetchedScores != null) {
            // Handle the case when fetchedScores is null or not yet initialized
            List<Integer> topScores = fetchedScores.subList(0, Math.min(5, fetchedScores.size()));
            for (Integer score : topScores) {
                if (score < playerScore) {
                    saveScoreToDB(playerScore);
                    break;
                }
            }
            fetchScores();
            System.out.println("Score has been compared");
            scoreCompared = true;
            scoreUpdated = true;
        }
        else System.out.println("uwu");
    }

    private void saveScoreToDB(int playerScore){
        _FB.saveScoreToDatabase(playerScore);
    }

    public void render(float deltaTime) {
        // The projection matrix
        batch.setProjectionMatrix(camera.combined);

        batch.begin();

        // Scrolling background.
        renderBackground(deltaTime);

        // Input
        detectInput(deltaTime);

        // Switch between game state.
        switch (gameState){
            case HOME_SCREEN:
                drawHomeScreenUI();
                break;

            case PLAYING_GROUND:
                drawPlayGroundUI();
                updatePlayGroundUI(deltaTime);
                break;

            case GAME_OVER_SCREEN:
                drawGameOverScreenUI();
                break;
        }
        // Background sound
        playSounds();
        batch.end();
    }

    private void setUpFonts(){
        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/EdgeOfTheGalaxyRegular-OVEa6.otf"));
        FreeTypeFontGenerator.FreeTypeFontParameter fontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();

        fontParameter.size = 200;
        fontParameter.borderWidth = 3.6f;
        fontParameter.color = Color.WHITE;
        fontH1 = fontGenerator.generateFont(fontParameter);

        fontParameter.size = 90;
        fontH2 = fontGenerator.generateFont(fontParameter);

        fontParameter.size = 72;
        fontH3 = fontGenerator.generateFont(fontParameter);

        // Scale fonts
        fontH1.getData().setScale(0.09f);
        fontH2.getData().setScale(0.1f);
        fontH3.getData().setScale(0.08f);

    }

    private void setUpGameScreen() {
        // Calculate margin
        hudVerticalMargin = fontH1.getCapHeight() / 2;
        hudLeftX = hudVerticalMargin;
        hudRightX = WORLD_WIDTH * 2 / 3 - hudLeftX;
        hudCenterX = WORLD_WIDTH / 3;
        hudRow1Y = WORLD_HEIGHT - hudVerticalMargin;
        hudRow2Y = hudRow1Y - hudVerticalMargin - fontH3.getCapHeight();

        hudSectionWidth = WORLD_WIDTH / 3;

        hudDisplayRow1Y = WORLD_HEIGHT * 3 / 4;
        hudDisplayRow2Y = hudDisplayRow1Y - 10 - hudVerticalMargin - fontH2.getCapHeight();
        hudDisplayRow3Y = hudDisplayRow2Y - 10 - hudVerticalMargin - fontH2.getCapHeight() *2;

        buttonRow1Y = hudDisplayRow3Y - 20 - hudVerticalMargin - fontH2.getCapHeight();
        buttonRow2Y = buttonRow1Y - hudVerticalMargin - fontH2.getCapHeight();

        // Set up bounds for buttons
        setButtonBounds(fontH1, "Start", startTextBounds, hudCenterX, buttonRow1Y+5);
        setButtonBounds(fontH1, "Exit", exitTextBounds, hudCenterX, buttonRow2Y);

    }
    // Button bounds
    private void setButtonBounds(BitmapFont font, String buttonText, Rectangle bounds, float centerX, float rowY) {
        GlyphLayout buttonLayout = new GlyphLayout(font, buttonText);
        float buttonWidth = buttonLayout.width;
        float buttonHeight = buttonLayout.height;

        bounds.set(
                centerX,
                rowY,
                buttonWidth,
                buttonHeight
        );
    }
    // Home screen
    private void drawHomeScreenUI() {
        if(!scoreFetched){
            fetchScores();
            scoreFetched = true;
        }
        // Texts
        fontH1.draw(batch, "Galaxy Pilot", hudCenterX, hudDisplayRow1Y, hudSectionWidth, Align.center, false);
        fontH2.draw(batch, "Press Enter to Play!", hudCenterX, hudDisplayRow2Y, hudSectionWidth, Align.center, false);

        // Draw buttons
        fontH1.draw(batch, "Start", startTextBounds.x, startTextBounds.y + startTextBounds.height, hudSectionWidth, Align.center, false);
        fontH1.draw(batch, "Exit", exitTextBounds.x, exitTextBounds.y + exitTextBounds.height, hudSectionWidth, Align.center, false);
    }
    // Playground screen
    private void drawPlayGroundUI(){
        if(playerDead){
            enemyShipList.clear();
            playerLaserList.clear();
            enemyLaserList.clear();
            explosionList.clear();

            // Clear Shield and power.
            shieldList.clear();
            powerList.clear();
            compareScore();
            if(scoreCompared){
                gameState = GameState.GAME_OVER_SCREEN;
            }
        }
        playerDead = false;

        // Render labels
        fontH3.draw(batch, String.format(Locale.getDefault(), "%06d", playerScore), hudLeftX-6, hudRow1Y+5, hudSectionWidth, Align.left, false);

        // Icons
        for(int i = 1; i<=playerShip.shield; i++){
            float x = WORLD_WIDTH - 6*i;
            float y = WORLD_HEIGHT - 15;

            Icons icon = new Icons(x, y, 5, 5, 0, shieldIconTextureRegion);
            icon.draw(batch);
        }

        // Icons
        for(int i = 1; i<=playerShip.lives; i++){
            float x = WORLD_WIDTH - 6*i;
            float y = WORLD_HEIGHT - 8;

            Icons icon = new Icons(x, y, 5, 5, 0, livesIconTextureRegion);
            icon.draw(batch);
        }
    }

    private void drawGameOverScreenUI() {
        if(scoreUpdated){
            // Render Display text
            fontH1.draw(batch, "GAME OVER!", hudCenterX, hudDisplayRow1Y+30, hudSectionWidth, Align.center, false);
            fontH2.draw(batch, "Press Enter to Play Again!", hudCenterX, hudDisplayRow2Y+35, hudSectionWidth, Align.center, false);

            // Render fetched scores
            if (fetchedScores != null) {
                List<Integer> topScores = fetchedScores.subList(0, Math.min(5, fetchedScores.size()));
                int i = 1;
                float startY = hudDisplayRow3Y+50; // Starting Y position for rendering scores
                for (Integer score : topScores) {
                    fontH3.draw(batch, String.format(Locale.getDefault(), "HIGH SCORE "+i+": %d", score), hudCenterX, startY, hudSectionWidth, Align.center, false);
                    startY -= (fontH3.getLineHeight())+5; // Move to the next line
                    i++;
                }
            }

            // Render player's score
            fontH3.draw(batch, String.format(Locale.getDefault(), "YOUR SCORE: %d", playerScore), hudCenterX, hudDisplayRow3Y-5, hudSectionWidth, Align.center, false);

            // Draw buttons
            fontH1.draw(batch, "Start", startTextBounds.x, startTextBounds.y + startTextBounds.height, hudSectionWidth, Align.center, false);
            fontH1.draw(batch, "Exit", exitTextBounds.x, exitTextBounds.y + exitTextBounds.height, hudSectionWidth, Align.center, false);
        }
    }

    // Game background
    private void renderBackground(float deltaTime) {
        // Updating the background image position
        for (int layer = 0; layer < backgroundOffsets.length; layer++) {
            backgroundOffsets[layer] += deltaTime * backgroundMaxScrollingSpeed * (layer + 1) / 8;
            if (backgroundOffsets[layer] > WORLD_HEIGHT) {
                backgroundOffsets[layer] = 0;
            }
            batch.draw(backgrounds[layer], 0, -backgroundOffsets[layer], WORLD_WIDTH, backgroundHeight);
        }
    }

    // Playground
    private void updatePlayGroundUI(float deltaTime) {
        playerShip.draw(batch);
        playerShip.update(deltaTime);

        spawnEnemyShips(deltaTime);
        updateEnemies(deltaTime);

        renderLaser(deltaTime);
        detectCollisions();
        updateAndRenderExplosions(deltaTime);

        spawnShields();
        renderShields(deltaTime);

        spawnPowers();
        renderPowers(deltaTime);
    }

    private void updateEnemies(float deltaTime) {
        for (EnemyShip enemyShip : enemyShipList) {
            enemyShip.draw(batch);
            enemyShip.update(deltaTime);
            moveEnemy(enemyShip, deltaTime);
        }
    }
    private void detectInput(float deltaTime) {
        // Key & Touch input.
        if (Gdx.input.isTouched()) {
            float touchX = Gdx.input.getX();
            float touchY = Gdx.input.getY();

            // Convert touch coordinates to the world coordinates
            Vector3 touchPoint = new Vector3(touchX, touchY, 0);
            camera.unproject(touchPoint);

            if(gameState == GameState.HOME_SCREEN || gameState == GameState.GAME_OVER_SCREEN){
                // Check if the touch is within the bounds of the "Start" text
                if (isTouchWithinBounds(touchPoint.x, touchPoint.y, startTextBounds)) {
                    restartGame();
                    gameState = GameState.PLAYING_GROUND;
                }

                // Check if the touch is within the bounds of the "Exit" text
                if (isTouchWithinBounds(touchPoint.x, touchPoint.y, exitTextBounds)) {
                    Gdx.app.exit();  // Exit the game
                }
            }
        }

        if((gameState == GameState.HOME_SCREEN || gameState == GameState.GAME_OVER_SCREEN) && Gdx.input.isKeyPressed(Input.Keys.ENTER)){
            restartGame();
            gameState = GameState.PLAYING_GROUND;
        }

        // Key input
        // Strategy: determine the max distance the ship can move.
        // Check each key that matters and move accordingly.
        float leftLimit, rightLimit, upLimit, downLimit;
        leftLimit = -playerShip.boundingBox.x;
        downLimit = -playerShip.boundingBox.y;
        rightLimit = WORLD_WIDTH - playerShip.boundingBox.x - playerShip.boundingBox.width;
        upLimit = WORLD_HEIGHT /2 - playerShip.boundingBox.y - playerShip.boundingBox.height;

        if(Gdx.input.isKeyPressed(Input.Keys.RIGHT) && rightLimit > 0){
            playerShip.translate(Math.min(playerShip.movementSpeed*deltaTime, rightLimit), 0f);
        }
        if(Gdx.input.isKeyPressed(Input.Keys.UP) && upLimit > 0){
            playerShip.translate(0f, Math.min(playerShip.movementSpeed*deltaTime, upLimit));
        }
        if(Gdx.input.isKeyPressed(Input.Keys.LEFT) && leftLimit < 0){
            playerShip.translate(Math.max(-playerShip.movementSpeed*deltaTime, leftLimit), 0f);
        }
        if(Gdx.input.isKeyPressed(Input.Keys.DOWN) && downLimit < 0){
            playerShip.translate(0f, Math.max(-playerShip.movementSpeed*deltaTime, downLimit));
        }

        // Touch input.
        if(Gdx.input.isTouched()){
            // Getting screen position
            float xTouchPixels = Gdx.input.getX();
            float yTouchPixels = Gdx.input.getY();

            // Convert to world position
            Vector2 touchPoint = new Vector2(xTouchPixels, yTouchPixels);
            touchPoint = viewport.unproject(touchPoint);

            // Calculate X & Y difference
            Vector2 playerShipCenter = new Vector2(
                    playerShip.boundingBox.x + playerShip.boundingBox.width/2,
                    playerShip.boundingBox.y + playerShip.boundingBox.height/2
            );

            float touchDistance = touchPoint.dst(playerShipCenter);

            float TOUCH_MOVEMENT_THRESHOLD = 0.5f;
            if(touchDistance > TOUCH_MOVEMENT_THRESHOLD){
                float xTouchDifference = touchPoint.x - playerShipCenter.x;
                float yTouchDifference = touchPoint.y - playerShipCenter.y;

                // Scale to the maximum speed of the ship
                float xMove = xTouchDifference / touchDistance * playerShip.movementSpeed * deltaTime;
                float yMove = yTouchDifference / touchDistance * playerShip.movementSpeed * deltaTime;

                if(xMove > 0) xMove = Math.min(xMove, rightLimit);
                else xMove = Math.max(xMove, leftLimit);

                if(yMove > 0) yMove = Math.min(yMove, upLimit);
                else yMove = Math.max(yMove, downLimit);

                playerShip.translate(xMove, yMove);
            }
        }
    }

    // Helper method to check if a point (touch) is within the bounds of a rectangle
    private boolean isTouchWithinBounds(float touchX, float touchY, Rectangle bounds) {
        return touchX >= bounds.x && touchX <= bounds.x + bounds.width
                && touchY >= bounds.y && touchY <= bounds.y + bounds.height;
    }

    public void spawnPlayerShip() {
        playerShip = new PlayerShip(
                WORLD_WIDTH / 2,
                WORLD_HEIGHT / 4,
                10,
                10,
                50,
                2,
                2,
                3,
                4,
                45,
                0.5f,
                playerShipTextureRegion,
                playerShieldTextureRegion,
                playerLaserTextureRegion);
    }

    private void spawnEnemyShips(float deltaTime){
        enemySpawnTimer += deltaTime;
        superEnemySpawnTimer += deltaTime;
        superEliteEnemySpawnTimer += deltaTime;

        float timeBetweenEnemySpawn = 3f;
        float timeBetweenSuperEnemySpawn = 12f;
        float timeBetweenSuperEliteEnemySpawn = 25f;
        int power, shield;
        float width, height;

        TextureRegion enemyShipTypeTextureRegion;
        if(superEnemySpawnTimer > timeBetweenSuperEnemySpawn && playerShip.power <= 3 ){
            power = 2;
            shield = 2;
            width = 10;
            height = 10;
            enemyShipTypeTextureRegion = superEnemyShipTextureRegion;
            superEnemySpawnTimer -= timeBetweenSuperEnemySpawn;
        }
        else {
            power = 1;
            shield = 1;
            width = 10;
            height = 10;
            enemyShipTypeTextureRegion = enemyShipTextureRegion;
        }

        if(superEliteEnemySpawnTimer > timeBetweenSuperEliteEnemySpawn && playerShip.power > 2){
            power = 3;
            shield = 3;
            width = 12;
            height = 12;
            enemyShipTypeTextureRegion = superEliteEnemyShipTextureRegion;
            superEliteEnemySpawnTimer -= timeBetweenSuperEliteEnemySpawn;
        }

        if(enemySpawnTimer > timeBetweenEnemySpawn){
            enemyShipList.add(new EnemyShip(
                    SpaceShooterGame.random.nextFloat()*(WORLD_WIDTH-10)+5,
                    WORLD_HEIGHT - 5,
                    width,
                    height,
                    40,
                    shield,
                    power,
                    3,
                    4,
                    50,
                    0.8f,
                    enemyShipTypeTextureRegion,
                    enemyShieldTextureRegion,
                    enemyLaserTextureRegion
            ));
            enemySpawnTimer -= timeBetweenEnemySpawn;
        }
    }

    private void moveEnemy(EnemyShip enemyShip, float deltaTime){
        //Strategy: determine the max distance the ship can move.
        float leftLimit, rightLimit, upLimit, downLimit;
        leftLimit = -enemyShip.boundingBox.x;
        downLimit = WORLD_HEIGHT /2 - enemyShip.boundingBox.y;
        rightLimit = WORLD_WIDTH - enemyShip.boundingBox.x - enemyShip.boundingBox.width;
        upLimit = WORLD_HEIGHT - enemyShip.boundingBox.y - enemyShip.boundingBox.height;

        float xMove = enemyShip.getDirectionVector().x * enemyShip.movementSpeed * deltaTime;
        float yMove = enemyShip.getDirectionVector().y * enemyShip.movementSpeed * deltaTime;

        if(xMove > 0) xMove = Math.min(xMove, rightLimit);
        else xMove = Math.max(xMove, leftLimit);

        if(yMove > 0) yMove = Math.min(yMove, upLimit);
        else yMove = Math.max(yMove, downLimit);

        enemyShip.translate(xMove, yMove);
    }

    private void renderLaser(float deltaTime){
        ListIterator<Laser> iterator;

        //Create new lasers & remove old laser
        //Player lasers
        if (playerShip.canFireLaser()) {
            Laser[] lasers = playerShip.fireLasers();
            playerLaserList.addAll(Arrays.asList(lasers));
        }

        iterator = playerLaserList.listIterator();
        while(iterator.hasNext()) {
            Laser laser = iterator.next();
            laser.update(deltaTime);
            laser.draw(batch);
            laser.boundingBox.y += laser.movementSpeed*deltaTime;
            if (laser.boundingBox.y > WORLD_HEIGHT) {
                iterator.remove();
            }
        }

        //Create new lasers & remove old laser
        //Enemy lasers
        for (EnemyShip enemyShip : enemyShipList) {
            if (enemyShip.canFireLaser()) {
                Laser[] lasers = enemyShip.fireLasers();
                enemyLaserList.addAll(Arrays.asList(lasers));
            }
        }

        iterator = enemyLaserList.listIterator();
        while(iterator.hasNext()) {
            Laser laser = iterator.next();
            laser.draw(batch);
            laser.boundingBox.y -= laser.movementSpeed*deltaTime;
            if (laser.boundingBox.y + laser.boundingBox.height < 0) {
                iterator.remove();
            }
        }
    }

    private void detectCollisions(){
        ListIterator<Laser> laserListIterator;
        //For each player laser, check whether it intersects an enemy ship.
        laserListIterator = playerLaserList.listIterator();
        while(laserListIterator.hasNext()) {
            Laser laser = laserListIterator.next();
            ListIterator<EnemyShip> enemyShipListIterator = enemyShipList.listIterator();
            while (enemyShipListIterator.hasNext()) {
                EnemyShip enemyShip = enemyShipListIterator.next();

                if (enemyShip.intersects(laser.boundingBox)) {
                    if(enemyShip.hitAndCheckDestroyed(laser)){

                        enemyShipListIterator.remove();
                        explosionList.add(
                            new Explosion(
                                explosionTexture,
                                new Rectangle(enemyShip.boundingBox),
                                0.7f
                            )
                        );
                        int scoreIncrease = 0;
                        switch (enemyShip.getPower()) {
                            case 1:
                                scoreIncrease = 100;
                                break;
                            case 2:
                                scoreIncrease = 200;
                                break;
                            case 3:
                                scoreIncrease = 300;
                                break;
                        }

                        score += scoreIncrease;
                        playerScore = score;
                    }
                    laserListIterator.remove();
                    break;
                }
            }
        }

        //For each enemy laser, check whether it intersects an player ship.
        laserListIterator = enemyLaserList.listIterator();
        while(laserListIterator.hasNext()) {
            Laser laser = laserListIterator.next();
            if(playerShip.intersects(laser.boundingBox)){
                if(playerShip.hitAndCheckDestroyed(laser)){
                    playerShip.lives --;
                    if(playerShip.lives <= 0){
                        playerShip.lives = 0;
                        playerScore = score;
                        playerDead = true;
                        score = 0;
                    }
                    explosionList.add(
                        new Explosion(
                            explosionTexture,
                            new Rectangle(playerShip.boundingBox),
                            1.6f
                        )
                    );
                }
                laserListIterator.remove();
                playerShip.power = 1;
                break;
            }
        }
    }

    private void updateAndRenderExplosions(float deltaTime){
        ListIterator<Explosion> explosionListIterator = explosionList.listIterator();
        while (explosionListIterator.hasNext()){
            Explosion explosion = explosionListIterator.next();
            explosion.update(deltaTime);
            if(explosion.isFinished()){
                explosionListIterator.remove();
            } else {
                explosion.draw(batch);
            }
        }
    }

    private void spawnShields() {
        // Check if it's time to spawn a shield and playerShip.shield is less than 2
        timeSinceLastShieldSpawn += Gdx.graphics.getDeltaTime();
        if (timeSinceLastShieldSpawn > SHIELD_SPAWN_INTERVAL && playerShip.shield < 2) {
            // Reset the timer
            timeSinceLastShieldSpawn = 0f;

            // Spawn a new shield at a random X position and at the top of the screen
            float randomX = MathUtils.random(0, WORLD_WIDTH-10);
            float startY = WORLD_HEIGHT;
            Shield shield = new Shield(randomX, startY, 8, 8, -25f, shieldTextureRegion);
            shieldList.add(shield);
        }
    }

    private void renderShields(float deltaTime) {
        // Update and draw shields
        ListIterator<Shield> iterator = shieldList.listIterator();
        while (iterator.hasNext()) {
            Shield shield = iterator.next();
            shield.draw(batch); // Draw the shield
            shield.boundingBox.y += shield.fallSpeed * deltaTime; // Update shield position

            // Check for collision with playerShip
            if (shield.boundingBox.overlaps(playerShip.boundingBox)) {
                // Handle collision (increase playerShip shield, remove shield from list, etc.)
                playerShip.shield += 1;
                iterator.remove();
            }
            // Check if shield is out of screen
            if (shield.boundingBox.y + shield.boundingBox.height < 0) {
                iterator.remove(); // Remove shield if it's out of screen
            }
        }
    }

    private void spawnPowers(){
        timeSinceLastPowerSpawn += Gdx.graphics.getDeltaTime();
        if (timeSinceLastPowerSpawn > POWER_SPAWN_INTERVAL && playerShip.power < 3) {
            // Reset the timer
            timeSinceLastPowerSpawn = 0f;

            // Spawn a new shield at a random X position and at the top of the screen
            float randomX = MathUtils.random(0, WORLD_WIDTH-10);
            float startY = WORLD_HEIGHT;
            Power power = new Power(randomX, startY, 8, 8, -25f, powerTextureRegion);
            powerList.add(power);
        }
    }

    private void renderPowers(float deltaTime) {
        // Update and draw shields
        ListIterator<Power> iterator = powerList.listIterator();
        while (iterator.hasNext()) {
            Power power = iterator.next();
            power.draw(batch); // Draw the Power
            power.boundingBox.y += power.fallSpeed * deltaTime; // Update power position

            // Check for collision with playerShip
            if (power.boundingBox.overlaps(playerShip.boundingBox)) {
                // Handle collision (increase playerShip power, remove power from list, etc.)
                if (playerShip.power < 3) {
                    playerShip.power += 1;
                }
                iterator.remove();

                // Check if power is out of screen
                if (power.boundingBox.y + power.boundingBox.height < 0) {
                    iterator.remove(); // Remove power if it's out of screen
                }
            }
        }
    }

    private void restartGame() {
        scoreFetched = false;
        // Reset player's lives, score, and any other necessary parameters.
        spawnPlayerShip();
        score = 0;
        playerScore = 0;
        playerShip.power = 1;
        playerShip.shield = 2;

        // Clear enemy ships, lasers, and explosions.
        enemyShipList.clear();
        playerLaserList.clear();
        enemyLaserList.clear();
        explosionList.clear();

        // Clear Shield and power.
        shieldList.clear();
        powerList.clear();
    }

    private void playSounds() {
        switch (gameState) {
            case HOME_SCREEN:
                // Play the opening sound when on the home screen
                if (!isGameOpeningSoundPlaying) {
                    stopSounds();  // Stop any previously playing sounds
                    gameOpeningSound.play(0.5f);
                    gameOpeningSound.loop();
                    isGameOpeningSoundPlaying = true;
                }
                break;

            case PLAYING_GROUND:
                // Play the playing ground sound when the game starts
                if (!isPlayingGroundSoundPlaying) {
                    stopSounds();  // Stop any previously playing sounds
                    // playingGround Sound.
                    playingGroundSound.play(0.5f);
                    playingGroundSound.loop();
                    isPlayingGroundSoundPlaying = true;
                }
                break;

            case GAME_OVER_SCREEN:
                // Play the game over sound when the game is over
                if (!isGameOverSoundPlaying) {
                    stopSounds();  // Stop any previously playing sounds
                    gameOverSound.play(0.5f);
                    isGameOverSoundPlaying = true;
                }
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + gameState);
        }
    }

    private void stopSounds() {
        gameOpeningSound.stop();
        playingGroundSound.stop();
        gameOverSound.stop();

        // Reset playing flags
        isGameOpeningSoundPlaying = false;
        isPlayingGroundSoundPlaying = false;
        isGameOverSoundPlaying = false;
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void show() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        batch.dispose();
        gameOpeningSound.dispose();
        playingGroundSound.dispose();
        gameOverSound.dispose();
    }
}