package mchorse.aperture.client.gui;

import mchorse.aperture.Aperture;
import mchorse.aperture.ClientProxy;
import mchorse.aperture.camera.CameraExporter;
import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.camera.minema.MinemaIntegration;
import mchorse.aperture.capabilities.camera.Camera;
import mchorse.aperture.client.gui.panels.modifiers.GuiLookModifierPanel;
import mchorse.aperture.client.gui.utils.GuiTextHelpElement;
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
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Consumer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

public class GuiMinemaPanel extends GuiElement
{
    public GuiCameraEditor editor;

    public GuiElement fields;
    public GuiTextElement name;
    public GuiCirculateElement mode;
    public GuiTrackpadElement left;
    public GuiTrackpadElement right;
    public GuiButtonElement setLeft;
    public GuiButtonElement setRight;
    public GuiButtonElement movies;
    public GuiToggleElement trackingButton;
    public GuiElement trackingElementsWrapper;
    public GuiElement trackingElements;
    public GuiToggleElement originButton;
    public GuiTrackpadElement originX;
    public GuiTrackpadElement originY;
    public GuiTrackpadElement originZ;
    public GuiElement originElementsWrapper;
    public GuiLabel originTitle;
    public GuiElement originRow;
    public GuiElement originElements;
    public GuiElement selectorElement;
    public GuiTextHelpElement selector;
    public GuiButtonElement record;
    public static final CameraExporter trackingExporter = new CameraExporter();

    /** wrapper for custom recording mode elements */
    private GuiElement customWrapper;
    /** row for trackpads for start and end */
    private GuiElement leftRight;
    /** row for set start and end buttons */
    private GuiElement setLeftRight;

    private RecordingMode recordingMode = RecordingMode.FULL;
    private boolean recording;
    private int start;
    private int end;

    private long lastUpdate;
    private String lastName;
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");

    public GuiMinemaPanel(Minecraft mc, GuiCameraEditor editor)
    {
        super(mc);

        this.editor = editor;

        this.fields = new GuiElement(mc);
        this.name = new GuiTextElement(mc, (Consumer<String>) null).filename();
        this.name.tooltip(IKey.lang("aperture.gui.minema.output"));
        this.mode = new GuiCirculateElement(mc, (b) ->
        {
            this.switchMode(b);
            this.updateButtons();
        });

        for (RecordingMode mode : RecordingMode.values())
        {
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

        this.trackingButton = new GuiToggleElement(mc, IKey.lang("aperture.gui.minema.tracking_button"), (b) ->
        {
            this.updateButtons();
        });
        this.trackingButton.tooltip(IKey.lang("aperture.gui.minema.tracking_tooltip"));

        this.originButton = new GuiToggleElement(mc, IKey.lang("aperture.gui.minema.tracking_origin_title"), (b) ->
        {
            this.trackingExporter.relativeOrigin = b.isToggled();

            if (b.isToggled())
            {
                this.trackingExporter.setOriginX(this.originX.value);
                this.trackingExporter.setOriginY(this.originY.value);
                this.trackingExporter.setOriginZ(this.originZ.value);
            }
        });
        this.originButton.tooltip(IKey.lang("aperture.gui.minema.tracking_origin_title_tooltip"));

        this.trackingElementsWrapper = new GuiElement(mc);
        this.trackingElementsWrapper.flex().column(4).stretch().vertical().height(3);

        this.trackingElements = new GuiElement(mc);
        this.trackingElements.flex().column(4).stretch().vertical().height(3);
        this.trackingElements.marginTop(10).marginBottom(10);

        this.originElementsWrapper = new GuiElement(mc);
        this.originElementsWrapper.flex().column(4).stretch().vertical().height(3);

        this.originElements = new GuiElement(mc);
        this.originElements.flex().column(4).stretch().vertical().height(3);
        this.originElements.marginTop(7).marginBottom(7);

        this.originRow = Elements.row(mc,4);

        this.selectorElement = new GuiElement(mc);
        this.selectorElement.flex().column(4).stretch().vertical().height(1);

        this.originTitle = Elements.label(IKey.lang("aperture.gui.minema.tracking_origin_title"), 20).anchor(0, 1F);
        this.originTitle.tooltip(IKey.lang("aperture.gui.minema.tracking_origin_title_tooltip"));

        this.originX = new GuiTrackpadElement(mc, (value) ->
        {
            this.trackingExporter.setOriginX(value.doubleValue());
        });
        this.originX.tooltip(IKey.lang("aperture.gui.minema.tracking_origin_x"));

        this.originY = new GuiTrackpadElement(mc,  (value) ->
        {
            this.trackingExporter.setOriginY(value.doubleValue());
        });
        this.originY.tooltip(IKey.lang("aperture.gui.minema.tracking_origin_y"));

        this.originZ = new GuiTrackpadElement(mc,  (value) ->
        {
            this.trackingExporter.setOriginZ(value.doubleValue());
        });
        this.originZ.tooltip(IKey.lang("aperture.gui.minema.tracking_origin_z"));

        this.selector = new GuiTextHelpElement(mc, 500, (str) ->
        {
            this.trackingExporter.selector = str;
            this.trackingExporter.tryFindingEntity(str);
        });
        this.selector.link(GuiLookModifierPanel.TARGET_SELECTOR_HELP).tooltip(IKey.lang("aperture.gui.minema.tracking_entity_selector"));

        this.originRow.add(this.originX, this.originY, this.originZ);
        this.originElements.add(this.originButton, this.originRow);
        this.originElementsWrapper.add(this.originElements);

        this.selectorElement.add(this.selector);
        this.trackingElementsWrapper.add(this.originElementsWrapper, this.selectorElement);

        this.trackingElements.add(this.trackingButton);

        this.customWrapper = new GuiElement(mc);
        this.customWrapper.flex().column(4).stretch().vertical().height(2);

        this.leftRight = Elements.row(mc, 5, 0, 20, this.left, this.right);
        this.setLeftRight = Elements.row(mc, 5, 0, 20, this.setLeft, this.setRight);

        this.fields.flex().relative(this.flex()).w(1F).column(5).vertical().stretch().height(20).padding(10);
        this.flex().hTo(this.fields.flex(), 1F);

        this.fields.add(Elements.label(IKey.lang("aperture.gui.minema.title"), 12).background());
        this.fields.add(this.name, this.mode);

        this.fields.add(this.customWrapper, this.trackingElements, Elements.row(mc, 5, 0, 20, this.movies, this.record));

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
        this.trackingElementsWrapper.removeFromParent();
        this.leftRight.removeFromParent();
        this.setLeftRight.removeFromParent();

        if (this.trackingButton.isToggled())
        {
            this.trackingElements.add(this.trackingElementsWrapper);
        }

        if (this.recordingMode == RecordingMode.CUSTOM)
        {
            this.customWrapper.add(leftRight);
            this.customWrapper.add(setLeftRight);
        }

        this.getParent().resize();
    }

    public void setProfile(CameraProfile profile)
    {
        this.left.setValue(0);
        this.right.setValue(profile == null ? 30 : profile.getDuration());
    }

    private void switchMode(GuiCirculateElement b)
    {
        this.recordingMode = RecordingMode.values()[b.getValue()];
    }

    public boolean isRecording()
    {
        return this.recording;
    }

    private boolean isRunning()
    {
        return this.editor.getRunner().isRunning();
    }

    private String getFilename()
    {
        String text = this.name.field.getText();

        if (!text.isEmpty())
        {
            return text;
        }

        if (!Aperture.minemaDefaultProfileName.get())
        {
            return "";
        }

        text = this.editor.getProfile().getDestination().getFilename();

        if (this.recordingMode == RecordingMode.FIXTURE)
        {
            AbstractFixture fixture = this.editor.getFixture();

            if (fixture != null)
            {
                text += "-" + (this.editor.getProfile().fixtures.indexOf(fixture) + 1);
            }
        }

        return text;
    }

    private void calculateLeft(GuiButtonElement button)
    {
        int right = (int) (this.left.value + this.right.value);

        this.left.setValue(this.editor.timeline.value);
        this.right.setValue(right - this.left.value);
    }

    private void calculateRight(GuiButtonElement button)
    {
        this.right.setValue(this.editor.timeline.value - this.left.value);
    }

    private void openMovies(GuiButtonElement button)
    {
        MinemaIntegration.openMovies();
    }

    private void startRecording(GuiButtonElement button)
    {
        if (this.isRunning() || MinemaIntegration.isRecording())
        {
            return;
        }

        /* Calculate start and end ticks */
        this.start = (int) this.left.value;
        this.end = this.start + (int) this.right.value;

        if (this.recordingMode == RecordingMode.FIXTURE && this.editor.panel.delegate != null)
        {
            AbstractFixture fixture = this.editor.panel.delegate.fixture;

            this.start = (int) this.editor.getProfile().calculateOffset(fixture);
            this.end = (int) (this.start + fixture.getDuration());
        }
        else if (this.recordingMode == RecordingMode.FULL)
        {
            this.start = 0;
            this.end = (int) this.editor.getProfile().getDuration();
        }

        if (this.end - this.start <= 0)
        {
            return;
        }

        MinemaIntegration.setName(this.getFilename());

        try
        {
            MinemaIntegration.toggleRecording(true);
            this.editor.postOperation(() -> this.recording = true);
        }
        catch (Exception e)
        {
            GuiModal.addFullModal(this, () -> new GuiMessageModal(this.mc, IKey.str(MinemaIntegration.getMessage(e))));

            return;
        }

        if (this.trackingButton.isToggled())
        {
            this.trackingExporter.start(this.editor.getRunner());
        }

        ClientProxy.EVENT_BUS.post(new CameraEditorEvent.Rewind(this.editor, this.start));

        this.editor.timeline.setValueFromScrub(this.start);
        this.editor.updatePlayer(this.start, 0);

        if (!this.isRunning())
        {
            this.editor.togglePlayback();
        }

        this.editor.root.setVisible(false);
    }

    public void stop()
    {
        if (this.recording)
        {
            try
            {
                MinemaIntegration.toggleRecording(false);
            }
            catch (Exception e) {}

            if (this.isRunning())
            {
                this.editor.togglePlayback();
            }

            this.editor.root.setVisible(true);
            this.recording = false;

            if (this.trackingExporter.building)
            {
                this.trackingExporter.exportTrackingData((this.getFilename().isEmpty() ? this.format.format(new Date(System.currentTimeMillis())) : this.getFilename())+ ".json");
            }

            this.trackingExporter.reset();
        }
    }

    /**
     * Update the minema recording logic
     */
    public void minema(int ticks, float partialTicks)
    {
        if (!this.recording)
        {
            return;
        }

        if (!MinemaIntegration.isRecording())
        {
            this.stop();

            GuiModal.addFullModal(this, () -> new GuiMessageModal(this.mc, IKey.lang("aperture.gui.minema.premature_stop")));

            return;
        }
        else if (this.trackingExporter.building && this.isRunning())
        {
            this.trackingExporter.frameEnd(partialTicks);
        }

        if (this.isRunning() && ticks >= this.end)
        {
            this.editor.togglePlayback();
            this.stop();
        }
        else if (!this.isRunning())
        {
            this.stop();
        }

        if (Aperture.debugTicks.get())
        {
            this.font.drawStringWithShadow(String.valueOf(ticks + partialTicks), 0, 0, 0xffffff);
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