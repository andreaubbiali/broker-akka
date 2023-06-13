package it.brokerakka.brokerakka.springboot_events

import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Service

/**
 * This class is annotated with Service so it is initialized automatically by springboot.
 * Furthermore it implements InitializingBean so we can override the afterPropertiesSet. This method
 * will be called automatically after the class has been created and variables are injected.
 */
@Service
class InitializationService : InitializingBean {

    public val prop: Int = 5

    init {
        println("Partenza")
    }

    override fun afterPropertiesSet() {
        println("AFTER PROPERTIES DONE")
    }
}