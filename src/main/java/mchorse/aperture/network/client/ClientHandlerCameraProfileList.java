package mchorse.aperture.network.client;

import mchorse.aperture.ClientProxy;
import mchorse.aperture.camera.CameraAPI;
import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.destination.AbstractDestination;
import mchorse.aperture.camera.destination.ClientDestination;
import mchorse.aperture.camera.destination.ServerDestination;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.aperture.client.gui.GuiProfilesManager;
import mchorse.aperture.client.gui.GuiProfilesManager.CameraProfileEntry;
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
                manager.profiles.add(manager.createEntry(new ClientDestination(filename)));
            }

            for (String profile : message.cameras)
            {
                manager.profiles.add(manager.createEntry(new ServerDestination(profile)));
            }

            if (!ClientProxy.control.logged)
            {
                if (manager.profiles.list.getList().isEmpty())
                {
                    manager.createTemporary();
                }

                ClientProxy.control.logged = true;
            }

            manager.profiles.filter("", true);

            if (ClientProxy.control.currentProfile != null)
            {
                manager.selectProfile(ClientProxy.control.currentProfile);
            }
            else
            {
                manager.pickEntry(manager.profiles.list.getList().get(0));
            }
        }
    }
}