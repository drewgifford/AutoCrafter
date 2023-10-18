package com.drewgifford.autocrafter.screen;

import com.drewgifford.autocrafter.AutoCrafter;
import com.drewgifford.autocrafter.screen.crafter.CrafterScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public class ModScreens {

    public static final ScreenHandlerType<CrafterScreenHandler> CRAFTER_SCREEN_HANDLER = Registry.register(
            Registries.SCREEN_HANDLER, new Identifier(AutoCrafter.MOD_ID, "crafter"),
            new ExtendedScreenHandlerType<>(CrafterScreenHandler::new));

    public static void registerScreenHandlers(){
        AutoCrafter.LOGGER.info("Registering screen handlers for " + AutoCrafter.MOD_ID);
    }


}
