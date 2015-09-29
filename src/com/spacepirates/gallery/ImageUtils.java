/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spacepirates.gallery;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.imageio.stream.FileCacheImageInputStream;

/**
 *
 * @author scott
 */
public class ImageUtils {

  /*
  public BufferedImage cropToSquare(ImageInfo image) {
  BufferedImage croppedImage;
  if (fullWidth > fullHeight) {
  final int diffX = fullWidth - fullHeight;
  thumbnailImage =
  bImage.getSubimage(diffX / 2, 0, fullWidth - diffX, fullHeight);
  } else if (fullHeight > fullWidth) {
  final int diffY = fullHeight - fullWidth;
  thumbnailImage =
  bImage.getSubimage(0, diffY / 2, fullWidth, fullHeight - diffY);
  } else { // Image is already square
  thumbnailImage = bImage;
  }
  }
   */
  /**
   * Scale an image.
   * @param image the image info wrapping the image
   * @param bImage the buffered image
   * @param width the scaled width
   * @param height the scaled height
   * @return the image info for the scaled image
   */
  public final ImageInfo scaleImage(final ImageInfo image,
    final BufferedImage bImage,
    final Integer width, final Integer height) {

    final ImageInfo scaledImage = new ImageInfo();

    try {
      final int fullWidth = bImage.getWidth();
      final int fullHeight = bImage.getHeight();
      if (fullWidth > width || fullHeight > height) {
        int preX, preY;
        Double pXd, pYd;
        // landscape
        if (fullWidth >= fullHeight && fullWidth > width) {
          preX = width;
          pYd = 1.0 * width * fullHeight / fullWidth;
          preY = pYd.intValue();
        } else if (fullHeight > height) {  //portrait
          preY = height;
          pXd = 1.0 * height * fullWidth / fullHeight;
          preX = pXd.intValue();
        } else { //square
          preX = fullWidth;
          preY = fullHeight;
        }

        scaledImage.setWidth(preX);
        scaledImage.setHeight(preY);

        final BufferedImage bScaledImage =
          this.getScaledBufferedImage(bImage, preX, preY);
        ImageIO.write(
          bScaledImage, image.getExtention(), scaledImage.getFile());
      } else {
        scaledImage.setFile(image.getFile());
        //scaledImage.setUrl(this.getImageFileURL(image));
      }

    } catch (IOException e) {
      //this.addMessage(e.getMessage());
    }
    return scaledImage;
  }

  /**
   * Scale an image.
   * @param bImage the image to be scaled
   * @param width scaled width
   * @param height scaled height
   * @return scaled BufferedImage
   */
  private BufferedImage getScaledBufferedImage(final BufferedImage bImage,
    final int width, final int height) {
    final Image scaledImage =
      bImage.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH);
    final BufferedImage bsImage =
      new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    final Graphics2D g2d = bsImage.createGraphics();
    g2d.drawImage(scaledImage, 0, 0, null);
    return bsImage;
  }

  /**
   * Rotate an image, saves the File.
   * @param imageInfo ImageInfo for the image file
   * @param degrees Degrees or rotation
   */
  public static void rotateImage(final ImageInfo imageInfo,
    final Integer degrees) {
    FileInputStream fis = null;
    BufferedImage inImage = null;
    BufferedImage outImage = null;
    try {

      fis = new FileInputStream(imageInfo.getFile());
      FileCacheImageInputStream iis = new FileCacheImageInputStream(fis, null);

      inImage = ImageIO.read(iis);
      int outWidth = inImage.getWidth();
      int outHeight = inImage.getHeight();
      if (degrees == Math.abs(90)) {
        outWidth = inImage.getHeight();
        outHeight = inImage.getWidth();
      }
      AffineTransform rotateTransform = AffineTransform.getRotateInstance(
        degrees * Math.PI / 180.0, inImage.getWidth() / 2.0, inImage.getHeight() / 2.0);
      AffineTransform translationTransform;
      translationTransform = findTranslation(rotateTransform, inImage);
      rotateTransform.preConcatenate(translationTransform);
      outImage = new BufferedImage(outWidth, outHeight, inImage.getType());
      Graphics2D g2d = (Graphics2D) outImage.getGraphics();

      g2d.drawImage(inImage, rotateTransform, null);
      g2d.dispose();
      ImageIO.write(outImage, "JPEG", imageInfo.getFile());

    } catch (IOException ex) {
    } finally {
      try {
        fis.close();
      } catch (IOException ex) {
      }
    }
  }

  /**
   * Generate a transformation of a buffered image, e.g. change height and
   * width.
   * @param aTransform initial transform
   * @param image the image to be transformed
   * @return the AffineTransform
   */
  private static AffineTransform findTranslation(
    final AffineTransform aTransform, final BufferedImage image) {
    Point2D p2din, p2dout;

    p2din = new Point2D.Double(0.0, 0.0);
    p2dout = aTransform.transform(p2din, null);
    final double ytrans = p2dout.getY();

    p2din = new Point2D.Double(0, image.getHeight());
    p2dout = aTransform.transform(p2din, null);
    final double xtrans = p2dout.getX();

    final AffineTransform tat = new AffineTransform();
    tat.translate(-xtrans, -ytrans);
    return tat;
  }
}
