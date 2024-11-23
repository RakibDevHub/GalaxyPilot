package com.sanfi.galaxypilot;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

class Shield {
    // Position & Dimensions
    Rectangle boundingBox;

    TextureRegion textureRegion;

    // Shield characteristics
    float shieldWidth, shieldHeight;
    float fallSpeed; // world units per second

    public Shield(float x, float y, float width, float height, float fallSpeed, TextureRegion textureRegion) {
        this.boundingBox = new Rectangle(x, y, width, height);
        this.fallSpeed = fallSpeed;
        this.shieldWidth = width;
        this.shieldHeight = height;
        this.textureRegion = textureRegion;
    }

    public void draw(Batch batch) {
        batch.draw(textureRegion, boundingBox.x, boundingBox.y, shieldWidth, shieldHeight);
    }
//
//    public void update(float deltaTime) {
//        boundingBox.y += fallSpeed * deltaTime;
//    }
//
//    public boolean collidesWith(Rectangle otherRectangle) {
//        return boundingBox.overlaps(otherRectangle);
//    }
}
