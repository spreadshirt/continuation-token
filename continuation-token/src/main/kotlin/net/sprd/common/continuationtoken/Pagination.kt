@file:JvmName("Pagination")

package net.sprd.common.continuationtoken

import java.util.LinkedList
import java.util.zip.CRC32

/**
 * Creates a page for the given entities, the previous token and the page size.
 * @param entities as received from the database using the query advice. The list must be ordered by the timestamp and the id.
 * @param previousToken the continuation token of the last page that was used to retrieve the entities from the database.
 * @param pageSize the required page size
 * @return a page containing:
 * - the actual entities of the page. It may contain not every entities given in the parameter. In this case, the offset has been applied.
 * - a new continuation token. The token can be null if there is no next page.
 */
fun <P : Pageable> createPage(entities: List<P>, previousToken: ContinuationToken?, pageSize: Int): Page<P> {
    if (entities.isEmpty()) {
        return createEmptyPage(previousToken)
    }

    if (previousToken == null) {
        return createInitialPage(entities, pageSize)
    }

    // don't skip if the next page starts with a different timestamp
    val timestampsDiffer = entities.first().getTimestamp() != previousToken.timestamp
    val checksumsDiffer = checksumsAreUnequal(previousToken, entities)
    if (timestampsDiffer || checksumsDiffer) {
        return createOffsetPage(entities, 0, pageSize)
    }
    return createOffsetPage(entities, previousToken.offset, pageSize)
}

/**
 * Calculates the Query Advice based on a continuation token and the required page size.
 * The returned QueryAdvice object contains the values for limit and timestamp
 * that have to be used in the query.
 *
 * Example query:
 *
 * ```sql
 * SELECT * FROM Entities WHERE UNIX_TIMESTAMP(timestamp) >= :timestamp ORDER BY timestamp, id ASC LIMIT :limit
 * ```
 */
fun calculateQueryAdvice(token: ContinuationToken?, pageSize: Int): QueryAdvice {
    token ?: return QueryAdvice(limit = pageSize, timestamp = 0)
    return QueryAdvice(limit = token.offset + pageSize, timestamp = token.timestamp)
}

/**
 *
 * @param left
 * @param right
 * @return
 * - a negative integer if left < right
 * - zero if left == right
 * - a positive integer if left > right
 */
fun <T : Pageable> compareByDateModifiedAndIdAscending(left: T, right: T): Int {
    val timeDelta = left.getTimestamp().compareTo(right.getTimestamp())
    return when {
        timeDelta == 0 -> left.getID().compareTo(right.getID())
        else -> timeDelta
    }
}

/**
 * Returns true if checksums do not match.
 *
 * Checksums are calculated from a slice of entities that represents those with same timestamp of the last page, i.e.
 * those which were used to calculate the checksum of the previous page.
 * The checksums will not match if the elements skipped by offset differ since the last page query.
 */
private fun <P : Pageable> checksumsAreUnequal(previousToken: ContinuationToken, entities: List<P>): Boolean {
    val checksumSlice = entities.subList(0, previousToken.offset)
    return previousToken.checksum != createTokenFromEntities(checksumSlice)?.checksum
}

private fun <P : Pageable> createEmptyPage(token: ContinuationToken?): Page<P> = Page(listOf(), token, false)
private fun <P : Pageable> createLastPage(entities: List<P>, token: ContinuationToken?): Page<P> = Page(entities, token, false)
private fun <P : Pageable> createInitialPage(entities: List<P>, pageSize: Int): Page<P> = createOffsetPage(entities, 0, pageSize)

internal fun <P : Pageable> createOffsetPage(entities: List<P>, offset: Int, pageSize: Int): Page<P> {
    val entitiesOffset = skipOffset(entities, offset)
    if (isEndOfFeed(entitiesOffset, pageSize)) {
        return createLastPage(entitiesOffset, createTokenFromEntities(entities))
    }
    return Page(entitiesOffset, createTokenFromEntities(entities), hasNext = true)
}

private fun isEndOfFeed(entities: List<Pageable>, pageSize: Int) = entities.size < pageSize
private fun <P : Pageable> skipOffset(entities: List<P>, offset: Int) = entities.subList(offset, entities.size)

private fun List<Pageable>.ids(): List<String> = this.map(Pageable::getID)

internal fun createToken(ids: List<String>,
                         latestTimeStamp: Long,
                         offset: Int): ContinuationToken? {
    val checksum = createCRC32Checksum(ids)
    return ContinuationToken(
            timestamp = latestTimeStamp,
            offset = offset,
            checksum = checksum
    )
}

internal fun createTokenFromEntities(entities: List<Pageable>): ContinuationToken? {
    val latestEntities = getLatestEntities(entities)
    if (latestEntities.isEmpty()) {
        return null
    }
    return createToken(latestEntities.ids(), latestEntities.last().getTimestamp(), latestEntities.size)
}

private fun createCRC32Checksum(ids: List<String>): Long {
    val hash = CRC32()
    hash.update(ids.joinToString("_").toByteArray())
    return hash.value
}

internal fun getLatestEntities(entities: List<Pageable>): List<Pageable> {
    if (entities.isEmpty()) {
        return listOf()
    }
    val lastEntity = entities.last()
    val entitiesSharingNewestTimestamp = LinkedList<Pageable>()
    entities.asReversed()
            .takeWhile { it.getTimestamp() == lastEntity.getTimestamp() }
            .forEach { entitiesSharingNewestTimestamp.push(it) }
    return entitiesSharingNewestTimestamp
}
