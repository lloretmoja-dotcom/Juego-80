package com.boug.runner.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import com.boug.runner.R;

public class BougCharacter {
    private Context context;
    
    // Position and size
    private float x, y;
    private float width = 80;
    private float height = 120;
    private float groundY;
    
    // Physics
    private float velocityY = 0;
    private float gravity = 0.8f;
    private float jumpPower = -15f;
    private float maxVelocityY = 20f;
    
    // States
    private boolean isJumping = false;
    private boolean isDoubleJumping = false;
    private boolean isSliding = false;
    private boolean isShieldActive = false;
    private boolean isMagnetActive = false;
    private boolean isDucking = false;
    
    // Animation
    private int frameIndex = 0;
    private float frameTimer = 0;
    private float animationSpeed = 0.1f;
    private Bitmap[] runFrames;
    private Bitmap jumpFrame;
    private Bitmap slideFrame;
    private Bitmap duckFrame;
    
    // Collision
    private Rect hitBox;
    
    // Power-ups
    private long shieldTimer = 0;
    private long magnetTimer = 0;
    private int jumpCount = 0;
    private final int MAX_JUMPS = 2;

    public BougCharacter(Context context) {
        this.context = context;
        loadSprites();
        reset();
    }

    private void loadSprites() {
        // Load character sprites
        // In production, use proper sprite sheets
        runFrames = new Bitmap[4];
        for (int i = 0; i < runFrames.length; i++) {
            runFrames[i] = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.boug_run_1 + i);
        }
        jumpFrame = BitmapFactory.decodeResource(context.getResources(),
            R.drawable.boug_jump);
        slideFrame = BitmapFactory.decodeResource(context.getResources(),
            R.drawable.boug_slide);
        duckFrame = BitmapFactory.decodeResource(context.getResources(),
            R.drawable.boug_duck);
    }

    public void init(float screenWidth, float screenHeight, float groundY) {
        this.x = screenWidth * 0.2f;
        this.groundY = groundY;
        this.y = groundY - height;
        this.hitBox = new Rect((int)x, (int)y, (int)(x + width), (int)(y + height));
    }

    public void update() {
        // Apply gravity
        if (isJumping || !isOnGround()) {
            velocityY += gravity;
            y += velocityY;
        }

        // Ground collision
        if (y >= groundY - height) {
            y = groundY - height;
            velocityY = 0;
            isJumping = false;
            isDoubleJumping = false;
            jumpCount = 0;
        }

        // Update hit box
        updateHitBox();

        // Update power-ups
        updatePowerUps();

        // Update animation
        updateAnimation();
    }

    private void updateHitBox() {
        if (isSliding) {
            hitBox.set((int)x, (int)(y + height - 60), (int)(x + width), (int)(y + height));
        } else if (isDucking) {
            hitBox.set((int)x, (int)(y + height - 80), (int)(x + width), (int)(y + height));
        } else {
            hitBox.set((int)x, (int)y, (int)(x + width), (int)(y + height));
        }
    }

    private void updatePowerUps() {
        if (isShieldActive && System.currentTimeMillis() - shieldTimer > 5000) {
            isShieldActive = false;
        }
        if (isMagnetActive && System.currentTimeMillis() - magnetTimer > 7000) {
            isMagnetActive = false;
        }
    }

    private void updateAnimation() {
        if (!isJumping && !isSliding && !isDucking) {
            frameTimer += animationSpeed;
            if (frameTimer >= 1) {
                frameTimer = 0;
                frameIndex = (frameIndex + 1) % runFrames.length;
            }
        }
    }

    public void draw(Canvas canvas, Paint paint) {
        Bitmap currentFrame;
        
        if (isSliding) {
            currentFrame = slideFrame;
        } else if (isDucking) {
            currentFrame = duckFrame;
        } else if (isJumping) {
            currentFrame = jumpFrame;
        } else {
            currentFrame = runFrames[frameIndex];
        }

        // Apply shield effect if active
        if (isShieldActive) {
            // Draw shield effect
            canvas.drawCircle(x + width/2, y + height/2, width/2 + 10, paint);
        }

        // Draw character
        canvas.drawBitmap(currentFrame, x, y, paint);
    }

    public void jump() {
        if (jumpCount < MAX_JUMPS) {
            if (jumpCount == 0) {
                velocityY = jumpPower;
                isJumping = true;
            } else if (jumpCount == 1) {
                velocityY = jumpPower * 0.8f;
                isDoubleJumping = true;
            }
            jumpCount++;
        }
    }

    public void slide() {
        if (!isJumping) {
            isSliding = true;
            isDucking = false;
        }
    }

    public void releaseSlide() {
        isSliding = false;
        isDucking = false;
    }

    public void duck() {
        if (!isJumping) {
            isDucking = true;
            isSliding = false;
        }
    }

    public void activateShield() {
        isShieldActive = true;
        shieldTimer = System.currentTimeMillis();
    }

    public void activateMagnet() {
        isMagnetActive = true;
        magnetTimer = System.currentTimeMillis();
    }

    public void reset() {
        velocityY = 0;
        isJumping = false;
        isDoubleJumping = false;
        isSliding = false;
        isShieldActive = false;
        isMagnetActive = false;
        jumpCount = 0;
    }

    public boolean isOnGround() {
        return y >= groundY - height - 1;
    }

    public boolean collidesWith(GameObject object) {
        return hitBox.intersect(object.getHitBox());
    }

    // Getters
    public float getX() { return x; }
    public float getY() { return y; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }
    public Rect getHitBox() { return hitBox; }
    public boolean isShieldActive() { return isShieldActive; }
    public boolean isMagnetActive() { return isMagnetActive; }
    public boolean isSliding() { return isSliding; }
    public boolean isDucking() { return isDucking; }
    public boolean isJumping() { return isJumping; }
}
