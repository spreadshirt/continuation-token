@file:JvmName("Pagination")

package net.sprd.common.continuationtoken

import java.util.LinkedList
import java.util.zip.CRC32

//TODO implement checksum fallback

fun createPage(entities: List<Pageable>, previousToken: ContinuationToken?, requiredPageSize: Int): Page {
    if (entities.isEmpty()) {
        return Page(entities = listOf(), token = null)
    }
    if (previousToken == null || currentPageStartsWithADifferentTimestampThanInToken(entities, previousToken)) {
        //don't skip
        val token = createTokenForPage(entities, entities, requiredPageSize)
        return Page(entities = entities, token = token)
    }

    val entitiesForNextPage = skipOffset(entities, previousToken)
    val token = createTokenForPage(entities, entitiesForNextPage, requiredPageSize)
    return Page(entities = entitiesForNextPage, token = token)
}

private fun fillUpWholePage(entities: List<Pageable>, requiredPageSize: Int): Boolean =
        entities.size >= requiredPageSize

private fun currentPageStartsWithADifferentTimestampThanInToken(allEntitiesSinceIncludingTs: List<Pageable>, previousToken: ContinuationToken): Boolean {
    val timestampOfFirstElement = allEntitiesSinceIncludingTs.first().getTimestamp()
    return timestampOfFirstElement != previousToken.timestamp
}

fun calculateQueryAdvice(token: ContinuationToken?, pageSize: Int): QueryAdvice {
    token ?: return QueryAdvice(limit = pageSize, timestamp = 0)
    return QueryAdvice(limit = token.offset + pageSize, timestamp = token.timestamp)
}

private fun skipOffset(entitiesSinceIncludingTs: List<Pageable>, token: ContinuationToken) =
        entitiesSinceIncludingTs.subList(token.offset, entitiesSinceIncludingTs.size)

/**
 * @param entitiesForNextPage includes skip/offset
 */
internal fun createTokenForPage(allEntitiesSinceIncludingTs: List<Pageable>,
                                entitiesForNextPage: List<Pageable>,
                                requiredPageSize: Int): ContinuationToken? {
    if (allEntitiesSinceIncludingTs.isEmpty()) {
        return null
    }
    if (!fillUpWholePage(entitiesForNextPage, requiredPageSize)) {
        return null // no next token required
    }
    val highestEntities = getEntitiesWithHighestTimestamp(allEntitiesSinceIncludingTs)
    val highestTimestamp = highestEntities.last().getTimestamp()
    val ids = highestEntities.map(Pageable::getID)
    val checksum = createCRC32Checksum(ids)
    return ContinuationToken(
            timestamp = highestTimestamp,
            offset = highestEntities.size,
            checksum = checksum
    )
}

private fun createCRC32Checksum(ids: List<String>): Long {
    val hash = CRC32()
    hash.update(ids.joinToString("_").toByteArray())
    return hash.value
}

internal fun getEntitiesWithHighestTimestamp(entities: List<Pageable>): List<Pageable> {
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
