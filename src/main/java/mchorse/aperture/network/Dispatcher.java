package mchorse.aperture.network;

import mchorse.aperture.Aperture;
import mchorse.aperture.network.client.ClientHandlerCameraProfile;
import mchorse.aperture.network.client.ClientHandlerCameraProfileList;
import mchorse.aperture.network.client.ClientHandlerCameraState;
import mchorse.aperture.network.client.ClientHandlerRenameCameraProfile;
import mchorse.aperture.network.common.PacketCameraProfile;
import mchorse.aperture.network.common.PacketCameraProfileList;
import mchorse.aperture.network.common.PacketCameraReset;
import mchorse.aperture.network.common.PacketCameraState;
import mchorse.aperture.network.common.PacketLoadCameraProfile;
import mchorse.aperture.network.common.PacketRenameCameraProfile;
import mchorse.aperture.network.common.PacketRequestCameraProfiles;
import mchorse.aperture.network.server.ServerHandlerCameraProfile;
import mchorse.aperture.network.server.ServerHandlerCameraReset;
import mchorse.aperture.network.server.ServerHandlerLoadCameraProfile;
import mchorse.aperture.network.server.ServerHandlerRenameCameraProfile;
import mchorse.aperture.network.server.ServerHandlerRequestCameraProfiles;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Network dispatcher
 *
 * @author Ernio (Ernest Sadowski)
 */
public class Dispatcher
{
    private static final SimpleNetworkWrapper DISPATCHER = NetworkRegistry.INSTANCE.newSimpleChannel(Aperture.MODID);
    private static byte PACKET_ID;

    public static SimpleNetworkWrapper get()
    {
        return DISPATCHER;
    }

    /**
     * Send message to players who are tracking given entity
     */
    public static void sendToTracked(Entity entity, IMessage message)
    {
        EntityTracker tracker = ((WorldServer) entity.worldObj).getEntityTracker();

        for (EntityPlayer player : tracker.getTrackingPlayers(entity))
        {
            DISPATCHER.sendTo(message, (EntityPlayerMP) player);
        }
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
        /* Camera management */
        register(PacketCameraProfile.class, ClientHandlerCameraProfile.class, Side.CLIENT);
        register(PacketCameraProfile.class, ServerHandlerCameraProfile.class, Side.SERVER);
        register(PacketCameraReset.class, ServerHandlerCameraReset.class, Side.SERVER);
        register(PacketCameraState.class, ClientHandlerCameraState.class, Side.CLIENT);
        register(PacketLoadCameraProfile.class, ServerHandlerLoadCameraProfile.class, Side.SERVER);
        register(PacketRequestCameraProfiles.class, ServerHandlerRequestCameraProfiles.class, Side.SERVER);
        register(PacketCameraProfileList.class, ClientHandlerCameraProfileList.class, Side.CLIENT);
        register(PacketRenameCameraProfile.class, ClientHandlerRenameCameraProfile.class, Side.CLIENT);
        register(PacketRenameCameraProfile.class, ServerHandlerRenameCameraProfile.class, Side.SERVER);
    }

    /**
     * Register given message with given message handler on a given side
     */
    private static <REQ extends IMessage, REPLY extends IMessage> void register(Class<REQ> message, Class<? extends IMessageHandler<REQ, REPLY>> handler, Side side)
    {
        DISPATCHER.registerMessage(handler, message, PACKET_ID++, side);
    }
}