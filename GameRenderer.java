package com.boug.runner.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.List;

public class GameRenderer extends SurfaceView implements SurfaceHolder.Callback {
    private BougGame game;
    private Paint paint;
    private Paint groundPaint;
    private Paint skyPaint;
    private Paint scorePaint;
    
    private float screenWidth;
    private float screenHeight;
    private boolean isInitialized = false;
    
    private OnGameOverListener gameOverListener;
    private OnScoreUpdateListener scoreUpdateListener;

    public interface OnGameOverListener {
        void onGameOver();
    }

    public interface OnScoreUpdateListener {
        void onScoreUpdate(int score);
    }

    public GameRenderer(Context context, BougGame game) {
        super(context);
        this.game = game;
        getHolder().addCallback(this);
        
        paint = new Paint();
        paint.setAntiAlias(true);
        
        groundPaint = new Paint();
        groundPaint.setColor(Color.parseColor("#4CAF50"));
        groundPaint.setStyle(Paint.Style.FILL);
        
        skyPaint = new Paint();
        skyPaint.setColor(Color.parseColor("#87CEEB"));
        skyPaint.setStyle(Paint.Style.FILL);
        
        scorePaint = new Paint();
        scorePaint.setColor(Color.WHITE);
        scorePaint.setTextSize(60);
        scorePaint.setFakeBoldText(true);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!isInitialized) {
            screenWidth = getWidth();
            screenHeight = getHeight();
            game.init(screenWidth, screenHeight);
            isInitialized = true;
        }
        draw();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (!isInitialized) {
            screenWidth = width;
            screenHeight = height;
            game.init(screenWidth, screenHeight);
            isInitialized = true;
        }
        draw();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Cleanup
    }

    public void draw() {
        if (!isInitialized) return;
        
        SurfaceHolder holder = getHolder();
        Canvas canvas = holder.lockCanvas();
        
        if (canvas != null) {
            // Draw background
            canvas.drawRect(0, 0, screenWidth, screenHeight, skyPaint);
            
            // Draw ground
            float groundY = game.getGroundY();
            canvas.drawRect(0, groundY, screenWidth, screenHeight, groundPaint);
            
            // Draw game objects
            drawGameObjects(canvas);
            
            // Draw score
            String scoreText = "Score: " + game.getScore();
            canvas.drawText(scoreText, 20, 80, scorePaint);
            
            // Draw coins count
            String coinsText = "🪙 " + game.getCoinsCollected();
            canvas.drawText(coinsText, screenWidth - 200, 80, scorePaint);
            
            holder.unlockCanvasAndPost(canvas);
        }
    }

    private void drawGameObjects(Canvas canvas) {
        // Draw boug
        game.getBoug().draw(canvas, paint);
        
        // Draw obstacles
        List<Obstacle> obstacles = game.getObstacles();
        for (Obstacle obstacle : obstacles) {
            obstacle.draw(canvas, paint);
        }
        
        // Draw coins
        List<Coin> coins = game.getCoins();
        for (Coin coin : coins) {
            coin.draw(canvas, paint);
        }
        
        // Draw diamonds
        List<Diamond> diamonds = game.getDiamonds();
        for (Diamond diamond : diamonds) {
            diamond.draw(canvas, paint);
        }
        
        // Draw power-ups
        List<PowerUp> powerUps = game.getPowerUps();
        for (PowerUp powerUp : powerUps) {
            powerUp.draw(canvas, paint);
        }
    }

    public void update() {
        game.update();
        
        // Update score
        if (scoreUpdateListener != null) {
            scoreUpdateListener.onScoreUpdate(game.getScore());
        }
        
        // Check game over
        if (!game.isRunning() && gameOverListener != null) {
            gameOverListener.onGameOver();
        }
    }

    public void setOnGameOverListener(OnGameOverListener listener) {
        this.gameOverListener = listener;
    }

    public void setOnScoreUpdateListener(OnScoreUpdateListener listener) {
        this.scoreUpdateListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }
}
