package com.drewgifford.autocrafter.inventory;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class CrafterSlot extends Slot {

    private boolean locked;

    public CrafterSlot(Inventory inventory, int index, int x, int y, boolean locked) {
        super(inventory, index, x, y);

        this.locked = locked;
    }

    public boolean isLocked(){
        return this.locked;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return (!this.isLocked());
    }
}
