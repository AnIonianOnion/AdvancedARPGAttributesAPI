package com.anionianonion.advanced_arpg_attributes_api.capability;

import com.anionianonion.advanced_arpg_attributes_api.StatContainer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class StatContainerCapability {

    public static final Capability<StatContainer> INSTANCE =
            CapabilityManager.get(new CapabilityToken<>() {});
}
