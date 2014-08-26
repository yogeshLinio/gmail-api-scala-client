package gmailapi.methods

import akka.actor.Actor
import gmailapi.oauth2.OAuth2Identity
import gmailapi.resources.GmailSerializer 
import gmailapi.resources.{ Message, MessageFormat, MessageAttachment, MessageHeader, MessagePart, MessageList } 
import org.json4s.jackson.Serialization.{ read, write }
import org.json4s.jackson.JsonMethods.parse
import scala.collection.immutable.Map
import spray.http.{ HttpCredentials, HttpEntity, HttpMethods, ContentTypes, Uri }


object Messages {
  import GmailSerializer._

  case class Delete(id: String, userId: String = "me")
    (implicit val token: OAuth2Identity) extends GmailRestRequest {

    val uri = s"$baseUri/users/$userId/messages/$id"
    val method = HttpMethods.DELETE
    val credentials : Option[HttpCredentials] = token
    val entity = HttpEntity.Empty
    val unmarshaller = None
  }

  case class Get(id: String, format:MessageFormat.Value = MessageFormat.Full, userId: String = "me") 
    (implicit val token: OAuth2Identity) extends GmailRestRequest {

    val uri = s"$baseUri/users/$userId/messages/$id"
    val method = HttpMethods.GET
    val credentials : Option[HttpCredentials] = token
    val entity = HttpEntity.Empty
    val unmarshaller = Some(read[Message](_:String))
  }
  
  case class Insert(message: Message, userId: String = "me") 
    (implicit val token: OAuth2Identity) extends GmailRestRequest {
    assert(message.id == None)
    assert(message.snippet == None)
    assert(message.historyId == None)
    assert(message.payload == None)
    assert(message.sizeEstimate == None)
    assert(message.raw != None)

    val uri = s"$baseUri/users/$userId/messages"
    val method = HttpMethods.POST
    val credentials : Option[HttpCredentials] = token
    val entity : HttpEntity = HttpEntity(ContentTypes.`application/json`, write(message))
    val unmarshaller = Some(read[Message](_:String))
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
      
      Uri(s"$baseUri/users/$userId/messages") withQuery (queryBuilder.result()) toString
    }
    val method = HttpMethods.GET
    val credentials : Option[HttpCredentials] = token
    val entity = HttpEntity.Empty
    val unmarshaller = Some(read[MessageList](_:String))
  }
  
  case class Modify(
    id: String, 
    addLabelIds: Seq[String] = Nil,
    removeLabelIds: Seq[String] = Nil,
    userId: String = "me") 
    (implicit val token: OAuth2Identity) extends GmailRestRequest {
    
    val uri = s"$baseUri/users/$userId/messages/$id/modify"
    val method = HttpMethods.POST
    val credentials : Option[HttpCredentials] = token
    val entity = HttpEntity(ContentTypes.`application/json`, write(Map(
      "addLabelIds" -> addLabelIds,
      "removeLabelIds" -> removeLabelIds)))
    val unmarshaller = Some(read[Message](_:String))
  }
  
  case class Send(message: Message, userId: String = "me") 
    (implicit val token: OAuth2Identity) extends GmailRestRequest {
    assert(message.id == None)
    assert(message.labelIds == Nil)
    assert(message.snippet == None)
    assert(message.historyId == None)
    assert(message.payload == None)
    assert(message.sizeEstimate == None)
    assert(message.raw != None)

    val uri = s"$baseUri/users/$userId/messages/send"
    val method = HttpMethods.POST
    val credentials : Option[HttpCredentials] = token
    val entity : HttpEntity = HttpEntity(ContentTypes.`application/json`, write(message))
    val unmarshaller = Some(read[Message](_:String))
  }

  case class Trash(id: String, userId: String = "me") 
    (implicit val token: OAuth2Identity) extends GmailRestRequest {

    val uri = s"$baseUri/users/$userId/messages/$id/trash"
    val method = HttpMethods.POST
    val credentials : Option[HttpCredentials] = token
    val entity = HttpEntity.Empty
    val unmarshaller = Some(read[Message](_:String))
  }

  case class Untrash(id: String, userId: String = "me") 
    (implicit val token: OAuth2Identity) extends GmailRestRequest {

    val uri = s"$baseUri/users/$userId/messages/$id/untrash"
    val method = HttpMethods.POST
    val credentials : Option[HttpCredentials] = token
    val entity = HttpEntity.Empty
    val unmarshaller = Some(read[Message](_:String))
  }
  
  case class Import(message: Message, userId: String = "me") 
    (implicit val token: OAuth2Identity) extends GmailRestRequest {
    assert(message.id == None)
    assert(message.snippet == None)
    assert(message.historyId == None)
    assert(message.payload == None)
    assert(message.sizeEstimate == None)
    assert(message.raw != None)

    val uri = s"$baseUri/users/$userId/messages/import"
    val method = HttpMethods.POST
    val credentials : Option[HttpCredentials] = token
    val entity : HttpEntity = HttpEntity(ContentTypes.`application/json`, write(message))
    val unmarshaller = Some(read[Message](_:String))
  }
  
  object Attachments {
    case class Get(id: String, messageId: String, userId: String = "me") 
      (implicit val token: OAuth2Identity) extends GmailRestRequest {

      val uri = s"$baseUri/users/$userId/messages/$messageId/attachments/$id"
      val method = HttpMethods.GET
      val credentials : Option[HttpCredentials] = token
      val entity = HttpEntity.Empty
      val unmarshaller = Some(read[MessageAttachment](_:String))
    }
  }
}
  