package com.shiver.polymorphlegacy.compat;

import java.util.Collections;
import java.util.List;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import zone.rong.mixinbooter.ILateMixinLoader;

public final class CompatibilityLoader implements ILateMixinLoader {
    @Override
    public List<String> getMixinConfigs() {
        return Collections.singletonList("mixins.polymorph_legacy.jei.json");
    }

    @Override
    public boolean shouldMixinConfigQueue(String config) {
        return FMLLaunchHandler.side().isClient() && Loader.isModLoaded("jei");
    }
}
