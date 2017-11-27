@file:JvmName("ContinuationTokenParser")

package net.sprd.common.continuationtoken

internal val TOKEN_DELIMITER = ":"

/**
 * @throws InvalidContinuationTokenException if the string is not a valid token.
 */
@Throws(InvalidContinuationTokenException::class)
fun String.toContinuationToken(): ContinuationToken {
    val parts = this.split(TOKEN_DELIMITER)
    try {
        return buildAndValidate(parts[0].toLong(), parts[1].toInt(), parts[2].toLong())
    } catch (ex: Exception) {
        throw InvalidContinuationTokenException("Invalid token '$this'.", ex)
    }
}

fun buildAndValidate(timestamp: Long, offset: Int, checksum: Long): ContinuationToken {
    when {
        timestamp < 0 -> throw IllegalArgumentException("Timestamp is negative")
        offset < 0 -> throw IllegalArgumentException("Checksum is negative")
        else -> return ContinuationToken(timestamp, offset, checksum)
    }
}

class InvalidContinuationTokenException(message: String, cause: Exception) : RuntimeException(message, cause)