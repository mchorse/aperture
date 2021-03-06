package mchorse.aperture.camera;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import mchorse.aperture.Aperture;
import mchorse.aperture.camera.destination.AbstractDestination;
import mchorse.aperture.capabilities.camera.Camera;
import mchorse.aperture.capabilities.camera.ICamera;
import mchorse.aperture.network.Dispatcher;
import mchorse.aperture.network.common.PacketCameraProfile;
import mchorse.aperture.network.common.PacketCameraState;
import mchorse.mclib.utils.JsonUtils;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.DimensionManager;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 * Utilities for camera classes
 *
 * Includes methods for writing/reading camera profile, getting camera JSON
 * builder and stuff like sending camera profile to the client.
 */
public class CameraUtils
{
    /**
     * Get path to camera profile file (located in current world save's folder)
     */
    public static File cameraFile(String filename)
    {
        File file = new File(DimensionManager.getCurrentSaveRootDirectory() + "/aperture/cameras");

        if (!file.exists())
        {
            file.mkdirs();
        }

        return new File(file, filename + ".json");
    }

    /**
     * Read CameraProfile instance from given file
     */
    public static String readCameraProfile(String filename) throws Exception
    {
        File file = cameraFile(filename);
        DataInputStream stream = new DataInputStream(new FileInputStream(file));
        Scanner scanner = new Scanner(stream, "UTF-8");
        String content = scanner.useDelimiter("\\A").next();

        scanner.close();

        return content;
    }

    /**
     * Write CameraProfile instance to given file
     */
    public static void writeCameraProfile(String filename, String profile) throws IOException
    {
        PrintWriter printer = new PrintWriter(cameraFile(filename));

        printer.print(profile);
        printer.close();
    }

    /* Commands */

    /**
     * Send a camera profile that was read from given file to player.
     *
     * This method also checks if player has same named camera profile, and if
     * it's expired (server has newer version), send him new one.
     */
    public static void sendProfileToPlayer(String filename, EntityPlayerMP player, boolean play, boolean force)
    {
        try
        {
            if (!force && playerHasProfile(player, filename, play))
            {
                return;
            }

            if (!cameraFile(filename).isFile())
            {
                Aperture.l10n.error(player, "profile.cant_load", filename);

                return;
            }

            CameraProfile profile = readProfile(filename);
            ICamera recording = Camera.get(player);

            recording.setCurrentProfile(filename);
            recording.setCurrentProfileTimestamp(System.currentTimeMillis());

            Dispatcher.sendTo(new PacketCameraProfile(filename, profile, play), player);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Aperture.l10n.error(player, "profile.cant_load", filename);
        }
    }

    public static CameraProfile readProfile(String filename) throws Exception
    {
        return readProfileFromJSON(readCameraProfile(filename));
    }

    public static CameraProfile readProfileFromJSON(String json) throws Exception
    {
        CameraProfile profile = new CameraProfile(null);
        JsonElement element = new JsonParser().parse(json);

        if (!element.isJsonObject())
        {
            return profile;
        }

        profile.fromJSON(element.getAsJsonObject());

        return profile;
    }

    /**
     * Checks whether player has older camera profile
     */
    private static boolean playerHasProfile(EntityPlayerMP player, String filename, boolean play)
    {
        ICamera recording = Camera.get(player);
        File profile = cameraFile(filename);

        boolean hasSame = recording.currentProfile().equals(filename);
        boolean isNewer = recording.currentProfileTimestamp() >= profile.lastModified();

        if (hasSame && isNewer)
        {
            if (play)
            {
                Dispatcher.sendTo(new PacketCameraState(filename, true), player);
            }
            else
            {
                Aperture.l10n.info(player, "profile.loaded", filename);
            }

            return true;
        }

        return false;
    }

    /**
     * Save given camera profile to file. Inform user about the problem, if the
     * camera profile couldn't be saved.
     */
    public static boolean saveCameraProfile(String filename, String profile, EntityPlayerMP player)
    {
        try
        {
            writeCameraProfile(filename, profile);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            Aperture.l10n.error(player, "profile.cant_save", filename);

            return false;
        }

        return true;
    }

    /**
     * Turn a camera profile into JSON string
     * 
     * This method is also responsible for doing JSON prettifying, such as 
     * making sure there are 4 spaces for indentation. 
     */
    public static String toJSON(CameraProfile profile)
    {
        JsonObject object = new JsonObject();

        profile.toJSON(object);

        return JsonUtils.jsonToPretty(object);
    }

    /**
     * Rename camera profile 
     */
    public static boolean renameProfile(String from, String to)
    {
        File fromFile = cameraFile(from);
        File toFile = cameraFile(to);

        return fromFile.renameTo(toFile);
    }

    /**
     * Rename camera profile 
     */
    public static boolean removeProfile(String profile)
    {
        File file = cameraFile(profile);

        return file.delete();
    }

    public static float parseAspectRation(String ratio, float old)
    {
        try
        {
            return Float.parseFloat(ratio);
        }
        catch (Exception e)
        {
            try
            {
                String[] strips = ratio.split(":");

                if (strips.length >= 2)
                {
                    return Float.parseFloat(strips[0]) / Float.parseFloat(strips[1]);
                }
            }
            catch (Exception ee)
            {}
        }

        return old;
    }
}