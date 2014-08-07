#!/bin/sh

${RABBITMQCTL:="sudo rabbitmqctl"}
${RABBITMQ_PLUGINS:="sudo rabbitmq-plugins"}

# guest:guest has full access to /

$RABBITMQCTL add_vhost /
$RABBITMQCTL add_user guest guest
$RABBITMQCTL set_permissions -p / guest ".*" ".*" ".*"


# green_bunny:green_bunny_password has full access to bunny_testbed

$RABBITMQCTL add_vhost bunny_testbed
$RABBITMQCTL add_user green_bunny green_bunny_password
$RABBITMQCTL set_permissions -p bunny_testbed green_bunny ".*" ".*" ".*"


# guest:guest has full access to bunny_testbed

$RABBITMQCTL set_permissions -p bunny_testbed guest ".*" ".*" ".*"


# green_bunny_reader:reader_password has read access to bunny_testbed

$RABBITMQCTL add_user green_bunny_reader reader_password
$RABBITMQCTL set_permissions -p bunny_testbed green_bunny_reader "^---$" "^---$" ".*"

# requires RabbitMQ 3.0+
# $RABBITMQ_PLUGINS enable rabbitmq_consistent_hash_exchange
