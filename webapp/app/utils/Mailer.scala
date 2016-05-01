package utils

import play.twirl.api.Html
import play.api.i18n.Messages
//import views.html.mails

object Mailer {

  //implicit def fromHtmlToString(html: Html): String = html.toString

  def welcome(username: String, email: String, link: String)(implicit ms: MailService, m: Messages) {
    ms.sendEmailAsync(email)(
      subject = Messages("mail.welcome.subject"),
      s"welcome $username",
      s"text and $link"
      //bodyHtml = mails.welcome(user.firstName, link),
      //bodyText = mails.welcomeTxt(user.firstName, link)
    )
  }

  def forgotPassword(email: String, link: String)(implicit ms: MailService, m: Messages) {
    ms.sendEmailAsync(email)(
      subject = Messages("mail.forgotpwd.subject"),
      s"email sent because requested",
      s"text and $link"
      //bodyHtml = mails.forgotPassword(email, link),
      //bodyText = mails.forgotPasswordTxt(email, link)
    )
  }

}