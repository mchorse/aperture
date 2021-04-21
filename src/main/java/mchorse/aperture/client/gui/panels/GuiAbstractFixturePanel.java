package mchorse.aperture.client.gui.panels;

import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.aperture.client.gui.utils.undo.FixtureValueChangeUndo;
import mchorse.aperture.utils.TimeUtils;
import mchorse.aperture.utils.undo.IUndo;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.GuiScrollElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiColorElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTextElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTrackpadElement;
import mchorse.mclib.client.gui.utils.Elements;
import mchorse.mclib.client.gui.utils.keys.IKey;
import mchorse.mclib.config.values.Value;
import mchorse.mclib.utils.Direction;
import net.minecraft.client.Minecraft;

/**
 * Base class for abstract fixture panel
 *
 * This panel adds inputs for only two properties (which has every camera
 * fixture): name and duration.
 */
public abstract class GuiAbstractFixturePanel<T extends AbstractFixture> extends GuiElement implements IFixturePanel<T>
{
    public static final IKey CATEGORY = IKey.lang("aperture.gui.panels.keys.title");

    /**
     * Currently editing camera fixture
     */
    public T fixture;

    /* Am Stuff */
    public GuiCameraEditor editor;
    public GuiScrollElement left;
    public GuiScrollElement right;

    /* GUI fields */
    public GuiTextElement name;
    public GuiColorElement color;
    private GuiTrackpadElement duration;

    public static IUndo<CameraProfile> undo(GuiCameraEditor editor, Value property, Object newValue)
    {
        return undo(editor, property, property.getValue(), newValue);
    }

    public static IUndo<CameraProfile> undo(GuiCameraEditor editor, Value property, Object oldValue, Object newValue)
    {
        CameraProfile profile = editor.getProfile();
        AbstractFixture fixture = editor.getFixture();
        int index = profile.fixtures.indexOf(fixture);

        return new FixtureValueChangeUndo(index, property.getPath(), oldValue, newValue).view(editor.timeline);
    }

    public GuiAbstractFixturePanel(Minecraft mc, GuiCameraEditor editor)
    {
        super(mc);

        this.editor = editor;

        this.left = new GuiScrollElement(mc);
        this.left.scroll.opposite = true;
        this.left.flex().relative(this).y(20).w(130).hTo(this.area, 1F)
            .column(5).vertical().stretch().scroll().height(20).padding(10);

        this.right = new GuiScrollElement(mc);
        this.right.flex().relative(this).x(1F).y(20).w(130).hTo(this.area, 1F).anchorX(1F)
            .column(5).vertical().stretch().scroll().height(20).padding(10);

        this.name = new GuiTextElement(mc, 80, (str) -> this.editor.postUndo(this.undo(this.fixture.name, str)));
        this.name.tooltip(IKey.lang("aperture.gui.panels.name_tooltip"));

        this.color = new GuiColorElement(mc, (c) -> this.editor.postUndo(this.undo(this.fixture.color, c)));
        this.color.target(this).tooltip(IKey.lang("aperture.gui.panels.color_tooltip"));
        this.color.direction(Direction.RIGHT);

        this.duration = new GuiTrackpadElement(mc, (value) ->
        {
            this.updateDuration(TimeUtils.fromTime(value));
            this.editor.updatePlayerCurrently();
            this.editor.updateValues();
        });
        this.duration.tooltip(IKey.lang("aperture.gui.panels.duration"));

        this.left.add(Elements.label(IKey.lang("aperture.gui.panels.name")).background(), this.name, this.color, this.duration);
        this.add(this.left, this.right);
    }

    protected IUndo<CameraProfile> undo(Value value, Object newValue)
    {
        return undo(this.editor, value, newValue);
    }

    public void handleUndo(IUndo<CameraProfile> undo, boolean redo)
    {
        if (undo instanceof FixtureValueChangeUndo && ((FixtureValueChangeUndo) undo).getName().equals(this.fixture.duration.id))
        {
            this.editor.updateValues();
        }
    }

    public void setDuration(long ticks)
    {
        this.duration.setValue(TimeUtils.toTime(ticks));
    }

    public void updateDurationSettings()
    {
        TimeUtils.configure(this.duration, 1);
        this.setDuration(this.fixture.getDuration());
    }

    protected void updateDuration(long value)
    {
        this.editor.postUndo(this.undo(this.fixture.duration, value));
        this.editor.updateDuration(this.fixture);
    }

    public void profileWasUpdated()
    {}

    public void cameraEditorOpened()
    {
        this.updateDurationSettings();
    }

    @Override
    public void select(T fixture, long duration)
    {
        this.fixture = fixture;

        this.name.setText(fixture.name.get());
        this.color.picker.setColor(fixture.color.get());
        this.updateDurationSettings();
    }

    @Override
    public long currentOffset()
    {
        return this.editor.getProfile().calculateOffset(this.fixture);
    }

    public void editFixture(Position position)
    {
        this.select(this.fixture, -1);
        this.editor.updateProfile();
    }
}