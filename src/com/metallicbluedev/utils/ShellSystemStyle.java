package com.metallicbluedev.utils;

import com.metallicbluedev.logger.LoggerManager;
import java.awt.*;
import javax.swing.*;

/**
 * Récupère le style graphique de l'OS courant.
 * Très utile pour être cohérent dans toutes les fenêtres.
 *
 * @version 2.01.00
 * @author Sebastien Villemain
 */
public class ShellSystemStyle {

    /**
     * Détermine si le style graphique du système a été correctement chargé.
     */
    public static final boolean SHELL_SYSTEM_STYLE_LOADED = loadSystemLookAndFeel();

    /**
     * L'icone d'erreur.
     */
    public static final Image ERROR_ICON = getIcon(JOptionPane.ERROR_MESSAGE);

    /**
     * L'icone d'alerte.
     */
    public static final Image WARNING_ICON = getIcon(JOptionPane.WARNING_MESSAGE);

    /**
     * L'icone d'information.
     */
    public static final Image INFORMATION_ICON = getIcon(JOptionPane.INFORMATION_MESSAGE);

    /**
     * L'icone de question.
     */
    public static final Image QUESTION_ICON = getIcon(JOptionPane.QUESTION_MESSAGE);

    private ShellSystemStyle() {
        // NE RIEN FAIRE
    }

    /**
     * Charge le style graphique de l'OS courant.
     *
     * @return boolean succes.
     */
    public static boolean loadSystemLookAndFeel() {
        boolean loaded = false;

        if (!SHELL_SYSTEM_STYLE_LOADED) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                loaded = true;
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
                LoggerManager.getInstance().addWarning(e.getMessage());
            }
        } else {
            loaded = true;
        }
        return loaded;
    }

    /**
     * Retourne l'image de l'icone liée au type de message.
     *
     * @param iconType
     * @return
     */
    private static Image getIcon(int iconType) {
        Image icon = null;

        try {
            JOptionPane systemIcon = new JOptionPane("", iconType);

            for (int i = 0; i < systemIcon.getComponentCount(); i++) {
                icon = getComponentImage(systemIcon.getComponent(i));

                if (icon != null) {
                    break;
                }
            }
        } catch (Exception e) {
            icon = null;
        }

        if (icon == null) {
            icon = ImageHelper.createBufferedImage();
        }
        return icon;
    }

    /**
     * Retourne l'image provenant de l'icone d'un label.
     *
     * @param c
     * @return
     */
    private static Image getComponentImage(Component c) {
        Image icon = null;

        switch (c) {
            case JLabel label -> {
                if (label.getIcon() != null) {
                    icon = ImageHelper.toBufferedImage(label.getIcon());
                }
            }
            case JPanel panel -> {
                for (int i = 0; i < panel.getComponentCount(); i++) {
                    icon = getComponentImage(panel.getComponent(i));

                    if (icon != null) {
                        break;
                    }
                }
            }
            default -> {
            }
        }
        return icon;
    }
}
