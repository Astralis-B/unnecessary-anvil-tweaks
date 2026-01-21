package com.astralis;

public enum AnimationType {
    NONE("None", "§7No animation"),
    SLIDE("Slide", "§aText slides from right"),
    FADE("Fade", "§bText fades in smoothly"),
    SLIDE_UP("Slide Up", "§eText slides from bottom"),
    TYPEWRITER("Typewriter", "§6Text appears character by character"),
    SLIDE_FADE("Slide + Fade", "§5Combined slide and fade");

    private final String displayName;
    private final String description;

    AnimationType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}