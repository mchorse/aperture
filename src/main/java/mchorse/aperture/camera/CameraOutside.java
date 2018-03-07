package mchorse.aperture.camera;

import com.mojang.authlib.GameProfile;

import mchorse.aperture.Aperture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.EntityViewRenderEvent.FogColors;
import net.minecraftforge.client.event.EntityViewRenderEvent.FogDensity;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Camera outside handler
 * 
 * This class is responsible for handling camera outside mode
 */
public class CameraOutside
{
    /**
     * Entity which is used as a camera in the outside camera mode 
     */
    public EntityPlayer camera;

    /**
     * Whether outside mode is active
     */
    public boolean active;

    private Minecraft mc = Minecraft.getMinecraft();

    /**
     * Start handling outside mode 
     */
    public void start()
    {
        this.camera = new EntityOtherPlayerMP(this.mc.world, new GameProfile(null, "Camera"));
        this.active = true;
        this.mc.setRenderViewEntity(this.camera);

        GuiIngameForge.renderCrosshairs = false;
        MinecraftForge.EVENT_BUS.register(this);
    }

    /**
     * Stop handling outside mode 
     */
    public void stop()
    {
        this.camera = null;
        this.active = false;
        this.mc.setRenderViewEntity(this.mc.player);

        GuiIngameForge.renderCrosshairs = true;
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    /**
     * This event is responsible 
     */
    @SubscribeEvent
    public void onFogColor(FogColors event)
    {
        if (Aperture.proxy.config.camera_outside)
        {
            this.mc.gameSettings.thirdPersonView = 0;
            this.mc.setRenderViewEntity(this.camera);
        }
    }

    /**
     * This fixes the orientation of particles. It basically runs 
     * after {@link ActiveRenderInfo#updateRenderInfo(EntityPlayer, boolean)} 
     * gets called in {@link EntityRenderer} renderWorldPass() method, 
     * so that those fields in {@link ActiveRenderInfo} would get correct 
     * values based on the camera.  
     */
    @SubscribeEvent
    public void onFogDensity(FogDensity event)
    {
        EntityPlayer player = this.mc.player;

        double prevX = player.posX;
        double prevY = player.posY;
        double prevZ = player.posZ;
        float rotX = player.rotationYaw;
        float rotY = player.rotationPitch;

        player.setPositionAndRotation(this.camera.posX, this.camera.posY, this.camera.posZ, this.camera.rotationYaw, this.camera.rotationPitch);
        ActiveRenderInfo.updateRenderInfo(player, false);
        player.setPositionAndRotation(prevX, prevY, prevZ, rotX, rotY);
    }

    /**
     * Before player gets rendered, we must substitute the render view 
     * entity (it should be using {@link Minecraft#getRenderViewEntity()}, 
     * but it doesn't uses it), so we got to assign it to the client 
     * player.
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onPreRenderPlayer(RenderPlayerEvent.Pre event)
    {
        if (!Aperture.proxy.config.camera_outside_hide_player && event.getEntityPlayer() == this.mc.player)
        {
            this.mc.getRenderManager().renderViewEntity = this.mc.player;
        }
    }

    /**
     * After player gets rendered, we must turn it back to camera
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onPostRenderPlayer(RenderPlayerEvent.Post event)
    {
        if (!Aperture.proxy.config.camera_outside_hide_player && event.getEntityPlayer() == this.mc.player)
        {
            this.mc.getRenderManager().renderViewEntity = this.camera;
            this.mc.gameSettings.thirdPersonView = 0;
        }
    }

    /**
     * In outside mode, the hands should be hidden 
     */
    @SubscribeEvent
    public void onRenderHands(RenderHandEvent event)
    {
        event.setCanceled(true);
    }

    /**
     * This substitutes the render view entity back to player, so the 
     * inventory was rendered correctly. 
     */
    @SubscribeEvent
    public void onPreRenderOverlay(RenderGameOverlayEvent.Pre event)
    {
        if (event.getType() == ElementType.ALL)
        {
            this.mc.setRenderViewEntity(this.mc.player);
        }
    }

    /**
     * And in post render overlay, we return the render view entity back 
     * to camera (just in case) 
     */
    @SubscribeEvent
    public void onPostRenderOverlay(RenderGameOverlayEvent.Post event)
    {
        if (event.getType() == ElementType.ALL)
        {
            this.mc.setRenderViewEntity(this.camera);
        }
    }
}