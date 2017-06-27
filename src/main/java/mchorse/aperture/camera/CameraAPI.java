package mchorse.aperture.camera;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import mchorse.aperture.ClientProxy;
import mchorse.aperture.camera.destination.AbstractDestination;
import mchorse.aperture.camera.destination.ServerDestination;
import mchorse.aperture.network.Dispatcher;
import mchorse.aperture.network.common.PacketCameraState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Camera API
 * 
 * This class provides some useful API methods for working with cameras
 */
public class CameraAPI
{
    /**
     * Server side code to start playing camera by providing a resource location
     */
    public static void playCameraProfile(EntityPlayerMP player, ResourceLocation resource)
    {
        playCameraProfile(player, AbstractDestination.fromResourceLocation(resource));
    }

    /**
     * Server side code to start playing camera by providing an abstract destination
     */
    public static void playCameraProfile(EntityPlayerMP player, AbstractDestination destination)
    {
        if (destination instanceof ServerDestination)
        {
            CameraUtils.sendProfileToPlayer(destination.getFilename(), player, true, false);
        }
        else
        {
            Dispatcher.sendTo(new PacketCameraState(destination.getFilename(), true), player);
        }
    }

    /**
     * Get a list of camera profile names on the client side (from the config) 
     */
    @SideOnly(Side.CLIENT)
    public static List<String> getClientProfiles()
    {
        List<String> files = new ArrayList<String>();

        for (File file : ClientProxy.getClientCameras().listFiles(new JSONFileFilter()))
        {
            String filename = file.getName();

            filename = filename.substring(0, filename.lastIndexOf(".json"));
            files.add(filename);
        }

        return files;
    }

    /**
     * Get a list of camera profile names on the server side (from world save 
     * aperture/cameras folder).
     */
    public static List<String> listServerProfiles()
    {
        File file = new File(DimensionManager.getCurrentSaveRootDirectory() + "/aperture/cameras");
        List<String> files = new ArrayList<String>();

        file.mkdirs();

        for (File profile : file.listFiles(new JSONFileFilter()))
        {
            String filename = profile.getName();

            files.add(filename.substring(0, filename.lastIndexOf(".json")));
        }

        return files;
    }

    /**
     * JSON file filter 
     */
    public static class JSONFileFilter implements FileFilter
    {
        @Override
        public boolean accept(File file)
        {
            return file.isFile() && file.getName().endsWith(".json");
        }
    }
}