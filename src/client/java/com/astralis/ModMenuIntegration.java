package com.astralis;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.text.Text;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            ModConfig config = ModConfig.getInstance();

            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Text.literal("(Un)Necessary Anvil Tweaks"));

            ConfigCategory general = builder.getOrCreateCategory(Text.literal("General"));
            ConfigEntryBuilder entryBuilder = builder.entryBuilder();

            // Alineación de texto
            general.addEntry(entryBuilder.startEnumSelector(
                            Text.literal("Text Alignment"),
                            ModConfig.TextAlignment.class,
                            config.textAlignment
                    )
                    .setDefaultValue(ModConfig.TextAlignment.RIGHT)
                    .setEnumNameProvider(value ->
                            Text.literal("" + ((ModConfig.TextAlignment) value).getDisplayName()))
                    .setTooltip(
                            Text.literal("Horizontal alignment of the repair cost text"),
                            Text.literal(""),
                            Text.literal("LEFT §8- §7Text aligned to left edge"),
                            Text.literal("CENTER §8- §7Text centered in the field"),
                            Text.literal("RIGHT §8- §7Text aligned to right edge (default)")
                    )
                    .setSaveConsumer(newValue -> config.textAlignment = newValue)
                    .build());

            // Tipo de animación
            general.addEntry(entryBuilder.startEnumSelector(
                            Text.literal("Animation Type"),
                            AnimationType.class,
                            config.animationType
                    )
                    .setDefaultValue(AnimationType.SLIDE)
                    .setEnumNameProvider(value -> {
                        AnimationType animType = (AnimationType) value;
                        return Text.literal(animType.getDisplayName());
                    })
                    .setTooltip(
                            Text.literal("Animation style when text appears"),
                            Text.literal(""),
                            Text.literal("Slide §8- §7Text slides in from the right (default)"),
                            Text.literal("Fade §8- §7Text fades in smoothly"),
                            Text.literal("Slide Up §8- §7Text slides up from bottom"),
                            Text.literal("Typewriter §8- §7Text appears character by character"),
                            Text.literal("Slide + Fade §8- §7Combined slide and fade effect")
                    )
                    .setSaveConsumer(newValue -> {
                        config.animationType = newValue;
                    })
                    .build());

            // Velocidad de animación
            general.addEntry(entryBuilder.startIntSlider(
                            Text.literal("Animation Speed"),
                            (int)(config.animationSpeed * 10),
                            1, // 0.1x
                            30 // 3.0x
                    )
                    .setDefaultValue(10) // 1.0x
                    .setTextGetter(value -> {
                        float floatValue = value / 10.0f;
                        String speedText;
                        if (floatValue <= 0.3f) speedText = "§cVery Slow";
                        else if (floatValue <= 0.7f) speedText = "§6Slow";
                        else if (floatValue <= 1.3f) speedText = "§aNormal";
                        else if (floatValue <= 2.0f) speedText = "§eFast";
                        else speedText = "§bVery Fast";

                        return Text.literal(String.format("%s §7(%.1fx)", speedText, floatValue));
                    })
                    .setTooltip(
                            Text.literal("Controls how fast animations play"),
                            Text.literal(""),
                            Text.literal("§c0.1x §8- §7Extremely slow (10% speed)"),
                            Text.literal("§60.5x §8- §7Half speed"),
                            Text.literal("§a1.0x §8- §7Normal speed (default)"),
                            Text.literal("§e2.0x §8- §7Double speed"),
                            Text.literal("§b3.0x §8- §7Maximum speed")
                    )
                    .setSaveConsumer(newValue -> {
                        config.animationSpeed = newValue / 10.0f;
                    })
                    .build());

            // Shake para "Too Expensive"
            general.addEntry(entryBuilder.startBooleanToggle(
                            Text.literal("Shake on 'Too Expensive!'"),
                            config.enableShakeForTooExpensive
                    )
                    .setDefaultValue(true)
                    .setTooltip(
                            Text.literal("Special effect when repair is too expensive"),
                            Text.literal(""),
                            Text.literal("§aEnabled §8- §7Text vibrates for 'Too Expensive!' (default)"),
                            Text.literal("§cDisabled §8- §7Uses normal animation style"),
                            Text.literal("")
                    )
                    .setSaveConsumer(newValue -> config.enableShakeForTooExpensive = newValue)
                    .build());

            builder.setSavingRunnable(() -> {
                config.save();
                UnnecessaryAnvilTweaks.LOGGER.info("Configuration saved!");
                UnnecessaryAnvilTweaks.LOGGER.info("Alignment: {}", config.textAlignment.getDisplayName());
                UnnecessaryAnvilTweaks.LOGGER.info("Animation: {} | Speed: {}x",
                        config.animationType.getDisplayName(),
                        config.animationSpeed);
                UnnecessaryAnvilTweaks.LOGGER.info("Shake on Too Expensive: {}", config.enableShakeForTooExpensive);
            });

            return builder.build();
        };
    }
}