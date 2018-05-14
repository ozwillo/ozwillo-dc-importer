package org.ozwillo.dcimporter.extensions

import org.mockito.ArgumentMatcher
import org.ozwillo.dcimporter.model.datacore.DCBusinessResourceLight

class DCBusinessResourceLightMatcher(private val dcBusinessResourceLight: DCBusinessResourceLight) :
        ArgumentMatcher<DCBusinessResourceLight> {

    override fun matches(argument: DCBusinessResourceLight?): Boolean {
        return if (argument == null)
            false
        else argument.getUri() == dcBusinessResourceLight.getUri()
    }
}