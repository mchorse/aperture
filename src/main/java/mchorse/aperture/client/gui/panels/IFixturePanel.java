package mchorse.aperture.client.gui.panels;

import mchorse.aperture.camera.fixtures.AbstractFixture;
import net.minecraft.client.gui.GuiScreen;

/**
 * Fixture panel interface
 *
 * This dude is responsible for managing panel and editing given abstract
 * fixture.
 */
public interface IFixturePanel<T extends AbstractFixture> extends IGuiModule
{
    /**
     * Select T fixture
     *
     * This method will be responsible for selecting a camera fixture, needed
     * for filling up the fields with needed information.
     */
    public void select(T fixture);

    /**
     * Update this panel
     *
     * This method should be invoked from initGui() method of a
     * {@link GuiScreen}.
     */
    public void update(GuiScreen screen);
}