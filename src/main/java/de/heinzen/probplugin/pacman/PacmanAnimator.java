package de.heinzen.probplugin.pacman;

import java.util.*;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.prob.animator.domainobjects.AbstractEvalResult;
import de.prob.animator.domainobjects.EvalResult;
import de.prob.animator.domainobjects.EventB;
import de.prob.animator.domainobjects.FormulaExpand;
import de.prob.animator.domainobjects.IEvalElement;
import de.prob.exception.ProBError;
import de.prob.statespace.Trace;
import de.prob.translator.types.BObject;
import de.prob.translator.types.BigInteger;
import de.prob.translator.types.Set;
import de.prob.translator.types.Tuple;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Christoph Heinzen on 15.08.17.
 */
public class PacmanAnimator {
    public java.util.Set<String> subscribed = new java.util.HashSet<>();
 //   public Map<String, IEvalElement> nameToEvalElement = new HashMap<>();
    public PacmanAnimator() {}

    public EvalResult getValueOfVariable(Trace trace, String name) {
        if (!subscribed.contains(name)) {
            trace.getStateSpace().subscribe(this, new EventB(name));
            subscribed.add(name);
        }


        Map<IEvalElement, AbstractEvalResult> values = trace.getCurrentState().getValues();
   //     if (nameToEvalElement.containsKey(name)) {
   //         return (EvalResult) values.get(nameToEvalElement.get(name));
   //     }

        for (IEvalElement ele : values.keySet()) {
            if (ele.getCode().equals(name)) {
                System.out.println(ele + " " + System.identityHashCode(ele));
     //           nameToEvalElement.put(name, ele);
                return (EvalResult) values.get(ele);
            }
        }

        throw new RuntimeException("oop oop oop!");
    }

    public BigInteger getIntVariable(Trace trace, String name) {
        try {
            return (BigInteger) getValueOfVariable(trace, name).translate().getValue();
        } catch (BCompoundException e) {
            throw new ProBError(e);
        }
    }

    public Set getSetVariable(Trace trace, String name){
        try {
            return (Set) getValueOfVariable(trace, name).translate().getValue();
        } catch (BCompoundException e) {
            throw new ProBError(e);
        }
    }



    public Position getPosition(Trace trace, String eventbFormula) {
        EvalResult result = getValueOfVariable(trace, eventbFormula);
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

    public Position evalPosition(Trace trace, String eventbFormula) {
        EvalResult result = (EvalResult) trace.evalCurrent(eventbFormula, FormulaExpand.EXPAND);
        return resultToPosition(result);
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
        return getSetVariable(trace, "begehbar").contains(new Tuple(BigInteger.build(x), BigInteger.build(y)));
    }

}
