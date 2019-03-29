package de.heinzen.probplugin.pacman;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import de.prob.animator.domainobjects.EventB;
import de.prob.animator.domainobjects.FormulaExpand;
import de.prob.animator.domainobjects.IEvalElement;

final class PacmanFormulas {
	static final EventB BEGEHBAR = new EventB("begehbar", FormulaExpand.EXPAND);
	static final EventB COUNTER_SCORED = new EventB("counter_scored", FormulaExpand.EXPAND);
	static final EventB GEISTER_AKTUELL = new EventB("geister_aktuell", FormulaExpand.EXPAND);
	static final EventB GEISTERHOF = new EventB("geisterhof", FormulaExpand.EXPAND);
	static final EventB GEISTERHOF_ZUGANG = new EventB("geisterhof_zugang", FormulaExpand.EXPAND);
	static final EventB GEJAGTE_GEISTER = new EventB("gejagte_geister", FormulaExpand.EXPAND);
	static final EventB LEBEN = new EventB("leben", FormulaExpand.EXPAND);
	static final EventB PUNKTEFELDER_AKTUELL = new EventB("punktefelder_aktuell", FormulaExpand.EXPAND);
	static final EventB POSITION = new EventB("position", FormulaExpand.EXPAND);
	static final EventB SCORE = new EventB("score", FormulaExpand.EXPAND);
	static final EventB STARTPOSITION = new EventB("startposition", FormulaExpand.EXPAND);
	static final EventB VORHERIGE_POSITION = new EventB("vorherige_position", FormulaExpand.EXPAND);
	
	static final EventB[] COUNTER_GEIST = new EventB[4];
	static final EventB[] GEIST_COUNTER = new EventB[4];
	static final EventB[] POS_GEIST = new EventB[4];
	static final EventB[] POS_GEIST_ALT = new EventB[4];
	static final EventB[] STARTPOS_GEIST = new EventB[4];
	static {
		Arrays.setAll(PacmanFormulas.COUNTER_GEIST, i -> i < 2 ? null : new EventB("counter_geist_" + (i+1), FormulaExpand.EXPAND));
		Arrays.setAll(PacmanFormulas.GEIST_COUNTER, i -> new EventB("geist_" + (i+1) + "_counter", FormulaExpand.EXPAND));
		Arrays.setAll(PacmanFormulas.POS_GEIST, i -> new EventB("pos_geist_" + (i+1), FormulaExpand.EXPAND));
		Arrays.setAll(PacmanFormulas.POS_GEIST_ALT, i -> new EventB("pos_geist_" + (i+1) + "_alt", FormulaExpand.EXPAND));
		Arrays.setAll(PacmanFormulas.STARTPOS_GEIST, i -> new EventB("startpos_geist_" + (i+1), FormulaExpand.EXPAND));
	}
	
	static final Collection<IEvalElement> ALL;
	static {
		final Collection<IEvalElement> all = new ArrayList<>(Arrays.asList(
			BEGEHBAR,
			COUNTER_SCORED,
			GEISTER_AKTUELL,
			GEISTERHOF,
			GEISTERHOF_ZUGANG,
			GEJAGTE_GEISTER,
			LEBEN,
			PUNKTEFELDER_AKTUELL,
			POSITION,
			SCORE,
			STARTPOSITION,
			VORHERIGE_POSITION
		));
		all.addAll(Arrays.asList(COUNTER_GEIST));
		all.addAll(Arrays.asList(GEIST_COUNTER));
		all.addAll(Arrays.asList(POS_GEIST));
		all.addAll(Arrays.asList(POS_GEIST_ALT));
		all.addAll(Arrays.asList(STARTPOS_GEIST));
		all.removeAll(Collections.singleton(null));
		ALL = Collections.unmodifiableCollection(all);
	}
	
	private PacmanFormulas() {
		throw new AssertionError("Utility class");
	}
}
