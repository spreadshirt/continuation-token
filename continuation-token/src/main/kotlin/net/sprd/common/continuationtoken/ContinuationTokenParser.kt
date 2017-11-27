@file:JvmName("ContinuationTokenParser")
package net.sprd.common.continuationtoken

val TOKEN_DELIMITER = ":"

fun parse(tokenString: String): ContinuationToken {
    val parts = tokenString.split(TOKEN_DELIMITER)
    try {
        return ContinuationToken(
                timestamp = parts[0].toLong(),
                offset = parts[1].toInt(),
                checksum = parts[2].toLong()
        )
    } catch (ex: Exception) {
        throw ContinuationTokenParseException("Invalid token '$tokenString'.", ex)
    }
}

class ContinuationTokenParseException(message: String, cause: Exception) : RuntimeException(message, cause)