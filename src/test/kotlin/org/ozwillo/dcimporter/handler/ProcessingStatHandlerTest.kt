package org.ozwillo.dcimporter.handler

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.ozwillo.dcimporter.model.ProcessingStat
import org.ozwillo.dcimporter.repository.ProcessingStatRepository
import org.ozwillo.dcimporter.service.ProcessingResumeFields
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProcessingStatHandlerTest (@Autowired private val restTemplate: TestRestTemplate){

    @Autowired
    private lateinit var processingStatRepository: ProcessingStatRepository

    @BeforeAll
    fun beforeAll(){
        processingStatRepository.save(ProcessingStat(
            id = "Test-id-01",
            model = "model",
            organization = "123456789",
            action = "create"
        )).subscribe()
    }

    @Test
    fun getAll(){
        val entity = restTemplate.getForEntity("/api/stat-view/processing", String::class.java)
        assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(entity.body).contains("Test-id-01")
    }

    @Test
    fun getGeneralResume(){
        val entity = restTemplate.getForEntity("/api/stat-view/processing-resume", ProcessingResumeFields::class.java)
        val responseBody = entity.body
        assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)
        println(responseBody!!.nbreProcessing)
        assertThat(responseBody.nbreProcessing).isEqualTo(1)
        assertThat(responseBody.nbreDistinctModel).isEqualTo(1)
        assertThat(responseBody.nbreDistinctOrg).isEqualTo(1)
        assertThat(responseBody.resumeByModel.size).isEqualTo(1)
        assertThat(responseBody.resumeByModel[0].modelName).isEqualTo("model")
        assertThat(responseBody.resumeByModel[0].nbreProcessing).isEqualTo(1)
        assertThat(responseBody.resumeByModel[0].nbreCreated).isEqualTo(1)
        assertThat(responseBody.resumeByModel[0].nbreOfUpdate).isEqualTo(0)
        assertThat(responseBody.resumeByModel[0].nbreDeleted).isEqualTo(0)
        assertThat(responseBody.resumeByModel[0].nbreActive).isEqualTo(1)
        assertThat(responseBody.resumeByModel[0].nbreOrganization).isEqualTo(1)
        assertThat(responseBody.resumeByOrganization.size).isEqualTo(1)
        assertThat(responseBody.resumeByOrganization[0].orgSiret).isEqualTo("123456789")
        assertThat(responseBody.resumeByOrganization[0].nbreProcessing).isEqualTo(1)
        assertThat(responseBody.resumeByOrganization[0].nbreDistinctModel).isEqualTo(1)
        assertThat(responseBody.resumeByOrganization[0].nbreOfCreation).isEqualTo(1)
        assertThat(responseBody.resumeByOrganization[0].nbreOfUpdate).isEqualTo(0)
        assertThat(responseBody.resumeByOrganization[0].nbreOfDeletion).isEqualTo(0)
        assertThat(responseBody.resumeByOrganization[0].nbreActiveModel).isEqualTo(1)
        assertThat(responseBody.resumeByOrganization[0].modelResume.size).isEqualTo(1)
        assertThat(responseBody.resumeByOrganization[0].modelResume[0].modelName).isEqualTo("model")
        assertThat(responseBody.resumeByOrganization[0].modelResume[0].nbreCreated).isEqualTo(1)
        assertThat(responseBody.resumeByOrganization[0].modelResume[0].nbreOfUpdate).isEqualTo(0)
        assertThat(responseBody.resumeByOrganization[0].modelResume[0].nbreDeleted).isEqualTo(0)
        assertThat(responseBody.resumeByOrganization[0].modelResume[0].nbreActive).isEqualTo(1)
        assertThat(responseBody.resumeByOrganization[0].modelResume[0].nbreOrganization).isEqualTo(1)


    }
}