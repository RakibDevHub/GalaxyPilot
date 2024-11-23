package com.sanfi.galaxypilot;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

class EnemyShip extends Ship {

    Vector2 directionVector;
    float timeSinceLastDirectionChange = 0;
    float directionChangeFrequency = 0.75f;
//    int power;
    float width, height;
    TextureRegion textureRegion;

    public EnemyShip(
            float xCentre,
            float yCentre,
            float width,
            float height,
            float movementSpeed,
            int shield,
            int power,
            float laserWidth,
            float laserHeight,
            float laserMovementSpeed,
            float timeBetweenShots,
            TextureRegion shipTextureRegion,
            TextureRegion shieldTextureRegion,
            TextureRegion laserTextureRegion
    ) {
        super(xCentre, yCentre, width, height, movementSpeed, shield, power, laserWidth, laserHeight, laserMovementSpeed, timeBetweenShots, shipTextureRegion, shieldTextureRegion, laserTextureRegion);

        this.power = power;
        this.shield = shield;
        this.width = width;
        this.height = height;

        directionVector = new Vector2(0, -1);
        this.textureRegion = shipTextureRegion;
    }

    public Vector2 getDirectionVector() {
        return directionVector;
    }

    private void randomizeDirectionVector(){
        double bearing = SpaceShooterGame.random.nextDouble()*6.283185; //0 to 2*PI
        directionVector.x = (float)Math.sin(bearing);
        directionVector.y = (float)Math.cos(bearing);
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        timeSinceLastDirectionChange += deltaTime;

        if(timeSinceLastDirectionChange > directionChangeFrequency){
            randomizeDirectionVector();
            timeSinceLastDirectionChange -= directionChangeFrequency;
        }
    }

    @Override
    public Laser[] fireLasers() {
        Laser[] lasers;
        if (power >= 3) {
            // Player has power level 3, fire three lasers
            lasers = new Laser[3];
            lasers[0] = new Laser(
                    boundingBox.x + boundingBox.width * 0.07f,
                    boundingBox.y - laserHeight * 0.45f,
                    laserWidth,
                    laserHeight,
                    laserMovementSpeed,
                    laserTextureRegion
            );
            lasers[1] = new Laser(
                    boundingBox.x + boundingBox.width / 2,
                    boundingBox.y - laserHeight,
                    laserWidth,
                    laserHeight,
                    laserMovementSpeed,
                    laserTextureRegion
            );
            lasers[2] = new Laser(
                    boundingBox.x + boundingBox.width * 0.93f,
                    boundingBox.y - laserHeight * 0.45f,
                    laserWidth,
                    laserHeight,
                    laserMovementSpeed,
                    laserTextureRegion
            );
        } else if (power == 2) {
            // Player has power level 2, fire two lasers
            lasers = new Laser[2];
            lasers[0] = new Laser(
                    boundingBox.x + boundingBox.width * 0.07f,
                    boundingBox.y - laserHeight * 0.45f,
                    laserWidth,
                    laserHeight,
                    laserMovementSpeed,
                    laserTextureRegion
            );
            lasers[1] = new Laser(
                    boundingBox.x + boundingBox.width * 0.93f,
                    boundingBox.y - laserHeight * 0.45f,
                    laserWidth,
                    laserHeight,
                    laserMovementSpeed,
                    laserTextureRegion
            );
        } else {
            // Player has power level 1, fire one laser
            lasers = new Laser[1];
            lasers[0] = new Laser(
                    boundingBox.x + boundingBox.width / 2,
                    boundingBox.y - laserHeight,
                    laserWidth,
                    laserHeight,
                    laserMovementSpeed,
                    laserTextureRegion
            );
        }

        timeSinceLastShot = 0;
        return lasers;
    }

    public int getPower() {
        return power;
    }

    @Override
    public void draw(Batch batch) {
        batch.draw(textureRegion, boundingBox.x, boundingBox.y, boundingBox.width, boundingBox.height);
        if (shield > 0) {
            batch.draw(shieldTextureRegion, boundingBox.x, boundingBox.y-boundingBox.height*0.2f, boundingBox.width, boundingBox.height);
        }
    }
}
