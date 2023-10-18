package com.drewgifford.autocrafter.block.entity;

import com.drewgifford.autocrafter.AutoCrafter;
import com.drewgifford.autocrafter.screen.crafter.CrafterScreenHandler;
import com.drewgifford.autocrafter.block.ModBlocks;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CrafterBlockEntity extends DispenserBlockEntity implements ExtendedScreenHandlerFactory, SidedInventory {

    private DefaultedList<ItemStack> output;
    private DefaultedList<Boolean> lockedSlots;

    public CrafterBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);

        this.output = DefaultedList.ofSize(1, ItemStack.EMPTY);
        this.lockedSlots = DefaultedList.ofSize(9, false);

        this.lockedSlots.set(3, true);
    }

    public CrafterBlockEntity(BlockPos pos, BlockState state){
        this(ModBlocks.CRAFTER_BLOCK_ENTITY, pos, state);
    }


    RecipeInputInventory getRecipeInventory(){
        return new CraftingInventoryWrapper(this.getInvStackList(), 3, 3);
    }

    @Nullable
    public CraftingRecipe getCurrentRecipe(){

        World world = this.getWorld();

        RecipeInputInventory inventory = getRecipeInventory();

        inventory.getInputStacks().forEach((itemStack) -> {
           AutoCrafter.LOGGER.info(itemStack.toString());
        });

        Optional<RecipeEntry<CraftingRecipe>> optional = world.getRecipeManager().getFirstMatch(RecipeType.CRAFTING, inventory, world);

        if(!optional.isPresent()) return null;

        RecipeEntry<CraftingRecipe> recipeEntry = optional.get();
        CraftingRecipe recipe = recipeEntry.value();

        AutoCrafter.LOGGER.info("Has recipe!");

        return recipe;
    }

    public boolean hasRecipe(){
        return getCurrentRecipe() != null;
    }

    public DefaultedList<ItemStack> getItems(){
        return this.getInvStackList();
    }

    public List<ItemStack> craft() {

        List<ItemStack> items = new ArrayList<ItemStack>();
        CraftingRecipe recipe = getCurrentRecipe();


        if(recipe == null) return items;

        RecipeInputInventory inventory = getRecipeInventory();

        DefaultedList<ItemStack> remaining = recipe.getRemainder(inventory);
        ItemStack result = recipe.craft(inventory, world.getRegistryManager());

        if(!result.isEmpty()){

            remaining.forEach((itemStack) -> {
                items.add(itemStack);
            });

            this.markDirty();

        }

        items.add(result);

        return items;


    }

    public DefaultedList<ItemStack> getOutput(){
        return this.output;
    }

    public void setOutput(DefaultedList<ItemStack> output){
        this.output = output;
    }

    public DefaultedList<Boolean> getLockedSlots(){
        return this.lockedSlots;
    }



    @Override
    protected Text getContainerName() {
        return Text.translatable("container." + AutoCrafter.MOD_ID + ".crafter" );
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new CrafterScreenHandler(syncId, playerInventory, this);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeBlockPos(this.pos);
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        List<Integer> availableSlots = new ArrayList<Integer>();

        for(int i = 0; i < this.getInvStackList().size(); i++){
            if(!this.lockedSlots.get(i)) availableSlots.add(i);
        }

        return availableSlots.stream().mapToInt(i->i).toArray();
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        if(this.getInvStackList().get(slot).isEmpty() && !this.lockedSlots.get(slot)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return true;
    }

    public void setLocked(int slot, boolean value) {
        this.lockedSlots.set(slot, value);
    }
}
