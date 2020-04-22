package mchorse.aperture.client.gui;

import mchorse.aperture.Aperture;
import mchorse.aperture.ClientProxy;
import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.CameraRunner;
import mchorse.aperture.camera.data.Angle;
import mchorse.aperture.camera.data.Point;
import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.camera.fixtures.IdleFixture;
import mchorse.aperture.camera.fixtures.PathFixture;
import mchorse.aperture.client.gui.GuiPlaybackScrub.IScrubListener;
import mchorse.aperture.client.gui.config.GuiCameraConfig;
import mchorse.aperture.client.gui.config.GuiConfigCameraOptions;
import mchorse.aperture.client.gui.panels.GuiAbstractFixturePanel;
import mchorse.aperture.client.gui.panels.GuiPathFixturePanel;
import mchorse.aperture.events.CameraEditorEvent;
import mchorse.aperture.utils.APIcons;
import mchorse.mclib.client.MouseRenderer;
import mchorse.mclib.client.gui.framework.GuiBase;
import mchorse.mclib.client.gui.framework.elements.GuiDelegateElement;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiIconElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.utils.Icons;
import mchorse.mclib.client.gui.utils.resizers.IResizer;
import mchorse.mclib.utils.Direction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.GameType;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Camera editor GUI
 *
 * This GUI provides tools for managing camera profiles. 
 */
@SideOnly(Side.CLIENT)
public class GuiCameraEditor extends GuiBase implements IScrubListener
{
    /**
     * Registry of editing camera fixture panels. Per every fixture class type
     * there is supposed to be a class that is responsible for editing a
     * fixture.
     */
    public static final Map<Class<? extends AbstractFixture>, Class<? extends GuiAbstractFixturePanel<? extends AbstractFixture>>> PANELS = new HashMap<>();

    /* Strings */
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

    private float lastPartialTick = 0;
    private GameType lastGameMode = GameType.NOT_SET;
    public ResourceLocation overlayLocation;

    /**
     * This property saves state for the sync option, to allow more friendly
     */
    public boolean haveScrubbed;

    /**
     * Maximum scrub duration
     */
    public int maxScrub = 0;

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
     * Aspect ratio for black bars 
     */
    public float aspectRatio = 16F / 9F;

    /* GUI fields */

    /**
     * Play/pause button (very clever name, eh?)
     */
    public GuiIconElement plause;

    public GuiIconElement nextFrame;
    public GuiIconElement prevFrame;

    public GuiIconElement toPrevFixture;
    public GuiIconElement toNextFixture;

    public GuiIconElement moveForward;
    public GuiIconElement moveBackward;

    public GuiIconElement copyPosition;
    public GuiIconElement moveDuration;

    public GuiIconElement save;
    public GuiIconElement openConfig;
    public GuiIconElement openModifiers;
    public GuiIconElement openProfiles;

    public GuiIconElement add;
    public GuiIconElement dupe;
    public GuiIconElement replace;
    public GuiIconElement remove;

    public GuiIconElement cut;
    public GuiIconElement creation;

    /* Widgets */
    public GuiElement top;
    public GuiCameraConfig config;
    public GuiFixturesPopup popup;
    public GuiPlaybackScrub scrub;
    public GuiProfilesManager profiles;
    public GuiConfigCameraOptions cameraOptions;
    public GuiModifiersManager modifiers;
    public GuiDelegateElement<GuiAbstractFixturePanel> panel;
    public GuiElement hidden;

    /**
     * Initialize the camera editor with a camera profile.
     */
    public GuiCameraEditor(Minecraft mc, CameraRunner runner)
    {
        this.runner = runner;

        this.top = new GuiElement(mc)
        {
            @Override
            public boolean mouseClicked(GuiContext context)
            {
                if (super.mouseClicked(context))
                {
                    return true;
                }

                flight.mouseClicked(context);
                return false;
            }

            @Override
            public boolean mouseScrolled(GuiContext context)
            {
                if (super.mouseScrolled(context))
                {
                    return true;
                }

                flight.mouseScrolled(context);
                return false;
            }

            @Override
            public void mouseReleased(GuiContext context)
            {
                super.mouseReleased(context);
                flight.mouseReleased(context);
            }
        };
        this.top.flex().relative(this.viewport).wh(1F, 1F);
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
        this.toNextFixture = new GuiIconElement(mc, APIcons.FRAME_NEXT, (b) -> this.jumpToNextFixture());
        this.toNextFixture.tooltip(I18n.format("aperture.gui.tooltips.jump_next_fixture"));
        this.nextFrame = new GuiIconElement(mc, APIcons.FORWARD, (b) -> this.jumpToNextFrame());
        this.nextFrame.tooltip(I18n.format("aperture.gui.tooltips.jump_next_frame"));
        this.plause = new GuiIconElement(mc, APIcons.PLAY, (b) ->
        {
            this.setFlight(false);
            this.runner.toggle(this.profile, this.scrub.value);
            this.updatePlauseButton();

            this.playing = this.runner.isRunning();

            if (!this.playing)
            {
                this.runner.attachOutside();
                this.updatePlayerCurrently();
            }

            ClientProxy.EVENT_BUS.post(new CameraEditorEvent.Playback(this, this.playing, this.scrub.value));
        });
        this.plause.tooltip(I18n.format("aperture.gui.tooltips.plause"), Direction.BOTTOM);
        this.prevFrame = new GuiIconElement(mc, APIcons.BACKWARD, (b) -> this.jumpToPrevFrame());
        this.prevFrame.tooltip(I18n.format("aperture.gui.tooltips.jump_prev_frame"));
        this.toPrevFixture = new GuiIconElement(mc, APIcons.FRAME_PREV, (b) -> this.jumpToPrevFixture());
        this.toPrevFixture.tooltip(I18n.format("aperture.gui.tooltips.jump_prev_fixture"));

        this.openProfiles = new GuiIconElement(mc, Icons.MORE, (b) -> this.hidePopups(this.profiles));
        this.openProfiles.tooltip(I18n.format("aperture.gui.tooltips.profiles"));
        this.openConfig = new GuiIconElement(mc, Icons.GEAR, (b) -> this.hidePopups(this.config));
        this.openConfig.tooltip(I18n.format("aperture.gui.tooltips.config"));
        this.openModifiers = new GuiIconElement(mc, Icons.FILTER, (b) -> this.hidePopups(this.modifiers));
        this.openModifiers.tooltip(I18n.format("aperture.gui.tooltips.modifiers"));
        this.save = new GuiIconElement(mc, Icons.SAVED, (b) -> this.saveProfile());
        this.save.tooltip(I18n.format("aperture.gui.tooltips.save"));

        this.add = new GuiIconElement(mc, Icons.ADD, (b) -> this.hideReplacingPopups(this.popup, false));
        this.add.tooltip(I18n.format("aperture.gui.tooltips.add"));
        this.dupe = new GuiIconElement(mc, Icons.DUPE, (b) -> this.dupeFixture());
        this.dupe.tooltip(I18n.format("aperture.gui.tooltips.dupe"));
        this.replace = new GuiIconElement(mc, Icons.REFRESH, (b) -> this.hideReplacingPopups(this.popup, true));
        this.replace.tooltip(I18n.format("aperture.gui.tooltips.replace"));
        this.remove = new GuiIconElement(mc, Icons.REMOVE, (b) -> this.removeFixture());
        this.remove.tooltip(I18n.format("aperture.gui.tooltips.remove"));

        this.creation = new GuiIconElement(mc, APIcons.INTERACTIVE, (b) -> this.toggleCreation());
        this.creation.tooltip(I18n.format("aperture.gui.tooltips.creation"));
        this.cut = new GuiIconElement(mc, Icons.CUT, (b) -> this.cutFixture());
        this.cut.tooltip(I18n.format("aperture.gui.tooltips.cut"));
        this.moveForward = new GuiIconElement(mc, APIcons.MOVE_FORWARD, (b) -> this.moveTo(1));
        this.moveForward.tooltip(I18n.format("aperture.gui.tooltips.move_up"));
        this.moveDuration = new GuiIconElement(mc, APIcons.SHIFT, (b) -> this.shiftDurationToCursor());
        this.moveDuration.tooltip(I18n.format("aperture.gui.tooltips.move_duration"));
        this.copyPosition = new GuiIconElement(mc, APIcons.POSITION, (b) -> this.editFixture());
        this.copyPosition.tooltip(I18n.format("aperture.gui.tooltips.copy_position"));
        this.moveBackward = new GuiIconElement(mc, APIcons.MOVE_BACK, (b) -> this.moveTo(-1));
        this.moveBackward.tooltip(I18n.format("aperture.gui.tooltips.move_down"));

        /* Button placement */
        this.toNextFixture.flex().relative(this.viewport).set(0, 2, 16, 16).x(0.5F, 32);
        this.nextFrame.flex().relative(this.toNextFixture.resizer()).set(-20, 0, 16, 16);
        this.plause.flex().relative(this.nextFrame.resizer()).set(-20, 0, 16, 16);
        this.prevFrame.flex().relative(this.plause.resizer()).set(-20, 0, 16, 16);
        this.toPrevFixture.flex().relative(this.prevFrame.resizer()).set(-20, 0, 16, 16);

        this.openProfiles.flex().relative(this.viewport).set(0, 2, 16, 16).x(1, -18);
        this.openConfig.flex().relative(this.openProfiles.resizer()).set(-20, 0, 16, 16);
        this.openModifiers.flex().relative(this.openConfig.resizer()).set(-20, 0, 16, 16);
        this.save.flex().relative(this.openModifiers.resizer()).set(-20, 0, 16, 16);

        this.add.flex().relative(this.save.resizer()).set(-100, 0, 16, 16);
        this.dupe.flex().relative(this.add.resizer()).set(20, 0, 16, 16);
        this.replace.flex().relative(this.dupe.resizer()).set(20, 0, 16, 16);
        this.remove.flex().relative(this.replace.resizer()).set(20, 0, 16, 16);

        this.cut.flex().relative(this.viewport).set(82, 2, 16, 16);
        this.creation.flex().relative(this.cut.resizer()).set(20, 0, 16, 16);
        this.moveForward.flex().relative(this.cut.resizer()).set(-20, 0, 16, 16);
        this.moveDuration.flex().relative(this.moveForward.resizer()).set(-20, 0, 16, 16);
        this.copyPosition.flex().relative(this.moveDuration.resizer()).set(-20, 0, 16, 16);
        this.moveBackward.flex().relative(this.copyPosition.resizer()).set(-20, 0, 16, 16);

        /* Setup areas of widgets */
        this.scrub.flex().relative(this.viewport).set(10, 0, 0, 20).y(1, -20).w(1, -20);

        this.panel.flex().relative(this.viewport).set(10, 40, 0, 0).w(1, -20).h(1, -70);
        this.popup.flex().relative(this.add.resizer()).set(-44, 18, 62, 122);
        this.config.flex().relative(this.panel.resizer()).set(0, -20, 160, 0).x(1, -180 + 10).h(1, 20);
        this.profiles.flex().relative(this.panel.resizer()).set(0, -20, 160, 0).x(1, -160 + 10).h(1, 20);
        this.modifiers.flex().relative(this.panel.resizer()).set(0, -20, 220, 0).x(1, -260 + 10).h(1, 20);

        /* Adding everything */
        this.hidden = new GuiElement(mc);
        this.hidden.add(this.toNextFixture, this.nextFrame, this.plause, this.prevFrame, this.toPrevFixture);
        this.hidden.add(this.cut, this.creation, this.moveForward, this.moveDuration, this.copyPosition, this.moveBackward);
        this.hidden.add(this.add, this.dupe, this.replace, this.remove, this.save, this.openConfig, this.openModifiers);
        this.hidden.add(this.scrub, this.panel, this.popup, this.config, this.modifiers);

        this.cameraProfileWasChanged(this.profile);
        this.updatePlauseButton();
        this.updateValues();

        this.hidePopups(this.profiles);
        this.top.add(this.hidden, this.openProfiles, this.profiles, this.cameraOptions.overlayPicker);
        this.root.add(this.top);

        /* Let other classes have fun with camera editor's position and such */
        ClientProxy.EVENT_BUS.post(new CameraEditorEvent.Init(this));

        /* Register keybinds */
        String fixture = "Fixtures";
        String modes = "Modes";
        String editor = "Camera editor";

        this.root.keys().register("Toggle camera editor's visibility", Keyboard.KEY_F1, () -> this.top.toggleVisible()).category(editor);
        this.root.keys().register("Toggle flight mode", Keyboard.KEY_F, () -> this.cameraOptions.flight.clickItself(this.context)).category(modes);
        this.root.keys().register("Toggle vertical flight mode", Keyboard.KEY_V, () -> this.flight.vertical = !this.flight.vertical).category(modes);

        Supplier<Boolean> active = () -> !(this.flight.enabled || this.profile == null);

        this.root.keys().register("Save camera profile", Keyboard.KEY_S, () -> this.save.clickItself(this.context)).held(Keyboard.KEY_LCONTROL).active(active).category(editor);
        this.root.keys().register("Toggle sync mode", Keyboard.KEY_S, () -> this.cameraOptions.sync.clickItself(this.context)).active(active).category(modes);
        this.root.keys().register("Toggle outside mode", Keyboard.KEY_O, () -> this.cameraOptions.outside.clickItself(this.context)).active(active).category(modes);
        this.root.keys().register("Play/pause", Keyboard.KEY_SPACE, () -> this.plause.clickItself(this.context)).active(active).category(editor);
        this.root.keys().register("Deselect current fixture", Keyboard.KEY_D, () -> this.pickCameraFixture(null, 0)).active(active).category(fixture);
        this.root.keys().register("Shift duration to cursor", Keyboard.KEY_M, this::shiftDurationToCursor).active(active).category(fixture);
        this.root.keys().register("Jump to next fixture", Keyboard.KEY_RIGHT, this::jumpToNextFixture).held(Keyboard.KEY_LSHIFT).active(active).category(editor);
        this.root.keys().register("Jump to previous fixture", Keyboard.KEY_LEFT, this::jumpToPrevFixture).held(Keyboard.KEY_LSHIFT).active(active).category(editor);
        this.root.keys().register("Jump to next frame", Keyboard.KEY_RIGHT, this::jumpToNextFrame).active(active).category(editor);
        this.root.keys().register("Jump to previous frame", Keyboard.KEY_LEFT, this::jumpToPrevFrame).active(active).category(editor);
        this.root.keys().register("Apply current camera position", Keyboard.KEY_B, () ->
        {
            if (this.panel.delegate != null)
            {
                this.editFixture();
            }
        }).active(active).category(fixture);

        this.root.keys().register("Toggle modifiers popup", Keyboard.KEY_N, () -> this.openModifiers.clickItself(this.context)).active(active).category(fixture);
        this.root.keys().register("Toggle fixture looping", Keyboard.KEY_R, () -> this.cameraOptions.loop.clickItself(this.context)).active(active).category(fixture);
        this.root.keys().register("Cut fixture", Keyboard.KEY_C, () -> this.cut.clickItself(this.context)).active(active).category(fixture);
        this.root.keys().register("Toggle interactive mode", Keyboard.KEY_I, () -> this.creation.clickItself(this.context)).active(active).category(fixture);
    }

    public boolean isSyncing()
    {
        return Aperture.editorSync.get();
    }

    /**
     * Teleport player and setup position, motion and angle based on the value
     * was scrubbed from playback scrubber.
     */
    @Override
    public void scrubbed(GuiPlaybackScrub scrub, int value, boolean fromScrub)
    {
        if (this.runner.isRunning())
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

                if (this.isSyncing())
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
            AbstractFixture fixture = this.profile.get(index).copy();

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
            this.save.both(profile != null && profile.dirty ? Icons.SAVE : Icons.SAVED);
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
        ClientProxy.control.cache();
        this.lastGameMode = ClientProxy.runner.getGameMode(player);
        this.setAspectRatio(Aperture.editorLetterboxAspect.get());

        if (Aperture.spectator.get() && !Aperture.outside.get())
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
        return this.runner.outside.active ? this.runner.outside.camera : this.mc.player;
    }

    public void updateOverlay()
    {
        this.overlayLocation = Aperture.editorOverlayRL.get();
    }

    public void updatePlayerCurrently()
    {
        this.updatePlayerCurrently(this.lastPartialTick);
    }

    /**
     * Update player to current value in the scrub
     */
    public void updatePlayerCurrently(float partialTicks)
    {
        if ((this.isSyncing() || this.runner.outside.active) && !this.runner.isRunning())
        {
            this.updatePlayer(this.scrub.value, partialTicks);
        }
    }

    /**
     * Update player
     */
    public void updatePlayer(long tick, float ticks)
    {
        if (this.profile == null)
        {
            return;
        }

        long duration = this.profile.getDuration();

        tick = tick < 0 ? 0 : tick;
        tick = tick > duration ? duration : tick;

        EntityPlayer player = Minecraft.getMinecraft().player;

        this.position.set(player);
        this.profile.applyProfile(tick, ticks, this.lastPartialTick, this.position);

        this.position.apply(this.getCamera());
        ClientProxy.control.setRollAndFOV(this.position.angle.roll, this.position.angle.fov);
    }

    /**
     * This method should be invoked when values in the panel were modified
     */
    public void updateValues()
    {
        if (this.profile != null)
        {
            this.scrub.max = Math.max((int) this.profile.getDuration(), this.maxScrub);
            this.scrub.setValue(this.scrub.value);
        }
        else
        {
            this.scrub.max = this.maxScrub;
            this.scrub.setValue(0);
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

        if (this.panel.delegate != null)
        {
            this.panel.delegate.profileWasUpdated();
        }
    }

    /**
     * Saves camera profile
     */
    public void saveProfile()
    {
        if (this.profile != null)
        {
            this.profile.save();
            this.profiles.init();
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

        this.popup.flex().relative(replacing ? this.replace.resizer() : this.add.resizer());
        this.popup.resize();

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
        this.plause.both(this.runner.isRunning() ? APIcons.PAUSE : APIcons.PLAY);
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
    protected void closeScreen()
    {
        Minecraft.getMinecraft().gameSettings.hideGUI = false;
        ClientProxy.control.restore();
        GuiIngameForge.renderHotbar = true;
        GuiIngameForge.renderCrosshairs = true;

        if (!this.runner.isRunning())
        {
            this.runner.detachOutside();
        }

        if (this.lastGameMode != GameType.NOT_SET)
        {
            this.mc.player.sendChatMessage("/gamemode " + this.lastGameMode.getID());
        }

        super.closeScreen();
    }

    /**
     * Draw everything on the screen
     */
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        /* What the fuck, why is partial tick is always 0.32 on 1.12.2? */
        partialTicks = Minecraft.getMinecraft().getRenderPartialTicks();

        boolean isRunning = this.runner.isRunning();

        if (isRunning)
        {
            this.lastPartialTick = partialTicks;
        }

        if (Aperture.editorLoop.get() && isRunning && this.panel.delegate != null)
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
            this.flight.animate(this.context, this.position);
            this.position.apply(this.getCamera());
            ClientProxy.control.roll = this.position.angle.roll;
            this.mc.gameSettings.fovSetting = this.position.angle.fov;

            if (this.isSyncing() && this.haveScrubbed)
            {
                this.editFixture();
            }
        }

        if (this.profile != null)
        {
            this.drawOverlays();
        }

        if (!this.top.isVisible())
        {
            MouseRenderer.disable();

            /* Little tip for the users who don't know what they did */
            if (!isRunning && Aperture.editorF1Tooltip.get())
            {
                this.fontRenderer.drawStringWithShadow(I18n.format("aperture.gui.editor.f1"), 5, this.height - 12, 0xffffff);
            }

            super.drawScreen(mouseX, mouseY, partialTicks);

            return;
        }

        this.drawGradientRect(0, 0, width, 20, 0x66000000, 0);
        this.drawIcons();

        if (this.profiles.isVisible())
        {
            Gui.drawRect(width - 20, 0, width, 20, 0xaa000000);
        }

        IResizer panel = this.panel.resizer();

        if (this.profile != null)
        {
            /* Draw backgrounds for popups */
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

            if (this.creating)
            {
                Gui.drawRect(this.creation.area.x - 2, 0, this.creation.area.x + 18, 20, 0xaa000000);
            }

            /* Update playback scrub */
            boolean running = this.runner.isRunning();

            if (running)
            {
                this.scrub.setValue((int) this.runner.ticks);
            }

            /* Rewind playback back to 0 */
            if (!running && this.playing)
            {
                this.updatePlauseButton();
                this.runner.attachOutside();
                this.scrub.setValueFromScrub(0);

                ClientProxy.EVENT_BUS.post(new CameraEditorEvent.Rewind(this, this.scrub.value));
                this.playing = false;
            }

            /* Sync the player on current tick */
            if ((this.runner.outside.active || (this.isSyncing() && this.haveScrubbed)) && !this.flight.enabled)
            {
                this.updatePlayerCurrently(partialTicks);
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks);

        /* Display flight speed */
        if (this.flight.enabled)
        {
            this.flight.drawSpeed(this.fontRenderer, panel.getX() + panel.getW() - 10, panel.getY() + panel.getH() - 5);
        }

        /* Display position variables */
        if ((this.isSyncing() || this.runner.isRunning()) && Aperture.editorDisplayPosition.get())
        {
            this.drawPosition(panel);
        }
    }

    /**
     * Draw icons for indicating different active states (like syncing 
     * or flight mode) 
     */
    private void drawIcons()
    {
        if (!this.isSyncing() && !this.flight.enabled)
        {
            return;
        }

        int x = this.width - 18;
        int y = 22;

        Gui.drawRect(this.width - (this.isSyncing() ? 20 : 0) - (this.flight.enabled ? 20 : 0), y - 2, this.width, y + 18, 0x88000000);
        GlStateManager.color(1, 1, 1, 1);

        if (this.isSyncing())
        {
            Icons.DOWNLOAD.render(x, y);
            x -= 20;
        }

        if (this.flight.enabled)
        {
            (this.flight.vertical ? APIcons.HELICOPTER : APIcons.PLANE).render(x, y);
        }
    }

    /**
     * Draw information about current camera's position
     */
    private void drawPosition(IResizer panel)
    {
        Position pos = this.runner.isRunning() ? this.runner.getPosition() : this.position;
        Point point = pos.point;
        Angle angle = pos.angle;

        String[] labels = new String[] {this.stringX + ": " + point.x, this.stringY + ": " + point.y, this.stringZ + ": " + point.z, this.stringYaw + ": " + angle.yaw, this.stringPitch + ": " + angle.pitch, this.stringRoll + ": " + angle.roll, this.stringFov + ": " + angle.fov};
        int i = 6;

        for (String label : labels)
        {
            int width = this.fontRenderer.getStringWidth(label);
            int y = panel.getY() + panel.getH() - 5 - 15 * i;
            int x = panel.getX();

            Gui.drawRect(x, y - 3, x + width + 4, y + 10, 0xbb000000);
            this.fontRenderer.drawStringWithShadow(label, x + 2, y, 0xffffff);

            i--;
        }
    }

    /**
     * Draw different camera type overlays (custom texture overlay, letterbox, rule of thirds and crosshair)
     */
    private void drawOverlays()
    {
        if (Aperture.editorOverlay.get() && this.overlayLocation != null)
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

        if (Aperture.editorLetterbox.get() && this.aspectRatio > 0)
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

        if (!this.top.isVisible())
        {
            return;
        }

        if (Aperture.editorRuleOfThirds.get())
        {
            int color = 0xcccc0000;

            Gui.drawRect(rx + rw / 3 - 1, ry, rx + rw / 3, ry + rh, color);
            Gui.drawRect(rx + rw - rw / 3, ry, rx + rw - rw / 3 + 1, ry + rh, color);

            Gui.drawRect(rx, ry + rh / 3 - 1, rx + rw, ry + rh / 3, color);
            Gui.drawRect(rx, ry + rh - rh / 3, rx + rw, ry + rh - rh / 3 + 1, color);
        }

        if (Aperture.editorCrosshair.get())
        {
            GlStateManager.enableBlend();
            GlStateManager.enableAlpha();
            this.mc.renderEngine.bindTexture(Gui.ICONS);
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.color(1, 1, 1, 1);
            this.drawTexturedModalRect(this.viewport.mx() - 7, this.viewport.my() - 7, 0, 0, 16, 16);
        }
    }
}