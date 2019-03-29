package de.heinzen.probplugin.pacman;

import java.util.ArrayList;
import java.util.List;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.prob.animator.domainobjects.EvalResult;
import de.prob.animator.domainobjects.IEvalElement;
import de.prob.exception.ProBError;
import de.prob.statespace.Trace;
import de.prob.translator.types.BObject;
import de.prob.translator.types.BigInteger;
import de.prob.translator.types.Number;
import de.prob.translator.types.Set;
import de.prob.translator.types.Tuple;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Christoph Heinzen on 15.08.17.
 */
public class PacmanAnimator {
    public PacmanAnimator() {}

    public EvalResult getValueOfVariable(Trace trace, IEvalElement formula) {
        return (EvalResult)trace.getCurrentState().getValues().get(formula);
    }

    public BigInteger getIntVariable(Trace trace, IEvalElement formula) {
        try {
            return (BigInteger) getValueOfVariable(trace, formula).translate().getValue();
        } catch (BCompoundException e) {
            throw new ProBError(e);
        }
    }

    public Set getSetVariable(Trace trace, IEvalElement formula){
        try {
            return (Set) getValueOfVariable(trace, formula).translate().getValue();
        } catch (BCompoundException e) {
            throw new ProBError(e);
        }
    }

    public Position getPosition(Trace trace, IEvalElement formula) {
        EvalResult result = getValueOfVariable(trace, formula);
        return resultToPosition(result);
    }

    @NotNull
    private Position resultToPosition(EvalResult result) {
        Tuple value;
        try {
            value = (Tuple) result.translate().getValue();
        } catch (BCompoundException e) {
            throw new ProBError(e);
        }
        return new Position(value);
    }

    public List<Position> getPositions(Trace trace, IEvalElement formula) {
        Set translatedSet = getSetVariable(trace, formula);

        List<Position> positions = new ArrayList<>(translatedSet.size());
        for (BObject tuple : translatedSet) {
            Tuple object = (Tuple) tuple;
            positions.add(new Position(((BigInteger)object.get(0)).intValue(), ((BigInteger)object.get(1)).intValue()));
        }
        return positions;
    }

    public boolean checkPosition(Trace trace, Position pos) {
        return checkPosition(trace, pos.getX(), pos.getY());
    }

    public boolean checkPosition(Trace trace, int x, int y) {
        return getSetVariable(trace, PacmanFormulas.BEGEHBAR).contains(new Tuple(Number.build(x), Number.build(y)));
    }

}
