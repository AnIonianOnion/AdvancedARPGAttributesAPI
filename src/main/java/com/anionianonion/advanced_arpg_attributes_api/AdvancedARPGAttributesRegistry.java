package com.anionianonion.advanced_arpg_attributes_api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Set;
import java.util.function.BiConsumer;

public class AdvancedARPGAttributesRegistry {

    private static final HashMap<ResourceLocation, AdvancedARPGAttribute> advancedAttributesRegistry = new HashMap<>();
    private static final HashMap<Attribute, BiConsumer<Player, Float>> attributeCapFunctions = new HashMap<>();


    public static void regAttribute(ResourceLocation rl, Set<AdvancedARPGAttribute.ModifierType> allowedModifierTypes, Set<String> tags) {
        if(!advancedAttributesRegistry.containsKey(rl)) advancedAttributesRegistry.put(rl, new AdvancedARPGAttribute(rl, allowedModifierTypes, tags));
    }

    public static void regAttribute(ResourceLocation rl, Set<String> tags) {
        if(!advancedAttributesRegistry.containsKey(rl)) advancedAttributesRegistry.put(rl, new AdvancedARPGAttribute(rl, tags));
    }

    public static HashMap<ResourceLocation, AdvancedARPGAttribute> get() {
        return advancedAttributesRegistry;
    }

    public static HashMap<Attribute, BiConsumer<Player, Float>> getAttributeCapFunctions() {
        return attributeCapFunctions;
    }
}
