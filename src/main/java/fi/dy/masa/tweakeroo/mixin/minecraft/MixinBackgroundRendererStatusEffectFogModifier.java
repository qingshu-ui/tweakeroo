package fi.dy.masa.tweakeroo.mixin.minecraft;

import fi.dy.masa.tweakeroo.config.Configs;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.client.render.BackgroundRenderer$StatusEffectFogModifier")
public interface MixinBackgroundRendererStatusEffectFogModifier {

    @Shadow
    RegistryEntry<StatusEffect> getStatusEffect();

    // disable darkness
    @Inject(
            method = "shouldApply",
            at = @At("TAIL"),
            cancellable = true
    )
    default void onShouldApply(LivingEntity entity, float tickDelta, CallbackInfoReturnable<Boolean> cir) {
        if (Configs.Disable.DISABLE_DARKNESS.getBooleanValue() && this.getStatusEffect() == StatusEffects.DARKNESS) {
            cir.setReturnValue(false);
        }
    }
}
