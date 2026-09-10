package com.anionianonion.advanced_arpg_attributes_api.api;


import com.anionianonion.advanced_arpg_attributes_api.AdvancedARPGAttribute;
import com.anionianonion.advanced_arpg_attributes_api.AdvancedARPGAttributesMod;
import com.anionianonion.advanced_arpg_attributes_api.AdvancedARPGAttributesRegistry;
import com.anionianonion.advanced_arpg_attributes_api.StatContainer;
import com.anionianonion.advanced_arpg_attributes_api.capability.StatContainerCapability;
import com.google.common.collect.Multimap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class AdvancedARPGAttributesAPI {

    private static final Set<String> validTags = new HashSet<>();
    private static final HashMap<Class<? extends Item>, String> classesOfValidMeleeWeaponItemClassesToWeaponTags = new HashMap<>();
    private static final HashMap<Class<? extends Item>, String> classesOfValidRangedWeaponItemClassesToWeaponTags = new HashMap<>();

    public static void registerTag(String newTag) {
        if(newTag != null) validTags.add(newTag);
    }

    public static Set<String> getValidTags() {
        return validTags;
    }

    public static Set<String> getValidWeapons() {
        var meleeWeapons = classesOfValidMeleeWeaponItemClassesToWeaponTags
                .values()
                .stream()
                .filter(tag -> !tag.isEmpty())
                .collect(Collectors.toSet());

        var rangedWeapons = classesOfValidRangedWeaponItemClassesToWeaponTags.
                values().
                stream()
                .filter(tag -> !tag.isEmpty())
                .collect(Collectors.toSet());

        var validWeapons = new HashSet<String>();
        validWeapons.addAll(meleeWeapons);
        validWeapons.addAll(rangedWeapons);

        return validWeapons;
    }

    public static void validateAttributes() {
        var entries = new HashSet<>(AdvancedARPGAttributesRegistry.get().entrySet());
        for(var attributeEntry : entries) {
            if(!validTags.containsAll(attributeEntry.getValue().getTags())) AdvancedARPGAttributesRegistry.get().remove(attributeEntry.getKey());
        }
    }

    public static void regAttribute(ResourceLocation rl, Set<AdvancedARPGAttribute.ModifierType> allowedModifierTypes, Set<String> tags) {
        AdvancedARPGAttributesRegistry.regAttribute(rl, allowedModifierTypes, tags);
    }

    public static void regAttribute(ResourceLocation rl, Set<String> tags) {
        AdvancedARPGAttributesRegistry.regAttribute(rl, tags);
    }

    public static HashMap<ResourceLocation, AdvancedARPGAttribute> getRegistry() {
        return AdvancedARPGAttributesRegistry.get();
    }

    public static void addPlayerExecutedFunctionToAttribute(Attribute a, BiConsumer<Player, Float> function) {
        AdvancedARPGAttributesRegistry.getAttributeCapFunctions().put(a, function);
    }

    public static void registerMeleeWeaponClassAndTag(Class<? extends Item> itemClass, String tag) {
        classesOfValidMeleeWeaponItemClassesToWeaponTags.put(itemClass, tag);
    }

    public static HashMap<Class<? extends Item>, String> getClassesOfMeleeWeaponItemsToTag() {
        return classesOfValidMeleeWeaponItemClassesToWeaponTags;
    }

    public static void registerRangedWeaponClassAndTag(Class<? extends Item> itemClass, String tag) {
        classesOfValidRangedWeaponItemClassesToWeaponTags.put(itemClass, tag);
    }

    public static HashMap<Class<? extends Item>, String> getClassesOfRangedWeaponItemsToTag() {
        return classesOfValidRangedWeaponItemClassesToWeaponTags;
    }


    public static float getResult(LivingEntity livingEntity, Set<ResourceLocation> filteredAttributeRLs) {
        float add = 0;
        float increase = 0;
        float more = 1;

        StatContainer statContainer = livingEntity.getCapability(StatContainerCapability.INSTANCE).resolve().orElse(null);
        if(statContainer == null) return 0;

        for(var attributeRL : filteredAttributeRLs) {
            var allowedMods = AdvancedARPGAttributesRegistry.get(attributeRL).getAllowedModifierTypes();

            if(allowedMods.contains(AdvancedARPGAttribute.ModifierType.ADDED)) {

                var attribute = ForgeRegistries.ATTRIBUTES.getValue(attributeRL);
                if(AdvancedARPGAttributesRegistry.get(attributeRL).isInheritingBase() && attribute != null) {
                    add += (float) livingEntity.getAttribute(attribute).getBaseValue();
                }
                else {
                    add += AdvancedARPGAttributesRegistry.get(attributeRL).getBaseValue();
                }

                for(var modifier : statContainer.getAddedModifiers().get(attributeRL)) {
                    assert modifier != null;
                    float amount = (float) modifier.getAmount();
                    add += amount;
                }
            }

            if(allowedMods.contains(AdvancedARPGAttribute.ModifierType.INCREASED)) {
                for(var modifier : statContainer.getIncreaseModifiers().get(attributeRL)) {
                    assert modifier != null;
                    float amount = (float) modifier.getAmount();
                    increase += amount;
                }
            }

            if(allowedMods.contains(AdvancedARPGAttribute.ModifierType.MORE)) {
                for(var modifier : statContainer.getMoreModifiers().get(attributeRL)) {
                    assert modifier != null;
                    float amount = (float) modifier.getAmount();
                    more *= (1 + amount);
                }
            }
        }

        return add * (1 + increase) * more;

    }

    public static float getResultOfSingleAttribute(LivingEntity livingEntity, ResourceLocation attributeRL) {
        float add = 0;
        float increase = 0;
        float more = 1;

        StatContainer statContainer = livingEntity.getCapability(StatContainerCapability.INSTANCE).resolve().orElse(null);
        if(statContainer == null) return 0;

        var allowedMods = AdvancedARPGAttributesRegistry.get(attributeRL).getAllowedModifierTypes();

        if(allowedMods.contains(AdvancedARPGAttribute.ModifierType.ADDED)) {
            var attribute = ForgeRegistries.ATTRIBUTES.getValue(attributeRL);
            if(AdvancedARPGAttributesRegistry.get(attributeRL).isInheritingBase() && attribute != null) {
                add += (float) livingEntity.getAttribute(attribute).getBaseValue();
            }
            else {
                add += AdvancedARPGAttributesRegistry.get(attributeRL).getBaseValue();
            }

            for(var modifier : statContainer.getAddedModifiers().get(attributeRL)) {
                assert modifier != null;
                float amount = (float) modifier.getAmount();
            }
        }

        if(allowedMods.contains(AdvancedARPGAttribute.ModifierType.INCREASED)) {
            for(var modifier : statContainer.getIncreaseModifiers().get(attributeRL)) {
                assert modifier != null;
                float amount = (float) modifier.getAmount();
                increase += amount;
            }
        }

        if(allowedMods.contains(AdvancedARPGAttribute.ModifierType.MORE)) {
            for(var modifier : statContainer.getMoreModifiers().get(attributeRL)) {
                assert modifier != null;
                float amount = (float) modifier.getAmount();
                more *= (1 + amount);
            }
        }
        var result = add * (1 + increase) * more;
        var cap = statContainer.getLockedAttributeValue(attributeRL);
        if(cap == null) return result;
        return Math.min(result, cap);
    }

    public static float[] getData(LivingEntity livingEntity, Set<ResourceLocation> filteredAttributeRLs) {
        float[] data = new float[3];

        float add = 0;
        float increase = 0;
        float more = 1;

        StatContainer statContainer = livingEntity.getCapability(StatContainerCapability.INSTANCE).resolve().orElse(null);
        if(statContainer == null) {
            data[0] = add;
            data[1] = increase;
            data[2] = more - 1;
            return data;
        }


        for(var attributeRL : filteredAttributeRLs) {
            var allowedMods = AdvancedARPGAttributesRegistry.get(attributeRL).getAllowedModifierTypes();

            if(allowedMods.contains(AdvancedARPGAttribute.ModifierType.ADDED)) {
                var attribute = ForgeRegistries.ATTRIBUTES.getValue(attributeRL);
                if(AdvancedARPGAttributesRegistry.get(attributeRL).isInheritingBase() && attribute != null) {
                    add += (float) livingEntity.getAttribute(attribute).getBaseValue();
                }
                else {
                    add += AdvancedARPGAttributesRegistry.get(attributeRL).getBaseValue();
                }
                
                for(var modifier : statContainer.getAddedModifiers().get(attributeRL)) {
                    assert modifier != null;
                    float amount = (float) modifier.getAmount();
                    add += amount;
                }
            }

            if(allowedMods.contains(AdvancedARPGAttribute.ModifierType.INCREASED)) {
                for(var modifier : statContainer.getIncreaseModifiers().get(attributeRL)) {
                    assert modifier != null;
                    float amount = (float) modifier.getAmount();
                    increase += amount;
                }
            }

            if(allowedMods.contains(AdvancedARPGAttribute.ModifierType.MORE)) {
                for(var modifier : statContainer.getMoreModifiers().get(attributeRL)) {
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
        var attributeEntries = AdvancedARPGAttributesRegistry.get().entrySet();
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

    public static ResourceLocation getClosestMatchingAttribute(Set<String> tags) {

        //worst case scenario:
        if(tags.isEmpty()) return null;

        //ideally, attribute is found that matches all the tags on first iteration.
        List<ResourceLocation> ret = new ArrayList<>();

        for(AdvancedARPGAttribute attribute : AdvancedARPGAttributesAPI.getRegistry().values()) {
            if(attribute.getTags().containsAll(tags)) ret.add(attribute.getRl());
        }

        if(!ret.isEmpty()) return ret.get(0);
        else {
            //otherwise, we split the lists smaller, and do it again

                //backup
            var setToList = tags.stream().toList();
            for(int i = 0; i < setToList.size(); i++) {
                var newList = new ArrayList<>(setToList);
                newList.remove(i);
                return getClosestMatchingAttribute(new HashSet<>(newList));
            }
            return null;
        }
    }

    //forgot to mention this
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

        AdvancedARPGAttributesMod.LOGGER.info("stat container a");
        logDataFromStatContainer(a);
        AdvancedARPGAttributesMod.LOGGER.info("stat container b");
        logDataFromStatContainer(b);

        ///moving on to adding data from StatContainer b
        //need to get the tags from stat container b and replace the tags all at once with the replacement
        convertModifiersAndAdd(resultStatContainer, b.getAddedModifiers(), tagToReplaceToNewReplacementTagMap);
        convertModifiersAndAdd(resultStatContainer, b.getIncreaseModifiers(), tagToReplaceToNewReplacementTagMap);
        convertModifiersAndAdd(resultStatContainer, b.getMoreModifiers(), tagToReplaceToNewReplacementTagMap);

        AdvancedARPGAttributesMod.LOGGER.info("resultant stat container");
        logDataFromStatContainer(resultStatContainer);

        return resultStatContainer;
    }

    public static void convertModifiersAndAdd(StatContainer resultStatContainer, Multimap<ResourceLocation, AttributeModifier> multimap, HashMap<String, String> tagToReplaceToNewReplacementTagMap) {
        for(var addedEntry : multimap.entries()) {
            var rl = addedEntry.getKey();
            var am = addedEntry.getValue();

            var aaattribute = AdvancedARPGAttributesAPI.getRegistry().get(rl);
            if(aaattribute == null) continue;

            //replacement
            var newMutableTags = new HashSet<>(aaattribute.getTags());
            for(var entry : tagToReplaceToNewReplacementTagMap.entrySet()) {

                var tagToReplace = entry.getKey();
                var replacement = entry.getValue();

                if(newMutableTags.contains(tagToReplace)) {
                    newMutableTags.remove(tagToReplace);
                    newMutableTags.add(replacement);
                }
            }

            var replaceAttributesRLs = AdvancedARPGAttributesAPI.getFilteredAttributes(newMutableTags);
            AdvancedARPGAttributesMod.LOGGER.info("potential attribute replacements: " + replaceAttributesRLs);
            var replacementAttributeRL = getClosestMatchingAttribute(newMutableTags);

            if(replacementAttributeRL != null) {
                var replacementAttributeId = replacementAttributeRL.toString();
                resultStatContainer.addModifier(am, replacementAttributeId);
            }
        }
    }

    public static void logDataFromStatContainer(StatContainer statContainer) {
        AdvancedARPGAttributesMod.LOGGER.info("added modifiers");
        for(var key : statContainer.getAddedModifiers().keySet()) {
            AdvancedARPGAttributesMod.LOGGER.info(key + " "  + statContainer.getAddedModifiers().get(key).toString());
        }

        AdvancedARPGAttributesMod.LOGGER.info("increases");
        for(var key : statContainer.getIncreaseModifiers().keySet()) {
            AdvancedARPGAttributesMod.LOGGER.info(key + " " + statContainer.getIncreaseModifiers().get(key).toString());
        }

        AdvancedARPGAttributesMod.LOGGER.info("more's");
        for(var key : statContainer.getMoreModifiers().keySet()) {
            AdvancedARPGAttributesMod.LOGGER.info(key + " " + statContainer.getMoreModifiers().get(key).toString());
        }
    }

}
