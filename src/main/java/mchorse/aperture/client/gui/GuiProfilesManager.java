package mchorse.aperture.client.gui;

import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Consumer;

import mchorse.aperture.ClientProxy;
import mchorse.aperture.camera.CameraAPI;
import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.destination.AbstractDestination;
import mchorse.aperture.camera.destination.ClientDestination;
import mchorse.aperture.camera.destination.ServerDestination;
import mchorse.aperture.network.Dispatcher;
import mchorse.aperture.network.common.PacketRequestCameraProfiles;
import mchorse.mclib.client.gui.framework.GuiTooltip;
import mchorse.mclib.client.gui.framework.GuiTooltip.TooltipDirection;
import mchorse.mclib.client.gui.framework.elements.GuiButtonElement;
import mchorse.mclib.client.gui.framework.elements.GuiDelegateElement;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.IGuiElement;
import mchorse.mclib.client.gui.framework.elements.list.GuiListElement;
import mchorse.mclib.client.gui.framework.elements.modals.GuiConfirmModal;
import mchorse.mclib.client.gui.framework.elements.modals.GuiPromptModal;
import mchorse.mclib.client.gui.widgets.buttons.GuiTextureButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
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
    public GuiCameraEditor editor;

    public GuiCameraProfilesList profiles;
    public GuiButtonElement<GuiTextureButton> add;
    public GuiButtonElement<GuiTextureButton> rename;
    public GuiButtonElement<GuiTextureButton> convert;
    public GuiButtonElement<GuiTextureButton> remove;
    public GuiDelegateElement<IGuiElement> modal;

    private String title = I18n.format("aperture.gui.profiles.title");

    public GuiProfilesManager(Minecraft mc, GuiCameraEditor editor)
    {
        super(mc);

        this.editor = editor;
        this.createChildren();

        this.profiles = new GuiCameraProfilesList(mc, (entry) -> this.pickEntry(entry));
        this.add = GuiButtonElement.icon(mc, GuiCameraEditor.EDITOR_TEXTURE, 224, 0, 224, 16, (b) -> this.add()).tooltip(I18n.format("aperture.gui.profiles.add_tooltip"), TooltipDirection.BOTTOM);
        this.rename = GuiButtonElement.icon(mc, GuiCameraEditor.EDITOR_TEXTURE, 160, 32, 160, 48, (b) -> this.rename()).tooltip(I18n.format("aperture.gui.profiles.rename_tooltip"), TooltipDirection.BOTTOM);
        this.convert = GuiButtonElement.icon(mc, GuiCameraEditor.EDITOR_TEXTURE, 0, 32, 0, 48, (b) -> this.convert()).tooltip(I18n.format("aperture.gui.profiles.convert_tooltip"), TooltipDirection.BOTTOM);
        this.remove = GuiButtonElement.icon(mc, GuiCameraEditor.EDITOR_TEXTURE, 240, 0, 240, 16, (b) -> this.remove()).tooltip(I18n.format("aperture.gui.profiles.remove_tooltip"), TooltipDirection.BOTTOM);
        this.modal = new GuiDelegateElement<IGuiElement>(mc, null);

        this.profiles.resizer().parent(this.area).set(5, 25, 0, 0).w(1, -10).h(1, -35);
        this.remove.resizer().parent(this.area).set(0, 2, 16, 16).x(1, -18);
        this.rename.resizer().relative(this.remove.resizer()).set(-20, 0, 16, 16);
        this.convert.resizer().relative(this.rename.resizer()).set(-20, 0, 16, 16);
        this.add.resizer().relative(this.convert.resizer()).set(-20, 0, 16, 16);
        this.modal.resizer().parent(this.area).set(0, 0, 0, 0).w(1, 0).h(1, 0);

        this.convert.setEnabled(false);
        this.children.add(this.profiles, this.add, this.rename, this.convert, this.remove, this.modal);
    }

    private void add()
    {
        this.modal.setDelegate(new GuiPromptModal(this.mc, this.modal, I18n.format("aperture.gui.profiles.add_modal"), (name) -> this.add(name)));
    }

    private void add(String name)
    {
        if (name.isEmpty())
        {
            return;
        }

        CameraProfile profile = new CameraProfile(AbstractDestination.create(name));
        CameraProfileEntry entry = new CameraProfileEntry(profile.getDestination(), profile);
        ClientProxy.control.addProfile(profile);

        this.editor.selectProfile(profile);
        this.profiles.add(entry);
        this.profiles.setCurrent(entry);
    }

    private void rename()
    {
        GuiPromptModal modal = new GuiPromptModal(this.mc, this.modal, I18n.format("aperture.gui.profiles.rename_modal"), (name) -> this.rename(name));
        modal.setValue(this.profiles.getCurrent().destination.getFilename());

        this.modal.setDelegate(modal);
    }

    private void rename(String name)
    {
        CameraProfileEntry entry = this.profiles.getCurrent();
        AbstractDestination dest = entry.profile.getDestination();

        dest.rename(name);
    }

    private void convert()
    {
        if (this.profiles.current == -1)
        {
            return;
        }

        CameraProfileEntry entry = this.profiles.getCurrent();

        AbstractDestination dest = entry.profile.getDestination();
        String filename = dest.getFilename();
        AbstractDestination newDest = dest instanceof ClientDestination ? new ServerDestination(filename) : new ClientDestination(filename);

        if (!ClientProxy.control.hasSimilar(newDest))
        {
            entry.profile.setDestination(newDest);
            entry.destination = newDest;
        }
    }

    private void remove()
    {
        this.modal.setDelegate(new GuiConfirmModal(this.mc, this.modal, I18n.format("aperture.gui.profiles.remove_modal"), (confirmed) -> this.remove(confirmed)));
    }

    private void remove(boolean confirmed)
    {
        if (confirmed)
        {
            CameraProfileEntry entry = this.profiles.getCurrent();
            ClientProxy.control.profiles.remove(entry.profile);

            /* Reset current camera profile only removed one is was current profile */
            if (this.editor.getProfile() == entry.profile)
            {
                ClientProxy.control.currentProfile = null;
                this.editor.selectProfile(null);
            }

            this.profiles.remove(entry);
            entry.profile.getDestination().remove();
        }
    }

    public void selectProfile(CameraProfile profile)
    {
        this.profiles.setCurrent(profile);
        this.convert.setEnabled(this.profiles.current != -1);
    }

    /**
     * Rename camera profile (callback from the network handlers)
     */
    public void rename(AbstractDestination from, String to)
    {
        CameraProfile profile = ClientProxy.control.getProfile(from);

        if (profile != null)
        {
            profile.getDestination().setFilename(to);
        }
    }

    /**
     * Remove camera profile (callback from the network handlers)
     */
    public void remove(ServerDestination serverDestination)
    {
        CameraProfile profile = ClientProxy.control.getProfile(serverDestination);

        if (profile != null)
        {
            ClientProxy.control.removeProfile(profile);
        }
    }

    private void pickEntry(CameraProfileEntry entry)
    {
        if (entry.profile == null)
        {
            entry.destination.load();
        }
        else
        {
            this.editor.selectProfile(entry.profile);
        }
    }

    public void init()
    {
        this.profiles.clear();

        for (CameraProfile profile : ClientProxy.control.profiles)
        {
            this.profiles.add(this.createEntry(profile.getDestination()));
        }

        for (String filename : CameraAPI.getClientProfiles())
        {
            this.profiles.add(this.createEntry(new ClientDestination(filename)));
        }

        Dispatcher.sendToServer(new PacketRequestCameraProfiles());

        this.profiles.sort();
        this.selectProfile(ClientProxy.control.currentProfile);
    }

    public CameraProfileEntry createEntry(AbstractDestination dest)
    {
        CameraProfile profile = ClientProxy.control.getProfile(dest);

        return new CameraProfileEntry(profile == null ? dest : profile.getDestination(), profile);
    }

    @Override
    public void draw(GuiTooltip tooltip, int mouseX, int mouseY, float partialTicks)
    {
        Gui.drawRect(this.area.x, this.area.y, this.area.getX(1), this.area.getY(1), 0xaa000000);
        Gui.drawRect(this.area.x, this.area.y, this.area.getX(1), this.area.y + 20, 0x88000000);

        this.font.drawStringWithShadow(this.title, this.area.x + 6, this.area.y + 7, 0xffffff);

        super.draw(tooltip, mouseX, mouseY, partialTicks);
    }

    /**
     * Camera profile entry
     * 
     * Stores the destination, but beside that also 
     */
    public static class CameraProfileEntry
    {
        public AbstractDestination destination;
        public CameraProfile profile;

        public CameraProfileEntry(AbstractDestination destination, CameraProfile profile)
        {
            this.destination = destination;
            this.profile = profile;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj instanceof CameraProfileEntry)
            {
                CameraProfileEntry entry = (CameraProfileEntry) obj;

                return this.profile == entry.profile && Objects.equals(entry.destination, this.destination);
            }

            return super.equals(obj);
        }
    }

    /**
     * Camera profile list, all in one 
     */
    public static class GuiCameraProfilesList extends GuiListElement<CameraProfileEntry>
    {
        public GuiCameraProfilesList(Minecraft mc, Consumer<CameraProfileEntry> callback)
        {
            super(mc, callback);
        }

        @Override
        public void sort()
        {
            Collections.sort(this.list, new Comparator<CameraProfileEntry>()
            {
                @Override
                public int compare(CameraProfileEntry o1, CameraProfileEntry o2)
                {
                    return o1.destination.getFilename().compareToIgnoreCase(o2.destination.getFilename());
                }
            });
        }

        @Override
        public void add(CameraProfileEntry element)
        {
            if (element != null && !this.list.contains(element))
            {
                if (element.profile != null)
                {
                    element.destination = element.profile.getDestination();
                }

                super.add(element);
            }
        }

        public boolean setCurrent(CameraProfile profile)
        {
            if (profile == null)
            {
                return false;
            }

            for (CameraProfileEntry entry : this.list)
            {
                if (entry.destination.equals(profile.getDestination()) && entry.profile == null)
                {
                    entry.profile = profile;
                }

                if (entry.profile == profile)
                {
                    this.setCurrent(entry);

                    return true;
                }
            }

            this.current = -1;

            return false;
        }

        @Override
        public void drawElement(CameraProfileEntry element, int i, int x, int y, boolean hover)
        {
            boolean hasProfile = element.profile != null;

            if (this.current == i)
            {
                Gui.drawRect(x, y, x + this.scroll.w, y + this.scroll.scrollItemSize, 0x880088ff);
            }

            GlStateManager.enableAlpha();
            this.mc.renderEngine.bindTexture(GuiCameraEditor.EDITOR_TEXTURE);

            if (hasProfile)
            {
                GlStateManager.color(1, 1, 1, 1);
            }
            else
            {
                GlStateManager.color(0.5F, 0.5F, 0.5F, 1);
            }

            if (element.destination instanceof ClientDestination)
            {
                Gui.drawModalRectWithCustomSizedTexture(x + 2, y + 2, 16, 32, 16, 16, 256, 256);
            }
            else
            {
                Gui.drawModalRectWithCustomSizedTexture(x + 2, y + 2, 0, 32, 16, 16, 256, 256);
            }

            GlStateManager.disableAlpha();

            this.font.drawStringWithShadow(element.destination.getFilename(), x + 4 + 16, y + 6, hasProfile ? (hover ? 16777120 : 0xffffff) : 0x888888);
        }
    }
}