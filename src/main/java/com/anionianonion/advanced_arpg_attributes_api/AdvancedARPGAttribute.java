package com.anionianonion.advanced_arpg_attributes_api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class AdvancedARPGAttribute {

    private ResourceLocation rl;
    public enum ModifierType {ADDED, INCREASED, MORE };
    private Set<ModifierType> allowedModifierTypes;
    private Set<String> tags;
    private float baseValue;

    private static final HashMap<ResourceLocation, AdvancedARPGAttribute> advancedAttributesRegistry = new HashMap<>();
    private static final HashMap<Attribute, BiConsumer<Player, Float>> attributeCapFunctions = new HashMap<>();
    /**
     Constructor which specifies name, and customizing allowedModifier types, as well as automatic registration.
     */
    public AdvancedARPGAttribute(ResourceLocation rl, Set<ModifierType> allowedModifierTypes, Set<String> tags) {
        this.rl = rl;
        this.allowedModifierTypes = allowedModifierTypes;
        this.tags = tags;

        if(!advancedAttributesRegistry.containsKey(rl)) advancedAttributesRegistry.put(rl, this);
    }

    public AdvancedARPGAttribute(ResourceLocation rl, Set<String> tags) {
        this.rl = rl;
        this.tags = tags;
        this.allowedModifierTypes = Set.of(ModifierType.ADDED, ModifierType.INCREASED, ModifierType.MORE);
        if(!advancedAttributesRegistry.containsKey(rl)) advancedAttributesRegistry.put(rl, this);
    }

    public static void regAttribute(ResourceLocation rl, Set<ModifierType> allowedModifierTypes, Set<String> tags) {
        if(!advancedAttributesRegistry.containsKey(rl)) advancedAttributesRegistry.put(rl, new AdvancedARPGAttribute(rl, allowedModifierTypes, tags));
    }

    public static void regAttribute(ResourceLocation rl, Set<String> tags) {
        if(!advancedAttributesRegistry.containsKey(rl)) advancedAttributesRegistry.put(rl, new AdvancedARPGAttribute(rl, tags));
    }

    public static HashMap<ResourceLocation, AdvancedARPGAttribute> getAdvancedAttributesRegistry() {
        return advancedAttributesRegistry;
    }

    public Set<ModifierType> getAllowedModifierTypes() {
        return allowedModifierTypes;
    }

    public void setAllowedModifierTypes(Set<ModifierType> allowedModifierTypes) {
        this.allowedModifierTypes = allowedModifierTypes;
    }

    public ResourceLocation getRl() {
        return rl;
    }

    public void setRl(ResourceLocation rl) {
        this.rl = rl;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public static AdvancedARPGAttribute get(ResourceLocation id) {
        return advancedAttributesRegistry.get(id);
    }

    public float getBaseValue() {
        return baseValue;
    }

    public void setBaseValue(float baseValue) {
        this.baseValue = baseValue;
    }

    public void setBaseValue(Supplier<Float> baseValueSupplier) {
        this.baseValue = baseValueSupplier.get();
    }

    public static HashMap<Attribute, BiConsumer<Player, Float>> getAttributeCapFunctions() {
        return attributeCapFunctions;
    }
}
