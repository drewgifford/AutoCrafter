package com.drewgifford.autocrafter.block;

import com.drewgifford.autocrafter.AutoCrafter;
import com.drewgifford.autocrafter.block.entity.CrafterBlockEntity;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.block.entity.BlockEntityType;

public class ModBlocks {

    public static final Block CRAFTER = registerBlock("crafter",
            new CrafterBlock(FabricBlockSettings.copyOf(Blocks.DROPPER)));

    public static final BlockEntityType<CrafterBlockEntity> CRAFTER_BLOCK_ENTITY = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            new Identifier(AutoCrafter.MOD_ID, "crafter_block_entity"),
            FabricBlockEntityTypeBuilder.create(CrafterBlockEntity::new, CRAFTER).build()
    );

    public static Block registerBlock(String name, Block block){
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, new Identifier(AutoCrafter.MOD_ID, name), block);
    }

    private static Item registerBlockItem(String name, Block block){
        return Registry.register(Registries.ITEM, new Identifier(AutoCrafter.MOD_ID, name),
                new BlockItem(block, new FabricItemSettings()));
    }


    public static void registerBlocks(){
        AutoCrafter.LOGGER.info("Registering Blocks for " + AutoCrafter.MOD_ID);
    }

}
