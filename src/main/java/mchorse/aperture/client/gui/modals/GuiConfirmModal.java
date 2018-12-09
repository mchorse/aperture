package mchorse.aperture.client.gui.modals;

import java.util.function.Consumer;

import mchorse.mclib.client.gui.framework.GuiTooltip;
import mchorse.mclib.client.gui.framework.elements.GuiButtonElement;
import mchorse.mclib.client.gui.framework.elements.GuiDelegateElement;
import mchorse.mclib.client.gui.framework.elements.IGuiElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;

public class GuiConfirmModal extends GuiModal
{
    public String label;

    private GuiButtonElement<GuiButton> confirm;
    private GuiButtonElement<GuiButton> cancel;

    public Consumer<Boolean> callback;

    public GuiConfirmModal(Minecraft mc, GuiDelegateElement<IGuiElement> parent, String label, Consumer<Boolean> callback)
    {
        super(mc, parent);

        this.parent = parent;
        this.label = label;
        this.callback = callback;

        this.confirm = GuiButtonElement.button(mc, I18n.format("aperture.gui.ok"), (b) -> this.close(true));
        this.confirm.resizer().parent(this.area).set(10, 0, 0, 20).y(1, -30).w(0.5F, -15);

        this.cancel = GuiButtonElement.button(mc, I18n.format("aperture.gui.cancel"), (b) -> this.close(false));
        this.cancel.resizer().parent(this.area).set(10, 0, 0, 20).x(0.5F, 5).y(1, -30).w(0.5F, -15);

        this.children.add(this.confirm, this.cancel);
    }

    public void close(boolean confirmed)
    {
        if (this.callback != null)
        {
            this.callback.accept(confirmed);
        }

        this.parent.setDelegate(null);
    }

    @Override
    public void draw(GuiTooltip tooltip, int mouseX, int mouseY, float partialTicks)
    {
        super.draw(tooltip, mouseX, mouseY, partialTicks);

        this.font.drawSplitString(this.label, this.area.x + 10, this.area.y + 10, this.area.w - 20, 0xffffff);
    }
}
