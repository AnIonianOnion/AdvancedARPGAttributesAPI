package com.anionianonion.advanced_arpg_attributes_api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

public class AdvancedARPGAttribute {

    private ResourceLocation rl;
    public enum ModifierType {ADDED, INCREASED, MORE };
    private Set<ModifierType> allowedModifierTypes;
    private Set<String> tags;
    private float baseValue;
    private boolean inheritBase = false;

    private UUID uuidForAddModifier;
    private UUID uuidForIncreaseModifier;
    private UUID uuidForMoreModifier;

    /**
     Constructor which specifies name, and customizing allowedModifier types, as well as automatic registration.
     */
    public AdvancedARPGAttribute(ResourceLocation rl, Set<ModifierType> allowedModifierTypes, Set<String> tags) {
        this.rl = rl;
        this.allowedModifierTypes = allowedModifierTypes;
        this.tags = tags;

        var registry = AdvancedARPGAttributesRegistry.get();
        if(!registry.containsKey(rl)) registry.put(rl, this);
    }

    public AdvancedARPGAttribute(ResourceLocation rl, Set<String> tags) {
        this.rl = rl;
        this.tags = tags;
        this.allowedModifierTypes = Set.of(ModifierType.ADDED, ModifierType.INCREASED, ModifierType.MORE);
        var registry = AdvancedARPGAttributesRegistry.get();
        if(!registry.containsKey(rl)) registry.put(rl, this);
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

    public float getBaseValue() {
        return baseValue;
    }

    public void setBaseValue(float baseValue) {
        this.baseValue = baseValue;
    }

    public void setBaseValue(Supplier<Float> baseValueSupplier) {
        this.baseValue = baseValueSupplier.get();
    }

    public boolean isInheritingBase() {
        return this.inheritBase;
    }

    public void setInheritBase(boolean b) {
        this.inheritBase = b;
    }

    public UUID getUuidForAddModifier() {
        return this.uuidForAddModifier;
    }
    public void setUuidForAddModifier(UUID uuidForAddModifier) {
        this.uuidForAddModifier = uuidForAddModifier;
    }
    public UUID getUuidForIncreaseModifier() {
        return uuidForIncreaseModifier;
    }
    public void setUuidForIncreaseModifier(UUID uuidForIncreaseModifier) {
        this.uuidForIncreaseModifier = uuidForIncreaseModifier;
    }
    public UUID getUuidForMoreModifier() {
        return this.uuidForMoreModifier;
    }
    public void setUuidForMoreModifier(UUID uuidForMoreModifier) {
        this.uuidForMoreModifier = uuidForMoreModifier;
    }

    public UUID getUUID(AttributeModifier.Operation operation) {
        return switch (operation) {
            case ADDITION -> this.uuidForAddModifier;
            case MULTIPLY_BASE -> this.uuidForIncreaseModifier;
            case MULTIPLY_TOTAL -> this.uuidForMoreModifier;
        };
    }

}
