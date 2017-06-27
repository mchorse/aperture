package mchorse.aperture.camera.destination;

import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.CameraUtils;
import mchorse.aperture.network.Dispatcher;
import mchorse.aperture.network.common.PacketCameraProfile;
import mchorse.aperture.network.common.PacketLoadCameraProfile;
import net.minecraft.util.ResourceLocation;

/**
 * Server destination
 * 
 * Saves and reloads camera profile from server's world.
 */
public class ServerDestination extends AbstractDestination
{
    public ServerDestination(String filename)
    {
        super(filename);
    }

    @Override
    public boolean equals(Object obj)
    {
        return super.equals(obj) && obj instanceof ServerDestination;
    }

    @Override
    public void save(CameraProfile profile)
    {
        Dispatcher.sendToServer(new PacketCameraProfile(this.filename, CameraUtils.toJSON(profile)));
    }

    @Override
    public void load()
    {
        Dispatcher.sendToServer(new PacketLoadCameraProfile(this.filename, true));
    }

    @Override
    public ResourceLocation toResourceLocation()
    {
        return new ResourceLocation("server", this.filename);
    }
}