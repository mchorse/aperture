package mchorse.aperture.client.gui;

import mchorse.aperture.Aperture;
import mchorse.aperture.camera.FixtureRegistry;
import mchorse.aperture.camera.FixtureRegistry.FixtureInfo;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiButtonElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraft.client.Minecraft;

import java.util.function.Consumer;

/**
 * Fixtures popup
 * 
 * Allows to select the type of camera fixture the user wants to create.
 */
public class GuiFixtures extends GuiElement
{
    public Consumer<AbstractFixture> callback;

    public GuiFixtures(Minecraft mc, Consumer<AbstractFixture> callback)
    {
        super(mc);

        this.callback = callback;

        this.flex().column(0).vertical().stretch().height(20).padding(2);

        for (FixtureInfo info : FixtureRegistry.CLIENT.values())
        {
            byte type = info.type;
            int color = info.color.getRGBAColor();

            this.add(new GuiButtonElement(mc, IKey.lang(info.title), (b) -> this.actionPerformed(type)).color(color));
        }
    }

    /**
     * Select a fixture
     */
    private void actionPerformed(byte type)
    {
        long duration = Aperture.duration.get();
        AbstractFixture fixture = null;

        try
        {
            fixture = FixtureRegistry.fromType(type, duration);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        if (fixture != null && this.callback != null)
        {
            this.callback.accept(fixture);
        }
    }

    @Override
    public void draw(GuiContext context)
    {
        this.area.draw(0xaa000000);

        super.draw(context);
    }
}