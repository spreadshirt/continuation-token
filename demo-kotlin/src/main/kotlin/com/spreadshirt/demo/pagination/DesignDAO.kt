package com.spreadshirt.demo.pagination

import com.spreadshirt.continuationtoken.ContinuationToken
import com.spreadshirt.continuationtoken.Page
import com.spreadshirt.continuationtoken.calculateQueryAdvice
import com.spreadshirt.continuationtoken.createPage
import org.springframework.jdbc.core.JdbcTemplate
import java.sql.ResultSet
import java.time.Instant
import javax.sql.DataSource


class DesignDAO(dataSource: DataSource) {

    private val template = JdbcTemplate(dataSource)

    fun getDesignsSince(modifiedSince: Instant, pageSize: Int): Page<DesignEntity> {
        val sql = """SELECT * FROM designs
            WHERE dateModified >= FROM_UNIXTIME(${modifiedSince.epochSecond})
            ORDER BY dateModified asc, id asc
            LIMIT $pageSize;"""
        val designs = template.query(sql, this::mapToDesign)
        return createPage(designs, null, pageSize)
    }

    fun getNextDesignPage(token: ContinuationToken?, pageSize: Int): Page<DesignEntity> {
        val queryAdvice = calculateQueryAdvice(token, pageSize)
        val sql = """SELECT * FROM designs
            WHERE dateModified >= FROM_UNIXTIME(${queryAdvice.timestamp})
            ORDER BY dateModified asc, id asc
            LIMIT ${queryAdvice.limit};"""
        val designs = template.query(sql, this::mapToDesign)
        return createPage(designs, token, pageSize)
    }

    private fun mapToDesign(rs: ResultSet, rowNum: Int) = DesignEntity(
            id = rs.getString("id"),
            title = rs.getString("title"),
            imageUrl = rs.getString("imageUrl"),
            dateModified = rs.getTimestamp("dateModified").toInstant()
    )
}
