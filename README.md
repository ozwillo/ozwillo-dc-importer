# DC Importer

DC Importer is a data synchronizer allowing external application to communicate with the [Ozwillo Datacore](https://github.com/ozwillo/ozwillo-datacore)

### Features

* Integration of _Marchés Publics_
* Integration of _Maarch_
* Integration of _Publik_
* Integration of _Marchés Sécurisés_
* Integration of IoT data sent in the SenML format

### Infos

Activated actuator endpoints : 

* Info : `http --json GET http://{host}/actuator/info Authorization:"Basic Base64(login:password)"`
  * Provide basic information about the application
* Health : `http --json GET http://{host}/actuator/health Authorization:"Basic Base64(login:password)"`
  * Display application health status
* Httptrace : `http --json GET http://{host}/actuator/httptrace Authorization:"Basic Base64(login:password)"`
  * Display last 100 HTTP requests

### Needed

Following external applications and plugins are needed for the DC Importer application to work : 

* [RabbitMQ](http://www.rabbitmq.com/)
  * Recommended version : 3.7.6+
  * Required plugins : `rabbitmq_management`, `rabbitmq_shovel` and `rabbitmq_shovel_management`, `rabbitmq_mqtt`
* [Erlang](http://www.erlang.org/downloads)
  * Recommended version : 8.3 (RabbitMQ dependency)
