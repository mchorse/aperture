package mchorse.aperture.client.gui;

import mchorse.aperture.ClientProxy;
import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.destination.AbstractDestination;
import mchorse.aperture.camera.destination.ClientDestination;
import mchorse.aperture.camera.destination.ServerDestination;
import mchorse.aperture.client.gui.panels.IGuiModule;
import mchorse.aperture.utils.Rect;
import mchorse.aperture.utils.ScrollArea;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;

/**
 * Camera profile manager GUI
 * 
 * This GUI is responsible managing currently loaded and possible for loading 
 * camera profiles. 
 */
public class GuiProfilesManager implements IGuiModule
{
    private Minecraft mc = Minecraft.getMinecraft();

    public Rect rect = new Rect();
    public ScrollArea scroll = new ScrollArea(16);
    public IProfileListener listener;
    public boolean visible;
    public boolean showLoaded = true;

    public GuiButton quit;
    public GuiButton loaded;
    public GuiButton load;
    public GuiButton add;

    public GuiTextField name;

    public GuiProfilesManager(IProfileListener listener)
    {
        this.listener = listener;

        /* TODO: extract strings */
        this.quit = new GuiButton(0, 0, 0, "X");
        this.loaded = new GuiButton(1, 0, 0, "Loaded");
        this.load = new GuiButton(2, 0, 0, "Load");
        this.add = new GuiButton(3, 0, 0, "New");

        this.name = new GuiTextField(0, this.mc.fontRendererObj, 0, 0, 0, 0);
    }

    public void init()
    {

    }

    public void update(int x, int y, int w, int h)
    {
        this.rect.set(x, y, w, h);
        this.scroll.set(x + 5, y + 30, w - 10, h - 60);
        this.scroll.scrollItemSize = 20;
        this.scroll.setSize(ClientProxy.control.profiles.size());

        int span = (w - 35) / 2;

        this.setSize(this.quit, x + w - 25, y + 5, 20, 20);
        this.setSize(this.loaded, x + 5, y + 5, span, 20);
        this.setSize(this.load, x + 5 + span, y + 5, span, 20);

        this.setSize(this.add, x + w - 45, y + h - 25, 40, 20);
        this.setSize(this.name, x + 5, y + h - 25, w - 55, 20);
        this.updateButtons();
    }

    private void updateButtons()
    {
        this.loaded.enabled = !this.showLoaded;
        this.load.enabled = this.showLoaded;
    }

    private void setSize(GuiButton button, int x, int y, int w, int h)
    {
        button.xPosition = x;
        button.yPosition = y;
        button.width = w;
        button.height = h;
    }

    private void setSize(GuiTextField field, int x, int y, int w, int h)
    {
        field.xPosition = x + 1;
        field.yPosition = y + 1;
        field.width = w - 2;
        field.height = h - 2;
    }

    /**
     * Is mouse pointer inside 
     */
    public boolean isInside(int x, int y)
    {
        return this.visible && this.rect.isInside(x, y);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        if (!this.visible)
        {
            return;
        }

        if (this.quit.mousePressed(mc, mouseX, mouseY))
        {
            this.visible = false;
        }

        if (this.load.mousePressed(mc, mouseX, mouseY) || this.loaded.mousePressed(mc, mouseX, mouseY))
        {
            this.showLoaded = !this.showLoaded;
            this.updateButtons();
        }

        if (this.add.mousePressed(mc, mouseX, mouseY) && !this.name.getText().isEmpty())
        {
            CameraProfile profile = new CameraProfile(new ServerDestination(this.name.getText()));
            ClientProxy.control.addProfile(profile);

            this.listener.selectProfile(profile);

            this.name.setText("");
            this.name.setCursorPositionZero();
        }

        if (this.scroll.isInside(mouseX, mouseY))
        {
            int index = this.scroll.getIndex(mouseX, mouseY);

            if (index >= 0)
            {
                boolean isReverse = mouseX - this.scroll.x >= this.scroll.w - 60;
                boolean isX = mouseX - this.scroll.x >= this.scroll.w - 40;
                boolean isArrow = mouseX - this.scroll.x >= this.scroll.w - 20;

                if (isArrow)
                {
                    this.listener.selectProfile(ClientProxy.control.profiles.get(index));
                }
                else if (isX)
                {
                    ClientProxy.control.profiles.remove(index);
                    ClientProxy.control.currentProfile = null;

                    this.scroll.setSize(ClientProxy.control.profiles.size());
                    this.listener.selectProfile(null);
                }
                else if (isReverse && ClientProxy.control.currentProfile != null)
                {
                    AbstractDestination dest = ClientProxy.control.currentProfile.getDestination();
                    String filename = dest.getFilename();
                    AbstractDestination newDest = dest instanceof ClientDestination ? new ServerDestination(filename) : new ClientDestination(filename);

                    ClientProxy.control.currentProfile.setDestination(newDest);
                }
            }
        }

        this.name.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state)
    {}

    @Override
    public void keyTyped(char typedChar, int keyCode)
    {
        if (!this.visible)
        {
            return;
        }

        this.name.textboxKeyTyped(typedChar, keyCode);
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks)
    {
        if (!this.visible)
        {
            return;
        }

        Gui.drawRect(this.rect.x, this.rect.y, this.rect.x + this.rect.w, this.rect.y + this.rect.h, 0x88000000);
        Gui.drawRect(this.scroll.x, this.scroll.y, this.scroll.x + this.scroll.w, this.scroll.y + this.scroll.h, 0x88000000);

        this.quit.drawButton(mc, mouseX, mouseY);
        this.loaded.drawButton(mc, mouseX, mouseY);
        this.load.drawButton(mc, mouseX, mouseY);
        this.add.drawButton(mc, mouseX, mouseY);

        this.name.drawTextBox();

        this.drawScrollArea(mouseX, mouseY);
    }

    private void drawScrollArea(int mouseX, int mouseY)
    {
        int x = this.scroll.x;
        int y = this.scroll.y - this.scroll.scroll;
        int w = this.scroll.w;

        if (this.showLoaded)
        {
            for (CameraProfile profile : ClientProxy.control.profiles)
            {
                AbstractDestination dest = profile.getDestination();
                boolean hovered = this.scroll.isInside(mouseX, mouseY) && mouseY >= y && mouseY < y + this.scroll.scrollItemSize;

                if (hovered)
                {
                    Gui.drawRect(x, y, x + w, y + this.scroll.scrollItemSize, 0x88000000);
                }

                this.mc.fontRendererObj.drawStringWithShadow(dest.getFilename(), x + 22, y + 7, 0xffffff);
                this.mc.renderEngine.bindTexture(GuiCameraEditor.EDITOR_TEXTURE);

                if (hovered)
                {
                    boolean isArrow = mouseX >= x + w - 20;
                    boolean isX = mouseX >= x + w - 40 && mouseX < x + w - 20;
                    boolean isReverse = mouseX >= x + w - 60 && mouseX < x + w - 40;

                    GlStateManager.color(1, 1, 1, 1);
                    Gui.drawModalRectWithCustomSizedTexture(x + w - 18, y + 2, 80, 32 + (isArrow ? 0 : 16), 16, 16, 256, 256);
                    Gui.drawModalRectWithCustomSizedTexture(x + w - 38, y + 2, 64, 32 + (isX ? 0 : 16), 16, 16, 256, 256);

                    if (dest instanceof ClientDestination)
                    {
                        Gui.drawModalRectWithCustomSizedTexture(x + w - 58, y + 2, 112, 32 + (isReverse ? 0 : 16), 16, 16, 256, 256);
                    }
                    else
                    {
                        Gui.drawModalRectWithCustomSizedTexture(x + w - 58, y + 2, 128, 32 + (isReverse ? 0 : 16), 16, 16, 256, 256);
                    }
                }

                Gui.drawModalRectWithCustomSizedTexture(x + 2, y + 2, 64 + (dest instanceof ClientDestination ? 16 : 0), 64, 16, 16, 256, 256);

                y += this.scroll.scrollItemSize;
            }
        }
        else
        {
            /* TODO: Draw things */
        }
    }

    /**
     * This dude is responsible for listening for an event of camera profile 
     * selection. 
     */
    public static interface IProfileListener
    {
        public void selectProfile(CameraProfile profile);
    }
}