package mchorse.aperture.network.server;

import mchorse.aperture.camera.CameraUtils;
import mchorse.aperture.capabilities.camera.Camera;
import mchorse.aperture.capabilities.camera.ICamera;
import mchorse.aperture.network.common.PacketCameraProfile;
import mchorse.aperture.utils.L10n;
import mchorse.mclib.network.ServerMessageHandler;
import mchorse.mclib.utils.Patterns;
import net.minecraft.entity.player.EntityPlayerMP;

public class ServerHandlerCameraProfile extends ServerMessageHandler<PacketCameraProfile>
{
    @Override
    public void run(EntityPlayerMP player, PacketCameraProfile message)
    {
        if (!Patterns.FILENAME.matcher(message.filename).matches())
        {
            L10n.error(player, "profile.wrong_filename", message.filename);

            return;
        }

        if (CameraUtils.saveCameraProfile(message.filename, CameraUtils.toJSON(message.profile), player))
        {
            ICamera cap = Camera.get(player);

            cap.setCurrentProfile(message.filename);
            cap.setCurrentProfileTimestamp(System.currentTimeMillis());

            L10n.success(player, "profile.save", message.filename);
        }
    }
}