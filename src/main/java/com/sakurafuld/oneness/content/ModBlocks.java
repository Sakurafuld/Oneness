package com.sakurafuld.oneness.content;

import com.sakurafuld.oneness.Deets;
import com.sakurafuld.oneness.content.oneness.OnenessBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> REGISTRY =
            DeferredRegister.create(ForgeRegistries.BLOCKS, Deets.ONENESS);

    public static final RegistryObject<Block> ONENESS;


    static {

        ONENESS = register("oneness",
                () -> new OnenessBlock(BlockBehaviour.Properties.of(Material.METAL).strength(20.0F, 600.0F).noOcclusion().requiresCorrectToolForDrops()));

    }

    public static <T extends Block> RegistryObject<T> register(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = REGISTRY.register(name, block);
        ModItems.REGISTRY.register(name, () -> new BlockItem(toReturn.get(), new Item.Properties().tab(Deets.TAB)));

        return toReturn;
    }
    public static <T extends Block> RegistryObject<T> register(String name, Supplier<T> block, BiFunction<T, Item.Properties, ? extends BlockItem> item){
        RegistryObject<T> toReturn = REGISTRY.register(name, block);
        ModItems.REGISTRY.register(name, () -> item.apply(toReturn.get(), new Item.Properties().tab(Deets.TAB)));
        return toReturn;
    }
    //タグ定義のバグ防止.
    public static RegistryObject<Block> dummy(String id) {
        return REGISTRY.register(id, () -> new Block(BlockBehaviour.Properties.of(Material.AIR)));
    }
}
