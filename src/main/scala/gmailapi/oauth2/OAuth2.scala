package gmailapi.oauth2

import akka.actor.Actor
import com.typesafe.config.Config
import gmailapi.methods.GmailRestRequest
import org.json4s.jackson.JsonMethods.parse
import spray.http.{ HttpCredentials, HttpEntity, HttpMethods, ContentTypes, FormData, Uri }
import spray.httpx.marshalling.marshal

object OAuth2 {
  
  case class RequestToken(authCode: String)
    (implicit val config: Config) extends GmailRestRequest {

    val uri = "https://accounts.google.com/o/oauth2/token"
    val method = HttpMethods.POST
    val credentials = None
    val entity : HttpEntity = marshal(FormData(Map(
          "code" -> authCode,
          "client_id" -> config.getString("oauth2.clientId"),
	      "client_secret" -> config.getString("oauth2.clientSecret"),
	      "redirect_uri" -> config.getString("oauth2.redirectUri"),
	      "grant_type" -> "authorization_code"))
	    ) match {
          case Right(httpEntity) => httpEntity
          case Left(_) => HttpEntity.Empty 
        }
    val unmarshaller = Some( (response:String) => {
	  val json = parse(response)
	  implicit val format = org.json4s.DefaultFormats
	  val accessToken = (json \\ "access_token").extract[String]
	  val refreshToken = (json \\ "refresh_token").extract[String]
	  val expiresIn = (json \\ "expires_in").extract[Long]
	  val now = System.currentTimeMillis / 1000L
	  
	  OAuth2Identity(accessToken, refreshToken, now + expiresIn)
    })
      
  } 
  
  case class ValidateToken(token: OAuth2Identity)
    (implicit val config: Config) extends GmailRestRequest {
      
    val uri = "https://www.googleapis.com/oauth2/v1/tokeninfo"
    val method = HttpMethods.POST 
    val credentials = None
    val entity : HttpEntity = marshal(FormData(Map(
          "access_token" -> token.accessToken ))
	    ) match {
          case Right(httpEntity) => httpEntity
          case Left(_) => HttpEntity.Empty 
        }
    val unmarshaller = Some((response:String) => {
	  implicit val format = org.json4s.DefaultFormats
	  val json = parse(response)
	  val expiresIn = (json \\ "expires_in").extract[Long]
	  val now = System.currentTimeMillis / 1000L
	  OAuth2Identity(
	        token.accessToken,
	        token.refreshToken,
	        now + expiresIn,
	        Some((json \\ "user_id").extract[String]), 
	        Some((json \\ "email").extract[String]), 
	        (json \\ "scope").extract[String].split(" "),
	        token.name,
	        token.givenName,
	        token.familyName,
	        token.picture, 
	        token.gender,
	        token.locale)
    })
  }
   
  case class RefreshToken(token: OAuth2Identity) 
    (implicit val config: Config) extends GmailRestRequest {
    
    val uri = "https://accounts.google.com/o/oauth2/token"
    val method = HttpMethods.POST 
    val credentials = None
    val entity : HttpEntity = marshal(FormData(Map(
          "refresh_token" -> token.refreshToken,
          "client_id" -> config.getString("oauth2.clientId"),
	      "client_secret" -> config.getString("oauth2.clientSecret"),
	      "grant_type" -> "refresh_token"))
	    ) match {
          case Right(httpEntity) => httpEntity
          case Left(_) => HttpEntity.Empty 
        }
    val unmarshaller = Some((response:String) => {
	  implicit val format = org.json4s.DefaultFormats
	  val json = parse(response)
	  val accessToken = (json \\ "access_token").extract[String]
	  val expiresIn = (json \\ "expires_in").extract[Long]
	  val now = System.currentTimeMillis / 1000L

	  OAuth2Identity(
	    accessToken,
	    token.refreshToken,
	    now + expiresIn,
	    token.userId, 
	    token.email, 
	    token.scope,
		token.name,
	    token.givenName,
	    token.familyName,
	    token.picture, 
	    token.gender,
	    token.locale)
    })
  }
  
  case class GetUserInfo(token: OAuth2Identity)
    (implicit val config: Config) extends GmailRestRequest {
      
    val uri = Uri("https://www.googleapis.com/oauth2/v1/userinfo") withQuery (
          "access_token" -> token.accessToken) toString
    val method = HttpMethods.GET
    val credentials = None
    val entity = HttpEntity.Empty
    val unmarshaller = Some((response:String) => {
	  implicit val format = org.json4s.DefaultFormats
	  val json = parse(response)
	  val now = System.currentTimeMillis / 1000L
	  OAuth2Identity(
	        token.accessToken,
	        token.refreshToken,
	        token.expiration,
	        Some((json \\ "id").extract[String]), 
	        Some((json \\ "email").extract[String]), 
	        token.scope,
	        Some((json \\ "name").extract[String]), 
	        Some((json \\ "given_name").extract[String]), 
	        Some((json \\ "family_name").extract[String]), 
	        Some((json \\ "picture").extract[String]), 
	        Some((json \\ "gender").extract[String]), 
	        Some((json \\ "locale").extract[String]) )
    })
  }
  

}