package com.spreadshirt.demo.pagination.util

import org.springframework.jdbc.core.JdbcTemplate
import java.time.Instant
import javax.sql.DataSource

class DesignCreator(dataSource: DataSource) {

    private val utilTemplate = JdbcTemplate(dataSource)

    fun createDesigns(amount: Int, startDate: Instant = Instant.now()) {
        val values = (1..amount).mapIndexed { i, _ ->
            arrayOf(
                    i,
                    "Cat $i",
                    "http://domain.de/cat$i.jpg",
                    startDate.plusSeconds(i.toLong()).epochSecond
            )
        }
        utilTemplate.batchUpdate("INSERT INTO designs (id, title, imageUrl, dateModified) VALUES (?, ?, ?, FROM_UNIXTIME(?))", values)
    }
}