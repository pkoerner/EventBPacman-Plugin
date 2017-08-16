package de.heinzen.probplugin.pacman;

import de.prob.statespace.Trace;
import de.prob2.ui.plugin.ProBConnection;
import de.prob2.ui.plugin.ProBPlugin;
import de.prob2.ui.prob2fx.CurrentTrace;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.scene.control.Tab;
import javafx.scene.input.KeyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.fortsoft.pf4j.PluginWrapper;


/**
 * Created by Christoph Heinzen on 14.08.17.
 */
public class PacmanPlugin extends ProBPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(PacmanPlugin.class);

    private Tab pacmanTab;
    private ChangeListener<Trace> currentTraceChangeListener;
    private EventHandler<? super KeyEvent> keyListener;

    public PacmanPlugin(PluginWrapper pluginWrapper) {
        super(pluginWrapper);
    }

    @Override
    public void startPlugin() {
        System.out.println("Stopping " + getName());
        ProBConnection proBConnection = getProBConnection();
        CurrentTrace currentTrace = proBConnection.getCurrentTrace();

        //create GUI
        pacmanTab = new Tab("Pacman");

        PacmanAnimator animator = new PacmanAnimator(currentTrace);
        PacmanGui gui = new PacmanGui(animator);
        PacmanLogic logic = new PacmanLogic(gui, animator);


        if (currentTrace.getCurrentState() != null && currentTrace.getCurrentState().isInitialised()) {
            gui.createGui(pacmanTab);
            gui.update(currentTrace.get());
        }

        currentTraceChangeListener = (observable, oldValue, newValue) -> {
            //TODO: check if the pacman machine is loaded
            if (oldValue != null && oldValue.getCurrentState() != null
                    && newValue != null && newValue.getCurrentState() != null
                    && !oldValue.getCurrentState().isInitialised()
                    && newValue.getCurrentState().isInitialised()) {
                //animator.setCurrentTrace(observable);
                gui.createGui(pacmanTab);
                gui.update(newValue);
            } else if ( newValue != null && newValue.getCurrentState() != null
                    && newValue.getCurrentState().isInitialised()) {
                gui.update(newValue);
            }
        };

        keyListener = event -> logic.movePacman(event.getCode());

        proBConnection.addTab(pacmanTab);
        currentTrace.addListener(currentTraceChangeListener);
        proBConnection.getStageManager().getMainStage().getScene().addEventFilter(KeyEvent.KEY_PRESSED, keyListener);
    }

    @Override
    public void stopPlugin() {
        System.out.println("Stopping " + getName());
        getProBConnection().getCurrentTrace().removeListener(currentTraceChangeListener);
        getProBConnection().removeTab(pacmanTab);
        getProBConnection().getStageManager().getMainStage().getScene().removeEventFilter(KeyEvent.KEY_PRESSED, keyListener);
    }

    @Override
    public String getName() {
        return "Event-B Pacman";
    }
}
