package mchorse.aperture.network.client;

import mchorse.aperture.ClientProxy;
import mchorse.aperture.camera.CameraAPI;
import mchorse.aperture.camera.destination.ClientDestination;
import mchorse.aperture.camera.destination.ServerDestination;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.aperture.client.gui.GuiProfilesManager;
import mchorse.aperture.network.common.PacketCameraProfileList;
import mchorse.mclib.network.ClientMessageHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ClientHandlerCameraProfileList extends ClientMessageHandler<PacketCameraProfileList>
{
    @Override
    @SideOnly(Side.CLIENT)
    public void run(EntityPlayerSP player, PacketCameraProfileList message)
    {
        GuiScreen current = Minecraft.getMinecraft().currentScreen;

        if (current instanceof GuiCameraEditor)
        {
            GuiProfilesManager manager = ((GuiCameraEditor) current).profiles;

            for (String filename : CameraAPI.getClientProfiles())
            {
                manager.addProfile(new ClientDestination(filename));
            }

            for (String profile : message.cameras)
            {
                manager.addProfile(new ServerDestination(profile));
            }

            manager.profiles.filter("", true);
            manager.profiles.list.sort();

            if (ClientProxy.control.currentProfile == null)
            {
                manager.selectFirstAvailable(manager.profiles.list.getIndex());
            }
        }
    }
}