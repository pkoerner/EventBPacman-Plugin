package de.heinzen.probplugin.pacman;

import de.prob.statespace.Trace;
import de.prob2.ui.plugin.ProBPlugin;
import de.prob2.ui.plugin.ProBPluginHelper;
import de.prob2.ui.plugin.ProBPluginManager;
import de.prob2.ui.prob2fx.CurrentTrace;

import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.scene.control.Tab;
import javafx.scene.input.KeyEvent;

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

        final EventHandler<KeyEvent> keyListener = event -> currentTrace.set(logic.movePacman(currentTrace.get(), event.getCode()));

        if (currentTrace.getCurrentState() != null && currentTrace.getCurrentState().isInitialised()) {
            gui.createGui(currentTrace.get(), pacmanTab, keyListener);
        }

        currentTraceChangeListener = (observable, oldValue, newValue) -> {
            //TODO: check if the pacman machine is loaded
            if (oldValue != null && oldValue.getCurrentState() != null
                    && newValue != null && newValue.getCurrentState() != null
                    && !oldValue.getCurrentState().isInitialised()
                    && newValue.getCurrentState().isInitialised()) {
                gui.createGui(newValue, pacmanTab, keyListener);
            } else if ( newValue != null && newValue.getCurrentState() != null
                    && newValue.getCurrentState().isInitialised()) {
                gui.update(newValue);
            }
        };

        getProBPluginHelper().addTab(pacmanTab);
        currentTrace.addListener(currentTraceChangeListener);
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
