package com.astralis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class ModConfig {
    // Serializador JSON
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // Archivo de configuración
    private static final File CONFIG_FILE = new File(
            FabricLoader.getInstance().getConfigDir().toFile(),
            "unnecessary-anvil-tweaks.json"
    );

    // Opciones de configuración
    public TextAlignment textAlignment = TextAlignment.RIGHT;
    public AnimationType animationType = AnimationType.SLIDE;
    public float animationSpeed = 1.0f;
    public boolean resetAnimationOnChange = false;
    public boolean enableShakeForTooExpensive = true;

    // Duraciones base (segundos para velocidad 1.0)
    private static final float BASE_SLIDE = 0.5f;
    private static final float BASE_FADE = 0.3f;
    private static final float BASE_SLIDE_UP = 0.3f;
    private static final float BASE_TYPEWRITER = 0.8f;
    private static final float BASE_SLIDE_FADE = 0.7f;
    private static final float BASE_SHAKE = 0.3f;

    // Alineación de texto
    public enum TextAlignment {
        LEFT("LEFT"),
        CENTER("CENTER"),
        RIGHT("RIGHT");

        private final String displayName;

        TextAlignment(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Duración real de animación (ajustada por velocidad)
    public float getActualDuration() {
        if (animationType == null) {
            animationType = AnimationType.SLIDE;
        }

        float baseDuration = getBaseDurationForType(animationType);
        return baseDuration / animationSpeed;
    }

    // Duración del efecto shake
    public float getShakeDuration() {
        return BASE_SHAKE / animationSpeed;
    }

    // Detectar texto "Too Expensive"
    public boolean isTooExpensiveText(String text) {
        if (text == null || text.isEmpty()) return false;

        String lowerText = text.toLowerCase();
        return lowerText.contains("too expensive") ||
                lowerText.contains("demasiado caro") ||
                lowerText.equals("too expensive!") ||
                lowerText.equals("demasiado caro!");
    }

    // Duración base según tipo de animación
    private float getBaseDurationForType(AnimationType type) {
        if (type == null) {
            return BASE_SLIDE;
        }

        switch (type) {
            case SLIDE:
                return BASE_SLIDE;
            case FADE:
                return BASE_FADE;
            case SLIDE_UP:
                return BASE_SLIDE_UP;
            case TYPEWRITER:
                return BASE_TYPEWRITER;
            case SLIDE_FADE:
                return BASE_SLIDE_FADE;
            default:
                return BASE_SLIDE;
        }
    }

    // Singleton - Instancia única
    private static ModConfig INSTANCE;

    public static ModConfig load() {
        if (INSTANCE != null) {
            return INSTANCE;
        }

        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                INSTANCE = GSON.fromJson(reader, ModConfig.class);

                // Validar valores críticos
                if (INSTANCE.textAlignment == null) {
                    INSTANCE.textAlignment = TextAlignment.RIGHT;
                }
                if (INSTANCE.animationType == null) {
                    INSTANCE.animationType = AnimationType.SLIDE;
                }
                if (INSTANCE.animationSpeed < 0.1f) {
                    INSTANCE.animationSpeed = 1.0f;
                }

                return INSTANCE;
            } catch (Exception e) {
                UnnecessaryAnvilTweaks.LOGGER.error("Error loading config, using defaults", e);
            }
        }

        INSTANCE = new ModConfig();
        INSTANCE.save();
        return INSTANCE;
    }

    public void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(this, writer);
        } catch (Exception e) {
            UnnecessaryAnvilTweaks.LOGGER.error("Error saving config", e);
        }
    }

    public static ModConfig getInstance() {
        if (INSTANCE == null) {
            return load();
        }
        return INSTANCE;
    }
}