package mchorse.aperture.client.gui.panels.modules;

import mchorse.aperture.camera.data.Point;
import mchorse.aperture.client.gui.dashboard.GuiCameraEditor;
import mchorse.mclib.client.gui.framework.elements.input.GuiTrackpadElement;
import mchorse.mclib.client.gui.utils.Elements;
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraft.client.Minecraft;

/**
 * Point GUI module
 *
 * This class unifies three trackpads into one object which edits a {@link Point},
 * and makes it way easier to reuse in other classes.
 */
public class GuiPointModule extends GuiAbstractModule
{
    public GuiTrackpadElement x;
    public GuiTrackpadElement y;
    public GuiTrackpadElement z;

    public Point point;

    public GuiPointModule(Minecraft mc, GuiCameraEditor editor)
    {
        super(mc, editor);

        this.x = new GuiTrackpadElement(mc, (value) ->
        {
            this.point.x = value;
            this.editor.updateProfile();
        });
        this.x.tooltip(IKey.lang("aperture.gui.panels.x"));

        this.y = new GuiTrackpadElement(mc, (value) ->
        {
            this.point.y = value;
            this.editor.updateProfile();
        });
        this.y.tooltip(IKey.lang("aperture.gui.panels.y"));

        this.z = new GuiTrackpadElement(mc, (value) ->
        {
            this.point.z = value;
            this.editor.updateProfile();
        });
        this.z.tooltip(IKey.lang("aperture.gui.panels.z"));

        this.x.values(0.1F);
        this.y.values(0.1F);
        this.z.values(0.1F);

        this.flex().column(5).vertical().stretch().height(20);
        this.add(Elements.label(IKey.lang("aperture.gui.panels.position")).background(0x88000000), this.x, this.y, this.z);
    }

    public void fill(Point point)
    {
        this.point = point;

        this.x.setValue((float) point.x);
        this.y.setValue((float) point.y);
        this.z.setValue((float) point.z);
    }
}