package mchorse.aperture.client.gui.panels;

import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.aperture.client.gui.utils.GuiUtils;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.GuiScrollElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTextElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTrackpadElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.utils.Elements;
import mchorse.mclib.client.gui.utils.keys.IKey;
import mchorse.mclib.utils.Direction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

/**
 * Base class for abstract fixture panel
 *
 * This panel adds inputs for only two properties (which has every camera
 * fixture): name and duration.
 */
public abstract class GuiAbstractFixturePanel<T extends AbstractFixture> extends GuiElement implements IFixturePanel<T>
{
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
    public GuiTrackpadElement duration;

    public GuiAbstractFixturePanel(Minecraft mc, GuiCameraEditor editor)
    {
        super(mc);

        this.left = new GuiScrollElement(mc);
        this.left.scroll.opposite = true;
        this.left.flex().relative(this).y(20).w(120).h(1F, -20)
            .column(5).vertical().stretch().scroll().height(20).padding(10);

        this.right = new GuiScrollElement(mc);
        this.right.flex().relative(this).x(1F).y(20).w(120).h(1F, -20).anchorX(1F)
            .column(5).vertical().stretch().scroll().height(20).padding(10);

        this.name = new GuiTextElement(mc, 80, (str) ->
        {
            this.fixture.setName(str);
            this.editor.updateValues();
            this.editor.updateProfile();
        });

        this.duration = new GuiTrackpadElement(mc, (value) ->
        {
            this.updateDuration(value.longValue());
            this.editor.updatePlayerCurrently();
            this.editor.updateProfile();
        });
        this.duration.tooltip(IKey.lang("aperture.gui.panels.duration"));
        this.duration.values(1.0F).limit(1, Float.POSITIVE_INFINITY, true);

        this.left.add(Elements.label(IKey.lang("aperture.gui.panels.name")).background(0x88000000), this.name, this.duration);
        this.add(this.left, this.right);

        this.editor = editor;
    }

    protected void updateDuration(long value)
    {
        this.fixture.setDuration(value);
        this.editor.updateValues();
    }

    public void profileWasUpdated()
    {}

    @Override
    public void select(T fixture, long duration)
    {
        this.fixture = fixture;

        this.name.setText(fixture.getName());
        this.duration.setValue(fixture.getDuration());
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