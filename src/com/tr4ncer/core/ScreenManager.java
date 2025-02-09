package com.tr4ncer.core;

import com.tr4ncer.factory.*;
import com.tr4ncer.logger.*;
import com.tr4ncer.threading.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.lang.reflect.*;
import javax.swing.*;

/**
 * Gestionnaire d'écran.
 * Permet principalement la gestion d'accélération graphique.
 * Entièrement compatible avec le mode fenêtrée et le plein écran.
 * <p>
 * Remarque :
 * Ne pas oublier de le rafraichir et de détruire les graphismes.
 *
 * @version 3.00.03
 * @author Sebastien Villemain
 */
public class ScreenManager implements EntityProcess {

    /**
     * Liste des écrans disponibles.
     */
    private static final GraphicsDevice[] DEVICES = getDevices();

    /**
     * Gestionnaire de configuration graphique.
     */
    private static GraphicsConfiguration graphicsConfiguration = null;

    /**
     * La stratégie appliquée sur l'écran.
     */
    private final DeviceStrategy strategy = new DeviceStrategy();

    /**
     * Numéro de l'écran.
     */
    private int deviceNumber = 0;

    /**
     * La zone représentant le visuel en mode fenêtrée.
     */
    private Canvas component = null;

    /**
     * Etat du plein écran.
     * Détermine l'utilisation du mode hardware (rendu par la carte graphique) ou du mode software (rendu par logiciel).
     */
    private boolean fullScreenWindow = false;

    protected ScreenManager() {
        // NE RIEN FAIRE
    }

    /**
     * Retourne la configuration graphique.
     *
     * @return
     */
    public static GraphicsConfiguration getGraphicsConfiguration() {
        GraphicsConfiguration configuration = graphicsConfiguration;

        if (configuration == null) {
            if (FactoryManager.hasInstance(ScreenManager.class)) {
                configuration = FactoryManager.getInstance(ScreenManager.class).getConfiguration();
            }

            if (configuration == null) {
                configuration = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
            }
        }
        return configuration;
    }

    /**
     * Change le gestionnaire de configuration graphique.
     *
     * @param gConfiguration
     */
    public static void setGraphicsConfiguration(GraphicsConfiguration gConfiguration) {
        if (graphicsConfiguration != gConfiguration) {
            graphicsConfiguration = gConfiguration;
        }
    }

    /**
     * Retourne la liste des écrans disponibles.
     *
     * @return
     */
    private static GraphicsDevice[] getDevices() {
        GraphicsDevice[] devices;

        try {
            devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
        } catch (HeadlessException e) {
            devices = new GraphicsDevice[1];
            devices[0] = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        }
        return devices;
    }

    /**
     * Returne le gestionnaire de configuration graphique lié de l'écran en cours.
     *
     * @return
     */
    public GraphicsConfiguration getConfiguration() {
        GraphicsConfiguration configuration = null;

        synchronized (strategy) {
            GraphicsConfiguration[] configurations = DEVICES[deviceNumber].getConfigurations();

            if (configurations != null && configurations.length > 0) {
                configuration = configurations[0];
            }
        }
        return configuration;
    }

    /**
     * Retourne la liste des modes d'affichage compatibles.
     *
     * @return
     */
    public DisplayMode[] getCompatibleDisplayModes() {
        synchronized (strategy) {
            return DEVICES[deviceNumber].getDisplayModes();
        }
    }

    /**
     * Retourne le mode d'affichage courant.
     *
     * @return
     */
    public DisplayMode getCurrentDisplayMode() {
        synchronized (strategy) {
            return DEVICES[deviceNumber].getDisplayMode();
        }
    }

    /**
     * Mise à jour du numéro de l'écran en focus.
     * Uniquement du mode software.
     *
     * @return
     */
    private void setDeviceNumber() {
        if (!fullScreenWindow) {
            Window w = getWindow(component);

            if (w != null) {
                // Ne surtout pas mettre à jour le numéro de l'écran si la fenêtre n'est pas visible
                if (w.isVisible()) {
                    int index = 0;
                    Point start = w.getLocation();

                    for (GraphicsDevice device : DEVICES) {
                        GraphicsConfiguration[] configurations = device.getConfigurations();

                        if (configurations != null && configurations.length > 0) {
                            if (configurations[0].getBounds().contains(start)) {
                                break;
                            }
                        }

                        index++;
                    }

                    synchronized (strategy) {
                        if (index != deviceNumber) {
                            LoggerManager.getInstance().addDebug("Updating device number to screen " + index + ".");
                            deviceNumber = index;
                        }
                    }
                }
            }
        }
    }

    /**
     * Retourne les dimensions maximum de l'écran (gestion du multi-écran inclus).
     *
     * @return
     */
    public static Rectangle getMaximumBounds() {
        Rectangle rslt = new Rectangle();

        for (GraphicsDevice device : DEVICES) {
            for (GraphicsConfiguration configuration : device.getConfigurations()) {
                Rectangle.union(rslt, configuration.getBounds(), rslt);
            }
        }
        return rslt;
    }

    /**
     * Retourne les dimensions de l'écran en focus.
     *
     * @return
     */
    public Rectangle getCurrentBounds() {
        Rectangle rslt = new Rectangle();

        synchronized (strategy) {
            for (GraphicsConfiguration configuration : DEVICES[deviceNumber].getConfigurations()) {
                Rectangle.union(rslt, configuration.getBounds(), rslt);
            }
        }
        return rslt;
    }

    /**
     * Retourne le premier mode d'affichage compatible.
     * Si aucun mode d'affichage dans la liste n'est compatible, retourne
     * <code>null</code>.
     *
     * @param modes
     * @return DisplayMode or <code>null</code>.
     */
    public DisplayMode findFirstCompatibleMode(DisplayMode modes[]) {
        DisplayMode compatibleMode = null;

        // Affichage courant
        DisplayMode currentMode = getCurrentDisplayMode();

        // Liste des affichages compatibles
        DisplayMode compatibleModes[] = getCompatibleDisplayModes();

display:for (DisplayMode mode : modes) {
            // Si le mode correspond a celui utilisé
            if (displayModesMatch(mode, currentMode)) {
                compatibleMode = mode;
                break;
            }

            for (DisplayMode testMode : compatibleModes) {
                if (displayModesMatch(mode, testMode)) {
                    compatibleMode = mode;
                    break display;
                }
            }
        }
        return compatibleMode;
    }

    /**
     * Vérifie si les deux modes d'affichges sont similaire, et donc compatible.
     * Pour déterminer la compatibilité, on se base sur la taille de la résolution,
     * sur la profondeur de bit (16, 24, 32...) et sur le taux de rafraichissement.
     *
     * @param mode1
     * @param mode2
     * @return
     */
    public static boolean displayModesMatch(DisplayMode mode1, DisplayMode mode2) {
        boolean match = true;

        // Si les tailles ne correspondent pas
        if (mode1.getWidth() != mode2.getWidth()
            || mode1.getHeight() != mode2.getHeight()) {
            match = false;
        }

        // Si le niveau de profondeur (16 bit, 24 bit ou 32 bit) n'est pas correcte
        if (match
            && mode1.getBitDepth() != DisplayMode.BIT_DEPTH_MULTI
            && mode2.getBitDepth() != DisplayMode.BIT_DEPTH_MULTI
            && mode1.getBitDepth() != mode2.getBitDepth()) {
            match = false;
        }

        // Taux de raffraichissement
        if (match
            && mode1.getRefreshRate() != DisplayMode.REFRESH_RATE_UNKNOWN
            && mode2.getRefreshRate() != DisplayMode.REFRESH_RATE_UNKNOWN
            && mode1.getRefreshRate() != mode2.getRefreshRate()) {
            match = false;
        }
        return match;
    }

    /**
     * Création d'une fenêtre en plein écran avec le mode choisi.
     * Plus d'information sur {@link #setFullScreen(java.awt.DisplayMode, java.awt.Window)}.
     *
     * @param displayMode
     */
    public void setFullScreen(DisplayMode displayMode) {
        // Création de la fenêtre pour le mode de plein écran
        Frame frame = new Frame();
        frame.setTitle("No name");

        setFullScreen(displayMode, frame);
    }

    /**
     * Affichage d'une fenêtre en plein écran avec le mode choisi.
     * Si le mode choisi n'est pas supporté, ou si il y a une erreur,
     * l'affichage en plein écran se fera avec les paramétres courant.
     * <p>
     * Une tentative d'augmentation du buffer est aussi executée.
     *
     * @param displayMode
     * @param w
     */
    public void setFullScreen(DisplayMode displayMode, Window w) {
        boolean success = false;

        if (w != null) {
            // Reconfiguration pour la mise en plein écran
            w.setVisible(false);
            w.dispose();

            if (w instanceof Frame frame) {
                frame.setUndecorated(true);
            }

            // Inhibe la méthode courante d'affichage du composant
            // http://bugs.sun.com/view_bug.do?bug_id=6415012
            w.setIgnoreRepaint(true);

            if (w instanceof Frame frame) {
                frame.setResizable(false);
            }

            w.setVisible(true);

            synchronized (strategy) {
                // Tentative de mise en plein écran
                DEVICES[deviceNumber].setFullScreenWindow(w);

                // Si il est possible de changer le mode d'affichage
                if (displayMode != null
                    && DEVICES[deviceNumber].isDisplayChangeSupported()) {
                    success = true;

                    try {
                        DEVICES[deviceNumber].setDisplayMode(displayMode);
                    } catch (IllegalArgumentException ex) {
                        success = false;
                    }

                    // Correction d'un problème de taille sur Mac OS X
                    w.setSize(displayMode.getWidth(), displayMode.getHeight());
                }

                // Vérification du support de plein écran
                // Note: Si Linux, il est possible que isFullScreenSupported() retourne "false", même si le plein écran fonctionne
                fullScreenWindow = DEVICES[deviceNumber].isFullScreenSupported() && success;
            }

            fireHardwareStrategy();
        }

        if (success) {
            LoggerManager.getInstance().addDebug("Switching to full screen mode"
                                                 + (displayMode != null ? " (" + displayMode.getWidth() + "*" + displayMode.getHeight() + ")" : "")
                                                 + " for frame " + (w != null ? w.getName() : "") + ".");
        }
    }

    /**
     * Retourne la fenêtre représentant l'affichage géré.
     *
     * @return Window or <code>null</code>.
     */
    private Window getCurrentWindow() {
        Window w;

        if (fullScreenWindow) {
            synchronized (strategy) {
                w = DEVICES[deviceNumber].getFullScreenWindow();
            }
        } else {
            w = getWindow(component);
        }
        return w;
    }

    /**
     * Vérifie si le plein écran est activé.
     *
     * @return
     */
    public boolean isFullScreenWindow() {
        return fullScreenWindow;
    }

    /**
     * Affecte le composant fenêtrée de l'écran.
     *
     * @param component
     */
    public void setComponent(Canvas component) {
        if (component != null
            && this.component != component) {
            // Gestion du multi-écran
            if (DEVICES.length > 1) {
                // Nettoyage de l'ancien composant
                Window w = getWindow(this.component);

                if (w != null) {
                    if (strategy != null) {
                        w.removeComponentListener(strategy);
                    }
                }

                // Integration du nouveau composant
                w = getWindow(component);

                if (w != null) {
                    w.addComponentListener(strategy);
                }
            }

            this.component = component;

            fireHardwareStrategy();
            setDeviceNumber();
        }
    }

    /**
     * Retourne la fenêtre parente.
     * En cas d'erreur, retourne {@code null}.
     *
     * @param component
     * @return
     */
    private static Window getWindow(Component component) {
        Window w = null;

        if (component != null) {
            Container c = component.getParent();

            while (c != null) {
                if (c instanceof Window window) {
                    w = window;
                    break;
                }

                c = c.getParent();
            }
        }
        return w;
    }

    /**
     * Retourne le composant principal de l'écran.
     * En cas d'erreur, retourne
     * <code>null</code>.
     *
     * @return Container or <code>null</code>.
     */
    public Container getComponent() {
        Container c = null;

        if (fullScreenWindow) {
            Window w = getCurrentWindow();

            if (w instanceof JFrame) {
                c = ((JFrame) getCurrentWindow()).getContentPane();
            } else if (w != null) {
                Component[] components = w.getComponents();

                if (components != null) {
                    // N'importe quel composant
                    for (Component wComponent : w.getComponents()) {
                        if (wComponent instanceof Container container) {
                            c = container;
                            break;
                        }
                    }
                }
            }
        } else {
            c = component.getParent();
        }
        return c;
    }

    /**
     * Retourne une capture d'écran de la zone actuellement gérée.
     * En cas d'erreur, retourne
     * <code>null</code>.
     *
     * @return
     */
    public Image snapshots() {
        Image image = null;
        Container zone = getComponent();

        if (zone != null) {
            try {
                Robot robot = new Robot();
                image = robot.createScreenCapture(new Rectangle(zone.getX(), zone.getY(), zone.getWidth(), zone.getHeight()));
            } catch (AWTException e) {
                LoggerManager.getInstance().addError(e);
            }
        }
        return image;
    }

    /**
     * Retourne les graphismes suivant le contexte de l'écran.
     * Si il y a une erreur (par exemple le mode plein écran inactif),
     * retourne
     * <code>null</code>.
     * <p>
     * Ne pas oublier de détruire les graphismes.
     *
     * @return Graphics2D or <code>null</code>.
     */
    public Graphics2D getGraphics() {
        Graphics2D g = null;
        BufferStrategy bStrategy = null;

        if (fullScreenWindow) {
            // Si on est en plein écran, nous récupèrons la frame
            Window w = getCurrentWindow();

            if (w != null) {
                bStrategy = w.getBufferStrategy();
            }
        } else if (component != null) {
            bStrategy = component.getBufferStrategy();
        }

        // Si le buffer est prêt, nous utilisons AWT pour le rafraichissement hardware
        if (bStrategy != null) {
            try {
                g = (Graphics2D) bStrategy.getDrawGraphics();
            } catch (Exception ex) {
                LoggerManager.getInstance().addError(ex);
            }
        }
        return g;
    }

    /**
     * Provoque une mise à jour de l'écran.
     */
    public void update() {
        BufferStrategy bStrategy = null;

        if (fullScreenWindow) {
            Window w = getCurrentWindow();

            if (w != null) {
                bStrategy = w.getBufferStrategy();
            }
        } else if (component != null) {
            bStrategy = component.getBufferStrategy();
        }

        // Si le buffer n'a pas été perdu
        if (bStrategy != null
            && !bStrategy.contentsLost()) {
            // On lit le buffer suivant ou on change le pointeur de l'affichage
            // Méthode de blitting ou de flipping
            // Envoie toutes les données du buffer mémoire vers le buffer d'affichage
            bStrategy.show();
        }

        // Synchronisation de l'écran vers le système.
        // Cette méthode s'assure que l'écran est a jour.
        // Elle corrige également un problème sous Linux (event queue)
        Toolkit.getDefaultToolkit().sync();
    }

    /**
     * Retourne la largueur de l'écran.
     *
     * @return
     */
    public int getWidth() {
        int width = 0;

        // Si on est en plein écran
        if (fullScreenWindow) {
            // On récupère la frame
            Window w = getCurrentWindow();

            // Si la frame est utilisable
            if (w != null) {
                // On retourne la largueur de la fenêtre
                width = w.getWidth();
            }
        } else if (component != null) {
            // Sinon, on retourne la largueur du composant
            width = component.getWidth();
        }
        return width;
    }

    /**
     * Retourne la hauteur de l'écran.
     *
     * @return
     */
    public int getHeight() {
        int height = 0;

        // Si on est en plein écran
        if (fullScreenWindow) {
            // On récupère la frame
            Window w = getCurrentWindow();

            // Si la frame est utilisable
            if (w != null) {
                // On retourne la hauteur de la fenêtre
                height = w.getHeight();
            }
        } else if (component != null) {
            // Sinon, on retourne la largueur du composant
            height = component.getHeight();
        }
        return height;
    }

    /**
     * Restaure l'écran si besoin.
     */
    public void restoreScreen() {
        boolean success = false;

        if (fullScreenWindow) {
            // Fin du rendu hardware: récupération de la fenêtre en plein écran
            Window w = getCurrentWindow();

            // Reconfiguration de la fenêtre
            if (w != null) {
                w.setVisible(false);
                w.dispose();

                synchronized (strategy) {
                    DEVICES[deviceNumber].setFullScreenWindow(null);
                }

                if (w instanceof Frame frame) {
                    frame.setUndecorated(false);
                }

                w.setIgnoreRepaint(false);

                if (w instanceof Frame frame) {
                    frame.setResizable(true);
                }

                w.invalidate();
                w.setVisible(true);
                w.repaint();

                fireHardwareStrategy();

                success = true;
            }

            // Signal la sortie du plein écran
            fullScreenWindow = false;

            fireHardwareStrategy();

            if (success) {
                LoggerManager.getInstance().addDebug("Switching to windowed mode"
                                                     + " for frame " + (w != null ? w.getName() : "") + ".");
            }
        }
    }

    /**
     * Exécute la stratégie actuellement configurée.
     */
    private void fireHardwareStrategy() {
        try {
            EventQueue.invokeAndWait(new HardwareBufferStrategy());
        } catch (InterruptedException | InvocationTargetException ex) {
            LoggerManager.getInstance().addError(ex);
        }
    }

    /**
     * Retourne le nombre de méga octet (Mb) disponible pour la carte graphique.
     *
     * @return MegaBytes available
     */
    public int getAvailableAcceleratedMemory() {
        synchronized (strategy) {
            // Calcule le nombre de Méga Octets libres dans la carte graphique
            return DEVICES[deviceNumber].getAvailableAcceleratedMemory() / 1048576;
        }
    }

    @Override
    public String getInformation() {
        return ("Device=" + deviceNumber + " Component=" + (component != null ? component.getName() : "null") + " Fullscreen=" + (fullScreenWindow ? "On" : "Off"));
    }

    @Override
    public void createProcess() {
    }

    @Override
    public void destroyProcess() {
        if (strategy.running()) {
            strategy.stop();
        }
    }

    /**
     * Rendu par la carte graphique.
     * - Tentative d'augmentation du buffer.
     * - Nous crééons une nouvelle stratégie de buffer.
     * - Le buffer défini une nouvelle taille dans la VRAM.
     */
    private class HardwareBufferStrategy implements Runnable {

        @Override
        public void run() {
            /*
             * Nombre de couche pour le buffer.
             * Il est recommandé d'utiliser le double-buffering (deux couches).
             * Au maximum trois couches possibles (triple-buffering)
             * mais il est possible d'avoir des ralentissements...
             */
            if (fullScreenWindow) {
                Window w = getCurrentWindow();

                if (w != null) {
                    if (!w.getIgnoreRepaint()) {
                        w.setIgnoreRepaint(true);
                    }

                    try {
                        w.createBufferStrategy(2);
                    } catch (Exception ex) {
                        LoggerManager.getInstance().addError(ex);
                    }

                    // Mise à jour du gestionnaire graphique
                    setGraphicsConfiguration(w.getGraphicsConfiguration());
                }
            } else {
                if (component != null) {
                    if (!component.getIgnoreRepaint()) {
                        component.setIgnoreRepaint(true);
                    }

                    try {
                        component.createBufferStrategy(2);
                    } catch (Exception ex) {
                        LoggerManager.getInstance().addError(ex);
                    }

                    // Mise à jour du gestionnaire graphique
                    setGraphicsConfiguration(component.getGraphicsConfiguration());
                }
            }
        }
    }

    /**
     * Détection du changement d'écran.
     */
    private class DeviceStrategy implements ServiceProcess, ComponentListener {

        private volatile boolean locked = false;

        private long lastEventTimestamp = 0;

        @Override
        public void run() {
            locked = true;

            if (!fullScreenWindow) {
                // Attente de la fin du mouvement de l'application
                while (lastEventTimestamp > System.currentTimeMillis()) {
                    if (!locked) {
                        // Dévrouillage forcé par une action exterieur
                        break;
                    }
                    FactoryManager.getInstance(PerformanceMeasurement.class).optimizedSleep();
                }

                setDeviceNumber();
            }

            locked = false;
        }

        @Override
        public void componentResized(ComponentEvent e) {
            lastEventTimestamp = System.currentTimeMillis() + 500;
            start();
        }

        @Override
        public void componentMoved(ComponentEvent e) {
            lastEventTimestamp = System.currentTimeMillis() + 500;
            start();
        }

        @Override
        public void componentShown(ComponentEvent e) {
            locked = false;
        }

        @Override
        public void componentHidden(ComponentEvent e) {
            locked = true;
        }

        @Override
        public void start() {
            if (!locked) {
                locked = true;

                Thread process = new ThreadHolderTask(this);
                process.setPriority(Thread.MIN_PRIORITY);
                process.start();
            }
        }

        @Override
        public void stop() {
            locked = false;
        }

        @Override
        public boolean running() {
            return !locked;
        }
    }
}
