package fi.dy.masa.tweakeroo.mixin.essentialgui;

import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.util.CameraEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    @Redirect(
            method = "render",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/MinecraftClient;crosshairTarget:Lnet/minecraft/util/hit/HitResult;",
                    ordinal = 1
            )
    )
    private HitResult onRender(MinecraftClient mc) {
        CameraEntity entity;
        HitResult hit;
        if (FeatureToggle.TWEAK_FREE_CAMERA.getBooleanValue() &&
            (entity = CameraEntity.getCamera()) != null &&
            null != (hit = entity.getCrosshairTarget())) {
            return hit;
        }
        return mc.crosshairTarget;
    }
}
