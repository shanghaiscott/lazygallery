/*
 * ImageInfo.java
 *
 * Created on December 4, 2004, 8:23 PM
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
import com.spacepirates.swing.chooser.ImageNameFilter;
import java.io.File;
import java.util.Date;

/**
 *
 * @author scott
 */
public class ImageInfo implements java.io.Serializable, java.lang.Comparable {
  
  private static final long serialVersionUID = 1L;
  private File file;
  private int width;
  private int height;

  private String title;
  private String caption;

  private String url;
  private boolean exifPresent = false;
  private ImageInfo previewInfo;
  private ImageInfo thumbnailInfo;
  private ImageInfo feedInfo;

  /**
   *  Construct a new ImageInfo for the given file name. 
   */
  public ImageInfo (File fileName) {
    file = fileName;
  }
  
  public ImageInfo() {    
  }
  
  public void setFile (File fileName) {
    this.file = fileName;
  }
  
  public File getFile () {
    return this.file;
  }
  
  public void setWidth(int inSizeX) {
    this.width = inSizeX;
  }

  public int getWidth() {
    return this.width;
  }
  
  public void setHeight(int inSizeY) {
    this.height = inSizeY;
  }

  public int getHeight() {
    return this.height;
  }

  public long getSizeMB() {
    return (this.getFile().length() / 1000000);
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getCaption() {
    return caption;
  }

  public void setCaption(String caption) {
    this.caption = caption;
  }

  public Date getDate() {
    return new Date(this.getFile().lastModified());
  }


  public String getExtention() {
    return ImageNameFilter.getExtension(this.file.getName());
  }
  
  public String getFilename() {
    return this.file.getName();
  }
  
  public String getFilePath() {
    return this.file.getPath();
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }
  
  @Override
  public int compareTo(Object inImageInfo) throws ClassCastException {
      
     if (!(inImageInfo instanceof ImageInfo)) {
       throw new ClassCastException("ImageInfo object expected.");
     }
     String thisName = this.getFilename();
     String thatName = ((ImageInfo) inImageInfo).getFilename();
     return thisName.compareTo(thatName);
  }

  public ImageInfo getPreviewInfo() {
    return previewInfo;
  }

  public void setPreviewInfo(ImageInfo previewInfo) {
    this.previewInfo = previewInfo;
  }

  public ImageInfo getThumbnailInfo() {
    return thumbnailInfo;
  }

  public void setThumbnailInfo(ImageInfo thumbnailInfo) {
    this.thumbnailInfo = thumbnailInfo;
  }

  public boolean isExifPresent() {
    return exifPresent;
  }

  public void setExifPresent(boolean exifPresent) {
    this.exifPresent = exifPresent;
  }

  /**
   * @return the feedInfo
   */
  public ImageInfo getFeedInfo() {
    return feedInfo;
  }

  /**
   * @param feedInfo the feedInfo to set
   */
  public void setFeedInfo(ImageInfo feedInfo) {
    this.feedInfo = feedInfo;
  }
 
}
