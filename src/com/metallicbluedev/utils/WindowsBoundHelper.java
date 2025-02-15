package com.metallicbluedev.utils;

import com.metallicbluedev.core.*;
import java.awt.*;
import javax.swing.*;

/**
 *
 * @author Sébastien Villemain
 */
public class WindowsBoundHelper {

    /**
     * Taille par défaut de la fenêtre.
     */
    public static final Dimension DEFAULT_APPLICATION_DIMENSION = new Dimension(600, 500);

    /**
     * Taille de la fenêtre.
     */
    private Dimension applicationDimension = null;

    /**
     * Position de la fenêtre.
     */
    private Point applicationLocation = null;

    public WindowsBoundHelper() {

    }

    /**
     * Dimension configurée de la fenêtre.
     *
     * @return the applicationDimension.
     */
    public final Dimension getApplicationDimension() {
        return applicationDimension;
    }

    /**
     * Change la dimension configurée de la fenêtre.
     *
     * @param applicationDimension
     */
    public final void setApplicationDimension(Dimension applicationDimension) {
        checkApplicationDimension(applicationDimension.width, applicationDimension.height);
    }

    /**
     * Change la dimension configurée de la fenêtre.
     *
     * @param windowWidth
     * @param windowHeight
     */
    public final void setApplicationDimension(int windowWidth, int windowHeight) {
        checkApplicationDimension(windowWidth, windowHeight);
    }

    /**
     * Configuration de base pour la fenêtre.
     *
     * @param frame
     */
    public final void configure(Frame frame) {
        if (applicationDimension != null) {
            frame.setSize(applicationDimension);

            // Vérifie si l'application est configurée en mode plein écran
            Rectangle display = ScreenManager.getMaximumBounds();

            // Maximise la fenêtre si besoin
            if (applicationDimension.width > (display.getWidth() - 50)
                || applicationDimension.height > (display.getHeight() - 50)) {
                frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            }
        }

        // Positionnement de la fenêtre
        if (applicationLocation != null) {
            frame.setLocation(applicationLocation);
        } else {
            frame.setLocationRelativeTo(null);
        }
    }

    /**
     * Vérifie et affecte une dimension valide à la fenêtre de l'application.
     *
     * @param windowWidth
     * @param windowHeight
     */
    private void checkApplicationDimension(int windowWidth, int windowHeight) {
        if (applicationDimension == null
            || applicationDimension.width != windowWidth
            || applicationDimension.height != windowHeight) {
            // Vérification de la taille minimum de la fenêtre
            if (windowWidth < 150 || windowHeight < 150) {
                windowWidth = DEFAULT_APPLICATION_DIMENSION.width;
                windowHeight = DEFAULT_APPLICATION_DIMENSION.height;
            }

            applicationDimension = new Dimension(windowWidth, windowHeight);
        }
    }

    /**
     * Position configurée de la fenêtre.
     * Si aucune position précisée, retourne
     * <code>null</code>.
     *
     * @return Point or <code>null</code>.
     */
    public final Point getApplicationLocation() {
        return applicationLocation;
    }

    /**
     * Change la position configurée de la fenêtre.
     *
     * @param applicationLocation
     */
    public final void setApplicationLocation(Point applicationLocation) {
        checkApplicationLocation(applicationLocation.x, applicationLocation.y);
    }

    /**
     * Change la position configurée de la fenêtre.
     *
     * @param windowX
     * @param windowY
     */
    public final void setApplicationLocation(int windowX, int windowY) {
        checkApplicationLocation(windowX, windowY);
    }

    /**
     * Vérifie et affecte une position valide à la fenêtre.
     *
     * @param windowX
     * @param windowY
     */
    private void checkApplicationLocation(int windowX, int windowY) {
        if (applicationLocation == null
            || applicationLocation.x != windowX
            || applicationLocation.y != windowY) {
            Rectangle display = ScreenManager.getMaximumBounds();

            if ((windowX + (applicationDimension != null ? applicationDimension.width : 0)) > display.getWidth()) {
                windowX = 0;
            }

            if ((windowY + (applicationDimension != null ? applicationDimension.height : 0)) > display.getHeight()) {
                windowY = 0;
            }

            if (windowX > 0 && windowY > 0) {
                applicationLocation = new Point(windowX, windowY);
            } else {
                applicationLocation = null;
            }
        }
    }
}
