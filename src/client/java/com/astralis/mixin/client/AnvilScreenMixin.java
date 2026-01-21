package com.astralis.mixin.client;

import com.astralis.AnimationType;
import com.astralis.ModConfig;
import com.astralis.TextAnimator;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AnvilScreen.class)
public class AnvilScreenMixin {
	@Unique
	private final TextAnimator animator = new TextAnimator();

	@Unique
	private String currentDisplayedText = "";

	@Unique
	private long lastTextUpdateTime = 0;

	@Unique
	private long fadeStartTime = 0;

	@Unique
	private boolean isFadingIn = false;

	@Unique
	private String fadingText = "";

	// Eliminar fondo del texto
	@Redirect(
			method = "drawForeground",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/DrawContext;fill(IIIII)V",
					ordinal = 0
			)
	)
	private void removeBackground(
			DrawContext context,
			int x1, int y1, int x2, int y2, int color
	) {
		// No dibujar fondo (hacemos que el método no haga nada)
	}

	// Reemplazar dibujado de texto con versión animada
	@Redirect(
			method = "drawForeground",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)V"
			)
	)
	private void drawAnimatedText(
			DrawContext context,
			TextRenderer renderer,
			Text text,
			int x,
			int y,
			int color
	) {
		ModConfig config = ModConfig.getInstance();
		String incomingText = text.getString();

		// Detectar si el texto es nuevo
		boolean isNewText = !incomingText.equals(currentDisplayedText);

		if (isNewText) {
			currentDisplayedText = incomingText;
			lastTextUpdateTime = System.currentTimeMillis();

			// Activar fade in si es "Too Expensive"
			if (config.enableShakeForTooExpensive && config.isTooExpensiveText(incomingText)) {
				isFadingIn = true;
				fadeStartTime = System.currentTimeMillis();
				fadingText = incomingText;
			} else {
				isFadingIn = false;
			}
		}

		// Calcular posición según alineación
		int textWidth = renderer.getWidth(text);
		int fieldStart = 8;
		int fieldEnd = 168;
		int fieldWidth = fieldEnd - fieldStart;

		int targetX = x;
		int targetY = y - 3;

		switch (config.textAlignment) {
			case LEFT:
				targetX = fieldStart;
				break;
			case CENTER:
				targetX = fieldStart + (fieldWidth - textWidth) / 2;
				break;
			case RIGHT:
				targetX = fieldEnd - textWidth;
				break;
		}

		// Obtener valores de animación
		TextAnimator.AnimationValues animValues = animator.getAnimation(
				targetX, targetY, incomingText, color, config, isNewText
		);

		// Aplicar fade in global si corresponde
		float globalAlpha = 1.0f;

		if (isFadingIn && incomingText.equals(fadingText)) {
			long currentTime = System.currentTimeMillis();
			long fadeElapsed = currentTime - fadeStartTime;
			long fadeDuration = 150; // 150ms

			if (fadeElapsed < fadeDuration) {
				globalAlpha = fadeElapsed / (float) fadeDuration;
				globalAlpha = globalAlpha * globalAlpha; // Suavizado
			} else {
				isFadingIn = false;
				globalAlpha = 1.0f;
			}
		}

		// Combinar alpha del animador con alpha global
		float finalAlpha = animValues.alpha * globalAlpha;

		// Aplicar alpha al color final
		int alphaByte = (int)(finalAlpha * 255);
		int finalColor = (animValues.color & 0x00FFFFFF) | (alphaByte << 24);

		// Dibujar texto animado
		if (animValues.isPartialText) {
			Text partialText = Text.literal(animValues.displayText);
			context.drawTextWithShadow(renderer, partialText, animValues.x, animValues.y, finalColor);
		} else {
			context.drawTextWithShadow(renderer, text, animValues.x, animValues.y, finalColor);
		}
	}
}