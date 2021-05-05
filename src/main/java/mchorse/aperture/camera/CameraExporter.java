package mchorse.aperture.camera;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import info.ata4.minecraft.minema.Minema;
import info.ata4.minecraft.minema.MinemaAPI;
import info.ata4.minecraft.minema.client.config.MinemaConfig;
import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.minema.MinemaIntegration;
import net.minecraftforge.fml.common.Optional;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

/**
 * This is an exporter for the camera movement to a json file.
 * @author Christian F. (known as Chryfi)
 */
public class CameraExporter
{
    private boolean relativeOrigin = false;
    private JsonArray trackingData = new JsonArray();
    private double[] trackingInitialPos = {0,0,0};
    private int heldframes = 1; //to determine double frames with minema's held frames
    public boolean building = false;

    public void setRelativeOrigin(boolean state)
    {
        this.relativeOrigin = state;
    }

    public void setOriginX(double x)
    {
        this.trackingInitialPos[0] = x;
    }

    public void setOriginY(double y)
    {
        this.trackingInitialPos[1] = y - 1.62; //1.62 for default eye height
    }

    public void setOriginZ(double z)
    {
        this.trackingInitialPos[2] = z;
    }

    /**
     * reset the class variables to default values - standby for next export process
     */
    public void reset()
    {
        this.trackingData = new JsonArray();
        this.trackingInitialPos[0] = 0;
        this.trackingInitialPos[1] = 0;
        this.trackingInitialPos[2] = 0;
        this.building = false;
        this.heldframes = 1;
    }

    @Optional.Method(modid = Minema.MODID)
    public void addFrame(Position position)
    {
        JsonObject frame = new JsonObject();
        JsonArray positionData = new JsonArray();
        JsonArray angleData = new JsonArray();

        if (!this.relativeOrigin && this.trackingInitialPos[0] == 0 && this.trackingInitialPos[1] == 0 && this.trackingInitialPos[2] == 0)
        {
            this.trackingInitialPos[0] = position.point.x;
            this.trackingInitialPos[1] = position.point.y;
            this.trackingInitialPos[2] = position.point.z;
        }

        positionData.add(position.point.x - this.trackingInitialPos[0]);
        positionData.add(position.point.y - this.trackingInitialPos[1]);
        positionData.add(position.point.z - this.trackingInitialPos[2]);

        angleData.add(position.angle.fov);
        angleData.add(position.angle.roll);
        angleData.add(position.angle.yaw);
        angleData.add(position.angle.pitch);

        frame.add("position", positionData);
        frame.add("angle", angleData);

        if (MinemaIntegration.isAvailable())
        {
            if (Minema.instance.getConfig().heldFrames.get()>1)
            {
                if (this.heldframes>1)
                {
                    JsonObject prevFrame = this.trackingData.get(this.trackingData.size()-1).getAsJsonObject();
                    JsonArray prevPositionData = prevFrame.get("position").getAsJsonArray();
                    JsonArray prevAngleData = prevFrame.get("angle").getAsJsonArray();

                    if (prevAngleData.equals(angleData) && prevPositionData.equals(positionData))
                    {
                        this.heldframes++;
                        return;
                    }
                    else
                    {
                        this.heldframes = 1;
                    }
                }

                this.heldframes = (this.heldframes<Minema.instance.getConfig().heldFrames.get()) ? this.heldframes + 1 : 1;
            }
        }

        this.trackingData.add(frame);

        this.building = true;
    }

    @Optional.Method(modid = Minema.MODID)
    public void exportTrackingData(String filename)
    {
        if (MinemaIntegration.isAvailable())
        {
            try
            {
                FileWriter file = new FileWriter(MinemaAPI.getCapturePath().toURI().getPath() + filename);

                file.write(this.trackingData.toString());
                file.close();
                System.out.println("Successfully created the tracking data file.");
            }
            catch (IOException e)
            {
                System.out.println("An error occurred during writing the tracking data file.");
                e.printStackTrace();
            }
        }
    }
}
