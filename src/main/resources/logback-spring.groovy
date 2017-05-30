scan()

appender("CONSOLE", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "| %highlight(%-5level) | %d{HH:mm:ss.SSS} | %logger - %msg%n"
    }
}


logger("org.oasis_eu.spring.kernel.security", DEBUG)
logger("org.ozwillo.dcimporter", DEBUG)
logger("org.oasis_eu.spring.util.KernelLoggingInterceptor", DEBUG) // ERROR, WARN (prod), INFO (preprod, dev), DEBUG 
logger("kernelLogging.logFullErrorResponses", DEBUG) // DEBUG logs any response, INFO only errors

root(WARN, ["CONSOLE"])