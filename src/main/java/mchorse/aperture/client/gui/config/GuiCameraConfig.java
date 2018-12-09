package mchorse.aperture.client.gui.config;

import org.lwjgl.opengl.GL11;

import mchorse.aperture.ClientProxy;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.aperture.events.CameraEditorEvent;
import mchorse.mclib.client.gui.framework.GuiTooltip;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.GuiElements;
import mchorse.mclib.client.gui.utils.GuiUtils;
import mchorse.mclib.client.gui.utils.ScrollArea;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;

public class GuiCameraConfig extends GuiElement
{
    public GuiElements<GuiAbstractConfigOptions> options = new GuiElements<>();
    public GuiCameraEditor editor;

    public ScrollArea scroll = new ScrollArea(0);

    public GuiCameraConfig(Minecraft mc, GuiCameraEditor editor)
    {
        super(mc);

        this.editor = editor;
        this.createChildren();
        this.options.add(new GuiConfigCameraOptions(mc, editor));

        CameraEditorEvent.Options event = new CameraEditorEvent.Options(editor);
        ClientProxy.EVENT_BUS.post(event);

        for (GuiAbstractConfigOptions option : event.options)
        {
            this.options.add(option);
        }
    }

    @Override
    public void resize(int width, int height)
    {
        int max = 0;
        int y = 0;

        for (GuiAbstractConfigOptions option : this.options.elements)
        {
            option.setVisible(option.isActive());

            if (!option.isVisible())
            {
                continue;
            }

            option.update();
            option.resizer().parent(this.area).set(0, y, 0, option.getHeight()).w(1, 0);
            max = Math.max(max, option.getWidth());
            y += option.getHeight();
        }

        super.resize(width, height);

        int x = this.area.x;
        int w = this.area.w;

        this.area.setPos(x + w - max, this.area.y);
        this.area.setSize(max, Math.min(y, this.area.h));

        this.options.resize(width, height);

        this.scroll.scrollSize = y;
        this.scroll.copy(this.area);
        this.scroll.clamp();
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        return super.mouseClicked(mouseX, mouseY, mouseButton) || this.options.mouseClicked(mouseX, mouseY + this.scroll.scroll, mouseButton) || this.scroll.mouseClicked(mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(int mouseX, int mouseY, int scroll)
    {
        return super.mouseScrolled(mouseX, mouseY, scroll) || this.options.mouseScrolled(mouseX, mouseY + this.scroll.scroll, scroll) || this.scroll.mouseScroll(mouseX, mouseY, scroll);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state)
    {
        super.mouseReleased(mouseX, mouseY, state);
        this.options.mouseReleased(mouseX, mouseY + this.scroll.scroll, state);
        this.scroll.mouseReleased(mouseX, mouseY);
    }

    @Override
    public void draw(GuiTooltip tooltip, int mouseX, int mouseY, float partialTicks)
    {
        this.scroll.drag(mouseX, mouseX);

        Gui.drawRect(this.scroll.x, this.scroll.y, this.scroll.getX(1), this.scroll.getY(1), 0xaa000000);

        GuiUtils.scissor(this.scroll.x, this.scroll.y, this.scroll.w, this.scroll.h, this.editor.width, this.editor.height);
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, -this.scroll.scroll, 0);

        this.options.draw(tooltip, mouseX, mouseY + this.scroll.scroll, partialTicks);

        GlStateManager.popMatrix();
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        super.draw(tooltip, mouseX, mouseY, partialTicks);

        this.scroll.drawScrollbar();
    }
}