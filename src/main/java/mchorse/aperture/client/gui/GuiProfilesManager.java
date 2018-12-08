package mchorse.aperture.client.gui;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import mchorse.aperture.ClientProxy;
import mchorse.aperture.camera.CameraAPI;
import mchorse.aperture.camera.CameraControl;
import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.destination.AbstractDestination;
import mchorse.aperture.camera.destination.ClientDestination;
import mchorse.aperture.camera.destination.ServerDestination;
import mchorse.aperture.network.Dispatcher;
import mchorse.aperture.network.common.PacketRequestCameraProfiles;
import mchorse.mclib.client.gui.framework.GuiTooltip;
import mchorse.mclib.client.gui.framework.elements.GuiButtonElement;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.GuiTextElement;
import mchorse.mclib.client.gui.utils.GuiUtils;
import mchorse.mclib.client.gui.utils.ScrollArea;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;

/**
 * Camera profile manager GUI
 * 
 * This GUI is responsible managing currently loaded and possible for loading 
 * camera profiles. 
 */
public class GuiProfilesManager extends GuiElement
{
    public ScrollArea scrollLoaded = new ScrollArea(20);
    public ScrollArea scrollLoad = new ScrollArea(20);

    public GuiCameraEditor editor;
    public boolean showLoaded = true;
    public boolean rename = false;
    public boolean error = false;
    public List<AbstractDestination> destToLoad = new ArrayList<AbstractDestination>();

    public GuiButtonElement<GuiButton> loaded;
    public GuiButtonElement<GuiButton> load;
    public GuiButtonElement<GuiButton> add;

    public GuiTextElement name;

    public GuiProfilesManager(Minecraft mc, GuiCameraEditor editor)
    {
        super(mc);

        this.editor = editor;
        this.createChildren();

        this.loaded = GuiButtonElement.button(mc, I18n.format("aperture.gui.profiles.loaded"), (b) ->
        {
            this.showLoaded = !this.showLoaded;
            this.updateButtons();
        });

        this.load = GuiButtonElement.button(mc, I18n.format("aperture.gui.profiles.load"), (b) ->
        {
            this.showLoaded = !this.showLoaded;
            this.updateButtons();
        });

        this.add = GuiButtonElement.button(mc, I18n.format("aperture.gui.profiles.new"), (b) ->
        {
            this.createCameraProfile(this.name.field.getText());
        });

        this.name = new GuiTextElement(mc, 80, null);

        this.loaded.resizer().parent(this.area).set(5, 5, 0, 20).w(0.5F, -6);
        this.load.resizer().parent(this.area).set(0, 5, 0, 20).w(0.5F, -6).x(0.5F, 1);
        this.add.resizer().parent(this.area).set(0, 0, 40, 20).x(1, -45).y(1, -25);
        this.name.resizer().parent(this.area).set(5, 0, 0, 20).y(1, -25).w(1, -55);

        this.loaded.setEnabled(false);
        this.children.add(this.loaded, this.load, this.add, this.name);
    }

    public void init()
    {
        this.destToLoad.clear();
        this.rename = false;

        for (String filename : CameraAPI.getClientProfiles())
        {
            this.destToLoad.add(new ClientDestination(filename));
        }

        Dispatcher.sendToServer(new PacketRequestCameraProfiles());
    }

    @Override
    public void resize(int width, int height)
    {
        super.resize(width, height);

        this.scrollLoad.set(this.area.x + 5, this.area.y + 30, this.area.w - 10, this.area.h - 60);
        this.scrollLoaded.set(this.area.x + 5, this.area.y + 30, this.area.w - 10, this.area.h - 60);
        this.scrollLoaded.setSize(ClientProxy.control.profiles.size());
    }

    private void updateButtons()
    {
        this.loaded.setEnabled(!this.showLoaded);
        this.load.setEnabled(this.showLoaded);

        this.add.button.displayString = this.rename ? I18n.format("aperture.gui.profiles.rename") : I18n.format("aperture.gui.profiles.new");
        this.name.field.setTextColor(0xffffff);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        if (super.mouseClicked(mouseX, mouseY, mouseButton))
        {
            return true;
        }

        if (this.scrollLoaded.isInside(mouseX, mouseY))
        {
            if (this.showLoaded)
            {
                if (!this.rename)
                {
                    int index = this.scrollLoaded.getIndex(mouseX, mouseY);

                    if (index >= 0 && index < ClientProxy.control.profiles.size())
                    {
                        boolean isRename = mouseX - this.scrollLoaded.x >= this.scrollLoaded.w - 60;
                        boolean isReverse = mouseX - this.scrollLoaded.x >= this.scrollLoaded.w - 40;
                        boolean isX = mouseX - this.scrollLoaded.x >= this.scrollLoaded.w - 20;

                        if (isX)
                        {
                            /* Reset current camera profile only removed one is was current profile */
                            if (this.editor.getProfile() == ClientProxy.control.profiles.remove(index))
                            {
                                ClientProxy.control.currentProfile = null;
                                this.editor.selectProfile(null);
                            }

                            this.scrollLoaded.setSize(ClientProxy.control.profiles.size());
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
                        else if (isRename)
                        {
                            CameraProfile profile = ClientProxy.control.profiles.get(index);

                            this.rename = true;
                            this.updateButtons();

                            this.name.setText(profile.getDestination().getFilename());
                            this.editor.selectProfile(ClientProxy.control.profiles.get(index));
                        }
                        else
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
                    this.destToLoad.get(index).load();
                }
            }
        }

        return false;
    }

    private void createCameraProfile(String text)
    {
        if (text.isEmpty())
        {
            return;
        }

        if (this.rename)
        {
            if (this.error)
            {
                return;
            }

            AbstractDestination dest = this.editor.getProfile().getDestination();

            dest.rename(text);
            dest.setFilename(text);

            this.rename = false;
            this.updateButtons();
        }
        else
        {
            CameraProfile profile = new CameraProfile(AbstractDestination.create(text));
            ClientProxy.control.addProfile(profile);

            this.editor.selectProfile(profile);
        }

        this.name.setText("");
    }

    @Override
    public boolean mouseScrolled(int mouseX, int mouseY, int scroll)
    {
        if (super.mouseScrolled(mouseX, mouseY, scroll))
        {
            return true;
        }

        ScrollArea area = this.showLoaded ? this.scrollLoaded : this.scrollLoad;

        if (area.isInside(mouseX, mouseY))
        {
            area.scrollBy(scroll);

            return true;
        }

        return false;
    }

    @Override
    public void keyTyped(char typedChar, int keyCode)
    {
        if (keyCode == Keyboard.KEY_RETURN)
        {
            this.createCameraProfile(this.name.field.getText());

            return;
        }

        super.keyTyped(typedChar, keyCode);

        /* Canceling renaming */
        if (this.rename)
        {
            if (keyCode == Keyboard.KEY_ESCAPE)
            {
                this.rename = false;
                this.updateButtons();
                this.name.setText("");
            }
            else
            {
                this.updateRename();
            }
        }
    }

    private void updateRename()
    {
        this.name.field.setTextColor(0xffffff);
        this.error = false;

        CameraProfile profile = this.editor.getProfile();

        if (profile != null)
        {
            String name = this.name.field.getText();
            AbstractDestination profileDest = profile.getDestination();

            for (AbstractDestination dest : this.destToLoad)
            {
                if (dest.getFilename().equals(name) && !dest.equals(profileDest))
                {
                    this.name.field.setTextColor(0xff2244);
                    this.error = true;

                    break;
                }
            }
        }
    }

    @Override
    public void draw(GuiTooltip tooltip, int mouseX, int mouseY, float partialTicks)
    {
        Gui.drawRect(this.area.x, this.area.y, this.area.x + this.area.w, this.area.y + this.area.h, 0xaa000000);
        Gui.drawRect(this.scrollLoaded.x, this.scrollLoaded.y, this.scrollLoaded.x + this.scrollLoaded.w, this.scrollLoaded.y + this.scrollLoaded.h, 0x88000000);

        super.draw(tooltip, mouseX, mouseY, partialTicks);

        if (!this.name.field.isFocused() && this.name.field.getText().isEmpty())
        {
            this.font.drawStringWithShadow(this.rename ? I18n.format("aperture.gui.profiles.rename_profile") : I18n.format("aperture.gui.profiles.tooltip"), this.name.area.x + 4, this.name.area.y + 6, 0xaaaaaa);
        }

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
                    boolean isRename = mouseX >= x + w - 60 && mouseX < x + w - 40;

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

                    Gui.drawModalRectWithCustomSizedTexture(x + w - 58, y + 2, 160, 32 + (isRename ? 0 : 16), 16, 16, 256, 256);
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
     * Rename camera profile 
     */
    public void rename(AbstractDestination from, String to)
    {
        CameraProfile profile = ClientProxy.control.getProfile(from);

        if (profile != null)
        {
            profile.getDestination().setFilename(to);
        }

        for (AbstractDestination dest : this.destToLoad)
        {
            if (dest.equals(from))
            {
                dest.setFilename(to);
            }
        }
    }
}