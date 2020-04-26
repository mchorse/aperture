package mchorse.aperture.client.gui;

import mchorse.aperture.ClientProxy;
import mchorse.aperture.camera.CameraAPI;
import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.destination.AbstractDestination;
import mchorse.aperture.camera.destination.ClientDestination;
import mchorse.aperture.camera.destination.ServerDestination;
import mchorse.aperture.network.Dispatcher;
import mchorse.aperture.network.common.PacketRequestCameraProfiles;
import mchorse.mclib.client.gui.framework.elements.GuiDelegateElement;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiIconElement;
import mchorse.mclib.client.gui.framework.elements.list.GuiListElement;
import mchorse.mclib.client.gui.framework.elements.list.GuiSearchListElement;
import mchorse.mclib.client.gui.framework.elements.modals.GuiConfirmModal;
import mchorse.mclib.client.gui.framework.elements.modals.GuiModal;
import mchorse.mclib.client.gui.framework.elements.modals.GuiPromptModal;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.framework.elements.utils.GuiLabel;
import mchorse.mclib.client.gui.utils.Elements;
import mchorse.mclib.client.gui.utils.Icons;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Camera profile manager GUI
 * 
 * This GUI is responsible managing currently loaded and possible for loading 
 * camera profiles. 
 */
public class GuiProfilesManager extends GuiElement
{
    public GuiCameraEditor editor;

    public GuiCameraProfilesSearchList profiles;
    public GuiIconElement rename;
    public GuiIconElement convert;
    public GuiIconElement add;
    public GuiIconElement dupe;
    public GuiIconElement remove;
    public GuiDelegateElement<GuiModal> modal;

    private String title = I18n.format("aperture.gui.profiles.title");

    public GuiProfilesManager(Minecraft mc, GuiCameraEditor editor)
    {
        super(mc);

        this.editor = editor;

        this.profiles = new GuiCameraProfilesSearchList(mc, (entry) -> this.pickEntry(entry.get(0)));
        this.profiles.label = I18n.format("aperture.gui.search");
        this.rename = new GuiIconElement(mc, Icons.EDIT, (b) -> this.rename());
        this.rename.tooltip(I18n.format("aperture.gui.profiles.rename_tooltip"));
        this.convert = new GuiIconElement(mc, Icons.SERVER, (b) -> this.convert());
        this.convert.tooltip(I18n.format("aperture.gui.profiles.convert_tooltip"));
        this.add = new GuiIconElement(mc, Icons.ADD, (b) -> this.add());
        this.add.tooltip(I18n.format("aperture.gui.profiles.add_tooltip"));
        this.dupe = new GuiIconElement(mc, Icons.DUPE, (b) -> this.dupe());
        this.dupe.tooltip(I18n.format("aperture.gui.profiles.dupe_tooltip"));
        this.remove = new GuiIconElement(mc, Icons.REMOVE, (b) -> this.remove());
        this.remove.tooltip(I18n.format("aperture.gui.profiles.remove_tooltip"));
        this.modal = new GuiDelegateElement<GuiModal>(mc, null);

        this.profiles.flex().relative(this).set(10, 28, 0, 0).w(1, -20).h(1, -38);
        this.remove.flex().relative(this).set(0, 4, 20, 20).x(1, -30);
        this.dupe.flex().relative(this.remove).set(-20, 0, 20, 20);
        this.add.flex().relative(this.dupe).set(-20, 0, 20, 20);
        this.rename.flex().relative(this.add).set(-20, 0, 20, 20);
        this.convert.flex().relative(this.rename).set(-20, 0, 20, 20);
        this.modal.flex().relative(this).set(0, 0, 0, 0).w(1, 0).h(1, 0);

        GuiLabel label = Elements.label(I18n.format("aperture.gui.profiles.title")).background(0x88000000);

        label.flex().relative(this).set(10, 10, 0, 20);

        this.add(label, this.profiles, this.remove, this.dupe, this.add, this.rename, this.convert, this.modal);
    }

    private void add()
    {
        this.modal.setDelegate(new GuiPromptModal(this.mc, I18n.format("aperture.gui.profiles.add_modal"), (name) -> this.add(name)));
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
        this.profiles.filter("", true);
        this.profiles.list.setCurrent(entry);
    }

    private void dupe()
    {
        CameraProfileEntry entry = this.profiles.list.getCurrentFirst();

        if (entry == null)
        {
            return;
        }

        String filename = entry.destination.getFilename();
        GuiPromptModal modal = new GuiPromptModal(this.mc, I18n.format("aperture.gui.profiles.dupe_modal"), (name) ->
        {
            if (!name.equals(filename))
            {
                this.dupe(name);
            }
        });
        modal.setValue(filename);

        this.modal.setDelegate(modal);
    }

    private void dupe(String name)
    {
        CameraProfileEntry entry = this.profiles.list.getCurrentFirst();

        if (entry != null)
        {
            CameraProfile profile = entry.profile.copy();

            profile.getDestination().setFilename(name);
            profile.dirty();

            CameraProfileEntry newEntry = new CameraProfileEntry(profile.getDestination(), profile);

            ClientProxy.control.addProfile(profile);

            this.editor.selectProfile(profile);
            this.profiles.add(newEntry);
            this.profiles.filter("", true);
            this.profiles.list.setCurrent(newEntry);
        }
    }

    private void rename()
    {
        CameraProfileEntry entry = this.profiles.list.getCurrentFirst();

        if (entry == null)
        {
            return;
        }

        GuiPromptModal modal = new GuiPromptModal(this.mc, I18n.format("aperture.gui.profiles.rename_modal"), (name) -> this.rename(name));
        modal.setValue(entry.destination.getFilename());

        this.modal.setDelegate(modal);
    }

    private void rename(String name)
    {
        CameraProfileEntry entry = this.profiles.list.getCurrentFirst();
        AbstractDestination dest = entry.profile.getDestination();

        dest.rename(name);
        this.rename(dest, name);
    }

    private void convert()
    {
        CameraProfileEntry entry = this.profiles.list.getCurrentFirst();

        if (entry != null)
        {
            return;
        }

        AbstractDestination dest = entry.profile.getDestination();
        String filename = dest.getFilename();
        AbstractDestination newDest = dest instanceof ClientDestination ? new ServerDestination(filename) : new ClientDestination(filename);

        if (!ClientProxy.control.hasSimilar(newDest))
        {
            entry.profile.setDestination(newDest);
            entry.destination = newDest;
        }

        this.init();
    }

    private void remove()
    {
        this.modal.setDelegate(new GuiConfirmModal(this.mc, I18n.format("aperture.gui.profiles.remove_modal"), (confirmed) -> this.remove(confirmed)));
    }

    private void remove(boolean confirmed)
    {
        if (confirmed)
        {
            CameraProfileEntry entry = this.profiles.list.getCurrentFirst();

            if (entry == null)
            {
                return;
            }

            ClientProxy.control.profiles.remove(entry.profile);

            /* Reset current camera profile only removed one is was current profile */
            if (this.editor.getProfile() == entry.profile)
            {
                ClientProxy.control.currentProfile = null;
                this.editor.selectProfile(null);
            }

            this.profiles.list.remove(entry);
            this.profiles.filter("", true);
            entry.destination.remove();
        }
    }

    public void selectProfile(CameraProfile profile)
    {
        ((GuiCameraProfilesList) this.profiles.list).setCurrent(profile);
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
        this.profiles.list.clear();

        for (CameraProfile profile : ClientProxy.control.profiles)
        {
            this.profiles.add(this.createEntry(profile.getDestination()));
        }

        if (ClientProxy.server)
        {
            Dispatcher.sendToServer(new PacketRequestCameraProfiles());
        }
        else
        {
            for (String filename : CameraAPI.getClientProfiles())
            {
                this.profiles.add(this.createEntry(new ClientDestination(filename)));
            }

            this.profiles.filter("", true);
            this.selectProfile(ClientProxy.control.currentProfile);
        }
    }

    public CameraProfileEntry createEntry(AbstractDestination dest)
    {
        CameraProfile profile = ClientProxy.control.getProfile(dest);

        return new CameraProfileEntry(profile == null ? dest : profile.getDestination(), profile);
    }

    @Override
    public void draw(GuiContext context)
    {
        Gui.drawRect(this.area.x, this.area.y, this.area.ex(), this.area.ey(), 0xaa000000);

        super.draw(context);
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
        public String toString()
        {
            return this.destination.getFilename();
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
     * Search list of camera profiles 
     */
    public static class GuiCameraProfilesSearchList extends GuiSearchListElement<CameraProfileEntry>
    {
        public GuiCameraProfilesSearchList(Minecraft mc, Consumer<List<CameraProfileEntry>> callback)
        {
            super(mc, callback);
        }

        @Override
        protected GuiListElement<CameraProfileEntry> createList(Minecraft mc, Consumer<List<CameraProfileEntry>> callback)
        {
            return new GuiCameraProfilesList(mc, callback);
        }

        public void add(CameraProfileEntry element)
        {
            if (element != null && !this.list.getList().contains(element))
            {
                if (element.profile != null)
                {
                    element.destination = element.profile.getDestination();
                }

                this.list.add(element);
            }
        }
    }

    /**
     * Camera profile list, all in one 
     */
    public static class GuiCameraProfilesList extends GuiListElement<CameraProfileEntry>
    {
        public GuiCameraProfilesList(Minecraft mc, Consumer<List<CameraProfileEntry>> callback)
        {
            super(mc, callback);
        }

        @Override
        protected boolean sortElements()
        {
            Collections.sort(this.list, (o1, o2) -> o1.destination.getFilename().compareToIgnoreCase(o2.destination.getFilename()));

            return true;
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

            this.current.clear();

            return false;
        }

        @Override
        protected void drawElementPart(CameraProfileEntry element, int i, int x, int y, boolean hover, boolean selected)
        {
            boolean hasProfile = element.profile != null;

            if (hasProfile)
            {
                GlStateManager.color(1, 1, 1, 1);
            }
            else
            {
                GlStateManager.color(0.5F, 0.5F, 0.5F, 1);
            }

            (element.destination instanceof ClientDestination ? Icons.FOLDER : Icons.SERVER).render(x + 2, y + 2);

            this.font.drawStringWithShadow(element.destination.getFilename(), x + 4 + 16, y + 6, hasProfile ? (hover ? 16777120 : 0xffffff) : 0x888888);
        }
    }
}