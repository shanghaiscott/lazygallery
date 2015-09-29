/*
 * ImageGallery.java
 *
 * Created on December 4, 2004, 9:07 PM
 *
 * Copyright: Scott Douglass <scott@swdouglass.com>.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * on the World Wide Web for more details:
 * http://www.fsf.org/licensing/licenses/gpl.txt
 *
 */
package com.spacepirates.gallery;

import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.metadata.MetadataException;
import com.spacepirates.swing.chooser.ImageNameFilter;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.imageio.stream.FileCacheImageInputStream;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.awt.Image;
import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.Collections;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifDirectory;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Logger;

/**
 *
 * @author scott
 */
public class ImageGallery implements java.io.Serializable {

  /** Logger for this class. */
  private static final Logger LOG =
    Logger.getLogger(ImageGallery.class.getName());
  /** Serialization UUID. */
  private static final long serialVersionUID = -8070937702768746717L;
  /** Default feed image x dimension: 72. */
  private static final Integer D_FEEDX = 72;
  /** Default feed image y dimension: 72. */
  private static final Integer D_FEEDY = 72;
  /** Default thumbnail x dimension: 120. */
  private static final Integer D_THUMBX = 120;
  /** Default thumbnail y dimension: 120. */
  private static final Integer D_THUMBY = 120;
  /** Default preview x dimension: 800. */
  private static final Integer D_PREX = 800;
  /** Default preview y dimension: 600. */
  private static final Integer D_PREY = 600;
  /** Default number of thumbnail images per row. */
  private static final Integer D_THUMBS = 6;
  /** Map of images. */
  private Map<String, ImageInfo> images =
    new LinkedHashMap<String, ImageInfo>();
  /** Width of feed image. */
  private int feedX = D_FEEDX;
  /** Height of feed image. */
  private int feedY = D_FEEDY;
  /** Width of thumbnail image. */
  private int thumbnailX = D_THUMBX;
  /** Height of thumbnail image. */
  private int thumbnailY = D_THUMBY;
  /** Height of preview image. */
  private int previewX = D_PREX;
  /** Width of preview image. */
  private int previewY = D_PREY;
  /** Root directory of webapp for images. */
  private String galleryRoot = "/images";
  /** Root directory for this gallery. */
  private String galleryRootPath;
  /** Root directory for the images in the gallery. */
  private String imageDirName;
  /** Full path of the root directory for images in the gallery. */
  private String imageDirPath;
  /** Name of the thumbnail image directory. */
  private String thumbDirName = "/thumbnails/";
  /** Full path of the thumbnail image directory. */
  private String thumbDirPath;
  /** Name of the preview image directory. */
  private String previewDirName = "/previews/";
  /** Full path of the preview image directory. */
  private String previewDirPath;
  /** Name of the feed directory. */
  private String feedDirName = "/feed/";
  /** Full path of the feed directory. */
  private String feedDirPath;
  /**
   * Number of thumbnails per row in gallery grid.
   * FIXME: remove this presentation attribute.
   */
  private int thumbsPerRow = D_THUMBS;
  /** List of messages to display in galley. */
  private final List<String> messages = new ArrayList<String>();
  /** ImageNameFilter for selecting only images. */
  private static ImageNameFilter inf = new ImageNameFilter();
  /** System file separator characters. */
  public static final String FILE_SEP = System.getProperty("file.separator");
  /** Flag to identify if gallery is modified. */
  private Boolean modified = false;
  /** Flag to identify if there is an RSS feed for this gallery. */
  private Boolean feed = false;
  /** List of image processing threads. */
  private final List<Thread> threads = new ArrayList<Thread>();
  /** Flag to set use of lower case or all image file names. */
  private Boolean lowerCaseNames = false;
  /**  Allow canceling image generation run from the GUI. */
  private static volatile boolean stopRequested = false;
  /** Flag to turn on rotation of images based on EXIF data. */
  private Boolean rotateImages = false;

  /**
   * Create an image gallery.
   * @param baseDir name of images directory relative to gallery root
   * @param thumbs number of thumbnails per row
   */
  public ImageGallery(final String baseDir, final int thumbs) {
    this.init(baseDir, thumbs, false, false);
  }

  /**
   * Create an image gallery.
   * @param baseDir name of images directory
   * @param thumbs numbe of thumbnails per row
   * @param feedThumbnails generate feed thumbnails if true
   * @param useThreads use multiple threads if true
   */
  public ImageGallery(final String baseDir, final int thumbs,
    final Boolean feedThumbnails, final Boolean useThreads) {
    this.init(baseDir, thumbs, feedThumbnails, useThreads);
  }

  /**
   * Create the gallery.
   * @param baseDir image directory
   * @param thumbs number of thumbnails per row
   */
  public final void init(final String baseDir, final int thumbs) {
    feed = false;
    this.init(baseDir, thumbs, false, false);
  }

  /**
   * Creates a new instance of ImageGallery.
   * @param baseDir Starting point for location of images.
   * @param thumbs Number of thumbnails per row to display.
   * @param useThreads if true, use threads
   * @param feedThumbnails if true, generate feed thumbnails
   */
  public final void init(final String baseDir, final int thumbs,
    final Boolean feedThumbnails, final Boolean useThreads) {
    feed = feedThumbnails;
    galleryRootPath = baseDir.substring(
      0, baseDir.indexOf(galleryRoot)) + galleryRoot;
    imageDirPath = baseDir;
    imageDirName = baseDir.substring(
      baseDir.lastIndexOf(galleryRoot));
    thumbDirPath = baseDir + thumbDirName;
    previewDirPath = baseDir + previewDirName;
    thumbsPerRow = thumbs;

    feedDirPath = baseDir + feedDirName;

    messages.add("Creating gallery for directory: " + baseDir);
    messages.add("Root directory: " + galleryRoot);
    messages.add("Image directory: " + imageDirPath);
    messages.add("Preview directory: " + previewDirPath);
    messages.add("Thumbnail directory: " + thumbDirPath);
    messages.add("Feed thumbnail directory: " + feedDirPath);

    if (lowerCaseNames) {
      this.lowerCaseFileNames(baseDir);
    }
    // get list of images in baseDir
    this.listImages(baseDir);

    // generate thumbnails and previews for images in baseDir
    if (useThreads) {
      final Thread tmanager = new Thread(new TaskManager(), "taskmanager");
      tmanager.start();
    } else {
      this.createPreviewsAndThumbnails();
      this.removeOrphans(thumbDirPath);
      this.removeOrphans(previewDirPath);

      if (feed) {
        this.removeOrphans(feedDirPath);
      }
      this.addBlankThumbnails();
    }
  }

  /** Default constructor. */
  public ImageGallery() {
    // For using ImageGallery as a Java bean.
  }

  /**
   * Convert all filenames to lower case, recursively.
   * @param baseDir the directory to start in
   */
  private void lowerCaseFileNames(final String baseDir) {
    final File root = new File(baseDir);
    if (root.exists()) {
      final File[] files = root.listFiles(inf);
      for (int i = 0; i < files.length; i++) {
        files[i].setExecutable(false, false);
        if (!files[i].renameTo(new File(baseDir + FILE_SEP
          + files[i].getName().toLowerCase()))) {
          LOG.warning("Rename failed for: ".concat(files[i].getName()));
        }
      }
    }
  }

  /**
   * List images in a directory, save in messages.
   * @param baseDir the root directory
   */
  private void listImages(final String baseDir) {
    final File root = new File(baseDir);
    if (root.exists()) {
      final File[] files = root.listFiles(inf);
      final List<File> fileList = Arrays.asList(files);
      Collections.sort(fileList);
      this.addMessage("Number of image files: " + fileList.size());
      for (File file : fileList) {
        this.addMessage("Found image: " + file.getName());
        this.addImage(file.getName(), new ImageInfo(file));
      }
    } else {
      this.addMessage("No such folder: " + baseDir);
    }
  }

  /**
   * Remove images in a directory which are not known to be part of the gallery.
   * @param dirPath the root directory
   */
  private void removeOrphans(final String dirPath) {
    final File root = new File(dirPath);
    final File[] allFiles = root.listFiles(inf);
    if (allFiles != null) {
      for (int i = 0; i < allFiles.length; i++) {
        if (!images.containsKey(allFiles[i].getName())) {
          if (!allFiles[i].delete()) {
            LOG.warning("Delete failed: ".concat(allFiles[i].getName()));
          }
          if (!isModified()) {
            setModified(true);
          }
        }
      }
    }
  }

  /**
   * Class that manages checks if threads are alive.
   */
  private class TaskManager implements Runnable {

    /** Time to wait before checking for thread completion. */
    private final Integer pollInterval = 2000;

    @Override
    public void run() {
      divideAndConquer2();
      int score = threads.size();
      while (score > 0) {
        try {
          Thread.sleep(pollInterval);
        } catch (InterruptedException e) {
          //
        }
        for (Thread thread : threads) {
          if (!thread.isAlive()) {
            score--;
          }
        }
      }
      createPreviewsAndThumbnails();
      removeOrphans(getThumbDirPath());
      removeOrphans(getPreviewDirPath());

      if (feed) {
        removeOrphans(getFeedDirPath());
      }
      addBlankThumbnails();
    }
  }

  /**
   * Process photos in parallel using all available CPUs.
   */
  private void divideAndConquer2() {
    final Runtime runtime = Runtime.getRuntime();
    divideAndConquer2(runtime.availableProcessors());
  }

  /**
   * Process photos in parallel.
   * @param cpus the number of CPUs to utilize
   */
  private void divideAndConquer2(final Integer cpus) {
    final Object[] original = this.getImages().values().toArray();
    final int setsize = original.length / cpus;
    int j = 0;
    for (int i = 1; i <= cpus; i++) {
      final Collection set = new ArrayList();
      int realsize = setsize * i;
      if (i == cpus) {
        realsize = realsize + (original.length % cpus);
      }
      for (; j < realsize; j++) {
        set.add(original[j]);
      }
      final Runner runner = new Runner(set);
      final Thread thread = new Thread(runner, "lazygallery" + i);
      thread.setDaemon(true);
      synchronized (threads) {
        threads.add(thread);
      }

      thread.start();
    }
  }

  /**
   * In most cases, the Runnable interface should be used if you are only
   * planning to override the run() method and no other Thread methods.
   * This is important because classes should not be subclassed unless the
   * programmer intends on modifying or enhancing the fundamental
   * behavior of the class.
   */
  private class Runner implements Runnable {

    /** Collection of ImageInfos. */
    private Collection<ImageInfo> rImages;

    /**
     * Create a Runner.
     * @param inRImages the ImageInfos to be processed
     */
    public Runner(final Collection<ImageInfo> inRImages) {
      rImages = inRImages;
    }

    @Override
    public void run() {
      createPreviewsAndThumbnails(rImages);
    }

    /**
     * Get the ImageInfo collection.
     * @return the ImageInfo collection
     */
    public Collection<ImageInfo> getRImages() {
      return rImages;
    }

    /**
     * Set the ImageInfo collection.
     * @param inRImages ImageInfo collection to process
     */
    public void setRImages(final Collection<ImageInfo> inRImages) {
      rImages = inRImages;
    }
  }

  /**
   * Create the preview and thumbnail images.
   */
  private void createPreviewsAndThumbnails() {
    this.createPreviewsAndThumbnails(this.getImages().values());
  }

  /**
   * Create previews and thumbnails for Collection of ImageInfos.
   * @param inImages the collection of ImageInfos
   */
  private void createPreviewsAndThumbnails(
    final Collection<ImageInfo> inImages) {
    final File previewDir = new File(this.getPreviewDirPath());
    if (!previewDir.exists() && !previewDir.mkdir()) {
      LOG.warning("Create directory failed: ".concat(previewDir.getName()));
    }

    final File thumbnailDir = new File(this.getThumbDirPath());
    if (!thumbnailDir.exists() && !thumbnailDir.mkdir()) {
      LOG.warning("Create directory failed: ".concat(thumbnailDir.getName()));
    }

    if (feed) {
      final File feedDir = new File(this.getFeedDirPath());
      if (!feedDir.exists() && !feedDir.mkdir()) {
        LOG.warning("Create directory failed: ".concat(feedDir.getName()));
      }
    }

    if (getRotateImages()) {
      this.exifAndRotation(inImages);
    }

    for (ImageInfo imageInfo : inImages) {
      if (isStopRequested()) {
        break;
      }
      BufferedImage bImage = null;
      try {
        final FileInputStream fis = new FileInputStream(imageInfo.getFile());
        final FileCacheImageInputStream iis =
          new FileCacheImageInputStream(fis, null);

        ImageInfo previewInfo = new ImageInfo(
          new File(this.getPreviewDirPath() + imageInfo.getFile().getName()));
        previewInfo.setUrl(this.getPreviewFileURL(imageInfo));
        // if the preview does not already exist, create it.
        if (!previewInfo.getFile().exists()) {
          // we're creating a new preview, so mark the gallery as modified
          if (!isModified()) {
            setModified(true);
          }
          bImage = ImageIO.read(iis);
          createPreview(imageInfo, previewInfo, bImage);
        }
        imageInfo.setPreviewInfo(previewInfo);
        //System.out.println("Thread: " + Thread.currentThread().getName()
        //+ " ImageInfo: " + previewInfo.getFilename());

        final ImageInfo thumbInfo = new ImageInfo(
          new File(thumbDirPath + imageInfo.getFile().getName()));
        if (!thumbInfo.getFile().exists()) {
          if (bImage == null) {
            bImage = ImageIO.read(iis);
          }
          createThumbnail(imageInfo, thumbInfo, bImage);
        }
        imageInfo.setThumbnailInfo(thumbInfo);

        if (feed) {
          final ImageInfo feedInfo = new ImageInfo(new File(this.feedDirPath
            + imageInfo.getFile().getName()));
          if (!feedInfo.getFile().exists()) {
            if (bImage == null) {
              bImage = ImageIO.read(iis);
            }
            createThumbnail(imageInfo, feedInfo, bImage, this.getFeedX(),
              this.getFeedY());
          }
        }

        createExifFile(imageInfo);

      } catch (IOException e) {
        this.addMessage(e.getMessage());
      }
    }
  }

  /**
   * Check the EXIF data for orientation, and rotate image if necessary.
   * http://www.impulseadventure.com/photo/exif-orientation.html
   * http://www.drewnoakes.com/code/exif/sampleUsage.html
   * http://forums.sun.com/thread.jspa?threadID=5178452
   * file:///opt/java/sdk/se/1.6.0-docs/api/index.html
   * http://www.java2s.com/Code/Java/2D-Graphics-GUI/RotateImage45Degrees.htm
   * http://math.rice.edu/~pcmi/sphere/drg_txt.html
   * @param rimages the collection of rimages
   */
  private void exifAndRotation(final Collection<ImageInfo> rimages) {
    for (ImageInfo image : rimages) {
      if ((image.getExtention().equals(ImageNameFilter.JPEG)
        || image.getExtention().equals(ImageNameFilter.JPG))) {
        try {
          final Metadata metadata =
            JpegMetadataReader.readMetadata(image.getFile());
          final Directory exifDirectory =
            metadata.getDirectory(ExifDirectory.class);
          final int orientation =
            exifDirectory.getInt(ExifDirectory.TAG_ORIENTATION);
          if (orientation == 6) { // rotate 90 degress CW
            ImageUtils.rotateImage(image, 90);
          } else if (orientation == 8) { // rotate 90 degrees CCW (-90)
            ImageUtils.rotateImage(image, -90);
          } else if (orientation == 3) { // roate 180 degrees CW
            ImageUtils.rotateImage(image, 180);
          }
        } catch (MetadataException ex) {
        } catch (JpegProcessingException ex) {
        }
      }
    }
  }
  /**
   * This method generates the preview images. If the original image is smaller
   * than the preview image dimensions, just set the URL for the preview to
   * point to the original image file and don'tmanager create a preview image.
   * @param image the ImageInfo
   * @param previewInfo the preview ImageInfo
   * @param bImage a buffered image
   * @return the ImageInfo
   */
  private ImageInfo createPreview(final ImageInfo image,
    final ImageInfo previewInfo, final BufferedImage bImage) {
    try {
      final int fullWidth = bImage.getWidth();
      final int fullHeight = bImage.getHeight();
      if (fullWidth > this.getPreviewX() || fullHeight > this.getPreviewY()) {
        int preX, preY;
        Double pXd, pYd;
        // landscape
        if (fullWidth >= fullHeight && fullWidth > this.getPreviewX()) {
          preX = this.getPreviewX();
          pYd = 1.0 * this.getPreviewX() * fullHeight / fullWidth;
          preY = pYd.intValue();
        } else if (fullHeight > this.getPreviewY()) {  //portrait
          preY = this.getPreviewY();
          pXd = 1.0 * this.getPreviewY() * fullWidth / fullHeight;
          preX = pXd.intValue();
        } else { //square
          preX = fullWidth;
          preY = fullHeight;
        }

        previewInfo.setWidth(preX);
        previewInfo.setHeight(preY);

        final BufferedImage scaledImage =
          this.getScaledBufferedImage(bImage, preX, preY);
        ImageIO.write(scaledImage, image.getExtention(), previewInfo.getFile());
      } else {
        previewInfo.setFile(image.getFile());
        previewInfo.setUrl(this.getImageFileURL(image));
      }

    } catch (IOException e) {
      this.addMessage(e.getMessage());
      System.out.println("error: " + e);
      //e.printStackTrace();
    }
    return previewInfo;
  }

  /**
   * Create a single thumbnail image using default thumbnail size.
   * @param imageInfo the full size image info
   * @param thumbInfo the thumbnail info
   * @param bImage a BufferedImage
   * @return the thumbnail info
   */
  private ImageInfo createThumbnail(final ImageInfo imageInfo,
    final ImageInfo thumbInfo, final BufferedImage bImage) {
    return createThumbnail(imageInfo, thumbInfo, bImage, this.getThumbnailX(),
      this.getThumbnailY());
  }

  /**
   * Create a single thumbnail image of the specified dimensions.
   * @param image the full size image info
   * @param thumbInfo the thumbnail image info
   * @param bImage the BufferedImage
   * @param inThumbnailX the width
   * @param inThumbnailY the height
   * @return the thumbnail image info
   */
  private ImageInfo createThumbnail(final ImageInfo image,
    final ImageInfo thumbInfo, final BufferedImage bImage,
    final Integer inThumbnailX, final Integer inThumbnailY) {
    try {
      final int fullWidth = bImage.getWidth();
      final int fullHeight = bImage.getHeight();
      // save this metadata with the original ImageInfo object
      image.setWidth(fullWidth);
      image.setHeight(fullHeight);

      // now build the thumbnail image by first cropping it square, and then
      // scaling it.
      BufferedImage thumbnailImage;
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

      thumbnailImage = this.getScaledBufferedImage(thumbnailImage,
        inThumbnailX, inThumbnailY);

      ImageIO.write(thumbnailImage, image.getExtention(), thumbInfo.getFile());
    } catch (IOException e) {
      this.addMessage(e.getLocalizedMessage());
    }

    return thumbInfo;
  }

  /**
   * Create the .exif file from the image info.
   * @param image to be processed
   */
  private void createExifFile(final ImageInfo image) {
    final File exifFile = new File(this.getPreviewDirPath()
      + image.getFile().getName() + ".exif");

    if (exifFile.exists()) {
      image.setExifPresent(true);
    } else {
      final File noExifFile = new File(this.getPreviewDirPath()
        + image.getFile().getName() + ".noexif");
      if (!noExifFile.exists()
         &&  (image.getExtention().equals(ImageNameFilter.JPEG)
         || image.getExtention().equals(ImageNameFilter.JPG))) {
        try {
          Writer output = new BufferedWriter(new FileWriter(exifFile));
          // Extract any EXIF metadata to a file
          Metadata metadata = JpegMetadataReader.readMetadata(image.getFile());
          if (metadata.getDirectoryCount() > 0) {
            image.setExifPresent(true);
            // iterate through metadata directories
            Iterator directories = metadata.getDirectoryIterator();

            while (directories.hasNext()) {
              Directory directory = (Directory) directories.next();
              // iterate through tags and print to System.out
              Iterator tags = directory.getTagIterator();
              while (tags.hasNext()) {
                Tag tag = (Tag) tags.next();
                output.write(tag + "\n");
              }
            }
            output.close();

          } else {
            output = new BufferedWriter(new FileWriter(noExifFile));
            output.write("No EXIF data for this image");
            output.close();
          }

        } catch (Exception e) {
          System.out.println("error handling exif: " + e);
          //e.printStackTrace();
        }
      }
    }
  }

  private BufferedImage getScaledBufferedImage(BufferedImage bImage, int x, int y) {
    Image scaledImage = bImage.getScaledInstance(x, y, java.awt.Image.SCALE_SMOOTH);
    BufferedImage bsImage = new BufferedImage(x, y, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2 = bsImage.createGraphics();
    g2.drawImage(scaledImage, 0, 0, null);
    return bsImage;
  }

  /**
   * This method creates empty ImageInfos to aid in GUI loops.
   */
  private void addBlankThumbnails() {
    int blanksNeeded = 0;
    int numImages = this.getImages().size();
    int numThumbs = this.getThumbsPerRow();
    int remainder = numImages % this.getThumbsPerRow();
    while (remainder != 0) {
      blanksNeeded++;
      remainder = (numImages + blanksNeeded) % numThumbs;
    }

    for (int i = 0; i < blanksNeeded; i++) {
      ImageInfo blankInfo = new ImageInfo(null);
      blankInfo.setTitle("blank" + i);
      this.getImages().put("blank" + i + ".jpg", blankInfo);
    }
  }
  /**
   * @return the stopRequested
   */
  public static boolean isStopRequested() {
    return stopRequested;
  }

  /**
   * @param aStopRequested the stopRequested to set
   */
  public static void setStopRequested(boolean aStopRequested) {
    stopRequested = aStopRequested;
  }

  /**
   * @return the rotateImages
   */
  public Boolean getRotateImages() {
    return rotateImages;
  }

  /**
   * @param aRotateImages the rotateImages to set
   */
  public void setRotateImages(Boolean aRotateImages) {
    rotateImages = aRotateImages;
  }

  public Map<String, ImageInfo> getImages() {
    return images;
  }

  private void setImages(LinkedHashMap<String, ImageInfo> images) {
    this.images = images;
  }

  public int getThumbnailX() {
    return thumbnailX;
  }

  private void setThumbnailX(int thumbnailX) {
    this.thumbnailX = thumbnailX;
  }

  public int getThumbnailY() {
    return thumbnailY;
  }

  public void setThumbnailY(int thumbnailY) {
    this.thumbnailY = thumbnailY;
  }

  public int getPreviewX() {
    return previewX;
  }

  private void setPreviewX(int previewX) {
    this.previewX = previewX;
  }

  public int getPreviewY() {
    return previewY;
  }

  private void setPreviewY(int previewY) {
    this.previewY = previewY;
  }

  public String getThumbDirName() {
    return thumbDirName;
  }

  private void setThumbDirName(String thumbDirName) {
    this.thumbDirName = thumbDirName;
  }

  public String getThumbDirPath() {
    return thumbDirPath;
  }

  private void setThumbDirPath(String thumbDirPath) {
    this.thumbDirPath = thumbDirPath;
  }

  public String getPreviewDirName() {
    return previewDirName;
  }

  private void setPreviewDirName(String previewDirName) {
    this.previewDirName = previewDirName;
  }

  public String getPreviewDirPath() {
    return previewDirPath;
  }

  private void setPreviewDirPath(String previewDirPath) {
    this.previewDirPath = previewDirPath;
  }

  public List<String> getMessages() {
    return this.messages;
  }

  public String getPreviewFileURL(ImageInfo i) {
    return getFileURL(i, this.getPreviewDirName());
  }

  public String getThumbnailFileURL(ImageInfo i) {
    return getFileURL(i, this.getThumbDirName());
  }

  public String getFeedFileURL(ImageInfo i) {
    return getFileURL(i, this.getFeedDirName());
  }

  public String getImageFileURL(ImageInfo i) {
    return getFileURL(i, "/");
  }

  public String getFileURL(ImageInfo i, String extraPath) {
    StringBuilder sb = new StringBuilder();
    sb.append(this.getImageDirName());
    sb.append(extraPath);
    sb.append(i.getFilename());
    return sb.toString();
  }

  public String getMessage(int i) {
    return this.messages.get(i);
  }

  public int getThumbsPerRow() {
    return thumbsPerRow;
  }

  private void setThumbsPerRow(int thumbsPerRow) {
    this.thumbsPerRow = thumbsPerRow;
  }

  public String getGalleryRoot() {
    return this.galleryRoot;
  }

  public void setGalleryRoot(String inGalleryRoot) {
    this.galleryRoot = inGalleryRoot;
  }

  public String getImageDirName() {
    return imageDirName;
  }

  public void setImageDirName(String imageDirName) {
    this.imageDirName = imageDirName;
  }

  public String getGalleryRootPath() {
    return galleryRootPath;
  }

  public void setGalleryRootPath(String galleryRootPath) {
    this.galleryRootPath = galleryRootPath;
  }

  public void addMessage(String inMessage) {
    this.messages.add(inMessage);
  }

  public void addImage(String imageName, ImageInfo imageInfo) {
    this.images.put(imageName, imageInfo);
  }

  public String getImageDirPath() {
    return imageDirPath;
  }

  public void setImageDirPath(String imageDirPath) {
    this.imageDirPath = imageDirPath;
  }

  /**
   * @return the modified
   */
  public Boolean isModified() {
    return modified;
  }

  /**
   * @param modified the modified to set
   */
  public void setModified(Boolean isModified) {
    this.modified = isModified;
  }

  /**
   * @return the feedDirName
   */
  public String getFeedDirName() {
    return feedDirName;
  }

  /**
   * @param feedDirName the feedDirName to set
   */
  public void setFeedDirName(String feedDirName) {
    this.feedDirName = feedDirName;
  }

  /**
   * @return the feedDirPath
   */
  public String getFeedDirPath() {
    return feedDirPath;
  }

  /**
   * @param feedDirPath the feedDirPath to set
   */
  public void setFeedDirPath(String feedDirPath) {
    this.feedDirPath = feedDirPath;
  }

  /**
   * @return the feedX
   */
  public int getFeedX() {
    return feedX;
  }

  /**
   * @param feedX the feedX to set
   */
  public void setFeedX(int feedX) {
    this.feedX = feedX;
  }

  /**
   * @return the feedY
   */
  public int getFeedY() {
    return feedY;
  }

  /**
   * @param feedY the feedY to set
   */
  public void setFeedY(int feedY) {
    this.feedY = feedY;
  }

  /**
   * @return the feed
   */
  public Boolean getIsFeed() {
    return feed;
  }

  /**
   * @param feed the feed to set
   */
  public void setIsFeed(Boolean isFeed) {
    this.feed = isFeed;
  }

  /**
   * @return the lowerCaseNames
   */
  public Boolean getLowerCaseNames() {
    return lowerCaseNames;
  }

  /**
   * @param lowerCaseNames the lowerCaseNames to set
   */
  public void setLowerCaseNames(Boolean lowerCaseNames) {
    this.lowerCaseNames = lowerCaseNames;
  }
}
