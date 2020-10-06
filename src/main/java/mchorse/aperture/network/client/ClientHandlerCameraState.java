package mchorse.aperture.network.client;

import mchorse.aperture.ClientProxy;
import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.destination.AbstractDestination;
import mchorse.aperture.camera.destination.ClientDestination;
import mchorse.aperture.network.common.PacketCameraState;
import mchorse.mclib.network.ClientMessageHandler;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Client handler camera state
 *
 * This client handler is responsible for running or stopping the camera. It
 * is pretty simple handler.
 */
public class ClientHandlerCameraState extends ClientMessageHandler<PacketCameraState>
{
    @Override
    @SideOnly(Side.CLIENT)
    public void run(EntityPlayerSP player, PacketCameraState message)
    {
        if (message.toPlay)
        {
            AbstractDestination destination = AbstractDestination.create(message.filename);

            if (destination instanceof ClientDestination)
            {
                new ClientDestination(message.filename).load();
            }

            if (message.filename.isEmpty())
            {
                ClientProxy.runner.start(ClientProxy.control.currentProfile);
            }
            else
            {
                CameraProfile profile = ClientProxy.control.getProfile(destination);

                if (profile == null)
                {
                    ClientProxy.runner.start(ClientProxy.control.currentProfile);
                }
                else
                {
                    ClientProxy.runner.start(profile);
                }
            }
        }
        else
        {
            ClientProxy.runner.stop();
        }
    }
}