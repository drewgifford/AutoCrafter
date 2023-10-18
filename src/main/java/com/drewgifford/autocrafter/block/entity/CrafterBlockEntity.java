package com.drewgifford.autocrafter.block.entity;

import com.drewgifford.autocrafter.AutoCrafter;
import com.drewgifford.autocrafter.block.ModBlocks;
import com.drewgifford.autocrafter.screen.crafter.CrafterScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BarrelBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.LecternBlock;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.ContainerLock;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
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
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

public class CrafterBlockEntity extends DispenserBlockEntity implements ExtendedScreenHandlerFactory, SidedInventory {

    private final DefaultedList<Boolean> lockedSlots = DefaultedList.ofSize(9, false);;


    public CrafterBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);

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

        if(world == null) return null;

        RecipeInputInventory inventory = getRecipeInventory();

        Optional<RecipeEntry<CraftingRecipe>> optional = world.getRecipeManager().getFirstMatch(RecipeType.CRAFTING, inventory, world);

        if(optional.isEmpty()) return null;

        RecipeEntry<CraftingRecipe> recipeEntry = optional.get();

        return recipeEntry.value();
    }

    public DefaultedList<ItemStack> getItems(){
        return this.getInvStackList();
    }

    public List<ItemStack> craft() {

        List<ItemStack> items = new ArrayList<>();
        CraftingRecipe recipe = getCurrentRecipe();


        if(recipe == null || world == null) return items;

        RecipeInputInventory inventory = getRecipeInventory();

        DefaultedList<ItemStack> remaining = recipe.getRemainder(inventory);
        ItemStack result = recipe.craft(inventory, world.getRegistryManager());

        if(!result.isEmpty()){

            items.addAll(remaining);

            for(ItemStack stack : this.getInvStackList()){
                stack.decrement(1);
            }

            this.markDirty();

        }

        items.add(result);

        return items;


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
        List<Integer> availableSlots = new ArrayList<>();

        for(int i = 0; i < this.getInvStackList().size(); i++){
            if(!this.lockedSlots.get(i)) availableSlots.add(i);
        }

        return availableSlots.stream().mapToInt(i->i).toArray();
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return this.getInvStackList().get(slot).isEmpty() && !this.lockedSlots.get(slot);
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return true;
    }

    public void setLocked(int slot, boolean value) {

        this.lockedSlots.set(slot, value);
        this.markDirty();

    }

    public int getComparatorOutput() {

        long value = this.getItems().stream().filter(stack -> !stack.isEmpty()).count();

        return MathHelper.clamp((int)value, 0, 9);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);

        if (nbt.contains("LockedSlots", NbtElement.INT_ARRAY_TYPE)) {

            List<Integer> list = Arrays.stream(nbt.getIntArray("LockedSlots")).boxed().toList();

            AutoCrafter.LOGGER.info("Loading Locked slots: " + Arrays.toString(list.toArray()));

            for(int i = 0; i < this.lockedSlots.size(); i++){
                this.setLocked(i, list.get(i) == 1);
            }
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);

        List<Integer> list = new ArrayList<>();

        for (boolean lockedSlot : this.lockedSlots) {
            list.add(lockedSlot ? 1 : 0);
        }

        AutoCrafter.LOGGER.info("Saving Locked slots: " + Arrays.toString(list.toArray()));

        if(!list.isEmpty()){
            nbt.putIntArray("LockedSlots", list);
        }
    }

}
