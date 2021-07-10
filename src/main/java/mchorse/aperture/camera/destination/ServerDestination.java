package mchorse.aperture.camera.destination;

import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.network.Dispatcher;
import mchorse.aperture.network.common.PacketCameraProfile;
import mchorse.aperture.network.common.PacketLoadCameraProfile;
import mchorse.aperture.network.common.PacketRemoveCameraProfile;
import mchorse.aperture.network.common.PacketRenameCameraProfile;
import mchorse.mclib.utils.resources.RLUtils;
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
    public void rename(String name)
    {
        Dispatcher.sendToServer(new PacketRenameCameraProfile(this.filename, name));
    }

    @Override
    public void save(CameraProfile profile)
    {
        Dispatcher.sendToServer(new PacketCameraProfile(this.filename, profile));
    }

    @Override
    public void load()
    {
        Dispatcher.sendToServer(new PacketLoadCameraProfile(this.filename, true));
    }

    @Override
    public void remove()
    {
        Dispatcher.sendToServer(new PacketRemoveCameraProfile(this.filename));
    }

    @Override
    public ResourceLocation toResourceLocation()
    {
        return RLUtils.create("server", this.filename);
    }
}