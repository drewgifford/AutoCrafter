package com.drewgifford.autocrafter.screen.crafter;

import com.drewgifford.autocrafter.AutoCrafter;
import com.drewgifford.autocrafter.block.CrafterBlock;
import com.drewgifford.autocrafter.block.entity.CrafterBlockEntity;
import com.drewgifford.autocrafter.inventory.CrafterSlot;
import com.drewgifford.autocrafter.inventory.InheritedCraftingInventory;
import com.drewgifford.autocrafter.inventory.LockedSlot;
import com.drewgifford.autocrafter.screen.ModScreens;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public class CrafterScreenHandler extends AbstractRecipeScreenHandler<RecipeInputInventory> {

    private final RecipeInputInventory input;
    private final Inventory result;
    public final CrafterBlockEntity blockEntity;
    private final PlayerEntity player;

    public CrafterScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf){
        this(syncId, playerInventory, playerInventory.player.getWorld().getBlockEntity(buf.readBlockPos()));
    }

    public CrafterScreenHandler(int syncId, PlayerInventory playerInventory, BlockEntity blockEntity) {
        super(ModScreens.CRAFTER_SCREEN_HANDLER, syncId);

        this.blockEntity = ((CrafterBlockEntity) blockEntity);

        // Maybe instead of passing in BlockEntity#getItems(), pass in the BlockEntity itself
        // and change the InheritedCraftingInventory to use that inventory instead, only providing
        // methods deriving from there? That should fix the comparator problem. you're doing great!
        this.input = new InheritedCraftingInventory(this.blockEntity, this);



        this.result = new SimpleInventory(1);
        this.player = playerInventory.player;

        this.addSlot(new LockedSlot(this.result, 0, 124, 35));

        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 3; ++x) {

                this.addSlot(new CrafterSlot(this.input, x + y * 3, 30 + x * 18, 17 + y * 18, false));

            }
        }
        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);



        this.updateResult();


    }

    protected void updateResult(){

        World world = this.blockEntity.getWorld();

        if(world == null) return;


        if(world.isClient()) return;



        CraftingRecipe recipe = this.blockEntity.getCurrentRecipe();


        if(recipe == null) {
            this.result.setStack(0, ItemStack.EMPTY);
        } else {
            this.result.setStack(0, recipe.getResult(world.getRegistryManager()));
        }
        this.result.markDirty();

    }

    @Override
    public void onContentChanged(Inventory inventory){
        this.updateResult();
    }



    public boolean isTriggered(){
        if(this.blockEntity.getWorld() == null) return false;

        return this.blockEntity.getWorld().getBlockState(this.blockEntity.getPos()).get(CrafterBlock.TRIGGERED);
    }

    public DefaultedList<Boolean> getLockedSlots(){
        return this.blockEntity.getLockedSlots();
    }

    public boolean isLocked(int slot){
        return this.blockEntity.getLockedSlots().get(slot);
    }
    public void setLocked(int slot, boolean value){
        this.blockEntity.setLocked(slot, value);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);

        if (slot.hasStack()) { // if the slot you are trying to move has a valid stack
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();

            if (invSlot < this.input.size()) {
                if (!this.insertItem(originalStack, this.input.size(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(originalStack, 0, this.input.size(), false)) {
                return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }

        return newStack;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.input.canPlayerUse(player);
    }

    private void addPlayerInventory(PlayerInventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(PlayerInventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    @Override
    public void populateRecipeFinder(RecipeMatcher finder) {
        this.input.provideRecipeInputs(finder);
    }

    @Override
    public void clearCraftingSlots() {
    }

    @Override
    public boolean matches(RecipeEntry<? extends Recipe<RecipeInputInventory>> recipe) {
        return recipe.value().matches(this.input, this.player.getWorld());
    }

    @Override
    public int getCraftingResultSlotIndex() {
        return 0;
    }

    @Override
    public int getCraftingWidth() {
        return this.input.getWidth();
    }

    @Override
    public int getCraftingHeight() {
        return this.input.getHeight();
    }

    @Override
    public int getCraftingSlotCount() {
        return 10;
    }

    @Override
    public RecipeBookCategory getCategory() {
        return RecipeBookCategory.CRAFTING;
    }

    @Override
    public boolean canInsertIntoSlot(int index) {
        return index != this.getCraftingResultSlotIndex();
    }

    public static boolean isValidSlot(Slot slot, ScreenHandler handler){
        if(slot == null) return false;
        if(!(slot instanceof CrafterSlot)) return false;
        if(slot.getIndex() >= 9 || slot.getIndex() < 0) return false;
        if(!slot.getStack().isEmpty()) return false;

        ItemStack cursorStack = handler.getCursorStack();
        return cursorStack.isEmpty();
    }

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        super.onSlotClick(slotIndex, button, actionType, player);

        // CORRECTION. DO NOT KEEP

        if(!actionType.equals(SlotActionType.PICKUP)) return;

        // Slot verification

        if(isValidSlot(slots.get(slotIndex), this)){
            slotIndex = slotIndex - 1; // Correction for weird behavior

            boolean isLocked = this.isLocked(slotIndex);
            this.setLocked(slotIndex, !isLocked);
        }

    }
}
