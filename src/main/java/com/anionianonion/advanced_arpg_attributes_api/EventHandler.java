package com.anionianonion.advanced_arpg_attributes_api;

import com.anionianonion.advanced_arpg_attributes_api.capability.StatContainerCapability;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

import static com.anionianonion.advanced_arpg_attributes_api.StatContainer.updateAdvancedARPGAttributeModifiers;


@Mod.EventBusSubscriber(modid = AdvancedARPGAttributesMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EventHandler {

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent e) {
        players.add((ServerPlayer) e.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent e) {
        players.remove((ServerPlayer) e.getEntity());
        index = Mth.clamp(index - 1, 0, index);
    }

    private static final Stack<ServerPlayer> players = new Stack<>();
    private static int index = 0;

    //length needed for passing thru every player in the list
    private static int fullCycleLength = 10;
    private static int fullCycleTickTracker = 0;

    //spacing of time in-between each player (including the same player, when cycle resets if there is only one player)
    private static int playerInterval = 10;

    private static int theoreticalPlayerCount = 1;


    //we want attribute bonuses from Minecraft to be counted as AdvancedARPGAttributes
    //ItemAttributeModifierEvent and LivingEquipmentChangeEvent both don't work for this.
    //the former doesn't have a way to get the entity that triggered it.
    //the latter doesn't trigger the optional from the entity's capability
    //this is why we're using a standard onTick function instead.
    @SubscribeEvent
    public static void onTick(TickEvent.ServerTickEvent e) {

        ///things needed:
        //a clock to execute code and track of running when it's player's turn \___>  can be same clock
        //a clock to stop program from running when it's not anyone's turn. ___/
        //a clock that triggers recalibrating itself and the other clock intervals once everyone is done running, or if a player has joined or left.

        if(players.isEmpty()) return;

        //do stuff to the players round-robin when it's time
        if(fullCycleTickTracker == index * playerInterval) {

            //do stuff to player
            var player = players.get(index);
            updateAdvancedARPGAttributeModifiers(player);

            //tell it to move to next player.
            index++;

            //restart the list of players from index 0 when it reaches the maximum.
            if(index >= players.size()) index = 0;
        }


        //after almost everything is finished, increase the tracker
        fullCycleTickTracker++;

        //reset it back to 0 if it reaches the maximum.
        // also recalculate
        if(fullCycleTickTracker >= fullCycleLength) {

            /// reset cycle
            fullCycleTickTracker = 0;

            /// recalculation of ticks
            //base case is players.size == 1, where it shouldn't trigger. so trigger only if players.size() > theoreticalPlayerCount.
            while(players.size() > theoreticalPlayerCount) {
                theoreticalPlayerCount *= 2;
                fullCycleLength *= 2;
            }

            playerInterval = fullCycleLength / players.size();
        }

    }

    private static void info(String message) {
        AdvancedARPGAttributesMod.LOGGER.info(message);
    }


    @SubscribeEvent
    public static void onRespawn(PlayerEvent.Clone e) {
        if(!e.isWasDeath()) return;

        var oldPlayer = e.getOriginal();
        var respawnedPlayer = e.getEntity();

        players.remove((ServerPlayer) oldPlayer);
        players.add((ServerPlayer) respawnedPlayer);

        oldPlayer.reviveCaps();
        oldPlayer.getCapability(StatContainerCapability.INSTANCE).ifPresent(oldStatContainer -> {
            respawnedPlayer.getCapability(StatContainerCapability.INSTANCE).ifPresent(newStatContainer -> {

                //todo: fix statContainer being removed and not being renewed on death
                newStatContainer.setAddedModifiers(oldStatContainer.getAddedModifiers());
                newStatContainer.setIncreaseModifiers(oldStatContainer.getIncreaseModifiers());
                newStatContainer.setMoreModifiers(oldStatContainer.getMoreModifiers());
                newStatContainer.attributeCaps = oldStatContainer.attributeCaps;
            });
        });
        oldPlayer.invalidateCaps();
    }
}
