package org.ozwillo.dcimporter.service.rabbitMQ

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.ozwillo.dcimporter.util.BindingKeyAction
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.regex.Pattern

@ExtendWith(SpringExtension::class)
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SenderTest {

    @Autowired
    private lateinit var sender: Sender

    @Test
    fun `Assert true regular expression of get binding key`() {
        val bindingKeyPattern =
            Pattern.compile("^\\w+\\.[0-9]+\\.[a-z0-9_:]+\\.(?:create|update|delete)+$", Pattern.CASE_INSENSITIVE)
        val bindingKey =
            sender.getKey("marchepublic_0", "marchepublic_0:consultation", "123456789", BindingKeyAction.CREATE)
        val matcher = bindingKeyPattern.matcher(bindingKey)
        assertTrue(matcher.matches())
    }
}