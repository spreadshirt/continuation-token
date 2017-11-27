@file:JvmName("ContinuationTokenParser")
package net.sprd.common.continuationtoken

internal val TOKEN_DELIMITER = ":"

fun String.toContinuationToken(): ContinuationToken {
    val parts = this.split(TOKEN_DELIMITER)
    try {
        return ContinuationToken(
                timestamp = parts[0].toLong(),
                offset = parts[1].toInt(),
                checksum = parts[2].toLong()
        )
    } catch (ex: Exception) {
        throw ContinuationTokenParseException("Invalid token '$this'.", ex)
    }
}

class ContinuationTokenParseException(message: String, cause: Exception) : RuntimeException(message, cause)