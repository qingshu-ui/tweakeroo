package fi.dy.masa.tweakeroo.tweaks

import fi.dy.masa.malilib.config.IConfigBoolean
import fi.dy.masa.malilib.interfaces.IValueChangeCallback
import fi.dy.masa.tweakeroo.config.FeatureToggle
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects

class FakeNightVision(
    feature: FeatureToggle,
    val mc: MinecraftClient
) : IValueChangeCallback<IConfigBoolean> {

    companion object {
        var feature: FeatureToggle? = null

        @JvmStatic
        fun onGameJoined(mc: MinecraftClient) {
            val player = mc.player ?: return
            feature?.run {
                if (this.booleanValue) {
                    if (!player.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
                        player.addStatusEffect(StatusEffectInstance(StatusEffects.NIGHT_VISION, -1))
                    }
                }
            }
        }
    }

    init {
        FakeNightVision.feature = feature
    }

    override fun onValueChanged(config: IConfigBoolean) {
        val player = mc.player ?: return
        if (config.booleanValue) {
            player.addStatusEffect(StatusEffectInstance(StatusEffects.NIGHT_VISION, -1))
        } else if (player.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
            if (player.getStatusEffect(StatusEffects.NIGHT_VISION)!!.isInfinite) {
                player.removeStatusEffect(StatusEffects.NIGHT_VISION)
            }
        }
    }

}