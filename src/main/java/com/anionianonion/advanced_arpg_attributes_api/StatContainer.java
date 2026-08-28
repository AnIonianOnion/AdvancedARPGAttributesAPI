package com.anionianonion.advanced_arpg_attributes_api;

import com.anionianonion.advanced_arpg_attributes_api.api.AdvancedARPGAttributesAPI;
import com.anionianonion.advanced_arpg_attributes_api.capability.StatContainerCapability;
import com.anionianonion.advanced_arpg_attributes_api.util.RandomHelpers;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.anionianonion.advanced_arpg_attributes_api.AdvancedARPGAttribute.ModifierType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiConsumer;

public class StatContainer implements INBTSerializable<CompoundTag> {

    private Multimap<ResourceLocation, AttributeModifier> addedModifiers = ArrayListMultimap.create();
    private Multimap<ResourceLocation, AttributeModifier> increaseModifiers = ArrayListMultimap.create();
    private Multimap<ResourceLocation, AttributeModifier> moreModifiers = ArrayListMultimap.create();

    public Multimap<ResourceLocation, AttributeModifier> getAddedModifiers() {
        return addedModifiers;
    }
    public void setAddedModifiers(Multimap<ResourceLocation, AttributeModifier> addedModifiers) {
        this.addedModifiers = addedModifiers;
    }
    public Multimap<ResourceLocation, AttributeModifier> getIncreaseModifiers() {
        return increaseModifiers;
    }
    public void setIncreaseModifiers(Multimap<ResourceLocation, AttributeModifier> increaseModifiers) {
        this.increaseModifiers = increaseModifiers;
    }
    public Multimap<ResourceLocation, AttributeModifier> getMoreModifiers() {
        return moreModifiers;
    }
    public void setMoreModifiers(Multimap<ResourceLocation, AttributeModifier> moreModifiers) {
        this.moreModifiers = moreModifiers;
    }

    //todo: remember to properly serialize/deserialize
    public HashMap<ResourceLocation, Float> attributeCaps = new HashMap<>();

    /**
     *
     * @param attributeModifier minecraft modifier.
     * @param attributeId String id of valid registered attributes
     */
    public void addModifier(AttributeModifier attributeModifier, String attributeId) {
        ResourceLocation rl = ResourceLocation.tryParse(attributeId);
        if(rl == null) return;

        var attribute = AdvancedARPGAttribute.get(rl);
        if(attribute == null) return;

        var minecraftAttribute = ForgeRegistries.ATTRIBUTES.getValue(rl);
        if(minecraftAttribute == null) return;

        var modifierType = switch (attributeModifier.getOperation()) {
            case ADDITION -> ModifierType.ADDED;
            case MULTIPLY_BASE -> ModifierType.INCREASED;
            case MULTIPLY_TOTAL -> ModifierType.MORE;
        };

        if(!attribute.getAllowedModifierTypes().contains(modifierType)) return;

        var multimap = switch(attributeModifier.getOperation()) {
            case ADDITION -> addedModifiers;
            case MULTIPLY_BASE -> increaseModifiers;
            case MULTIPLY_TOTAL -> moreModifiers;
        };

        if(!multimap.containsValue(attributeModifier)) multimap.put(rl, attributeModifier);
    }

    public void removeModifier(AttributeModifier attributeModifier, String attributeId) {
        ResourceLocation rl = ResourceLocation.tryParse(attributeId);
        if(rl == null) return;

        var multimap = switch(attributeModifier.getOperation()) {
            case ADDITION -> addedModifiers;
            case MULTIPLY_BASE -> increaseModifiers;
            case MULTIPLY_TOTAL -> moreModifiers;
        };

        multimap.remove(rl, attributeModifier);
    }

    public void readdModifiersFromPlayer(ServerPlayer player) {

        AdvancedARPGAttributesAPI api = new AdvancedARPGAttributesAPI();

        //the only way to get all player attributes from the player, which is saved as a tag
        var attributesTag = player.getAttributes().save();

        //add all modifiers on the player, by looping through the attribute tags on the player (which is only associated with the base value and the name of the attribute inside the Player's NBT)
        for(Tag attributeTag : attributesTag) {

            //convert Tag into a CompoundTag so we can get its keys
            CompoundTag compoundTag = (CompoundTag) attributeTag;
            String attributeId = compoundTag.getString("Name");

            //convert attribute id String into a ResourceLocation, and check if it's valid in both Minecraft, and our list of valid AAAttributes.
            //If not valid, skip.
            ResourceLocation rl = ResourceLocation.tryParse(attributeId);
            if(rl == null) continue;
            if(!api.getRegistry().containsKey(rl)) continue;

            Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(rl);
            if(attribute == null) continue;

            //grab the modifiers the player have for that attribute and store them inside the stat container.
            var modifiers = Objects.requireNonNull(player.getAttribute(attribute)).getModifiers();
            for(var modifier : modifiers) {
                this.addModifier(modifier, attributeId);
            }
        }
    }

    public void clearModifiersForRecalculation() {

        this.addedModifiers.clear();
        this.increaseModifiers.clear();
        this.moreModifiers.clear();
    }
    /**
     Each player has a stat container, and we are setting a cap for a specified attribute within that container.
     Therefore, this can be used to set players' attribute caps individually for specific attributes.
     Can be used for implementing features like Chaos Inoculation from PoE.
     */
    public void lockAttribute(String attributeId, float amount) {
        ResourceLocation rl = RandomHelpers.getValidResourceLocationOfValidAAAttribute(attributeId);

        if(rl == null) {
            AdvancedARPGAttributesMod.LOGGER.info(String.format("resource location is null for %s when trying to set the attribute cap", attributeId));
            return;
        }
        attributeCaps.put(rl, amount);
    }

    public Float getLockedAttributeValue(String attributeId) {
        ResourceLocation rl = RandomHelpers.getValidResourceLocationOfValidAAAttribute(attributeId);
        return getLockedAttributeValue(rl);
    }

    public Float getLockedAttributeValue(ResourceLocation rl) {
        if(rl == null) return null;
        return attributeCaps.get(rl);
    }

    public void unlockAttribute(String attributeId) {
        ResourceLocation rl = RandomHelpers.getValidResourceLocationOfValidAAAttribute(attributeId);
        if(attributeCaps.containsKey(rl) && attributeCaps.get(rl) != null) {
            AdvancedARPGAttributesMod.LOGGER.info(String.format("Found key: %s. Removing cap of %s", attributeId, attributeCaps.get(rl)));
        }
        else if(attributeCaps.containsKey(rl)) {
            AdvancedARPGAttributesMod.LOGGER.info(String.format("Found key: %s. Its value is somehow null.", attributeId));
        }
        attributeCaps.remove(rl);
    }

    /// Serialization/Deserialization
    @Override
    public CompoundTag serializeNBT() {
        //root of data
        CompoundTag statContainerTag = new CompoundTag();

        //there should be an UUId of that item attached to the item already
        statContainerTag.put("addedModifiers", serializeModifierMap(addedModifiers));
        statContainerTag.put("increaseModifiers", serializeModifierMap(increaseModifiers));
        statContainerTag.put("moreModifiers", serializeModifierMap(moreModifiers));
        statContainerTag.put("attributeCaps", serializeAttributeCaps());

        return statContainerTag;
    }
    @Override
    public void deserializeNBT(CompoundTag nbt) {

        deserializeModifierMap(nbt.getCompound("addedModifiers"), addedModifiers);
        deserializeModifierMap(nbt.getCompound("increaseModifiers"), increaseModifiers);
        deserializeModifierMap(nbt.getCompound("moreModifiers"), moreModifiers);
        deserializeAttributeCaps(nbt.getCompound("attributeCaps"));
    }

    private CompoundTag serializeModifierMap(Multimap<ResourceLocation, AttributeModifier> attributesAndModifiers) {

        CompoundTag attributesAndModifiersTag = new CompoundTag();

        for(var attributeKey : attributesAndModifiers.keys()) {
            //represents a list
            ListTag list = new ListTag();
            String attributeId = attributeKey.toString();

            //get the AttributeModifier values associated with the key from original multimap and copy to list tag
            for(var mod : attributesAndModifiers.get(attributeKey)) {
                CompoundTag tag = new CompoundTag();
                tag.putUUID("uuid", mod.getId());
                tag.putDouble("amount", mod.getAmount());
                list.add(tag);
            }

            //Phase 1: Inner. !!!! key associated with list
            attributesAndModifiersTag.put(attributeId, list);
        }

        return attributesAndModifiersTag;
    }
    private void deserializeModifierMap(CompoundTag attributesAndModifiersMultimapAsTag, Multimap<ResourceLocation, AttributeModifier> outputMultiMapReference) {
        outputMultiMapReference.clear(); //ensure no stale data

        int operation;

        if(outputMultiMapReference == addedModifiers) operation = 0;
        else if (outputMultiMapReference == increaseModifiers) operation = 1;
        else if (outputMultiMapReference == moreModifiers) operation = 2;
        else throw new IllegalArgumentException("Unknown map");

        for (String attributeIdKey : attributesAndModifiersMultimapAsTag.getAllKeys()) {
            ResourceLocation rl = ResourceLocation.tryParse(attributeIdKey);
            if(rl == null) continue;

            ListTag list = attributesAndModifiersMultimapAsTag.getList(attributeIdKey, Tag.TAG_COMPOUND);

            for (Tag t : list) {
                CompoundTag tag = (CompoundTag) t;

                UUID attributeUUID = tag.getUUID("uuid");
                double amount = tag.getDouble("amount");

                AttributeModifier mod = new AttributeModifier(attributeUUID, attributeIdKey, amount, AttributeModifier.Operation.fromValue(operation));
                outputMultiMapReference.put(rl, mod);
            }
        }
    }

    private CompoundTag serializeAttributeCaps() {

        CompoundTag attributeCapsTag = new CompoundTag();

        for(var entry : attributeCaps.entrySet()) {
            attributeCapsTag.putFloat(entry.getKey().toString(), entry.getValue());
        }

        return attributeCapsTag;
    }
    private void deserializeAttributeCaps(CompoundTag attributeCapsAsTag) {
        this.attributeCaps.clear();

        for(var key : attributeCapsAsTag.getAllKeys()) {
            this.attributeCaps.put(ResourceLocation.parse(key), attributeCapsAsTag.getFloat(key));
        }
    }

    /// static methods
    public static void updateAdvancedARPGAttributeModifiers(ServerPlayer player) {
        player.getCapability(StatContainerCapability.INSTANCE).ifPresent(statContainerCapability -> {
            //recalculate from a blank slate
            statContainerCapability.clearModifiersForRecalculation();
            statContainerCapability.readdModifiersFromPlayer(player);

            //we want to set player's attributes to their limits based on the individual player's attribute caps.
            var attributeCaps = statContainerCapability.attributeCaps;

            //
            applyAttributeLocks(player, attributeCaps);


        });
    }

    /**
     * This method loops through the resource-location-to-locked-attribute-value map, and "locks" all the player's attributes at those values,
     * by setting the base value of each attribute to that value, and removing all other modifiers. It also applies special functions if the attribute for the resource location that exists in attributeCaps also exists in attributeCapFunctions.
     * *Related*: For special functionalities, like locking a player's health at 1 hp, you must use <code>AdvancedARPGAttributesAPI#addPlayerExecutedFunctionToAttribute</code>
     * ((an instance of AdvancedARPGAttributesAPI).addPlayerExecutedFunctionToAttribute()).
     *  For example: <code>api.addPlayerExecutedFunctionToAttribute(Attributes.MAX_HEALTH, (player, lockedValue) -> { if(player.isAlive()) { player.setHealth(lockedValue); }})</code>
     */
    public static void applyAttributeLocks(ServerPlayer player, HashMap<ResourceLocation, Float> attributeCaps) {
        for(var attributeCap : attributeCaps.entrySet()) {
            var attributeKey = attributeCap.getKey();
            Attribute a = ForgeRegistries.ATTRIBUTES.getValue(attributeKey);
            if(a == null) continue;

            Objects.requireNonNull(player.getAttribute(a)).removeModifiers();
            Objects.requireNonNull(player.getAttribute(a)).setBaseValue(attributeCap.getValue());

            if(AdvancedARPGAttribute.getAttributeCapFunctions().containsKey(a)) {
                BiConsumer<Player, Float> function = AdvancedARPGAttribute.getAttributeCapFunctions().get(a);
                function.accept(player, attributeCap.getValue());
            }
        }
    }
}
