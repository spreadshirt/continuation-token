package net.sprd.demo.pagination

import net.sprd.common.continuationtoken.Pageable
import java.time.Instant

data class DesignEntity(
        val id: String,
        val title: String,
        val imageUrl: String,
        val dateModified: Instant
): Pageable {
    override fun getID() = id
    override fun getTimestamp() = dateModified.epochSecond
}