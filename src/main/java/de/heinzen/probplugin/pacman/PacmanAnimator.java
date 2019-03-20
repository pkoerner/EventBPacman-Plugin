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
import de.prob2.ui.prob2fx.CurrentTrace;

/**
 * Created by Christoph Heinzen on 15.08.17.
 */
public class PacmanAnimator {

    private CurrentTrace currentTrace;

    public PacmanAnimator(CurrentTrace currentTrace) {
        this.currentTrace = currentTrace;
    }

    private Trace getTrace() {
        return currentTrace.getValue();
    }

    private void setTrace(Trace trace) {
        currentTrace.set(trace);
    }

    public Position getPosition(String eventbFormula) {
        EvalResult result = (EvalResult) getTrace().evalCurrent(eventbFormula, FormulaExpand.EXPAND);
        Tuple value;
        try {
            value = (Tuple) result.translate().getValue();
        } catch (BCompoundException e) {
            throw new ProBError(e);
        }
        return new Position(((BigInteger)value.get(0)).intValue(), ((BigInteger)value.get(1)).intValue());
    }

    public List<Position> getPositions(String eventbFormula) {
        EventB begehbar = new EventB(eventbFormula, Collections.emptySet(), FormulaExpand.EXPAND);
        EvalResult result = (EvalResult) getTrace().evalCurrent(begehbar);
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

    public boolean checkPosition(Position pos) {
        return checkPosition(pos.getX(), pos.getY());
    }

    public boolean checkPosition(int x, int y) {
        return check("(" + x + "|->" + y + ") : begehbar");
    }

    public int getNumber(String eventbFormula) {
        EvalResult result = (EvalResult) getTrace().evalCurrent(eventbFormula, FormulaExpand.EXPAND);
        try {
            return ((BigInteger) result.translate().getValue()).intValue();
        } catch (BCompoundException e) {
            throw new ProBError(e);
        }
    }

    public boolean check(String eventbFormula) {
        EvalResult result = (EvalResult) getTrace().evalCurrent(eventbFormula, FormulaExpand.EXPAND);
        try {
            return ((de.prob.translator.types.Boolean) result.translate().getValue()).booleanValue();
        } catch (BCompoundException e) {
            throw new ProBError(e);
        }
    }

    public boolean checkEvent(String event) {
        return getTrace().canExecuteEvent(event);
    }

    public boolean checkEvent(String event, String parameter) {
        return getTrace().canExecuteEvent(event, parameter);
    }

    public void execute(String event) {
        setTrace(getTrace().execute(event));
    }

    public void execute(String event, String parameter) {
        setTrace(getTrace().execute(event, parameter));
    }
}
