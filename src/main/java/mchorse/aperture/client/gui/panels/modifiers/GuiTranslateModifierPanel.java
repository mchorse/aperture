package mchorse.aperture.client.gui.panels.modifiers;

import mchorse.aperture.camera.modifiers.TranslateModifier;
import mchorse.aperture.client.gui.GuiModifiersManager;
import mchorse.mclib.client.gui.framework.elements.GuiTrackpadElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

public class GuiTranslateModifierPanel extends GuiAbstractModifierPanel<TranslateModifier>
{
    public GuiTrackpadElement x;
    public GuiTrackpadElement y;
    public GuiTrackpadElement z;

    public GuiTranslateModifierPanel(Minecraft mc, TranslateModifier modifier, GuiModifiersManager modifiers)
    {
        super(mc, modifier, modifiers);

        this.x = new GuiTrackpadElement(mc, I18n.format("aperture.gui.panels.x"), (value) ->
        {
            this.modifier.translate.x = value;
            this.modifiers.editor.updateProfile();
        });

        this.y = new GuiTrackpadElement(mc, I18n.format("aperture.gui.panels.y"), (value) ->
        {
            this.modifier.translate.y = value;
            this.modifiers.editor.updateProfile();
        });

        this.z = new GuiTrackpadElement(mc, I18n.format("aperture.gui.panels.z"), (value) ->
        {
            this.modifier.translate.z = value;
            this.modifiers.editor.updateProfile();
        });

        this.x.resizer().parent(this.area).set(5, 25, 0, 20).w(0.5F, -10);
        this.y.resizer().parent(this.area).set(5, 25, 0, 20).x(0.5F, 5).w(0.5F, -10);
        this.z.resizer().parent(this.area).set(5, 50, 0, 20).w(1, -10);

        this.children.add(this.x, this.y, this.z);
    }

    @Override
    public void resize(int width, int height)
    {
        super.resize(width, height);

        this.x.setValue(this.modifier.translate.x);
        this.y.setValue(this.modifier.translate.y);
        this.z.setValue(this.modifier.translate.z);
    }

    @Override
    public int getHeight()
    {
        return 75;
    }
}