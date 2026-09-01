package com.anionianonion.advanced_arpg_attributes_api.commands;

import com.anionianonion.advanced_arpg_attributes_api.api.AdvancedARPGAttributesAPI;
import com.anionianonion.advanced_arpg_attributes_api.capability.StatContainerCapability;
import com.anionianonion.elementals_api.ModAttributes;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SeeModifiersCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(
                Commands.literal("see_attribute_modifiers")
                .executes(commandContext -> {
                    var source = commandContext.getSource();
                    try {
                        var player = source.getPlayerOrException();
                        var attributesTag = player.getAttributes().save();

                        player.sendSystemMessage(Component.literal(attributesTag.getAsString()));

                        player.getCapability(StatContainerCapability.INSTANCE).ifPresent(cap -> {

                            for(var attributeTag : attributesTag) {

                                CompoundTag compoundTag = (CompoundTag) attributeTag;
                                String attributeId = compoundTag.getString("Name");

                                ResourceLocation rl = ResourceLocation.tryParse(attributeId);
                                if(rl == null) continue;
                                if(!AdvancedARPGAttributesAPI.getRegistry().containsKey(rl)) continue;

                                Attribute attribute = ModAttributes.getAttribute(attributeId);
                                var modifiers = Objects.requireNonNull(player.getAttribute(attribute)).getModifiers();

                                player.sendSystemMessage(Component.literal(modifiers.toString()));
                            }

                        });
                    }
                    catch (CommandSyntaxException e) {
                        source.sendFailure(Component.literal("Must be a player to run this command."));
                    }
                    return 0;
                })
                        .then(Commands.argument("attribute_id", StringArgumentType.string())
                                .executes(commandContext -> {
                                    String attributeId = StringArgumentType.getString(commandContext, "attribute_id");

                                    var source = commandContext.getSource();
                                    try {
                                        var player = source.getPlayerOrException();
                                        player.getCapability(StatContainerCapability.INSTANCE).ifPresent(cap -> {

                                            var rl = ResourceLocation.tryParse(attributeId);
                                            if(rl == null) return;

                                            List<Float> addedValues = new ArrayList<>();
                                            cap.getAddedModifiers().get(rl).forEach(attributeModifier -> {
                                                addedValues.add((float) attributeModifier.getAmount());
                                            });

                                            player.sendSystemMessage(Component.literal("ADDITION" + addedValues));

                                            List<Float> increaseValues = new ArrayList<>();
                                            cap.getIncreaseModifiers().get(rl).forEach(attributeModifier -> {
                                                increaseValues.add((float) attributeModifier.getAmount());
                                            });
                                            player.sendSystemMessage(Component.literal("INCREASES" + increaseValues));

                                            List<Float> moreValues = new ArrayList<>();
                                            cap.getMoreModifiers().get(rl).forEach(attributeModifier -> {
                                                moreValues.add((float) attributeModifier.getAmount());
                                            });
                                            player.sendSystemMessage(Component.literal("MORES" + moreValues));
                                        });
                                    }
                                    catch (CommandSyntaxException e) {
                                        source.sendFailure(Component.literal("Must be a player to run this command."));
                                    }
                                    return 0;
                        }))
        );
    }
}
