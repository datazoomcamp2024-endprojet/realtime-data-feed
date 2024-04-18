# Bitcoin volume calculation realtime - (end project Datazoomcamp 2024)

## Problem statement

We want to calculate the volume of "BUY" and "SELL" transaction of bitcoin for specific period of times ( per minutes, hours, days...).

The collection of transaction can be done from different sources of data. In this Exemple we use Coinbase which is one of the largest centralized exchange of cryptocurrencies.

One of the challenge is the large amount of data :
the number of transactions can be huge.

So it is difficult to have aggregated data such as volume for short period of times without too much delay, or near real time.

The goal of this project is too show a way to address this issue.

1. By connecting to Coinbase through their websocket, we get transactions in real time. 
2. Transactions are injected into kafka, which process them in real time as well
3. The volume aggregation is done by using stateful streaming technique thanks to the kafka stream api

## Description of the data aggregation part ( kafka stream)

In order to get bitcoin transactions volume we need to define dimensions for our aggregation.

-   the first obvious one is the "side" of a transaction ( buy or sell)
-   the second one is the period of time for bucketting our transactions, also called a "time window" : In our case we limit to one minute windows

In summary, transactions are grouped by side and time window, for each group we sum the price of each transaction

After each kafka event is processed a new state of the volume for a specific side and time window is emitted, and stored in a database , so we can expose later the volumes for a large time scale.

for more detail check the java class [OneMinuteWindowKstream](src/main/java/org/example/OneMinuteWindowKstream.java) which contains the mechanic explained above.

## description of ingestion part ( websocket -> kafka)

This part is more obvious.
A websocket connection is attached to coinbase websoket endpoint , and each event received is broadcasted to a kafka topic.

please refer to [python connector](websocket/connect.py)


## Cloud based solution

The infrasctructures chosen to handle the data are big query in GCP and a kafka cluster in Confluent cloud

please refer to the terraform directory to check how the infrastructure definition of both [google cloud part](terraform/google-cloud) and [confluent cloud part](terraform/confluent-cloud).

for simplicity, there is no cloud infrasctructure definition or deployment to the cloud for the 2 apps that ( python and java part), so we will run them locally in docker.



## How to reproduce the solution 

### pre-requisite
install python3 and java11

### create cloud infrastructure

### install terraform
follow instruction here https://developer.hashicorp.com/terraform/install

### infrastructure as a code - kafka
This section allows to recreate/update a kafka cluster in confluent cloud using terraform template.

#### create confluent cloud account
If you already have a confluent cloud account you can skip this step.

go to https://confluent.cloud/
and follow the given instructions to get your account working
you can skip the payment method and you will have a trial budget for a few week.


#### create a confluent api key and secret
this will be recquired for terraform to have access to your confluent could account
go to https://confluent.cloud/settings/api-keys/create?user_dir=ASC&user_sort=status and follow the procedure.

copy or download them but remind that it s security issue to keep them, but you will need them to run the terraform code.

#### running terraform code
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

### infrastructure as a code - big query
This section allows to recreate/update big query dataset in google cloud platform with terraform.

#### create GCP account
If you already have a google cloud account you can skip this step.

go to https://console.cloud.google.com/
and follow the given instructions to get your account working
you have to provide payment method BUT don't worry you will have a trial budget for 3 months ( it's advised to set up an budget alert since the solution provided is quite data intensive and could reach a certain amount of paid service if you keep it running for a few days without interuption).

configure gcloud cli ( required for terraform auth to gcp) https://cloud.google.com/sdk/docs/initializing


#### running terraform code
-   go in terraform/google-cloud folder
``cd terraform/google-cloud/``
-   initialise terraform
``terraform init``
-   check the planning 
``terraform plan``
-   apply the plan
``terraform apply``
and check that everything was created from https://console.cloud.google.com/bigquery

check also that the gcp-key.json as been create on your local , which is used by the java ( kafka stream) app to authenticate to gcp using the service account created in terraform


#### build the data aggregation part
to build the data consumer executable
``./gradlew clean build -PcustomName=consumer -PmainClass=org.example.OneMinuteWindowKstream``


## run local applications vs cloud based kafka and bigquery

### create env file
use the template given from the ``env`` file to create your own ``.env``
``cp env .env``
replace cluster key, secret and url with yours ( created after applying terraform plan) and your gcp project id

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


## run all in local kafka ( missing bigquery part)
if you want to use kafka locally  
using docker in order to provide a working environment without pre-installation like java, etcc..

1- build artifact ( jar file)
2- build and run the docker containers

#### start the containers
``docker-compose build --no-cache``
``docker-compose up``

#### to check kafka cluster info
http://localhost:9021/