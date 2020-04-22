package mchorse.aperture.client.gui.panels.modules;

import mchorse.aperture.camera.fixtures.CircularFixture;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.mclib.client.gui.framework.elements.input.GuiTrackpadElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

/**
 * Circular GUI module
 *
 * This class unifies four trackpads into one object which edits
 * {@link CircularFixture}'s other properties, and makes it way easier to reuse
 * in other classes.
 */
public class GuiCircularModule extends GuiAbstractModule
{
    public GuiTrackpadElement offset;
    public GuiTrackpadElement pitch;
    public GuiTrackpadElement circles;
    public GuiTrackpadElement distance;

    public CircularFixture fixture;

    public GuiCircularModule(Minecraft mc, GuiCameraEditor editor)
    {
        super(mc, editor);

        this.offset = new GuiTrackpadElement(mc, (value) ->
        {
            this.fixture.offset = value;
            this.editor.updateProfile();
        });
        this.offset.tooltip(I18n.format("aperture.gui.panels.offset"));

        this.pitch = new GuiTrackpadElement(mc, (value) ->
        {
            this.fixture.pitch = value;
            this.editor.updateProfile();
        });
        this.pitch.tooltip(I18n.format("aperture.gui.panels.pitch"));

        this.circles = new GuiTrackpadElement(mc, (value) ->
        {
            this.fixture.circles = value;
            this.editor.updateProfile();
        });
        this.circles.tooltip(I18n.format("aperture.gui.panels.circles"));

        this.distance = new GuiTrackpadElement(mc, (value) ->
        {
            this.fixture.distance = value;
            this.editor.updateProfile();
        });
        this.distance.tooltip(I18n.format("aperture.gui.panels.distance"));

        this.offset.flex().relative(this).set(0, 0, 0, 20).w(1, 0);
        this.pitch.flex().relative(this).set(0, 20, 0, 20).w(1, 0);
        this.circles.flex().relative(this).set(0, 40, 0, 20).w(1, 0);
        this.distance.flex().relative(this).set(0, 60, 0, 20).w(1, 0);

        this.add(this.offset, this.pitch, this.circles, this.distance);
    }

    public void fill(CircularFixture fixture)
    {
        this.fixture = fixture;

        this.offset.setValue(fixture.offset);
        this.pitch.setValue(fixture.pitch);
        this.circles.setValue(fixture.circles);
        this.distance.setValue(fixture.distance);
    }
}