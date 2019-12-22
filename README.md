[![Build Status](https://travis-ci.org/ozwillo/ozwillo-dc-importer.svg?branch=develop)](https://travis-ci.org/ozwillo/ozwillo-dc-importer)
[![ktlint](https://img.shields.io/badge/code%20style-%E2%9D%A4-FF4081.svg)](https://ktlint.github.io/)

# DC Importer

DC Importer is a data synchronizer allowing external application to communicate with the [Ozwillo Datacore](https://github.com/ozwillo/ozwillo-datacore)

## Features

* Integration of _Maarch_
* Integration of _Publik_
* Integration of IoT data sent in the SenML format

## Infos

Activated actuator endpoints : 

* Info : `http http://{host}/actuator/info -a user:password`
  * Provide basic information about the application
* Health : `http http://{host}/actuator/health -a user:password`
  * Display application health status
* Httptrace : `http http://{host}/actuator/httptrace -a user:password`
  * Display last 100 HTTP requests

## Dependencies

Following external applications and plugins are needed for the DC Importer application to work (you can also use the provided `docker-compose.yml` file) : 

* [RabbitMQ](http://www.rabbitmq.com/)
  * Recommended version : 3.7.6+
  * Required plugins : `rabbitmq_management`, `rabbitmq_shovel`, `rabbitmq_shovel_management` and `rabbitmq_mqtt`
* [Erlang](http://www.erlang.org/downloads)
  * Recommended version : 8.3 (RabbitMQ dependency)
