package org.ozwillo.dcimporter.service.initializer

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.ozwillo.dcimporter.repository.BusinessAppConfigurationRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest
@ActiveProfiles(profiles = ["test", "dev"])
class PublikInitializerTest {

    @Autowired
    lateinit var publikInitializer: PublikInitializer

    @Autowired
    lateinit var businessAppConfigurationRepository: BusinessAppConfigurationRepository

    @Test
    fun testCorrectInitialization() {
        val initialCount = businessAppConfigurationRepository.count().block()!!
        assertTrue(initialCount > 0, "Default configurations should have been bootstrapped")
        publikInitializer.init()
        assertEquals(initialCount, businessAppConfigurationRepository.count().block()!!,
                "A manual launch of the initializer should not create a new configuration")
    }
}
