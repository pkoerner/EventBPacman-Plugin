package de.heinzen.probplugin.pacman;

import de.prob.model.representation.Machine;
import de.prob.statespace.Trace;
import de.prob2.ui.plugin.ProBPlugin;
import de.prob2.ui.plugin.ProBPluginHelper;
import de.prob2.ui.plugin.ProBPluginManager;
import de.prob2.ui.prob2fx.CurrentTrace;

import javafx.beans.value.ChangeListener;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;

import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Christoph Heinzen on 14.08.17.
 */
public class PacmanPlugin extends ProBPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(PacmanPlugin.class);

    private Tab pacmanTab;
    private ChangeListener<Trace> currentTraceChangeListener;

    public PacmanPlugin(final PluginWrapper pluginWrapper, final ProBPluginManager proBPluginManager, final ProBPluginHelper proBPluginHelper) {
        super(pluginWrapper, proBPluginManager, proBPluginHelper);
    }

    @Override
    public void startPlugin() {
        CurrentTrace currentTrace = getProBPluginHelper().getCurrentTrace();

        //create GUI
        pacmanTab = new Tab("Pacman");

        PacmanAnimator animator = new PacmanAnimator();
        PacmanGui gui = new PacmanGui(animator);
        PacmanLogic logic = new PacmanLogic(gui, animator);

        currentTraceChangeListener = (o, from, to) -> {
            if (to == null) {
                pacmanTab.setContent(new Label("No model loaded"));
            } else if (!(to.getStateSpace().getMainComponent() instanceof Machine) || !((Machine)to.getStateSpace().getMainComponent()).getName().contains("Pacman")) {
                pacmanTab.setContent(new Label("This is not a Pacman machine"));
            } else if (!to.getCurrentState().isInitialised()) {
                pacmanTab.setContent(new Label("Machine not initialized"));
            } else if (pacmanTab.getContent() == null || pacmanTab.getContent() instanceof Label) {
                gui.createGui(to, pacmanTab, event -> currentTrace.set(logic.movePacman(currentTrace.get(), event.getCode())));
            } else {
                gui.update(to);
            }
        };

        getProBPluginHelper().addTab(pacmanTab);
        currentTrace.addListener(currentTraceChangeListener);
        currentTraceChangeListener.changed(currentTrace, null, currentTrace.get());
    }

    @Override
    public void stopPlugin() {
        getProBPluginHelper().getCurrentTrace().removeListener(currentTraceChangeListener);
        getProBPluginHelper().removeTab(pacmanTab);
    }

    @Override
    public String getName() {
        return "Event-B Pacman";
    }
}
