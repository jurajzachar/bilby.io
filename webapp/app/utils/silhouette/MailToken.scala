package utils.silhouette

import org.joda.time.DateTime

/**
 * @author juri
 *
 */
trait MailToken {
/**
 * @return
 */
def id: String
/**
 * @return
 */
def email: String
/**
 * @return
 */
def expirationTime: DateTime
/**
 * @return
 */
def isExpired = expirationTime.isBeforeNow
}