# javaps-wacodis-backend
![Build](https://github.com/WaCoDiS/javaps-wacodis-backend/workflows/Build/badge.svg)  
Processing backend for javaPS containing processes for WaCoDiS project

## Dockerized javaPS

### Building the image

For building a javaPS docker image run `docker build -t wacodis/javaps:latest --build-arg CACHE_DATE=$(date) .`.
The build time argument `CACHE_DATE` can be used to invalidate cache in order to only build the changed _javaps-wacodis-backend_.

### Configurations

Be sure to overwrite the properties in `wacdodis.env` just to provide the credentials for the Copernicus Open Access Hub and to set a working directory.

### Run the container

You can simply run the container with Compose. To enable memory configurations use the following command: `docker-compose --compatibility up`
If you prefer `docker run` you can also use `docker run -p 8080:8080 --env-file ./wacodis.env wacodis/javaps:1.x ` or execute a customized command.

### Execute WPS processes

To execute WPS processes send your requests to the following endpoint: http://localhost:8080/wacodis-javaps/service.
