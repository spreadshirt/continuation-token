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
        return ContinuationToken(
                timestamp = parts[0].toLong(),
                offset = parts[1].toInt(),
                checksum = parts[2].toLong()
        )
    } catch (ex: Exception) {
        throw InvalidContinuationTokenException("Invalid token '$this'.", ex)
    }
}

class InvalidContinuationTokenException(message: String, cause: Exception) : RuntimeException(message, cause)