package fi.dy.masa.tweakeroo.tweaks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import com.google.common.collect.ImmutableSet;

import net.minecraft.block.*;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.ComparatorMode;
import net.minecraft.block.enums.SlabType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import fi.dy.masa.tweakeroo.Tweakeroo;
import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.data.DataManager;
import fi.dy.masa.tweakeroo.util.EasyPlacementProtocol;

public class PlacementHandler
{
    public static final ImmutableSet<Property<?>> WHITELISTED_PROPERTIES = ImmutableSet.of(
            // BooleanProperty:
            // INVERTED - DaylightDetector
            // OPEN - Barrel, Door, FenceGate, Trapdoor
            // PERSISTENT - Leaves (Disabled)
            Properties.INVERTED,
            Properties.OPEN,
            //Properties.PERSISTENT,
            // TODO --> TEST (Boolean)
            Properties.POWERED,
            Properties.LOCKED,
            //Properties.WATERLOGGED
            // EnumProperty:
            // ATTACHMENT - Bells
            // AXIS - Pillar
            // BLOCK_HALF - Stairs, Trapdoor
            // BLOCK_FACE - Button, Grindstone, Lever
            // CHEST_TYPE - Chest
            // COMPARATOR_MODE - Comparator
            // DOOR_HINGE - Door
            // ORIENTATION - Crafter
            // RAIL_SHAPE / STRAIGHT_RAIL_SHAPE - Rails
            // SLAB_TYPE - Slab - PARTIAL ONLY: TOP and BOTTOM, not DOUBLE
            // STAIR_SHAPE - Stairs (needed to get the correct state, otherwise the player facing would be a factor)
            // BLOCK_FACE - Button, Grindstone, Lever
            Properties.ATTACHMENT,
            Properties.AXIS,
            Properties.BLOCK_HALF,
            Properties.BLOCK_FACE,
            Properties.CHEST_TYPE,
            Properties.COMPARATOR_MODE,
            Properties.DOOR_HINGE,
            Properties.FACING,
            Properties.HOPPER_FACING,
            Properties.HORIZONTAL_FACING,
            Properties.ORIENTATION,
            Properties.RAIL_SHAPE,
            Properties.STRAIGHT_RAIL_SHAPE,
            Properties.SLAB_TYPE,
            Properties.STAIR_SHAPE,
            // IntProperty:
            // BITES - Cake
            // DELAY - Repeater
            // NOTE - NoteBlock
            // ROTATION - Banner, Sign, Skull
            Properties.BITES,
            Properties.DELAY,
            Properties.NOTE,
            Properties.ROTATION
    );

    public static EasyPlacementProtocol getEffectiveProtocolVersion()
    {
        EasyPlacementProtocol protocol = (EasyPlacementProtocol) Configs.Generic.ACCURATE_PLACEMENT_PROTOCOL_MODE.getOptionListValue();

        if (protocol == EasyPlacementProtocol.AUTO)
        {
            if (MinecraftClient.getInstance().isInSingleplayer() || DataManager.getInstance().hasServuxServer())
            {
                return EasyPlacementProtocol.V3;
            }

            if (DataManager.getInstance().hasCarpetServer())
            {
                return EasyPlacementProtocol.V2;
            }

            return EasyPlacementProtocol.SLAB_ONLY;
        }

        return protocol;
    }

    @Nullable
    public static BlockState applyPlacementProtocolToPlacementState(BlockState state, UseContext context)
    {
        EasyPlacementProtocol protocol = getEffectiveProtocolVersion();

        if (protocol == EasyPlacementProtocol.V3)
        {
            return applyPlacementProtocolV3(state, context);
        }
        else if (protocol == EasyPlacementProtocol.V2)
        {
            return applyPlacementProtocolV2(state, context);
        }
        else
        {
            return state;
        }
    }

    public static BlockState applyPlacementProtocolV2(BlockState state, UseContext context)
    {
        int protocolValue = (int) (context.getHitVec().x - (double) context.getPos().getX()) - 2;

        if (protocolValue < 0)
        {
            return state;
        }

        Optional<DirectionProperty> property = fi.dy.masa.malilib.util.game.BlockUtils.getFirstDirectionProperty(state);

        if (property.isPresent())
        {
            state = applyDirectionProperty(state, context, property.get(), protocolValue);

            if (state == null)
            {
                return null;
            }
        }
        else if (state.contains(Properties.AXIS))
        {
            Direction.Axis axis = Direction.Axis.VALUES[((protocolValue >> 1) & 0x3) % 3];

            if (Properties.AXIS.getValues().contains(axis))
            {
                state = state.with(Properties.AXIS, axis);
            }
        }

        // Divide by two, and then remove the 4 bits used for the facing
        protocolValue >>>= 5;

        if (protocolValue > 0)
        {
            Block block = state.getBlock();

            if (block instanceof RepeaterBlock)
            {
                Integer delay = protocolValue;

                if (RepeaterBlock.DELAY.getValues().contains(delay))
                {
                    state = state.with(RepeaterBlock.DELAY, delay);
                }
            }
            else if (block instanceof ComparatorBlock)
            {
                state = state.with(ComparatorBlock.MODE, ComparatorMode.SUBTRACT);
            }
        }

        if (state.contains(Properties.BLOCK_HALF))
        {
            state = state.with(Properties.BLOCK_HALF, protocolValue > 0 ? BlockHalf.TOP : BlockHalf.BOTTOM);
        }

        return state;
    }

    public static <T extends Comparable<T>> BlockState applyPlacementProtocolV3(BlockState state, UseContext context)
    {
        int protocolValue = (int) (context.getHitVec().x - (double) context.getPos().getX()) - 2;
        BlockState oldState = state;
        //System.out.printf("hit vec.x %s, pos.x: %s\n", context.getHitVec().getX(), context.getPos().getX());
        //System.out.printf("raw protocol value in: 0x%08X\n", protocolValue);

        if (protocolValue < 0)
        {
            return oldState;
        }

        Optional<DirectionProperty> property = fi.dy.masa.malilib.util.game.BlockUtils.getFirstDirectionProperty(state);

        // DirectionProperty - allow all except: VERTICAL_DIRECTION (PointedDripstone)
        if (property.isPresent() && property.get() != Properties.VERTICAL_DIRECTION)
        {
            //System.out.printf("applying: 0x%08X\n", protocolValue);
            state = applyDirectionProperty(state, context, property.get(), protocolValue);

            if (state == null)
            {
                return null;
            }

            if (state.canPlaceAt(context.getWorld(), context.getPos()))
            {
                //System.out.printf("validator passed for \"%s\"\n", property.getName());
                oldState = state;
            }
            else
            {
                //System.out.printf("validator failed for \"%s\"\n", property.getName());
                state = oldState;
            }

            // Consume the bits used for the facing
            protocolValue >>>= 3;
        }
        // Consume the lowest unused bit
        protocolValue >>>= 1;

        List<Property<?>> propList = new ArrayList<>(state.getBlock().getStateManager().getProperties());
        propList.sort(Comparator.comparing(Property::getName));

        try
        {
            for (Property<?> p : propList)
            {
                if ((property.isPresent() && !property.get().equals(p)) ||
                    (property.isEmpty()) &&
                    WHITELISTED_PROPERTIES.contains(p))
                {
                    @SuppressWarnings("unchecked")
                    Property<T> prop = (Property<T>) p;
                    List<T> list = new ArrayList<>(prop.getValues());
                    list.sort(Comparable::compareTo);

                    int requiredBits = MathHelper.floorLog2(MathHelper.smallestEncompassingPowerOfTwo(list.size()));
                    int bitMask = ~(0xFFFFFFFF << requiredBits);
                    int valueIndex = protocolValue & bitMask;
                    //System.out.printf("trying to apply valInd: %d, bits: %d, prot val: 0x%08X\n", valueIndex, requiredBits, protocolValue);

                    if (valueIndex >= 0 && valueIndex < list.size())
                    {
                        T value = list.get(valueIndex);

                        if (state.get(prop).equals(value) == false &&
                            value != SlabType.DOUBLE) // don't allow duping slabs by forcing a double slab via the protocol
                        {
                            //System.out.printf("applying \"%s\": %s\n", prop.getName(), value);
                            state = state.with(prop, value);

                            if (state.canPlaceAt(context.getWorld(), context.getPos()))
                            {
                                //System.out.printf("validator passed for \"%s\"\n", prop.getName());
                                oldState = state;
                            }
                            else
                            {
                                //System.out.printf("validator failed for \"%s\"\n", prop.getName());
                                state = oldState;
                            }
                        }

                        protocolValue >>>= requiredBits;
                    }
                }
            }
        }
        catch (Exception e)
        {
            Tweakeroo.logger.warn("Exception trying to apply placement protocol value", e);
        }

        if (state.canPlaceAt(context.getWorld(), context.getPos()))
        {
            //System.out.printf("validator passed for \"%s\"\n", state);
            return state;
        }
        else
        {
            //System.out.printf("validator failed for \"%s\"\n", state);
            return null;
        }
    }

    private static BlockState applyDirectionProperty(BlockState state, UseContext context,
                                                     DirectionProperty property, int protocolValue)
    {
        Direction facingOrig = state.get(property);
        Direction facing = facingOrig;
        int decodedFacingIndex = (protocolValue & 0xF) >> 1;

        if (decodedFacingIndex == 6) // the opposite of the normal facing requested
        {
            facing = facing.getOpposite();
        }
        else if (decodedFacingIndex >= 0 && decodedFacingIndex <= 5)
        {
            facing = Direction.byId(decodedFacingIndex);

            if (property.getValues().contains(facing) == false)
            {
                facing = context.getEntity().getHorizontalFacing().getOpposite();
            }
        }

        //System.out.printf("plop facing: %s -> %s (raw: %d, dec: %d)\n", facingOrig, facing, protocolValue, decodedFacingIndex);

        if (facing != facingOrig && property.getValues().contains(facing))
        {
            if (state.getBlock() instanceof BedBlock)
            {
                BlockPos headPos = context.pos.offset(facing);
                ItemPlacementContext ctx = context.getItemPlacementContext();

                if (context.getWorld().getBlockState(headPos).canReplace(ctx) == false)
                {
                    return null;
                }
            }

            state = state.with(property, facing);
        }

        return state;
    }

    public static class UseContext
    {
        private final World world;
        private final BlockPos pos;
        private final Direction side;
        private final Vec3d hitVec;
        private final LivingEntity entity;
        private final Hand hand;
        @Nullable private final ItemPlacementContext itemPlacementContext;

        private UseContext(World world, BlockPos pos, Direction side, Vec3d hitVec,
                           LivingEntity entity, Hand hand, @Nullable ItemPlacementContext itemPlacementContext)
        {
            this.world = world;
            this.pos = pos;
            this.side = side;
            this.hitVec = hitVec;
            this.entity = entity;
            this.hand = hand;
            this.itemPlacementContext = itemPlacementContext;
        }

        /*
        public static UseContext of(World world, BlockPos pos, Direction side, Vec3d hitVec, LivingEntity entity, Hand hand)
        {
            return new UseContext(world, pos, side, hitVec, entity, hand, null);
        }
        */

        public static UseContext from(ItemPlacementContext ctx, Hand hand)
        {
            Vec3d pos = ctx.getHitPos();
            return new UseContext(ctx.getWorld(), ctx.getBlockPos(), ctx.getSide(), new Vec3d(pos.x, pos.y, pos.z),
                                  ctx.getPlayer(), hand, ctx);
        }

        public World getWorld()
        {
            return this.world;
        }

        public BlockPos getPos()
        {
            return this.pos;
        }

        public Direction getSide()
        {
            return this.side;
        }

        public Vec3d getHitVec()
        {
            return this.hitVec;
        }

        public LivingEntity getEntity()
        {
            return this.entity;
        }

        public Hand getHand()
        {
            return this.hand;
        }

        @Nullable
        public ItemPlacementContext getItemPlacementContext()
        {
            return this.itemPlacementContext;
        }
    }
}
