package com.sanfi.galaxypilot;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

class Icons {
    float x, y, width, height;
    int value;
    TextureRegion textureRegion;
    public Icons(float x, float y, float width, float height, int value, TextureRegion textureRegion){
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.value = value;
        this.textureRegion = textureRegion;
    }

    public void draw(Batch batch) {
        batch.draw(textureRegion, x, y, width, height);
    }
}
