package mchorse.aperture.client.gui.utils.undo;

import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.mclib.utils.undo.IUndo;

public class FixtureAddRemoveUndo extends CameraProfileUndo
{
    private int index;
    private AbstractFixture fixture;
    private AbstractFixture lastFixture;

    public static IUndo<CameraProfile> create(GuiCameraEditor editor, CameraProfile profile, int index, AbstractFixture fixture)
    {
        return new FixtureAddRemoveUndo(index, fixture, profile.get(index)).view(editor.timeline);
    }

    public FixtureAddRemoveUndo(int index, AbstractFixture fixture, AbstractFixture lastFixture)
    {
        this.index = index;
        this.fixture = fixture;
        this.lastFixture = lastFixture;
    }

    public int getIndex()
    {
        return this.index;
    }

    public int getTargetIndex(boolean redo)
    {
        if (!redo)
        {
            return this.isRemove() ? this.index : this.index - 1;
        }

        return this.index;
    }

    public boolean isRemove()
    {
        return this.fixture == null;
    }

    @Override
    public IUndo<CameraProfile> noMerging()
    {
        return this;
    }

    @Override
    public boolean isMergeable(IUndo<CameraProfile> undo)
    {
        return false;
    }

    @Override
    public void merge(IUndo<CameraProfile> undo)
    {}

    @Override
    public void undo(CameraProfile context)
    {
        if (this.isRemove())
        {
            context.add(this.lastFixture.copy(), this.index);
        }
        else
        {
            context.remove(this.index);
        }
    }

    @Override
    public void redo(CameraProfile context)
    {
        if (this.isRemove())
        {
            context.remove(this.index);
        }
        else
        {
            context.add(this.fixture.copy(), this.index);
        }
    }
}