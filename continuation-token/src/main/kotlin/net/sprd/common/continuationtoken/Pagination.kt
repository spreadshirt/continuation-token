@file:JvmName("Pagination")

package net.sprd.common.continuationtoken

import java.util.LinkedList
import java.util.zip.CRC32

//TODO implement checksum fallback

fun createPage(entities: List<Pageable>, previousToken: ContinuationToken?, pageSize: Int): Page {
    if (entities.isEmpty()) {
        return EmptyPage()
    }

    if (previousToken == null) {
        return FullPage(entities, pageSize)
    }

    val offsetIsInvalid = previousToken.offset < 0 || previousToken.offset > entities.size
    val timestampsDiffer = entities.first().getTimestamp() != previousToken.timestamp
    if (offsetIsInvalid || timestampsDiffer) {
        return FullPage(entities, pageSize)
    }

    return createOffsetPage(entities, previousToken, pageSize)
}

internal fun createOffsetPage(entities: List<Pageable>, previousToken: ContinuationToken, pageSize: Int): Page {
    val entitiesOffset = skipOffset(entities, previousToken)
    if (isEndOfFeed(entitiesOffset, pageSize)) {
        return Page(entitiesOffset, null)
    }

    val latestEntities = getLatestEntities(entities)
    val latestTimeStamp = latestEntities.last().getTimestamp()
    val token = createToken(latestEntities.ids(), latestTimeStamp, offset = latestEntities.size)
    return Page(entitiesOffset, token)
}

fun isEndOfFeed(entities: List<Pageable>, pageSize: Int): Boolean = entities.size < pageSize

fun List<Pageable>.ids(): List<String> = this.map(Pageable::getID)

fun calculateQueryAdvice(token: ContinuationToken?, pageSize: Int): QueryAdvice {
    token ?: return QueryAdvice(limit = pageSize, timestamp = 0)
    return QueryAdvice(limit = token.offset + pageSize, timestamp = token.timestamp)
}

private fun skipOffset(entities: List<Pageable>, token: ContinuationToken) =
        entities.subList(token.offset, entities.size)

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

fun createTokenFromEntities(entities: List<Pageable>): ContinuationToken? {
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
