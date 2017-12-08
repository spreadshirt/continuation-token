package net.sprd.demo.pagination

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import net.sprd.common.continuationtoken.toContinuationToken
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status

class DesignResource(private val dao: DesignDAO) {

    //TODO add modified_since parameter

    fun getDesigns(request: Request): Response {
        val token = request.query("continuationToken")?.toContinuationToken()
        val pageSize = request.query("pageSize")?.toInt() ?: 3
        val page = dao.getDesigns(token, pageSize)
        val dto = PageDTO(
                results = page.entities.map(::mapToDTO),
                continuationToken = page.token?.toString(),
                nextPage = page.token?.let { "http://localhost:8000/designs?pageSize=$pageSize&continuationToken=${page.token}" }
        )
        return Response(Status.OK)
                .header("Content-Type", "application/json;charset=UTF-8")
                .body(dto.toJson())
    }
}

private fun mapToDTO(entity: DesignEntity) = DesignDTO(
        id = entity.id,
        title = entity.title,
        imageUrl = entity.imageUrl,
        dateModified = entity.dateModified.epochSecond
)

data class DesignDTO(
        val id: String,
        val title: String,
        val imageUrl: String,
        val dateModified: Long
)

data class PageDTO(
        val results: List<DesignDTO>,
        val continuationToken: String?,
        val nextPage: String?
)

private val mapper = jacksonObjectMapper()
private fun Any.toJson() = mapper.writeValueAsString(this)

