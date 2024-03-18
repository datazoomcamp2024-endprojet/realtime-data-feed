## how to run the app in isolation 
using docker in order to provide a working environment without pre-installation like java, etcc..

1- build artifact ( jar file)
2- build and run the docker containers

### build artifact
#### building the consumer
to build the consumer executable
``./gradlew build -PcustomName=consumer -PmainClass=org.example.JsonConsumer``

### start the service using remote kafka cluster
the service recquires some secrets like cluster key etc...
you need to create a env file that contains those secret then build and run the containers

#### create env file
use the template given from the ``env`` file to create your own ``.env``
``cp env .env``

#### start the containers
``docker-compose build --no-cache``
``docker-compose up``

### to check kafka cluster info
http://localhost:9021/
