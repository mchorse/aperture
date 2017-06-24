package mchorse.aperture.client.gui;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import mchorse.aperture.ClientProxy;
import mchorse.aperture.camera.CameraControl;
import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.destination.AbstractDestination;
import mchorse.aperture.camera.destination.ClientDestination;
import mchorse.aperture.camera.destination.ServerDestination;
import mchorse.aperture.client.gui.panels.IGuiModule;
import mchorse.aperture.client.gui.utils.GuiUtils;
import mchorse.aperture.network.Dispatcher;
import mchorse.aperture.network.common.PacketRequestCameraProfiles;
import mchorse.aperture.utils.Area;
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

    public Area rect = new Area();
    public ScrollArea scrollLoaded = new ScrollArea(20);
    public ScrollArea scrollLoad = new ScrollArea(20);
    public GuiCameraEditor editor;
    public boolean visible;
    public boolean showLoaded = true;
    public List<AbstractDestination> destToLoad = new ArrayList<AbstractDestination>();

    public GuiButton loaded;
    public GuiButton load;
    public GuiButton add;

    public GuiTextField name;

    public GuiProfilesManager(GuiCameraEditor editor)
    {
        this.editor = editor;

        /* TODO: extract strings */
        this.loaded = new GuiButton(1, 0, 0, "Loaded");
        this.load = new GuiButton(2, 0, 0, "Load");
        this.add = new GuiButton(3, 0, 0, "New");

        this.name = new GuiTextField(0, this.mc.fontRendererObj, 0, 0, 0, 0);
    }

    public void init()
    {
        this.destToLoad.clear();

        for (File file : ClientProxy.getClientCameras().listFiles(new JSONFileFilter()))
        {
            String filename = file.getName();

            filename = filename.substring(0, filename.lastIndexOf(".json"));

            this.destToLoad.add(new ClientDestination(filename));
        }

        Dispatcher.sendToServer(new PacketRequestCameraProfiles());
    }

    public void update(int x, int y, int w, int h)
    {
        this.rect.set(x, y, w, h);
        this.scrollLoad.set(x + 5, y + 30, w - 10, h - 60);
        this.scrollLoaded.set(x + 5, y + 30, w - 10, h - 60);
        this.scrollLoaded.setSize(ClientProxy.control.profiles.size());

        int span = (w - 12) / 2;

        this.setSize(this.loaded, x + 5, y + 5, span, 20);
        this.setSize(this.load, x + span + 7, y + 5, span, 20);

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

        if (this.load.mousePressed(mc, mouseX, mouseY) || this.loaded.mousePressed(mc, mouseX, mouseY))
        {
            this.showLoaded = !this.showLoaded;
            this.updateButtons();
        }

        if (this.add.mousePressed(mc, mouseX, mouseY) && !this.name.getText().isEmpty())
        {
            CameraProfile profile = new CameraProfile(new ServerDestination(this.name.getText()));
            ClientProxy.control.addProfile(profile);

            this.editor.selectProfile(profile);

            this.name.setText("");
            this.name.setCursorPositionZero();
        }

        if (this.scrollLoaded.isInside(mouseX, mouseY))
        {
            if (this.showLoaded)
            {
                int index = this.scrollLoaded.getIndex(mouseX, mouseY);

                if (index >= 0)
                {
                    boolean isReverse = mouseX - this.scrollLoaded.x >= this.scrollLoaded.w - 40;
                    boolean isX = mouseX - this.scrollLoaded.x >= this.scrollLoaded.w - 20;

                    if (isX)
                    {
                        ClientProxy.control.profiles.remove(index);
                        ClientProxy.control.currentProfile = null;

                        this.scrollLoaded.setSize(ClientProxy.control.profiles.size());
                        this.editor.selectProfile(null);
                    }
                    else if (isReverse)
                    {
                        CameraProfile profile = ClientProxy.control.profiles.get(index);

                        AbstractDestination dest = profile.getDestination();
                        String filename = dest.getFilename();
                        AbstractDestination newDest = dest instanceof ClientDestination ? new ServerDestination(filename) : new ClientDestination(filename);

                        if (!ClientProxy.control.hasSimilar(newDest))
                        {
                            profile.setDestination(newDest);
                        }
                    }
                    else
                    {
                        if (index >= 0 && index < ClientProxy.control.profiles.size())
                        {
                            this.editor.selectProfile(ClientProxy.control.profiles.get(index));
                        }
                    }
                }
            }
            else
            {
                int index = this.scrollLoad.getIndex(mouseX, mouseY);

                if (index >= 0 && index < this.destToLoad.size())
                {
                    this.destToLoad.get(index).reload();
                }
            }
        }

        this.name.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public void mouseScroll(int mouseX, int mouseY, int scroll)
    {
        ScrollArea area = this.showLoaded ? this.scrollLoaded : this.scrollLoad;

        if (area.isInside(mouseX, mouseY))
        {
            area.scrollBy(scroll);
        }
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

        Gui.drawRect(this.rect.x, this.rect.y, this.rect.x + this.rect.w, this.rect.y + this.rect.h, 0xaa000000);
        Gui.drawRect(this.scrollLoaded.x, this.scrollLoaded.y, this.scrollLoaded.x + this.scrollLoaded.w, this.scrollLoaded.y + this.scrollLoaded.h, 0x88000000);

        this.loaded.drawButton(mc, mouseX, mouseY);
        this.load.drawButton(mc, mouseX, mouseY);
        this.add.drawButton(mc, mouseX, mouseY);

        this.name.drawTextBox();

        GuiUtils.scissor(this.scrollLoaded.x, this.scrollLoaded.y, this.scrollLoaded.w, this.scrollLoaded.h, this.editor.width, this.editor.height);

        this.drawScrollArea(mouseX, mouseY);

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    private void drawScrollArea(int mouseX, int mouseY)
    {
        CameraControl control = ClientProxy.control;

        if (this.showLoaded)
        {
            this.scrollLoaded.setSize(control.profiles.size());

            int x = this.scrollLoaded.x;
            int y = this.scrollLoaded.y - this.scrollLoaded.scroll;
            int w = this.scrollLoaded.w;

            for (CameraProfile profile : control.profiles)
            {
                AbstractDestination dest = profile.getDestination();
                boolean hovered = this.scrollLoaded.isInside(mouseX, mouseY) && mouseY >= y && mouseY < y + this.scrollLoaded.scrollItemSize;
                boolean current = control.currentProfile != null ? dest.equals(control.currentProfile.getDestination()) : false;

                if (hovered || current)
                {
                    Gui.drawRect(x, y, x + w, y + this.scrollLoaded.scrollItemSize, current ? 0x880088ff : 0x88000000);
                }

                this.mc.fontRendererObj.drawStringWithShadow(dest.getFilename(), x + 22, y + 7, 0xffffff);
                this.mc.renderEngine.bindTexture(GuiCameraEditor.EDITOR_TEXTURE);

                if (hovered)
                {
                    boolean isX = mouseX >= x + w - 20;
                    boolean isReverse = mouseX >= x + w - 40 && mouseX < x + w - 20;

                    GlStateManager.color(1, 1, 1, 1);
                    Gui.drawModalRectWithCustomSizedTexture(x + w - 18, y + 2, 32, 32 + (isX ? 0 : 16), 16, 16, 256, 256);

                    if (dest instanceof ClientDestination)
                    {
                        Gui.drawModalRectWithCustomSizedTexture(x + w - 38, y + 2, 0, 32 + (isReverse ? 0 : 16), 16, 16, 256, 256);
                    }
                    else
                    {
                        Gui.drawModalRectWithCustomSizedTexture(x + w - 38, y + 2, 16, 32 + (isReverse ? 0 : 16), 16, 16, 256, 256);
                    }
                }

                Gui.drawModalRectWithCustomSizedTexture(x + 2, y + 2, 0 + (dest instanceof ClientDestination ? 16 : 0), 32, 16, 16, 256, 256);

                y += this.scrollLoaded.scrollItemSize;
            }
        }
        else
        {
            int x = this.scrollLoad.x;
            int y = this.scrollLoad.y - this.scrollLoad.scroll;
            int w = this.scrollLoad.w;

            for (AbstractDestination dest : this.destToLoad)
            {
                boolean hovered = this.scrollLoad.isInside(mouseX, mouseY) && mouseY >= y && mouseY < y + this.scrollLoad.scrollItemSize;
                boolean current = control.currentProfile != null ? dest.equals(control.currentProfile.getDestination()) : false;

                if (hovered || current)
                {
                    Gui.drawRect(x, y, x + w, y + this.scrollLoaded.scrollItemSize, current ? 0x880088ff : 0x88000000);
                }

                this.mc.fontRendererObj.drawStringWithShadow(dest.getFilename(), x + 22, y + 7, 0xffffff);
                this.mc.renderEngine.bindTexture(GuiCameraEditor.EDITOR_TEXTURE);

                GlStateManager.color(1, 1, 1, 1);

                if (hovered)
                {
                    Gui.drawModalRectWithCustomSizedTexture(x + w - 18, y + 2, 48, 32, 16, 16, 256, 256);
                }

                Gui.drawModalRectWithCustomSizedTexture(x + 2, y + 2, 0 + (dest instanceof ClientDestination ? 16 : 0), 32, 16, 16, 256, 256);

                y += this.scrollLoad.scrollItemSize;
            }
        }

        ScrollArea area = this.showLoaded ? this.scrollLoaded : this.scrollLoad;

        if (area.scrollSize > area.h)
        {
            int mh = area.h - 4;
            int x = area.x + area.w - 4;
            int h = (int) (((area.scrollSize - area.h) / (float) area.h) * mh);
            int y = area.y + (int) (area.scroll / (float) (area.scrollSize - area.h) * (mh - h)) + 2;

            Gui.drawRect(x, y, x + 2, y + h, 0x88ffffff);
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

    /**
     * JSON file filter 
     */
    public static class JSONFileFilter implements FileFilter
    {
        @Override
        public boolean accept(File file)
        {
            return file.isFile() && file.getName().endsWith(".json");
        }
    }
}