package mchorse.aperture.client;

import java.lang.reflect.Field;

import org.lwjgl.input.Keyboard;

import mchorse.aperture.Aperture;
import mchorse.aperture.ClientProxy;
import mchorse.aperture.camera.CameraControl;
import mchorse.aperture.camera.smooth.SmoothCamera;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.aperture.events.CameraProfileChangedEvent;
import mchorse.aperture.utils.L10n;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.command.CommandException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Separate event handler for keyboard events
 *
 * This keyboard handler handles, yet, only key bindings related to the camera,
 * and by the way, there are a log of them!
 */
@SideOnly(Side.CLIENT)
public class KeyboardHandler
{
    /**
     * Whether it's in replay 
     */
    public static boolean inReplay;

    private Minecraft mc = Minecraft.getMinecraft();

    /* Camera profile keys */
    private KeyBinding toggleRender;

    private KeyBinding startRunning;
    private KeyBinding stopRunning;

    /* Roll and FOV */
    public KeyBinding addRoll;
    public KeyBinding reduceRoll;
    private KeyBinding resetRoll;

    public KeyBinding addFov;
    public KeyBinding reduceFov;
    private KeyBinding resetFov;

    /* Camera control */
    private KeyBinding stepUp;
    private KeyBinding stepDown;
    private KeyBinding stepLeft;
    private KeyBinding stepRight;
    private KeyBinding stepFront;
    private KeyBinding stepBack;

    private KeyBinding rotateUp;
    private KeyBinding rotateDown;
    private KeyBinding rotateLeft;
    private KeyBinding rotateRight;

    /* Misc. */
    private KeyBinding cameraEditor;
    private KeyBinding smoothCamera;

    /**
     * Check whether ReplayMod's replay is currently running 
     */
    public static boolean checkReplayWorld()
    {
        try
        {
            Class replayMod = Class.forName("com.replaymod.replay.ReplayModReplay");
            Field replayField = replayMod.getField("instance");
            Field replayHandlerField = replayMod.getDeclaredField("replayHandler");

            replayHandlerField.setAccessible(true);

            if (replayHandlerField.get(replayField.get(null)) != null)
            {
                return true;
            }
        }
        catch (Exception e)
        {}

        return false;
    }

    /**
     * Create and register key bindings for mod
     */
    public KeyboardHandler()
    {
        /* Key categories */
        String camera = "key.aperture.camera";
        String profile = "key.aperture.profile.title";
        String control = "key.aperture.control.title";
        String misc = "key.aperture.misc";

        this.toggleRender = new KeyBinding("key.aperture.profile.toggle", Keyboard.KEY_P, profile);

        ClientRegistry.registerKeyBinding(this.toggleRender);

        this.startRunning = new KeyBinding("key.aperture.profile.start", Keyboard.KEY_Z, profile);
        this.stopRunning = new KeyBinding("key.aperture.profile.stop", Keyboard.KEY_X, profile);

        ClientRegistry.registerKeyBinding(this.startRunning);
        ClientRegistry.registerKeyBinding(this.stopRunning);

        /* Roll and FOV */
        this.addRoll = new KeyBinding("key.aperture.roll.add", Keyboard.KEY_NONE, camera);
        this.reduceRoll = new KeyBinding("key.aperture.roll.reduce", Keyboard.KEY_NONE, camera);
        this.resetRoll = new KeyBinding("key.aperture.roll.reset", Keyboard.KEY_NONE, camera);

        ClientRegistry.registerKeyBinding(this.addRoll);
        ClientRegistry.registerKeyBinding(this.reduceRoll);
        ClientRegistry.registerKeyBinding(this.resetRoll);

        this.addFov = new KeyBinding("key.aperture.fov.add", Keyboard.KEY_NONE, camera);
        this.reduceFov = new KeyBinding("key.aperture.fov.reduce", Keyboard.KEY_NONE, camera);
        this.resetFov = new KeyBinding("key.aperture.fov.reset", Keyboard.KEY_NONE, camera);

        ClientRegistry.registerKeyBinding(this.addFov);
        ClientRegistry.registerKeyBinding(this.reduceFov);
        ClientRegistry.registerKeyBinding(this.resetFov);

        /* Camera control */
        this.stepUp = new KeyBinding("key.aperture.control.stepUp", Keyboard.KEY_NONE, control);
        this.stepDown = new KeyBinding("key.aperture.control.stepDown", Keyboard.KEY_NONE, control);
        this.stepLeft = new KeyBinding("key.aperture.control.stepLeft", Keyboard.KEY_NONE, control);
        this.stepRight = new KeyBinding("key.aperture.control.stepRight", Keyboard.KEY_NONE, control);
        this.stepFront = new KeyBinding("key.aperture.control.stepFront", Keyboard.KEY_NONE, control);
        this.stepBack = new KeyBinding("key.aperture.control.stepBack", Keyboard.KEY_NONE, control);

        ClientRegistry.registerKeyBinding(this.stepUp);
        ClientRegistry.registerKeyBinding(this.stepDown);
        ClientRegistry.registerKeyBinding(this.stepLeft);
        ClientRegistry.registerKeyBinding(this.stepRight);
        ClientRegistry.registerKeyBinding(this.stepFront);
        ClientRegistry.registerKeyBinding(this.stepBack);

        this.rotateUp = new KeyBinding("key.aperture.control.rotateUp", Keyboard.KEY_NONE, control);
        this.rotateDown = new KeyBinding("key.aperture.control.rotateDown", Keyboard.KEY_NONE, control);
        this.rotateLeft = new KeyBinding("key.aperture.control.rotateLeft", Keyboard.KEY_NONE, control);
        this.rotateRight = new KeyBinding("key.aperture.control.rotateRight", Keyboard.KEY_NONE, control);

        ClientRegistry.registerKeyBinding(this.rotateUp);
        ClientRegistry.registerKeyBinding(this.rotateDown);
        ClientRegistry.registerKeyBinding(this.rotateLeft);
        ClientRegistry.registerKeyBinding(this.rotateRight);

        /* Misc */
        this.cameraEditor = new KeyBinding("key.aperture.camera_editor", Keyboard.KEY_C, misc);
        this.smoothCamera = new KeyBinding("key.aperture.smooth_camera", Keyboard.KEY_NONE, misc);

        ClientRegistry.registerKeyBinding(this.cameraEditor);
        ClientRegistry.registerKeyBinding(this.smoothCamera);
    }

    @SubscribeEvent
    public void onUserLogIn(ClientConnectedToServerEvent event)
    {
        if (Loader.isModLoaded("replaymod"))
        {
            inReplay = checkReplayWorld();
        }
    }

    @SubscribeEvent
    public void onUserLogOut(ClientDisconnectionFromServerEvent event)
    {
        ClientProxy.control.reset();

        if (Loader.isModLoaded("replaymod"))
        {
            inReplay = false;
        }
    }

    /**
     * Handle keys
     */
    @SubscribeEvent
    public void onKey(InputEvent.KeyInputEvent event)
    {
        EntityPlayer player = this.mc.player;

        /* Checking whether player is in ReplayMod's replay world */
        if (inReplay)
        {
            return;
        }

        try
        {
            this.handleCameraBindings(player);
        }
        catch (CommandException e)
        {
            L10n.error(player, e.getMessage(), e.getErrorObjects());
        }

        /* Misc. */
        if (this.cameraEditor.isPressed())
        {
            GuiCameraEditor editor = ClientProxy.getCameraEditor();

            editor.updateCameraEditor(player);
            player.setVelocity(0, 0, 0);
            this.mc.displayGuiScreen(editor);
        }

        if (this.smoothCamera.isPressed())
        {
            SmoothCamera camera = ClientProxy.renderer.smooth;
            Property enabled = Aperture.proxy.forge.getCategory("smooth").get("smooth_enabled");

            enabled.set(!enabled.getBoolean());

            Aperture.proxy.onConfigChange(Aperture.proxy.config);
            Aperture.proxy.forge.save();

            if (camera.enabled)
            {
                camera.set(player.rotationYaw, -player.rotationPitch);
            }
        }
    }

    /**
     * Here goes all key bindings that related to camera profile.
     */
    private void handleCameraBindings(EntityPlayer player) throws CommandException
    {
        CameraControl control = ClientProxy.control;

        if (this.toggleRender.isPressed())
        {
            ClientProxy.renderer.toggleRender();
        }

        /* Starting stopping */
        if (this.startRunning.isPressed())
        {
            ClientProxy.runner.start(ClientProxy.control.currentProfile);
        }
        else if (this.stopRunning.isPressed())
        {
            ClientProxy.runner.stop();
        }

        if (this.resetRoll.isPressed())
        {
            control.resetRoll();
        }

        if (this.resetFov.isPressed())
        {
            control.resetFOV();
        }
    }

    /**
     * Client tick event is used for doing stuff like tick based stuff
     */
    @SubscribeEvent
    public void onClientTick(ClientTickEvent event)
    {
        EntityPlayer player = this.mc.player;

        /* Camera control keys handling */
        if (player != null)
        {
            if (inReplay)
            {
                return;
            }

            if (!ClientProxy.renderer.smooth.enabled)
            {
                CameraControl control = ClientProxy.control;

                /* Roll key handling */
                if (this.addRoll.isKeyDown())
                {
                    control.roll += 0.5F;
                }
                else if (this.reduceRoll.isKeyDown())
                {
                    control.roll -= 0.5F;
                }

                /* FOV key handling */
                if (this.addFov.isKeyDown())
                {
                    Minecraft.getMinecraft().gameSettings.fovSetting += 0.25F;
                }
                else if (this.reduceFov.isKeyDown())
                {
                    Minecraft.getMinecraft().gameSettings.fovSetting += -0.25F;
                }
            }

            double factor = Aperture.proxy.config.camera_step_factor;
            double angleFactor = Aperture.proxy.config.camera_rotate_factor;

            float yaw = player.rotationYaw;
            float pitch = player.rotationPitch;

            if (this.rotateUp.isKeyDown() || this.rotateDown.isKeyDown())
            {
                pitch += (this.rotateUp.isKeyDown() ? -angleFactor : angleFactor);
            }

            if (this.rotateLeft.isKeyDown() || this.rotateRight.isKeyDown())
            {
                yaw += (this.rotateLeft.isKeyDown() ? -angleFactor : angleFactor);
            }

            double x = player.posX;
            double y = player.posY;
            double z = player.posZ;

            double xx = 0;
            double yy = 0;
            double zz = 0;

            if (this.stepUp.isKeyDown() || this.stepDown.isKeyDown())
            {
                yy = (this.stepUp.isKeyDown() ? factor : -factor);
            }

            if (this.stepLeft.isKeyDown() || this.stepRight.isKeyDown())
            {
                xx = (this.stepLeft.isKeyDown() ? factor : -factor);
            }

            if (this.stepFront.isKeyDown() || this.stepBack.isKeyDown())
            {
                zz = (this.stepFront.isKeyDown() ? factor : -factor);
            }

            if (xx != 0 || yy != 0 || zz != 0 || yaw != player.rotationYaw || pitch != player.rotationPitch)
            {
                Vec3d vec = new Vec3d(xx, yy, zz);

                vec = vec.rotateYaw(-yaw / 180 * (float) Math.PI);

                x += vec.xCoord;
                y += vec.yCoord;
                z += vec.zCoord;

                player.setPositionAndRotation(x, y, z, yaw, pitch);
                player.setVelocity(0, 0, 0);
            }
        }
    }

    /* Camera profile handlers */

    /**
     * When camera profile is getting modified, this event is getting invoked
     * 
     * Works only on the client
     */
    @SubscribeEvent
    public void onCameraProfileChanged(CameraProfileChangedEvent event)
    {
        GuiScreen screen = Minecraft.getMinecraft().currentScreen;

        if (screen instanceof GuiCameraEditor)
        {
            ((GuiCameraEditor) screen).cameraProfileWasChanged(event.profile);
        }
    }
}