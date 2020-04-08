package mchorse.aperture.client.gui.panels;

import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.aperture.client.gui.utils.GuiUtils;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTextElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTrackpadElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
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

    /* Stuff */
    public GuiCameraEditor editor;

    /* GUI fields */
    public GuiTextElement name;
    public GuiTrackpadElement duration;

    public GuiAbstractFixturePanel(Minecraft mc, GuiCameraEditor editor)
    {
        super(mc);

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
        this.duration.tooltip(I18n.format("aperture.gui.panels.duration"), Direction.BOTTOM);
        this.duration.values(1.0F).limit(1, Float.POSITIVE_INFINITY, true);

        this.name.flex().relative(this.area).set(0, 10, 100, 20);
        this.duration.flex().relative(this.area).set(0, 35, 100, 20);

        this.add(this.name, this.duration);

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

    @Override
    public void draw(GuiContext context)
    {
        super.draw(context);

        if (!this.name.field.isFocused())
        {
            GuiUtils.drawRightString(this.font, I18n.format("aperture.gui.panels.name"), this.name.area.x + this.name.area.w - 4, this.name.area.y + 6, 0xffaaaaaa);
        }
    }
}