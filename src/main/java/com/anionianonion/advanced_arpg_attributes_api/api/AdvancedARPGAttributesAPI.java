package com.anionianonion.advanced_arpg_attributes_api.api;


import com.anionianonion.advanced_arpg_attributes_api.AdvancedARPGAttribute;
import com.anionianonion.advanced_arpg_attributes_api.AdvancedARPGAttributesMod;
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

    public static void registerTag(String newTag) {
        if(newTag != null) validTags.add(newTag);
    }

    public static Set<String> getValidTags() {
        return validTags;
    }

    public static Set<String> getValidWeapons() {
        return classesOfValidWeaponItemClassesToWeaponTags.values()
                .stream()
                .filter(tag -> !tag.isEmpty())
                .collect(Collectors.toSet());
    }

    public static void validateAttributes() {
        var entries = new HashSet<>(AdvancedARPGAttribute.getAdvancedAttributesRegistry().entrySet());
        for(var attributeEntry : entries) {
            if(!validTags.containsAll(attributeEntry.getValue().getTags())) AdvancedARPGAttribute.getAdvancedAttributesRegistry().remove(attributeEntry.getKey());
        }
    }

    public static void regAttribute(ResourceLocation rl, Set<AdvancedARPGAttribute.ModifierType> allowedModifierTypes, Set<String> tags) {
        AdvancedARPGAttribute.regAttribute(rl, allowedModifierTypes, tags);
    }

    public static void regAttribute(ResourceLocation rl, Set<String> tags) {
        AdvancedARPGAttribute.regAttribute(rl, tags);
    }

    public static HashMap<ResourceLocation, AdvancedARPGAttribute> getRegistry() {
        return AdvancedARPGAttribute.getAdvancedAttributesRegistry();
    }

    public static void addPlayerExecutedFunctionToAttribute(Attribute a, BiConsumer<Player, Float> function) {
        AdvancedARPGAttribute.getAttributeCapFunctions().put(a, function);
    }

    public static void registerWeaponClassAndTag(Class<? extends Item> itemClass, String tag) {
        classesOfValidWeaponItemClassesToWeaponTags.put(itemClass, tag);
    }

    public static HashMap<Class<? extends Item>, String> getClassesOfWeaponItemsToTag() {
        return classesOfValidWeaponItemClassesToWeaponTags;
    }

    public static float getResult(StatContainer statContainer, Set<ResourceLocation> filteredAttributeIds) {
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

    public static float getResultOfSingleAttribute(StatContainer statContainer, ResourceLocation attributeId) {
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

    public static float[] getData(StatContainer statContainer, Set<ResourceLocation> filteredAttributeIds) {
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

    public static Set<ResourceLocation> getFilteredAttributes(String... tags) {
        return getFilteredAttributes(Set.of(tags));
    }

    public static Set<ResourceLocation> getFilteredAttributes(Set<String> tags) {
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

    /**
     Takes two StatContainers, and a Hashmap where the keys are the tags to replace, and the values are the new replacements.
     Use cases: use summoner's attributes to be included in minion's attributes as well
     @return a newStatContainer containing the original StatContainer a's attribute resource locations and modifiers; and the attribute resource locations and modifiers from StatContainer b that have a conversion attribute that matches the tags in tagToReplaceToNewReplacementTagMap.
     */
    public static StatContainer getNewStatContainerByRemappingBtoA(StatContainer a, StatContainer b, HashMap<String, String> tagToReplaceToNewReplacementTagMap) {

        StatContainer resultStatContainer = new StatContainer();

        ///adds everything in StatContainer a to new stat container that we will return
        for(var addedEntry : a.getAddedModifiers().entries()) {
            var attributeId = addedEntry.getKey().toString();
            var modifier = addedEntry.getValue();
            resultStatContainer.addModifier(modifier, attributeId);
        }

        for(var increasedEntry : a.getIncreaseModifiers().entries()) {
            var attributeId = increasedEntry.getKey().toString();
            var modifier = increasedEntry.getValue();
            resultStatContainer.addModifier(modifier, attributeId);
        }

        for(var moreEntry : a.getMoreModifiers().entries()) {
            var attributeId = moreEntry.getKey().toString();
            var modifier = moreEntry.getValue();
            resultStatContainer.addModifier(modifier, attributeId);
        }

        ///moving on to StatContainer b
        for(var entry : tagToReplaceToNewReplacementTagMap.entrySet()) {
            var attributeTagToReplace = entry.getKey();
            var replacement = entry.getValue();

            for(var addedEntry : b.getAddedModifiers().entries()) {
                var attributeRL = addedEntry.getKey();
                var modifier = addedEntry.getValue();

                var advancedAPGAttribute = AdvancedARPGAttribute.get(attributeRL);
                if(advancedAPGAttribute == null) continue;

                if(!advancedAPGAttribute.getTags().contains(attributeTagToReplace)) continue;

                var newTags = new HashSet<>(advancedAPGAttribute.getTags());
                newTags.remove(attributeTagToReplace);
                newTags.add(replacement);

                var replacementAttributeRL = (ResourceLocation) AdvancedARPGAttributesAPI.getFilteredAttributes(newTags).toArray()[0];
                var replacementAttributeId = replacementAttributeRL.toString();

                resultStatContainer.addModifier(modifier, replacementAttributeId);

            }

            for(var increasedEntry : b.getIncreaseModifiers().entries()) {
                var attributeRL = increasedEntry.getKey();
                var modifier = increasedEntry.getValue();

                var advancedAPGAttribute = AdvancedARPGAttribute.get(attributeRL);
                if(advancedAPGAttribute == null) continue;

                if(!advancedAPGAttribute.getTags().contains(attributeTagToReplace)) continue;

                var newTags = new HashSet<>(advancedAPGAttribute.getTags());
                newTags.remove(attributeTagToReplace);
                newTags.add(replacement);

                var replacementAttributeRL = (ResourceLocation) AdvancedARPGAttributesAPI.getFilteredAttributes(newTags).toArray()[0];
                var replacementAttributeId = replacementAttributeRL.toString();

                resultStatContainer.addModifier(modifier, replacementAttributeId);

            }

            for(var moreEntry : b.getMoreModifiers().entries()) {
                var attributeRL = moreEntry.getKey();
                var modifier = moreEntry.getValue();

                var advancedAPGAttribute = AdvancedARPGAttribute.get(attributeRL);
                if(advancedAPGAttribute == null) continue;

                if(!advancedAPGAttribute.getTags().contains(attributeTagToReplace)) continue;

                var newTags = new HashSet<>(advancedAPGAttribute.getTags());
                newTags.remove(attributeTagToReplace);
                newTags.add(replacement);

                var replacementAttributeRL = (ResourceLocation) AdvancedARPGAttributesAPI.getFilteredAttributes(newTags).toArray()[0];
                var replacementAttributeId = replacementAttributeRL.toString();

                resultStatContainer.addModifier(modifier, replacementAttributeId);

            }

        }

        logDataFromStatContainer(resultStatContainer);

        return resultStatContainer;
    }

    public static void logDataFromStatContainer(StatContainer statContainer) {
        for(var key : statContainer.getAddedModifiers().keySet()) {
            AdvancedARPGAttributesMod.LOGGER.info(key + " "  + statContainer.getAddedModifiers().get(key).toString());
        }

        for(var key : statContainer.getIncreaseModifiers().keySet()) {
            AdvancedARPGAttributesMod.LOGGER.info(key + " " + statContainer.getIncreaseModifiers().get(key).toString());
        }

        for(var key : statContainer.getMoreModifiers().keySet()) {
            AdvancedARPGAttributesMod.LOGGER.info(key + " " + statContainer.getMoreModifiers().get(key).toString());
        }
    }

}
