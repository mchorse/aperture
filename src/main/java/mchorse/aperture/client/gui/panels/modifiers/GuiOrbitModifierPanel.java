package mchorse.aperture.client.gui.panels.modifiers;

import mchorse.aperture.camera.modifiers.OrbitModifier;
import mchorse.aperture.client.gui.GuiModifiersManager;
import mchorse.aperture.client.gui.utils.GuiUtils;
import mchorse.mclib.client.gui.framework.GuiTooltip;
import mchorse.mclib.client.gui.framework.elements.GuiButtonElement;
import mchorse.mclib.client.gui.framework.elements.GuiTextElement;
import mchorse.mclib.client.gui.framework.elements.GuiTrackpadElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.client.config.GuiCheckBox;

public class GuiOrbitModifierPanel extends GuiAbstractModifierPanel<OrbitModifier>
{
    public GuiTrackpadElement yaw;
    public GuiTrackpadElement pitch;
    public GuiTrackpadElement distance;

    public GuiButtonElement<GuiCheckBox> copy;
    public GuiTextElement selector;

    public GuiOrbitModifierPanel(Minecraft mc, OrbitModifier modifier, GuiModifiersManager modifiers)
    {
        super(mc, modifier, modifiers);

        this.yaw = new GuiTrackpadElement(mc, I18n.format("aperture.gui.panels.yaw"), (value) ->
        {
            this.modifier.yaw = value;
            this.modifiers.editor.updateProfile();
        });

        this.pitch = new GuiTrackpadElement(mc, I18n.format("aperture.gui.panels.pitch"), (value) ->
        {
            this.modifier.pitch = value;
            this.modifiers.editor.updateProfile();
        });

        this.distance = new GuiTrackpadElement(mc, I18n.format("aperture.gui.panels.distance"), (value) ->
        {
            this.modifier.distance = value;
            this.modifiers.editor.updateProfile();
        });

        this.copy = GuiButtonElement.checkbox(mc, I18n.format("aperture.gui.modifiers.panels.copy_entity"), false, (b) ->
        {
            this.modifier.copy = b.button.isChecked();
            this.modifiers.editor.updateProfile();
        });

        this.selector = new GuiTextElement(mc, 500, (str) ->
        {
            this.modifier.selector = str;
            this.modifier.tryFindingEntity();
            this.modifiers.editor.updateProfile();
        });

        this.selector.resizer().parent(this.area).set(5, 25, 0, 20).w(1, -10);
        this.yaw.resizer().parent(this.area).set(5, 50, 0, 20).w(0.5F, -10);
        this.pitch.resizer().parent(this.area).set(5, 50, 0, 20).x(0.5F, 5).w(0.5F, -10);
        this.distance.resizer().parent(this.area).set(5, 75, 0, 20).w(0.5F, -10);
        this.copy.resizer().parent(this.area).set(5, 79, this.copy.button.width, 11).x(0.5F, 5).w(0.5F, -10);

        this.children.add(this.selector, this.yaw, this.pitch, this.distance, this.copy);
    }

    @Override
    public void resize(int width, int height)
    {
        super.resize(width, height);

        this.yaw.setValue(this.modifier.yaw);
        this.pitch.setValue(this.modifier.pitch);
        this.distance.setValue(this.modifier.distance);

        this.copy.button.setIsChecked(this.modifier.copy);
        this.selector.setText(this.modifier.selector);
    }

    @Override
    public int getHeight()
    {
        return 100;
    }

    @Override
    public void draw(GuiTooltip tooltip, int mouseX, int mouseY, float partialTicks)
    {
        super.draw(tooltip, mouseX, mouseY, partialTicks);

        if (!this.selector.field.isFocused())
        {
            GuiUtils.drawRightString(font, I18n.format("aperture.gui.panels.selector"), this.selector.area.x + this.selector.area.w - 4, this.selector.area.y + 5, 0xffaaaaaa);
        }
    }
}