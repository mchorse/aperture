package mchorse.aperture.camera.destination;

import java.io.File;

import org.apache.commons.io.FileUtils;

import mchorse.aperture.ClientProxy;
import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.CameraUtils;

/**
 * Client destination
 * 
 * Saves and reloads camera prfoile from Aperture's config folder.
 */
public class ClientDestination extends AbstractDestination
{
    public ClientDestination(String filename)
    {
        super(filename);
    }

    @Override
    public boolean equals(Object obj)
    {
        return super.equals(obj) && obj instanceof ClientDestination;
    }

    @Override
    public void save(CameraProfile profile)
    {
        try
        {
            FileUtils.write(new File(ClientProxy.getClientCameras(), this.filename + ".json"), CameraUtils.toJSON(profile));

            /* TODO: inform user about success */
        }
        catch (Exception e)
        {
            /* TODO: inform user about error */
        }
    }

    @Override
    public void reload()
    {
        try
        {
            String json = FileUtils.readFileToString(new File(ClientProxy.getClientCameras(), this.filename + ".json"));
            CameraProfile newProfile = CameraUtils.cameraJSONBuilder(false).fromJson(json, CameraProfile.class);

            newProfile.setDestination(this);
            newProfile.dirty = false;
            ClientProxy.control.addProfile(newProfile);

            /* TODO: inform user about success */
        }
        catch (Exception e)
        {
            /* TODO: inform user about error */
        }
    }
}