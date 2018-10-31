package org.ozwillo.dcimporter.repository

import org.ozwillo.dcimporter.model.DataAccessRequest
import org.springframework.data.mongodb.repository.ReactiveMongoRepository

interface DataAccessRequestRepository: ReactiveMongoRepository<DataAccessRequest, String>