package fi.dy.masa.tweakeroo.event;

import com.llamalad7.mixinextras.lib.apache.commons.tuple.Pair;
import com.mojang.blaze3d.systems.RenderSystem;
import org.joml.Matrix4f;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.interfaces.IRenderer;
import fi.dy.masa.malilib.render.InventoryOverlay;
import fi.dy.masa.malilib.util.ActiveMode;
import fi.dy.masa.malilib.util.Color4f;
import fi.dy.masa.malilib.util.InventoryUtils;
import fi.dy.masa.malilib.util.WorldUtils;
import fi.dy.masa.malilib.util.nbt.NbtKeys;
import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.config.Hotkeys;
import fi.dy.masa.tweakeroo.data.ServerDataSyncer;
import fi.dy.masa.tweakeroo.renderer.RenderUtils;
import fi.dy.masa.tweakeroo.util.RayTraceUtils;

public class RenderHandler implements IRenderer
{
    private static final RenderHandler INSTANCE = new RenderHandler();
    private final MinecraftClient mc;

    public RenderHandler()
    {
        this.mc = MinecraftClient.getInstance();
    }

    public static RenderHandler getInstance()
    {
        return INSTANCE;
    }

    @Override
    public void onRenderGameOverlayPost(DrawContext drawContext)
    {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (FeatureToggle.TWEAK_HOTBAR_SWAP.getBooleanValue() &&
            Hotkeys.HOTBAR_SWAP_BASE.getKeybind().isKeybindHeld())
        {
            RenderUtils.renderHotbarSwapOverlay(mc, drawContext);
        }
        else if (FeatureToggle.TWEAK_HOTBAR_SCROLL.getBooleanValue() &&
                 Hotkeys.HOTBAR_SCROLL.getKeybind().isKeybindHeld())
        {
            RenderUtils.renderHotbarScrollOverlay(mc, drawContext);
        }

        if (FeatureToggle.TWEAK_INVENTORY_PREVIEW.getBooleanValue() &&
            Hotkeys.INVENTORY_PREVIEW.getKeybind().isKeybindHeld())
        {
            InventoryOverlay.Context context = RayTraceUtils.getTargetInventory(mc);

            if (context != null)
            {
                RenderUtils.renderInventoryOverlay(context, drawContext);
            }
        }

        if (FeatureToggle.TWEAK_PLAYER_INVENTORY_PEEK.getBooleanValue() &&
            Hotkeys.PLAYER_INVENTORY_PEEK.getKeybind().isKeybindHeld())
        {
            RenderUtils.renderPlayerInventoryOverlay(mc, drawContext);
        }

        if (FeatureToggle.TWEAK_SNAP_AIM.getBooleanValue() &&
            Configs.Generic.SNAP_AIM_INDICATOR.getBooleanValue())
        {
            RenderUtils.renderSnapAimAngleIndicator(drawContext);
        }

        if (FeatureToggle.TWEAK_ELYTRA_CAMERA.getBooleanValue())
        {
            ActiveMode mode = (ActiveMode) Configs.Generic.ELYTRA_CAMERA_INDICATOR.getOptionListValue();

            if (mode == ActiveMode.ALWAYS || (mode == ActiveMode.WITH_KEY && Hotkeys.ELYTRA_CAMERA.getKeybind().isKeybindHeld()))
            {
                RenderUtils.renderPitchLockIndicator(mc, drawContext);
            }
        }
    }

    @Override
    public void onRenderTooltipLast(DrawContext drawContext, ItemStack stack, int x, int y)
    {
        Item item = stack.getItem();
        if (item instanceof FilledMapItem)
        {
            if (FeatureToggle.TWEAK_MAP_PREVIEW.getBooleanValue() &&
                (Configs.Generic.MAP_PREVIEW_REQUIRE_SHIFT.getBooleanValue() == false || GuiBase.isShiftDown()))
            {
                fi.dy.masa.malilib.render.RenderUtils.renderMapPreview(stack, x, y, Configs.Generic.MAP_PREVIEW_SIZE.getIntegerValue(), false);
            }
        }
        else if (stack.getComponents().contains(DataComponentTypes.CONTAINER) && InventoryUtils.shulkerBoxHasItems(stack))
        {
            if (FeatureToggle.TWEAK_SHULKERBOX_DISPLAY.getBooleanValue() &&
                (Configs.Generic.SHULKER_DISPLAY_REQUIRE_SHIFT.getBooleanValue() == false || GuiBase.isShiftDown()))
            {
                fi.dy.masa.malilib.render.RenderUtils.renderShulkerBoxPreview(stack, x, y, Configs.Generic.SHULKER_DISPLAY_BACKGROUND_COLOR.getBooleanValue(), drawContext);
            }
        }
        else if (stack.isOf(Items.ENDER_CHEST) && Configs.Generic.SHULKER_DISPLAY_ENDER_CHEST.getBooleanValue())
        {
            if (FeatureToggle.TWEAK_SHULKERBOX_DISPLAY.getBooleanValue() &&
                (Configs.Generic.SHULKER_DISPLAY_REQUIRE_SHIFT.getBooleanValue() == false || GuiBase.isShiftDown()))
            {
                World world = WorldUtils.getBestWorld(this.mc);
                if (world == null || this.mc.player == null)
                {
                    return;
                }
                PlayerEntity player = world.getPlayerByUuid(this.mc.player.getUuid());

                if (player != null)
                {
                    Pair<Entity, NbtCompound> pair = ServerDataSyncer.getInstance().requestEntity(player.getId());
                    NbtCompound nbt = new NbtCompound();
                    EnderChestInventory inv;

                    if (pair != null && pair.getRight() != null && pair.getRight().contains(NbtKeys.ENDER_ITEMS))
                    {
                        inv = InventoryUtils.getPlayerEnderItemsFromNbt(pair.getRight(), world.getRegistryManager());
                    }
                    else
                    {
                        inv = player.getEnderChestInventory();
                    }

                    if (inv != null)
                    {
                        nbt.put(NbtKeys.ENDER_ITEMS, inv.toNbtList(world.getRegistryManager()));
                        fi.dy.masa.malilib.render.RenderUtils.renderNbtItemsPreview(stack, nbt, x, y, false, drawContext);
                    }
                }
            }
        }
        else if (stack.getComponents().contains(DataComponentTypes.BUNDLE_CONTENTS) && InventoryUtils.bundleHasItems(stack))
        {
            if (FeatureToggle.TWEAK_BUNDLE_DISPLAY.getBooleanValue() &&
                (Configs.Generic.BUNDLE_DISPLAY_REQUIRE_SHIFT.getBooleanValue() == false || GuiBase.isShiftDown()))
            {
                fi.dy.masa.malilib.render.RenderUtils.renderBundlePreview(stack, x, y, Configs.Generic.BUNDLE_DISPLAY_ROW_WIDTH.getIntegerValue(), Configs.Generic.BUNDLE_DISPLAY_BACKGROUND_COLOR.getBooleanValue(), drawContext);
            }
        }
    }

    @Override
    public void onRenderWorldLast(Matrix4f posMatrix, Matrix4f projMatrix)
    {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (mc.player != null)
        {
            this.renderOverlays(posMatrix, mc);
        }
    }

    private void renderOverlays(Matrix4f posMatrix, MinecraftClient mc)
    {
        Entity entity = mc.getCameraEntity();

        if (FeatureToggle.TWEAK_FLEXIBLE_BLOCK_PLACEMENT.getBooleanValue() &&
            entity != null &&
            mc.crosshairTarget != null &&
            mc.crosshairTarget.getType() == HitResult.Type.BLOCK &&
            (Hotkeys.FLEXIBLE_BLOCK_PLACEMENT_ROTATION.getKeybind().isKeybindHeld() ||
             Hotkeys.FLEXIBLE_BLOCK_PLACEMENT_OFFSET.getKeybind().isKeybindHeld() ||
             Hotkeys.FLEXIBLE_BLOCK_PLACEMENT_ADJACENT.getKeybind().isKeybindHeld()))
        {
            BlockHitResult hitResult = (BlockHitResult) mc.crosshairTarget;
            RenderSystem.depthMask(false);
            RenderSystem.disableCull();
            RenderSystem.disableDepthTest();

            fi.dy.masa.malilib.render.RenderUtils.setupBlend();

            Color4f color = Configs.Generic.FLEXIBLE_PLACEMENT_OVERLAY_COLOR.getColor();

            fi.dy.masa.malilib.render.RenderUtils.renderBlockTargetingOverlay(
                    entity,
                    hitResult.getBlockPos(),
                    hitResult.getSide(),
                    hitResult.getPos(),
                    color,
                    posMatrix,
                    mc);

            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
            RenderSystem.enableCull();
            RenderSystem.depthMask(true);
        }
    }
}
