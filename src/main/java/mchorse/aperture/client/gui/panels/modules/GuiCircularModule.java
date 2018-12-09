package mchorse.aperture.client.gui.panels.modules;

import mchorse.aperture.camera.fixtures.CircularFixture;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.mclib.client.gui.framework.elements.GuiTrackpadElement;
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

        this.offset = new GuiTrackpadElement(mc, I18n.format("aperture.gui.panels.offset"), (value) ->
        {
            this.fixture.offset = value;
            this.editor.updateProfile();
        });

        this.pitch = new GuiTrackpadElement(mc, I18n.format("aperture.gui.panels.pitch"), (value) ->
        {
            this.fixture.pitch = value;
            this.editor.updateProfile();
        });

        this.circles = new GuiTrackpadElement(mc, I18n.format("aperture.gui.panels.circles"), (value) ->
        {
            this.fixture.circles = value;
            this.editor.updateProfile();
        });

        this.distance = new GuiTrackpadElement(mc, I18n.format("aperture.gui.panels.distance"), (value) ->
        {
            this.fixture.distance = value;
            this.editor.updateProfile();
        });

        this.offset.resizer().parent(this.area).set(0, 0, 0, 20).w(1, 0);
        this.pitch.resizer().parent(this.area).set(0, 20, 0, 20).w(1, 0);
        this.circles.resizer().parent(this.area).set(0, 40, 0, 20).w(1, 0);
        this.distance.resizer().parent(this.area).set(0, 60, 0, 20).w(1, 0);

        this.children.add(this.offset, this.pitch, this.circles, this.distance);
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