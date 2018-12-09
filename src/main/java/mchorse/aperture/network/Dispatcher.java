package mchorse.aperture.network;

import mchorse.aperture.Aperture;
import mchorse.aperture.network.client.ClientHandlerCameraProfile;
import mchorse.aperture.network.client.ClientHandlerCameraProfileList;
import mchorse.aperture.network.client.ClientHandlerCameraState;
import mchorse.aperture.network.client.ClientHandlerRenameCameraProfile;
import mchorse.aperture.network.client.ClientHandlerRemoveCameraProfile;
import mchorse.aperture.network.common.PacketCameraProfile;
import mchorse.aperture.network.common.PacketCameraProfileList;
import mchorse.aperture.network.common.PacketCameraReset;
import mchorse.aperture.network.common.PacketCameraState;
import mchorse.aperture.network.common.PacketLoadCameraProfile;
import mchorse.aperture.network.common.PacketRemoveCameraProfile;
import mchorse.aperture.network.common.PacketRenameCameraProfile;
import mchorse.aperture.network.common.PacketRequestCameraProfiles;
import mchorse.aperture.network.server.ServerHandlerCameraProfile;
import mchorse.aperture.network.server.ServerHandlerCameraReset;
import mchorse.aperture.network.server.ServerHandlerLoadCameraProfile;
import mchorse.aperture.network.server.ServerHandlerRemoveCameraProfile;
import mchorse.aperture.network.server.ServerHandlerRenameCameraProfile;
import mchorse.aperture.network.server.ServerHandlerRequestCameraProfiles;
import mchorse.mclib.network.AbstractDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Network dispatcher
 *
 * @author Ernio (Ernest Sadowski)
 */
public class Dispatcher
{
    public static final AbstractDispatcher DISPATCHER = new AbstractDispatcher(Aperture.MODID)
    {
        @Override
        public void register()
        {
            this.register(PacketCameraProfile.class, ClientHandlerCameraProfile.class, Side.CLIENT);
            this.register(PacketCameraProfile.class, ServerHandlerCameraProfile.class, Side.SERVER);
            this.register(PacketCameraReset.class, ServerHandlerCameraReset.class, Side.SERVER);
            this.register(PacketCameraState.class, ClientHandlerCameraState.class, Side.CLIENT);
            this.register(PacketLoadCameraProfile.class, ServerHandlerLoadCameraProfile.class, Side.SERVER);
            this.register(PacketRequestCameraProfiles.class, ServerHandlerRequestCameraProfiles.class, Side.SERVER);
            this.register(PacketCameraProfileList.class, ClientHandlerCameraProfileList.class, Side.CLIENT);

            this.register(PacketRenameCameraProfile.class, ClientHandlerRenameCameraProfile.class, Side.CLIENT);
            this.register(PacketRenameCameraProfile.class, ServerHandlerRenameCameraProfile.class, Side.SERVER);

            this.register(PacketRemoveCameraProfile.class, ClientHandlerRemoveCameraProfile.class, Side.CLIENT);
            this.register(PacketRemoveCameraProfile.class, ServerHandlerRemoveCameraProfile.class, Side.SERVER);
        }
    };

    /**
     * Send message to players who are tracking given entity
     */
    public static void sendToTracked(Entity entity, IMessage message)
    {
        DISPATCHER.sendToTracked(entity, message);
    }

    /**
     * Send message to given player
     */
    public static void sendTo(IMessage message, EntityPlayerMP player)
    {
        DISPATCHER.sendTo(message, player);
    }

    /**
     * Send message to the server
     */
    public static void sendToServer(IMessage message)
    {
        DISPATCHER.sendToServer(message);
    }

    /**
     * Register all the networking messages and message handlers
     */
    public static void register()
    {
        DISPATCHER.register();
    }
}