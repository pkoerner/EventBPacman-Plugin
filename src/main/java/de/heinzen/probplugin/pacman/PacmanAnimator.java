package de.heinzen.probplugin.pacman;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.prob.animator.domainobjects.EvalResult;
import de.prob.animator.domainobjects.EventB;
import de.prob.animator.domainobjects.FormulaExpand;
import de.prob.exception.ProBError;
import de.prob.statespace.Trace;
import de.prob.translator.types.BObject;
import de.prob.translator.types.BigInteger;
import de.prob.translator.types.Set;
import de.prob.translator.types.Tuple;

/**
 * Created by Christoph Heinzen on 15.08.17.
 */
public class PacmanAnimator {
    public PacmanAnimator() {}

    public Position getPosition(Trace trace, String eventbFormula) {
        EvalResult result = (EvalResult) trace.evalCurrent(eventbFormula, FormulaExpand.EXPAND);
        Tuple value;
        try {
            value = (Tuple) result.translate().getValue();
        } catch (BCompoundException e) {
            throw new ProBError(e);
        }
        return new Position(((BigInteger)value.get(0)).intValue(), ((BigInteger)value.get(1)).intValue());
    }

    public List<Position> getPositions(Trace trace, String eventbFormula) {
        EventB begehbar = new EventB(eventbFormula, Collections.emptySet(), FormulaExpand.EXPAND);
        EvalResult result = (EvalResult) trace.evalCurrent(begehbar);
        Set translatedSet;
        try {
            translatedSet = (Set) result.translate().getValue();
        } catch (BCompoundException e) {
            throw new ProBError(e);
        }
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
        return check(trace, "(" + x + "|->" + y + ") : begehbar");
    }

    public int getNumber(Trace trace, String eventbFormula) {
        EvalResult result = (EvalResult) trace.evalCurrent(eventbFormula, FormulaExpand.EXPAND);
        try {
            return ((BigInteger) result.translate().getValue()).intValue();
        } catch (BCompoundException e) {
            throw new ProBError(e);
        }
    }

    public boolean check(Trace trace, String eventbFormula) {
        EvalResult result = (EvalResult) trace.evalCurrent(eventbFormula, FormulaExpand.EXPAND);
        try {
            return ((de.prob.translator.types.Boolean) result.translate().getValue()).booleanValue();
        } catch (BCompoundException e) {
            throw new ProBError(e);
        }
    }

    public boolean checkEvent(Trace trace, String event) {
        return trace.canExecuteEvent(event);
    }

    public boolean checkEvent(Trace trace, String event, String parameter) {
        return trace.canExecuteEvent(event, parameter);
    }

    public Trace execute(Trace trace, String event) {
        return trace.execute(event);
    }

    public Trace execute(Trace trace, String event, String parameter) {
        return trace.execute(event, parameter);
    }
}
