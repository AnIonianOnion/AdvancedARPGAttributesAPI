package com.anionianonion.advanced_arpg_attributes_api.capability;

import com.anionianonion.advanced_arpg_attributes_api.StatContainer;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StatContainerProvider implements ICapabilityProvider {

    private final StatContainer backend = new StatContainer();

    private final LazyOptional<StatContainer> optional =
            LazyOptional.of(() -> backend);


    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return cap == StatContainerCapability.INSTANCE ? optional.cast() : LazyOptional.empty();
    }
}
