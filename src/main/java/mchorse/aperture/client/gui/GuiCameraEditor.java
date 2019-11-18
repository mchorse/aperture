package mchorse.aperture.client.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mchorse.aperture.camera.fixtures.IdleFixture;
import mchorse.aperture.camera.fixtures.PathFixture;
import mchorse.aperture.client.gui.panels.GuiPathFixturePanel;
import org.lwjgl.input.Keyboard;

import com.google.gson.JsonParser;

import mchorse.aperture.Aperture;
import mchorse.aperture.ClientProxy;
import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.CameraRunner;
import mchorse.aperture.camera.data.Angle;
import mchorse.aperture.camera.data.Point;
import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.client.gui.GuiPlaybackScrub.IScrubListener;
import mchorse.aperture.client.gui.config.GuiCameraConfig;
import mchorse.aperture.client.gui.config.GuiConfigCameraOptions;
import mchorse.aperture.client.gui.panels.GuiAbstractFixturePanel;
import mchorse.aperture.events.CameraEditorEvent;
import mchorse.mclib.client.gui.framework.GuiBase;
import mchorse.mclib.client.gui.framework.GuiTooltip.TooltipDirection;
import mchorse.mclib.client.gui.framework.elements.GuiButtonElement;
import mchorse.mclib.client.gui.framework.elements.GuiDelegateElement;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.GuiElements;
import mchorse.mclib.client.gui.framework.elements.GuiTrackpadElement;
import mchorse.mclib.client.gui.framework.elements.IGuiElement;
import mchorse.mclib.client.gui.utils.Resizer;
import mchorse.mclib.client.gui.widgets.buttons.GuiTextureButton;
import mchorse.mclib.utils.resources.RLUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameType;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Camera editor GUI
 *
 * This GUI provides tools for managing camera profiles. 
 */
@SideOnly(Side.CLIENT)
public class GuiCameraEditor extends GuiBase implements IScrubListener
{
    /**
     * Camera editor texture
     */
    public static final ResourceLocation EDITOR_TEXTURE = new ResourceLocation("aperture:textures/gui/camera_editor.png");

    /**
     * Registry of editing camera fixture panels. Per every fixture class type
     * there is supposed to be a class that is responsible for editing a
     * fixture.
     */
    public static final Map<Class<? extends AbstractFixture>, Class<? extends GuiAbstractFixturePanel<? extends AbstractFixture>>> PANELS = new HashMap<>();

    /* Strings */
    private String stringSpeed = I18n.format("aperture.gui.editor.speed");
    private String stringX = I18n.format("aperture.gui.panels.x");
    private String stringY = I18n.format("aperture.gui.panels.y");
    private String stringZ = I18n.format("aperture.gui.panels.z");
    private String stringYaw = I18n.format("aperture.gui.panels.yaw");
    private String stringPitch = I18n.format("aperture.gui.panels.pitch");
    private String stringRoll = I18n.format("aperture.gui.panels.roll");
    private String stringFov = I18n.format("aperture.gui.panels.fov");

    /**
     * Currently editing camera profile
     */
    private CameraProfile profile;

    /**
     * Profile runner
     */
    private CameraRunner runner;

    /**
     * Flag for observing the runner
     */
    private boolean playing = false;

    /**
     * Flag for replacing a fixture
     */
    private boolean replacing = false;

    /**
     * Whether creation mode is activated
     */
    public boolean creating = false;
    public List<Integer> markers = new ArrayList<Integer>();

    /**
     * FOV which was user had before entering the GUI 
     */
    private float lastFov = 70.0F;
    private float lastRoll = 0;
    private GameType lastGameMode = GameType.NOT_SET;
    public ResourceLocation overlayLocation;

    /**
     * This property saves state for the sync option, to allow more friendly
     */
    public boolean haveScrubbed;

    /**
     * Whether cameras are sync'd every render tick. Usable for target based
     * fixtures only
     */
    public boolean syncing;

    /**
     * Maximum scrub duration
     */
    public int maxScrub = 0;

    /**
     * Whether current camera fixture should be played on repeat 
     */
    public boolean repeat;

    /**
     * Flight mode 
     */
    public Flight flight = new Flight();

    /**
     * Position 
     */
    public Position position = new Position(0, 0, 0, 0, 0);

    /**
     * Map of created fixture panels
     */
    public Map<Class<? extends AbstractFixture>, GuiAbstractFixturePanel<? extends AbstractFixture>> panels = new HashMap<>();

    /* Display options */

    /**
     * Whether camera editor should display camera information 
     */
    public boolean displayPosition;

    /**
     * Render rule of thirds 
     */
    public boolean ruleOfThirds;

    /**
     * Render black bars 
     */
    public boolean letterBox;

    /**
     * Aspect ratio for black bars 
     */
    public float aspectRatio = 16F / 9F;

    /* GUI fields */

    /**
     * Play/pause button (very clever name, eh?)
     */
    public GuiButtonElement<GuiTextureButton> plause;

    public GuiButtonElement<GuiTextureButton> nextFrame;
    public GuiButtonElement<GuiTextureButton> prevFrame;

    public GuiButtonElement<GuiTextureButton> toPrevFixture;
    public GuiButtonElement<GuiTextureButton> toNextFixture;

    public GuiButtonElement<GuiTextureButton> moveForward;
    public GuiButtonElement<GuiTextureButton> moveBackward;

    public GuiButtonElement<GuiTextureButton> copyPosition;
    public GuiButtonElement<GuiTextureButton> moveDuration;

    public GuiButtonElement<GuiTextureButton> save;
    public GuiButtonElement<GuiTextureButton> openConfig;
    public GuiButtonElement<GuiTextureButton> openModifiers;
    public GuiButtonElement<GuiTextureButton> openProfiles;

    public GuiButtonElement<GuiTextureButton> add;
    public GuiButtonElement<GuiTextureButton> dupe;
    public GuiButtonElement<GuiTextureButton> replace;
    public GuiButtonElement<GuiTextureButton> remove;

    public GuiButtonElement<GuiTextureButton> goTo;
    public GuiButtonElement<GuiTextureButton> cut;
    public GuiButtonElement<GuiTextureButton> creation;
    public GuiTrackpadElement frame;

    /* Widgets */
    public GuiCameraConfig config;
    public GuiFixturesPopup popup;
    public GuiPlaybackScrub scrub;
    public GuiProfilesManager profiles;
    public GuiConfigCameraOptions cameraOptions;
    public GuiModifiersManager modifiers;
    public GuiDelegateElement<GuiAbstractFixturePanel> panel;
    public GuiElements<IGuiElement> hidden = new GuiElements<IGuiElement>();

    /**
     * Initialize the camera editor with a camera profile.
     */
    public GuiCameraEditor(Minecraft mc, CameraRunner runner)
    {
        this.runner = runner;

        this.panel = new GuiDelegateElement<GuiAbstractFixturePanel>(mc, null);
        this.scrub = new GuiPlaybackScrub(mc, this, null);
        this.popup = new GuiFixturesPopup(mc, (fixture) ->
        {
            fixture.fromPlayer(this.getCamera());
            this.createFixture(fixture);
            this.popup.toggleVisible();
        });

        this.profiles = new GuiProfilesManager(mc, this);
        this.cameraOptions = new GuiConfigCameraOptions(mc, this);
        this.modifiers = new GuiModifiersManager(mc, this);
        this.config = new GuiCameraConfig(mc, this);

        /* Setup elements */
        this.toNextFixture = GuiButtonElement.icon(mc, EDITOR_TEXTURE, 64, 0, 64, 16, (b) -> this.jumpToNextFixture()).tooltip(I18n.format("aperture.gui.tooltips.jump_next_fixture"), TooltipDirection.BOTTOM);
        this.nextFrame = GuiButtonElement.icon(mc, EDITOR_TEXTURE, 32, 0, 32, 16, (b) -> this.jumpToNextFrame()).tooltip(I18n.format("aperture.gui.tooltips.jump_next_frame"), TooltipDirection.BOTTOM);
        this.plause = GuiButtonElement.icon(mc, EDITOR_TEXTURE, 0, 0, 0, 0, (b) ->
        {
            this.setFlight(false);
            this.runner.toggle(this.profile, this.scrub.value);
            this.updatePlauseButton();

            this.playing = this.runner.isRunning();

            if (!this.playing)
            {
                this.runner.attachOutside();
                this.updatePlayerCurrently(0.0F);
            }

            ClientProxy.EVENT_BUS.post(new CameraEditorEvent.Playback(this, this.playing, this.scrub.value));
        }).tooltip(I18n.format("aperture.gui.tooltips.plause"), TooltipDirection.BOTTOM);
        this.prevFrame = GuiButtonElement.icon(mc, EDITOR_TEXTURE, 48, 0, 48, 16, (b) -> this.jumpToPrevFrame()).tooltip(I18n.format("aperture.gui.tooltips.jump_prev_frame"), TooltipDirection.BOTTOM);
        this.toPrevFixture = GuiButtonElement.icon(mc, EDITOR_TEXTURE, 80, 0, 80, 16, (b) -> this.jumpToPrevFixture()).tooltip(I18n.format("aperture.gui.tooltips.jump_prev_fixture"), TooltipDirection.BOTTOM);

        this.openProfiles = GuiButtonElement.icon(mc, EDITOR_TEXTURE, 96, 0, 96, 16, (b) -> this.hidePopups(this.profiles)).tooltip(I18n.format("aperture.gui.tooltips.profiles"), TooltipDirection.BOTTOM);
        this.openConfig = GuiButtonElement.icon(mc, EDITOR_TEXTURE, 208, 0, 208, 16, (b) -> this.hidePopups(this.config)).tooltip(I18n.format("aperture.gui.tooltips.config"), TooltipDirection.BOTTOM);
        this.openModifiers = GuiButtonElement.icon(mc, EDITOR_TEXTURE, 80, 32, 80, 48, (b) -> this.hidePopups(this.modifiers)).tooltip(I18n.format("aperture.gui.tooltips.modifiers"), TooltipDirection.BOTTOM);
        this.save = GuiButtonElement.icon(mc, EDITOR_TEXTURE, 0, 0, 0, 0, (b) -> this.profile.save()).tooltip(I18n.format("aperture.gui.tooltips.save"), TooltipDirection.BOTTOM);

        this.add = GuiButtonElement.icon(mc, EDITOR_TEXTURE, 224, 0, 224, 16, (b) -> this.hideReplacingPopups(this.popup, false)).tooltip(I18n.format("aperture.gui.tooltips.add"), TooltipDirection.BOTTOM);
        this.dupe = GuiButtonElement.icon(mc, EDITOR_TEXTURE, 176, 32, 176, 48, (b) -> this.dupeFixture()).tooltip(I18n.format("aperture.gui.tooltips.dupe"), TooltipDirection.BOTTOM);
        this.replace = GuiButtonElement.icon(mc, EDITOR_TEXTURE, 208, 32, 208, 48, (b) -> this.hideReplacingPopups(this.popup, true)).tooltip(I18n.format("aperture.gui.tooltips.replace"), TooltipDirection.BOTTOM);
        this.remove = GuiButtonElement.icon(mc, EDITOR_TEXTURE, 240, 0, 240, 16, (b) -> this.removeFixture()).tooltip(I18n.format("aperture.gui.tooltips.remove"), TooltipDirection.BOTTOM);

        this.creation = GuiButtonElement.icon(mc, EDITOR_TEXTURE, 224, 32, 224, 48, (b) -> this.toggleCreation()).tooltip(I18n.format("aperture.gui.tooltips.creation"), TooltipDirection.BOTTOM);
        this.cut = GuiButtonElement.icon(mc, EDITOR_TEXTURE, 192, 32, 192, 48, (b) -> this.cutFixture()).tooltip(I18n.format("aperture.gui.tooltips.cut"), TooltipDirection.BOTTOM);
        this.goTo = GuiButtonElement.icon(mc, EDITOR_TEXTURE, 144, 32, 144, 48, (b) -> this.frame.toggleVisible()).tooltip(I18n.format("aperture.gui.tooltips.goto"), TooltipDirection.BOTTOM);
        this.moveForward = GuiButtonElement.icon(mc, EDITOR_TEXTURE, 144, 0, 144, 16, (b) -> this.moveTo(1)).tooltip(I18n.format("aperture.gui.tooltips.move_up"), TooltipDirection.BOTTOM);
        this.moveDuration = GuiButtonElement.icon(mc, EDITOR_TEXTURE, 192, 0, 192, 16, (b) -> this.shiftDurationToCursor()).tooltip(I18n.format("aperture.gui.tooltips.move_duration"), TooltipDirection.BOTTOM);
        this.copyPosition = GuiButtonElement.icon(mc, EDITOR_TEXTURE, 176, 0, 176, 16, (b) -> this.editFixture()).tooltip(I18n.format("aperture.gui.tooltips.copy_position"), TooltipDirection.BOTTOM);
        this.moveBackward = GuiButtonElement.icon(mc, EDITOR_TEXTURE, 160, 0, 160, 16, (b) -> this.moveTo(-1)).tooltip(I18n.format("aperture.gui.tooltips.move_down"), TooltipDirection.BOTTOM);

        this.frame = new GuiTrackpadElement(mc, "Frame", (value) -> this.scrub.setValueFromScrub(value.intValue()));

        this.frame.trackpad.title = "Frame";
        this.frame.trackpad.amplitude = 1.0F;
        this.frame.trackpad.integer = true;

        /* Button placement */
        this.toNextFixture.resizer().parent(this.area).set(0, 2, 16, 16).x(0.5F, 32);
        this.nextFrame.resizer().relative(this.toNextFixture.resizer()).set(-20, 0, 16, 16);
        this.plause.resizer().relative(this.nextFrame.resizer()).set(-20, 0, 16, 16);
        this.prevFrame.resizer().relative(this.plause.resizer()).set(-20, 0, 16, 16);
        this.toPrevFixture.resizer().relative(this.prevFrame.resizer()).set(-20, 0, 16, 16);

        this.openProfiles.resizer().parent(this.area).set(0, 2, 16, 16).x(1, -18);
        this.openConfig.resizer().relative(this.openProfiles.resizer()).set(-20, 0, 16, 16);
        this.openModifiers.resizer().relative(this.openConfig.resizer()).set(-20, 0, 16, 16);
        this.save.resizer().relative(this.openModifiers.resizer()).set(-20, 0, 16, 16);

        this.add.resizer().relative(this.save.resizer()).set(-90, 0, 16, 16);
        this.dupe.resizer().relative(this.add.resizer()).set(20, 0, 16, 16);
        this.replace.resizer().relative(this.dupe.resizer()).set(20, 0, 16, 16);
        this.remove.resizer().relative(this.replace.resizer()).set(20, 0, 16, 16);

        this.creation.resizer().relative(this.cut.resizer()).set(20, 0, 16, 16);
        this.cut.resizer().relative(this.goTo.resizer()).set(20, 0, 16, 16);
        this.goTo.resizer().parent(this.area).set(82, 2, 16, 16);
        this.moveForward.resizer().relative(this.goTo.resizer()).set(-20, 0, 16, 16);
        this.moveDuration.resizer().relative(this.moveForward.resizer()).set(-20, 0, 16, 16);
        this.copyPosition.resizer().relative(this.moveDuration.resizer()).set(-20, 0, 16, 16);
        this.moveBackward.resizer().relative(this.copyPosition.resizer()).set(-20, 0, 16, 16);

        this.frame.resizer().relative(this.goTo.resizer()).set(-2, 18, 80, 20);

        /* Setup areas of widgets */
        this.scrub.resizer().parent(this.area).set(10, 0, 0, 20).y(1, -20).w(1, -20);

        this.panel.resizer().parent(this.area).set(10, 40, 0, 0).w(1, -20).h(1, -70);
        this.popup.resizer().relative(this.add.resizer()).set(-44, 18, 62, 122);
        this.config.resizer().relative(this.panel.resizer()).set(0, -20, 160, 0).x(1, -180 + 10).h(1, 20);
        this.profiles.resizer().relative(this.panel.resizer()).set(0, -20, 160, 0).x(1, -160 + 10).h(1, 20);
        this.modifiers.resizer().relative(this.panel.resizer()).set(0, -20, 220, 0).x(1, -260 + 10).h(1, 20);

        /* Adding everything */
        this.hidden.add(this.toNextFixture, this.nextFrame, this.plause, this.prevFrame, this.toPrevFixture);
        this.hidden.add(this.creation, this.cut, this.goTo, this.moveForward, this.moveDuration, this.copyPosition, this.moveBackward);
        this.hidden.add(this.add, this.dupe, this.replace, this.remove, this.save, this.openConfig, this.openModifiers);
        this.hidden.add(this.scrub, this.panel, this.frame, this.popup, this.config, this.modifiers);

        this.cameraProfileWasChanged(this.profile);
        this.updatePlauseButton();
        this.updateValues();

        this.hidePopups(this.profiles);
        this.frame.setVisible(false);

        this.elements.add(this.hidden, this.openProfiles, this.profiles, this.cameraOptions.overlayPicker);

        /* Let other classes have fun with camera editor's fields' 
         * position and such */
        ClientProxy.EVENT_BUS.post(new CameraEditorEvent.Init(this));
    }

    /**
     * Teleport player and setup position, motion and angle based on the value
     * was scrubbed from playback scrubber.
     */
    @Override
    public void scrubbed(GuiPlaybackScrub scrub, int value, boolean fromScrub)
    {
        if (!this.frame.trackpad.text.isFocused())
        {
            this.frame.setValue(value);
        }

        if (!this.runner.isRunning() && (this.syncing || this.runner.outside.active))
        {
            this.updatePlayer(value, 0.0F);
        }
        else
        {
            this.runner.ticks = value;
        }

        if (fromScrub)
        {
            this.haveScrubbed = true;

            ClientProxy.EVENT_BUS.post(new CameraEditorEvent.Scrubbed(this, this.runner.isRunning(), this.scrub.value));
        }
    }

    /**
     * Pick a camera fixture
     *
     * This method is responsible for setting current fixture panel which in
     * turn then will allow to edit properties of the camera fixture
     */
    @SuppressWarnings("unchecked")
    public void pickCameraFixture(AbstractFixture fixture, long duration)
    {
        this.setFlight(false);

        if (fixture == null)
        {
            this.scrub.index = -1;
            this.panel.setDelegate(null);
        }
        else
        {
            if (!this.panels.containsKey(fixture.getClass()))
            {
                try
                {
                    this.panels.put(fixture.getClass(), PANELS.get(fixture.getClass()).getConstructor(Minecraft.class, GuiCameraEditor.class).newInstance(this.mc, this));
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            GuiAbstractFixturePanel<AbstractFixture> panel = (GuiAbstractFixturePanel<AbstractFixture>) this.panels.get(fixture.getClass());

            if (panel != null)
            {
                this.panel.setDelegate(panel);
                panel.select(fixture, duration);

                if (this.syncing)
                {
                    this.scrub.setValue((int) panel.currentOffset());
                }

                this.scrub.index = this.profile.getAll().indexOf(fixture);
            }
            else
            {
                this.panel.setDelegate(null);
            }
        }

        this.modifiers.setFixture(fixture);
    }

    /**
     * Add a fixture to camera profile
     */
    public void createFixture(AbstractFixture fixture)
    {
        if (fixture == null)
        {
            return;
        }

        if (this.replacing && !this.profile.has(this.scrub.index))
        {
            return;
        }

        if (this.panel.delegate == null)
        {
            this.profile.add(fixture);
        }
        else
        {
            if (this.replacing)
            {
                this.profile.replace(fixture, this.scrub.index);
                this.replacing = false;
            }
            else
            {
                this.profile.add(fixture, this.scrub.index);
            }
        }

        this.updateValues();
        this.pickCameraFixture(fixture, 0);
    }

    /**
     * Duplicate current fixture 
     */
    private void dupeFixture()
    {
        int index = this.scrub.index;

        if (this.profile.has(index))
        {
            AbstractFixture fixture = this.profile.get(index).clone();

            this.profile.add(fixture);
            this.pickCameraFixture(fixture, 0);
            this.updateValues();
        }
    }

    /**
     * Remove current fixture 
     */
    private void removeFixture()
    {
        int index = this.scrub.index;

        if (this.profile.has(index))
        {
            this.profile.remove(index);
            this.scrub.index--;

            if (this.scrub.index >= 0)
            {
                this.pickCameraFixture(this.profile.get(this.scrub.index), 0);
            }
            else
            {
                this.pickCameraFixture(null, 0);
            }

            this.updateValues();
        }
    }

    /**
     * Toggles creation mode which allows creating fixtures by placing markers
     */
    private void toggleCreation()
    {
        this.creating = !this.creating;

        if (!this.creating)
        {
            Collections.sort(this.markers);

            long duration = this.profile.getDuration();

            for (Integer tick : this.markers)
            {
                long difference = tick - duration;

                if (tick < duration || difference <= 0) continue;

                IdleFixture fixture = new IdleFixture(difference);

                fixture.fromPlayer(this.getCamera());
                this.profile.add(fixture);

                duration += difference;
            }

            this.updateValues();
            this.markers.clear();
        }
    }

    /**
     * Add a creation marker
     */
    public void addMarker(int tick)
    {
        if (this.markers.contains(tick))
        {
            this.markers.remove((Integer) tick);
        }
        else
        {
            this.markers.add(tick);
        }
    }

    /**
     * Cut a fixture currently under playback's cursor in two pieces 
     */
    private void cutFixture()
    {
        if (this.profile != null)
        {
            this.profile.cut(this.scrub.value);
        }
    }

    /**
     * Camera profile was selected from the profile manager 
     */
    public void selectProfile(CameraProfile profile)
    {
        boolean same = profile == this.profile;
        ClientProxy.control.currentProfile = profile;

        this.setProfile(profile);
        this.cameraProfileWasChanged(profile);

        if (!same)
        {
            this.pickCameraFixture(null, 0);
        }
    }

    public void cameraProfileWasChanged(CameraProfile profile)
    {
        if (this.save != null)
        {
            boolean dirty = profile == null ? false : profile.dirty;

            int x = dirty ? 128 : 112;
            int y = dirty ? 0 : 0;

            this.save.button.setTexPos(x, y).setActiveTexPos(x, y + 16);
        }
    }

    /**
     * Reset the camera editor
     */
    public void reset()
    {
        this.setProfile(null);
        this.scrub.value = 0;
        this.flight.vertical = false;
        this.replacing = false;
        this.creating = false;
        this.markers.clear();
    }

    /**
     * Set flight mode
     */
    public void setFlight(boolean flight)
    {
        if (!this.runner.isRunning() || !flight)
        {
            this.flight.enabled = flight;
        }

        this.cameraOptions.update();
        this.haveScrubbed = true;
    }

    /**
     * Set aspect ratio for letter box feature. This method parses the 
     * aspect ratio either for float or "float:float" format and sets it 
     * as aspect ratio. 
     */
    public void setAspectRatio(String aspectRatio)
    {
        float aspect = this.aspectRatio;

        try
        {
            aspect = Float.parseFloat(aspectRatio);
        }
        catch (Exception e)
        {
            try
            {
                String[] strips = aspectRatio.split(":");

                if (strips.length >= 2)
                {
                    aspect = Float.parseFloat(strips[0]) / Float.parseFloat(strips[1]);
                }
            }
            catch (Exception ee)
            {}
        }

        this.aspectRatio = aspect;
    }

    public void addPathPoint()
    {
        if (this.panel.delegate != null && this.panel.delegate.fixture instanceof PathFixture)
        {
            ((GuiPathFixturePanel) this.panel.delegate).points.addPoint();
        }
    }

    /**
     * Set camera profile
     */
    public void setProfile(CameraProfile profile)
    {
        boolean isOldSame = this.profile == profile;

        this.profile = profile;
        this.profiles.selectProfile(profile);
        this.scrub.setProfile(profile);
        this.hidden.setVisible(profile != null);

        if (!isOldSame)
        {
            this.panel.setDelegate(null);
        }
        else if (this.panel.delegate != null)
        {
            this.scrub.index = profile.getAll().indexOf(this.panel.delegate.fixture);
        }

        if (this.profile == null)
        {
            this.profiles.setVisible(true);
        }
    }

    /**
     * Update the state of camera editor (should be invoked upon opening this 
     * screen)
     */
    public void updateCameraEditor(EntityPlayer player)
    {
        this.updateOverlay();
        this.position.set(player);
        this.selectProfile(ClientProxy.control.currentProfile);
        this.profiles.init();

        Minecraft.getMinecraft().gameSettings.hideGUI = true;
        GuiIngameForge.renderHotbar = false;
        GuiIngameForge.renderCrosshairs = false;

        this.maxScrub = 0;
        this.haveScrubbed = false;
        this.flight.enabled = false;
        this.lastFov = Minecraft.getMinecraft().gameSettings.fovSetting;
        this.lastRoll = ClientProxy.control.roll;
        this.lastGameMode = ClientProxy.runner.getGameMode(player);
        this.setAspectRatio(Aperture.proxy.config.aspect_ratio);

        if (Aperture.proxy.config.camera_spectator)
        {
            if (this.lastGameMode != GameType.SPECTATOR)
            {
                ((EntityPlayerSP) player).sendChatMessage("/gamemode 3");
            }
        }
        else
        {
            this.lastGameMode = GameType.NOT_SET;
        }

        this.runner.attachOutside();
    }

    public CameraProfile getProfile()
    {
        return this.profile;
    }

    public EntityPlayer getCamera()
    {
        return this.runner.outside.active ? this.runner.outside.camera : this.mc.thePlayer;
    }

    public void updateOverlay()
    {
        try
        {
            this.overlayLocation = RLUtils.create(new JsonParser().parse(Aperture.proxy.config.camera_editor_overlay_rl));
        }
        catch (Exception e)
        {
            this.overlayLocation = null;
        }
    }

    /**
     * Update player to current value in the scrub
     */
    public void updatePlayerCurrently(float ticks)
    {
        if ((this.syncing || this.runner.outside.active) && !this.runner.isRunning())
        {
            this.updatePlayer(this.scrub.value, ticks);
        }
    }

    /**
     * Update player
     */
    public void updatePlayer(long tick, float ticks)
    {
        long duration = this.profile.getDuration();

        tick = tick < 0 ? 0 : tick;
        tick = tick > duration ? duration : tick;

        EntityPlayer player = Minecraft.getMinecraft().thePlayer;

        this.position.set(player);
        this.profile.applyProfile(tick, ticks, this.position);

        this.position.apply(this.getCamera());
        ClientProxy.control.setRollAndFOV(this.position.angle.roll, this.position.angle.fov);
    }

    /**
     * This method should be invoked when values in the panel were modified
     */
    public void updateValues()
    {
        this.frame.trackpad.min = 0;

        if (this.profile != null)
        {
            this.scrub.max = Math.max((int) this.profile.getDuration(), this.maxScrub);
            this.scrub.setValue(this.scrub.value);
            this.frame.trackpad.max = this.profile.getDuration();
            this.frame.setValue(this.scrub.value);
        }
        else
        {
            this.scrub.max = this.maxScrub;
            this.scrub.setValue(0);
            this.frame.trackpad.max = 0;
            this.frame.setValue(0);
        }
    }

    /**
     * Makes camera profile as dirty as possible 
     */
    public void updateProfile()
    {
        if (this.profile != null)
        {
            this.profile.dirty();
        }
    }

    /**
     * Get player's current position 
     */
    public Position getPosition()
    {
        Position position = new Position(this.getCamera());

        if (this.panel.delegate != null && !this.panel.delegate.fixture.getModifiers().isEmpty())
        {
            Position withModifiers = new Position();
            this.profile.applyProfile(this.scrub.value, 0, withModifiers);

            Position noModifiers = new Position();
            this.profile.applyProfile(this.scrub.value, 0, noModifiers, false);

            /* Get difference between modified and unmodified position */
            withModifiers.point.x -= noModifiers.point.x;
            withModifiers.point.y -= noModifiers.point.y;
            withModifiers.point.z -= noModifiers.point.z;
            withModifiers.angle.yaw -= noModifiers.angle.yaw;
            withModifiers.angle.pitch -= noModifiers.angle.pitch;
            withModifiers.angle.roll -= noModifiers.angle.roll;
            withModifiers.angle.fov -= noModifiers.angle.fov;

            /* Apply the difference */
            position.point.x -= withModifiers.point.x;
            position.point.y -= withModifiers.point.y;
            position.point.z -= withModifiers.point.z;
            position.angle.yaw -= withModifiers.angle.yaw;
            position.angle.pitch -= withModifiers.angle.pitch;
            position.angle.roll -= withModifiers.angle.roll;
            position.angle.fov -= withModifiers.angle.fov;
        }

        return position;
    }

    /**
     * This GUI shouldn't pause the game, because camera runs on the world's
     * update loop.
     */
    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }

    private void hideReplacingPopups(GuiElement exception, boolean replacing)
    {
        if (this.replacing != replacing && exception.isVisible())
        {
            exception.toggleVisible();
        }

        this.replacing = replacing;

        this.popup.resizer().relative(replacing ? this.replace.resizer() : this.add.resizer());
        this.popup.resize(this.width, this.height);

        this.hidePopups(exception);
    }

    private void hidePopups(GuiElement exception)
    {
        boolean was = exception.isVisible();

        this.profiles.setVisible(false);
        this.config.setVisible(false);
        this.modifiers.setVisible(false);
        this.popup.setVisible(false);

        exception.setVisible(!was);
    }

    /**
     * Update display icon of the plause button
     */
    private void updatePlauseButton()
    {
        int x = this.runner.isRunning() ? 16 : 0;

        this.plause.button.setTexPos(x, 0).setActiveTexPos(x, 16);
    }

    /**
     * Jump to the next frame (tick)
     */
    private void jumpToNextFrame()
    {
        this.scrub.setValueFromScrub(this.scrub.value + 1);
    }

    /**
     * Jump to the previous frame (tick) 
     */
    private void jumpToPrevFrame()
    {
        this.scrub.setValueFromScrub(this.scrub.value - 1);
    }

    private void editFixture()
    {
        if (this.panel.delegate != null)
        {
            this.panel.delegate.editFixture(this.getPosition());
        }

        this.haveScrubbed = true;
    }

    /**
     * Shift duration to the cursor  
     */
    private void shiftDurationToCursor()
    {
        if (this.panel.delegate == null)
        {
            return;
        }

        /* Move duration to the scrub location */
        AbstractFixture fixture = this.profile.get(this.scrub.index);
        long offset = this.profile.calculateOffset(fixture);

        if (this.scrub.value > offset && fixture != null)
        {
            fixture.setDuration(this.scrub.value - offset);
            this.updateProfile();

            this.updateValues();
            this.panel.delegate.select(fixture, 0);
        }
    }

    /**
     * Jump to the next camera fixture
     */
    private void jumpToNextFixture()
    {
        this.scrub.setValueFromScrub((int) this.profile.calculateOffset(this.scrub.value, true));
    }

    /**
     * Jump to previous fixture
     */
    private void jumpToPrevFixture()
    {
        this.scrub.setValueFromScrub((int) this.profile.calculateOffset(this.scrub.value - 1, false));
    }

    /**
     * Move current fixture
     */
    private void moveTo(int direction)
    {
        CameraProfile profile = this.profile;
        int index = this.scrub.index;
        int to = index + direction;

        profile.move(index, to);

        if (profile.has(to))
        {
            this.scrub.index = to;
        }
    }

    @Override
    public void keyPressed(char typedChar, int keyCode)
    {
        if (keyCode == Keyboard.KEY_F1)
        {
            this.elements.setVisible(!this.elements.isVisible());
        }

        if (keyCode == Keyboard.KEY_F)
        {
            /* Toggle flight */
            this.cameraOptions.flight.mouseClicked(this.cameraOptions.flight.area.x + 1, this.cameraOptions.flight.area.y + 1, 0);
            this.cameraOptions.update();
        }

        if (keyCode == Keyboard.KEY_V)
        {
            this.flight.vertical = !this.flight.vertical;
        }

        if (this.flight.enabled)
        {
            return;
        }

        boolean shift = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);

        if (keyCode == Keyboard.KEY_S)
        {
            if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) && this.profile != null)
            {
                /* Save camera profile */
                this.save.button.playPressSound(this.mc.getSoundHandler());
                this.profile.save();
            }
            else
            {
                /* Toggle sync */
                this.cameraOptions.sync.mouseClicked(this.cameraOptions.sync.area.x + 1, this.cameraOptions.sync.area.y + 1, 0);
            }
        }
        else if (keyCode == Keyboard.KEY_O)
        {
            /* Toggle outside mode */
            this.cameraOptions.outside.mouseClicked(this.cameraOptions.outside.area.x + 1, this.cameraOptions.outside.area.y + 1, 0);
        }
        else if (keyCode == Keyboard.KEY_SPACE)
        {
            /* Play/pause */
            this.plause.mouseClicked(this.plause.area.x, this.plause.area.y, 0);
        }
        else if (keyCode == Keyboard.KEY_D)
        {
            /* Deselect current fixture */
            this.pickCameraFixture(null, 0);
        }
        else if (keyCode == Keyboard.KEY_M)
        {
            this.shiftDurationToCursor();
        }
        else if (keyCode == Keyboard.KEY_RIGHT)
        {
            if (shift)
            {
                this.jumpToNextFixture();
            }
            else
            {
                this.jumpToNextFrame();
            }
        }
        else if (keyCode == Keyboard.KEY_LEFT)
        {
            if (shift)
            {
                this.jumpToPrevFixture();
            }
            else
            {
                this.jumpToPrevFrame();
            }
        }
        else if (keyCode == Keyboard.KEY_B && this.panel.delegate != null)
        {
            /* Copy the position */
            this.editFixture();
        }
        else if (keyCode == Keyboard.KEY_N)
        {
            this.modifiers.toggleVisible();
        }
        else if (keyCode == Keyboard.KEY_R)
        {
            this.cameraOptions.repeat.mouseClicked(this.cameraOptions.repeat.area.x + 1, this.cameraOptions.repeat.area.y + 1, 0);
        }
        else if (keyCode == Keyboard.KEY_C)
        {
            this.cut.mouseClicked(this.cut.area.x + 1, this.cut.area.y + 1, 0);
        }
        else if (keyCode == Keyboard.KEY_I)
        {
            this.creation.mouseClicked(this.creation.area.x + 1, this.creation.area.y + 1, 0);
        }
    }

    @Override
    protected void closeScreen()
    {
        Minecraft.getMinecraft().gameSettings.hideGUI = false;
        Minecraft.getMinecraft().gameSettings.fovSetting = this.lastFov;
        ClientProxy.control.roll = this.lastRoll;
        GuiIngameForge.renderHotbar = true;
        GuiIngameForge.renderCrosshairs = true;

        if (!this.runner.isRunning())
        {
            this.runner.detachOutside();
        }

        if (this.lastGameMode != GameType.NOT_SET)
        {
            this.mc.thePlayer.sendChatMessage("/gamemode " + this.lastGameMode.getID());
        }

        super.closeScreen();
    }

    @Override
    protected void mouseScrolled(int x, int y, int scroll)
    {
        super.mouseScrolled(x, y, scroll);

        if (this.flight.enabled)
        {
            float factor = 1000;
            boolean zoomIn = scroll > 0;

            if ((zoomIn && this.flight.speed <= 10) || (!zoomIn && this.flight.speed < 10))
            {
                factor = 1;
            }
            else if ((zoomIn && this.flight.speed <= 100) || (!zoomIn && this.flight.speed < 100))
            {
                factor = 10;
            }
            else if ((zoomIn && this.flight.speed <= 1000) || (!zoomIn && this.flight.speed < 1000))
            {
                factor = 100;
            }

            this.flight.speed -= Math.copySign(factor, scroll);
            this.flight.speed = MathHelper.clamp_int(this.flight.speed, 1, 50000);
        }
    }

    /**
     * Draw everything on the screen
     */
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        boolean isRunning = this.runner.isRunning();

        if (this.repeat && isRunning && this.panel.delegate != null)
        {
            AbstractFixture fixture = this.panel.delegate.fixture;
            long target = this.profile.calculateOffset(fixture) + fixture.getDuration();

            if (this.runner.ticks >= target - 1)
            {
                this.scrub.setValueFromScrub((int) (target - fixture.getDuration()));
            }
        }

        if (this.flight.enabled)
        {
            this.flight.animate(this.position);
            this.position.apply(this.getCamera());
            ClientProxy.control.roll = this.position.angle.roll;
            this.mc.gameSettings.fovSetting = this.position.angle.fov;

            if (this.syncing && this.haveScrubbed)
            {
                this.editFixture();
            }
        }

        if (this.profile != null)
        {
            if (Aperture.proxy.config.camera_editor_overlay && this.overlayLocation != null)
            {
                this.mc.renderEngine.bindTexture(this.overlayLocation);
                Gui.drawModalRectWithCustomSizedTexture(0, 0, 0, 0, this.width, this.height, this.width, this.height);
            }

            /* Readjustable values for rule of thirds in case of letter 
             * box enabled */
            int rx = 0;
            int ry = 0;
            int rw = this.width;
            int rh = this.height;

            if (this.letterBox && this.aspectRatio > 0)
            {
                int width = (int) (this.aspectRatio * this.height);

                if (width != this.width)
                {
                    if (width < this.width)
                    {
                        /* Horizontal bars */
                        int w = (this.width - width) / 2;

                        Gui.drawRect(0, 0, w, this.height, 0xff000000);
                        Gui.drawRect(this.width - w, 0, this.width, this.height, 0xff000000);

                        rx = w;
                        rw -= w * 2;
                    }
                    else
                    {
                        /* Vertical bars */
                        int h = (int) (this.height - (1F / this.aspectRatio * this.width)) / 2;

                        Gui.drawRect(0, 0, this.width, h, 0xff000000);
                        Gui.drawRect(0, this.height - h, this.width, this.height, 0xff000000);

                        ry = h;
                        rh -= h * 2;
                    }
                }
            }

            if (this.ruleOfThirds && this.elements.isVisible())
            {
                int color = 0xcccc0000;

                Gui.drawRect(rx + rw / 3 - 1, ry, rx + rw / 3, ry + rh, color);
                Gui.drawRect(rx + rw - rw / 3, ry, rx + rw - rw / 3 + 1, ry + rh, color);

                Gui.drawRect(rx, ry + rh / 3 - 1, rx + rw, ry + rh / 3, color);
                Gui.drawRect(rx, ry + rh - rh / 3, rx + rw, ry + rh - rh / 3 + 1, color);
            }
        }

        if (!this.elements.isVisible() || (isRunning && Aperture.proxy.config.camera_minema))
        {
            /* Little tip for the users who don't know what they did */
            if (!isRunning)
            {
                this.fontRendererObj.drawStringWithShadow(I18n.format("aperture.gui.editor.f1"), 5, this.height - 12, 0xffffff);
            }

            return;
        }

        this.drawGradientRect(0, 0, width, 20, 0x66000000, 0);

        if (this.profiles.isVisible())
        {
            Gui.drawRect(width - 20, 0, width, 20, 0xaa000000);
        }

        Resizer panel = this.panel.resizer();

        if (this.profile != null)
        {
            if (this.config.isVisible())
            {
                Gui.drawRect(width - 40, 0, width - 20, 20, 0xaa000000);
            }

            if (this.modifiers.isVisible())
            {
                Gui.drawRect(width - 60, 0, width - 40, 20, 0xaa000000);
            }

            if (this.popup.isVisible())
            {
                if (this.replacing)
                {
                    Gui.drawRect(this.replace.area.x - 2, 0, this.replace.area.x + 18, 20, 0xaa000000);
                }
                else
                {
                    Gui.drawRect(this.add.area.x - 2, 0, this.add.area.x + 18, 20, 0xaa000000);
                }
            }

            if (this.frame.isVisible())
            {
                Gui.drawRect(this.goTo.area.x - 2, 0, this.goTo.area.x + 18, 20, 0xaa000000);
            }

            if (this.creating)
            {
                Gui.drawRect(this.creation.area.x - 2, 0, this.creation.area.x + 18, 20, 0xaa000000);
            }

            boolean running = this.runner.isRunning();

            if (running)
            {
                this.scrub.value = (int) this.runner.ticks;
                this.scrub.value = MathHelper.clamp_int(this.scrub.value, this.scrub.min, this.scrub.max);
                this.frame.setValue(this.scrub.value);
            }

            if (!running && this.playing)
            {
                this.updatePlauseButton();
                this.runner.attachOutside();
                this.scrub.setValueFromScrub(0);

                ClientProxy.EVENT_BUS.post(new CameraEditorEvent.Playback(this, false, this.scrub.value));
                this.playing = false;
            }

            /* Sync the player on current tick */
            if ((this.runner.outside.active || (this.syncing && this.haveScrubbed)) && !this.flight.enabled)
            {
                this.updatePlayerCurrently(0.0F);
            }

            this.drawIcons();
        }

        super.drawScreen(mouseX, mouseY, partialTicks);

        /* Display camera attributes on top of everything else */
        boolean running = this.runner.isRunning();

        /* Display flight speed */
        if (this.flight.enabled)
        {
            float flightSpeed = this.flight.speed / 1000F;
            String speedFormat = "%.0f";

            if (flightSpeed < 0.01F) speedFormat = "%.3f";
            else if (flightSpeed < 0.1F) speedFormat = "%.2f";
            else if (flightSpeed < 1F) speedFormat = "%.1f";

            String speed = String.format(this.stringSpeed + ": " + speedFormat, flightSpeed);
            int width = this.fontRendererObj.getStringWidth(speed);
            int x = panel.getX() + panel.getW() - 10 - width;
            int y = panel.getY() + panel.getH() - 5;

            Gui.drawRect(x - 2, y - 3, x + width + 2, y + 10, 0xbb000000);
            this.fontRendererObj.drawStringWithShadow(speed, x, y, 0xffffff);
        }

        /* Display position variables */
        if ((this.syncing || running) && this.displayPosition)
        {
            Position pos = running ? this.runner.getPosition() : this.position;
            Point point = pos.point;
            Angle angle = pos.angle;

            String[] labels = new String[] {this.stringX + ": " + point.x, this.stringY + ": " + point.y, this.stringZ + ": " + point.z, this.stringYaw + ": " + angle.yaw, this.stringPitch + ": " + angle.pitch, this.stringRoll + ": " + angle.roll, this.stringFov + ": " + angle.fov};
            int i = 6;

            for (String label : labels)
            {
                int width = this.fontRendererObj.getStringWidth(label);
                int y = panel.getY() + panel.getH() - 5 - 15 * i;
                int x = panel.getX();

                Gui.drawRect(x, y - 3, x + width + 4, y + 10, 0xbb000000);
                this.fontRendererObj.drawStringWithShadow(label, x + 2, y, 0xffffff);

                i--;
            }
        }
    }

    /**
     * Draw icons for indicating different active states (like syncing 
     * or flight mode) 
     */
    private void drawIcons()
    {
        if (!this.syncing && !this.flight.enabled)
        {
            return;
        }

        int x = this.width - 18;
        int y = 22;

        this.mc.renderEngine.bindTexture(EDITOR_TEXTURE);

        if (this.syncing || this.flight.enabled)
        {
            Gui.drawRect(this.width - (this.syncing ? 20 : 0) - (this.flight.enabled ? 20 : 0), y - 2, this.width, y + 18, 0x88000000);
        }

        GlStateManager.color(1, 1, 1, 1);

        if (this.syncing)
        {
            Gui.drawModalRectWithCustomSizedTexture(x, y, 64, 32, 16, 16, 256, 256);
            x -= 20;
        }

        if (this.flight.enabled)
        {
            int v = this.flight.vertical ? 64 : 48;

            Gui.drawModalRectWithCustomSizedTexture(x, y, 64, v, 16, 16, 256, 256);
        }
    }
}