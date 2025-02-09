package com.tr4ncer.utils;

import com.tr4ncer.core.*;
import com.tr4ncer.logger.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import javax.swing.*;

/**
 * Utilitaire de transformation sur les images.
 *
 * @version 1.03.01
 * @author Sebastien Villemain
 */
public class ImageHelper {

    private ImageHelper() {
        // NE RIEN FAIRE
    }

    /**
     * Retourne horizontalement une image (comme un miroire).
     *
     * @param image
     * @return
     */
    public static BufferedImage getMirrorImage(Image image) {
        BufferedImage bufferedImage = null;

        if (image != null) {
            AffineTransform transform = new AffineTransform();
            transform.scale(-1, 1);
            transform.translate(-1 * image.getWidth(null), 0);
            bufferedImage = transform(image, transform);
        }
        return bufferedImage;
    }

    /**
     * Retourne verticalement une image.
     *
     * @param image
     * @return
     */
    public static BufferedImage getFlippedImage(Image image) {
        BufferedImage bufferedImage = null;

        if (image != null) {
            AffineTransform transform = new AffineTransform();
            transform.scale(1, -1);
            transform.translate(0, -1 * image.getHeight(null));
            transform(image, transform);
        }
        return bufferedImage;
    }

    /**
     * Retourne le buffer d'une image.
     *
     * @param image
     * @return BufferedImage
     */
    public static BufferedImage toBufferedImage(Image image) {
        // Préparation du l'image bufferisée
        BufferedImage bufferedImage = null;

        if (image != null) {
            // Vérification avant calculs
            if (image instanceof BufferedImage bufferedImage1) {
                // L'image est déja une instance de BufferedImage
                bufferedImage = bufferedImage1;
            } else {
                // On crée la nouvelle image
                bufferedImage = createBufferedImage(image.getWidth(null), image.getHeight(null), Transparency.TRANSLUCENT);

                // Dessin de l'image final
                Graphics g = bufferedImage.getGraphics();
                g.drawImage(image, 0, 0, null);
                g.dispose();
            }
        }
        return bufferedImage;
    }

    /**
     * Retourne le buffer d'une image.
     *
     * @param icon
     * @return
     */
    public static BufferedImage toBufferedImage(Icon icon) {
        BufferedImage bufferedImage = null;

        if (icon != null) {
            if (icon instanceof ImageIcon imageIcon) {
                bufferedImage = toBufferedImage(imageIcon.getImage());
            } else {
                bufferedImage = createBufferedImage(icon.getIconWidth(), icon.getIconHeight(), Transparency.TRANSLUCENT);
                icon.paintIcon(null, bufferedImage.getGraphics(), 0, 0);
            }
        }
        return bufferedImage;
    }

    /**
     * Créé une nouvelle image bufferisée vide (image compatible).
     *
     * @param width The width.
     * @param height The height.
     * @param type The type of the image.
     * @return A compatible image.
     */
    private static BufferedImage createBufferedImage(int width, int height, int type) {
        return ScreenManager.getGraphicsConfiguration().createCompatibleImage(width, height, type);
    }

    /**
     * Créé une nouvelle image bufferisée vide transparente (image compatible).
     *
     * @param width
     * @param height
     * @return
     */
    public static BufferedImage createTranslucentBufferedImage(int width, int height) {
        return createBufferedImage(width, height, Transparency.TRANSLUCENT);
    }

    /**
     * Créé une nouvelle image bufferisée vide opaque (image compatible).
     *
     * @param width
     * @param height
     * @return
     */
    public static BufferedImage createOpaqueBufferedImage(int width, int height) {
        return createBufferedImage(width, height, Transparency.OPAQUE);
    }

    /**
     * Créé une nouvelle image bufferisée vide entièrement opaque ou transparente (image compatible).
     *
     * @param width
     * @param height
     * @return
     */
    public static BufferedImage createBitmaskBufferedImage(int width, int height) {
        return createBufferedImage(width, height, Transparency.BITMASK);
    }

    /**
     * Créé une nouvelle image bufferisée vide avec une taille par défaut (image compatible).
     *
     * @return
     */
    public static BufferedImage createBufferedImage() {
        return createTranslucentBufferedImage(10, 10);
    }

    /**
     * Redimensionne une image pour une taille exacte.
     *
     * @param image Image à redimensionner.
     * @param width Largeur de l'image cible.
     * @param height Hauteur de l'image cible.
     * @return Image redimensionnée.
     */
    public static BufferedImage scale(Image image, int width, int height) {
        BufferedImage bufferedImage = null;

        if (image != null) {
            int type;

            // Préparation de l'image bufferisée aux bonnes dimensions
            if (image instanceof BufferedImage bufferedImage1) {
                type = bufferedImage1.getType();
            } else {
                type = BufferedImage.TYPE_INT_ARGB;
            }

            bufferedImage = new BufferedImage(width, height, type);

            // On dessine sur le graphique de l'image bufferisée
            Graphics2D g = bufferedImage.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.drawImage(image, 0, 0, width, height, null);
            g.dispose();
        }
        return bufferedImage;
    }

    /**
     * Redimensionne une image en suivant les tailles recommandées.
     *
     * @param image
     * @param widthMax
     * @param heightMax
     * @return
     */
    public static BufferedImage scaleMax(Image image, int widthMax, int heightMax) {
        BufferedImage newImage = null;

        if (image != null) {
            if (widthMax > 0 && heightMax > 0) {
                int width = image.getWidth(null);
                int height = image.getHeight(null);

                double scaleX = (double) widthMax / width;
                double scaleY = (double) heightMax / height;
                double scale;

                if (scaleX < scaleY) {
                    scale = scaleX;
                } else {
                    scale = scaleY;
                }

                int newWidth = (int) (width * scale);
                int newHeight = (int) (height * scale);

                newImage = scale(image, newWidth, newHeight);
            }
        }

        if (newImage == null) {
            if (image instanceof BufferedImage bufferedImage) {
                newImage = bufferedImage;
            }
        }
        return newImage;
    }

    /**
     * Redimensionne une image avec un coefficient.
     * Un coefficient inferieur à 1 réduit l'image,
     * alors qu'un coefficient supperieur à 1 l'agrandie.
     *
     * @param image
     * @param scale
     * @return
     */
    public static BufferedImage scale(Image image, float scale) {
        BufferedImage newImage = null;

        if (image != null) {
            int width = image.getWidth(null);
            int height = image.getHeight(null);

            int newWidth = (int) (width * scale);
            int newHeight = (int) (height * scale);

            newImage = scale(image, newWidth, newHeight);
        }
        return newImage;
    }

    /**
     * Vérifie si l'image contient des pixels transparent.
     *
     * @param image
     * @return
     */
    public static boolean hasAlpha(Image image) {
        boolean rslt = false;

        if (image != null) {
            if (image instanceof BufferedImage bimage) {
                rslt = bimage.getColorModel().hasAlpha();
            } else {
                // Use a pixel grabber to retrieve the image's color model;
                // grabbing a single pixel is usually sufficient
                PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);

                try {
                    pg.grabPixels();
                    rslt = pg.getColorModel().hasAlpha();
                } catch (InterruptedException e) {
                    LoggerManager.getInstance().addError(e);
                }
            }
        }
        return rslt;
    }

    /**
     * Vérifie si l'image contient un pixel transparent aux coordonnées précisées.
     *
     * @param image
     * @param x
     * @param y
     * @param alphaMaxLevel Seuil maximum de tolèrence (de 0 à 255).
     * @return
     */
    public static boolean hasAlphaAt(Image image, int x, int y, int alphaMaxLevel) {
        boolean rslt = false;

        if (image != null) {
            if (image instanceof BufferedImage bimage) {
                rslt = bimage.getAlphaRaster().getSample(x, y, 0) > alphaMaxLevel;
            }
        }
        return rslt;
    }

    /**
     * Transforme une image.
     *
     * @param image
     * @param transform
     * @return
     */
    public static BufferedImage transform(Image image, AffineTransform transform) {
        BufferedImage newImage = null;

        if (image != null) {
            newImage = createBufferedImage(image.getWidth(null), image.getHeight(null), Transparency.TRANSLUCENT);

            Graphics2D g = (Graphics2D) newImage.getGraphics();
            g.drawImage(image, transform, null);
            g.dispose();
        }
        return newImage;
    }
}
