package com.drewgifford.autocrafter.screen.crafter;

import com.drewgifford.autocrafter.AutoCrafter;
import com.drewgifford.autocrafter.inventory.CrafterSlot;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookProvider;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class CrafterScreen extends HandledScreen<CrafterScreenHandler> implements RecipeBookProvider {

    private static final Identifier TEXTURE = new Identifier(AutoCrafter.MOD_ID, "textures/gui/crafter_gui.png");
    private final RecipeBookWidget recipeBook = new RecipeBookWidget();
    private boolean narrow;

    public List<ButtonWidget> buttons;

    public CrafterScreen(CrafterScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();

        this.narrow = this.width < 379; // If the recipe book made the screen narrow
        initRecipeBook();

    }

    private void initRecipeBook(){

        this.recipeBook.initialize(
                this.width, this.height, this.client, this.narrow, this.handler
        );

        // Move the left side of the draw area to the left side of the recipe book
        this.x = this.recipeBook.findLeftEdge(this.width, this.backgroundWidth);

        this.addDrawableChild(new TexturedButtonWidget(this.x + 5, this.height / 2 - 49, 20, 18, RecipeBookWidget.BUTTON_TEXTURES, button -> {

            // Toggle recipe book
            this.recipeBook.toggleOpen();
            this.x = this.recipeBook.findLeftEdge(this.width, this.backgroundWidth);
            button.setPosition(this.x + 5, this.height / 2 - 49);

        }));

        this.addSelectableChild(this.recipeBook);
        this.setInitialFocus(this.recipeBook);

        this.titleX = 29;

    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {

        // If the recipe book is open and the screen is narrow
        if (this.recipeBook.isOpen() && this.narrow) {
            this.renderBackground(context, mouseX, mouseY, delta);
            this.recipeBook.render(context, mouseX, mouseY, delta);
        } else {
            super.render(context, mouseX, mouseY, delta);
            this.recipeBook.render(context, mouseX, mouseY, delta);
            this.recipeBook.drawGhostSlots(context, this.x, this.y, true, delta);
        }

        this.drawMouseoverTooltip(context, mouseX, mouseY);
        this.recipeBook.drawTooltip(context, this.x, this.y, mouseX, mouseY);

        this.renderTriggeredArrow(context);
        this.renderLockedSlots(context);
        this.renderTooltip(context, mouseX, mouseY);

    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY){

        int middleY = (this.height - this.backgroundHeight) / 2;

        // Draw main background texture
        context.drawTexture(TEXTURE, this.x, middleY, 0, 0, this.backgroundWidth, this.backgroundHeight);
    }

    static MutableText ENABLE_STRING = Text.translatable("gui."+AutoCrafter.MOD_ID+".crafter.button_enable");
    static MutableText DISABLE_STRING = Text.translatable("gui."+AutoCrafter.MOD_ID+".crafter.button_disable");

    private void renderLockedSlots(DrawContext context){

        DefaultedList<Boolean> lockedSlots = this.handler.getLockedSlots();

        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 3; ++x) {

                int slotIndex = x + y * 3;
                if(lockedSlots.get(slotIndex)){

                    int offsetX = 30 + x * 18 - 1;
                    int offsetY = 17 + y * 18 - 1;

                    context.drawTexture(TEXTURE, this.x + offsetX, this.y + offsetY, 177, 15, 18, 18);
                }
            }
        }
    }

    @Override
    protected boolean isPointWithinBounds(int x, int y, int width, int height, double pointX, double pointY) {
        return (!this.narrow || !this.recipeBook.isOpen()) && super.isPointWithinBounds(x, y, width, height, pointX, pointY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.recipeBook.mouseClicked(mouseX, mouseY, button)) {
            this.setFocused(this.recipeBook);
            return true;
        }
        if (this.narrow && this.recipeBook.isOpen()) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected boolean isClickOutsideBounds(double mouseX, double mouseY, int left, int top, int button) {
        boolean bl = mouseX < (double)left || mouseY < (double)top || mouseX >= (double)(left + this.backgroundWidth) || mouseY >= (double)(top + this.backgroundHeight);
        return this.recipeBook.isClickOutsideBounds(mouseX, mouseY, this.x, this.y, this.backgroundWidth, this.backgroundHeight, button) && bl;
    }

    @Override
    protected void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType) {
        super.onMouseClick(slot, slotId, button, actionType);

        this.recipeBook.slotClicked(slot);

        if(this.client == null) return;
        this.handleSlotClick(slot, slotId, button, actionType);
    }

    @Override
    public void refreshRecipeBook() {
        this.recipeBook.refresh();
    }

    @Override
    public RecipeBookWidget getRecipeBookWidget() {
        return this.recipeBook;
    }

    // NEW CODE

    private void renderTriggeredArrow(DrawContext context) {
        if(handler.isTriggered()){
            context.drawTexture(TEXTURE, this.x + 90, this.y + 35, 177, 0, 22, 15);
        }
    }


    private void handleSlotClick(Slot slot, int slotId, int button, SlotActionType actionType){

        /*if(!actionType.equals(SlotActionType.PICKUP)) return;

        // Slot verification

        AutoCrafter.LOGGER.info("Slot index: " + slot.getIndex());

        if(CrafterScreenHandler.isValidSlot(slot, this.handler)){
            boolean isLocked = this.handler.isLocked(slot.getIndex());
            this.handler.setLocked(slot.getIndex(), !isLocked);
        }*/

    }

    private void renderTooltip(DrawContext context, int mouseX, int mouseY){

        if(this.client == null) return;
        
        Slot slot = this.getSlotAt((float) mouseX, (float) mouseY);

        // Slot verification
        if(CrafterScreenHandler.isValidSlot(slot, this.handler)) {
            MutableText text = DISABLE_STRING;
            if (this.handler.isLocked(slot.getIndex())) text = ENABLE_STRING;
            context.drawTooltip(this.client.textRenderer, text, mouseX, mouseY);
        }
    }


    private Slot getSlotAt(double x, double y) {
        for (int i = 0; i < ((ScreenHandler)this.handler).slots.size(); ++i) {
            Slot slot = ((ScreenHandler)this.handler).slots.get(i);
            if (!this.isPointOverSlot(slot, x, y) || !slot.isEnabled()) continue;
            return slot;
        }
        return null;
    }

    private boolean isPointOverSlot(Slot slot, double pointX, double pointY) {
        return this.isPointWithinBounds(slot.x, slot.y, 16, 16, pointX, pointY);
    }

}