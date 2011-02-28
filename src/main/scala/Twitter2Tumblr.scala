import collection.JavaConversions._
import io.Source
import xml.{NodeSeq, XML, Elem, Node}

import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.client.methods.HttpPost
import org.apache.http.params.BasicHttpParams
import org.apache.http.entity.BufferedHttpEntity
import org.apache.http.entity.StringEntity
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.NameValuePair
import org.apache.http.message.BasicNameValuePair

object Twitter2Tumblr {

  def main(args: Array[String]) {
    require(args.length == 3)
    val name = args(0)     // twitter and tumblr name (assuming they are same)
    val email = args(1)    // tumblr email
    val password = args(2) // tumblr password

    val twitterUrl = "http://api.twitter.com/1/statuses/user_timeline.atom?screen_name=%s&count=200".format(name)
    val tumblrUrl = "http://www.tumblr.com/api/write"

    // read user timeline from twitter api
    val entries = nodes(twitterUrl, _ \\ "entry")

    // post each tweet to tumblr
    entries.foreach(entry => write(tumblrUrl, entry, name, email, password))
  }

	// read and parse atom feed
	def read(url: String) = {
		val src = Source.fromURL(url)("UTF-8")
	  try {
	    XML.loadString(src.getLines.mkString)
	  } finally {
	    src.close()
    }
  }

  // get xml nodes
	def nodes(url: String, op: Elem => NodeSeq) = op(read(url))

	// write to tumblr
	def write(url: String, entry: Node, name: String, email: String, password: String) {
    val content = (entry \ "content").text.replace("%s: ".format(name), "")
    val published = (entry \ "published").text
    val source = (entry \ "source").text
		println(content)
    println(published)
    println(source)

    val httpClient = new DefaultHttpClient
    val httpPost = new HttpPost(url)

    // http://www.tumblr.com/docs/en/api#api_write
    val params = Map(
      "email" -> email,
      "password" -> password,
      "type" -> "regular",
      "generator" -> source,
      "date" -> published,
      "body" -> content,
      "group" -> "%s.tumblr.com".format(name),
      "send-to-twitter" -> "no"
    )

    // convert scala Iterable to java List
    val httpParams = new java.util.ArrayList[NameValuePair]()
    params.map(pair => httpParams.add(new BasicNameValuePair(pair._1, pair._2)))

    httpPost.setEntity(new UrlEncodedFormEntity(httpParams, "UTF-8"))

    Thread.sleep(1000) // avoid exceeding tumblr rate limit

    val httpResponse = httpClient.execute(httpPost)
    val statusLine = httpResponse.getStatusLine
    val statusCode = statusLine.getStatusCode
    val result = new BufferedHttpEntity(httpResponse.getEntity).getContent

    statusCode match {
      case 201 => println("Success! The new post ID is %s".format(
        Source.fromInputStream(result).mkString))
      case 403 => println("Bad email or password")
      case _ => println("Error: %s".format(statusLine))
    }
  }
}
