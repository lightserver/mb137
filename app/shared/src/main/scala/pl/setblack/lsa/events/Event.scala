package pl.setblack.lsa.events

/**
 * Event with some content.
 *
 * Events  go to Domains.
 */
class Event[T](
                val content: T,
                val id: Long,
                val sender: Long) {
}



