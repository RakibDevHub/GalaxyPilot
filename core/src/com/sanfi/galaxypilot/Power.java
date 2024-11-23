package com.sanfi.galaxypilot;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

class Power {
    // Position & Dimensions
    Rectangle boundingBox;

    TextureRegion textureRegion;

    // Power characteristics
    float powerWidth, powerHeight;
    float fallSpeed; // world units per second

    public Power(float x, float y, float width, float height, float fallSpeed, TextureRegion textureRegion) {
        this.boundingBox = new Rectangle(x, y, width, height);
        this.fallSpeed = fallSpeed;
        this.powerWidth = width;
        this.powerHeight = height;
        this.textureRegion = textureRegion;
    }

    public void draw(Batch batch) {
        batch.draw(textureRegion, boundingBox.x, boundingBox.y, powerWidth, powerHeight);
    }

    public void update(float deltaTime) {
        boundingBox.y += fallSpeed * deltaTime;
    }

    public boolean collidesWith(Rectangle otherRectangle) {
        return boundingBox.overlaps(otherRectangle);
    }
}
