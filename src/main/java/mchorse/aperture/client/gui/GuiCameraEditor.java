package mchorse.aperture.client.gui;

import mchorse.aperture.Aperture;
import mchorse.aperture.ClientProxy;
import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.CameraRunner;
import mchorse.aperture.camera.CameraUtils;
import mchorse.aperture.camera.FixtureRegistry;
import mchorse.aperture.camera.data.Angle;
import mchorse.aperture.camera.data.Point;
import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.camera.fixtures.IdleFixture;
import mchorse.aperture.camera.fixtures.PathFixture;
import mchorse.aperture.client.KeyboardHandler;
import mchorse.aperture.client.gui.config.GuiCameraConfig;
import mchorse.aperture.client.gui.config.GuiConfigCameraOptions;
import mchorse.aperture.client.gui.panels.GuiAbstractFixturePanel;
import mchorse.aperture.client.gui.panels.GuiPathFixturePanel;
import mchorse.aperture.client.gui.utils.undo.CameraProfileUndo;
import mchorse.aperture.client.gui.utils.undo.FixtureAddRemoveUndo;
import mchorse.aperture.client.gui.utils.undo.FixtureValueChangeUndo;
import mchorse.aperture.events.CameraEditorEvent;
import mchorse.aperture.utils.APIcons;
import mchorse.mclib.client.InputRenderer;
import mchorse.mclib.client.gui.framework.GuiBase;
import mchorse.mclib.client.gui.framework.elements.GuiDelegateElement;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiIconElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.utils.GuiUtils;
import mchorse.mclib.client.gui.utils.Icons;
import mchorse.mclib.client.gui.utils.keys.IKey;
import mchorse.mclib.client.gui.utils.resizers.IResizer;
import mchorse.mclib.utils.ColorUtils;
import mchorse.mclib.utils.Direction;
import mchorse.mclib.utils.undo.CompoundUndo;
import mchorse.mclib.utils.undo.IUndo;
import mchorse.mclib.utils.undo.UndoManager;
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
public class GuiCameraEditor extends GuiBase
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
    private long lastSave = 0;
    public ResourceLocation overlayLocation;

    /**
     * This property saves state for the sync option, to allow more friendly
     */
    public boolean haveScrubbed;

    /**
     * Maximum timeline duration
     */
    public int maxScrub = 0;

    /**
     * Flight mode 
     */
    public Flight flight;

    /**
     * Position 
     */
    public Position position = new Position(0, 0, 0, 0, 0);

    /**
     * Last position
     */
    public Position lastPosition = new Position(0, 0, 0, 0, 0);

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

    public GuiElement leftBar;
    public GuiElement rightBar;
    public GuiElement middleBar;
    public GuiElement creationBar;

    /**
     * Play/pause button (very clever name, eh?)
     */
    public GuiIconElement plause;
    public GuiIconElement nextFrame;
    public GuiIconElement prevFrame;
    public GuiIconElement prevFixture;
    public GuiIconElement nextFixture;

    public GuiIconElement moveForward;
    public GuiIconElement moveBackward;
    public GuiIconElement copyPosition;
    public GuiIconElement moveDuration;
    public GuiIconElement cut;
    public GuiIconElement creation;

    public GuiIconElement save;
    public GuiIconElement openMinema;
    public GuiIconElement openModifiers;
    public GuiIconElement openConfig;
    public GuiIconElement openProfiles;

    public GuiIconElement add;
    public GuiIconElement dupe;
    public GuiIconElement replace;
    public GuiIconElement remove;

    /* Widgets */
    public GuiElement top;
    public GuiCameraConfig config;
    public GuiFixtures fixtures;
    public GuiPlaybackScrub timeline;
    public GuiProfilesManager profiles;
    public GuiConfigCameraOptions cameraOptions;
    public GuiModifiersManager modifiers;
    public GuiMinemaPanel minema;
    public GuiDelegateElement<GuiAbstractFixturePanel> panel;

    /**
     * Initialize the camera editor with a camera profile.
     */
    public GuiCameraEditor(Minecraft mc, CameraRunner runner)
    {
        this.runner = runner;
        this.flight = new Flight(this);

        this.top = new GuiElement(mc)
        {
            @Override
            public void draw(GuiContext context)
            {
                super.draw(context);

                /* Display flight speed */
                GuiCameraEditor editor = GuiCameraEditor.this;
                IResizer panel = editor.panel.resizer();

                if (editor.flight.isFlightEnabled())
                {
                    editor.flight.drawSpeed(this.font, panel.getX() + panel.getW() - 10, panel.getY() + panel.getH() - 5);
                }

                /* Display position variables */
                if ((editor.isSyncing() || editor.runner.isRunning()) && Aperture.editorDisplayPosition.get())
                {
                    editor.drawPosition(panel);
                }
            }
        };
        this.top.flex().relative(this.viewport).wh(1F, 1F);
        this.panel = new GuiDelegateElement<GuiAbstractFixturePanel>(mc, null);
        this.timeline = new GuiPlaybackScrub(mc, this, null);
        this.fixtures = new GuiFixtures(mc, (fixture) ->
        {
            fixture.fromPlayer(this.getCamera());
            this.createFixture(fixture);
            this.fixtures.setVisible(false);
        });

        this.profiles = new GuiProfilesManager(mc, this);
        this.cameraOptions = new GuiConfigCameraOptions(mc, this);
        this.modifiers = new GuiModifiersManager(mc, this);
        this.config = new GuiCameraConfig(mc, this);
        this.minema = new GuiMinemaPanel(mc, this);

        /* Setup elements */
        this.nextFixture = new GuiIconElement(mc, APIcons.FRAME_NEXT, (b) -> this.jumpToNextFixture());
        this.nextFixture.tooltip(IKey.lang("aperture.gui.tooltips.jump_next_fixture"));
        this.nextFrame = new GuiIconElement(mc, APIcons.FORWARD, (b) -> this.jumpToNextFrame());
        this.nextFrame.tooltip(IKey.lang("aperture.gui.tooltips.jump_next_frame"));
        this.plause = new GuiIconElement(mc, Icons.PLAY, (b) -> this.togglePlayback());
        this.plause.tooltip(IKey.lang("aperture.gui.tooltips.plause"), Direction.BOTTOM);
        this.prevFrame = new GuiIconElement(mc, APIcons.BACKWARD, (b) -> this.jumpToPrevFrame());
        this.prevFrame.tooltip(IKey.lang("aperture.gui.tooltips.jump_prev_frame"));
        this.prevFixture = new GuiIconElement(mc, APIcons.FRAME_PREV, (b) -> this.jumpToPrevFixture());
        this.prevFixture.tooltip(IKey.lang("aperture.gui.tooltips.jump_prev_fixture"));

        this.save = new GuiIconElement(mc, Icons.SAVED, (b) -> this.saveProfile());
        this.save.tooltip(IKey.lang("aperture.gui.tooltips.save"));
        this.openMinema = new GuiIconElement(mc, APIcons.MINEMA, (b) -> this.hidePopups(this.minema));
        this.openMinema.tooltip(IKey.lang("aperture.gui.tooltips.minema"));
        this.openModifiers = new GuiIconElement(mc, Icons.FILTER, (b) -> this.hidePopups(this.modifiers));
        this.openModifiers.tooltip(IKey.lang("aperture.gui.tooltips.modifiers"));
        this.openConfig = new GuiIconElement(mc, Icons.GEAR, (b) -> this.hidePopups(this.config));
        this.openConfig.tooltip(IKey.lang("aperture.gui.tooltips.config"));
        this.openProfiles = new GuiIconElement(mc, Icons.MORE, (b) -> this.hidePopups(this.profiles));
        this.openProfiles.tooltip(IKey.lang("aperture.gui.tooltips.profiles"));

        this.add = new GuiIconElement(mc, Icons.ADD, (b) -> this.hideReplacingPopups(this.fixtures, false));
        this.add.tooltip(IKey.lang("aperture.gui.tooltips.add"));
        this.dupe = new GuiIconElement(mc, Icons.DUPE, (b) -> this.dupeFixture());
        this.dupe.tooltip(IKey.lang("aperture.gui.tooltips.dupe"));
        this.replace = new GuiIconElement(mc, Icons.REFRESH, (b) -> this.hideReplacingPopups(this.fixtures, true));
        this.replace.tooltip(IKey.lang("aperture.gui.tooltips.replace"));
        this.remove = new GuiIconElement(mc, Icons.REMOVE, (b) -> this.removeFixture());
        this.remove.tooltip(IKey.lang("aperture.gui.tooltips.remove"));

        this.creation = new GuiIconElement(mc, APIcons.INTERACTIVE, (b) -> this.toggleCreation());
        this.creation.tooltip(IKey.lang("aperture.gui.tooltips.creation"));
        this.cut = new GuiIconElement(mc, Icons.CUT, (b) -> this.cutFixture());
        this.cut.tooltip(IKey.lang("aperture.gui.tooltips.cut"));
        this.moveForward = new GuiIconElement(mc, Icons.SHIFT_FORWARD, (b) -> this.moveTo(1));
        this.moveForward.tooltip(IKey.lang("aperture.gui.tooltips.move_up"));
        this.moveDuration = new GuiIconElement(mc, Icons.SHIFT_TO, (b) -> this.shiftDurationToCursor());
        this.moveDuration.tooltip(IKey.lang("aperture.gui.tooltips.move_duration"));
        this.copyPosition = new GuiIconElement(mc, Icons.MOVE_TO, (b) -> this.editFixture(false));
        this.copyPosition.tooltip(IKey.lang("aperture.gui.tooltips.copy_position"));
        this.moveBackward = new GuiIconElement(mc, Icons.SHIFT_BACKWARD, (b) -> this.moveTo(-1));
        this.moveBackward.tooltip(IKey.lang("aperture.gui.tooltips.move_down"));

        /* Button placement */
        this.leftBar = new GuiElement(mc);
        this.rightBar = new GuiElement(mc);
        this.middleBar = new GuiElement(mc);
        this.creationBar = new GuiElement(mc);

        this.leftBar.flex().relative(this.top.flex()).row(0).resize().width(20).height(20);
        this.rightBar.flex().relative(this.top.flex()).x(1F).anchorX(1F).row(0).resize().width(20).height(20);
        this.middleBar.flex().relative(this.top.flex()).x(0.5F).anchorX(0.5F).row(0).resize().width(20).height(20);
        this.creationBar.flex().row(0).resize().width(20).height(20);

        this.leftBar.add(this.moveBackward, this.moveForward, this.copyPosition, this.moveDuration, this.cut, this.creation);
        this.middleBar.add(this.prevFixture, this.prevFrame, this.plause, this.nextFrame, this.nextFixture);
        this.rightBar.add(this.save, this.openMinema, this.openModifiers, this.openConfig, this.openProfiles);
        this.creationBar.add(this.add, this.dupe, this.replace, this.remove);

        /* Setup areas of widgets */
        this.timeline.flex().relative(this.top).set(10, 0, 0, 20).y(1, -20).w(1, -20);
        this.panel.flex().relative(this.top).set(0, 20, 0, 0).w(1F).hTo(this.timeline.area);
        this.config.flex().relative(this.openConfig).xy(1F, 1F).anchorX(1F).w(200).hTo(this.panel.flex(), 1F);
        this.profiles.flex().relative(this.openProfiles).xy(1F, 1F).anchorX(1F).w(190).hTo(this.panel.flex(), 1F);
        this.modifiers.flex().relative(this.openModifiers).xy(1F, 1F).anchorX(1F).w(210).hTo(this.panel.flex(), 1F);
        this.minema.flex().relative(this.openMinema).xy(1F, 1F).anchorX(1F).w(200);

        /* Adding everything */
        this.hidePopups(this.profiles);
        this.top.add(this.leftBar, this.middleBar, this.rightBar, this.creationBar);
        this.top.add(this.timeline, this.panel, this.fixtures, this.profiles, this.config, this.modifiers, this.minema);
        this.root.add(this.flight, this.top);

        /* Let other classes have fun with camera editor's position and such */
        ClientProxy.EVENT_BUS.post(new CameraEditorEvent.Init(this));

        /* Register keybinds */
        IKey fixture = IKey.lang("aperture.gui.editor.keys.fixture.title");
        IKey modes = IKey.lang("aperture.gui.editor.keys.modes.title");
        IKey editor = IKey.lang("aperture.gui.editor.keys.editor.title");
        IKey looping = IKey.lang("aperture.gui.editor.keys.looping.title");
        Supplier<Boolean> active = this::isFlightDisabled;

        this.root.keys().register(IKey.lang("aperture.gui.editor.keys.editor.toggle"), Keyboard.KEY_F1, () -> this.top.toggleVisible()).category(editor);
        this.root.keys().register(IKey.lang("aperture.gui.editor.keys.editor.modifiers"), Keyboard.KEY_N, () -> this.openModifiers.clickItself(this.context)).active(active).category(editor);
        this.root.keys().register(IKey.lang("aperture.gui.editor.keys.editor.save"), Keyboard.KEY_S, () -> this.save.clickItself(this.context)).held(Keyboard.KEY_LCONTROL).active(active).category(editor);
        this.root.keys().register(IKey.lang("aperture.gui.editor.keys.editor.plause"), Keyboard.KEY_SPACE, () -> this.plause.clickItself(this.context)).active(active).category(editor);
        this.root.keys().register(IKey.lang("aperture.gui.editor.keys.editor.next_fixture"), Keyboard.KEY_RIGHT, this::jumpToNextFixture).held(Keyboard.KEY_LSHIFT).active(active).category(editor);
        this.root.keys().register(IKey.lang("aperture.gui.editor.keys.editor.prev_fixture"), Keyboard.KEY_LEFT, this::jumpToPrevFixture).held(Keyboard.KEY_LSHIFT).active(active).category(editor);
        this.root.keys().register(IKey.lang("aperture.gui.editor.keys.editor.next"), Keyboard.KEY_RIGHT, this::jumpToNextFrame).active(active).category(editor);
        this.root.keys().register(IKey.lang("aperture.gui.editor.keys.editor.prev"), Keyboard.KEY_LEFT, this::jumpToPrevFrame).active(active).category(editor);
        this.root.keys().register(IKey.lang("aperture.gui.editor.keys.editor.undo"), Keyboard.KEY_Z, this::undo).held(Keyboard.KEY_LCONTROL).category(editor);
        this.root.keys().register(IKey.lang("aperture.gui.editor.keys.editor.redo"), Keyboard.KEY_Y, this::redo).held(Keyboard.KEY_LCONTROL).category(editor);

        this.root.keys().register(IKey.lang("aperture.gui.editor.keys.fixture.deselect"), Keyboard.KEY_D, () -> this.pickCameraFixture(null, 0)).active(active).category(fixture);
        this.root.keys().register(IKey.lang("aperture.gui.editor.keys.fixture.shift"), Keyboard.KEY_M, this::shiftDurationToCursor).active(active).category(fixture);
        this.root.keys().register(IKey.lang("aperture.gui.editor.keys.fixture.copy"), Keyboard.KEY_B, () -> this.editFixture(false)).active(active).category(fixture);
        this.root.keys().register(IKey.lang("aperture.gui.editor.keys.fixture.cut"), Keyboard.KEY_C, () -> this.cut.clickItself(this.context)).held(Keyboard.KEY_LMENU).active(active).category(fixture);

        for (byte i = 0; i < FixtureRegistry.getNextId(); i ++)
        {
            FixtureRegistry.FixtureInfo info = FixtureRegistry.getInfo(i);
            IKey label = IKey.format("aperture.gui.editor.keys.fixture.add", IKey.lang(info.title));
            byte type = i;

            this.root.keys()
                .register(label, Keyboard.KEY_1 + i, () -> this.fixtures.createFixture(type))
                .held(Keyboard.KEY_LCONTROL).active(active).category(fixture);
        }

        this.root.keys().register(IKey.lang("aperture.gui.editor.keys.modes.flight"), Keyboard.KEY_F, () -> this.cameraOptions.flight.clickItself(this.context)).category(modes);
        this.root.keys().register(IKey.lang("aperture.gui.editor.keys.modes.vertical"), Keyboard.KEY_V, () -> this.flight.toggleMovementType()).active(() -> this.flight.isFlightEnabled()).category(modes);
        this.root.keys().register(IKey.lang("aperture.gui.editor.keys.modes.sync"), Keyboard.KEY_S, () -> this.cameraOptions.sync.clickItself(this.context)).active(active).category(modes);
        this.root.keys().register(IKey.lang("aperture.gui.editor.keys.modes.ouside"), Keyboard.KEY_O, () -> this.cameraOptions.outside.clickItself(this.context)).active(active).category(modes);
        this.root.keys().register(IKey.lang("aperture.gui.editor.keys.modes.interactive"), Keyboard.KEY_I, () -> this.creation.clickItself(this.context)).active(active).category(modes);

        this.root.keys().register(IKey.lang("aperture.gui.editor.keys.modes.looping"), Keyboard.KEY_L, () -> this.cameraOptions.loop.clickItself(this.context)).active(active).category(looping);
        this.root.keys().register(IKey.lang("aperture.gui.editor.keys.looping.set_min"), Keyboard.KEY_LBRACKET, () -> this.timeline.setLoopMin(this.timeline.value)).active(active).category(looping);
        this.root.keys().register(IKey.lang("aperture.gui.editor.keys.looping.set_max"), Keyboard.KEY_RBRACKET, () -> this.timeline.setLoopMax(this.timeline.value)).active(active).category(looping);
    }

    public void postUndo(IUndo<CameraProfile> undo)
    {
        this.postUndo(undo, true, false);
    }

    public void postUndo(IUndo<CameraProfile> undo, boolean apply)
    {
        this.postUndo(undo, apply, false);
    }

    public void postUndoCallback(IUndo<CameraProfile> undo)
    {
        this.postUndo(undo, true, true);
    }

    public void postUndo(IUndo<CameraProfile> undo, boolean apply, boolean callback)
    {
        if (undo == null)
        {
            throw new RuntimeException("Given undo is null!");
        }

        CameraProfile profile = this.getProfile();
        UndoManager<CameraProfile> undoManager = profile.undoManager;

        undoManager.setCallback(callback ? this::handleUndos : null);

        if (apply)
        {
            undoManager.pushApplyUndo(undo, profile);
        }
        else
        {
            undoManager.pushUndo(undo);
        }

        undoManager.setCallback(this::handleUndos);

        this.updateProfile();
    }

    private void handleUndos(IUndo<CameraProfile> undo, boolean redo)
    {
        int cursor = -1;
        double min = -1;
        double max = -1;
        int index = -1;
        String name = "";

        if (undo instanceof FixtureValueChangeUndo)
        {
            FixtureValueChangeUndo value = (FixtureValueChangeUndo) undo;

            index = value.getIndex();
            name = value.getName();
        }
        else if (undo instanceof CompoundUndo && ((CompoundUndo) undo).has(FixtureValueChangeUndo.class))
        {
            FixtureValueChangeUndo value = (FixtureValueChangeUndo) ((CompoundUndo) undo).getFirst(FixtureValueChangeUndo.class);

            index = value.getIndex();
            name = value.getName();
        }

        if (index >= 0)
        {
            this.pickCameraFixture(this.getProfile().get(index), -1);
            this.panel.delegate.handleUndo(undo, redo);
            this.modifiers.handleUndo(undo, redo);

            if (name.endsWith("duration"))
            {
                this.updateValues();
            }
        }

        if (name.contains("modifiers"))
        {
            this.hidePopups(this.modifiers, true);
        }
        else if (name.startsWith("curves"))
        {
            this.hidePopups(this.profiles, true);

            if (!this.profiles.curves.isVisible())
            {
                this.profiles.toggleKeyframes();
            }
        }

        /* Handle adding/removing undo */
        index = -2;

        if (undo instanceof FixtureAddRemoveUndo)
        {
            FixtureAddRemoveUndo addRemoveUndo = (FixtureAddRemoveUndo) undo;

            index = addRemoveUndo.getTargetIndex(redo);
        }
        else if (undo instanceof CompoundUndo && ((CompoundUndo) undo).has(FixtureAddRemoveUndo.class))
        {
            CompoundUndo compound = (CompoundUndo) undo;
            FixtureAddRemoveUndo addRemoveUndo = (FixtureAddRemoveUndo) (redo ? compound.getLast(FixtureAddRemoveUndo.class) : compound.getFirst(FixtureAddRemoveUndo.class));

            index = addRemoveUndo.getTargetIndex(redo);
        }

        if (index >= -1)
        {
            CameraProfile profile = this.getProfile();
            AbstractFixture fixture = profile.size() == 0 ? null : profile.get(index);

            this.pickCameraFixture(fixture, -1);
            this.updateValues();
            this.timeline.rescale();
        }

        /* Handle viewport */
        if (undo instanceof CameraProfileUndo)
        {
            CameraProfileUndo profileUndo = (CameraProfileUndo) undo;

            cursor = profileUndo.cursor;
            min = profileUndo.viewMin;
            max = profileUndo.viewMax;
        }
        else if (undo instanceof CompoundUndo && ((CompoundUndo) undo).has(CameraProfileUndo.class))
        {
            CameraProfileUndo profileUndo = (CameraProfileUndo) ((CompoundUndo) undo).getFirst(CameraProfileUndo.class);

            cursor = profileUndo.cursor;
            min = profileUndo.viewMin;
            max = profileUndo.viewMax;
        }

        if (cursor >= 0 && this.timeline.value != cursor)
        {
            this.timeline.setValueFromScrub(cursor);
        }

        if (min >= 0 && max >= 0 && min != max)
        {
            this.timeline.scale.view(min, max);
        }
    }

    public void markLastNoMerging()
    {
        IUndo<CameraProfile> undo = this.getProfile().undoManager.getCurrentUndo();

        if (undo != null)
        {
            undo.noMerging();
        }
    }

    public void undo()
    {
        CameraProfile profile = this.getProfile();

        if (profile != null && profile.undoManager.undo(profile))
        {
            GuiUtils.playClick();
        }
    }

    public void redo()
    {
        CameraProfile profile = this.getProfile();

        if (profile != null && profile.undoManager.redo(profile))
        {
            GuiUtils.playClick();
        }
    }

    public void exit()
    {
        this.closeScreen();
    }

    public void postRewind(int tick)
    {
        ClientProxy.EVENT_BUS.post(new CameraEditorEvent.Rewind(this, tick));
    }

    public void postPlayback(int tick)
    {
        this.postPlayback(tick, this.playing);
    }

    public void postPlayback(int tick, boolean playing)
    {
        ClientProxy.EVENT_BUS.post(new CameraEditorEvent.Playback(this, playing, tick));
    }

    public void postScrub(int tick)
    {
        ClientProxy.EVENT_BUS.post(new CameraEditorEvent.Scrubbed(this, this.runner.isRunning(), tick));
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

    public boolean isSyncing()
    {
        return Aperture.editorSync.get();
    }

    public boolean isFlightEnabled()
    {
        return this.flight.isFlightEnabled();
    }

    public boolean isFlightDisabled()
    {
        return !this.flight.isFlightEnabled();
    }

    public void haveScrubbed()
    {
        this.haveScrubbed = true;
    }

    /**
     * Teleport player and setup position, motion and angle based on the value
     * was scrubbed from playback scrubber.
     */
    public void scrubbed(GuiPlaybackScrub scrub, int value, boolean fromScrub)
    {
        if (this.runner.isRunning())
        {
            this.runner.ticks = value;
        }

        if (fromScrub)
        {
            this.haveScrubbed();

            this.postScrub(value);
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
            this.timeline.index = -1;
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
                    int offset = (int) panel.currentOffset();

                    this.timeline.setValueFromScrub((int) (offset + duration));
                }

                this.timeline.index = this.getProfile().fixtures.indexOf(fixture);
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

        if (this.replacing && !this.getProfile().has(this.timeline.index))
        {
            return;
        }

        CameraProfile profile = this.getProfile();

        if (this.panel.delegate == null)
        {
            this.postUndoCallback(FixtureAddRemoveUndo.create(this, profile, profile.size(), fixture));
        }
        else
        {
            if (this.replacing)
            {
                AbstractFixture present = profile.get(this.timeline.index);

                fixture.copyByReplacing(present);
                this.postUndoCallback(new CompoundUndo<CameraProfile>(
                    FixtureAddRemoveUndo.create(this, profile, this.timeline.index, null),
                    FixtureAddRemoveUndo.create(this, profile, this.timeline.index, fixture)
                ).noMerging());

                this.replacing = false;
            }
            else
            {
                this.postUndoCallback(FixtureAddRemoveUndo.create(this, profile, this.timeline.index + 1, fixture));
            }
        }
    }

    /**
     * Duplicate current fixture 
     */
    private void dupeFixture()
    {
        int index = this.timeline.index;

        if (this.getProfile().has(index))
        {
            CameraProfile profile = this.getProfile();

            this.postUndoCallback(FixtureAddRemoveUndo.create(this, profile, index + 1, profile.get(index).copy()));
        }
    }

    /**
     * Remove current fixture 
     */
    private void removeFixture()
    {
        int index = this.timeline.index;
        CameraProfile profile = this.getProfile();

        if (profile.has(index))
        {
            this.postUndo(FixtureAddRemoveUndo.create(this, profile, index, null));
            this.pickCameraFixture(index - 1 >= 0 ? profile.get(index - 1) : null, -1);
            this.updateValues();
            this.timeline.rescale();
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

            CameraProfile profile = this.getProfile();
            List<IUndo<CameraProfile>> fixtures = new ArrayList<IUndo<CameraProfile>>();

            long duration = profile.getDuration();
            int i = 0;

            for (Integer tick : this.markers)
            {
                long difference = tick - duration;

                if (tick < duration || difference <= 0)
                {
                    continue;
                }

                IdleFixture fixture = new IdleFixture(difference);

                fixture.fromPlayer(this.getCamera());
                fixtures.add(FixtureAddRemoveUndo.create(this, profile, profile.size() + i, fixture));

                duration += difference;
                i += 1;
            }

            this.postUndoCallback(new CompoundUndo(fixtures.toArray(new IUndo[fixtures.size()])).noMerging());
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
        int where = this.timeline.value;

        CameraProfile profile = this.getProfile();
        AbstractFixture current = profile.atTick(where);

        if (current != null)
        {
            long offset = profile.calculateOffset(current);
            long duration = current.getDuration();
            long diff = where - offset;

            if (diff <= 0 || diff >= duration)
            {
                return;
            }

            AbstractFixture fixture = current.copy();
            AbstractFixture newFixture = fixture.breakDown(diff);

            if (newFixture == null)
            {
                return;
            }

            fixture.setDuration(diff);

            int index = profile.fixtures.indexOf(current);

            this.postUndoCallback(new CompoundUndo<CameraProfile>(
                new FixtureValueChangeUndo(index, profile.fixtures.getPath() + "." + index, current.copy(), fixture).view(this.timeline),
                new FixtureAddRemoveUndo(index + 1, newFixture, null).view(this.timeline)
            ).noMerging());
        }
    }

    /**
     * Set flight mode
     */
    public void setFlight(boolean flight)
    {
        if (flight)
        {
            this.lastPartialTick = 0;
        }

        if (!this.runner.isRunning() || !flight)
        {
            this.flight.setFlightEnabled(flight);

            /* Marking latest undo as unmergeable */
            if (this.isSyncing())
            {
                if (!flight)
                {
                    this.markLastNoMerging();
                }
                else
                {
                    this.lastPosition.set(Position.ZERO);
                }
            }
        }

        this.cameraOptions.update();

        if (flight)
        {
            this.haveScrubbed();
        }
    }

    /**
     * Set aspect ratio for letter box feature. This method parses the 
     * aspect ratio either for float or "float:float" format and sets it 
     * as aspect ratio. 
     */
    public void setAspectRatio(String aspectRatio)
    {
        this.aspectRatio = CameraUtils.parseAspectRation(aspectRatio, this.aspectRatio);
    }

    public void addPathPoint()
    {
        if (this.getFixture() instanceof PathFixture)
        {
            ((GuiPathFixturePanel) this.panel.delegate).points.addPoint();
        }
    }

    /**
     * Set camera profile
     */
    public boolean setProfile(CameraProfile profile)
    {
        boolean isSame = this.getCurrentProfile() == profile;

        this.setCurrentProfile(profile);
        this.profiles.selectProfile(profile);
        this.timeline.setProfile(profile);
        this.updateSaveButton(profile);
        this.minema.setProfile(profile);

        if (!isSame)
        {
            this.pickCameraFixture(null, 0);
        }
        else if (this.panel.delegate != null)
        {
            this.timeline.index = profile.fixtures.indexOf(this.getFixture());
        }

        this.lastSave = System.currentTimeMillis();

        return isSame;
    }

    public void updateSaveButton(CameraProfile profile)
    {
        if (this.save != null)
        {
            this.save.both(profile != null && profile.dirty ? Icons.SAVE : Icons.SAVED);
        }
    }

    /**
     * Update the state of camera editor (should be invoked upon opening this 
     * screen)
     */
    public void updateCameraEditor(EntityPlayer player)
    {
        KeyboardHandler.setSmoothCamera(false);

        this.maxScrub = 0;
        this.haveScrubbed = false;

        this.updateOverlay();
        this.position.set(player);
        this.setProfile(ClientProxy.control.currentProfile);
        this.profiles.init();

        if (this.panel.delegate != null)
        {
            this.panel.delegate.cameraEditorOpened();
            this.modifiers.cameraEditorOpened();
        }

        Minecraft mc = Minecraft.getMinecraft();
        mc.gameSettings.hideGUI = true;
        GuiIngameForge.renderHotbar = false;
        GuiIngameForge.renderCrosshairs = false;

        this.flight.setFlightEnabled(false);
        ClientProxy.control.cache();
        this.setAspectRatio(Aperture.editorLetterboxAspect.get());
        mc.gameSettings.thirdPersonView = 0;
        mc.setRenderViewEntity(player);

        if (Aperture.spectator.get() && !Aperture.outside.get())
        {
            if (ClientProxy.control.lastGameMode != GameType.SPECTATOR)
            {
                ((EntityPlayerSP) player).sendChatMessage("/gamemode 3");
            }
        }

        this.runner.attachOutside();
    }

    public CameraRunner getRunner()
    {
        return this.runner;
    }

    /**
     * Get current camera profile
     *
     * If the camera profile is null, then improvise...
     */
    public CameraProfile getProfile()
    {
        if (this.getCurrentProfile() == null)
        {
            this.profiles.selectFirstAvailable(-1);
        }

        return this.getCurrentProfile();
    }

    private CameraProfile getCurrentProfile()
    {
        return ClientProxy.control.currentProfile;
    }

    private void setCurrentProfile(CameraProfile profile)
    {
        ClientProxy.control.currentProfile = profile;
    }

    public AbstractFixture getFixture()
    {
        return this.panel.delegate == null ? null : this.panel.delegate.fixture;
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
     * Update player to current value in the timeline
     */
    public void updatePlayerCurrently(float partialTicks)
    {
        if ((this.isSyncing() || this.runner.outside.active) && !this.runner.isRunning())
        {
            this.updatePlayer(this.timeline.value, partialTicks);
        }
    }

    /**
     * Update player
     */
    public void updatePlayer(long tick, float ticks)
    {
        long duration = this.getProfile().getDuration();

        tick = tick < 0 ? 0 : tick;
        tick = tick > duration ? duration : tick;

        EntityPlayer player = Minecraft.getMinecraft().player;

        this.position.set(player);
        this.getProfile().applyCurves(tick, this.lastPartialTick);
        this.getProfile().applyProfile(tick, ticks, this.lastPartialTick, this.position);

        this.position.apply(this.getCamera());
        ClientProxy.control.setRollAndFOV(this.position.angle.roll, this.position.angle.fov);
    }

    /**
     * This method should be invoked when values in the panel were modified
     */
    public void updateValues()
    {
        this.timeline.set(0, Math.max((int) this.getProfile().getDuration(), this.maxScrub));
        this.timeline.setValue(this.timeline.value);
    }

    public void updateDuration(AbstractFixture fixture)
    {
        this.timeline.rescale();
        this.updateValues();

        this.profiles.updateDuration();
        this.modifiers.updateDuration();
    }

    /**
     * Makes camera profile as dirty as possible 
     */
    public void updateProfile()
    {
        this.getProfile().dirty();

        if (this.panel.delegate != null)
        {
            this.panel.delegate.profileWasUpdated();
        }

        this.updateSaveButton(this.getProfile());
    }

    /**
     * Saves camera profile
     */
    public void saveProfile()
    {
        this.getProfile().save();
        this.profiles.init();
    }

    /**
     * Get player's current position 
     */
    public Position getPosition()
    {
        Position position = new Position(this.getCamera());
        AbstractFixture fixture = this.getFixture();

        if (fixture != null && fixture.modifiers.size() != 0)
        {
            Position withModifiers = new Position();
            this.getProfile().applyProfile(this.timeline.value, 0, withModifiers);

            Position noModifiers = new Position();
            this.getProfile().applyProfile(this.timeline.value, 0, noModifiers, false);

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

    private void hideReplacingPopups(GuiElement exception, boolean replacing)
    {
        if (this.replacing != replacing && exception.isVisible())
        {
            exception.toggleVisible();
        }

        this.replacing = replacing;

        this.fixtures.flex().relative(replacing ? this.replace.resizer() : this.add.resizer());
        this.fixtures.resize();

        this.hidePopups(exception);
    }

    private void hidePopups(GuiElement exception)
    {
        this.hidePopups(exception, !exception.isVisible());
    }

    private void hidePopups(GuiElement exception, boolean visible)
    {
        this.profiles.setVisible(false);
        this.config.setVisible(false);
        this.modifiers.setVisible(false);
        this.fixtures.setVisible(false);
        this.minema.setVisible(false);

        exception.setVisible(visible);
    }

    /**
     * Update display icon of the plause button
     */
    private void updatePlauseButton()
    {
        this.plause.both(this.runner.isRunning() ? Icons.PAUSE : Icons.PLAY);
    }

    /**
     * Jump to the next frame (tick)
     */
    private void jumpToNextFrame()
    {
        this.timeline.setValueFromScrub(this.timeline.value + 1);
    }

    /**
     * Jump to the previous frame (tick) 
     */
    private void jumpToPrevFrame()
    {
        this.timeline.setValueFromScrub(this.timeline.value - 1);
    }

    private void editFixture(boolean checkForBeingStatic)
    {
        Position current = this.getPosition();

        if (this.panel.delegate != null)
        {
            if (!checkForBeingStatic || !this.lastPosition.equals(current))
            {
                this.panel.delegate.editFixture(current);
            }
        }

        this.lastPosition.set(current);
        this.haveScrubbed();
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

        /* Move duration to the timeline location */
        CameraProfile profile = getProfile();
        AbstractFixture fixture = profile.get(this.timeline.index);
        long offset = profile.calculateOffset(fixture);

        if (this.timeline.value > offset && fixture != null)
        {
            this.postUndo(GuiAbstractFixturePanel.undo(this, fixture.duration, this.timeline.value - offset));

            this.updateValues();
            this.panel.delegate.select(fixture, 0);
        }
    }

    /**
     * Jump to the next camera fixture
     */
    private void jumpToNextFixture()
    {
        this.timeline.setValueFromScrub((int) this.getProfile().calculateOffset(this.timeline.value, true));
    }

    /**
     * Jump to previous fixture
     */
    private void jumpToPrevFixture()
    {
        this.timeline.setValueFromScrub((int) this.getProfile().calculateOffset(this.timeline.value - 1, false));
    }

    /**
     * Move current fixture
     */
    private void moveTo(int direction)
    {
        CameraProfile profile = this.getProfile();
        int from = this.timeline.index;
        int to = from + direction;

        if (profile.has(from) && profile.has(to))
        {
            AbstractFixture fixture = profile.get(from);

            this.postUndoCallback(new CompoundUndo<CameraProfile>(
                FixtureAddRemoveUndo.create(this, profile, from, null),
                FixtureAddRemoveUndo.create(this, profile, to, fixture)
            ).noMerging());
        }
    }

    public void togglePlayback()
    {
        this.setFlight(false);
        this.runner.toggle(this.getProfile(), this.timeline.value, () -> Math.max(this.getProfile().getDuration(), this.maxScrub));
        this.updatePlauseButton();

        this.playing = this.runner.isRunning();

        if (!this.playing)
        {
            this.runner.attachOutside();
            this.updatePlayerCurrently();
        }

        this.postPlayback(this.timeline.value);
    }

    @Override
    protected void viewportSet()
    {
        this.creationBar.flex().relative(this.rightBar.flex()).x(-20).y(0F).anchorX(1F);
        this.fixtures.flex().anchorX(1F).xy(1F, 1F).w(70);

        int a = this.creationBar.flex().getX();
        int b = this.middleBar.flex().getX() + this.middleBar.flex().getW();
        int diff = a - b;

        if (diff < 0)
        {
            this.creationBar.flex().relative(this.leftBar.flex()).x(0).y(1F).anchorX(0F);
            this.fixtures.flex().anchorX(0F).xy(0F, 1F).w(70);
        }
    }

    @Override
    protected void closeScreen()
    {
        this.minema.stop();
        Minecraft.getMinecraft().gameSettings.hideGUI = false;
        ClientProxy.control.restore();
        GuiIngameForge.renderHotbar = true;
        GuiIngameForge.renderCrosshairs = true;

        if (!this.runner.isRunning())
        {
            this.runner.detachOutside();
        }

        super.closeScreen();
    }

    /* Rendering code */

    /**
     * Draw everything on the screen
     */
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        /* What the fuck, why is partial tick is always 0.32 on 1.12.2? */
        partialTicks = Minecraft.getMinecraft().getRenderPartialTicks();

        this.lastPartialTick = 0;
        if (this.runner.isRunning())
        {
            this.lastPartialTick = partialTicks;
        }

        this.updateLogic(partialTicks);
        this.drawOverlays();

        if (!this.top.canBeSeen())
        {
            InputRenderer.disable();

            /* Little tip for users who don't know what they did */
            if (this.canRenderF1Tooltip())
            {
                this.fontRenderer.drawStringWithShadow(I18n.format("aperture.gui.editor.f1"), 5, this.height - 12, 0xffffff);
            }
        }
        else
        {
            this.drawEditorsBackground();
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    /**
     * Whether the F1 tooltip (for users who accidentally hit F1 without knowing)
     * should be rendered?
     */
    private boolean canRenderF1Tooltip()
    {
        return Aperture.editorF1Tooltip.get() && !(this.runner.isRunning() || this.minema.isRecording());
    }

    /**
     * Update logic for such components as repeat fixture, minema recording,
     * sync mode, flight mode, etc.
     */
    private void updateLogic(float partialTicks)
    {
        AbstractFixture fixture = this.getFixture();

        if (!this.haveScrubbed)
        {
            this.mc.player.motionX = 0;
            this.mc.player.motionY = 0;
            this.mc.player.motionZ = 0;
        }

        int autoSave = Aperture.editorAutoSave.get();

        if (autoSave > 0)
        {
            long current = System.currentTimeMillis();

            if (this.lastSave == 0)
            {
                this.lastSave = current;
            }

            if (current >= this.lastSave + autoSave * 1000)
            {
                if (this.getProfile().dirty && this.isFlightDisabled())
                {
                    this.saveProfile();
                }

                this.lastSave = current;
            }
        }

        /* Loop fixture */
        if (Aperture.editorLoop.get() && this.runner.isRunning() && !this.minema.isRecording())
        {
            long min = -1;
            long max = -1;

            if (fixture != null)
            {
                min = this.getProfile().calculateOffset(fixture);
                max = min + fixture.getDuration();
            }

            if (this.timeline.loopMin != this.timeline.loopMax && this.timeline.loopMin >= 0 && this.timeline.loopMin < this.timeline.loopMax)
            {
                min = this.timeline.loopMin;
                max = this.timeline.loopMax;
            }

            max = Math.min(max, Math.max(this.getProfile().getDuration(), this.maxScrub));

            if (min >= 0 && max >= 0 && min < max && this.runner.ticks >= max - 1)
            {
                this.timeline.setValueFromScrub((int) min);
            }
        }

        /* Animate flight mode */
        this.flight.animate(this.context, this.position);

        if (this.flight.isFlightEnabled())
        {
            this.position.apply(this.getCamera());
            ClientProxy.control.roll = this.position.angle.roll;
            this.mc.gameSettings.fovSetting = this.position.angle.fov;

            if (this.isSyncing() && this.haveScrubbed)
            {
                this.editFixture(true);
            }
        }

        /* Update playback timeline */
        if (this.runner.isRunning())
        {
            this.timeline.setValue((int) this.runner.ticks);
        }

        /* Rewind playback back to 0 */
        if (!this.runner.isRunning() && this.playing)
        {
            this.updatePlauseButton();
            this.runner.attachOutside();
            this.timeline.setValueFromScrub(0);

            this.postRewind(this.timeline.value);
            this.playing = false;
        }

        /* Sync the player on current tick */
        if (!this.flight.isFlightEnabled() && (this.runner.outside.active || (this.isSyncing() && this.haveScrubbed)))
        {
            this.updatePlayerCurrently(partialTicks);
        }

        this.minema.minema(this.runner.isRunning() ? (int) this.runner.ticks : this.timeline.value, partialTicks);
    }

    /**
     * Draw little squares behind some of the visible elements
     */
    private void drawEditorsBackground()
    {
        this.drawGradientRect(0, 0, width, 20, 0x66000000, 0);
        this.drawIcons();

        /* Draw backgrounds for popups */
        if (this.profiles.isVisible())
        {
            this.openProfiles.area.draw(0xaa000000);
        }
        else if (this.config.isVisible())
        {
            this.openConfig.area.draw(0xaa000000);
        }
        else if (this.modifiers.isVisible())
        {
            this.openModifiers.area.draw(0xaa000000);
        }
        else if (this.minema.isVisible())
        {
            this.openMinema.area.draw(0xaa000000);
        }

        if (this.fixtures.isVisible())
        {
            if (this.replacing)
            {
                this.replace.area.draw(0xaa000000);
            }
            else
            {
                this.add.area.draw(0xaa000000);
            }
        }

        if (this.creating)
        {
            this.creation.area.draw(0xaa000000);
        }
    }

    /**
     * Draw icons for indicating different active states (like syncing 
     * or flight mode) 
     */
    private void drawIcons()
    {
        if (!this.isSyncing() && !this.flight.isFlightEnabled())
        {
            return;
        }

        int x = this.width - 18;
        int y = 22;

        Gui.drawRect(this.width - (this.isSyncing() ? 20 : 0) - (this.flight.isFlightEnabled() ? 20 : 0), y - 2, this.width, y + 18, ColorUtils.HALF_BLACK);
        GlStateManager.color(1, 1, 1, 1);

        if (this.isSyncing())
        {
            Icons.DOWNLOAD.render(x, y);
            x -= 20;
        }

        if (this.flight.isFlightEnabled())
        {
            this.flight.getMovementType().icon.render(x, y);
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
            int y = panel.getY() + panel.getH() - 20 - 15 * i;
            int x = panel.getX() + 10;

            Gui.drawRect(x, y - 3, x + width + 4, y + 10, 0xbb000000);
            this.fontRenderer.drawStringWithShadow(label, x + 2, y, 0xffffff);

            i--;
        }
    }

    /**
     * Draw different camera type overlays (custom texture overlay, letterbox,
     * rule of thirds and crosshair)
     */
    private void drawOverlays()
    {
        if (Aperture.editorOverlay.get() && this.overlayLocation != null)
        {
            this.mc.renderEngine.bindTexture(this.overlayLocation);
            Gui.drawModalRectWithCustomSizedTexture(0, 0, 0, 0, this.width, this.height, this.width, this.height);
        }

        /* Readjustable values for rule of thirds in case of letter box enabled */
        int rx = 0;
        int ry = 0;
        int rw = this.width;
        int rh = this.height;

        /* The rendering went to RenderingHandler */
        if (Aperture.editorLetterbox.get() && this.aspectRatio > 0)
        {
            int width = (int) (this.aspectRatio * this.height);

            if (width != this.width)
            {
                if (width < this.width)
                {
                    /* Horizontal bars */
                    int w = (this.width - width) / 2;

                    rx = w;
                    rw -= w * 2;
                }
                else
                {
                    /* Vertical bars */
                    int h = (int) (this.height - (1F / this.aspectRatio * this.width)) / 2;

                    ry = h;
                    rh -= h * 2;
                }
            }
        }

        if (!this.top.canBeSeen())
        {
            return;
        }

        if (Aperture.editorRuleOfThirds.get())
        {
            int color = Aperture.editorGuidesColor.get();

            Gui.drawRect(rx + rw / 3 - 1, ry, rx + rw / 3, ry + rh, color);
            Gui.drawRect(rx + rw - rw / 3, ry, rx + rw - rw / 3 + 1, ry + rh, color);

            Gui.drawRect(rx, ry + rh / 3 - 1, rx + rw, ry + rh / 3, color);
            Gui.drawRect(rx, ry + rh - rh / 3, rx + rw, ry + rh - rh / 3 + 1, color);
        }

        if (Aperture.editorCenterLines.get())
        {
            int color = Aperture.editorGuidesColor.get();

            this.drawHorizontalLine(0, this.width, this.height / 2, color);
            this.drawVerticalLine(this.width / 2, 0, this.height, color);
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