package com.sakurafuld.oneness.content;

import com.sakurafuld.oneness.Deets;
import com.sakurafuld.oneness.content.oneness.OnenessBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> REGISTRY =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, Deets.ONENESS);

    public static final RegistryObject<BlockEntityType<OnenessBlockEntity>> ONENESS;

    static {

        ONENESS = REGISTRY.register("oneness", () -> BlockEntityType.Builder.of(OnenessBlockEntity::new, ModBlocks.ONENESS.get()).build(null));

    }
}
