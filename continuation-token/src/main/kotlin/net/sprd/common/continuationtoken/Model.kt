package net.sprd.common.continuationtoken

/** a continuation token passed by the client to resume a pagination **/
data class ContinuationToken(
        /** latest timestamp of the previous page */
        val timestamp: Long,
        /** number of elements to be skipped in a new query */
        val offset: Int,
        /** used to detect modifications during pagination */
        val checksum: Long
) {
    override fun toString() = "$timestamp$TOKEN_DELIMITER$offset$TOKEN_DELIMITER$checksum"
}

data class QueryAdvice(
        /** entities with the same or newer timestamp must be queried */
        val timestamp: Long,
        val limit: Int
)

data class Page(
        val entities: List<Pageable>,
        val token: ContinuationToken?
)

fun EmptyPage(): Page {
    return Page(listOf(), null)
}

fun FullPage(entities: List<Pageable>, pageSize: Int): Page {
    if (isEndOfFeed(entities, pageSize)) {
        return Page(entities, null)
    }

    val latestEntities = getLatestEntities(entities)
    return Page(entities, createToken(latestEntities.ids(), latestEntities.last().getTimestamp(), latestEntities.size))
}

interface Pageable {
    fun getID(): String
    fun getTimestamp(): Long
}
