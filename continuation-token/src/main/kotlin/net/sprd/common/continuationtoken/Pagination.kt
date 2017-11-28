@file:JvmName("Pagination")

package net.sprd.common.continuationtoken

import java.util.LinkedList
import java.util.zip.CRC32

//TODO implement checksum fallback

fun <P : Pageable> createPage(entities: List<P>, previousToken: ContinuationToken?, pageSize: Int): Page<P> {
    if (entities.isEmpty()) {
        return createEmptyPage()
    }

    if (previousToken == null) {
        return createInitialPage(entities, pageSize)
    }

    // don't skip if the next page starts with a different timestamp
    val timestampsDiffer = entities.first().getTimestamp() != previousToken.timestamp
    if (timestampsDiffer) {
        return createOffsetPage(entities, 0, pageSize)
    }

    return createOffsetPage(entities, previousToken.offset, pageSize)
}

private fun <P : Pageable> createEmptyPage(): Page<P> = Page(listOf(), null)
private fun <P : Pageable> createLastPage(entities: List<P>): Page<P> = Page(entities, null)
private fun <P : Pageable> createInitialPage(entities: List<P>, pageSize: Int): Page<P> = createOffsetPage(entities, 0, pageSize)

internal fun <P : Pageable> createOffsetPage(entities: List<P>, offset: Int, pageSize: Int): Page<P> {
    val entitiesOffset = skipOffset(entities, offset)
    if (isEndOfFeed(entitiesOffset, pageSize)) {
        return createLastPage(entitiesOffset)
    }
    val latestEntities = getLatestEntities(entities)
    val latestTimeStamp = latestEntities.last().getTimestamp()
    val token = createToken(latestEntities.ids(), latestTimeStamp, offset = latestEntities.size)
    return Page(entitiesOffset, token)
}

private fun isEndOfFeed(entities: List<Pageable>, pageSize: Int) = entities.size < pageSize
private fun <P : Pageable> skipOffset(entities: List<P>, offset: Int) = entities.subList(offset, entities.size)

fun List<Pageable>.ids(): List<String> = this.map(Pageable::getID)

fun calculateQueryAdvice(token: ContinuationToken?, pageSize: Int): QueryAdvice {
    token ?: return QueryAdvice(limit = pageSize, timestamp = 0)
    return QueryAdvice(limit = token.offset + pageSize, timestamp = token.timestamp)
}

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

