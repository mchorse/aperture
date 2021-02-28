package mchorse.aperture.client.gui.panels.modules;

import mchorse.aperture.camera.data.InterpolationType;
import mchorse.aperture.camera.fixtures.PathFixture;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.aperture.client.gui.panels.GuiAbstractFixturePanel;
import mchorse.aperture.client.gui.panels.GuiPathFixturePanel;
import mchorse.aperture.client.gui.utils.GuiInterpolationTypeList;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiButtonElement;
import mchorse.mclib.client.gui.utils.Elements;
import mchorse.mclib.client.gui.utils.GuiUtils;
import mchorse.mclib.client.gui.utils.keys.IKey;
import mchorse.mclib.utils.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

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

    private GuiPathFixturePanel panel;

    public GuiInterpModule(Minecraft mc, GuiCameraEditor editor, GuiPathFixturePanel panel)
    {
        super(mc, editor);

        this.panel = panel;

        this.pos = new GuiButtonElement(mc, IKey.lang(""), (b) ->
        {
            if (this.interps.hasParent())
            {
                this.interps.removeFromParent();
            }
            else
            {
                this.pickPos = true;
                this.interps.setCurrent(this.fixture.interpolation.get());

                this.getParentContainer().add(this.interps);
                this.interps.flex().relative(this.pos);
                this.interps.resize();
            }
        });

        this.angle = new GuiButtonElement(mc, IKey.lang(""), (b) ->
        {
            if (this.interps.hasParent())
            {
                this.interps.removeFromParent();
            }
            else
            {
                this.pickPos = false;
                this.interps.setCurrent(this.fixture.interpolationAngle.get());

                this.getParentContainer().add(this.interps);
                this.interps.flex().relative(this.angle);
                this.interps.resize();
            }
        });

        this.interps = new GuiInterpolationTypeList(mc, (interp) ->
        {
            if (this.pickPos)
            {
                this.editor.postUndo(GuiAbstractFixturePanel.undo(this.editor, this.fixture.interpolation, interp.get(0)));
                this.pos.label.set(interp.get(0).getKey());
            }
            else
            {
                this.editor.postUndo(GuiAbstractFixturePanel.undo(this.editor, this.fixture.interpolationAngle, interp.get(0)));
                this.angle.label.set(interp.get(0).getKey());
            }

            this.panel.interpolationWasUpdated(this.pickPos);
            this.interps.removeFromParent();
        });
        this.interps.markIgnored();

        this.interps.flex().y(1F).w(1F).h(96);

        this.flex().column(5).vertical().stretch().height(20);
        this.add(Elements.label(IKey.lang("aperture.gui.panels.position")).background(0x88000000), this.pos);
        this.add(Elements.label(IKey.lang("aperture.gui.panels.angle")).background(0x88000000), this.angle);

        this.keys().register(IKey.lang("aperture.gui.panels.keys.path_position"), Keyboard.KEY_P, this::togglePosition).held(Keyboard.KEY_LCONTROL).active(editor::isFlightDisabled).category(GuiAbstractFixturePanel.CATEGORY);
        this.keys().register(IKey.lang("aperture.gui.panels.keys.path_angle"), Keyboard.KEY_A, this::toggleAngle).held(Keyboard.KEY_LCONTROL).active(editor::isFlightDisabled).category(GuiAbstractFixturePanel.CATEGORY);
    }

    private void togglePosition()
    {
        InterpolationType type = this.next(this.fixture.interpolation.get(), this.pos);

        this.editor.postUndo(GuiAbstractFixturePanel.undo(this.editor, this.fixture.interpolation, type));
    }

    private void toggleAngle()
    {
        InterpolationType type = this.next(this.fixture.interpolation.get(), this.pos);

        this.editor.postUndo(GuiAbstractFixturePanel.undo(this.editor, this.fixture.interpolationAngle, type));
    }

    private InterpolationType next(InterpolationType interp, GuiButtonElement button)
    {
        int factor = GuiScreen.isShiftKeyDown() ? -1 : 1;
        int index = MathUtils.cycler(interp.ordinal() + factor, 0, InterpolationType.values().length - 1);

        interp = InterpolationType.values()[index];
        button.label.set(interp.getKey());
        this.interps.setCurrent(interp);
        this.editor.updateProfile();
        GuiUtils.playClick();

        return interp;
    }

    public void fill(PathFixture fixture)
    {
        this.fixture = fixture;

        this.interps.removeFromParent();
        this.interps.setCurrent(fixture.interpolation.get());
        this.pos.label.set(this.interps.getCurrentFirst().getKey());
        this.interps.setCurrent(fixture.interpolationAngle.get());
        this.angle.label.set(this.interps.getCurrentFirst().getKey());
    }
}