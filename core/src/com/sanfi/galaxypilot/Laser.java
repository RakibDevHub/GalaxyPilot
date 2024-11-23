package com.sanfi.galaxypilot;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

class Laser {

    // Position & Dimensions
    Rectangle boundingBox;

    // Laser characteristics
    float movementSpeed; //world units per second
    Vector2 direction;

    // Graphics
    TextureRegion textureRegion;

    public Laser(
            float xCentre,
            float yBottom,
            float width,
            float height,
            float movementSpeed,
            TextureRegion textureRegion
    ) {
        this.boundingBox = new Rectangle(
                xCentre - width / 2,
                yBottom,
                width,
                height
        );

        this.movementSpeed = movementSpeed;
        this.direction = new Vector2(0, 1);
        this.textureRegion = textureRegion;
    }

    public void setDirection(float x, float y) {
        direction.set(x, y).nor();  // Set and normalize the direction vector
    }

    public void update(float deltaTime) {
        // Update the laser position based on its direction
        boundingBox.x += direction.x * movementSpeed * deltaTime;
        boundingBox.y += direction.y * movementSpeed * deltaTime;
    }

    public void draw(Batch batch) {
        batch.draw(textureRegion, boundingBox.x, boundingBox.y, boundingBox.width, boundingBox.height);
    }
}
