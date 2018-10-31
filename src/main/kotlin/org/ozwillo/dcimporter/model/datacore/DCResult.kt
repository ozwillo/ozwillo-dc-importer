package org.ozwillo.dcimporter.model.datacore

import org.springframework.http.HttpStatus

open class DCResult(status: HttpStatus)

class DCResultSingle(status: HttpStatus, val resource: DCResourceLight) : DCResult(status)

class DCResultError(status: HttpStatus, vararg errors: String) : DCResult(status)