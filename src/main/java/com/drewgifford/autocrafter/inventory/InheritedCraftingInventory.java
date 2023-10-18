package com.drewgifford.autocrafter.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.collection.DefaultedList;

import java.util.Iterator;

public class InheritedCraftingInventory extends CraftingInventory implements Inventory {

    public Inventory inventory;
    private final ScreenHandler handler;

    public InheritedCraftingInventory(Inventory inventory, ScreenHandler handler){
        super(handler, 3, 3);
        this.inventory = inventory;
        this.handler = handler;
    }

    @Override
    public boolean isEmpty() {
        return this.inventory.isEmpty();
    }

    @Override
    public ItemStack getStack(int slot) {
        return this.inventory.getStack(slot);
    }

    @Override
    public int size() {
        return this.inventory.size();
    }

    @Override
    public ItemStack removeStack(int slot) {
        return this.inventory.removeStack(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {

        ItemStack stack = this.inventory.removeStack(slot, amount);

        if(!stack.isEmpty()){
            if(this.handler != null){
                this.handler.onContentChanged(this);
            }
        };

        return stack;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {

        this.inventory.setStack(slot, stack);

        if(this.handler != null){
            this.handler.onContentChanged(this);
        }
    }

    @Override
    public void markDirty() {
        this.inventory.markDirty();
        if(this.handler != null){
            this.handler.onContentChanged(this);
        }

    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    @Override
    public void clear() {
        this.inventory.clear();
    }
}
