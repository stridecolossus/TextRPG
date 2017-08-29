package org.sarge.textrpg.common;

import org.sarge.textrpg.entity.Stance;

/**
 * Base-class for an action that can only be performed for stances {@link Stance#DEFAULT} or {@link Stance#SNEAKING}.
 * @author Sarge
 * @see #isValidStance(Stance)
 */
public abstract class AbstractActiveAction extends AbstractAction {
    protected AbstractActiveAction() {
        super();
    }

    protected AbstractActiveAction(String name) {
        super(name);
    }

    @Override
    public boolean isValidStance(Stance stance) {
        switch(stance) {
            case DEFAULT:
            case SNEAKING:
                return true;

            default:
                return false;
        }
    }
}
