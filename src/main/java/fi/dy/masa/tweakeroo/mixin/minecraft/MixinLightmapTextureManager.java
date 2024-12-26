package fi.dy.masa.tweakeroo.mixin.minecraft;

import fi.dy.masa.tweakeroo.config.Configs;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LightmapTextureManager.class)
public abstract class MixinLightmapTextureManager {

    // disable darkness
    @Redirect(
            method = "getDarknessFactor",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/effect/StatusEffectInstance;getFadeFactor(Lnet/minecraft/entity/LivingEntity;F)F"
            )
    )
    private float onGetDarknessFactor(StatusEffectInstance instance, LivingEntity entity, float tickDelta) {
        if (Configs.Disable.DISABLE_DARKNESS.getBooleanValue()) {
            return 0.0F;
        }
        return instance.getFadeFactor(entity, tickDelta);
    }
}
