package mchorse.aperture.client.gui.panels;

import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.camera.fixtures.PathFixture;

/**
 * Fixture panel interface
 *
 * This dude is responsible for managing panel and editing given abstract
 * fixture.
 */
public interface IFixturePanel<T extends AbstractFixture>
{
    /**
     * Select T fixture
     *
     * This method will be responsible for selecting a camera fixture, needed
     * for filling up the fields with needed information.
     */
    public void select(T fixture, long duration);

    /**
     * Return offset for current fixture editing state
     * 
     * Useful mostly for composite camera fixtures like {@link PathFixture}.
     */
    public long currentOffset();
}