package com.github.hexx.rsstumblrbot

import java.util.Date
import scala.collection.JavaConverters._
import com.sun.syndication.feed.synd.SyndEntry
import com.sun.syndication.io.{ SyndFeedInput, XmlReader }
import _root_.dispatch._
import _root_.dispatch.gae.Http
import _root_.dispatch.oauth.{ Consumer, Token }
import unfiltered.request._
import unfiltered.response._
import com.github.hexx.dispatch.tumblr._
import com.github.hexx.gaeds._
import com.github.hexx.gaeds.Property._

trait Config {
  def rssURL: String
  def tumblrHostname: String
  def consumerKey: String
  def consumerSecret: String
  def accessToken: String
  def accessSecret: String
  def entryLimit = 1000
}

class Entry(
    val title: Property[String], 
    val link: Property[String], 
    val description: Property[String],
    val date: Property[Date])
  extends Mapper[Entry] {
  def this() = this("", "", "", new Date)
  def isNew = Entry.query.filter(_.link #== link).count == 0
}

object Entry extends Entry

class RSSTumblrBot extends unfiltered.filter.Plan {
  val rconfig = new Config {
    val rssURL = "http://groups.google.com/group/scala-jp/feed/rss_v2_0_msgs.xml"
    val tumblrHostname = "pab-rssbot.tumblr.com"
    val consumerKey = "YOUR CONSUMER KEY"
    val consumerSecret = "YOUR CONSUMER SECRET"
    val accessToken = "YOUR ACCESS TOKEN"
    val accessSecret = "YOUR ACCESS SECRET"
  }

  def intent = {
    case GET(Path("/")) => {
      val http = new Http
      val rss = http(url(rconfig.rssURL) as_str)

      def parseFeed(feed: String) = {
        val input = new SyndFeedInput
        val reader = new XmlReader(new java.io.ByteArrayInputStream(feed.getBytes("UTF-8")))
        for (syndfeed <- input.build(reader).getEntries.asInstanceOf[java.util.ArrayList[SyndEntry]].asScala) yield {
          new Entry(syndfeed.getTitle, syndfeed.getLink, syndfeed.getDescription.getValue, syndfeed.getPublishedDate)
        }
      }

      def postTumblr(entry: Entry) {
        val consumer = Consumer(rconfig.consumerKey, rconfig.consumerSecret)
        val token = Token(rconfig.accessToken, rconfig.accessSecret)
        http(Blog.postLink(rconfig.tumblrHostname, consumer, token, entry.link)
             title entry.title
             description ("<blockquote>" + entry.description + "</blockquote>")
             date entry.date)
      }

      for (entry <- parseFeed(rss).sortWith(_.date before _.date); if entry.isNew) {
        postTumblr(entry)
        if (Entry.query.count >= rconfig.entryLimit) {
          Datastore.delete(Entry.query.sort(_.date asc).asSingleKey)
        }
        entry.put()
      }
      http.shutdown()
      Ok ~> ResponseString("OK")
    }
  }
}
