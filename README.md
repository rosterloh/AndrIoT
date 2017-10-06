# AndrIoT

Android Things application for Raspberry Pi 3

![Rpi3](https://developer.android.com/things/images/raspberry-pi-3-board.png)

## Hardware Pinout

![Pinout](https://developer.android.com/things/images/pinout-raspberrypi.png)

## Cloud IoT

Creating a registry

    gcloud beta iot registries create --region=REGION
    
Creating a device

    gcloud beta iot registries create --region=REGION --registry=REGISTRY

Creating a PubSub topic

    gcloud beta pubsub subscriptions create projects/<PROJECT>/subscriptions/<SUBSCRIPTION> --topic <TOPIC>
    
Subscribing to topics

    gcloud beta pubsub subscriptions pull --auto-ack projects/<PROJECT>/subscriptions/<SUBSCRIPTION>