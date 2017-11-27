package net.sprd.demo.pagination

import net.sprd.common.continuationtoken.ContinuationToken
import net.sprd.common.continuationtoken.calculateQueryAdvice
import net.sprd.common.continuationtoken.createPage
import org.springframework.jdbc.core.JdbcTemplate
import java.sql.ResultSet
import javax.sql.DataSource


class DesignDAO(dataSource: DataSource) {

    private val template = JdbcTemplate(dataSource)

    fun getDesigns(token: ContinuationToken?, pageSize: Int): DesignPageEntity {
        val queryAdvice = calculateQueryAdvice(token, pageSize)
        val sql = """SELECT * FROM designs
            WHERE UNIX_TIMESTAMP(dateModified) >= ${queryAdvice.timestamp}
            ORDER BY dateModified asc, id asc
            LIMIT ${queryAdvice.limit};"""
        val designs = template.query(sql, this::mapToDesign)
        val nextPage = createPage(designs, token, pageSize)
        return DesignPageEntity(nextPage.entities as List<DesignEntity>, nextPage.token)
    }

    private fun mapToDesign(rs: ResultSet, rowNum: Int) = DesignEntity(
            id = rs.getString("id"),
            title = rs.getString("title"),
            imageUrl = rs.getString("imageUrl"),
            dateModified = rs.getTimestamp("dateModified").toInstant()
    )
}

data class DesignPageEntity(
        val designs: List<DesignEntity>,
        val token: ContinuationToken?
)
