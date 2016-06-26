/*
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
package com.spacepirates.swing.chooser;

import java.io.File;
import javax.swing.filechooser.FileFilter;

public class ImageNameFilter extends FileFilter
        implements java.io.FilenameFilter {

  public final static String JPEG = "jpeg";
  public final static String JPG = "jpg";
  public final static String GIF = "gif";
  public final static String TIFF = "tiff";
  public final static String TIF = "tif";
  public final static String PNG = "png";

  //Accept all directories and all gif, jpg, tiff, or png files.
  @Override
  public boolean accept(File f) {
    if (f.isDirectory()) {
      return true;
    }

    String extension = ImageNameFilter.getExtension(f.getName());
    if (extension != null) {
      if (extension.equals(ImageNameFilter.TIFF) ||
              extension.equals(ImageNameFilter.TIF) ||
              extension.equals(ImageNameFilter.GIF) ||
              extension.equals(ImageNameFilter.JPEG) ||
              extension.equals(ImageNameFilter.JPG) ||
              extension.equals(ImageNameFilter.PNG)) {
        return true;
      } else {
        return false;
      }
    }

    return false;
  }

  public static String getExtension(String s) {
    String ext = null;
    int i = s.lastIndexOf('.');
    if (i > 0 && i < s.length() - 1) {
      ext = s.substring(i + 1).toLowerCase();
    }
    return ext;
  }

  //Accept all gif, jpg, tiff, or png files.
  @Override
  public boolean accept(File fileDir, String fileName) {
    boolean result = false;
    String extension = getExtension(fileName);
    if (extension != null) {
      if (extension.equals(ImageNameFilter.TIFF) ||
              extension.equals(ImageNameFilter.TIF) ||
              extension.equals(ImageNameFilter.GIF) ||
              extension.equals(ImageNameFilter.JPEG) ||
              extension.equals(ImageNameFilter.JPG) ||
              extension.equals(ImageNameFilter.PNG)) {
        result = true;
      }
    }

    return result;
  }

  //The description of this filter
  @Override
  public String getDescription() {
    return "Just Images";
  }
}
