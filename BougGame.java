package com.boug.runner.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.boug.runner.R;
import com.boug.runner.utils.PreferencesManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BougGame {
    private Context context;
    private BougCharacter boug;
    private List<Obstacle> obstacles;
    private List<Coin> coins;
    private List<Diamond> diamonds;
    private List<PowerUp> powerUps;
    
    private int score = 0;
    private int coinsCollected = 0;
    private int diamondsCollected = 0;
    private float gameSpeed = 10f;
    private float maxSpeed = 20f;
    private float speedIncrement = 0.001f;
    
    private Random random;
    private float groundY;
    private float screenWidth;
    private float screenHeight;
    
    private float obstacleTimer = 0;
    private float coinTimer = 0;
    private float diamondTimer = 0;
    private float powerUpTimer = 0;
    
    private boolean isRunning = true;

    public BougGame(Context context) {
        this.context = context;
        this.random = new Random();
        this.obstacles = new ArrayList<>();
        this.coins = new ArrayList<>();
        this.diamonds = new ArrayList<>();
        this.powerUps = new ArrayList<>();
        
        boug = new BougCharacter(context);
    }

    public void init(float screenWidth, float screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.groundY = screenHeight * 0.8f;
        
        boug.init(screenWidth, screenHeight, groundY);
    }

    public void update() {
        if (!isRunning) return;

        // Update game speed
        gameSpeed += speedIncrement;
        if (gameSpeed > maxSpeed) {
            gameSpeed = maxSpeed;
        }

        // Update score
        score += 1;

        // Update character
        boug.update();

        // Spawn obstacles
        obstacleTimer += 1;
        if (obstacleTimer > 60 - (gameSpeed / 2)) {
            spawnObstacle();
            obstacleTimer = 0;
        }

        // Spawn coins
        coinTimer += 1;
        if (coinTimer > 30) {
            spawnCoins();
            coinTimer = 0;
        }

        // Spawn diamonds
        diamondTimer += 1;
        if (diamondTimer > 200) {
            spawnDiamond();
            diamondTimer = 0;
        }

        // Update obstacles
        for (int i = obstacles.size() - 1; i >= 0; i--) {
            Obstacle obstacle = obstacles.get(i);
            obstacle.update(gameSpeed);
            
            // Check collision with boug
            if (boug.collidesWith(obstacle)) {
                isRunning = false;
                return;
            }
            
            // Remove if off screen
            if (obstacle.isOffScreen()) {
                obstacles.remove(i);
            }
        }

        // Update coins
        for (int i = coins.size() - 1; i >= 0; i--) {
            Coin coin = coins.get(i);
            coin.update(gameSpeed);
            
            // Check collection
            if (boug.collidesWith(coin)) {
                coinsCollected++;
                PreferencesManager.getInstance().addCoins(1);
                coins.remove(i);
                continue;
            }
            
            // Remove if off screen
            if (coin.isOffScreen()) {
                coins.remove(i);
            }
        }

        // Update diamonds
        for (int i = diamonds.size() - 1; i >= 0; i--) {
            Diamond diamond = diamonds.get(i);
            diamond.update(gameSpeed);
            
            // Check collection
            if (boug.collidesWith(diamond)) {
                diamondsCollected++;
                PreferencesManager.getInstance().addDiamonds(1);
                diamonds.remove(i);
                continue;
            }
            
            // Remove if off screen
            if (diamond.isOffScreen()) {
                diamonds.remove(i);
            }
        }

        // Update power-ups
        for (int i = powerUps.size() - 1; i >= 0; i--) {
            PowerUp powerUp = powerUps.get(i);
            powerUp.update(gameSpeed);
            
            // Check collection
            if (boug.collidesWith(powerUp)) {
                applyPowerUp(powerUp.getType());
                powerUps.remove(i);
                continue;
            }
            
            // Remove if off screen
            if (powerUp.isOffScreen()) {
                powerUps.remove(i);
            }
        }
    }

    private void spawnObstacle() {
        float x = screenWidth;
        float y = groundY;
        ObstacleType type = ObstacleType.values()[random.nextInt(ObstacleType.values().length)];
        Obstacle obstacle = new Obstacle(context, type, x, y);
        obstacles.add(obstacle);
    }

    private void spawnCoins() {
        // Spawn 3-5 coins in a row
        int count = 3 + random.nextInt(3);
        float x = screenWidth;
        float y = groundY - 100 - random.nextInt(200);
        
        for (int i = 0; i < count; i++) {
            Coin coin = new Coin(context, x + (i * 60), y);
            coins.add(coin);
        }
    }

    private void spawnDiamond() {
        float x = screenWidth;
        float y = groundY - 150 - random.nextInt(200);
        Diamond diamond = new Diamond(context, x, y);
        diamonds.add(diamond);
    }

    private void applyPowerUp(PowerUpType type) {
        switch (type) {
            case SHIELD:
                boug.activateShield();
                break;
            case MAGNET:
                boug.activateMagnet();
                break;
            case DOUBLE_COINS:
                // Implement double coins
                break;
        }
    }

    public void jump() {
        boug.jump();
    }

    public void slide() {
        boug.slide();
    }

    public void releaseSlide() {
        boug.releaseSlide();
    }

    public void reset() {
        score = 0;
        coinsCollected = 0;
        diamondsCollected = 0;
        gameSpeed = 10f;
        isRunning = true;
        obstacles.clear();
        coins.clear();
        diamonds.clear();
        powerUps.clear();
        boug.reset();
    }

    // Getters
    public BougCharacter getBoug() {
        return boug;
    }

    public List<Obstacle> getObstacles() {
        return obstacles;
    }

    public List<Coin> getCoins() {
        return coins;
    }

    public List<Diamond> getDiamonds() {
        return diamonds;
    }

    public List<PowerUp> getPowerUps() {
        return powerUps;
    }

    public int getScore() {
        return score;
    }

    public int getCoinsCollected() {
        return coinsCollected;
    }

    public int getDiamondsCollected() {
        return diamondsCollected;
    }

    public float getGroundY() {
        return groundY;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public float getGameSpeed() {
        return gameSpeed;
    }
}
