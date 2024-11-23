package com.sanfi.galaxypilot;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

class PlayerShip extends Ship {

    int lives;
    public PlayerShip(
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
            TextureRegion playerShieldTextureRegion,
            TextureRegion laserTextureRegion) {
        super(xCentre, yCentre, width, height, movementSpeed, shield, power, laserWidth, laserHeight, laserMovementSpeed, timeBetweenShots, shipTextureRegion, playerShieldTextureRegion, laserTextureRegion);
        this.lives = 3;
        this.power = power;
    }

    @Override
    public Laser[] fireLasers() {
        Laser[] lasers;
        if (power >= 3) {
            // Player has power level 3, fire three lasers
            lasers = new Laser[3];
            lasers[0] = new Laser(
                    boundingBox.x + boundingBox.width * 0.07f,
                    boundingBox.y + boundingBox.height * 0.45f,
                    laserWidth,
                    laserHeight,
                    laserMovementSpeed,
                    laserTextureRegion
            );
            lasers[1] = new Laser(
                    boundingBox.x + boundingBox.width / 2,
                    boundingBox.y + boundingBox.height,
                    laserWidth,
                    laserHeight,
                    laserMovementSpeed,
                    laserTextureRegion
            );
            lasers[2] = new Laser(
                    boundingBox.x + boundingBox.width * 0.93f,
                    boundingBox.y + boundingBox.height * 0.45f,
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
                    boundingBox.y + boundingBox.height * 0.45f,
                    laserWidth,
                    laserHeight,
                    laserMovementSpeed,
                    laserTextureRegion
            );
            lasers[1] = new Laser(
                    boundingBox.x + boundingBox.width * 0.93f,
                    boundingBox.y + boundingBox.height * 0.45f,
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
                    boundingBox.y + boundingBox.height,
                    laserWidth,
                    laserHeight,
                    laserMovementSpeed,
                    laserTextureRegion
            );
        }

        timeSinceLastShot = 0;
        return lasers;
    }
}