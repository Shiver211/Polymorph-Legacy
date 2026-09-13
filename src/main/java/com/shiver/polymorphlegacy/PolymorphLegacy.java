package com.shiver.polymorphlegacy;

import com.shiver.polymorphlegacy.network.PolymorphNetwork;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION,
        acceptedMinecraftVersions = "[1.12.2]", dependencies = "required-after:mixinbooter")
public final class PolymorphLegacy {
    @SidedProxy(clientSide = "com.shiver.polymorphlegacy.client.ClientProxy",
            serverSide = "com.shiver.polymorphlegacy.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        PolymorphNetwork.init();
        proxy.init();
    }
}
