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
 */
package com.spacepirates.gallery;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author scott
 */
public class HttpImageGallery implements java.io.Serializable {
  /** Serialization UUID. */
  private static final long serialVersionUID = -6845834488748625437L;
  /** File separator characters. */
  public static final String FILE_SEP = System.getProperty("file.separator");

  /**
   * JSPWiki property or servlet context init parameter setting file system path
   * to root directory for images.
   */
  public static final String P_DOCROOT = "root";

  /**
   * The default value for the file portion of the base URL, of the
   * root directory  of the images.
   * This means http://server/images without an intervening context.
   */
  public static final String D_IMAGES = "/images";

  /** Default number of thumbnails in one row of a table display. */
  private static final Integer D_THUMBS = 6;

  /** The root directory in the URL to the images. */
  private String root = D_IMAGES;
  /** The number of thumbnails per row when rendered in a table. */
  private Integer thumbs = D_THUMBS;

  /** Flag to enable/disable debugging output. */
  private Boolean debug = false;
  /** Flag to enable rendering with a JavaScript library. */
  private Boolean lightbox = false;
  /** File name of the icon to use as a link to EXIF data for a picture. */
  private String exifIcon = "exif.png";
  /** File name of the icon for the link to the full size picture. */
  private String printIcon = "print.png";
  /** Caption to render under the gallery. */
  private String caption;
  /** Directory name for images above P_DOCROOT. */
  private String images;
  /** Directory name for icons above P_DOCROOT. */
  private String icons;
  /** URL for the site hosting the gallery. */
  private String siteURL;
  /** Value of the context part of the URL. */
  private String imageContext;
  /** Value of the webapp context. */
  private String contextPath;
  /** Value of the servlet path. */
  private String servletPath = "";
  /** Gallery HTML output file name. */
  private String htmlFileName = "index.html";
  /** Flag to enable HTML output file. */
  private Boolean html = false;
  /** Gallery description file name.*/
  private String descriptionFileName = "readme.html";
  /** Gallery description. */
  private String description =
    "<a href=\"http://swdouglass.com/wiki/Wiki.jsp?page=LazyGallery\">LazyGallery</a>";
  /** Author, used for the RSS page. */
  private String author;
  /** Title, used for the RSS page. */
  private String title;
  /** Flag when set to true, generate media RSS. */
  private Boolean feed = false;
  /** The servlet request. */
  private HttpServletRequest request;
  /** The link to the jsp/jspwiki page which the gallery resides in. */
  private String linkURL;
  /** Language setting for the RSS page.*/
  private String lang = "en-us";
  /** Flag when set to true, use multiple threads for image processing. */
  private Boolean threads = false;
  /** Flag, when set to true attempt to rotate pictures portrait size.*/
  private Boolean rotate = false;
  /** The ImageGallery to be rendered. */
  private ImageGallery imageGallery;

  /**
   * Initialize the gallery, getting properties from the HttpServletRequest
   * and associated servlet container managed classes.
   * @param inRequest an HttpServletRequest
   */
  public final void init(final HttpServletRequest inRequest) {
    // JSPWiki sometimes does NOT have an HttpRequest...
    if (inRequest != null) {
      setRequest(inRequest); // save reference for later
      setContextPath(inRequest.getContextPath());
      setImageContext(inRequest.getContextPath());
      // If not starting with a /, assume we're relative to the inRequest
      // context path. Otherwise assume the path is relative to the web server
      // root / context.
      /*
      if (! this.getImageRootDir().startsWith("/")) {
        cpath = inRequest.getContextPath() + "/";
        servletPath = inRequest.getSession().getServletContext()
         .getRealPath(inRequest.getServletPath());
        // add this because we need it later on
        this.setImageRootDir("/" + this.getImageRootDir());
      } else {
        // get the root context and the real path of the base of it
        // This should work in Tomcat thanks to crossContext="true", but it
        // doesn't.
        servletPath = inRequest.getSession().getServletContext()
          .getContext("").getRealPath("/");
        // Instead we'll use a parameter on context initialization.
        servletPath = inRequest.getSession().getServletContext()
          .getInitParameter(HttpImageGallery.P_DOCROOT);
      }
      */

      if (inRequest.getSession().getServletContext()
        .getInitParameter(P_DOCROOT) == null) {
        setServletPath(inRequest.getSession().getServletContext()
          .getRealPath(inRequest.getServletPath()));
      } else {
        setServletPath(inRequest.getSession().getServletContext()
          .getInitParameter(P_DOCROOT));
        // remove webapp context from consideration as images are not in webapp
        setImageContext("");
      }
      final StringBuilder sbImageDir = new StringBuilder();

      sbImageDir.append(getServletPath().substring(0,
        getServletPath().lastIndexOf(FILE_SEP)));
      sbImageDir.append(this.getImageRootDir()); // relative to servlet context
      sbImageDir.append(this.getImageDir());

      if (this.getIconDir() == null) {
        this.setIconDir(D_IMAGES);
      }

      setImageGallery(new ImageGallery(sbImageDir.toString(),
        this.getThumbsPerRow(), feed, getUseThreads()));
      getImageGallery().setRotateImages(rotate);

      this.readDescription();
      if (this.getCaption() == null) {
        this.setCaption(getDescription());
      }
    }
  }

  @Override
  public final String toString() {
    final StringBuilder gallery = new StringBuilder("");
    gallery.append("<div class=\"lazygallery\">\n");
    gallery.append("  <table style=\"text-align: center;\">\n");

    int counter = 1;
    for (ImageInfo image : getImageGallery().getImages().values()) {
      if (counter == 1) {
        gallery.append("    <tr>\n");
      }
      
      if (image.getFile() == null) {
        gallery.append("      <td>&nbsp;</td>\n");
      } else {
        gallery.append("      <td><a href=\"");
        if (this.getLightbox()) {
          gallery.append(getImageContext());
          gallery.append(image.getPreviewInfo().getUrl());
          gallery.append("\" rel=\"lightbox[");
          gallery.append(this.getImageDir());
          gallery.append("]");
        } else {
          gallery.append(getImageContext());
          gallery.append("preview.jsp");
          gallery.append(getImageContext());
          gallery.append(image.getPreviewInfo().getUrl());
        }
        gallery.append("\"><img src=\"");
        gallery.append(getImageContext());
        gallery.append(getImageGallery().getThumbnailFileURL(image));
        gallery.append("\" alt=\"");
        gallery.append(image.getFilename());
        gallery.append("\" title=\"");
        gallery.append(image.getFilename());
        gallery.append("\" width=\"");
        gallery.append(getImageGallery().getThumbnailX());
        gallery.append("\" height=\"");
        gallery.append(getImageGallery().getThumbnailY());
        gallery.append("\"/></a>");
        gallery.append("<div class=\"LGicons\">");
        gallery.append("<a href=\"");
        gallery.append(getImageContext());
        gallery.append(getImageGallery().getImageFileURL(image));
        gallery.append("\">");
        gallery.append("<img src=\"");
        gallery.append(getContextPath());
        gallery.append(this.getIconDir());
        gallery.append("/");
        gallery.append(this.getPrintIcon());
        gallery.append("\" alt=\"hi-res\"/></a>");
        if (image.isExifPresent()) {
          gallery.append("<a href=\"");
          gallery.append(getImageContext());
          gallery.append(getImageGallery().getPreviewFileURL(image));
          gallery.append(".exif\">");
          gallery.append("<img src=\"");
          gallery.append(getContextPath());
          gallery.append(this.getIconDir());
          gallery.append("/");
          gallery.append(this.getExifIcon());
          gallery.append("\" alt=\"exif\"/></a>");
        }
        gallery.append("</div></td>\n");
      }
      counter++;
      if (counter > this.getThumbsPerRow()) {
        gallery.append("    </tr>\n");
        counter = 1;
      }
    }
    gallery.append("  </table>\n");
    gallery.append(this.getCaption());
    gallery.append("\n</div>\n");

    if (this.getDebug()) {
      gallery.append("<div id=\"debug\">\n");
      gallery.append("ServletContext path:");
      gallery.append(getContextPath());
      gallery.append("<br/>\n");
      gallery.append("Server real path: ");
      gallery.append(getServletPath());
      gallery.append("<br/>\n");
      gallery.append("Servlet Path: ");
      gallery.append(getServletPath());
      gallery.append("<br/>\n");
      gallery.append("Gallery URL: ");
      gallery.append(getLinkURL());
      gallery.append("<br/>\n");
      gallery.append("Extra path info: ");
      gallery.append(getRequest().getPathInfo());
      gallery.append("<br/>\n");
      gallery.append("Request parameter (imageDir): ");
      gallery.append(getRequest().getParameter("imageDir"));
      gallery.append("<br/>\n");
      gallery.append("Request parameter (page): ");
      gallery.append(getRequest().getParameter("page"));
      gallery.append("<br/>\n");
      gallery.append("Full path to images: ");
      gallery.append(getImageGallery().getImageDirPath());
      gallery.append("<br/>\n");
      gallery.append("URL path to images: ");
      gallery.append(getImageContext());
      gallery.append(getImageRootDir());
      gallery.append(getImageDir());
      gallery.append("<br/>\n");
      gallery.append("Messages from ImageGallery:<br/>\n");
      for (String s : getImageGallery().getMessages()) {
        gallery.append("&nbsp;");
        gallery.append(s);
        gallery.append("<br/>\n");
      }
      gallery.append("</div>\n");
      gallery.append("<div id=\"debugIcon\">\n");
      gallery.append(
        "<form action=\"javascript:toggleVisibility('debug')\">\n");
      gallery.append("<input type=\"image\" src=\"");
      gallery.append(getImageContext());
      gallery.append(this.getIconDir());
      gallery.append("/bug.png\" alt=\"debug\"/>\n");
      gallery.append("</form>\n");
      gallery.append("</div>\n");
    }

    if (feed) {
      gallery.append(new PhotoFeed(this));
    }

    // FIXME: refactor into separate method
    if (getMakeHtmlFile()) {
      final File htmlFile = new File(this.getImageGallery().getImageDirPath()
        + ImageGallery.FILE_SEP + getHtmlFileName());
      final StringBuilder htmlOutput = new StringBuilder();
      htmlOutput.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">");
      htmlOutput.append("<html>\n");
      htmlOutput.append("<head>\n");
      htmlOutput.append("<title>");
      htmlOutput.append(this.getTitle());
      htmlOutput.append("</title>");
      htmlOutput.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />");
      htmlOutput.append("</head>\n");
      htmlOutput.append("<body>\n");
      htmlOutput.append(gallery);
      htmlOutput.append("</body>\n");
      htmlOutput.append("</html>\n");
      try {
        final BufferedWriter out = new BufferedWriter(new FileWriter(htmlFile));
        out.write(htmlOutput.toString());
        out.close();
      } catch (IOException e) {
      }
      gallery.append("<div>");
      gallery.append("<a href=\"");
      gallery.append(getImageContext());
      gallery.append(getImageRootDir());
      gallery.append(getImageDir());
      gallery.append("/");
      gallery.append(getHtmlFileName());
      gallery.append("\">No JavaScript</a>");
      gallery.append("</div>");
    }
    return gallery.toString();
  }

  /**
   * Compute the base URL including the protocol, host and port (e.g.
   * http://localhost:8080, no trailing "/").
   * @return the URL
   */
  public final String getBaseURL() {
    final StringBuilder sbURL = new StringBuilder();

    try {
      final URL baseURL = new URL(getSiteURL());
      sbURL.append(baseURL.getProtocol());
      sbURL.append("://");
      sbURL.append(baseURL.getHost());
      if (baseURL.getPort() > 0) {
        sbURL.append(":");
        sbURL.append(baseURL.getPort());
      }
    } catch (MalformedURLException e) {
      //
    }

    return sbURL.toString();
  }

  /**
   * Read in the readme.htmlOutput file if present. If not present
   * and the global readme.htmlOutput file exists, use the global file.
   */
  private void readDescription() {
    final File dscr = new File(getImageGallery().getImageDirPath()
      + FILE_SEP + this.getDescriptionFileName());
    final File gdscr = new File(getImageGallery().getGalleryRootPath()
      + FILE_SEP + this.getDescriptionFileName());
    if (dscr.exists()) {
      this.setDescription(this.readTextFile(dscr).toString());
    } else if (gdscr.exists()) {
      this.setDescription(this.readTextFile(gdscr).toString());
    }
  }

  /**
   * Read in text from a file.
   * @param inFile the source file
   * @return a String with the text
   */
  private String readTextFile(final File inFile) {
    final StringBuilder contents = new StringBuilder();
    BufferedReader input = null;
    try {
      //use buffering
      //this implementation reads one line at a time
      input = new BufferedReader(new FileReader(inFile));
      String line = null; //not declared within while loop
      while ((line = input.readLine()) != null) {
        contents.append(line);
        contents.append(System.getProperty("line.separator"));
      }
    } catch (FileNotFoundException ex) {
      //this.addMessage(ex.getLocalizedMessage());
    } catch (IOException ex) {
      //this.addMessage(ex.getLocalizedMessage());
    } finally {
      try {
        if (input != null) {
          //flush and close both "input" and its underlying FileReader
          input.close();
        }
      } catch (IOException ex) {
        //this.addMessage(ex.getLocalizedMessage());
      }
    }
    return contents.toString();
  }

  /**
   * Get the feed flag.
   * @return true if has feed or false if not
   */
  public Boolean hasFeed() {
    return this.feed;
  }

  /**
   * Set the feed flag.
   * @param inHasFeed true if has feed, false if not
   */
  public void setHasFeed(final Boolean inHasFeed) {
    this.feed = inHasFeed;
  }
  public String getImageRootDir() {
    return root;
  }

  public void setImageRootDir(final String inImageRootDir) {
    this.root = inImageRootDir;
  }

  public int getThumbsPerRow() {
    return thumbs;
  }

  public void setThumbsPerRow(final int thumbsPerRow) {
    this.thumbs = thumbsPerRow;
  }

  public Boolean getDebug() {
    return debug;
  }

  public void setDebug(Boolean debug) {
    this.debug = debug;
  }

  public Boolean getLightbox() {
    return lightbox;
  }

  public void setLightbox(Boolean lightbox) {
    this.lightbox = lightbox;
  }

  public String getExifIcon() {
    return exifIcon;
  }

  public void setExifIcon(String exifIcon) {
    this.exifIcon = exifIcon;
  }

  public String getPrintIcon() {
    return printIcon;
  }

  public void setPrintIcon(String printIcon) {
    this.printIcon = printIcon;
  }

  public String getCaption() {
    return caption;
  }

  public void setCaption(String caption) {
    this.caption = caption;
  }

  public String getImageDir() {
    return images;
  }

  public void setImageDir(String imageDir) {
    this.images = imageDir;
  }

  public String getIconDir() {
    return icons;
  }

  public void setIconDir(String iconDir) {
    this.icons = iconDir;
  }

  /**
   * @return the siteURL
   */
  public String getSiteURL() {
    return siteURL;
  }

  /**
   * @param siteURL the siteURL to set
   */
  public void setSiteURL(String siteURL) {
    this.siteURL = siteURL;
  }

  /**
   * @return the imageGallery
   */
  public ImageGallery getImageGallery() {
    return imageGallery;
  }

  /**
   * @param imageGallery the imageGallery to set
   */
  public void setImageGallery(ImageGallery imageGallery) {
    this.imageGallery = imageGallery;
  }

  /**
   * @return the imageContext
   */
  public String getImageContext() {
    return imageContext;
  }

  /**
   * @param imageContext the imageContext to set
   */
  public void setImageContext(String imageContext) {
    this.imageContext = imageContext;
  }

  /**
   * @return the htmlFileName
   */
  public String getHtmlFileName() {
    return htmlFileName;
  }

  /**
   * @param htmlFileName the htmlFileName to set
   */
  public void setHtmlFileName(String htmlFileName) {
    this.htmlFileName = htmlFileName;
  }

  /**
   * @return the htmlOutput
   */
  public Boolean getMakeHtmlFile() {
    return html;
  }

  /**
   * @param htmlOutput the htmlOutput to set
   */
  public void setMakeHtmlFile(Boolean makeHtmlFile) {
    this.html = makeHtmlFile;
  }

  public String getDescriptionFileName() {
    return descriptionFileName;
  }

  public void setDescriptionFileName(String descriptionFileName) {
    this.descriptionFileName = descriptionFileName;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * @return the contextPath
   */
  public String getContextPath() {
    return contextPath;
  }

  /**
   * @param contextPath the contextPath to set
   */
  public void setContextPath(String contextPath) {
    this.contextPath = contextPath;
  }

  /**
   * @return the servletPath
   */
  public String getServletPath() {
    return servletPath;
  }

  /**
   * @param servletPath the servletPath to set
   */
  public void setServletPath(String servletPath) {
    this.servletPath = servletPath;
  }

  /**
   * @return the author
   */
  public String getAuthor() {
    return author;
  }

  /**
   * @param author the author to set
   */
  public void setAuthor(String author) {
    this.author = author;
  }

  /**
   * @return the title
   */
  public String getTitle() {
    return title;
  }

  /**
   * @param title the title to set
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * @return the request
   */
  public HttpServletRequest getRequest() {
    return request;
  }

  /**
   * @param request the request to set
   */
  public void setRequest(HttpServletRequest request) {
    this.request = request;
  }

  /**
   * @return the linkURL
   */
  public String getLinkURL() {
    return linkURL;
  }

  /**
   * @param linkURL the linkURL to set
   */
  public void setLinkURL(String linkURL) {
    this.linkURL = linkURL;
  }

  /**
   * @return the lang
   */
  public String getLangCode() {
    return lang;
  }

  /**
   * @param lang the lang to set
   */
  public void setLangCode(String langCode) {
    this.lang = langCode;
  }

  /**
   * @return the threads
   */
  public Boolean getUseThreads() {
    return threads;
  }

  /**
   * @param threads the threads to set
   */
  public void setUseThreads(Boolean useThreads) {
    this.threads = useThreads;
  }

  /**
   * @return the rotate
   */
  public Boolean getRotateImages() {
    return rotate;
  }

  /**
   * @param rotate the rotate to set
   */
  public void setRotateImages(Boolean rotateImages) {
    this.rotate = rotateImages;
  }
}
