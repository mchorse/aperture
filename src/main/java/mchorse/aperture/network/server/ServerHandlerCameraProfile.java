package mchorse.aperture.network.server;

import mchorse.aperture.camera.CameraUtils;
import mchorse.aperture.capabilities.camera.Camera;
import mchorse.aperture.capabilities.camera.ICamera;
import mchorse.aperture.network.common.PacketCameraProfile;
import mchorse.aperture.utils.L10n;
import net.minecraft.entity.player.EntityPlayerMP;

public class ServerHandlerCameraProfile extends ServerMessageHandler<PacketCameraProfile>
{
    @Override
    public void run(EntityPlayerMP player, PacketCameraProfile message)
    {
        if (CameraUtils.saveCameraProfile(message.filename, CameraUtils.toJSON(message.profile), player))
        {
            ICamera recording = Camera.get(player);

            recording.setCurrentProfile(message.filename);
            recording.setCurrentProfileTimestamp(System.currentTimeMillis());

            L10n.success(player, "profile.save", message.filename);
        }
    }
}