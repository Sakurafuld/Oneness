package com.sakurafuld.oneness.content;

import com.sakurafuld.oneness.content.oneness.OnenessTouchItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Function;

import static com.sakurafuld.oneness.Deets.ONENESS;
import static com.sakurafuld.oneness.Deets.TAB;

public class ModItems {
    public static final DeferredRegister<Item> REGISTRY =
            DeferredRegister.create(ForgeRegistries.ITEMS, ONENESS);

    public static final RegistryObject<Item> ONENESS_TOUCH;

    static {

        ONENESS_TOUCH = register("oneness_touch", OnenessTouchItem::new);

    }
    public static RegistryObject<Item> register(String name){
        return register(name, new Item.Properties().tab(TAB));
    }
    public static RegistryObject<Item> register(String name, Item.Properties prop){
        return REGISTRY.register(name, ()-> new Item(prop));
    }
    public static RegistryObject<Item> register(String name, Function<Item.Properties, ? extends Item> func){
        return REGISTRY.register(name, ()-> func.apply(new Item.Properties().tab(TAB)));
    }
}
