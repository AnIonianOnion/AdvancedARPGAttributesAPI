package com.anionianonion.advanced_arpg_attributes_api.capability;

import com.anionianonion.advanced_arpg_attributes_api.AdvancedARPGAttributesMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AdvancedARPGAttributesMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CapabilityEvents {

    @SubscribeEvent
    public static void onAttachLivingEntityCapabilities(AttachCapabilitiesEvent<Entity> event) {
        Entity entity = event.getObject();

        if (entity instanceof LivingEntity) {
            event.addCapability(ResourceLocation.fromNamespaceAndPath(AdvancedARPGAttributesMod.MOD_ID, "stat_container"), new StatContainerProvider());
        }
    }
}
