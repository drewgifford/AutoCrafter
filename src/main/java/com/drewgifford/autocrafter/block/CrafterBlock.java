package com.drewgifford.autocrafter.block;

import com.drewgifford.autocrafter.AutoCrafter;
import com.drewgifford.autocrafter.block.entity.CrafterBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.client.gui.screen.ingame.CraftingScreen;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;

import java.util.List;

public class CrafterBlock extends DispenserBlock implements BlockEntityProvider {

    private static final DispenserBehavior BEHAVIOR = new ItemDispenserBehavior(){
        @Override
        protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
            Direction direction = (Direction)pointer.state().get(DispenserBlock.FACING);
            Position position = DispenserBlock.getOutputLocation(pointer);
            spawnItem(pointer.world(), stack, 6, direction, position);
            return stack;
        }
    };

    public CrafterBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected void dispense(ServerWorld world, BlockState state, BlockPos pos) {

        AutoCrafter.LOGGER.info("DISPENSING!");

        CrafterBlockEntity crafterBlockEntity = world.getBlockEntity(pos, ModBlocks.CRAFTER_BLOCK_ENTITY).orElse(null);
        if (crafterBlockEntity == null) {
            return;
        }

        BlockPointer blockPointer = new BlockPointer(world, pos, state, crafterBlockEntity);

        List<ItemStack> itemStacks = crafterBlockEntity.craft();

        if(itemStacks.size() == 0){
            world.syncWorldEvent(WorldEvents.DISPENSER_FAILS, pos, 0);
        } else {
            Direction direction = world.getBlockState(pos).get(FACING);
            Inventory inventory = HopperBlockEntity.getInventoryAt(world, pos.offset(direction));

            if (inventory == null) {
                itemStacks.forEach((itemStack) -> {
                    BEHAVIOR.dispense(blockPointer, itemStack);
                });

            } else {
                itemStacks.forEach((itemStack) -> {
                    HopperBlockEntity.transfer(crafterBlockEntity, inventory, itemStack, direction.getOpposite());
                });
            }
        }


    }

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        CrafterBlockEntity entity = (CrafterBlockEntity) world.getBlockEntity(pos);

        if (entity.hasRecipe()) {
            return 15;
        } else {
            return 0;
        }
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new CrafterBlockEntity(pos, state);
    }

}
