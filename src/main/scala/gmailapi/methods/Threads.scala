package gmailapi.methods

import akka.actor.Actor
import gmailapi.oauth2.OAuth2Identity
import gmailapi.resources.{ Thread, ThreadList, GmailSerializer }
import org.json4s.jackson.Serialization.{ read, write }
import org.json4s.jackson.JsonMethods.parse
import scala.collection.immutable.Map
import spray.http.{ HttpCredentials, HttpEntity, HttpMethods, ContentTypes, Uri }

object Threads {
  import GmailSerializer._
  
  case class Get(id: String, userId: String = "me") 
    (implicit val token: OAuth2Identity) extends GmailRestRequest {

    val uri = s"$baseUri/users/$userId/threads/$id"
    val method = HttpMethods.GET
    val credentials : Option[HttpCredentials] = token
    val entity = HttpEntity.Empty
    val unmarshaller = Some(read[Thread](_:String))
  }

  case class List(
    includeSpamTrash: Boolean = false,
    labelIds: Seq[String] = Nil,
    maxResults: Option[Int] = None,
    pageToken: Option[String] = None,
    query: Option[String] = None,
    userId: String = "me")  
    (implicit val token: OAuth2Identity) extends GmailRestRequest {

    val uri = {
      val queryBuilder = Uri.Query.newBuilder
      queryBuilder += ("includeSpamTrash" -> includeSpamTrash.toString)
      labelIds foreach {labelIds => queryBuilder += ("labelIds" -> labelIds) }
      maxResults foreach {maxResults => queryBuilder += ("maxResults" -> maxResults.toString) }
      pageToken foreach {pageToken => queryBuilder += ("pageToken" -> pageToken) }
      query foreach {query => queryBuilder += ("query" -> query) }
      
      Uri(s"$baseUri/users/$userId/threads") withQuery (queryBuilder.result()) toString
    }
    val method = HttpMethods.GET
    val credentials : Option[HttpCredentials] = token
    val entity = HttpEntity.Empty
    val unmarshaller = Some(read[ThreadList](_:String))
  }
  
  case class Modify(
    id: String, 
    addLabelIds: Seq[String] = Nil,
    removeLabelIds: Seq[String] = Nil,
    userId: String = "me") 
    (implicit val token: OAuth2Identity) extends GmailRestRequest {
    
    val uri = s"$baseUri/users/$userId/threads/$id/modify"
    val method = HttpMethods.POST
    val credentials : Option[HttpCredentials] = token
    val entity = HttpEntity(ContentTypes.`application/json`, write(Map(
      "addLabelIds" -> addLabelIds,
      "removeLabelIds" -> removeLabelIds)))
    val unmarshaller = Some(read[Thread](_:String))
  }

  case class Delete(id: String, userId: String = "me")
    (implicit val token: OAuth2Identity) extends GmailRestRequest {
  
    val uri = s"$baseUri/users/$userId/threads/$id"
    val method = HttpMethods.DELETE
    val credentials : Option[HttpCredentials] = token
    val entity = HttpEntity.Empty
    val unmarshaller = None
  }
  
  case class Trash(id: String, userId: String = "me")
    (implicit val token: OAuth2Identity) extends GmailRestRequest {
  
    val uri = s"$baseUri/users/$userId/threads/$id/trash"
    val method = HttpMethods.POST
    val credentials : Option[HttpCredentials] = token
    val entity = HttpEntity.Empty
    val unmarshaller = Some(read[Thread](_:String))
  }
  
  case class Untrash(id: String, userId: String = "me")
    (implicit val token: OAuth2Identity) extends GmailRestRequest {
  
    val uri = s"$baseUri/users/$userId/threads/$id/untrash"
    val method = HttpMethods.POST
    val credentials : Option[HttpCredentials] = token
    val entity = HttpEntity.Empty
    val unmarshaller = Some(read[Thread](_:String))
  }
}
  