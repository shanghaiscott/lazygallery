/*
 * PhotoFeed.java
 *
 * Created on Feb 1, 2009, 12:41 AM
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author scott
 */
public class PhotoFeed {

  private String feedIcon = "images/feed/photo-feed.png";
  private String rssFileName = "lazygallery.rss";
  private String format = "EEE, dd MMM yyyy HH:mm:ss z";
  private StringBuilder rss = new StringBuilder();
  private StringBuilder link = new StringBuilder();
  private static final String GENERATOR = "    <generator>LazyGallery, http://swdouglass.com/wiki/Wiki.jsp?page=LazyGallery, Copyright 2005-2009, Scott Douglass</generator>\n";

  public PhotoFeed() {
  }

  public PhotoFeed(HttpImageGallery hig) {
    SimpleDateFormat sdf = new SimpleDateFormat(format);
    String now = sdf.format(new Date());
    String baseURL = hig.getBaseURL();
    // generate HTML for the link
    link.append("\n<div class=\"photofeed\">\n");
    link.append("<a href=\"");
    link.append(baseURL);
    link.append(hig.getImageContext());
    link.append(hig.getImageRootDir());
    link.append(hig.getImageDir());
    link.append("/");
    link.append(rssFileName);
    link.append("\"><img src=\"");
    link.append(hig.getSiteURL());
    link.append(feedIcon);
    link.append("\" alt=\"rss\"></a>");
    link.append("</div>\n");

    File rssFile = new File(hig.getImageGallery().getImageDirPath() +
            ImageGallery.FILE_SEP + rssFileName);
    
    if (! rssFile.exists() || hig.getImageGallery().isModified()) {
      // write the RSS to a file
      rss.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
      rss.append("<rss version=\"2.0\"\n");
      //rss.append("  xmlns:photo=\"http://www.pheed.com/pheed/\"\n");
      rss.append("  xmlns:dc=\"http://purl.org/dc/elements/1.1/\"\n");
      rss.append("  xmlns:media=\"http://search.yahoo.com/mrss/\">\n");
      rss.append("  <channel>\n");
      rss.append("    <title>");
      rss.append(hig.getTitle());
      rss.append("</title>\n");
      rss.append("    <description>");
      rss.append("LazyGallery PhotoFeed: ");
      rss.append(hig.getCaption().replaceAll("\\<.*?\\>", "")); // strip HTML
      rss.append("</description>\n");
      rss.append("    <link>");
      rss.append(hig.getLinkURL());
      rss.append("</link>\n");
      rss.append("    <language>");
      rss.append(hig.getLangCode());
      rss.append("</language>\n");
      /*rss.append("    <image>\n");
      rss.append("      <url></url>\n");
      rss.append("      <title></title>\n");
      rss.append("      <link></link>\n");
      rss.append("      <description></description>\n");
      rss.append("    </image>\n");*/
      rss.append(GENERATOR);
      rss.append("    <lastBuildDate>");
      rss.append(now);
      rss.append("</lastBuildDate>\n");
      rss.append("    <pubDate>");
      rss.append(now);
      rss.append("</pubDate>\n");

      // now iterate of the ImageInfos and build the items
      for (ImageInfo image : hig.getImageGallery().getImages().values()) {
        if (image.getTitle() != null && image.getTitle().startsWith("blank")) {
          // skip
        } else {
          rss.append("    <item>\n");
          rss.append("      <title>");
          rss.append(image.getFilename());
          rss.append("</title>\n");
          rss.append("      <link>");
          rss.append(baseURL);
          rss.append(hig.getImageContext());
          rss.append(hig.getImageGallery().getImageFileURL(image));
          rss.append("</link>\n");
          rss.append("      <guid isPermalink=\"true\">");
          rss.append(baseURL);
          rss.append(hig.getImageContext());
          rss.append(hig.getImageGallery().getImageFileURL(image));
          rss.append("</guid>\n");
          /*
          rss.append("      <photo:imgsrc>");
          rss.append(baseURL);
          rss.append(hig.getImageContext());
          rss.append(hig.getImageGallery().getPreviewFileURL(image));
          rss.append("</photo:imgsrc>\n");
          rss.append("      <photo:thumbnail>");
          rss.append(baseURL);
          rss.append(hig.getImageContext());
          rss.append(hig.getImageGallery().getThumbnailFileURL(image));
          rss.append("</photo:thumbnail>\n");
          */
          // the description will have the thumbnail image
          rss.append("      <description>");
          rss.append("<![CDATA[<table><tr><td><img src=\"");
          rss.append(baseURL);
          rss.append(hig.getImageContext());
          rss.append(hig.getImageGallery().getThumbnailFileURL(image));
          rss.append("\" alt=\"");
          rss.append(image.getFilename());
          rss.append("\"></td><td>");
          rss.append(image.getFilename());
          rss.append("</td></tr></table>]]>");
          rss.append("</description>\n");
          rss.append("      <pubDate>");
          rss.append(now);
          rss.append("</pubDate>\n");
          // using Media RSS standard name space (as used by picasa generated photo feed.
          rss.append("      <media:group>\n");
          rss.append("        <media:title type='plain'>");
          rss.append(image.getFilename());
          rss.append("</media:title>\n");
          rss.append("        <media:description type='plain'></media:description>\n");
          rss.append("        <media:keywords></media:keywords>\n");
          rss.append("        <media:content url='");
          rss.append(baseURL);
          rss.append(hig.getImageContext());
          rss.append(hig.getImageGallery().getImageFileURL(image));
          //FIXME I don't actually have the original image's pixel dimensions...
          /*rss.append(" height='");
          rss.append(image.getHeight()); //image height
          rss.append("' width='");
          rss.append(image.getWidth()); //image width
          rss.append("'");*/
          rss.append("' type='image/jpeg' medium='image' />\n");

          // rss feed thumbnail (smallest)
          rss.append("        <media:thumbnail url='");
          rss.append(baseURL);
          rss.append(hig.getImageContext());
          rss.append(hig.getImageGallery().getFeedFileURL(image));
          rss.append("' height='");
          rss.append(hig.getImageGallery().getFeedY()); //image height
          rss.append("' width='");
          rss.append(hig.getImageGallery().getFeedX()); //image width
          rss.append("' />\n");

          // next thumbnail
          rss.append("        <media:thumbnail url='");
          rss.append(baseURL);
          rss.append(hig.getImageContext());
          rss.append(hig.getImageGallery().getThumbnailFileURL(image));
          rss.append("' height='");
          rss.append(hig.getImageGallery().getThumbnailY()); //image height
          rss.append("' width='");
          rss.append(hig.getImageGallery().getThumbnailX()); //image width
          rss.append("' />\n");

          // preview = thumbnail for this
          rss.append("        <media:thumbnail url='");
          rss.append(baseURL);
          rss.append(hig.getImageContext());
          rss.append(hig.getImageGallery().getPreviewFileURL(image));
          rss.append("' height='");
          rss.append(hig.getImageGallery().getPreviewY()); //image height
          rss.append("' width='");
          rss.append(hig.getImageGallery().getPreviewX()); //image width
          rss.append("' />\n");  
           
          rss.append("        <media:credit>");
          rss.append(hig.getAuthor());
          rss.append("</media:credit>\n");
          rss.append("      </media:group>\n");

          rss.append("    </item>\n");
        }
      }

      // end items

      rss.append("  </channel>\n");
      rss.append("</rss>");

      // write the feed file
      try {
        BufferedWriter out = new BufferedWriter(new FileWriter(rssFile));
        out.write(rss.toString());
        out.close();
      } catch (IOException e) {
        System.out.println("error: " + e.getLocalizedMessage());
        //e.printStackTrace();
      }
    }
  }

  @Override
  public String toString() {
    return link.toString();
  }

  /**
   * @return the feedIcon
   */
  public String getFeedIcon() {
    return feedIcon;
  }

  /**
   * @param feedIcon the feedIcon to set
   */
  public void setFeedIcon(String feedIcon) {
    this.feedIcon = feedIcon;
  }
}
