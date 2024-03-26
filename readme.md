## build artifacts
#### build the consumer
to build the consumer executable
``./gradlew clean build -PcustomName=consumer -PmainClass=org.example.OneMinuteWindowKstream``


## run vs prod kafka and db

IMPORTANT : If you don't have a kafka cluster running in confluent or other cloud provider, please follow the "infrastructure as code" section to create your kafka cluster in confluence cloud before continuing.

the service recquires some secrets like cluster key etc...
you need to create a env file that contains those secret then build and run the containers

### create env file
use the template given from the ``env`` file to create your own ``.env``
``cp env .env``
replace cluster key, secret and url with yours ( created after applying terraform plan)

### Run producer against remote kafka
using docker in order to provide a working environment for python

1- build image ``docker-compose -f docker-compose-prod.yml build --no-cache producer``
2- build and run the docker container ``docker-compose -f docker-compose-prod.yml up producer``

### Run consumer against remote kafka
1- build image ``docker-compose -f docker-compose-prod.yml build --no-cache consumer``
2- execute ``docker-compose -f docker-compose-prod.yml up consumer``

### Run producer and consumer against remote kafka
1- build images ``docker-compose -f docker-compose-prod.yml build``
2 - execute ``docker-compose -f docker-compose-prod.yml up``



## infrastructure as a code
This section allows to recreate/update a kafka cluster in confluent cloud using terraform template.
This is important because keeping the definition of the infrastructure as code, will prevent infrastructure inconsistencies.

### create confluent cloud account
If you already have a confluent cloud account you can skip this step.

go to https://confluent.cloud/
and follow the steps to get your account working
you can skip the payment method and you will have a trial budget for a few week.

### install terraform
follow instruction here https://developer.hashicorp.com/terraform/install

### create a confluent api key and secret
this will be recquired for terraform to have access to your confluent could account
go to https://confluent.cloud/settings/api-keys/create?user_dir=ASC&user_sort=status and follow the procedure.

copy or download them but remind that it s security issue to keep them, but you will need them to run the terraform code.

### running terraform code
-   go in terraform/confluent-cloud folder
``cd terraform/confluent-cloud/``
-   initialise terraform
``terraform init``
-   check the planning 
``terraform plan``
a prompt will ask you for the key and secret you just created and your account id ( find here https://confluent.cloud/settings/org/accounts/users under ID column)
you can check the output it should display the list of component terraform is planning to create.
    -   a environment
    -   a kafka cluster
    -   some key to access it
    -   and 2 topics ( used by the apps in this repo)
-   apply the plan
``terraform apply``
and check that everything was created from https://confluent.cloud/environments
note that you will need the new created kafka cluster api key and secret for running apps
they can be found in terraform.tfstate file looking for `credentials` ( if you have jq installed execute ``terraform state pull | jq '.resources[] | select(.type == "confluent_kafka_topic") | .instances[0].attributes.credentials'``)



## run all in local
using docker in order to provide a working environment without pre-installation like java, etcc..

1- build artifact ( jar file)
2- build and run the docker containers

#### start the containers
``docker-compose build --no-cache``
``docker-compose up``

#### to check kafka cluster info
http://localhost:9021/