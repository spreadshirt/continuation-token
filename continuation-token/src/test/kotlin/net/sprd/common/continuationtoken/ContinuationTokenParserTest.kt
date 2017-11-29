package net.sprd.common.continuationtoken

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ContinuationTokenParserTest {

    @Test
    fun valid() {
        assertThat("1511443755_2_1842515611".toContinuationToken())
                .isEqualTo(ContinuationToken(timestamp = 1511443755, offset = 2, checksum = 1842515611))
        assertThat("1511443755_1_1842521611".toContinuationToken())
                .isEqualTo(ContinuationToken(timestamp = 1511443755, offset = 1, checksum = 1842521611))

        //also support timestamps with millisecond precision
        assertThat("1511443755999_1_1842521611".toContinuationToken())
                .isEqualTo(ContinuationToken(timestamp = 1511443755999, offset = 1, checksum = 1842521611))
    }

    @ParameterizedTest
    @MethodSource("invalidTokenProvider")
    fun invalid(invalidToken: String) {
        val exception = assertThrows(InvalidContinuationTokenException::class.java) {
            invalidToken.toContinuationToken()
        }
        assertThat(exception.message).isEqualTo("Invalid token '$invalidToken'.")
    }

    private fun invalidTokenProvider(): Stream<String> = Stream.of(
            "asdf_1_1842521611"
            , "1511443755_sadfasd_1842521611"
            , "1511443755_1_sadfasd"
            , "1511443755_1"
            , "1511443755_1_"
            , ""
            , "__"
            , "12__"
            , "12__213"
            , "_1231_213"
    )
}
