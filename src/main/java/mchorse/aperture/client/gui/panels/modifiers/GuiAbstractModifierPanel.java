package mchorse.aperture.client.gui.panels.modifiers;

import mchorse.aperture.camera.ModifierRegistry;
import mchorse.aperture.camera.modifiers.AbstractModifier;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.aperture.client.gui.GuiModifiersManager;
import mchorse.aperture.client.gui.panels.IGuiModule;
import mchorse.aperture.client.gui.utils.GuiUtils;
import mchorse.aperture.client.gui.widgets.buttons.GuiTextureButton;
import mchorse.aperture.utils.Area;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;

public abstract class GuiAbstractModifierPanel<T extends AbstractModifier> implements IGuiModule
{
    public T modifier;
    public GuiModifiersManager modifiers;
    public FontRenderer font;

    public String title = "";
    public Area area = new Area();

    public GuiButton remove;
    public GuiTextureButton enable;
    public GuiButton moveUp;
    public GuiButton moveDown;

    public GuiAbstractModifierPanel(T modifier, GuiModifiersManager modifiers, FontRenderer font)
    {
        this.modifier = modifier;
        this.modifiers = modifiers;
        this.font = font;

        this.remove = new GuiTextureButton(0, 0, 0, GuiCameraEditor.EDITOR_TEXTURE).setTexPos(32, 32).setActiveTexPos(32, 48);
        this.enable = new GuiTextureButton(1, 0, 0, GuiCameraEditor.EDITOR_TEXTURE);
        this.moveUp = new GuiTextureButton(2, 0, 0, GuiCameraEditor.EDITOR_TEXTURE).setTexPos(96, 32).setActiveTexPos(96, 48);
        this.moveDown = new GuiTextureButton(3, 0, 0, GuiCameraEditor.EDITOR_TEXTURE).setTexPos(96, 40).setActiveTexPos(96, 56);

        this.title = ModifierRegistry.CLIENT.get(modifier.getClass()).title;
    }

    public void update(int x, int y, int w)
    {
        this.area.set(x, y, w, this.getHeight());

        GuiUtils.setSize(this.remove, x + w - 18, y + 2, 16, 16);
        GuiUtils.setSize(this.enable, x + w - 38, y + 2, 16, 16);
        GuiUtils.setSize(this.moveUp, x + w - 58, y + 2, 16, 8);
        GuiUtils.setSize(this.moveDown, x + w - 58, y + 2 + 8, 16, 8);

        this.updateEnable();
    }

    private void updateEnable()
    {
        int x = this.modifier.enabled ? 128 : 112;

        this.enable.setTexPos(x, 32).setActiveTexPos(x, 48);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        Minecraft mc = this.modifiers.editor.mc;

        if (this.enable.mousePressed(mc, mouseX, mouseY))
        {
            this.modifier.enabled = !this.modifier.enabled;
            this.updateEnable();
            this.modifiers.editor.updateProfile();
        }

        if (this.remove.mousePressed(mc, mouseX, mouseY))
        {
            this.modifiers.removeModifier(this);
        }

        if (this.moveUp.mousePressed(mc, mouseX, mouseY))
        {
            this.modifiers.moveModifier(this, -1);
        }

        if (this.moveDown.mousePressed(mc, mouseX, mouseY))
        {
            this.modifiers.moveModifier(this, 1);
        }
    }

    public int getHeight()
    {
        return 20;
    }

    public boolean hasActiveTextfields()
    {
        return false;
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks)
    {
        int h = this.getHeight();
        int color = 0xaa000000 + ModifierRegistry.CLIENT.get(this.modifier.getClass()).color.getHex();

        Gui.drawRect(this.area.x, this.area.y, this.area.x + this.area.w, this.area.y + 20, color);
        this.font.drawStringWithShadow(this.title, this.area.x + 5, this.area.y + 7, 0xffffff);

        if (mouseX >= this.area.x && mouseY >= this.area.y && mouseX <= this.area.x + this.area.w && mouseY <= this.area.y + h)
        {
            this.remove.drawButton(this.modifiers.editor.mc, mouseX, mouseY);
            this.enable.drawButton(this.modifiers.editor.mc, mouseX, mouseY);
            this.moveUp.drawButton(this.modifiers.editor.mc, mouseX, mouseY);
            this.moveDown.drawButton(this.modifiers.editor.mc, mouseX, mouseY);
        }
    }
}