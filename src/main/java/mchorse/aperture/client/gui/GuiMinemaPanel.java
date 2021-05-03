package mchorse.aperture.client.gui;

import info.ata4.minecraft.minema.MinemaAPI;
import mchorse.aperture.Aperture;
import mchorse.aperture.ClientProxy;
import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.camera.minema.MinemaIntegration;
import mchorse.aperture.capabilities.camera.Camera;
import mchorse.aperture.events.CameraEditorEvent;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiButtonElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiCirculateElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiToggleElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTextElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTrackpadElement;
import mchorse.mclib.client.gui.framework.elements.modals.GuiMessageModal;
import mchorse.mclib.client.gui.framework.elements.modals.GuiModal;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.framework.elements.utils.GuiDraw;
import mchorse.mclib.client.gui.framework.elements.utils.GuiDrawable;
import mchorse.mclib.client.gui.framework.elements.utils.GuiLabel;
import mchorse.mclib.client.gui.utils.Elements;
import mchorse.mclib.client.gui.utils.Label;
import mchorse.mclib.client.gui.utils.keys.IKey;
import mchorse.mclib.config.values.ValueBoolean;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

public class GuiMinemaPanel extends GuiElement {
    public GuiCameraEditor editor;

    public GuiElement fields;
    public GuiTextElement name;
    public GuiCirculateElement mode;
    public GuiTrackpadElement left;
    public GuiTrackpadElement right;
    public GuiButtonElement setLeft;
    public GuiButtonElement setRight;
    public GuiButtonElement movies;
    public GuiToggleElement tracking;
    public GuiToggleElement originButton;
    public GuiTrackpadElement trackingX;
    public GuiTrackpadElement trackingY;
    public GuiTrackpadElement trackingZ;
    public GuiLabel originTitle;
    public GuiElement originRow;
    public GuiElement trackingElements;
    public GuiButtonElement record;

    private GuiElement leftRight;
    private GuiElement setLeftRight;

    private RecordingMode recordingMode = RecordingMode.FULL;
    private boolean recording;
    private boolean waiting;
    private int start;
    private int end;
    private JsonArray trackingData = new JsonArray();
    private double[] trackingInitialPos = {0,0,0};

    private long lastUpdate;
    private String lastName;
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");

    public GuiMinemaPanel(Minecraft mc, GuiCameraEditor editor) {
        super(mc);

        this.editor = editor;

        this.fields = new GuiElement(mc);
        this.name = new GuiTextElement(mc, (Consumer<String>) null).filename();
        this.name.tooltip(IKey.lang("aperture.gui.minema.output"));
        this.mode = new GuiCirculateElement(mc, this::switchMode);

        for (RecordingMode mode : RecordingMode.values()) {
            this.mode.addLabel(IKey.lang("aperture.gui.minema.modes." + mode.id));
        }

        this.mode.tooltip(IKey.lang("aperture.gui.minema.modes.tooltip"));

        this.left = new GuiTrackpadElement(mc, (Consumer<Double>) null);
        this.left.limit(0).integer();
        this.left.setValue(0);
        this.right = new GuiTrackpadElement(mc, (Consumer<Double>) null);
        this.right.limit(0).integer();
        this.right.setValue(0);
        this.setLeft = new GuiButtonElement(mc, IKey.lang("aperture.gui.minema.set_start"), this::calculateLeft);
        this.setLeft.tooltip(IKey.lang("aperture.gui.minema.set_start_tooltip"));
        this.setRight = new GuiButtonElement(mc, IKey.lang("aperture.gui.minema.set_duration"), this::calculateRight);
        this.setRight.tooltip(IKey.lang("aperture.gui.minema.set_duration_tooltip"));
        this.record = new GuiButtonElement(mc, IKey.lang("aperture.gui.minema.record"), this::startRecording);
        this.movies = new GuiButtonElement(mc, IKey.lang("minema.gui.movies_folder"), this::openMovies);
        this.tracking = new GuiToggleElement(mc, IKey.lang("aperture.gui.minema.tracking_button"), (b) ->
        {
            this.updateButtons();
        });
        this.tracking.tooltip(IKey.lang("aperture.gui.minema.tracking_tooltip"));

        this.originButton = new GuiToggleElement(mc, IKey.lang("aperture.gui.modifiers.tracking_origin_title"), null);
        this.originButton.tooltip(IKey.lang("aperture.gui.minema.tracking_origin_title_tooltip"));

        this.trackingElements = new GuiElement(mc);
        this.trackingElements.flex().column(2).stretch().vertical().height(3);

        this.trackingX = new GuiTrackpadElement(mc, (Consumer<Double>) null);
        this.trackingX.tooltip(IKey.lang("aperture.gui.minema.tracking_origin_x"));

        this.trackingY = new GuiTrackpadElement(mc, (Consumer<Double>) null);
        this.trackingY.tooltip(IKey.lang("aperture.gui.minema.tracking_origin_y"));

        this.trackingZ = new GuiTrackpadElement(mc, (Consumer<Double>) null);
        this.trackingZ.tooltip(IKey.lang("aperture.gui.minema.tracking_origin_z"));

        this.originTitle = Elements.label(IKey.lang("aperture.gui.minema.tracking_origin_title"), 20).anchor(0, 1F);
        this.originTitle.tooltip(IKey.lang("aperture.gui.minema.tracking_origin_title_tooltip"));

        this.originRow = Elements.row(mc,2, this.trackingX, this.trackingY, this.trackingZ);

        this.trackingElements.add(this.tracking);

        this.fields.flex().relative(this.flex()).w(1F).column(5).vertical().stretch().height(20).padding(10);
        this.flex().hTo(this.fields.flex(), 1F);

        this.fields.add(Elements.label(IKey.lang("aperture.gui.minema.title"), 12).background());
        this.fields.add(this.name, this.mode, this.trackingElements);

        this.fields.add(this.leftRight = Elements.row(mc, 5, 0, 20, this.left, this.right));
        this.fields.add(this.setLeftRight = Elements.row(mc, 5, 0, 20, this.setLeft, this.setRight));
        this.fields.add(Elements.row(mc, 5, 0, 20, this.movies, this.record));

        this.add(this.fields);
        this.add(new GuiDrawable((context) ->
        {
            if (this.fields.isVisible() && !this.name.isFocused() && this.name.field.getText().isEmpty()) {
                long current = System.currentTimeMillis();

                if (current > this.lastUpdate + 1000) {
                    this.lastUpdate = current;
                    this.lastName = this.format.format(new Date());
                }

                String filename = Aperture.minemaDefaultProfileName.get() ? this.getFilename() : this.lastName;

                this.font.drawStringWithShadow(filename, this.name.area.x + 5, this.name.area.my() - 4, 0x888888);
            }
        }));

        this.fields.setVisible(MinemaIntegration.isLoaded() && MinemaIntegration.isAvailable());
        this.switchMode(this.mode);
    }

    private void updateButtons()
    {
        this.originTitle.removeFromParent();
        this.originButton.removeFromParent();
        this.originRow.removeFromParent();

        if (this.tracking.isToggled())
        {
            this.trackingElements.add(this.originButton);
            this.trackingElements.add(this.originRow);
        }

        this.getParent().resize();
    }

    public void setProfile(CameraProfile profile) {
        this.left.setValue(0);
        this.right.setValue(profile == null ? 30 : profile.getDuration());
    }

    private void switchMode(GuiButtonElement element) {
        this.recordingMode = RecordingMode.values()[this.mode.getValue()];

        this.leftRight.setVisible(this.recordingMode == RecordingMode.CUSTOM);
        this.setLeftRight.setVisible(this.recordingMode == RecordingMode.CUSTOM);
    }

    public boolean isRecording() {
        return this.recording;
    }

    private boolean isRunning() {
        return this.editor.getRunner().isRunning();
    }

    private String getFilename() {
        String text = this.name.field.getText();

        if (!text.isEmpty()) {
            return text;
        }

        if (!Aperture.minemaDefaultProfileName.get()) {
            return "";
        }

        text = this.editor.getProfile().getDestination().getFilename();

        if (this.recordingMode == RecordingMode.FIXTURE) {
            AbstractFixture fixture = this.editor.getFixture();

            if (fixture != null) {
                text += "-" + (this.editor.getProfile().fixtures.indexOf(fixture) + 1);
            }
        }

        return text;
    }

    private void calculateLeft(GuiButtonElement button) {
        int right = (int) (this.left.value + this.right.value);

        this.left.setValue(this.editor.timeline.value);
        this.right.setValue(right - this.left.value);
    }

    private void calculateRight(GuiButtonElement button) {
        this.right.setValue(this.editor.timeline.value - this.left.value);
    }

    private void openMovies(GuiButtonElement button) {
        MinemaIntegration.openMovies();
    }

    private void startRecording(GuiButtonElement button) {
        if (this.isRunning() || MinemaIntegration.isRecording()) {
            return;
        }

        /* Calculate start and end ticks */
        this.start = (int) this.left.value;
        this.end = this.start + (int) this.right.value;

        if (this.recordingMode == RecordingMode.FIXTURE && this.editor.panel.delegate != null) {
            AbstractFixture fixture = this.editor.panel.delegate.fixture;

            this.start = (int) this.editor.getProfile().calculateOffset(fixture);
            this.end = (int) (this.start + fixture.getDuration());
        } else if (this.recordingMode == RecordingMode.FULL) {
            this.start = 0;
            this.end = (int) this.editor.getProfile().getDuration();
        }

        if (this.end - this.start <= 0) {
            return;
        }

        MinemaIntegration.setName(this.getFilename());

        try {
            MinemaIntegration.toggleRecording(true);
        } catch (Exception e) {
            GuiModal.addFullModal(this, () -> new GuiMessageModal(this.mc, IKey.str(MinemaIntegration.getMessage(e))));

            return;
        }

        ClientProxy.EVENT_BUS.post(new CameraEditorEvent.Rewind(this.editor, this.start));

        this.editor.timeline.setValueFromScrub(this.start);
        this.editor.updatePlayer(this.start, 0);

        this.editor.root.setVisible(false);
        this.recording = this.waiting = true;
    }

    public void stop() {
        if (this.recording) {
            try {
                MinemaIntegration.toggleRecording(false);
            } catch (Exception e) {
            }

            if (this.isRunning()) {
                this.editor.togglePlayback();
            }

            this.editor.root.setVisible(true);
            this.recording = this.waiting = false;

            if (this.trackingData.size()!=0)
            {
                exportTrackingData();
            }

            this.trackingData = new JsonArray();
            this.trackingInitialPos[0] = 0;
            this.trackingInitialPos[1] = 0;
            this.trackingInitialPos[2] = 0;
        }
    }

    /**
     * Update the minema recording logic
     */
    public void minema(int ticks, float partialTicks)
    {
        if (!this.recording) {
            return;
        }

        if (!MinemaIntegration.isRecording()) {
            this.stop();

            GuiModal.addFullModal(this, () -> new GuiMessageModal(this.mc, IKey.lang("aperture.gui.minema.premature_stop")));

            return;
        }
        else if (this.tracking.isToggled() && this.isRunning())
        {
            this.writeTrackingdata();
        }

        if (this.waiting) {
            if (!this.isRunning() && partialTicks == 0) {
                this.editor.togglePlayback();
                this.waiting = false;
            }
        } else {
            if (this.isRunning() && ticks >= this.end) {
                this.editor.togglePlayback();
            } else if (!this.isRunning()) {
                this.stop();
            }
        }

        if (Aperture.debugTicks.get()) {
            this.font.drawStringWithShadow(String.valueOf(ticks + partialTicks), 0, 0, 0xffffff);
        }
    }

    private void writeTrackingdata()
    {
        Position position = this.editor.getRunner().getPosition();
        JsonArray frame = new JsonArray();
        JsonObject frameData = new JsonObject();
        JsonArray positionData = new JsonArray();
        JsonArray angleData = new JsonArray();

        if (this.originButton.isToggled())
        {
            this.trackingInitialPos[0] = this.trackingX.value;
            this.trackingInitialPos[1] = this.trackingY.value - 1.62;
            this.trackingInitialPos[2] = this.trackingZ.value;
        }
        else if (this.trackingInitialPos[0] == 0 && this.trackingInitialPos[1] == 0 && this.trackingInitialPos[2] == 0)
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

        frameData.add("position", positionData);
        frameData.add("angle", angleData);

        frame.add(frameData);
        this.trackingData.add(frame);
    }

    private void exportTrackingData()
    {
        try
        {
            FileWriter file = new FileWriter(MinemaAPI.getCapturePath().toURI().getPath() +(this.getFilename().equals("") ?  this.format.format(new Date(System.currentTimeMillis())) : this.getFilename() )+ ".json");

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

    @Override
    public void draw(GuiContext context)
    {
        this.area.draw(0xaa000000);

        int x = this.area.x + 10;
        int y = this.area.my();

        if (!MinemaIntegration.isLoaded())
        {
            GuiDraw.drawMultiText(this.font, I18n.format("aperture.gui.minema.minema_not_installed"), x, y, 0xffffff, this.area.w - 20, 12, 0.5F, 0.5F);
        }
        else if (!MinemaIntegration.isAvailable())
        {
            GuiDraw.drawMultiText(this.font, I18n.format("aperture.gui.minema.minema_wrong_version"), x, y, 0xffffff, this.area.w - 20, 12, 0.5F, 0.5F);
        }

        super.draw(context);
    }

    public static enum RecordingMode
    {
        FULL("full"), FIXTURE("fixture"), CUSTOM("custom");

        public final String id;

        private RecordingMode(String id)
        {
            this.id = id;
        }
    }
}