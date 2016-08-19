#!/usr/bin/env bash
aws cloudformation create-stack --stack-name ical2json-ecs --template-body file://$1