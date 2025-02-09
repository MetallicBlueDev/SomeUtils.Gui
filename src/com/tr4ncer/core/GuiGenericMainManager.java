package com.tr4ncer.core;

import com.tr4ncer.utils.*;
import javax.swing.*;

/**
 *
 * @author Sébastien Villemain
 */
public abstract class GuiGenericMainManager extends GenericMainManager {

    @Override
    protected void onStartBeginning() {
        // Chargement du style graphique
        ShellSystemStyle.loadSystemLookAndFeel();
    }

    @Override
    public String getInformation() {
        StringBuilder builder = new StringBuilder();
        builder.append(super.getInformation());

        builder.append(StringHelper.LINE_SEPARATOR);

        builder.append("Look and feel loaded: ");
        builder.append(UIManager.getSystemLookAndFeelClassName());
        builder.append(StringHelper.LINE_SEPARATOR);

        return builder.toString();
    }

}
