package fi.dy.masa.tweakeroo.tweaks

import fi.dy.masa.malilib.config.IConfigBoolean
import fi.dy.masa.malilib.interfaces.IValueChangeCallback
import fi.dy.masa.tweakeroo.config.FeatureToggle
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects

private val mc: MinecraftClient = MinecraftClient.getInstance()

class FakeNightVision : IValueChangeCallback<IConfigBoolean> {

    companion object {

        @JvmStatic
        fun onGameJoined() {
            reApplyNightVision()
        }

        @JvmStatic
        fun onEffectCleared() {
            reApplyNightVision()
        }

        @JvmStatic
        fun onPlayerRespawned() {
            reApplyNightVision()
        }

        private fun reApplyNightVision() {
            mc.player?.run {
                if (FeatureToggle.TWEAK_FAKE_NIGHT_VISION.booleanValue) {
                    addStatusEffect(StatusEffectInstance(StatusEffects.NIGHT_VISION, -1))
                }
            }
        }
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