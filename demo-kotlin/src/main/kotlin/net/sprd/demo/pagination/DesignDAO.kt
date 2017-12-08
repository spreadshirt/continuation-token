package net.sprd.demo.pagination

import net.sprd.common.continuationtoken.ContinuationToken
import net.sprd.common.continuationtoken.Page
import net.sprd.common.continuationtoken.calculateQueryAdvice
import net.sprd.common.continuationtoken.createPage
import org.springframework.jdbc.core.JdbcTemplate
import java.sql.ResultSet
import javax.sql.DataSource


class DesignDAO(dataSource: DataSource) {

    private val template = JdbcTemplate(dataSource)

    fun getDesigns(token: ContinuationToken?, pageSize: Int): Page<DesignEntity> {
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
