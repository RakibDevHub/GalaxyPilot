package com.sanfi.galaxypilot;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

abstract class Ship {

    //Ship characteristics
    float movementSpeed;  //World units per second
    int shield, power;

    //Position & Dimension
    Rectangle boundingBox;

    //Laser information
    float laserWidth, laserHeight;
    float laserMovementSpeed;
    float timeBetweenShots;
    float timeSinceLastShot = 0;

    //Graphics
    TextureRegion shipTextureRegion, shieldTextureRegion, laserTextureRegion;

    public Ship(
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
        this.movementSpeed = movementSpeed;
        this.shield = shield;
        this.power = power;
        this.boundingBox = new Rectangle(
                xCentre - width / 2,
                yCentre - height / 2,
                width,
                height
        );

        this.laserWidth = laserWidth;
        this.laserHeight = laserHeight;
        this.laserMovementSpeed = laserMovementSpeed;
        this.timeBetweenShots = timeBetweenShots;
        this.shipTextureRegion = shipTextureRegion;
        this.shieldTextureRegion = shieldTextureRegion;
        this.laserTextureRegion = laserTextureRegion;
    }

    public void update(float deltaTime){
        timeSinceLastShot += deltaTime;
    }

    public boolean canFireLaser() {
        return (timeSinceLastShot - timeBetweenShots >= 0);
    }

    public abstract Laser[] fireLasers();

    //Checking collision between laser & ship
    public boolean intersects(Rectangle otherRectangle){
        return boundingBox.overlaps(otherRectangle);
    }

    public boolean hitAndCheckDestroyed(Laser laser){
        if(shield > 0){
            shield --;
            return false;
        }
        return true;
    }

    public void translate(float xChange, float yChange){
        boundingBox.setPosition(boundingBox.x + xChange, boundingBox.y + yChange);
    }

    public void draw(Batch batch) {
        batch.draw(
                shipTextureRegion, boundingBox.x, boundingBox.y, boundingBox.width, boundingBox.height);
        if (shield > 0) {
            batch.draw(shieldTextureRegion, boundingBox.x, boundingBox.y, boundingBox.width, boundingBox.height);
        }
    }
}
