/*
 * LazyGalleryTag.java
 *
 * Created on March 4, 2005, 12:41 PM
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

package com.spacepirates.tags;

import com.spacepirates.gallery.HttpImageGallery;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.tagext.TagSupport;


/**
 * Generates a user interface for a LazyGallery. This class is no longer
 * actively maintained.
 *
 * @author scott
 */
public class LazyGalleryTag extends TagSupport {
  /** Serialization UUID. */
  private static final long serialVersionUID = -3709946356519374907L;
  /** Parameter for the name of the folder with the images. */
  private static final String P_IMAGES = "images";
  /** The gallery object. */
  private final HttpImageGallery gallery = new HttpImageGallery();

  @Override
  public final int doStartTag() throws JspException {

    final HttpServletRequest request =
      (HttpServletRequest) pageContext.getRequest();
    // starting directory, e.g. /china
    if (request.getParameter(P_IMAGES) == null){
      gallery.setImageDir(request.getPathInfo());
    } else {
      gallery.setImageDir(request.getParameter(P_IMAGES));
    }

    try {
      final JspWriter out = pageContext.getOut();
      gallery.init(request);
      out.write(gallery.toString());
    } catch (IOException e) {
      throw new JspException(e);
    }
    return SKIP_BODY;
  }
}
