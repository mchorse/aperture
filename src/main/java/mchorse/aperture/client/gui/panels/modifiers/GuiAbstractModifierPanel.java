package mchorse.aperture.client.gui.panels.modifiers;

import mchorse.aperture.camera.ModifierRegistry;
import mchorse.aperture.camera.modifiers.AbstractModifier;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.aperture.client.gui.GuiModifiersManager;
import mchorse.mclib.client.gui.framework.GuiTooltip;
import mchorse.mclib.client.gui.framework.elements.GuiButtonElement;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.widgets.buttons.GuiTextureButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.resources.I18n;

public abstract class GuiAbstractModifierPanel<T extends AbstractModifier> extends GuiElement
{
    public T modifier;
    public GuiModifiersManager modifiers;

    public String title = "";

    public GuiButtonElement<GuiTextureButton> enable;
    public GuiButtonElement<GuiTextureButton> remove;
    public GuiButtonElement<GuiTextureButton> moveUp;
    public GuiButtonElement<GuiTextureButton> moveDown;
    public GuiButtonElement<GuiTextureButton> copy;

    public GuiAbstractModifierPanel(Minecraft mc, T modifier, GuiModifiersManager modifiers)
    {
        super(mc);

        this.modifier = modifier;
        this.modifiers = modifiers;

        this.createChildren();
        this.enable = GuiButtonElement.icon(mc, GuiCameraEditor.EDITOR_TEXTURE, 0, 0, 0, 0, (b) ->
        {
            this.modifier.enabled = !this.modifier.enabled;
            this.updateEnable();
            this.modifiers.editor.updateProfile();
        });
        this.remove = GuiButtonElement.icon(mc, GuiCameraEditor.EDITOR_TEXTURE, 32, 32, 32, 48, (b) -> this.modifiers.removeModifier(this));
        this.moveUp = GuiButtonElement.icon(mc, GuiCameraEditor.EDITOR_TEXTURE, 96, 32, 96, 48, (b) -> this.modifiers.moveModifier(this, -1));
        this.moveDown = GuiButtonElement.icon(mc, GuiCameraEditor.EDITOR_TEXTURE, 96, 40, 96, 56, (b) -> this.modifiers.moveModifier(this, 1));
        this.copy = GuiButtonElement.icon(mc, GuiCameraEditor.EDITOR_TEXTURE, 144, 32, 144, 48, (b) -> this.modifiers.setClipboard(this.modifier));

        this.remove.resizer().parent(this.area).set(0, 2, 16, 16).x(1, -18);
        this.enable.resizer().relative(this.remove.resizer()).set(-20, 0, 16, 16);
        this.moveUp.resizer().relative(this.enable.resizer()).set(-20, 0, 16, 8);
        this.moveDown.resizer().relative(this.enable.resizer()).set(-20, 8, 16, 8);
        this.copy.resizer().relative(this.moveUp.resizer()).set(-20, 0, 16, 16);

        this.children.add(this.enable, this.remove, this.moveUp, this.moveDown, this.copy);

        this.title = I18n.format(ModifierRegistry.CLIENT.get(modifier.getClass()).title);
    }

    @Override
    public void resize(int width, int height)
    {
        super.resize(width, height);

        this.updateEnable();
    }

    private void updateEnable()
    {
        int x = this.modifier.enabled ? 128 : 112;

        this.enable.button.setTexPos(x, 32).setActiveTexPos(x, 48);
    }

    public int getHeight()
    {
        return 20;
    }

    @Override
    public void draw(GuiTooltip tooltip, int mouseX, int mouseY, float partialTicks)
    {
        int color = 0xaa000000 + ModifierRegistry.CLIENT.get(this.modifier.getClass()).color.getHex();

        Gui.drawRect(this.area.x, this.area.y, this.area.x + this.area.w, this.area.y + 20, color);
        this.font.drawStringWithShadow(this.title, this.area.x + 5, this.area.y + 7, 0xffffff);

        boolean within = this.area.isInside(mouseX, mouseY);

        this.remove.setVisible(within);
        this.enable.setVisible(within);
        this.moveUp.setVisible(within);
        this.moveDown.setVisible(within);
        this.copy.setVisible(within);

        super.draw(tooltip, mouseX, mouseY, partialTicks);
    }
}