package com.anionianonion.advanced_arpg_attributes_api.api;


import com.anionianonion.advanced_arpg_attributes_api.AdvancedARPGAttribute;
import com.anionianonion.advanced_arpg_attributes_api.StatContainer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class AdvancedARPGAttributesAPI {

    private static final Set<String> validTags = new HashSet<>();
    private static final HashMap<Class<? extends Item>, String> classesOfValidWeaponItemClassesToWeaponTags = new HashMap<>();

    public void registerTag(String newTag) {
        if(newTag != null) validTags.add(newTag);
    }

    public Set<String> getValidTags() {
        return validTags;
    }

    public Set<String> getValidWeapons() {
        return classesOfValidWeaponItemClassesToWeaponTags.values()
                .stream()
                .filter(tag -> !tag.isEmpty())
                .collect(Collectors.toSet());
    }

    public void validateAttributes() {
        var entries = new HashSet<>(AdvancedARPGAttribute.getAdvancedAttributesRegistry().entrySet());
        for(var attributeEntry : entries) {
            if(!validTags.containsAll(attributeEntry.getValue().getTags())) AdvancedARPGAttribute.getAdvancedAttributesRegistry().remove(attributeEntry.getKey());
        }
    }

    public void regAttribute(ResourceLocation rl, Set<AdvancedARPGAttribute.ModifierType> allowedModifierTypes, Set<String> tags) {
        AdvancedARPGAttribute.regAttribute(rl, allowedModifierTypes, tags);
    }

    public void regAttribute(ResourceLocation rl, Set<String> tags) {
        AdvancedARPGAttribute.regAttribute(rl, tags);
    }

    public HashMap<ResourceLocation, AdvancedARPGAttribute> getRegistry() {
        return AdvancedARPGAttribute.getAdvancedAttributesRegistry();
    }

    public void addPlayerExecutedFunctionToAttribute(Attribute a, BiConsumer<Player, Float> function) {
        AdvancedARPGAttribute.getAttributeCapFunctions().put(a, function);
    }

    public void registerWeaponClassAndTag(Class<? extends Item> itemClass, String tag) {
        classesOfValidWeaponItemClassesToWeaponTags.put(itemClass, tag);
    }

    public HashMap<Class<? extends Item>, String> getClassesOfWeaponItemsToTag() {
        return classesOfValidWeaponItemClassesToWeaponTags;
    }

    public float getResult(StatContainer statContainer, Set<ResourceLocation> filteredAttributeIds) {
        float add = 0;
        float increase = 0;
        float more = 1;

        for(var attributeId : filteredAttributeIds) {
            var allowedMods = AdvancedARPGAttribute.get(attributeId).getAllowedModifierTypes();

            if(allowedMods.contains(AdvancedARPGAttribute.ModifierType.ADDED)) {
                add += AdvancedARPGAttribute.get(attributeId).getBaseValue();

                for(var modifier : statContainer.getAddedModifiers().get(attributeId)) {
                    assert modifier != null;
                    float amount = (float) modifier.getAmount();
                    add += amount;
                }
            }

            if(allowedMods.contains(AdvancedARPGAttribute.ModifierType.INCREASED)) {
                for(var modifier : statContainer.getIncreaseModifiers().get(attributeId)) {
                    assert modifier != null;
                    float amount = (float) modifier.getAmount();
                    increase += amount;
                }
            }

            if(allowedMods.contains(AdvancedARPGAttribute.ModifierType.MORE)) {
                for(var modifier : statContainer.getMoreModifiers().get(attributeId)) {
                    assert modifier != null;
                    float amount = (float) modifier.getAmount();
                    more *= (1 + amount);
                }
            }
        }

        return add * (1 + increase) * more;

    }

    public float getResultOfSingleAttribute(StatContainer statContainer, ResourceLocation attributeId) {
        float add = 0;
        float increase = 0;
        float more = 1;

        var allowedMods = AdvancedARPGAttribute.get(attributeId).getAllowedModifierTypes();

        if(allowedMods.contains(AdvancedARPGAttribute.ModifierType.ADDED)) {
            add += AdvancedARPGAttribute.get(attributeId).getBaseValue();

            for(var modifier : statContainer.getAddedModifiers().get(attributeId)) {
                assert modifier != null;
                float amount = (float) modifier.getAmount();
                add += amount;
            }
        }

        if(allowedMods.contains(AdvancedARPGAttribute.ModifierType.INCREASED)) {
            for(var modifier : statContainer.getIncreaseModifiers().get(attributeId)) {
                assert modifier != null;
                float amount = (float) modifier.getAmount();
                increase += amount;
            }
        }

        if(allowedMods.contains(AdvancedARPGAttribute.ModifierType.MORE)) {
            for(var modifier : statContainer.getMoreModifiers().get(attributeId)) {
                assert modifier != null;
                float amount = (float) modifier.getAmount();
                more *= (1 + amount);
            }
        }
        var result = add * (1 + increase) * more;
        var cap = statContainer.getLockedAttributeValue(attributeId);
        if(cap == null) return result;
        return Math.min(result, cap);
    }

    public float[] getData(StatContainer statContainer, Set<ResourceLocation> filteredAttributeIds) {
        float[] data = new float[3];

        float add = 0;
        float increase = 0;
        float more = 1;

        for(var attributeId : filteredAttributeIds) {
            var allowedMods = AdvancedARPGAttribute.get(attributeId).getAllowedModifierTypes();

            if(allowedMods.contains(AdvancedARPGAttribute.ModifierType.ADDED)) {
                for(var modifier : statContainer.getAddedModifiers().get(attributeId)) {
                    assert modifier != null;
                    float amount = (float) modifier.getAmount();
                    add += amount;
                }
            }

            if(allowedMods.contains(AdvancedARPGAttribute.ModifierType.INCREASED)) {
                for(var modifier : statContainer.getIncreaseModifiers().get(attributeId)) {
                    assert modifier != null;
                    float amount = (float) modifier.getAmount();
                    increase += amount;
                }
            }

            if(allowedMods.contains(AdvancedARPGAttribute.ModifierType.MORE)) {
                for(var modifier : statContainer.getMoreModifiers().get(attributeId)) {
                    assert modifier != null;
                    float amount = (float) modifier.getAmount();
                    more *= (1 + amount);
                }
            }
        }

        data[0] = add;
        data[1] = increase;
        data[2] = more - 1;
        return data;

    }

    public Set<ResourceLocation> getFilteredAttributes(String... tags) {
        return getFilteredAttributes(Set.of(tags));
    }

    public Set<ResourceLocation> getFilteredAttributes(Set<String> tags) {
        var attributeEntries = AdvancedARPGAttribute.getAdvancedAttributesRegistry().entrySet();
        Set<ResourceLocation> filtered = new HashSet<>();

        for(var attributeEntry : attributeEntries) {
            var attributeKey = attributeEntry.getKey();
            var attribute = attributeEntry.getValue();
            var requiredTags = attribute.getTags();

            //required tags is a subset of tags on an attribute that tags must have in order for that attribute to be considered using.
            if(tags.containsAll(requiredTags)) filtered.add(attributeKey);
        }
        return filtered;
    }
}
