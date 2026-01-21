package com.astralis;

import java.util.Random;

public class TextAnimator {
    private String animatedText = "";
    private long animationStartTime = 0;
    private float animationDuration = 0.5f;
    private AnimationType currentType = AnimationType.NONE;

    // Posiciones de animación
    private int startX = 0;
    private int startY = 0;
    private int targetX = 0;
    private int targetY = 0;

    // Texto completo para typewriter
    private String fullTypewriterText = "";

    // Variables para shake
    private boolean isShaking = false;
    private long shakeStartTime = 0;
    private Random random = new Random();
    private long lastShakeChangeTime = 0;
    private int currentShakeX = 0;
    private int currentShakeY = 0;

    // Control de fade in
    private boolean hasDoneFadeIn = false;

    public AnimationValues getAnimation(int targetX, int targetY, String text,
                                        int color, ModConfig config,
                                        boolean isNewText) {

        // Detectar "Too Expensive" para shake
        if (config.enableShakeForTooExpensive && config.isTooExpensiveText(text)) {
            if (!isShaking) {
                startShake(targetX, targetY, text, config);
                isShaking = true;
                shakeStartTime = System.currentTimeMillis();
                lastShakeChangeTime = shakeStartTime;
                hasDoneFadeIn = false;
            }

            long currentTime = System.currentTimeMillis();
            float elapsed = (currentTime - shakeStartTime) / 1000.0f;
            float progress = Math.min(1.0f, elapsed / config.getShakeDuration());

            if (progress >= 1.0f) {
                isShaking = false;
            } else {
                return createShakeValues(targetX, targetY, text, color, progress, currentTime);
            }
        }
        else if (isShaking) {
            isShaking = false;
            hasDoneFadeIn = false;
        }

        // Animación normal
        if (isNewText && config.animationType != AnimationType.NONE) {
            startNewAnimation(targetX, targetY, text, config);
        }

        if (animatedText.isEmpty() || config.animationType == AnimationType.NONE) {
            return createNormalValues(targetX, targetY, text, color);
        }

        long currentTime = System.currentTimeMillis();
        float elapsed = (currentTime - animationStartTime) / 1000.0f;
        float progress = Math.min(1.0f, elapsed / animationDuration);

        if (progress >= 1.0f) {
            animatedText = "";
            return createNormalValues(targetX, targetY, text, color);
        }

        return createAnimatedValues(text, color, config, progress);
    }

    private void startNewAnimation(int targetX, int targetY, String text, ModConfig config) {
        this.animatedText = text;
        this.animationStartTime = System.currentTimeMillis();
        this.animationDuration = config.getActualDuration();
        this.currentType = config.animationType;
        this.targetX = targetX;
        this.targetY = targetY;

        if (config.animationType == AnimationType.TYPEWRITER) {
            this.fullTypewriterText = text;
        }

        switch (config.animationType) {
            case SLIDE:
            case SLIDE_FADE:
                this.startX = targetX + 60;
                this.startY = targetY;
                break;

            case SLIDE_UP:
                this.startX = targetX;
                this.startY = targetY + 10;
                break;

            default:
                this.startX = targetX;
                this.startY = targetY;
        }
    }

    private void startShake(int targetX, int targetY, String text, ModConfig config) {
        this.targetX = targetX;
        this.targetY = targetY;
        this.currentShakeX = 0;
        this.currentShakeY = 0;
    }

    private AnimationValues createNormalValues(int x, int y, String text, int color) {
        AnimationValues values = new AnimationValues();
        values.x = x;
        values.y = y;
        values.alpha = 1.0f;
        values.displayText = text;
        values.color = color;
        values.isPartialText = false;
        return values;
    }

    private AnimationValues createAnimatedValues(String text, int color,
                                                 ModConfig config, float progress) {
        AnimationValues values = new AnimationValues();

        switch (config.animationType) {
            case SLIDE:
                values.x = calculateSlideX(progress);
                values.y = targetY;
                values.alpha = 1.0f;
                break;

            case FADE:
                values.x = targetX;
                values.y = targetY;
                values.alpha = calculateFadeAlpha(progress);
                break;

            case SLIDE_UP:
                values.x = targetX;
                values.y = calculateSlideUpY(progress);
                values.alpha = 1.0f;
                break;

            case TYPEWRITER:
                values.x = targetX;
                values.y = targetY;
                values.alpha = 1.0f;
                values.displayText = calculateTypewriterText(progress);
                values.isPartialText = progress < 0.99f;
                break;

            case SLIDE_FADE:
                values.x = calculateSlideX(progress);
                values.y = targetY;
                values.alpha = calculateFadeAlpha(progress);
                break;

            default:
                values.x = targetX;
                values.y = targetY;
                values.alpha = 1.0f;
        }

        if (config.animationType != AnimationType.TYPEWRITER) {
            values.displayText = text;
            values.isPartialText = false;
        }

        int alpha = (int)(values.alpha * 255);
        values.color = (color & 0x00FFFFFF) | (alpha << 24);

        return values;
    }

    // Shake sin fades
    private AnimationValues createShakeValues(int targetX, int targetY, String text,
                                              int color, float progress, long currentTime) {
        AnimationValues values = new AnimationValues();

        long time = currentTime - shakeStartTime;

        float freq = 7.0f;
        float amp = 1.5f;

        float shakeX = (float) Math.sin(time * 0.001 * freq * Math.PI * 2) * amp;
        float shakeY = (float) Math.cos(time * 0.001 * freq * Math.PI * 1.7) * (amp * 0.8f);

        values.x = targetX + (int) shakeX;
        values.y = targetY + (int) shakeY;

        values.alpha = 1.0f;
        values.displayText = text;
        values.isPartialText = false;
        values.color = color;

        return values;
    }

    // Métodos de cálculo

    private int calculateSlideX(float progress) {
        float eased = 1 - (1 - progress) * (1 - progress) * (1 - progress);
        return (int) (startX + (targetX - startX) * eased);
    }

    private int calculateSlideUpY(float progress) {
        float eased = 1 - (1 - progress) * (1 - progress);
        return (int) (startY + (targetY - startY) * eased);
    }

    private float calculateFadeAlpha(float progress) {
        return progress * progress;
    }

    private String calculateTypewriterText(float progress) {
        if (fullTypewriterText.isEmpty()) {
            return "";
        }

        int totalChars = fullTypewriterText.length();

        if (totalChars <= 3) {
            return fullTypewriterText;
        }

        int chars = (int) (totalChars * progress);

        if (progress > 0.01f && chars < 1) {
            chars = 1;
        }

        chars = Math.min(chars, totalChars);

        return fullTypewriterText.substring(0, chars);
    }

    public static class AnimationValues {
        public int x;
        public int y;
        public float alpha;
        public String displayText;
        public int color;
        public boolean isPartialText;
    }
}