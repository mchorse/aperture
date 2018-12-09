package mchorse.aperture.network.client;

import mchorse.aperture.ClientProxy;
import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.destination.AbstractDestination;
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

            for (String profile : message.cameras)
            {
                manager.profiles.add(manager.createEntry(new ServerDestination(profile)));
            }

            if (!ClientProxy.control.logged)
            {
                if (manager.profiles.getList().isEmpty())
                {
                    ClientProxy.control.addProfile(new CameraProfile(AbstractDestination.create("default")));
                }

                ClientProxy.control.logged = true;
            }

            manager.profiles.sort();
            manager.selectProfile(ClientProxy.control.currentProfile);
        }
    }
}