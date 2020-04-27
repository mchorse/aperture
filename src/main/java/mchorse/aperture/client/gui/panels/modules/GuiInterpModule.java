package mchorse.aperture.client.gui.panels.modules;

import mchorse.aperture.camera.fixtures.PathFixture;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.aperture.client.gui.utils.GuiInterpolationTypeList;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiButtonElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.utils.Elements;
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

/**
 * Path fixture interpolations GUI module
 *
 * This module is responsible for changing angle and/or position interpolation
 * of the path fixture.
 */
public class GuiInterpModule extends GuiAbstractModule
{
    public GuiButtonElement pos;
    public GuiButtonElement angle;
    public GuiInterpolationTypeList interps;

    public PathFixture fixture;
    public boolean pickPos = false;

    public GuiInterpModule(Minecraft mc, GuiCameraEditor editor)
    {
        super(mc, editor);

        this.pos = new GuiButtonElement(mc, IKey.lang(""), (b) ->
        {
            if (this.interps.isVisible())
            {
                this.interps.setVisible(false);
            }
            else
            {
                this.pickPos = true;
                this.interps.setCurrent(this.fixture.interpolationPos);
                this.interps.setVisible(true);

                this.interps.flex().relative(this.pos);
                this.interps.resize();
            }
        });

        this.angle = new GuiButtonElement(mc, IKey.lang(""), (b) ->
        {
            if (this.interps.isVisible())
            {
                this.interps.setVisible(false);
            }
            else
            {
                this.pickPos = false;
                this.interps.setCurrent(this.fixture.interpolationAngle);
                this.interps.setVisible(true);

                this.interps.flex().relative(this.angle);
                this.interps.resize();
            }
        });

        this.interps = new GuiInterpolationTypeList(mc, (interp) ->
        {
            if (this.pickPos)
            {
                this.fixture.interpolationPos = interp.get(0);
                this.pos.label.set(interp.get(0).getKey());
            }
            else
            {
                this.fixture.interpolationAngle = interp.get(0);
                this.angle.label.set(interp.get(0).getKey());
            }

            this.interps.setVisible(false);
            this.editor.updateProfile();
        });

        // this.interps

        this.flex().column(5).vertical().stretch().height(20);
        this.add(Elements.label(IKey.lang("aperture.gui.panels.position")).background(0x88000000), this.pos, Elements.label(IKey.lang("aperture.gui.panels.angle")).background(0x88000000), this.angle);
    }

    public void fill(PathFixture fixture)
    {
        this.fixture = fixture;

        this.interps.setVisible(false);
        this.interps.setCurrent(fixture.interpolationPos);
        this.pos.label.set(this.interps.getCurrentFirst().getKey());
        this.interps.setCurrent(fixture.interpolationAngle);
        this.angle.label.set(this.interps.getCurrentFirst().getKey());
    }
}