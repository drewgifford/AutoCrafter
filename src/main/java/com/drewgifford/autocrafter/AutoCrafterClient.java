package com.drewgifford.autocrafter;

import com.drewgifford.autocrafter.screen.crafter.CrafterScreen;
import com.drewgifford.autocrafter.screen.ModScreens;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class AutoCrafterClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        HandledScreens.register(ModScreens.CRAFTER_SCREEN_HANDLER, CrafterScreen::new);
    }

}
