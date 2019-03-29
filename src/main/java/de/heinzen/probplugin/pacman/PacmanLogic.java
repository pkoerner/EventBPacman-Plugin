package de.heinzen.probplugin.pacman;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import de.prob.statespace.Trace;
import de.prob.statespace.Transition;

import javafx.scene.input.KeyCode;

/**
 * Created by Christoph Heinzen on 15.08.17.
 */
public class PacmanLogic {

    private static final int[] GHOST_TURNING_POINTS = {70,270,340,540,590,790,840};
    private static final Position[] UP_DIRECTION_FORBIDDEN = {
            new Position(28, 44),
            new Position(22,44),
            new Position(28, 20),
            new Position(22, 20)};
    private static  final Position[] TUNNEL_ACCESS = {
            new Position(8,26),
            new Position(42,26)
    };

    private static final HashMap<KeyCode, String> KEY_TO_DIRECTION;
    static {
        KEY_TO_DIRECTION = new HashMap<>(4);
        KEY_TO_DIRECTION.put(KeyCode.UP, "oben");
        KEY_TO_DIRECTION.put(KeyCode.DOWN, "unten");
        KEY_TO_DIRECTION.put(KeyCode.RIGHT, "rechts");
        KEY_TO_DIRECTION.put(KeyCode.LEFT, "links");
    }

    private PacmanGui gui;
    private PacmanAnimator animator;

    public PacmanLogic(PacmanGui gui, PacmanAnimator animator) {
        this.gui = gui;
        this.animator = animator;
    }

    public Trace movePacman(Trace trace, KeyCode code) {
        Trace newTrace = trace;
        final String direction = KEY_TO_DIRECTION.get(code);
        if (direction != null) {
            newTrace = moveGhosts(newTrace);
            newTrace = tryToCatchGhosts(newTrace);
            final Optional<Trace> newTrace2 = tryToCatchPacman(newTrace);
            if (newTrace2.isPresent()) {
                newTrace = newTrace2.get();
            } else {
                final Transition bewegenScore = newTrace.getCurrentState().findTransition("bewegen_" + direction + "_score");
                if (bewegenScore != null) {
                    newTrace = newTrace.add(bewegenScore);
                    gui.updateScoreDots(newTrace);
                    gui.updateScoreValue(newTrace);
                    gui.updatePacman(newTrace);
                } else {
                    final Transition bewegenGhost = newTrace.getCurrentState().findTransition("bewegen_" + direction + "_ghost");
                    if (bewegenGhost != null) {
                        newTrace = newTrace.add(bewegenGhost);
                        for (int i = 0; i < 4; i++) {
                            gui.updateGhost(newTrace, i);
                        }
                        gui.updateGhostDots(newTrace);
                        gui.updatePacman(newTrace);
                    } else {
                        final Transition bewegen = newTrace.getCurrentState().findTransition("bewegen_" + direction);
                        if (bewegen != null) {
                            newTrace = newTrace.add(bewegen);
                            gui.updatePacman(newTrace);
                        } else {
                            final Transition tunneln = newTrace.getCurrentState().findTransition("tunneln");
                            if (tunneln != null) {
                                newTrace = newTrace.add(tunneln);
                                gui.updatePacman(newTrace);
                            }
                        }
                    }
                }
                newTrace = tryToCatchGhosts(newTrace);
                newTrace = tryToCatchPacman(newTrace).orElse(newTrace);
                final Transition geisterjagdAbbrechen = newTrace.getCurrentState().findTransition("geisterjagd_abbrechen");
                if (geisterjagdAbbrechen != null) {
                    newTrace = newTrace.add(geisterjagdAbbrechen);
                    for (int i = 0; i < 4; i++) {
                        gui.updateGhost(newTrace, i);
                    }
                }
            }
        }
        return newTrace;
    }

    private Trace moveGhosts(Trace trace) {
        Trace newTrace = trace;
        for (int ghost = 0; ghost < 4; ghost++) {
            newTrace = moveGhost(newTrace, ghost);
        }
        return newTrace;
    }

    private Trace moveGhost(Trace trace, int ghost) {
        Trace newTrace = trace;
        
        final Transition starteGeist = newTrace.getCurrentState().findTransition("starte_geist_" + (ghost + 1));
        if (starteGeist != null) {
            newTrace = newTrace.add(starteGeist);
            gui.updateGhost(newTrace, ghost);
        }
        if (ghost > 1 && animator.getIntVariable(newTrace, PacmanFormulas.COUNTER_SCORED).intValue()
                       < animator.getIntVariable(newTrace, PacmanFormulas.COUNTER_GEIST[ghost]).intValue()) {
            return newTrace;
        }
        Position next = computePosition(newTrace, ghost);

        final Transition bewegeGeist = newTrace.getCurrentState().findTransition("bewege_geist_" + (ghost + 1), "pos = (" + next.getX() + "|->" + next.getY() + ")");
        if (bewegeGeist != null) {
            newTrace = newTrace.add(bewegeGeist);
            gui.updateGhost(newTrace, ghost);
        }

        return newTrace;
    }

    private Position computePosition(Trace trace, int ghost) {
        final Position ghostPosOld = animator.getPosition(trace, PacmanFormulas.POS_GEIST_ALT[ghost]);
        int counter = animator.getIntVariable(trace, PacmanFormulas.GEIST_COUNTER[ghost]).intValue();
        if (IntStream.of(GHOST_TURNING_POINTS).anyMatch(x -> x == counter)) {
            return ghostPosOld;
        }

        final Position ghostPos = animator.getPosition(trace, PacmanFormulas.POS_GEIST[ghost]);

        List<Position> neighbours = getNeighbours(trace, ghostPos, ghostPosOld);
        if (neighbours.size() == 1) {
            return neighbours.get(0);
        }
        final Position target = getTarget(trace, ghost, counter);
        Position ret = null;
        final Optional<Position> mins = neighbours.stream().min(Comparator.comparingDouble(p -> p.getDistance(target)));
        if (mins.isPresent()) {
            ret = mins.get();
        }
        return ret;
    }

    private List<Position> getNeighbours(Trace trace, Position pos, Position posOld) {
        final Position[] directNeighbours = {
                new Position(pos.getX() + 2, pos.getY()),
                new Position(pos.getX() - 2, pos.getY()),
                new Position(pos.getX(), pos.getY() + 2),
                new Position(pos.getX(), pos.getY() - 2)};
        List<Position> neighbours = new ArrayList<>();
        for (Position p : directNeighbours) {
            // an bestimmten Stellen dürfen die Geister nicht nach oben gehen
            if (Arrays.stream(UP_DIRECTION_FORBIDDEN).anyMatch(x -> x.equals(pos)) && (pos.getY() - 2 == p.getY())) {
                continue;
            }
            if (!p.equals(posOld)
                    && Arrays.stream(TUNNEL_ACCESS).noneMatch(x -> x.equals(p))
                    && animator.checkPosition(trace, p)) {
                neighbours.add(p);
            }
        }
        return neighbours;
    }

    private Position getTarget(Trace trace, int ghost, int counter){
        if(counter < 70
                || (counter >= 270 && counter <= 340)
                || (counter >= 540 && counter <= 590)
                || (counter >= 790 && counter <= 840)){
            switch(ghost){
                case 0:
                    return new Position(48,-10);
                case 1:
                    return new Position(2,-10);
                case 2:
                    return new Position(52,66);
                case 3:
                    return new Position(-2,66);
            }
        }
        final Position pacmanPos = animator.getPosition(trace, PacmanFormulas.POSITION);
        final Position pacmanPosOld = animator.getPosition(trace, PacmanFormulas.VORHERIGE_POSITION);
        switch(ghost){
            case 0:
                return pacmanPos;
            case 1:
                if(pacmanPos.getX() - pacmanPosOld.getX() == 2) return new Position(pacmanPos.getX() + 8, pacmanPos.getY());
                if(pacmanPos.getX() - pacmanPosOld.getX() == -2) return new Position(pacmanPos.getX() - 8, pacmanPos.getY());
                if(pacmanPos.getY() - pacmanPosOld.getY() == 2) return new Position(pacmanPos.getX(), pacmanPos.getY() + 8);
                return new Position(pacmanPos.getX() - 8, pacmanPos.getY() - 8);
            case 2:
                Position targetTmp;
                if(pacmanPos.getX() - pacmanPosOld.getX() == 2){
                    targetTmp = new Position(pacmanPos.getX() + 4, pacmanPos.getY());
                }else if(pacmanPos.getX() - pacmanPosOld.getX() ==-2){
                    targetTmp = new Position(pacmanPos.getX() - 4, pacmanPos.getY());
                }else if(pacmanPos.getY() - pacmanPosOld.getY() == 2){
                    targetTmp = new Position(pacmanPos.getX(), pacmanPos.getY() + 4);
                }else{
                    targetTmp = new Position(pacmanPos.getX() - 4, pacmanPos.getY() - 4);
                }
                Position ghost1Pos = animator.getPosition(trace, PacmanFormulas.POS_GEIST[0]);
                int deltaX = targetTmp.getX() - ghost1Pos.getX();
                int deltaY = targetTmp.getY() - ghost1Pos.getY();
                return new Position(ghost1Pos.getX() + 2 * deltaX, ghost1Pos.getY() + 2 * deltaY);
            case 3:
                double dist = animator.getPosition(trace, PacmanFormulas.POS_GEIST[3]).getDistance(pacmanPos);
                if(dist < 8.0){
                    return new Position(-2,66);
                } else {
                    return pacmanPos;
                }
        }
        return null;
    }

    private Trace tryToCatchGhosts(Trace trace) {
        Trace newTrace = trace;
        for (int i = 1; i <= 4; i++) {
            final Transition op = newTrace.getCurrentState().findTransition("geist_" + i + "_fangen");
            if (op != null) {
                newTrace = newTrace.add(op);
                gui.updateGhost(newTrace, i-1);
                gui.updateScoreValue(newTrace);
                newTrace = moveGhost(newTrace, i - 1);
            }
        }
        return newTrace;
    }
    
    private Optional<Trace> tryToCatchPacman(Trace trace) {
        Trace newTrace = trace;
        final Transition op = newTrace.getCurrentState().findTransition("pacman_fangen");
        if (op != null) {
            newTrace = newTrace.add(op);
            gui.updatePacman(newTrace);
            for (int i = 0; i < 4; i++) {
                gui.updateGhost(newTrace, i);
            }
            gui.updateLives(newTrace);
            return Optional.of(newTrace);
        }
        return Optional.empty();
    }

}
