package mchorse.aperture.utils.undo;

import mchorse.aperture.client.gui.utils.undo.FixtureValueChangeUndo;

import java.util.ArrayList;
import java.util.List;

/**
 * Compound undo
 *
 * This generalized undo element allows to undo/redo multiple undo/redos
 * at a time
 */
public class CompoundUndo <T> implements IUndo<T>
{
    private List<IUndo<T>> undos = new ArrayList<IUndo<T>>();
    private boolean mergable = true;

    public CompoundUndo(IUndo<T>... undos)
    {
        for (IUndo<T> undo : undos)
        {
            if (undo == null)
            {
                continue;
            }

            this.undos.add(undo);
        }
    }

    public List<IUndo<T>> getUndos()
    {
        return this.undos;
    }

    /**
     * Get first undo matching given class
     */
    public IUndo<T> getFirst(Class<? extends IUndo<T>> clazz)
    {
        int i = 0;

        while (i < this.undos.size())
        {
            IUndo<T> undo = this.undos.get(i);

            if (clazz.isAssignableFrom(undo.getClass()))
            {
                return undo;
            }

            i += 1;
        }

        return null;
    }

    /**
     * Get last undo matching given class
     */
    public IUndo<T> getLast(Class<? extends IUndo<T>> clazz)
    {
        int i = this.undos.size() - 1;

        while (i >= 0)
        {
            IUndo<T> undo = this.undos.get(i);

            if (clazz.isAssignableFrom(undo.getClass()))
            {
                return undo;
            }

            i -= 1;
        }

        return null;
    }

    public boolean has(Class<FixtureValueChangeUndo> clazz)
    {
        for (IUndo<T> undo : this.undos)
        {
            if (clazz.isAssignableFrom(undo.getClass()))
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public IUndo<T> noMerging()
    {
        this.mergable = false;

        return this;
    }

    @Override
    public boolean isMergeable(IUndo<T> undo)
    {
        if (this.mergable && undo instanceof CompoundUndo && ((CompoundUndo<T>) undo).undos.size() == this.undos.size())
        {
            CompoundUndo<T> compound = (CompoundUndo<T>) undo;

            for (int i = 0; i < this.undos.size(); i++)
            {
                if (!this.undos.get(i).isMergeable(compound.undos.get(i)))
                {
                    return false;
                }
            }

            return true;
        }

        return false;
    }

    @Override
    public void merge(IUndo<T> undo)
    {
        CompoundUndo<T> theUndo = (CompoundUndo<T>) undo;

        for (int i = 0, c = this.undos.size(); i < c; i++)
        {
            IUndo<T> otherChildUndo = theUndo.undos.get(i);
            IUndo<T> myUndo = this.undos.get(i);

            if (myUndo.isMergeable(otherChildUndo))
            {
                myUndo.merge(otherChildUndo);
            }
        }
    }

    @Override
    public void undo(T context)
    {
        for (int i = this.undos.size() - 1; i >= 0; i--)
        {
            this.undos.get(i).undo(context);
        }
    }

    @Override
    public void redo(T context)
    {
        for (IUndo<T> undo : this.undos)
        {
            undo.redo(context);
        }
    }
}