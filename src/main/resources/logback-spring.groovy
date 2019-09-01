scan()

appender("CONSOLE", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "| %highlight(%-5level) | [%thread] | %d{HH:mm:ss.SSS} | %logger - %msg%n"
    }
}

logger("org.ozwillo.dcimporter", DEBUG)
logger("org.springframework.web.client", WARN)
logger("org.springframework.web.reactive.function.client", WARN)
logger("reactor.ipc.netty", WARN)
logger("io.reactivex.netty.protocol.http.client", WARN)
logger("io.netty.handler", WARN)

root(WARN, ["CONSOLE"])
