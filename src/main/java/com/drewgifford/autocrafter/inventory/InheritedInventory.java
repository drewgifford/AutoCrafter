package com.drewgifford.autocrafter.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.collection.DefaultedList;

import java.util.Collection;
import java.util.Iterator;

public class InheritedInventory implements Inventory {

    public DefaultedList<ItemStack> stacks;
    private ScreenHandler handler;

    public InheritedInventory(DefaultedList<ItemStack> stacks){
        this(stacks, null);
    }

    public InheritedInventory(DefaultedList<ItemStack> stacks, ScreenHandler handler){
        this.stacks = stacks;
        this.handler = handler;
    }

    public DefaultedList<ItemStack> getItems() {
        return this.stacks;
    }

    @Override
    public boolean isEmpty() {
        Iterator var1 = this.stacks.iterator();

        ItemStack itemStack;
        do {
            if (!var1.hasNext()) {
                return true;
            }

            itemStack = (ItemStack)var1.next();
        } while(itemStack.isEmpty());

        return false;
    }

    @Override
    public ItemStack getStack(int slot) {
        return slot >= this.size() ? ItemStack.EMPTY : (ItemStack)this.stacks.get(slot);
    }

    @Override
    public int size() {
        return this.stacks.size();
    }

    @Override
    public ItemStack removeStack(int slot) {
        return Inventories.removeStack(this.stacks, slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack itemStack = Inventories.splitStack(this.stacks, slot, amount);
        if (!itemStack.isEmpty()) {
            if(this.handler != null){
                this.handler.onContentChanged(this);
            }
        }

        return itemStack;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        this.stacks.set(slot, stack);
        if(this.handler != null){
            this.handler.onContentChanged(this);
        }
    }

    @Override
    public void markDirty() {

    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void clear() {
        this.stacks = DefaultedList.ofSize(this.stacks.size(), ItemStack.EMPTY);
    }
}
